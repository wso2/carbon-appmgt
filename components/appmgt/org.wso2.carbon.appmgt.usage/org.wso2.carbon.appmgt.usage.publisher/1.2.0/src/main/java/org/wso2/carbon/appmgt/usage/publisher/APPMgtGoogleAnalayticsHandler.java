/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.appmgt.usage.publisher;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.appmgt.usage.publisher.internal.APPManagerConfigurationServiceComponent;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsConstants;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsDataPublisher;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class APPMgtGoogleAnalayticsHandler extends AbstractHandler {
    private static final Log log = LogFactory.getLog(APPMgtGoogleAnalayticsHandler.class);

    private boolean enabled = APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().
            isGoogleAnalyticsTrackingEnabled();

    private static final String PROTOCOL_VERSION = "1";

    private static final String COOKIE_NAME = "APPM_ANALYTICS_COOKIE";

    private String trackingID = APPManagerConfigurationServiceComponent.
            getApiMgtConfigReaderService().getGoogleAnalyticsTrackingID();

    private ExecutorService executor = Executors.newFixedThreadPool(1);

    public boolean handleRequest(MessageContext messageContext) {
        if(!enabled) {
            return true;
        }

        /** Get Header Map **/
        Map headers = (Map) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        /** Get document referer **/
        String documentReferer = (String) headers.get(HttpHeaders.REFERER);
        if(isEmpty(documentReferer)) {
            documentReferer = "";
        } else {
            try {
                documentReferer = new URI(documentReferer).getPath();
            } catch (URISyntaxException e) {
                log.error(e.getMessage(), e);
                return true;
            }
        }

        /** Get invoked Application Name **/
        String appName = APPMgtUsageUtils.getAppNameFromSynapseEnvironment(messageContext, documentReferer);
        String appBasedCookieName = COOKIE_NAME + "_" + appName;

        /** Get User Agent **/
        String userAgent = (String) headers.get(HttpHeaders.USER_AGENT);
        if (isEmpty(userAgent)) {
            userAgent = "";
        }

        /** Get Analytics Cookie **/
        String cookieString = (String) headers.get(HTTPConstants.COOKIE_STRING);
        String analyticCookie = findCookie(cookieString, appBasedCookieName);

        /** Retrieve or create client UUID - and store in MC to lookup in response path **/
        String uuid = null;
        try {
            uuid = getClientUUID(messageContext, userAgent, analyticCookie);
            messageContext.setProperty(appBasedCookieName, uuid);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return true;
        }

        /** Get host and domain name **/
        String host = (String) headers.get(HttpHeaders.HOST);
        String domainName = host;
        if (host != null && host.indexOf(":") != -1){
            domainName = host.substring(0, host.indexOf(":"));
        }
        if (isEmpty(domainName)) {
            domainName = "";
        }

        GoogleAnalyticsData data = null;
        data = new GoogleAnalyticsData
                .DataBuilder(trackingID, PROTOCOL_VERSION , uuid, GoogleAnalyticsConstants.HIT_TYPE_PAGEVIEW)
                .setDocumentPath(documentReferer)
                .setCacheBuster(getRandomNumber())
                .setDocumentHostName(host)
                .setClientId(uuid)
                .build();

        Runnable googleAnalyticsPublisher = new GoogleAnalyticsPublisher(data, userAgent);
        executor.execute(googleAnalyticsPublisher);

        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        /** TODO refactor so common parts with handleRequest are pulled out **/
        Map headers = (Map) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);


        /** Get document referer **/
        String documentReferer = (String) headers.get(HttpHeaders.REFERER);
        if(isEmpty(documentReferer)) {
            documentReferer = "";
        } else {
            try {
                documentReferer = new URI(documentReferer).getPath();
            } catch (URISyntaxException e) {
                log.error(e.getMessage(), e);
                return true;
            }
        }

        /** Get invoked Application Name **/
        String appName = APPMgtUsageUtils.getAppNameFromSynapseEnvironment(messageContext, documentReferer);
        String appBasedCookieName = COOKIE_NAME + "_" + appName;

        String cookieValue = (String) messageContext.getProperty(appBasedCookieName);
        if(cookieValue != null) {
            String cookieString = appBasedCookieName + "=" + cookieValue + "; " + "path=" + "/"  ;
            headers.put(HTTPConstants.HEADER_SET_COOKIE, cookieString);
        }

        return true;
    }

    private String findCookie(String cookieString, String cookieName) {
        String rawCookies[] = cookieString.split(";");
        List<HttpCookie> cookies = new ArrayList<HttpCookie>();
        for(String cookie: rawCookies) {
            String[] split = cookie.split("=");
            if(split.length >= 2) {
                HttpCookie c = new HttpCookie(split[0].trim(), split[1].trim());
                cookies.add(c);
            }
        }

        for(HttpCookie cookie: cookies) {
            if(cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private String getClientUUID(MessageContext synCtx, String userAgent, String analyticCookie) throws
            NoSuchAlgorithmException, UnsupportedEncodingException {

        if (analyticCookie != null) {
            if(log.isDebugEnabled()) {
                log.debug("Client UUID Exists: " + analyticCookie);
            }
            return analyticCookie;
        }

        String message;
        message = userAgent + getRandomNumber() + UUID.randomUUID().toString();

        MessageDigest m = MessageDigest.getInstance("MD5");
        m.update(message.getBytes("UTF-8"), 0, message.length());
        byte[] sum = m.digest();
        BigInteger messageAsNumber = new BigInteger(1, sum);
        String md5String = messageAsNumber.toString(16);

        /* Pad to make sure id is 32 characters long. */
        while (md5String.length() < 32) {
            md5String = "0" + md5String;
        }

        if(log.isDebugEnabled()) {
            log.debug("Client UUID: " + "0x" + md5String.substring(0, 16));
        }

        return "0x" + md5String.substring(0, 16);
    }

    /**
     * A string is empty in our terms, if it is null, empty or a dash.
     */
    private static boolean isEmpty(String in) {
        return in == null || "-".equals(in) || "".equals(in);
    }

    /**
     * Get a random number string.
     *
     * @return
     */
    private static String getRandomNumber() {
        return Integer.toString((int) (Math.random() * 0x7fffffff));
    }

    /**
     * Make a tracking request to Google Analytics from this server.
     *
     */
    private class GoogleAnalyticsPublisher implements Runnable {
        GoogleAnalyticsData data;
        String userAgent;

        public GoogleAnalyticsPublisher(GoogleAnalyticsData data, String userAgent) {
            this.data = data;
            this.userAgent = userAgent;
        }

        @Override
        public void run() {
            List<NameValuePair> payload = GoogleAnalyticsDataPublisher.buildPayload(data);
            GoogleAnalyticsDataPublisher.publishPOST(payload, userAgent, false);
        }
    }

}
