/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.gateway.handlers.security.saml2;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.synapse.*;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.*;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.AuthenticatedIDP;
import org.wso2.carbon.appmgt.gateway.handlers.Utils;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.appmgt.gateway.handlers.security.Authenticator;
import org.wso2.carbon.appmgt.gateway.handlers.security.oauth.OAuthAuthenticator;
import org.wso2.carbon.appmgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.dto.SAMLTokenInfoDTO;
import org.wso2.carbon.appmgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.appmgt.impl.dto.WebAppInfoDTO;
import org.wso2.carbon.appmgt.impl.utils.AppContextCacheUtil;
import org.wso2.carbon.appmgt.impl.utils.NamedMatchList;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class SAML2AuthenticationHandler extends AbstractHandler implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(SAML2AuthenticationHandler.class);

    // Names of the attributes which IDP sends back after authentication takes place.
    private static final String IDP_CALLBACK_ATTRIBUTE_NAME_SAML_RESPONSE = "SAMLResponse";
    private static final String IDP_CALLBACK_ATTRIBUTE_NAME_AUTHENTICATED_IDPS = "AuthenticatedIdPs";
    private static final String IDP_CALLBACK_ATTRIBUTE_NAME_SAML_ASSERTION = "Assertion";
    private static final String IDP_CALLBACK_ATTRIBUTE_NAME_SAML_ASSERTION_NOT_ON_OR_AFTER = "NotOnOrAfter";

    // The element name which IDP uses when it issues an SLO request to the SP.
    private static final String IDP_CALLBACK_ATTRIBUTE_NAME_SAML_REQUEST = "SAMLRequest";

    // Names of the attributes which IDP sends back containing the relay state
    private static final String IDP_CALLBACK_ATTRIBUTE_NAME_RELAY_STATE = "RelayState";
    public static final String SAML_RESPONSE_HEADER = "AppMgtSAML2Response";

    private volatile Authenticator authenticator;
    private volatile SAML2Authenticator saml2Authenticator;
    private volatile WebAppInfoDTO webAppInfoDTO;

    public void init(SynapseEnvironment synapseEnvironment) {

        if (log.isDebugEnabled()) {
            log.debug("Initializing WebApp authentication handler instance");
        }

        authenticator = new OAuthAuthenticator();
        authenticator.init(synapseEnvironment);

        saml2Authenticator = new SAML2Authenticator();
        saml2Authenticator.init(synapseEnvironment);

        //Initialize the context cache by calling AppContextCacheUtil to pre-load cache
        AppContextCacheUtil.getTenantContextVersionUrlMap();
    }


    /**
     * Applies SAML 2 authentication if SSO is enabled.
     *
     * @param messageContext
     * @return
     */
    public boolean handleRequest(MessageContext messageContext) {

        // If SSO is not enabled skip this authentication handler.
        if (!isSSOEnabled()) {
            return true;
        }

        // Get request information.
        String webAppContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String webAppVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String httpVerb = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty("HTTP_METHOD");
        String resourcePath = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Handling request => context:%s, version:%s, method:%s, path:%s", webAppContext
                    , webAppVersion, httpVerb, resourcePath));
        }

        try {
            // Get App Info related to SSO handling.
            if (webAppInfoDTO == null) {
                WebAppInfoDTO webAppInfoDTO = getSSOInfoForApp(webAppContext, webAppVersion);
                constructAndSetFullyQualifiedSamlIssuerId(messageContext, webAppInfoDTO);
                this.webAppInfoDTO = webAppInfoDTO;
            }

            // If this is an SLO request we need to respond to the client (IDP) without continuing the flow.
            if (isSLORequestFromIDP(messageContext)) {

                if (log.isDebugEnabled()) {
                    log.debug("Request is an SLO request from the IDP");
                }

                handleSLORequest();

                if (log.isDebugEnabled()) {
                    log.debug("Sending SLO response to the IDP");
                }

                sendSLOResponse(messageContext);

                return false;
            }

            // If anonymous mode is enabled, skip this handler.
            boolean isAllowAnonymousApp = isAllowAnonymousApplication();

            // write to messageContext so then the same value can be accessed as a property in other handlers.
            messageContext.setProperty(AppMConstants.API_OVERVIEW_ALLOW_ANONYMOUS, isAllowAnonymousApp);
            if (isAllowAnonymousApp) {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Anonymous access is allowed for the app '%s'. Skipping the handler. ",
                            webAppInfoDTO.getContext()));
                }

                return true;
            }

            // If anonymous mode is enabled for this URL, skip the handler.
            boolean isAllowAnonymousUrl = isAllowAnonymousUrlPattern(httpVerb, resourcePath);
            messageContext.setProperty(AppMConstants.API_URI_ALLOW_ANONYMOUS, isAllowAnonymousUrl);
            if (isAllowAnonymousUrl) {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Anonymous access is allowed for the request path '%s'. Skipping the handler.",
                            resourcePath));
                }

                return true;
            }

            // Validate the authentication.
            // Authentication of the request is validated using either the session or the SAML response from the IDP.
            boolean isAuthenticationValidated = false;

            if (hasUserSession(messageContext)) {
                messageContext.setProperty(AppMConstants.APPM_SAML2_CACHE_HIT, 1);
                isAuthenticationValidated = validateUserSession(messageContext);
            } else if (isSAMLResponse(messageContext)) {

                messageContext.setProperty(AppMConstants.APPM_SAML2_CACHE_HIT, 0);

                if (log.isDebugEnabled()) {
                    log.debug("Request is a SAML response (callback) from the IDP.");
                }

                isAuthenticationValidated = validateSAMLResponse(messageContext);


                if (isAuthenticationValidated) {

                    // If the user should be redirected to the original requested URL if it not the index page.
                    if (redirectToOriginalRequestPath(messageContext)) {
                        return false;
                    }

                    //Note: When user authenticated, IdP sends the SAMLResponse to gateway as a POST request.
                    //We validate this SAMLResponse and allow request to go to backend.
                    //This is the first request goes to access the web-app which need to go as a GET request
                    //and we need to drop the SAMLResponse goes in the request body as well. Bellow code
                    //segment is to set the HTTP_METHOD as GET and set empty body in request.

                    getAxis2MessageContext(messageContext).setProperty("HTTP_METHOD", "GET");
                    try {
                        SOAPEnvelope env = OMAbstractFactory.getSOAP12Factory().createSOAPEnvelope();
                        env.addChild(OMAbstractFactory.getSOAP12Factory().createSOAPBody());
                        getAxis2MessageContext(messageContext).setEnvelope(env);
                    } catch (AxisFault axisFault) {
                        String msg = "Error occurred while constructing SOAPEnvelope for " +
                                messageContext.getProperty("REST_API_CONTEXT") + "/" +
                                messageContext.getProperty("SYNAPSE_REST_API_VERSION");
                        log.error(msg, axisFault);
                        throw new SynapseException(msg, axisFault);
                    }
                }
            }

            if (isAuthenticationValidated) {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Authentication of user '%s' has been validated.",
                            messageContext.getProperty(APISecurityConstants.SUBJECT)));
                }

                if (!isSubscribed(messageContext)) {

                    if (log.isDebugEnabled()) {
                        log.debug(String.format("'%s' is not subscribed for the app '%s'.",
                                messageContext.getProperty(APISecurityConstants.SUBJECT),
                                webAppInfoDTO.getContext()));
                    }

                    handleAuthFailure(messageContext, new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                            "You have not subscribed to this application"));
                    return false;
                }

                if (!isResourceAccessible(messageContext)) {

                    if (log.isDebugEnabled()) {
                        log.debug(String.format("'%s' is not authorized to access the resource '%s'.",
                                messageContext.getProperty(APISecurityConstants.SUBJECT), resourcePath));
                    }

                    handleAuthFailure(messageContext, new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                            "You are not authorized to access this resource"));
                    return false;
                }

                if (log.isDebugEnabled()) {
                    log.debug(String.format("User '%s' is allowed to access the resource '%s' ",
                            messageContext.getProperty(APISecurityConstants.SUBJECT), resourcePath));

                }

                return true;
            } else {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Either a session doesn't exist or the session doesn't have information of " +
                            "the app '%s'. Redirecting to the IDP.", webAppInfoDTO.getContext()));
                }

                redirectToIDPLogin(messageContext);
                return false;
            }
        } catch (AppManagementException e) {
            String errorMessage = "Error while handling authentication.";
            log.error(errorMessage);
            throw new SynapseException(errorMessage, e);
        } catch (APISecurityException e) {
            String errorMessage = "Error while handling authentication.";
            log.error(errorMessage);
            throw new SynapseException(errorMessage, e);
        }
    }

    /**
     * redirect the page to relay state location
     *
     * @param messageContext
     * @return if yes : true else false
     */
    private boolean redirectToOriginalRequestPath(MessageContext messageContext) throws AppManagementException {

        SOAPBody soapBody = messageContext.getEnvelope().getBody();
        boolean hasRelayState = false; //check if a relay state is available
        String relayState = ""; //contains replay state location where it should be redirected to
        if (soapBody != null) {
            if (soapBody.getChildren().hasNext()) {
                // Check whether there is a SAML request in the SOAP body.
                relayState = ((OMElement) ((OMElement) (soapBody.getChildren().next())).
                        getChildrenWithName(new QName(IDP_CALLBACK_ATTRIBUTE_NAME_RELAY_STATE)).next()).getText();

                if (!"null".equals(relayState)) {
                    hasRelayState = true;
                }
            }
        }

        // if relay state is available, redirect the request
        if (hasRelayState) {

            // Set the redirect location.
            String originalRequestUrl = (String) getSAML2RelayStateCache().get(relayState);

            if (originalRequestUrl == null) {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Can't find the store originally requested URL for the relay state %s",
                            relayState));
                }

                return false;

            }

            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

            // Set the cookie.
            String appmSamlSsoCookie = (String) messageContext.getProperty(AppMConstants.APPM_SAML2_COOKIE);
            Map<String, String> headers = (Map<String, String>) axis2MC.getProperty(
                    org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            setSamlSsoCookie(headers, appmSamlSsoCookie);

            axis2MC.setProperty(NhttpConstants.HTTP_SC, "302");
            messageContext.setResponse(true);
            messageContext.setProperty("RESPONSE", "true");
            messageContext.setTo(null);
            axis2MC.removeProperty("NO_ENTITY_BODY");
            String method = (String) axis2MC.getProperty(Constants.Configuration.HTTP_METHOD);

            /* If the request was not an entity enclosing request, send a XML response back */
            if (method.matches("^(?!.*(POST|PUT)).*$")) {
                axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/xml");
            }

            /* Always remove the ContentType - Let the formatter do its thing */
            axis2MC.removeProperty(Constants.Configuration.CONTENT_TYPE);

            headers.put("Location", originalRequestUrl);


            if (messageContext.getProperty("error_message_type") != null &&
                    messageContext.getProperty("error_message_type").toString().equalsIgnoreCase("application/json")) {
                axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/json");
            }

            if (log.isDebugEnabled()) {
                log.debug(String.format("Redirecting to the original requested url : %s", originalRequestUrl));
            }


            headers.remove(HttpHeaders.HOST);
            removeSecurityHeaders(headers);
            messageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
            Axis2Sender.sendBack(messageContext);

            return true;

        }
        return false;
    }

    /**
     * Check if the Anonymous Access is allowed for the overall app
     *
     * @return result : boolean result relevant to registry value
     */
    public boolean isAllowAnonymousApplication() {
        return webAppInfoDTO.getAllowAnonymous();
    }

    /**
     * Check if the Anonymous Access is allowed for the particular URL pattern
     *
     * @return boolean result either anonymous access allowed or not
     * @throws AppManagementException
     */
    public boolean isAllowAnonymousUrlPattern(String httpVerb, String requestPath) throws AppManagementException {

        // Get App URL Pattern Info
        VerbInfoDTO verbInfoDTO = getVerbInfoForApp(webAppInfoDTO.getContext(), webAppInfoDTO.getVersion());

        if (verbInfoDTO != null && verbInfoDTO.mapAllowAnonymousUrl != null) {

            NamedMatchList<String> matcher = new NamedMatchList<String>();

            for (String pattern : verbInfoDTO.mapAllowAnonymousUrl.keySet()) {
                matcher.add(pattern, pattern);
            }

            String httpVerbAndRequestPath = httpVerb + requestPath;

            String matchedPattern = matcher.match(httpVerbAndRequestPath);

            Boolean allowAnnoymous = verbInfoDTO.mapAllowAnonymousUrl.get(matchedPattern);

            if (allowAnnoymous != null) {
                return allowAnnoymous;
            }

        }

        return false;

    }

    public boolean handleResponse(MessageContext messageContext) {

        String appmSamlSsoCookie = (String) messageContext.getProperty(AppMConstants.APPM_SAML2_COOKIE);

        if (appmSamlSsoCookie != null) {

            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            Map<String, String> headers = (Map<String, String>) axis2MC.getProperty(
                    org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            setSamlSsoCookie(headers, appmSamlSsoCookie);
            messageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);

        }

        return true;
    }

    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Destroying WebApp authentication handler instance");
        }
    }

    private void sendSLOResponse(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Object headers = axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        if (headers != null && headers instanceof Map) {

            @SuppressWarnings("unchecked")
            Map<String, Object> headersMap = (Map<String, Object>) headers;

            headersMap.clear();
            axis2MessageContext.setProperty("HTTP_SC", "200");
            axis2MessageContext.setProperty("NO_ENTITY_BODY", Boolean.valueOf(true));
            messageContext.setProperty("RESPONSE", "true");
            messageContext.setTo(null);
            Axis2Sender.sendBack(messageContext);
        }
    }

    private void handleSLORequest() {
        // TODO Handle the SLO request to this app, from the IDP.
    }

    private boolean isSLORequestFromIDP(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        try {
            RelayUtils.buildMessage(axis2MessageContext);
        } catch (Exception e) {
            String errorMessage = "Error while building the message.";
            log.error(errorMessage, e);
            throw new SynapseException(errorMessage, e);
        }

        SOAPBody soapBody = messageContext.getEnvelope().getBody();

        if (soapBody != null) {

            // Try to get the SAML request in the SOAP body.
            // The expected structure is <body><mediate><SamlRequest></SamlRequest></mediate></body>

            if (soapBody.getChildren().hasNext()) {
                // Check whether there is a SAML request in the SOAP body.
                Iterator possibleSAMLRequestElements = ((OMElement) (soapBody.getChildren().next())).getChildrenWithName(new QName(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_REQUEST));
                return possibleSAMLRequestElements.hasNext();
            }
        }

        return false;
    }

    /**
     * Checks whether the request should be authenticated using the cookie.
     *
     * @param messageContext
     * @return true if the the request should be authenticated using the cookie, false otherwise.
     */
    private boolean hasUserSession(MessageContext messageContext) {

        // Cookie should be in the request and there should be an entry in the cache.
        String cookie = getSAMLCookie(messageContext);

        // Check the availability of cookie in cache.
        if (cookie != null && isSamlResponseInCache(cookie)) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Checks whether the request should be authenticated using the SAML response from the IDP.
     *
     * @param messageContext
     * @return true if the request should be authenticated using the SAML response from the IDP, false otherwise.
     */
    private boolean isSAMLResponse(MessageContext messageContext) {
        Map<String, String> idpResponseAttributes = getIDPResponseAttributes(messageContext);
        return idpResponseAttributes.get(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_RESPONSE) != null;
    }

    /**
     * Returns the cookie string of the request.
     *
     * @param messageContext
     * @return The cookie string of the request.
     */
    private String getCookieString(MessageContext messageContext) {
        Map<String, String> headers = getTransportHeaders(messageContext);
        return headers.get(HTTPConstants.COOKIE_STRING);
    }

    /**
     * Returns the SAML cookie value.
     *
     * @param messageContext
     * @return SAML cookie value.
     */
    private String getSAMLCookie(MessageContext messageContext) {
        String cookieString = getCookieString(messageContext);
        return getCookieValue(cookieString, AppMConstants.APPM_SAML2_COOKIE);
    }

    /**
     * Checks whether the SAML response is in cache.
     *
     * @param samlResponseKey
     * @return true if the SAML response is in cache, false otherwise.
     */
    private boolean isSamlResponseInCache(String samlResponseKey) {
        return getCachedSAMLResponse(samlResponseKey) != null;
    }

    /**
     * Returns SAML response from cache.
     *
     * @param cacheKey value of appmSamlSsoTokenId cookie value
     * @return The SAML response for the given key if the result Map contains the samlResponse of
     * webApp in cache , null otherwise.
     */
    public String getCachedSAMLResponse(String cacheKey) {
        Object response = getSAML2ConfigCache().get(cacheKey);
        if (response != null) {
            Map<String, SAMLTokenInfoDTO> samlResponsesMap = (HashMap<String, SAMLTokenInfoDTO>) response;
            String samlResponseOfWebApp = null;
            SAMLTokenInfoDTO samlTokenInfoDTO = samlResponsesMap.get(webAppInfoDTO.getSaml2SsoIssuer());

            if (samlTokenInfoDTO != null) {
                samlResponseOfWebApp = samlTokenInfoDTO.getEncodedSamlToken();
            }
            if (samlResponseOfWebApp != null) {
                return samlResponseOfWebApp;
            }
        }

        return null;
    }

    /**
     * Check whether saml token saved in cached is expired.
     *
     * @param cacheKey value of appmSamlSsoTokenId cookie value
     * @return Returns true if saml token is expired. Returns false if saml token is valid.
     */
    public boolean isSamlTokenExpired(String cacheKey) {
        Object response = getSAML2ConfigCache().get(cacheKey);
        if (response != null) {
            Map<String, SAMLTokenInfoDTO> samlResponsesMap = (HashMap<String, SAMLTokenInfoDTO>) response;
            DateTime samlTokenValidity = samlResponsesMap.get(webAppInfoDTO.getSaml2SsoIssuer()).getNotOnOrAfter();

            if (samlTokenValidity != null && samlTokenValidity.compareTo(new DateTime()) < 1) {
                // notOnOrAfter is an expired timestamp
                log.debug("NotOnOrAfter is having an expired timestamp in the cache for the SAML issuer = " + webAppInfoDTO.getSaml2SsoIssuer());
                return true;
            }
        }

        return false;
    }

    /**
     * Returns already cached userRoles.
     *
     * @param cacheKey
     * @return The SAML response for the given key if the response is in cache, null otherwise.
     */
    public String getCachedUserRoles(String cacheKey) {
        return (String) getUserRolesCache().get(cacheKey);
    }

    /**
     * Returns the user from cache.
     *
     * @param cacheKey
     * @return The cached user for the given key if the user is in cache, null otherwise.
     */
    private String getCachedLoggedInUser(String cacheKey) {
        return (String) saml2Authenticator.getKeyCache().get(cacheKey);
    }

    private void cacheUserProfile(String samlCookieValue, String subject, String roles) {
        saml2Authenticator.getKeyCache().put(samlCookieValue, subject);
        getUserRolesCache().put(samlCookieValue, roles);
    }

    /**
     * Checks whether the given SAML response is authenticated.
     *
     * @param samlAttributes
     * @return
     */
    public boolean isSAMLResponseAuthenticated(Map<String, Object> samlAttributes) {

        if (samlAttributes != null) {
            return samlAttributes.get(APISecurityConstants.SUBJECT) != null;
        }

        return false;
    }

    /**
     * Returns the Axis2 message context from the Synapse message context.
     *
     * @param messageContext
     * @return Axis2 message context.
     */
    private org.apache.axis2.context.MessageContext getAxis2MessageContext(MessageContext messageContext) {
        return ((Axis2MessageContext) messageContext).getAxis2MessageContext();
    }

    /**
     * Checks whether the request is a logout request.
     *
     * @param messageContext
     * @return true if the request is a logout request, false otherwise.
     */
    private boolean isLogoutRequest(MessageContext messageContext) {

        String inURL = getAxis2MessageContext(messageContext).getProperty("TransportInURL").toString();

        String logoutUrl = webAppInfoDTO.getLogoutUrl();
        if (logoutUrl != null && logoutUrl.endsWith(inURL)) {
            if (log.isDebugEnabled()) {
                log.debug("Logout URL Encountered");
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether SSO is enabled in App Manager config.
     *
     * @return true if SSO is enabled, false otherwise.
     */
    private boolean isSSOEnabled() {
        String idpUrl = getIDPUrl();
        return idpUrl != null;
    }

    /**
     * Returns SSO info related to the web app.
     *
     * @param webAppContext : Application Context
     * @param webAppVersion : Application Version
     * @return SSO info of the app.
     * @throws AppManagementException when an error returns while fetching data from database
     */
    private WebAppInfoDTO getSSOInfoForApp(String webAppContext, String webAppVersion) throws AppManagementException {
        try {
            return AppMDAO.getSAML2SSOConfigInfo(webAppContext, webAppVersion);
            // TODO: Add webAppInfoDTO to cache
        } catch (AppManagementException e) {
            //TODO: Handle exceptions
            return null;
        }

    }

    /**
     * Check the context/version and fetch the url pattern related data for the given url
     *
     * @param webAppContext : Application Context
     * @param webAppVersion : Application Version
     * @return VerbInfoDTO class object
     * @throws AppManagementException when an error returns while fetching data from database
     */
    private VerbInfoDTO getVerbInfoForApp(String webAppContext, String webAppVersion) throws AppManagementException {
        try {
            return AppMDAO.getVerbConfigInfo(webAppContext, webAppVersion);
        } catch (AppManagementException e) {
            throw e;
        }
    }

    /**
     * Checks whether JWT is enabled in App Manager config.
     *
     * @return true if JWT is enabled in App Manager config.
     */
    private boolean isJWTEnabled() {
        return AppMDAO.jwtGenerator != null;
    }

    /**
     * Checks whether the signed in user is subscribed to the requested app.
     *
     * @param messageContext
     * @return true if the signed in user is subscribed to the requested app.
     * @throws APISecurityException if there is an error in subscription check.
     */
    private boolean isSubscribed(MessageContext messageContext) throws APISecurityException {
        return saml2Authenticator.authenticate(messageContext);
    }

    /**
     * Adds cached JWT for signed in user, to the transport headers.
     *
     * @param messageContext
     */
    private void addCachedJWTToTransportHeaders(MessageContext messageContext) {

        String jwtCacheKey = (String) messageContext.getProperty(AppMConstants.APPM_SAML2_COOKIE);

        String jwt = null;

        Cache jwtCache = Caching.getCacheManager(AppMConstants.API_MANAGER_CACHE_MANAGER)
                .getCache(AppMConstants.JWT_CACHE_NAME);
        jwt = (String) jwtCache.get(jwtCacheKey);

        //Add JWT Token into transport headers
        if (jwt != null) {
            Map<String, String> headers = getTransportHeaders(messageContext);
            headers.put(authenticator.getSecurityContextHeader(), jwt);
        }
    }

    /**
     * Generate, caches and add the JWT to transport headers.
     *
     * @param messageContext
     * @param samlAttributes SAML attributes extracted from the SAML response. Even though it can be extracted again
     *                       using the passed message context, It's better to pass already extracted SAML attributes for performance's sake.
     * @throws org.wso2.carbon.appmgt.api.AppManagementException when there is an error in generating JWT.
     */
    private void generateJWTAndAddToTransportHeaders(MessageContext messageContext, Map<String, Object> samlAttributes) throws
            AppManagementException {

        String jwtCacheKey = (String) messageContext.getProperty(AppMConstants.APPM_SAML2_COOKIE);

        // Generate Token and update Cache.
        Cache jwtCache = Caching.getCacheManager(AppMConstants.API_MANAGER_CACHE_MANAGER).getCache(AppMConstants.JWT_CACHE_NAME);

        String webAppContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String webAppVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

        String jwtToken = AppMDAO.jwtGenerator.generateToken(samlAttributes, webAppContext, webAppVersion);
        jwtCache.put(jwtCacheKey, jwtToken);

        //Add JWT Token into transport headers.
        Map<String, String> headers = getTransportHeaders(messageContext);

        headers.put(authenticator.getSecurityContextHeader(), jwtToken);
    }

    /**
     * Handles authentication and authorization using cookie.
     *
     * @param messageContext
     * @return true if the user is authorized to access the resource, false otherwise.
     */
    private boolean validateUserSession(MessageContext messageContext) throws AppManagementException {

        Map<String, String> headers = getTransportHeaders(messageContext);

        String samlCookieValue = getSAMLCookie(messageContext);

        // If the SAML response is available in cache.
        if (isSamlResponseInCache(samlCookieValue)) {

            //Check whether saml token has expired
            if (isSamlTokenExpired(samlCookieValue)) {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Session is expired. (Cached SAML token is expired)"));
                }

                getSAML2ConfigCache().remove(samlCookieValue);
                return false;
            }

            String loggedInUser = getCachedLoggedInUser(samlCookieValue);
            AuthenticatedIDP[] authenticatedIDP = getCachedAuthenticatedIDP(samlCookieValue);

            messageContext.setProperty(APISecurityConstants.SUBJECT, loggedInUser);
            messageContext.setProperty(APISecurityConstants.AUTHENTICATED_IDP, authenticatedIDP);
            messageContext.setProperty(AppMConstants.APPM_SAML2_COOKIE, samlCookieValue);


            if (log.isDebugEnabled()) {
                log.debug(String.format("'%s' has a valid user session.", loggedInUser));
            }

            if (isLogoutRequest(messageContext)) {

                if (log.isDebugEnabled()) {
                    log.debug("Request is a logout request.");
                }

                handleLogoutRequest(messageContext);
            }

            if (isJWTEnabled()) { //JWT has enabled
                addCachedJWTToTransportHeaders(messageContext);
            }

            if (shouldAddSAMLResponseAsTransportHeader()) {
                headers.put(AppMConstants.APPM_SAML2_RESPONSE, getCachedSAMLResponse(samlCookieValue));
                messageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
            }


            return true;

        }

        return false;
    }

    /**
     * Checks whether the user has permissions to access the requested resources
     *
     * @return true if the user is allowed to access the resource.
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    private boolean isResourceAccessible(MessageContext synapseMessageContext) {

        try {

            String inUrl = getAxis2MessageContext(synapseMessageContext).getProperty("TransportInURL").toString();
            String webAppContext = (String) synapseMessageContext.getProperty(RESTConstants.REST_API_CONTEXT);
            String webAppVersion = (String) synapseMessageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

            String roles = getCachedUserRoles((String) synapseMessageContext.getProperty(AppMConstants.APPM_SAML2_COOKIE));

            org.apache.axis2.context.MessageContext axis2MsgContext;
            axis2MsgContext = ((Axis2MessageContext) synapseMessageContext).getAxis2MessageContext();

            String httpVerb = (String) axis2MsgContext.getProperty(Constants.Configuration.HTTP_METHOD);

            int appID = webAppInfoDTO.getAppID();

            AppMDAO appMDAO = new AppMDAO();
            ArrayList<String> mappingList = appMDAO.getInUrlMappingById(appID);
            ArrayList<String> mapperList = new ArrayList<String>();

            for (String mapping : mappingList) {
                String mapperUrl = webAppContext + "/" + webAppVersion + mapping;
                mapperList.add(mapperUrl);
            }
            Collections.sort(mapperList);
            Collections.reverse(mapperList);

            String matched = getMatchedURLPattern(mapperList, inUrl);
            if (matched != null) {
                String urlMapping = matched.substring((webAppContext + "/" + webAppVersion).length(), matched.length());

                // Set the relevant synapse properties to let the entitlement handler do its job properly.
                synapseMessageContext.setProperty(AppMConstants.MATCHED_URL_PATTERN_PROERTY_NAME, urlMapping);
                synapseMessageContext.setProperty(AppMConstants.MATCHED_APP_ID_PROERTY_NAME, appID);

                if (checkResourseAccessibleByRole(urlMapping, roles, appID, httpVerb)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }


        } catch (AppManagementException e) {
            log.error("Failed to check resources for user");
            return false;
        }
    }

    private String getUserRolesFromTheSAMLResponse(Map<String, Object> samlAttributes) {
        if (samlAttributes != null) {
            if (samlAttributes.get(APISecurityConstants.CLAIM_ROLES) != null) {
                return (String) samlAttributes.get(APISecurityConstants.CLAIM_ROLES);
            } else {
                return "";
            }
        }

        return "";
    }


    /**
     * Check weather the mapping url has the roles
     *
     * @param urlMapping
     * @param roles
     * @return true if the user is permitted to access the resource.
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */

    private boolean checkResourseAccessibleByRole(String urlMapping, String roles, int appId, String httpVerb) throws
            AppManagementException {

        AppMDAO appMDAO = new AppMDAO();
        String toBeMatchedRoles = appMDAO.getInUrlMappingRoles(appId, urlMapping, httpVerb);
        String toBeMatchedRole[] = getDelimitedRoles(toBeMatchedRoles);
        String contextRoles[] = getDelimitedRoles(roles);
        if (roles.equalsIgnoreCase("")) {
            return true;
        }
        if (toBeMatchedRoles == null || toBeMatchedRoles.isEmpty()) {
            return true;
        }

        for (int i = 0; i < contextRoles.length; i++) {
            for (int j = 0; j < toBeMatchedRole.length; j++) {
                if (contextRoles[i].equalsIgnoreCase(toBeMatchedRole[j])) {
                    return true;
                }
            }
        }
        return false;
    }

    private String[] getDelimitedRoles(String input) {

        String[] arr = input.split(",");
        return arr;
    }

    /**
     * Check weather the mapping url has the roles
     *
     * @param patternList
     * @param inUrl
     * @return true if the user is permitted to access the resource.
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */

    private String getMatchedURLPattern(ArrayList<String> patternList, String inUrl) {
        NamedMatchList<String> matcher = new NamedMatchList<String>();

        for (String pattern : patternList) {
            matcher.add(pattern, pattern);
        }

        String matched = matcher.match(inUrl);
        return matched;

    }

    /**
     * Handles authentication and authorization using SAML response.
     *
     * @param messageContext
     * @return true if the user is authorized for the requested resource.
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    private boolean validateSAMLResponse(MessageContext messageContext) throws AppManagementException {

        Map<String, String> headers = getTransportHeaders(messageContext);

        Map<String, String> idpResponseAttributes = getIDPResponseAttributes(messageContext);

        Map<String, Object> samlAttributes = getAttributesOfSAMLResponse(idpResponseAttributes);

        if (isSAMLResponseAuthenticated(samlAttributes)) {

            if (log.isDebugEnabled()) {
                log.debug("SAML response is valid.");
            }

            // Set the cookie value.
            String samlCookieValue = getSAMLCookie(messageContext);
            String samlResponse = idpResponseAttributes.get(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_RESPONSE);

            SAMLTokenInfoDTO samlTokenInfoDTO = new SAMLTokenInfoDTO();
            samlTokenInfoDTO.setEncodedSamlToken(samlResponse);
            samlTokenInfoDTO.setNotOnOrAfter((DateTime) samlAttributes.get(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_ASSERTION_NOT_ON_OR_AFTER));

            if (samlCookieValue == null) {
                samlCookieValue = UUID.randomUUID().toString();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Creating a new session for '%s' ", (String) samlAttributes.get(APISecurityConstants.SUBJECT)));
                }

                //Cache SAML response. Different apps may configured to have different claim values which result in different
                //SAML responses for each app. Map is required to store SAML responses of apps being invoked. Then set the cache
                //key as value of 'appmSamlSsoTokenId' cookie and this Map has the value and put it to cache.
                Map<String, SAMLTokenInfoDTO> samlResponsesMap = new HashMap<String, SAMLTokenInfoDTO>();
                samlResponsesMap.put(webAppInfoDTO.getSaml2SsoIssuer(), samlTokenInfoDTO);
                getSAML2ConfigCache().put(samlCookieValue, samlResponsesMap);
            } else {

                //This logic get executed when the uses is accessing a new app for the first time,
                // but have access another app in the same user session.
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Updating the existing session of %s for the app '%s'",
                            (String) samlAttributes.get(APISecurityConstants.SUBJECT),
                            webAppInfoDTO.getContext()));
                }

                Map<String, SAMLTokenInfoDTO> samlResponsesMap = (HashMap<String, SAMLTokenInfoDTO>) getSAML2ConfigCache().get(samlCookieValue);
                String samlIssuer = webAppInfoDTO.getSaml2SsoIssuer();

                if (samlResponsesMap != null && !samlResponsesMap.containsKey(samlIssuer)) {
                    samlResponsesMap.put(samlIssuer, samlTokenInfoDTO);
                    getSAML2ConfigCache().put(samlCookieValue, samlResponsesMap);
                } else { //when accessing though my-subscriptions page
                    samlResponsesMap = new HashMap<String, SAMLTokenInfoDTO>();
                    samlResponsesMap.put(samlIssuer, samlTokenInfoDTO);
                    getSAML2ConfigCache().put(samlCookieValue, samlResponsesMap);
                }
            }

            messageContext.setProperty(AppMConstants.APPM_SAML2_COOKIE, samlCookieValue);

            // Cache the user profile. e.g. subject, roles

            //APISecurityConstants.SUBJECT maps to authenticated userName
            String subject = (String) samlAttributes.get(APISecurityConstants.SUBJECT);
            String roles = getUserRolesFromTheSAMLResponse(samlAttributes);
            cacheUserProfile(samlCookieValue, subject, roles);

            messageContext.setProperty(APISecurityConstants.SUBJECT, subject);

            // Get the authenticated IDP if there is one.
            AuthenticatedIDP[] authenticatedIDP = getAuthenticatedIDP(idpResponseAttributes, samlAttributes);

            if (authenticatedIDP != null) {

                cacheAuthenticatedIDP(samlCookieValue, authenticatedIDP);

                // Set the authenticated IDP in message context, if there is one.
                messageContext.setProperty(APISecurityConstants.AUTHENTICATED_IDP, authenticatedIDP);
            }

            if (isJWTEnabled()) {
                try {
                    generateJWTAndAddToTransportHeaders(messageContext, samlAttributes);
                } catch (AppManagementException e) {
                    log.error("Error while generating the JWT");
                    throw e;
                }

            }
            if (shouldAddSAMLResponseAsTransportHeader()) {
                headers.put(SAML_RESPONSE_HEADER, samlResponse);
                messageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
            }

            if (isLogoutRequest(messageContext)) {

                if (log.isDebugEnabled()) {
                    log.debug("Request is a logout request.");
                }

                handleLogoutRequest(messageContext);
            }

            return true;
        }

        return false;

    }

    /**
     * Caches the given authenticated IDP.
     *
     * @param key
     * @param authenticatedIDPs
     */
    private void cacheAuthenticatedIDP(String key, AuthenticatedIDP[] authenticatedIDPs) {

        Cache cache = Caching.getCacheManager(AppMConstants.AUTHENTICATED_IDP_CACHE_MANAGER)
                .getCache(AppMConstants.AUTHENTICATED_IDP_CACHE);

        cache.put(key, authenticatedIDPs);
    }

    /**
     * Return the cached authenticated IDP for the given key.
     *
     * @param key
     * @return Cached authenticated IDP if there is one, null otherwise.
     */
    private AuthenticatedIDP[] getCachedAuthenticatedIDP(String key) {

        Cache cache = Caching.getCacheManager(AppMConstants.AUTHENTICATED_IDP_CACHE_MANAGER)
                .getCache(AppMConstants.AUTHENTICATED_IDP_CACHE);

        Object cachedObject = cache.get(key);

        if (cachedObject != null && cachedObject instanceof AuthenticatedIDP[]) {
            return (AuthenticatedIDP[]) cachedObject;
        } else {
            return null;
        }
    }

    /**
     * Returns transport headers of the message context.
     *
     * @param messageContext
     * @return Transport headers.
     */
    private Map<String, String> getTransportHeaders(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        return (Map<String, String>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    /**
     * Returns the attributes + subject on the given IDP response attributes.
     *
     * @param idpResponseAttributes A Map which contains the elements which IDP sends. It contains the SAML response and authenticated IDP.
     * @return
     */
    private Map<String, Object> getAttributesOfSAMLResponse(Map<String, String> idpResponseAttributes)
            throws AppManagementException {

        try {
            String samlResponse = idpResponseAttributes.get(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_RESPONSE);
            String decodedSAMLResponse = new String(Base64.decode(samlResponse), CharEncoding.UTF_8);
            XMLObject responseXmlObj = SAMLSSOUtil.unmarshall(decodedSAMLResponse);
            return getResult(responseXmlObj);
        } catch (IdentityException e) {
            String errorMessage = "Can't unmarshall the SAML response.";
            log.error(errorMessage);
            throw new AppManagementException(errorMessage, e);
        } catch (UnsupportedEncodingException e) {
            String errorMessage = "Can't decode the SAML response.";
            log.error(errorMessage);
            throw new AppManagementException(errorMessage, e);
        }

    }

    /**
     * Handles the logout request for an app.
     *
     * @param messageContext
     * @throws SynapseException
     */
    private void handleLogoutRequest(MessageContext messageContext) throws SynapseException, AppManagementException {

        String appmSaml2CookieValue = (String) messageContext.getProperty(AppMConstants.APPM_SAML2_COOKIE);

        messageContext.setProperty("isLogoutRequest", true);
        String encodedSAMLResponse = getCachedSAMLResponse(appmSaml2CookieValue);

        if (encodedSAMLResponse != null) {
            try {
                String decodedSAMLResponse = new String(Base64.decode(encodedSAMLResponse), CharEncoding.UTF_8);
                String samlAssertion = getSamlAssetionString(decodedSAMLResponse);
                XMLObject responseXmlObj = SAMLSSOUtil.unmarshall(samlAssertion);

                Assertion assertion = (Assertion) responseXmlObj;

                if (assertion != null) {
                    String subject = assertion.getSubject().getNameID().getValue();

                    AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);
                    String sessionIndex = authnStatement.getSessionIndex();

                    LogoutRequest logoutReq = buildLogoutRequest(subject, sessionIndex, webAppInfoDTO.getSaml2SsoIssuer());

                    String encodedSamlLogOutRequest = encodeRequestMessage(logoutReq);

                    if (encodedSamlLogOutRequest != null) {
                        getSAML2ConfigCache().remove(appmSaml2CookieValue);
                        sendSAMLRequestToIdP(messageContext, encodedSamlLogOutRequest);
                    } else {
                        throw new SynapseException("Error while sending logout request to IDP.");
                    }
                }

            } catch (IdentityException e) {
                String errorMessage = "Can't unmarshall the SAML response.";
                log.error(errorMessage);
                throw new AppManagementException(errorMessage, e);
            } catch (UnsupportedEncodingException e) {
                String errorMessage = "Can't decode the SAML response.";
                log.error(errorMessage);
                throw new AppManagementException(errorMessage, e);
            }
        }
    }

    private String encodeRequestMessage(RequestAbstractType requestMessage) throws AppManagementException {

        try {
            DefaultBootstrap.bootstrap();

            Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(requestMessage);
            Element authDOM = null;
            authDOM = marshaller.marshall(requestMessage);

            /* Compress the message */
            Deflater deflater = new Deflater(Deflater.DEFLATED, true);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
            StringWriter rspWrt = new StringWriter();
            XMLHelper.writeNode(authDOM, rspWrt);
            deflaterOutputStream.write(rspWrt.toString().getBytes(CharEncoding.UTF_8));
            deflaterOutputStream.close();

            /* Encoding the compressed message */
            String encodedRequestMessage = Base64.encodeBytes(byteArrayOutputStream.toByteArray(), Base64.DONT_BREAK_LINES);
            return URLEncoder.encode(encodedRequestMessage, "UTF-8").trim();

        } catch (MarshallingException e) {
            String errorMessage = "Cannot marshall the SAML request.";
            log.error(errorMessage, e);
            throw new AppManagementException(errorMessage, e);
        } catch (UnsupportedEncodingException e) {
            String errorMessage = "Cannot decode SAML request string to bytes.";
            log.error(errorMessage, e);
            throw new AppManagementException(errorMessage, e);
        } catch (IOException e) {
            String errorMessage = "Cannot write to the output stream.";
            log.error(errorMessage, e);
            throw new AppManagementException(errorMessage, e);
        } catch (ConfigurationException e) {
            String errorMessage = "Error while initializing opensaml library";
            log.error(errorMessage, e);
            throw new AppManagementException(errorMessage, e);
        }
    }


    /**
     * Get the authenticated IDP for this request. User identity is retrieved from samlAttributes and the IDP name is retrieved from idpResponseAttributes.
     *
     * @param idpResponseAttributes
     * @param samlAttributes
     * @return
     */
    private AuthenticatedIDP[] getAuthenticatedIDP(Map<String, String> idpResponseAttributes, Map<String, Object> samlAttributes) {

        // Get the identity
        String identity = (String) samlAttributes.get(APISecurityConstants.CLAIM_EMAIL);

        // Get the name of the IDP.
        String idpName = null;

        String encodedAuthenticatedIDPsString = idpResponseAttributes.get(IDP_CALLBACK_ATTRIBUTE_NAME_AUTHENTICATED_IDPS);

        if (encodedAuthenticatedIDPsString != null) {

            String authenticatedIDPJson = encodedAuthenticatedIDPsString.split("\\.")[1];

            // Sample JSON : {"iss":"wso2","exp":14051608961853000,"iat":1405160896185,
            //                  "idps":[{"idp":"enterprise1","authenticator":"GoogleOpenIDAuthenticator"}]}

            try {
                authenticatedIDPJson = URLDecoder.decode(authenticatedIDPJson, "UTF-8");
                authenticatedIDPJson = new String(Base64.decode(authenticatedIDPJson), CharEncoding.UTF_8);

                JSONObject parsedJson = (JSONObject) JSONValue.parse(authenticatedIDPJson);
                JSONArray idps = (JSONArray) parsedJson.get("idps");
                AuthenticatedIDP[] authenticatedIDPs = new AuthenticatedIDP[idps.size()];

                for (int i = 0; i < idps.size(); i++) {
                    JSONObject authenticatedIDPJSON = (JSONObject) idps.get(i);
                    idpName = authenticatedIDPJSON.get("idp").toString();

                    AuthenticatedIDP authenticatedIDP = new AuthenticatedIDP();
                    authenticatedIDP.setIdentity(identity);
                    authenticatedIDP.setIdpName(idpName);

                    authenticatedIDPs[i] = authenticatedIDP;
                }


                return authenticatedIDPs;

            } catch (Exception e) { // Catches parsing errors
                log.error(String.format("Error while decoding 'AuthenticatedIdps' string value : %s",
                        authenticatedIDPJson), e);
                return null;
            }
        }

        return null;
    }

    /*
     * Process the response and returns the results
	 */
    private Map<String, Object> getResult(XMLObject responseXmlObj) {
        if (responseXmlObj.getDOM().getNodeName().equals("saml2p:LogoutResponse")) {
            return null;
        }

        Response response = (Response) responseXmlObj;
        Assertion assertion = response.getAssertions().get(0);
        Map<String, Object> results = new HashMap<String, Object>();

        /*
           * If the request has failed, the IDP shouldn't send an assertion.
           * SSO profile spec 4.1.4.2 <Response> Usage
           */
        if (assertion != null) {
            String subject = assertion.getSubject().getNameID().getValue();
            results.put(APISecurityConstants.SUBJECT, subject); // get the subject

            //get saml token validity period
            if (assertion.getConditions() != null && assertion.getConditions().getNotOnOrAfter() != null) {
                results.put(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_ASSERTION_NOT_ON_OR_AFTER, assertion.getConditions().getNotOnOrAfter());
            }

            List<AttributeStatement> attributeStatementList = assertion.getAttributeStatements();

            if (attributeStatementList != null) {
                // we have received attributes of user
                Iterator<AttributeStatement> attribStatIter = attributeStatementList.iterator();
                while (attribStatIter.hasNext()) {
                    AttributeStatement statement = attribStatIter.next();
                    List<Attribute> attributesList = statement.getAttributes();
                    Iterator<Attribute> attributesIter = attributesList.iterator();
                    while (attributesIter.hasNext()) {
                        Attribute attrib = attributesIter.next();
                        Element value = attrib.getAttributeValues().get(0).getDOM();
                        String attribValue = value.getTextContent();
                        results.put(attrib.getName(), attribValue);
                    }
                }
            }
        }
        return results;
    }

    private Cache getSAML2ConfigCache() {
        return Caching.getCacheManager(AppMConstants.SAML2_CONFIG_CACHE_MANAGER)
                .getCache(AppMConstants.SAML2_CONFIG_CACHE);
    }

    private Cache getAppContextVersionConfigCache() {
        return Caching.getCacheManager(AppMConstants.APP_CONTEXT_VERSION_CACHE_MANAGER)
                .getCache(AppMConstants.APP_CONTEXT_VERSION_CONFIG_CACHE);
    }

    private Cache getUserRolesCache() {
        return Caching.getCacheManager(AppMConstants.USER_ROLES_CACHE_MANAGER)
                .getCache(AppMConstants.USER_ROLES_CONFIG_CACHE);
    }

    private Cache getSAML2RelayStateCache() {
        return Caching.getCacheManager(AppMConstants.SAML2_RELAY_STATE_CACHE_MANAGER)
                .getCache(AppMConstants.SAML2_RELAY_STATE_CACHE);
    }

    private void redirectToIDPLogin(MessageContext messageContext) throws AppManagementException {
        RequestAbstractType authnRequest = buildAuthnRequestObject(messageContext);
        String encodedSamlRequest = encodeRequestMessage(authnRequest);
        sendSAMLRequestToIdP(messageContext, encodedSamlRequest);
    }

    private AuthnRequest buildAuthnRequestObject(MessageContext messageContext) {

        /* Building Issuer object */
        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuerOb = issuerBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:assertion", "Issuer", "samlp");
        issuerOb.setValue(webAppInfoDTO.getSaml2SsoIssuer());

        /* NameIDPolicy */
        NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
        NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
        nameIdPolicy.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        nameIdPolicy.setSPNameQualifier("Issuer");
        nameIdPolicy.setAllowCreate(true);

        /* AuthnContextClass */
        AuthnContextClassRefBuilder authnContextClassRefBuilder = new AuthnContextClassRefBuilder();
        AuthnContextClassRef authnContextClassRef = authnContextClassRefBuilder.
                buildObject("urn:oasis:names:tc:SAML:2.0:assertion", "AuthnContextClassRef", "saml");
        authnContextClassRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");

        /* AuthnContex */
        RequestedAuthnContextBuilder requestedAuthnContextBuilder = new RequestedAuthnContextBuilder();
        RequestedAuthnContext requestedAuthnContext = requestedAuthnContextBuilder.buildObject();
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);

        DateTime issueInstant = new DateTime();
        String authReqRandomId = Integer.toHexString(new Random().nextInt(100));

        /* Creation of AuthRequestObject */
        AuthnRequestBuilder authRequestBuilder = new AuthnRequestBuilder();
        AuthnRequest authRequest = authRequestBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:protocol",
                "AuthnRequest", "samlp");
        authRequest.setForceAuthn(false);
        authRequest.setIsPassive(false);
        authRequest.setIssueInstant(issueInstant);
        authRequest.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        authRequest.setAssertionConsumerServiceURL(constructAssertionConsumerUrl(messageContext));
        authRequest.setIssuer(issuerOb);
        authRequest.setNameIDPolicy(nameIdPolicy);
        authRequest.setRequestedAuthnContext(requestedAuthnContext);
        authRequest.setID(authReqRandomId);
        authRequest.setDestination(getIDPUrl());
        authRequest.setVersion(SAMLVersion.VERSION_20);

        return authRequest;
    }

    /**
     * get Relay State if available
     *
     * @param messageContext
     * @return relay state path
     */
    private String getRelayState(MessageContext messageContext) {
        String replayState = "";
        String fullRequest = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        String context = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String version = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

        if (!context.endsWith("/")) {
            context += "/";
        }

        if (!version.endsWith("/")) {
            version += "/";
        }

        if (!(context + version).equals(fullRequest)) {

            if (log.isDebugEnabled()) {
                log.debug(String.format("Storing the original resource URL '%s'", fullRequest));
            }

            String urlKey = UUID.randomUUID().toString();
            getSAML2RelayStateCache().put(urlKey, fullRequest);
            replayState = "&" + IDP_CALLBACK_ATTRIBUTE_NAME_RELAY_STATE + "=" + urlKey;
        }
        return replayState;
    }

    private void sendSAMLRequestToIdP(MessageContext messageContext, String request) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        axis2MC.setProperty(NhttpConstants.HTTP_SC, "302");

        messageContext.setResponse(true);
        messageContext.setProperty("RESPONSE", "true");
        messageContext.setTo(null);
        axis2MC.removeProperty("NO_ENTITY_BODY");
        String method = (String) axis2MC.getProperty(Constants.Configuration.HTTP_METHOD);

        if (method.matches("^(?!.*(POST|PUT)).*$")) {
            /* If the request was not an entity enclosing request, send a XML response back */
            axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/xml");
        }

        /* Always remove the ContentType - Let the formatter do its thing */
        axis2MC.removeProperty(Constants.Configuration.CONTENT_TYPE);
        Map headerMap = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);


        headerMap.put("Location", getIDPUrl() + "?SAMLRequest=" + request + getRelayState(messageContext));

        if (messageContext.getProperty("error_message_type") != null &&
                messageContext.getProperty("error_message_type").toString().equalsIgnoreCase("application/json")) {
            axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/json");
        }

        headerMap.remove(HttpHeaders.HOST);
        Axis2Sender.sendBack(messageContext);
    }

    private LogoutRequest buildLogoutRequest(String user, String sessionIdx, String saml2Issuer) {

        LogoutRequest logoutReq = new LogoutRequestBuilder().buildObject();

        logoutReq.setID(UUID.randomUUID().toString());
        logoutReq.setDestination(getIDPUrl());

        DateTime issueInstant = new DateTime();
        logoutReq.setIssueInstant(issueInstant);
        logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + 5 * 60 * 1000));

        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(saml2Issuer);
        logoutReq.setIssuer(issuer);

        NameID nameId = new NameIDBuilder().buildObject();
        nameId.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        nameId.setValue(user);
        logoutReq.setNameID(nameId);

        SessionIndex sessionIndex = new SessionIndexBuilder().buildObject();
        sessionIndex.setSessionIndex(sessionIdx);
        logoutReq.getSessionIndexes().add(sessionIndex);

        logoutReq.setReason("Single Logout");

        return logoutReq;
    }


    private String getCookieValue(String cookieString, String cookieName) {
        if (cookieString != null && !cookieString.isEmpty()) {
            int cStart = cookieString.indexOf(cookieName + "=");
            int cEnd;
            if (cStart != -1) {
                cStart = cStart + cookieName.length() + 1;
                cEnd = cookieString.indexOf(";", cStart);
                if (cEnd == -1) {
                    cEnd = cookieString.length();
                }
                return cookieString.substring(cStart, cEnd);
            }
        }
        return null;
    }

    private void handleAuthFailure(MessageContext messageContext, APISecurityException e) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, e.getErrorCode());
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE,
                APISecurityConstants.getAuthenticationFailureMessage(e.getErrorCode()));
        messageContext.setProperty(SynapseConstants.ERROR_EXCEPTION, e);

        Mediator sequence = messageContext.getSequence(APISecurityConstants.API_AUTH_FAILURE_HANDLER);
        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }

        // By default we send a 401 response back
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/soap+xml");

        Map<String, String> headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        int status;
        if (e.getErrorCode() == APISecurityConstants.API_AUTH_GENERAL_ERROR) {
            status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        } else if (e.getErrorCode() == APISecurityConstants.API_AUTH_INCORRECT_API_RESOURCE || e.getErrorCode() == APISecurityConstants.API_AUTH_FORBIDDEN) {
            status = HttpStatus.SC_FORBIDDEN;
        } else {
            status = HttpStatus.SC_UNAUTHORIZED;
            headers = new HashMap<String, String>();
            headers.put(HttpHeaders.WWW_AUTHENTICATE, authenticator.getChallengeString());
        }

        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload(e));
        } else {
            Utils.setSOAPFault(messageContext, "Client", "Authentication Failure", e.getMessage());
        }
        if (Utils.isCORSEnabled()) {
        	/* For CORS support adding required headers to the fault response */
            headers.put(AppMConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, Utils.getAllowedOrigin(authenticator.getRequestOrigin()));
            headers.put(AppMConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS, Utils.getAllowedMethods());
            headers.put(AppMConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS, Utils.getAllowedHeaders());
        }

        // Remove App Manager specific security headers.
        removeSecurityHeaders(headers);

        // Set the amended HTTP headers.
        axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);

        Utils.sendFault(messageContext, status);
    }

    private void removeSecurityHeaders(Map<String, String> headers) {
        headers.remove(authenticator.getSecurityContextHeader());
        headers.remove(SAML_RESPONSE_HEADER);
    }

    private OMElement getFaultPayload(APISecurityException e) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APISecurityConstants.API_SECURITY_NS,
                APISecurityConstants.API_SECURITY_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(e.getErrorCode()));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(APISecurityConstants.getAuthenticationFailureMessage(e.getErrorCode()));
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(e.getMessage());

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }

    private String getSamlAssetionString(String samlResponse) {
        return samlResponse.substring(samlResponse.indexOf("<saml2:Assertion"),
                samlResponse.indexOf("</saml2:Assertion>")) + "</saml2:Assertion>";

    }

    /**
     * Returns the URL of the IDP.
     *
     * @return
     */
    private String getIDPUrl() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
                getFirstProperty(AppMConstants.SSO_CONFIGURATION_IDENTITY_PROVIDER_URL);
    }

    /**
     * Returns the status of enabledsaml sso configuraion.
     *
     * @return
     */
    private boolean getSamlSSOConfiguration() {
        return Boolean.parseBoolean(ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
                getFirstProperty(AppMConstants.SSO_CONFIGURATION_ENABLE_SSO_CONFIGURATION));
    }

    /**
     * Checks whether the SAML response should be added as a transport header.
     *
     * @return
     */
    private boolean shouldAddSAMLResponseAsTransportHeader() {
        return Boolean.parseBoolean(ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
                getFirstProperty(AppMConstants.API_CONSUMER_AUTHENTICATION_ADD_SAML_RESPONSE_HEADER_TO_OUT_MSG));
    }

    /**
     * Returns decoded  IDP response attributes.
     *
     * @param messageContext
     * @return A Map which contains the elements which IDP sends. It contains the SAML response and authenticated IDP.
     */
    private Map<String, String> getIDPResponseAttributes(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        Map<String, String> idpResponseAttributes = new HashMap<String, String>();

        try {
            RelayUtils.buildMessage(axis2MessageContext);
        } catch (Exception e) {
            log.error("Error while retrieving IDP response attributes.", e);
            return idpResponseAttributes;
        }

        Iterator iterator = messageContext.getEnvelope().getBody().getChildElements();
        while (iterator.hasNext()) {
            OMElement bodyElement = (OMElement) iterator.next();

            // Get SAML response.
            Iterator children = bodyElement.getChildrenWithName(new QName(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_RESPONSE));
            while (children.hasNext()) {
                OMElement ele = (OMElement) children.next();
                idpResponseAttributes.put(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_RESPONSE, ele.getText());
            }

            //Get SAML Assertion
            children = bodyElement.getChildrenWithName(new QName(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_ASSERTION));
            while (children.hasNext()) {
                OMElement ele = (OMElement) children.next();
                idpResponseAttributes.put(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_ASSERTION, ele.getText());
            }

            // Get authenticated IDPs.
            children = bodyElement.getChildrenWithName(new QName(IDP_CALLBACK_ATTRIBUTE_NAME_AUTHENTICATED_IDPS));
            while (children.hasNext()) {
                OMElement ele = (OMElement) children.next();
                idpResponseAttributes.put(IDP_CALLBACK_ATTRIBUTE_NAME_AUTHENTICATED_IDPS, ele.getText());
            }

        }

        return idpResponseAttributes;
    }

    private String getTenantDomainFromGatewayUrl(String gatewayUrl) {
        String tenantDomain = "";
        //"http://10.100.1.85:8280/t/you.com/tommy/1.0.0/";
        if (gatewayUrl.contains("/t/")) {
            String tmp;
            tmp = gatewayUrl.substring(gatewayUrl.indexOf("/t/") + 3);
            tenantDomain = tmp.substring(0, tmp.indexOf("/"));
        }
        return tenantDomain;
    }

    private void constructAndSetFullyQualifiedSamlIssuerId(MessageContext messageContext, WebAppInfoDTO webAppInfoDTO) {
        String assertionConsumerUrl = constructAssertionConsumerUrl(messageContext);
        String tenantDomain = getTenantDomainFromGatewayUrl(assertionConsumerUrl);
        String version = (String) messageContext.getProperty("SYNAPSE_REST_API_VERSION");

        String fullyQualifiedIssuer = webAppInfoDTO.getSaml2SsoIssuer();

        if (!tenantDomain.equals("")) {
            fullyQualifiedIssuer = fullyQualifiedIssuer + "-" + tenantDomain;
        }

        //Append version to SP name
        fullyQualifiedIssuer = fullyQualifiedIssuer + "-" + version;

        webAppInfoDTO.setSaml2SsoIssuer(fullyQualifiedIssuer);
    }

    private String constructAssertionConsumerUrl(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        Map<String, String> headers = (Map<String, String>) axis2MC.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String servicePrefix = axis2MC.getProperty("SERVICE_PREFIX").toString();
        //Note: Do not change to construct the assertionConsumerUrl directly using servicePrefix instead of headers.get("HOST").
        //It always gives IP for host which cause invalid assertionConsumerUrl
        String assertionConsumerUrl = servicePrefix.substring(0, servicePrefix.indexOf("/") + 2) +
                headers.get("HOST") + messageContext.getProperty("REST_API_CONTEXT") +
                "/" + messageContext.getProperty("SYNAPSE_REST_API_VERSION") + "/";

        return assertionConsumerUrl;
    }

    /**
     * Sets the SAML SSO cookie.
     *
     * @param headers
     * @param appmSamlSsoCookie
     */
    private void setSamlSsoCookie(Map<String, String> headers, String appmSamlSsoCookie) {

        String cookieString = (String) headers.get(HTTPConstants.HEADER_SET_COOKIE);

        String samlCookie = String.format("%s=%s; path=/", AppMConstants.APPM_SAML2_COOKIE, appmSamlSsoCookie);

        if (cookieString == null) {
            cookieString = samlCookie;
        } else {
            cookieString = cookieString + "; \nSet-Cookie: " + samlCookie;
        }

        headers.put(HTTPConstants.HEADER_SET_COOKIE, cookieString);

    }

}
