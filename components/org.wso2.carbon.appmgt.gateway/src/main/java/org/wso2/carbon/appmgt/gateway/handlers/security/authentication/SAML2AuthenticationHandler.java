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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.ResponseImpl;
import org.opensaml.xml.security.credential.Credential;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.URITemplate;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.appmgt.gateway.handlers.security.Session;
import org.wso2.carbon.appmgt.gateway.handlers.security.SessionStore;
import org.wso2.carbon.appmgt.gateway.handlers.security.saml2.IDPMessage;
import org.wso2.carbon.appmgt.gateway.handlers.security.saml2.SAMLException;
import org.wso2.carbon.appmgt.gateway.handlers.security.saml2.SAMLUtils;
import org.wso2.carbon.appmgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.gateway.token.JWTGenerator;
import org.wso2.carbon.appmgt.gateway.token.TokenGenerator;
import org.wso2.carbon.appmgt.gateway.utils.CacheManager;
import org.wso2.carbon.appmgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.DefaultAppRepository;
import org.wso2.carbon.appmgt.impl.SAMLConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.sso.saml.exception.IdentitySAML2SSOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles Gateway Authentication with SAML2
 */
public class SAML2AuthenticationHandler extends AbstractHandler implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(SAML2AuthenticationHandler.class);
    private static final String SET_COOKIE_PATTERN = "%s=%s; Path=%s;";
    private static final String SESSION_ATTRIBUTE_JWTS = "jwts";
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

        // Check per-app anonymous access first.
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
                webApp = new DefaultAppRepository(null).getWebAppByContextAndVersion(webAppContext, webAppVersion);
            }
        } catch (AppManagementException e) {
            String errorMessage = String.format("Can't fetch the web for '%s' from the repository.", fullResourceURL);
            GatewayUtils.logAndThrowException(log, errorMessage, e);
        }

        messageContext.setProperty(AppMConstants.MESSAGE_CONTEXT_PROPERTY_APP_ID, webApp.getDatabaseId());

        // Find a matched URI template.
        URITemplate matchedTemplate = GatewayUtils.findMatchedURITemplate(webApp, httpVerb, relativeResourceURL);
        messageContext.setProperty(AppMConstants.MESSAGE_CONTEXT_PROPERTY_MATCHED_URI_TEMPLATE, matchedTemplate);

        // If the request comes to the ACS URL, then it should be a SAML response or a request from the IDP.
        if(isACSURL(relativeResourceURL)){
            handleRequestToACSEndpoint(messageContext, session);

            // All requests to the ACS URL should be redirected to somewhere. e.g. IDP, app root URL
            return false;
        }

        // Handle logout requests. These requests don't need to  be authenticated.
        if(GatewayUtils.isLogoutURL(webApp, relativeResourceURL)){
            doLogout(session);
            redirectToIDPWithLogoutRequest(messageContext, session);
            return false;
        }

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
            SessionStore.getInstance().updateSession(session);
            setSessionCookie(messageContext, session.getUuid());
            requestAuthentication(messageContext);
            return false;
        }else {

            if(log.isDebugEnabled()){
                GatewayUtils.logWithRequestInfo(log, messageContext, String.format("Request to '%s' is authenticated. Subject = '%s'", fullResourceURL, authenticationContext.getSubject()));
            }

            // If this web app has not been access before in this session, redirect to the IDP.
            // This is done to make sure SLO works.

            if(!session.hasAppBeenAccessedBefore(webApp.getUUID())){
                GatewayUtils.logWithRequestInfo(log, messageContext, "This web app has not been accessed before in the current session. Doing SSO through IDP since it is needed to make SLO work.");
                session.setRequestedURL(fullResourceURL);
                SessionStore.getInstance().updateSession(session);
                requestAuthentication(messageContext);
                return false;
            }
            
            // Set the session as a message context property.
            messageContext.setProperty(AppMConstants.APPM_SAML2_COOKIE, session.getUuid());

            if(shouldSendSAMLResponseToBackend()){

                Map<String , String> samlResponses = (Map<String, String>) session.getAttribute(SAMLUtils.SESSION_ATTRIBUTE_RAW_SAML_RESPONSES);

                String samlResponseForApp = null;

                if(samlResponses != null && (samlResponseForApp = samlResponses.get(webApp.getUUID())) != null){

                    addTransportHeader(messageContext, HTTP_HEADER_SAML_RESPONSE, samlResponseForApp);

                    if(log.isDebugEnabled()){
                        GatewayUtils.logWithRequestInfo(log, messageContext, "SAML response has been set in the request to the backend.");
                    }

                }else{

                    if(log.isDebugEnabled()){
                        GatewayUtils.logWithRequestInfo(log, messageContext, "Couldn't find the SAML response for the app in the session.");
                    }
                }
            }

            if(isJWTEnabled()){

                String jwtHeaderName = configuration.getFirstProperty(APISecurityConstants.API_SECURITY_CONTEXT_HEADER);
                Map<String, String> generatedJWTs = (Map<String, String>) session.getAttribute(SESSION_ATTRIBUTE_JWTS);

                String jwtForApp = null;
                if(generatedJWTs != null && (jwtForApp = generatedJWTs.get(webApp.getUUID())) != null){
                    addTransportHeader(messageContext, jwtHeaderName, jwtForApp);
                    if(log.isDebugEnabled()){
                        GatewayUtils.logWithRequestInfo(log, messageContext, "JWT has been set in the request to the backend.");
                    }
                }else{
                    if(log.isDebugEnabled()){
                        GatewayUtils.logWithRequestInfo(log, messageContext, "Couldn't find the generated JWT for the app in the session.");
                    }
                }
            }

            return true;
        }
    }

    private void doLogout(Session session) {
        SessionStore.getInstance().removeSession(session.getUuid());
    }

    private OMElement handleSLORequest(MessageContext messageContext, LogoutRequest logoutRequest) {

        // Get the session index from the SLORequest and remove the relevant session.
        String sessionIndex = logoutRequest.getSessionIndexes().get(0).getSessionIndex();

        String sessionId = CacheManager.getInstance().getSessionIndexMappingCache().get(sessionIndex);

        if(sessionId != null){
            GatewayUtils.logWithRequestInfo(log, messageContext, String.format("Found a session id (md5 : '%s')for the given session index in the SLO request: '%s'. Clearing the session", GatewayUtils.getMD5Hash(sessionId), sessionIndex));
            SessionStore.getInstance().removeSession(sessionId);
            CacheManager.getInstance().getSessionIndexMappingCache().remove(sessionIndex);
        }else{
            GatewayUtils.logWithRequestInfo(log, messageContext, String.format("Couldn't find a session id for the given session index : '%s'", sessionIndex));
        }

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace("http://wso2.org/appm", "appm");
        OMElement payload = fac.createOMElement("SLOResponse", ns);

        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText("SLORequest has been successfully processed by WSO2 App Manager");

        payload.addChild(errorMessage);

        return payload;

    }

    private boolean handleRequestToACSEndpoint(MessageContext messageContext, Session session) {

        String fullResourceURL = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);

        // Build the incoming message.
        GatewayUtils.buildIncomingMessage(messageContext);

        IDPMessage idpMessage = null;
        try {
            idpMessage = SAMLUtils.processIDPMessage(messageContext);

            if(idpMessage.getSAMLResponse() == null && idpMessage.getSAMLRequest() == null){
                String errorMessage = String.format("A SAML request or response was not there in the request to the ACS URL ('%s')", fullResourceURL);
                GatewayUtils.logAndThrowException(log, errorMessage, null);
            }

            // Validate the signature if there is any.
            String responseSigningKeyAlias = configuration.getFirstProperty(AppMConstants.SSO_CONFIGURATION_RESPONSE_SIGNING_KEY_ALIAS);

            // User the certificate of the super tenant since the responses are signed by the super tenant.
            Credential certificate = GatewayUtils.getIDPCertificate("carbon.super", responseSigningKeyAlias);
            
            boolean isValidSignature = idpMessage.validateSignature(certificate);
            if(!isValidSignature){
                String errorMessage = String.format("The signature of the SAML message received by the ASC URL ('%s'), can't be validated.", fullResourceURL);
                GatewayUtils.logAndThrowException(log, errorMessage, null);
            }


        } catch (SAMLException e) {
            String errorMessage = String.format("Error while processing the IDP call back request to the ACS URL ('%s')", fullResourceURL);
            GatewayUtils.logAndThrowException(log, errorMessage, e);
        } catch (IdentitySAML2SSOException e) {
            String errorMessage = String.format("Error while processing the IDP call back request to the ACS URL ('%s')", fullResourceURL);
            GatewayUtils.logAndThrowException(log, errorMessage, e);
        }

        GatewayUtils.logWithRequestInfo(log, messageContext, String.format("%s is available in request.", idpMessage.getSAMLRequest() != null ? "SAMLRequest" : "SAMLResponse"));

        // If not configured, the SLO request URL and the SLO response URL is the ACS URL by default.
        // SLOResponse is handled in this method. But the SLORequest is handled generically in the rest of the handler code.
        if(idpMessage.isSLOResponse()){
            GatewayUtils.logWithRequestInfo(log, messageContext, "SAMLResponse in an SLO response.");
            GatewayUtils.redirectToURL(messageContext, GatewayUtils.getAppRootURL(messageContext));
            return false;
        }else if(idpMessage.isSLORequest()){
            GatewayUtils.logWithRequestInfo(log, messageContext, "SAMLRequest in an SLO request.");
            OMElement response = handleSLORequest(messageContext, (LogoutRequest) idpMessage.getSAMLRequest());
            GatewayUtils.send200(messageContext, response);
            return false;
        } else{

            if(idpMessage.isResponseValidityPeriodExpired()){

                if(log.isDebugEnabled()){
                    GatewayUtils.logWithRequestInfo(log, messageContext, "The validity period of the SAML Response is expired.");
                }
                requestAuthentication(messageContext);
                return false;
            }

            AuthenticationContext authenticationContext = getAuthenticationContextFromIDPCallback(idpMessage);

            if(authenticationContext.isAuthenticated()){;

                if(log.isDebugEnabled()){
                    GatewayUtils.logWithRequestInfo(log, messageContext, String.format("SAML response is authenticated. Subject = '%s'", authenticationContext.getSubject()));
                }

                session.setAuthenticationContext(authenticationContext);

                if(shouldSendSAMLResponseToBackend()){

                    Map<String, String> samlResponses = (Map<String, String>) session.getAttribute(SAMLUtils.SESSION_ATTRIBUTE_RAW_SAML_RESPONSES);

                    if(samlResponses == null){
                        samlResponses = new HashMap<String, String>();
                        session.addAttribute(SAMLUtils.SESSION_ATTRIBUTE_RAW_SAML_RESPONSES, samlResponses);
                    }

                    samlResponses.put(webApp.getUUID(), idpMessage.getRawSAMLResponse());
                }

                // Get the SAML session index.
                String sessionIndex = (String) SAMLUtils.getSessionIndex((ResponseImpl) idpMessage.getSAMLResponse());
                session.addAttribute(SAMLUtils.SESSION_ATTRIBUTE_SAML_SESSION_INDEX, sessionIndex);
                GatewayUtils.logWithRequestInfo(log, messageContext, String.format("Session index : %s", sessionIndex));

                // Add Session Index -> Session ID to a cache.
                CacheManager.getInstance().getSessionIndexMappingCache().put(sessionIndex, session.getUuid());

                // Mark this web app as an access web app in this session.
                session.addAccessedWebAppUUID(webApp.getUUID());

                Map<String, Object> userAttributes = getUserAttributes((ResponseImpl) idpMessage.getSAMLResponse());
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

                        //Generate and store the JWT against the app.
                        Map<String, String> generatedJWTs = (Map<String, String>) session.getAttribute(SESSION_ATTRIBUTE_JWTS);

                        if(generatedJWTs == null){
                            generatedJWTs = new HashMap<String, String>();
                            session.addAttribute(SESSION_ATTRIBUTE_JWTS, generatedJWTs);
                        }

                        generatedJWTs.put(webApp.getUUID(), getJWTGenerator().generateToken(userAttributes, webApp, messageContext));

                    } catch (AppManagementException e) {
                        String errorMessage = String.format("Can't generate JWT for the subject : '%s'",
                                authenticationContext.getSubject());
                        GatewayUtils.logAndThrowException(log, errorMessage, e);
                    }
                }

                SessionStore.getInstance().updateSession(session);
                if(session.getRequestedURL() != null){
                    GatewayUtils.redirectToURL(messageContext, session.getRequestedURL());
                }else{
                    log.warn(String.format("Original requested URL in the session is null. Redirecting to the app root URL."));
                    GatewayUtils.redirectToURL(messageContext, GatewayUtils.getAppRootURL(messageContext));

                }

                return false;
            }else{

                if(log.isDebugEnabled()){
                    GatewayUtils.logWithRequestInfo(log, messageContext, "SAML response is not authenticated.");
                }

                requestAuthentication(messageContext);
                return false;
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

    private Session getSession(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map<String, Object> headers = (Map<String, Object>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String cookieHeaderValue = (String) headers.get(HTTPConstants.HEADER_COOKIE);

        if(cookieHeaderValue != null){

            Map<String, String> cookies = parseRequestCookieHeader(cookieHeaderValue);

            if(cookies.get(AppMConstants.APPM_SAML2_COOKIE) != null){
                if(log.isDebugEnabled()){
                    GatewayUtils.logWithRequestInfo(log, messageContext, String.format("Cookie '%s' is available in the request.", AppMConstants.APPM_SAML2_COOKIE));
                }
                messageContext.setProperty(AppMConstants.APPM_SAML2_COOKIE, cookies.get(AppMConstants.APPM_SAML2_COOKIE));
            }else{
                if(log.isDebugEnabled()){
                    GatewayUtils.logWithRequestInfo(log, messageContext, String.format("Cookie '%s' is not available in the request.", AppMConstants.APPM_SAML2_COOKIE));
                }
            }
        }

        return GatewayUtils.getSession(messageContext, true);
    }

    private Map<String, String> parseRequestCookieHeader(String cookieHeaderValue) {

        Map<String, String> cookies = new HashMap<String, String>();

        if(cookieHeaderValue != null){

            String [] cookieTokens = cookieHeaderValue.split(";");

            if(cookieTokens != null && cookieTokens.length > 0){
                for(String cookieToken : cookieTokens){
                    String[] cookieNameAndValue = cookieToken.split("=");
                    if(cookieNameAndValue != null && cookieNameAndValue.length == 2){
                        cookies.put(cookieNameAndValue[0].trim(), cookieNameAndValue[1].trim());
                    }
                }
            }
        }

        return cookies;
    }

    private void redirectToIDPWithLogoutRequest(MessageContext messageContext, Session session) {
        LogoutRequest logoutRequest = SAMLUtils.buildLogoutRequest(webApp.getSaml2SsoIssuer(), session);
        GatewayUtils.logWithRequestInfo(log, messageContext, "Redirecting to the IDP for logging out.");
        GatewayUtils.redirectToIDPWithSAMLRequest(messageContext, logoutRequest);
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
        GatewayUtils.redirectToIDPWithSAMLRequest(messageContext, authenticationRequest);
    }

    private AuthenticationContext getAuthenticationContextFromIDPCallback(IDPMessage idpMessage) {
        AuthenticationContext authenticationContext = SAMLUtils.getAuthenticationContext(idpMessage);
        return authenticationContext;
    }

    private boolean isACSURL(String relativeResourceURL) {

        String acsUrlPostfix = configuration.getFirstProperty(AppMConstants.SSO_CONFIGURATION_ACS_URL_POSTFIX);

        return relativeResourceURL.equals(acsUrlPostfix) ||
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
