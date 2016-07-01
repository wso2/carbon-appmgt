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
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.impl.ResponseImpl;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.URITemplate;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.appmgt.gateway.handlers.security.Session;
import org.wso2.carbon.appmgt.gateway.handlers.security.SessionStore;
import org.wso2.carbon.appmgt.gateway.handlers.security.saml2.IDPCallback;
import org.wso2.carbon.appmgt.gateway.handlers.security.saml2.SAMLException;
import org.wso2.carbon.appmgt.gateway.handlers.security.saml2.SAMLUtils;
import org.wso2.carbon.appmgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.gateway.token.JWTGenerator;
import org.wso2.carbon.appmgt.gateway.token.TokenGenerator;
import org.wso2.carbon.appmgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.DefaultAppRepository;
import org.wso2.carbon.appmgt.impl.SAMLConstants;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles Gateway Authentication with SAML2
 */
public class SAML2AuthenticationHandler extends AbstractHandler implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(SAML2AuthenticationHandler.class);
    private static final String SET_COOKIE_PATTERN = "%s=%s; Path=%s;";
    private static final String SESSION_ATTRIBUTE_RAW_SAML_RESPONSE = "rawSAMLResponse";
    private static final String SESSION_ATTRIBUTE_JWT = "jwt";
    public static final String HTTP_HEADER_SAML_RESPONSE = "AppMgtSAML2Response";

    // A Synapse handler is instantiated per Synapse API.
    // So the web app for the relevant Synapse API can be fetched and stored as an instance variable.
    private WebApp webApp;
    private AppManagerConfiguration configuration;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        configuration = org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        Session session = getSession(messageContext);

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String webAppContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String webAppVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String fullResourceURL = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        String baseURL = String.format("%s/%s/", webAppContext, webAppVersion);
        String relativeResourceURL = StringUtils.substringAfter(fullResourceURL, baseURL);
        String httpVerb =   (String) axis2MessageContext.getProperty(Constants.Configuration.HTTP_METHOD);

        // Fetch the web app for the requested context and version.
        try {
            if(webApp == null){
                webApp = new DefaultAppRepository(null).getWebAppByContextAndVersion(webAppContext, webAppVersion, -1234);
            }
        } catch (AppManagementException e) {
            String errorMessage = String.format("Can't fetch the web for '%s' from the repository.", fullResourceURL);
            GatewayUtils.logAndThrowException(log, errorMessage, e);
        }

        // Find a matched URI template.
        URITemplate matchedTemplate = GatewayUtils.findMatchedURITemplate(webApp, httpVerb, relativeResourceURL);
        messageContext.setProperty(AppMConstants.MESSAGE_CONTEXT_PROPERTY_MATCHED_URI_TEMPLATE, matchedTemplate);

        // If the request comes to the ACS URL, then it should be the SAML response from the IDP.
        if(isACSURL(relativeResourceURL)){

            // Build the message.
            try {
                RelayUtils.buildMessage(axis2MessageContext);
            } catch (IOException e) {
                String errorMessage = String.format("Can't build the incoming request message for '%s'.", fullResourceURL);
                GatewayUtils.logAndThrowException(log, errorMessage, e);
            } catch (XMLStreamException e) {
                String errorMessage = String.format("Can't build the incoming request message for '%s'.", fullResourceURL);
                GatewayUtils.logAndThrowException(log, errorMessage, e);
            }

            IDPCallback idpCallback = null;
            try {
                idpCallback = SAMLUtils.processIDPCallback(messageContext);

                if(idpCallback.getSAMLResponse() == null){
                    String errorMessage = String.format("A SAML response was not there in the request to the ACS URL ('%s')", fullResourceURL);
                    GatewayUtils.logAndThrowException(log, errorMessage, null);
                }
            } catch (SAMLException e) {
                String errorMessage = String.format("Error while processing the IDP call back request to the ACS URL ('%s')", fullResourceURL);
                GatewayUtils.logAndThrowException(log, errorMessage, null);
            }

            GatewayUtils.logWithRequestInfo(log, messageContext, "SAMLResponse is available in request.");

            AuthenticationContext authenticationContext = getAuthenticationContextFromIDPCallback(idpCallback);

            if(authenticationContext.isAuthenticated()){;

                if(log.isDebugEnabled()){
                    GatewayUtils.logWithRequestInfo(log, messageContext, String.format("SAML response is authenticated. Subject = '%s'", authenticationContext.getSubject()));
                }

                session.setAuthenticationContext(authenticationContext);
                session.addAttribute(SESSION_ATTRIBUTE_RAW_SAML_RESPONSE, idpCallback.getRawSAMLResponse());

                Map<String, Object> userAttributes = getUserAttributes(idpCallback.getSAMLResponse());
                session.getAuthenticationContext().setAttributes(userAttributes);

                String roleAttributeValue = (String) userAttributes.get("http://wso2.org/claims/role");

                if(roleAttributeValue != null){
                    String[] roles = roleAttributeValue.split(",");
                    for(String role : roles){
                        session.getAuthenticationContext().addRole(role);
                    }
                }

                // Generate the JWT and store in the session.
                if(isJWTEnabled()){
                    try {
                        session.addAttribute(SESSION_ATTRIBUTE_JWT, getJWTGenerator().generateToken(userAttributes, webApp, messageContext));
                    } catch (AppManagementException e) {
                        String errorMessage = String.format("Can't generate JWT for the subject : '%s'",
                                authenticationContext.getSubject());
                        GatewayUtils.logAndThrowException(log, errorMessage, e);
                    }
                }

                SessionStore.getInstance().updateSession(session);
                redirectToURL(messageContext, session.getRequestedURL());
                return false;
            }else{

                if(log.isDebugEnabled()){
                    GatewayUtils.logWithRequestInfo(log, messageContext, "SAML response is not authenticated.");
                }

                requestAuthentication(messageContext);
                return false;
            }
        }else{

            if(GatewayUtils.isAnonymousAccessAllowed(webApp, matchedTemplate)){

                if(log.isDebugEnabled()){
                    GatewayUtils.logWithRequestInfo(log, messageContext, String.format("Request to '%s' is allowed for anonymous access", fullResourceURL));
                }

                messageContext.setProperty(AppMConstants.MESSAGE_CONTEXT_PROPERTY_GATEWAY_SKIP_SECURITY, true);
                return true;
            }

            AuthenticationContext authenticationContext = session.getAuthenticationContext();

            if(!authenticationContext.isAuthenticated()){

                if(log.isDebugEnabled()){
                    GatewayUtils.logWithRequestInfo(log, messageContext, String.format("Request to '%s' is not authenticated", fullResourceURL));
                }

                session.setRequestedURL(fullResourceURL);
                setSessionCookie(messageContext, session.getUuid());
                requestAuthentication(messageContext);
                return false;
            }else {

                if(log.isDebugEnabled()){
                    GatewayUtils.logWithRequestInfo(log, messageContext, String.format("Request to '%s' is authenticated. Subject = '%s'", fullResourceURL, authenticationContext.getSubject()));
                }

                // Set the session as a message context property.
                messageContext.setProperty(AppMConstants.APPM_SAML2_COOKIE, session.getUuid());

                if(shouldSendSAMLResponseToBackend()){

                    addTransportHeader(messageContext, HTTP_HEADER_SAML_RESPONSE, (String) session.getAttribute(SESSION_ATTRIBUTE_RAW_SAML_RESPONSE));

                    if(log.isDebugEnabled()){
                        GatewayUtils.logWithRequestInfo(log, messageContext, "SAML response has been set in the request to the backend.");
                    }
                }

                if(isJWTEnabled()){

                    String jwtHeaderName = configuration.getFirstProperty(APISecurityConstants.API_SECURITY_CONTEXT_HEADER);

                    addTransportHeader(messageContext, jwtHeaderName, (String) session.getAttribute(SESSION_ATTRIBUTE_JWT));

                    if(log.isDebugEnabled()){
                        GatewayUtils.logWithRequestInfo(log, messageContext, "JWT has been set in the request to the backend.");
                    }
                }

                return true;
            }
        }
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

   @Override
    public void destroy() {

    }

    // ----------------------------------------------------------------------------------------------------------

    private Session getSession(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map<String, Object> headers = (Map<String, Object>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String cookieHeaderValue = (String) headers.get(HTTPConstants.HEADER_COOKIE);

        boolean isCookiePresent = false;
        if(cookieHeaderValue != null){
            List<HttpCookie> cookies = HttpCookie.parse(cookieHeaderValue);

            for(HttpCookie cookie : cookies){
                if(AppMConstants.APPM_SAML2_COOKIE.equals(cookie.getName())){

                    messageContext.setProperty(AppMConstants.APPM_SAML2_COOKIE, cookie.getValue());
                    isCookiePresent = true;

                    if(log.isDebugEnabled()){
                        GatewayUtils.logWithRequestInfo(log, messageContext, String.format("Cookie '%s' is available in the request.", AppMConstants.APPM_SAML2_COOKIE));
                    }
                }
            }
        }

        if(!isCookiePresent){
            if(log.isDebugEnabled()){
                GatewayUtils.logWithRequestInfo(log, messageContext, String.format("Cookie '%s' is not available in the request.", AppMConstants.APPM_SAML2_COOKIE));
            }
        }


        return GatewayUtils.getSession(messageContext);
    }

    private void setSessionCookie(MessageContext messageContext, String cookieValue) {

        String setCookieString = String.format(SET_COOKIE_PATTERN, AppMConstants.APPM_SAML2_COOKIE, cookieValue, "/");

        addTransportHeader(messageContext, HTTPConstants.HEADER_SET_COOKIE, setCookieString);

        if(log.isDebugEnabled()){
            GatewayUtils.logWithRequestInfo(log, messageContext, String.format("Cookie '%s' has been set in the response", AppMConstants.APPM_SAML2_COOKIE));
        }

    }

    private void addTransportHeader(MessageContext messageContext, String headerName, String headerValue){
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map<String, Object> headers = (Map<String, Object>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        headers.put(headerName, headerValue);
        messageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
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
            GatewayUtils.logWithRequestInfo(log, messageContext, String.format("Sending HTTP redirect to '%s'", url));
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

    private boolean shouldSendSAMLResponseToBackend() {
        return Boolean.valueOf(configuration.getFirstProperty(AppMConstants.API_CONSUMER_AUTHENTICATION_ADD_SAML_RESPONSE_HEADER_TO_OUT_MSG));
    }

    private TokenGenerator getJWTGenerator() {
        TokenGenerator tokenGeneratorFromService = ServiceReferenceHolder.getInstance().getTokenGenerator();
        if(tokenGeneratorFromService != null) {
            return tokenGeneratorFromService;
        }
        return new JWTGenerator();
    }

    private Map<String, Object> getUserAttributes(ResponseImpl samlResponse) {

        Map<String, Object> userAttributes = new HashMap<>();

        // Add 'Subject'
        Assertion assertion = samlResponse.getAssertions().get(0);
        userAttributes.put(SAMLConstants.SAML2_ASSERTION_SUBJECT, assertion.getSubject().getNameID().getValue());

        // Add other user attributes.
        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
        if (attributeStatements != null) {
            for(AttributeStatement attributeStatement : attributeStatements){
                List<Attribute> attributes = attributeStatement.getAttributes();
                for(Attribute attribute : attributes){
                    userAttributes.put(attribute.getName(), attribute.getAttributeValues().get(0).getDOM().getTextContent());
                }
            }
        }

        return userAttributes;
    }

    private boolean isJWTEnabled() {

        if (configuration != null) {
            return configuration.isJWTEnabled();
        }

        return false;
    }
}
