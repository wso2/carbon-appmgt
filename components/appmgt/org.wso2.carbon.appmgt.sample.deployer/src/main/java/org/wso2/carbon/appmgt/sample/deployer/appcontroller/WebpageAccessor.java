/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appmgt.sample.deployer.appcontroller;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.sample.deployer.bean.WebAppDetail;
import org.wso2.carbon.appmgt.sample.deployer.configuration.Configuration;
import org.wso2.carbon.appmgt.sample.deployer.http.HttpHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Random;

/**
 * This class is use to accses web pages in sample web pages
 */
public class WebpageAccessor {

    final static Logger log = Logger.getLogger(WebpageAccessor.class.getName());
    private static HttpHandler httpHandler;
    private static String ipAddress;

    /**
     * This method is use for accses a web application according to user given hit count
     *
     * @param webAppDetail
     *            bean class of the web application
     *
     * @param ip
     *            IP address of the user mashine
     *
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     *            Throws this when failed to accses web application
     *            Throws this when thread failed to sleep
     */
    public static void accsesWebPages(WebAppDetail webAppDetail, String ip)
            throws AppManagementException {
        ipAddress =ip;
        String webContext = webAppDetail.getContext();
        httpHandler = new HttpHandler();
        String httpsBackEndUrl =Configuration.getHttpsUrl();
        String loginHtmlPage = null;
        StringBuilder webAppUrlBuilder = new StringBuilder();
        webAppUrlBuilder.append("http://");
        webAppUrlBuilder.append(ipAddress);
        webAppUrlBuilder.append(":");
        webAppUrlBuilder.append(Configuration.getGatewayPort("http"));
        webAppUrlBuilder.append(webAppDetail.getContext());
        webAppUrlBuilder.append("/1.0.0/");
        String webAppurl = webAppUrlBuilder.toString();
        String responceHtml = null;
        try {
            loginHtmlPage = httpHandler.getHtml(webAppurl);
            Document html = Jsoup.parse(loginHtmlPage);
            Element sessionDataKeyElement = html.select("input[name=sessionDataKey]").first();
            String sessionDataKey = sessionDataKeyElement.val();
            responceHtml = httpHandler.doPostHttps(httpsBackEndUrl + "/commonauth"
                    ,"username=subscriber_"+webAppDetail.getUserName()+"&password=subscriber&" +
                    "sessionDataKey=" + sessionDataKey+"&chkRemember=on", "none", "application/x-www-form-urlencoded");
            Document postHtml = Jsoup.parse(responceHtml);
            Element postHTMLResponse = postHtml.select("input[name=SAMLResponse]").first();
            String samlResponse = postHTMLResponse.val();
            String appmSamlSsoTokenId = httpHandler.doPostHttp(webAppurl,
                    "SAMLResponse=" + URLEncoder.encode(samlResponse, "UTF-8"), "appmSamlSsoTokenId",
                    "application/x-www-form-urlencoded; charset=UTF-8");
            Object[][] webPages = webAppDetail.getWebPagesurl();
            for(int i = 0;i < webPages.length ;i++){
                if(webPages[i][0].toString().equals("default")){
                    sendHit(Integer.parseInt(webPages[i][1].toString()),appmSamlSsoTokenId,webAppurl,webAppDetail.getTrackingCode());
                }else{
                    if(i == 1){
                        webAppurl = appendPageToUrl(webPages[i][0].toString(),webAppurl,true);
                    }else {
                        webAppurl = appendPageToUrl(webPages[i][0].toString(), webAppurl, false);
                    }
                    sendHit(Integer.parseInt(webPages[i][1].toString()), appmSamlSsoTokenId, webAppurl, webAppDetail.getTrackingCode());
                }
            }
        } catch (IOException e) {
            log.error("Error while accessing a web page", e);
            throw  new AppManagementException("Error while accessing a web page", e);
        }

    }

    /**
     * This method is send hits for give web page
     *
     * @param hitCount
     *            hit count of a web page
     *
     * @param appmSamlSsoTokenId
     *            appmSamlSsoTokenId of the web application
     *
     * @param webAppurl
     *            url of the web page
     *
     * @param trackingCode
     *            tracking code of the web application
     *
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     *            Throws this when failed to accses web application
     *            Throws this when thread failed to sleep
     */
    private static void sendHit(int hitCount,String appmSamlSsoTokenId,String webAppurl,String trackingCode)
            throws AppManagementException, IOException {
        for (int i = 0; i < hitCount; i++) {

            httpHandler.doGet("http://" + ipAddress + ":"+Configuration.getGatewayPort("http")+"/statistics/",
                    trackingCode, appmSamlSsoTokenId, webAppurl);
            log.debug("Web Page : " + webAppurl + " Hit count : " + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Error while accessing a web page", e);
                throw  new AppManagementException("Error while accessing a web page", e);
            }
        }

    }

    /**
     * This method is use to build a url
     *
     * @param pageName
     *            Page name of the web application
     *
     * @param webAppUrl
     *            Current url of web application
     *
     * @param isAppendLastOne
     *
     *
     */
    private static String appendPageToUrl(String pageName, String webAppUrl, boolean isAppendLastOne) {
        String elements[] = webAppUrl.split("/");
        StringBuilder newUrl = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            if (!elements[i].equals("")) {
                if (i == 0) {
                    newUrl.append(elements[i] + "//");
                } else if ((i == (elements.length - 1)) && isAppendLastOne) {
                    newUrl.append(elements[i] + "/");
                } else if (i != (elements.length - 1)) {
                    newUrl.append(elements[i] + "/");
                }
            }
        }
        newUrl.append(pageName + "/");
        return newUrl.toString();
    }
}


