/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   you may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.appmgt.gateway.handlers.security.authentication;

import org.apache.axis2.Constants;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.opensaml.saml2.core.AuthnRequest;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.gateway.handlers.security.Session;
import org.wso2.carbon.appmgt.gateway.handlers.security.SessionStore;
import org.wso2.carbon.appmgt.gateway.handlers.security.saml2.IDPCallback;
import org.wso2.carbon.appmgt.gateway.handlers.security.saml2.SAMLException;
import org.wso2.carbon.appmgt.gateway.handlers.security.saml2.SAMLUtils;
import org.wso2.carbon.appmgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.DefaultAppRepository;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

/**
 * Handles Gateway Authentication with SAML2
 */
public class SAML2AuthenticationHandler extends AbstractHandler implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(SAML2AuthenticationHandler.class);
    private static final String SET_COOKIE_PATTERN = "%s=%s; Path=%s;";

    // A Synapse handler is instantiated per Synapse API.
    // So the web app for the relevant Synapse API can be fetched and stored as an instance variable.
    private WebApp webApp;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String webAppContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String webAppVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String fullResourceURL = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        String baseURL = String.format("%s/%s/", webAppContext, webAppVersion);
        String relativeResourceURL = StringUtils.substringAfter(fullResourceURL, baseURL);

        String httpVerb = (String) messageContext.getProperty(Constants.Configuration.HTTP_METHOD);
        if(httpVerb == null) {
            httpVerb =   (String) axis2MessageContext.getProperty(Constants.Configuration.HTTP_METHOD);
        }

        if(log.isDebugEnabled()){
            log.debug(String.format("Request received : '%s'", fullResourceURL));
        }

        Session session = getSession(messageContext);

        // Fetch the web app for the requested context and version.
        try {
            if(webApp == null){
                webApp = new DefaultAppRepository(null).getWebAppByContextAndVersion(webAppContext, webAppVersion, -1234);
            }
        } catch (AppManagementException e) {
            String errorMessage = String.format("Can't fetch the web for '%s' from the repository.", fullResourceURL);
            logAndThrowException(errorMessage, e);
        }

        // If the request comes to the ACS URL, then it should be the SAML response from the IDP.
        if(isACSURL(relativeResourceURL)){

            // Build the message.
            try {
                RelayUtils.buildMessage(axis2MessageContext);
            } catch (IOException e) {
                String errorMessage = String.format("Can't build the incoming request message for '%s'.", fullResourceURL);
                logAndThrowException(errorMessage, e);
            } catch (XMLStreamException e) {
                String errorMessage = String.format("Can't build the incoming request message for '%s'.", fullResourceURL);
                logAndThrowException(errorMessage, e);
            }

            IDPCallback idpCallback = null;
            try {
                idpCallback = SAMLUtils.processIDPCallback(messageContext);

                if(idpCallback.getSAMLResponse() == null){
                    String errorMessage = String.format("A SAML response was not there in the request to the ACS URL ('%s')", fullResourceURL);
                    logAndThrowException(errorMessage, null);
                }
            } catch (SAMLException e) {
                String errorMessage = String.format("Error while processing the IDP call back request to the ACS URL ('%s')", fullResourceURL);
                logAndThrowException(errorMessage, null);
            }

            if(log.isDebugEnabled()){
                log.debug("SAMLResponse is available in request.");
            }

            AuthenticationContext authenticationContext = getAuthenticationContextFromIDPCallback(idpCallback);

            if(authenticationContext.isAuthenticated()){;

                if(log.isDebugEnabled()){
                    log.debug(String.format("SAML response is authenticated. Subject = '%s'", authenticationContext.getSubject()));
                }

                session.setAuthenticationContext(authenticationContext);
                SessionStore.getInstance().updateSession(session);

                redirectToURL(messageContext, session.getRequestedURL());
                return false;
            }else{
                if(log.isDebugEnabled()){
                    log.debug("SAML response is not authenticated.");
                }
                requestAuthentication(messageContext);
                return false;
            }
        }else{

            if(GatewayUtils.isAnonymousAccessAllowed(webApp, httpVerb, relativeResourceURL)){
                if(log.isDebugEnabled()){
                    log.debug(String.format("Request to '%s' is allowed for anonymous access", fullResourceURL));
                }
                return true;
            }

            AuthenticationContext authenticationContext = session.getAuthenticationContext();

            if(!authenticationContext.isAuthenticated()){

                if(log.isDebugEnabled()){
                    log.debug(String.format("Request to '%s' is not authenticated", fullResourceURL));
                }

                session.setRequestedURL(fullResourceURL);
                setSessionCookie(messageContext, session.getUuid());
                requestAuthentication(messageContext);
                return false;
            }else {

                if(log.isDebugEnabled()){
                    log.debug(String.format("Request to '%s' is authenticated. Subject = '%s'", fullResourceURL, authenticationContext.getSubject()));
                }
                return true;
            }
        }
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }


    // ----------------------------------------------------------------------------------------------------------

    private Session getSession(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map<String, Object> headers = (Map<String, Object>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String cookieHeaderValue = (String) headers.get(HTTPConstants.HEADER_COOKIE);

        if(cookieHeaderValue != null){
            List<HttpCookie> cookies = HttpCookie.parse(cookieHeaderValue);

            for(HttpCookie cookie : cookies){
                if(AppMConstants.APPM_SAML2_COOKIE.equals(cookie.getName())){

                    if(log.isDebugEnabled()){
                        log.debug(String.format("Cookie '%s' is available in the request.", AppMConstants.APPM_SAML2_COOKIE));
                    }

                    return SessionStore.getInstance().getSession(cookie.getValue());
                }
            }
        }

        if(log.isDebugEnabled()){
            log.debug(String.format("Cookie '%s' is not available in the request.", AppMConstants.APPM_SAML2_COOKIE));
        }

        return SessionStore.getInstance().getSession(null);
    }

    private void setSessionCookie(MessageContext messageContext, String cookieValue) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map<String, Object> headers = (Map<String, Object>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String setCookieString = String.format(SET_COOKIE_PATTERN, AppMConstants.APPM_SAML2_COOKIE, cookieValue, "/");

        headers.put(HTTPConstants.HEADER_SET_COOKIE, setCookieString);
        messageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);

        if(log.isDebugEnabled()){
            log.debug(String.format("Cookie '%s' has been set in the response", AppMConstants.APPM_SAML2_COOKIE));
        }
    }

    private void requestAuthentication(MessageContext messageContext) {

        AuthnRequest authenticationRequest = SAMLUtils.buildAuthenticationRequest(messageContext, webApp);

        String encodedAuthenticationRequest = null;
        try {
            encodedAuthenticationRequest = SAMLUtils.marshallAndEncodeSAMLRequest(authenticationRequest);
        } catch (SAMLException e) {
            e.printStackTrace();
        }

        String samlRequestURL = GatewayUtils.getIDPUrl() + "?SAMLRequest=" + encodedAuthenticationRequest;
        redirectToURL(messageContext, samlRequestURL);
    }

    private AuthenticationContext getAuthenticationContextFromIDPCallback(IDPCallback idpCallback) {
        AuthenticationContext authenticationContext = SAMLUtils.getAuthenticationContext(idpCallback);
        return authenticationContext;
    }

    private void redirectToURL(MessageContext messageContext, String url){

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext). getAxis2MessageContext();
        axis2MessageContext.setProperty(NhttpConstants.HTTP_SC, "302");

        messageContext.setResponse(true);
        messageContext.setProperty("RESPONSE", "true");
        messageContext.setTo(null);
        axis2MessageContext.removeProperty("NO_ENTITY_BODY");

        /* Always remove the ContentType - Let the formatter do its thing */
        axis2MessageContext.removeProperty(Constants.Configuration.CONTENT_TYPE);

        Map headerMap = (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        headerMap.put("Location", url);

        if(log.isDebugEnabled()){
            log.debug(String.format("Sending HTTP redirect to '%s'", url));
        }

        removeIrrelevantHeadersBeforeReponding(headerMap);

        Axis2Sender.sendBack(messageContext);

    }

    private void removeIrrelevantHeadersBeforeReponding(Map headerMap) {
        headerMap.remove(HttpHeaders.HOST);
        headerMap.remove(HTTPConstants.HEADER_COOKIE);
    }

    private boolean isACSURL(String relativeResourceURL) {
        return relativeResourceURL.equals(AppMConstants.GATEWAY_ACS_RELATIVE_URL) ||
                relativeResourceURL.equals(AppMConstants.GATEWAY_ACS_RELATIVE_URL + "/");
    }

    private void logAndThrowException(String errorMessage, Exception e) {
        log.error(errorMessage);

        if(e == null){
            throw new SynapseException(errorMessage);
        }else {
            throw new SynapseException(errorMessage, e);
        }
    }
}
