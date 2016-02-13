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
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
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
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.LogoutRequestBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.opensaml.saml2.core.impl.SessionIndexBuilder;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.AuthenticatedIDP;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.gateway.handlers.Utils;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.appmgt.gateway.handlers.security.Authenticator;
import org.wso2.carbon.appmgt.gateway.handlers.security.oauth.OAuthAuthenticator;
import org.wso2.carbon.appmgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.dto.SAMLTokenInfoDTO;
import org.wso2.carbon.appmgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.appmgt.impl.dto.WebAppInfoDTO;
import org.wso2.carbon.appmgt.impl.token.JWTGenerator;
import org.wso2.carbon.appmgt.impl.token.TokenGenerator;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    private static final String HTTP_HEADER_HOST = "HOST";
    private static final String PROPERTY_REST_API_CONTEXT = "REST_API_CONTEXT";

    private Authenticator authenticator;
    private SAML2Authenticator saml2Authenticator;
    private WebAppInfoDTO webAppInfoDTO;
    private WebApp webApp;
    private boolean isJWTEnabled = false;
    private TokenGenerator defaultTokenGenerator;

    public void init(SynapseEnvironment synapseEnvironment) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing WebApp authentication handler instance");
        }

        try {
            authenticator = new OAuthAuthenticator();
            saml2Authenticator = new SAML2Authenticator();
            webApp = new WebApp();
            initJWTCapabilities();
        } catch (Exception e) {
            // Just throw it here - Synapse will handle it
            throw new SynapseException("Error while initializing SAML or OAuth authenticator", e);
        }

        //Initialize the context cache by calling AppContextCacheUtil to pre-load cache
        AppContextCacheUtil.getTenantContextVersionUrlMap();

        authenticator.init(synapseEnvironment);
        saml2Authenticator.init(synapseEnvironment);
        defaultTokenGenerator = new JWTGenerator();
    }


    /**
     * Applies SAML 2 authentication if SSO is enabled.
     * @param messageContext
     * @return
     */
    public boolean handleRequest(MessageContext messageContext) {

         // application context
        String webAppContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        // application version
        String webAppVersion =
                (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

        // http verb (eg: GET,POST)
        String httpVerb = (String) messageContext.getProperty(Constants.Configuration.HTTP_METHOD);
        if(httpVerb == null) {
            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext();
            httpVerb =   (String) axis2MC.getProperty(Constants.Configuration.HTTP_METHOD);
        }
        // request full path (eg: /context/version/pattern)
        String fullReqPath =
                (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);

        try {
            // Get App Info related to SSO handling.
            if (webAppInfoDTO == null) {
                WebAppInfoDTO webAppInfoDTO = getSSOInfoForApp(webAppContext, webAppVersion);
                constructAndSetFullyQualifiedSamlIssuerId(messageContext, webAppInfoDTO);
                this.webAppInfoDTO = webAppInfoDTO;
                populateWebAppFromWebAppInfoDTO();
            }

            // If this is an SLO request we need to respond to the client (IDP) without continuing the flow. 
            if(isSLORequestFromIDP(messageContext)){
                if (log.isDebugEnabled()) {
                    log.debug("Request is an SLO request from the IDP");
                }

                handleSLORequest(messageContext);

                if (log.isDebugEnabled()) {
                    log.debug("Sending SLO response to the IDP");
                }
            	sendSLOResponse(messageContext);
            	return false;
            }
            

            // check if anonymous mode allowed for the entire app
            boolean isAllowAnonymousApp = isAllowAnonymousApplication();
            // write to messageContext so then the same value can be accessed as a
            // property in other handlers
            messageContext.setProperty(AppMConstants.API_OVERVIEW_ALLOW_ANONYMOUS, isAllowAnonymousApp);
            if (isAllowAnonymousApp) {
                // if anonymous mode is allowed move to next handler
                return true;
            }

            // check if anonymous mode allowed for particular URL pattern
            boolean isAllowAnonymousUrl = isAllowAnonymousUrlPattern(httpVerb, fullReqPath);
            // write to messageContext so then the same value can be accessed as a
            // property in other handlers
            messageContext.setProperty(AppMConstants.API_URI_ALLOW_ANONYMOUS, isAllowAnonymousUrl);
            if (isAllowAnonymousUrl) {
                // if anonymous mode is allowed move to next handler
                return true;
            }

            // If SSO is not enabled skip this authentication handler.
            if (!isSSOEnabled()) {
                return true;
            }

            boolean isAuthorized = false;
            boolean isResourceAccessible = false;

            if (shouldAuthenticateWithCookie(messageContext)) {
            	messageContext.setProperty(AppMConstants.APPM_SAML2_CACHE_HIT, 1);
                isAuthorized = handleSecurityUsingCookie(messageContext);
            } else if (shouldAuthenticateWithSAMLResponse(messageContext)) {

                if (log.isDebugEnabled()) {
                    log.debug("Processing SAML response");
                }

            	messageContext.setProperty(AppMConstants.APPM_SAML2_CACHE_HIT, 0);
                
            	isAuthorized = handleAuthorizationUsingSAMLResponse(messageContext);
                

                if (isAuthorized) {

                    String appmSamlSsoCookie = (String) messageContext.getProperty(AppMConstants.APPM_SAML2_COOKIE);
                    Map<String, Object> headers = (Map<String, Object>) getAxis2MessageContext(messageContext).getProperty(
                                                        org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                    String cookieString = (String) headers.get(HTTPConstants.HEADER_SET_COOKIE);

                    if (cookieString == null) {
                        cookieString = AppMConstants.APPM_SAML2_COOKIE + "=" + appmSamlSsoCookie + "; " + "path=" + "/";
                    } else {
                        cookieString = cookieString + " ;" + "\nSet-Cookie:" + AppMConstants.APPM_SAML2_COOKIE + "=" + appmSamlSsoCookie + ";" + " Path=" + "/";
                    }
                    headers.put(HTTPConstants.HEADER_SET_COOKIE, cookieString);
                    messageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);

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

            if (isAuthorized) {
            	if (!isLogoutRequest(messageContext)) {

                	if (checkResourceAccessible(messageContext)) {
                		setAppmSamlSsoCookie(messageContext);
                	}else{
                		handleAuthFailure(messageContext,
                				new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN, "You have no access to" +
                                        " this Resource"));
                		return false;
                	}
                    
                	//Include appmSamlSsoCookie to "Cookie" header before request send to backend
                }
                
                return true;
            } else {
                redirectToIDPLogin(messageContext);
                return false;
            }
        } catch (AppManagementException e) {
        	String errorMessage = "Error while handling authentication.";
            log.error(errorMessage);
            throw new SynapseException(errorMessage, e);
        }
    }

    private void populateWebAppFromWebAppInfoDTO() {
        if (webAppInfoDTO != null) {
            webApp.setContext(webAppInfoDTO.getContext());
            webApp.setAllowAnonymous(webAppInfoDTO.getAllowAnonymous());
            webApp.setSaml2SsoIssuer(webAppInfoDTO.getSaml2SsoIssuer());
            webApp.setIdpProviderURL(webAppInfoDTO.getIdpProviderUrl());
            webApp.setLogoutURL(webAppInfoDTO.getLogoutUrl());
            //TODO: Set all the properties in WebApp object.
        }
    }

    private void initJWTCapabilities() {
        AppManagerConfiguration configuration = org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder.
                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (configuration == null) {
            log.error("App Manager configuration is not initialized");
        } else {
            isJWTEnabled = configuration.isJWTEnabled();
        }
    }

	/**
     * Check if the Anonymous Access is allowed for the overall app
     *
     * @return result : boolean result relevant to registry value
     */
    private boolean isAllowAnonymousApplication() {
        return webAppInfoDTO.getAllowAnonymous();
    }

    /**
     * Check if the Anonymous Access is allowed for the particular URL pattern
     *
     * @return boolean result either anonymous access allowed or not
     * @throws AppManagementException 
     */
    private boolean isAllowAnonymousUrlPattern(String httpVerb, String requestPath) throws AppManagementException {

        // Get App URL Pattern Info
        VerbInfoDTO httpVerbInfo = getVerbInfoForApp(webAppInfoDTO.getContext(), webAppInfoDTO.getVersion());

        if (httpVerbInfo != null && httpVerbInfo.isEmptyAllowAnonymousUrlMap() == false) {

        	NamedMatchList<String> matcher = new NamedMatchList<String>();

            for (String pattern : httpVerbInfo.getAllowAnonymousUrlList()) {
                matcher.add(pattern, pattern);
            }

            String httpVerbAndRequestPath = httpVerb + requestPath;
        	
        	String matchedPattern = matcher.match(httpVerbAndRequestPath);

            if(matchedPattern == null) {
                return false;
            }
        	
        	Boolean allowAnonymous = httpVerbInfo.getAllowAnonymousUrl(matchedPattern);
        	
        	if(allowAnonymous != null){
        		return allowAnonymous;
        	}
        	
        }
        
        return false;
        
    }
    
    public boolean handleResponse(MessageContext messageContext) {
        
    	try {
			if (isAllowAnonymousApplication() ||
			    isAllowAnonymousUrlPattern((String) messageContext.getProperty("REST_METHOD"),
			                               (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH))) {
			    return true;
			}
        
	        String appmSamlSsoCookie = (String) messageContext.getProperty(AppMConstants.APPM_SAML2_COOKIE);
	
	        if (log.isDebugEnabled()) {
	            log.debug("Reading AppMConstants.APPM_SAML2_COOKIE from msg context");
	            log.debug(AppMConstants.APPM_SAML2_COOKIE + " : " + appmSamlSsoCookie);
	        }
	
	        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
	                getAxis2MessageContext();
	        Map<String, Object> headers = (Map<String, Object>) axis2MC.getProperty(
	                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
	        String cookieString = (String) headers.get(HTTPConstants.HEADER_SET_COOKIE);
	
	        if (log.isDebugEnabled()) {
	            log.debug("Exisiting set cookie string in transport headers : " + cookieString);
	        }
	
	        if (cookieString == null) {
	            cookieString = AppMConstants.APPM_SAML2_COOKIE + "=" + appmSamlSsoCookie + "; " + "path=" + "/";
	        } else {
	            cookieString = cookieString + " ;" + "\nSet-Cookie:" + AppMConstants.APPM_SAML2_COOKIE + "=" + appmSamlSsoCookie + ";" + " Path=" + "/";
	        }
	        if (log.isDebugEnabled()) {
	            log.debug("Updated set cookie string in transport headers : " + cookieString);
	        }
	        headers.put(HTTPConstants.HEADER_SET_COOKIE, cookieString);
	        messageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
	
	        return true;
    	} catch (AppManagementException e) {
    		String errorMessage = "Error while handling authentication.";
    		log.error(errorMessage);
    		throw new SynapseException(errorMessage, e);
    	}
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
            axis2MessageContext.setProperty("NO_ENTITY_BODY", new Boolean("true"));
            messageContext.setProperty("RESPONSE", "true");
            messageContext.setTo(null);
            Axis2Sender.sendBack(messageContext);
        }
	}

    private void handleSLORequest(MessageContext messageContext) throws AppManagementException {
        //Handle the SLO request to this app, from the IDP.
        try {
            String encodedRequest = null;
            SOAPBody soapBody = messageContext.getEnvelope().getBody();
            Iterator iterator = soapBody.getChildElements();

            while (iterator.hasNext()) {
                OMElement bodyElement = (OMElement) iterator.next();
                // Get SAML SLO request.
                OMElement element = bodyElement.getFirstChildWithName(new QName(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_REQUEST));
                if (element != null)
                    encodedRequest = element.getText();
            }

            if (encodedRequest != null) {
                String decodedSAMLResponse = new String(Base64.decode(encodedRequest), CharEncoding.UTF_8);
                XMLObject requestXmlObj = SAMLSSOUtil.unmarshall(decodedSAMLResponse);
                if (requestXmlObj instanceof LogoutRequest) {
                    LogoutRequest logoutRequest = (LogoutRequest) requestXmlObj;
                    String sessionIndex = logoutRequest.getSessionIndexes().get(0).getSessionIndex();
                    if (sessionIndex != null) {
                        String appmSaml2CookieValue = (String) getSAML2SessionIndexCache().get(sessionIndex);
                        getSAML2SessionIndexCache().remove(sessionIndex);
                        if (appmSaml2CookieValue != null)
                            getSAML2ConfigCache().remove(appmSaml2CookieValue);
                    } else {
                        throw new AppManagementException("SessionIndex not found in single logout request.");
                    }
                } else {
                    throw new AppManagementException("Invalid single logout request.");
                }
            } else {
                throw new AppManagementException("Couldn't find single logout request.");
            }

        } catch (IdentityException e) {
            String errorMessage = "Couldn't unmarshal the  single logout request.";
            log.error(errorMessage);
            throw new AppManagementException(errorMessage, e);
        } catch (UnsupportedEncodingException e) {
            String errorMessage = "Couldn't decode the single logout request.";
            log.error(errorMessage);
            throw new AppManagementException(errorMessage, e);
        }

    }


    private boolean isSLORequestFromIDP(MessageContext messageContext) {
		
    	org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        try {
            RelayUtils.buildMessage(axis2MessageContext);
        } catch (Exception e) {
        	String errorMessage = "Error while building the message.";
            log.error(errorMessage,e);
            throw new SynapseException(errorMessage, e);
        }

        SOAPBody soapBody = messageContext.getEnvelope().getBody();

        if(soapBody != null) {
            
            // Try to get the SAML request in the SOAP body.
        	// The expected structure is <body><mediate><SamlRequest></SamlRequest></mediate></body>
        	
        	if(soapBody.getChildren().hasNext()){
        		// Check whether there is a SAML request in the SOAP body.
        		Iterator possibleSAMLRequestElements = ((OMElement)(soapBody.getChildren().next())).getChildrenWithName(new QName(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_REQUEST));
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
    private boolean shouldAuthenticateWithCookie(MessageContext messageContext) {

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
     * @param messageContext
     * @return true if the request should be authenticated using the SAML response from the IDP, false otherwise.
     */
    private boolean shouldAuthenticateWithSAMLResponse(MessageContext messageContext){
        Map<String, String> idpResponseAttributes = getIDPResponseAttributes(messageContext);
        if (log.isDebugEnabled()) {
            log.debug("shouldAuthenticateWithSAMLResponse" + messageContext);
            log.debug("idpResponseAttributes.get(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_RESPONSE) : " + idpResponseAttributes.get(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_RESPONSE));
        }
        return idpResponseAttributes.get(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_RESPONSE) != null;
    }

    /**
     * Returns the cookie string of the request.
     * @param messageContext
     * @return The cookie string of the request.
     */
    private String getCookieString(MessageContext messageContext){
        Map<String, String> headers = getTransportHeaders(messageContext);
        return headers.get(HTTPConstants.COOKIE_STRING);
    }

    /**
     * Returns the SAML cookie value.
     * @param messageContext
     * @return SAML cookie value.
     */
    private String getSAMLCookie(MessageContext messageContext){
        String cookieString = getCookieString(messageContext);
        if (log.isDebugEnabled()) {
            log.debug("Requesting cookie : " + AppMConstants.APPM_SAML2_COOKIE + " value : " + cookieString +
                      " getCookieValue() : " + getCookieValue(cookieString, AppMConstants.APPM_SAML2_COOKIE));
        }
        return getCookieValue(cookieString, AppMConstants.APPM_SAML2_COOKIE);
    }

    /**
     * Checks whether the SAML response is in cache.
     * @param samlResponseKey
     * @return true if the SAML response is in cache, false otherwise.
     */
    private boolean isSamlResponseInCache(String samlResponseKey){
        return getCachedSAMLResponse(samlResponseKey) != null;
    }

    /**
     * Returns SAML response from cache.
     * @param cacheKey value of appmSamlSsoTokenId cookie value
     * @return The SAML response for the given key if the result Map contains the samlResponse of
     * webApp in cache , null otherwise.
     */
    public String getCachedSAMLResponse(String cacheKey){
        Object response = getSAML2ConfigCache().get(cacheKey);
        if(response != null){
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

    private String getCachedSessionIndex(String cacheKey) {
        Object response = getSAML2ConfigCache().get(cacheKey);
        String sessionIndex = null;
        if (response != null) {
            Map<String, SAMLTokenInfoDTO> samlResponsesMap = (HashMap<String, SAMLTokenInfoDTO>) response;
            SAMLTokenInfoDTO samlTokenInfoDTO = samlResponsesMap.get(webAppInfoDTO.getSaml2SsoIssuer());

            if (samlTokenInfoDTO != null) {
                sessionIndex = samlTokenInfoDTO.getSessionIndex();
            }
        }

        return sessionIndex;
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
     * @param cacheKey
     * @return The SAML response for the given key if the response is in cache, null otherwise.
     */
    public String getCachedUserRoles(String cacheKey){
        Object response = getSAML2ConfigCache().get(cacheKey);
        if(response != null){
            return (String) response;
        }

        return null;
    }

    /**
     * Returns the user from cache.
     * @param cacheKey
     * @return The cached user for the given key if the user is in cache, null otherwise.
     */
    private String getCachedLoggedInUser(String cacheKey){
        return (String) saml2Authenticator.getKeyCache().get(cacheKey);
    }

    /**
     * Checks whether the given SAML response is authenticated.
     * @param samlAttributes
     * @return
     */
    public boolean isSAMLResponseAuthenticated(Map<String, Object> samlAttributes) {

        if(samlAttributes != null) {
            return samlAttributes.get(APISecurityConstants.SUBJECT) != null;
        }

        return false;
    }

    /**
     * Returns the Axis2 message context from the Synapse message context.
     * @param messageContext
     * @return Axis2 message context.
     */
    private org.apache.axis2.context.MessageContext getAxis2MessageContext(MessageContext messageContext){
        return ((Axis2MessageContext) messageContext).getAxis2MessageContext();
    }

    /**
     * Checks whether the request is a logout request.
     * @param messageContext
     * @return true if the request is a logout request, false otherwise.
     */
    private boolean isLogoutRequest(MessageContext messageContext)  {

        String inURL = getAxis2MessageContext(messageContext).getProperty("TransportInURL").toString();

        String logoutUrl = webAppInfoDTO.getLogoutUrl();
        if (logoutUrl != null && logoutUrl.endsWith(inURL)) {
            if(log.isDebugEnabled()){
            	log.debug("Logout URL Encountered");
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether SSO is enabled in App Manager config.
     * @return true if SSO is enabled, false otherwise.
     */
    private boolean isSSOEnabled(){
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
        return AppMDAO.getVerbConfigInfo(webAppContext, webAppVersion);
    }

    /**
     * Checks whether the signed in user is subscribed to the requested app.
     * @param messageContext
     * @return true if the signed in user is subscribed to the requested app.
     * @throws APISecurityException if there is an error in subscription check.
     */
    private boolean isSubscribed(MessageContext messageContext) throws APISecurityException {
        return  saml2Authenticator.authenticate(messageContext);
    }

    /**
     * Adds cached JWT for signed in user, to the transport headers.
     * @param messageContext
     */
    private void addCachedJWTToTransportHeaders(MessageContext messageContext){

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
     * @param messageContext
     * @param samlAttributes SAML attributes extracted from the SAML response. Even though it can be extracted again
     *                       using the passed message context, It's better to pass already extracted SAML attributes
     *                       for performance's sake.
     * @throws org.wso2.carbon.appmgt.api.AppManagementException when there is an error in generating JWT.
     */
    private void generateJWTAndAddToTransportHeaders(MessageContext messageContext, Map<String, Object> samlAttributes,
                                                     WebApp webApp) throws AppManagementException {

        String jwtCacheKey = (String) messageContext.getProperty(AppMConstants.APPM_SAML2_COOKIE);

        // Generate Token and update Cache.
        Cache jwtCache = Caching.getCacheManager(AppMConstants.API_MANAGER_CACHE_MANAGER).getCache(
                AppMConstants.JWT_CACHE_NAME);

        String jwtToken = getTokenGenerator().generateToken(samlAttributes, webApp, messageContext);
        jwtCache.put(jwtCacheKey, jwtToken);

        //Add JWT Token into transport headers.
        Map<String, String> headers = getTransportHeaders(messageContext);

        headers.put(authenticator.getSecurityContextHeader(), jwtToken);
//        messageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
    }

    /**
     * Handles authentication and authorization using cookie.
     * @param messageContext
     * @return true if the user is authorized to access the resource, false otherwise.
     */
    private boolean handleSecurityUsingCookie(MessageContext messageContext){

        Map<String, String> headers = getTransportHeaders(messageContext);

        String samlCookieValue = getSAMLCookie(messageContext);

        // If the SAML response is available in cache.
        if (isSamlResponseInCache(samlCookieValue)) {

            //Check whether saml token has expired
            if (isSamlTokenExpired(samlCookieValue)) {
                String sessionIndex = getCachedSessionIndex(samlCookieValue);
                if (sessionIndex != null) {
                    getSAML2SessionIndexCache().remove(sessionIndex);
                }
                getSAML2ConfigCache().remove(samlCookieValue);
                return false;
            }

            String loggedInUser = getCachedLoggedInUser(samlCookieValue);
            AuthenticatedIDP[] authenticatedIDP = getCachedAuthenticatedIDP(samlCookieValue);

            try {

                messageContext.setProperty(APISecurityConstants.SUBJECT, loggedInUser);
                messageContext.setProperty(APISecurityConstants.AUTHENTICATED_IDP, authenticatedIDP);
                messageContext.setProperty(AppMConstants.APPM_SAML2_COOKIE, samlCookieValue);

                if (isLogoutRequest(messageContext)) {
                    handleLogoutRequest(messageContext);
                    return true;
                } else if (isSubscribed(messageContext)) { // Has subscribed
                    //TODO: check the expiration time

                    //TODO: Handle if JWT Cache expires while saml2Cookie active
                    if (isJWTEnabled) { //JWT has enabled
                        addCachedJWTToTransportHeaders(messageContext);
                    }

                    if (shouldAddSAMLResponseAsTransportHeader()) {
                        headers.put(AppMConstants.APPM_SAML2_RESPONSE, getCachedSAMLResponse(samlCookieValue));
                        messageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
                    }

                    return true;
                }else{ // Not subscribed.
                    handleAuthFailure(messageContext, new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                                                        "You have not subscribed to this Application"));

                    return false;
                }
            } catch (APISecurityException e) {
                handleAuthFailure(messageContext,
                        new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN, "You have not subscribe to this Application"));
            }

        }

        return false;
    }

    /**
     * Gets the SamlResponce and return the userRoles According to the Resource Required.
     * @return true if the user is allowed to access the resource.
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     * Role claim should be passed by default
     */

    private boolean checkResourceAccessible(MessageContext synapseMessageContext){

        Map<String, String> idpResponseAttributes = null;
        Map<String, Object> samlAttributes =null;
        String inUrl = getAxis2MessageContext(synapseMessageContext).getProperty("TransportInURL").toString();
        String webAppContext = (String) synapseMessageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String webAppVersion = (String) synapseMessageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

        String roles = getCachedUserRoles(AppMConstants.USER_ROLES_CACHE_KEY);

        org.apache.axis2.context.MessageContext axis2MsgContext;
        axis2MsgContext = ((Axis2MessageContext) synapseMessageContext).getAxis2MessageContext();

        String httpVerb = (String) axis2MsgContext.getProperty(Constants.Configuration.HTTP_METHOD);

        int appID = webAppInfoDTO.getAppID();

        ArrayList<String> mappingList = new ArrayList<String>();
        ArrayList<String> mapperList = new ArrayList<String>();
        AppMDAO appMDAO = new AppMDAO();

        try{

            mappingList = appMDAO.getInUrlMappingById(appID);

            for(String mapping : mappingList){
                String mapperUrl = webAppContext+"/"+webAppVersion+mapping;
                mapperList.add(mapperUrl);
            }
            Collections.sort(mapperList);
            Collections.reverse(mapperList);

            String matched = getMatchedURLPattern(mapperList,inUrl);
                if(matched!=null){
                    String urlMapping = matched.substring((webAppContext+"/"+webAppVersion).length(),matched.length());
                    
                    // Set the relevant synapse properties to let the entitlement handler do its job properly.
                    synapseMessageContext.setProperty(AppMConstants.MATCHED_URL_PATTERN_PROERTY_NAME, urlMapping);
                    synapseMessageContext.setProperty(AppMConstants.MATCHED_APP_ID_PROERTY_NAME, appID);
                    
                    if (checkResourseAccessibleByRole(urlMapping, roles, appID, httpVerb)){
                        return true;
                    }
                    else{
                        return false;
                    }
                }
                else{
                    return true;
                }


        }catch(AppManagementException e){
            log.error("Failed to check resources for user");
            return false;
        }
    }

    private String getUserRolesFromTheSAMLResponse(Map<String,Object> samlAttributes){
        if(samlAttributes != null) {
            if(samlAttributes.get(APISecurityConstants.CLAIM_ROLES)!=null){
                return (String)samlAttributes.get(APISecurityConstants.CLAIM_ROLES);
            }
            else{
                return "";
            }
        }

        return "";
    }


    /**
     * Check weather the mapping url has the roles
     * @param urlMapping
     * @param roles
     * @return true if the user is permitted to access the resource.
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    private boolean checkResourseAccessibleByRole(String urlMapping, String roles, int appId,
            String httpVerb) throws AppManagementException {

        AppMDAO appMDAO = new AppMDAO();
        String toBeMatchedRoles = appMDAO.getInUrlMappingRoles(appId, urlMapping, httpVerb);
        if (roles == null || roles.equalsIgnoreCase("")) {
            return true;
        }
        if (toBeMatchedRoles == null || toBeMatchedRoles.equals("")) {
            return true;
        }

        String toBeMatchedRole[] = getDelimitedRoles(toBeMatchedRoles);
        String contextRoles[] = getDelimitedRoles(roles);

        for (int i = 0; i < contextRoles.length; i++) {
            for (int j = 0; j < toBeMatchedRole.length; j++) {
                if (contextRoles[i].equalsIgnoreCase(toBeMatchedRole[j])) {
                    return true;
                }
            }
        }
        return false;
    }

    private String[] getDelimitedRoles(String input){

        String [] arr = input.split (",");
        return arr;
    }

    /**
     * Check weather the mapping url has the roles
     * @param patternList
     * @param inUrl
     * @return true if the user is permitted to access the resource.
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */

    private String getMatchedURLPattern(ArrayList<String> patternList,String inUrl){
        NamedMatchList<String> matcher = new NamedMatchList<String>();

        for(String pattern : patternList){
            matcher.add(pattern,pattern);
        }

        String matched = matcher.match(inUrl);
        return  matched;

    }

    /**
     * Handles authentication and authorization using SAML response.
     * @param messageContext
     * @return true if the user is authorized for the requested resource.
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    private boolean handleAuthorizationUsingSAMLResponse(MessageContext messageContext)  {

        Map<String, String> headers = getTransportHeaders(messageContext);

        Map<String, String> idpResponseAttributes = getIDPResponseAttributes(messageContext);

        Map<String, Object> samlAttributes = getAttributesOfSAMLResponse(idpResponseAttributes);

        if (isSAMLResponseAuthenticated(samlAttributes)) {
            // Set the cookie value.
            String samlCookieValue = getSAMLCookie(messageContext);
            String samlResponse = idpResponseAttributes.get(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_RESPONSE);
            String sessionIndex = (String)samlAttributes.get(AppMConstants.SAML2_SESSION_INDEX);

            SAMLTokenInfoDTO samlTokenInfoDTO = new SAMLTokenInfoDTO();
            samlTokenInfoDTO.setEncodedSamlToken(samlResponse);
            samlTokenInfoDTO.setNotOnOrAfter((DateTime) samlAttributes.get(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_ASSERTION_NOT_ON_OR_AFTER));
            samlTokenInfoDTO.setSessionIndex(sessionIndex);

            if (samlCookieValue == null) {
                samlCookieValue = UUID.randomUUID().toString();
                if (log.isDebugEnabled()) {
                    log.debug("generating samlCookieValue : " + samlCookieValue);
                }
                messageContext.setProperty(AppMConstants.APPM_SAML2_COOKIE, samlCookieValue);

                //Cache SAML response. Different apps may configured to have different claim values which result in different
                //SAML responses for each app. Map is required to store SAML responses of apps being invoked. Then set the cache
                //key as value of 'appmSamlSsoTokenId' cookie and this Map has the value and put it to cache.
                Map<String, SAMLTokenInfoDTO> samlResponsesMap = new HashMap<String, SAMLTokenInfoDTO>();
                samlResponsesMap.put(webAppInfoDTO.getSaml2SsoIssuer(), samlTokenInfoDTO);
                getSAML2ConfigCache().put(samlCookieValue, samlResponsesMap);
                if (sessionIndex != null)
                    getSAML2SessionIndexCache().put(sessionIndex, samlCookieValue);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("samlCookie already exists : " + samlCookieValue);
                }
                //This logic get executed when accessing an app that haven't accessed previously in the same browser
                Map<String, SAMLTokenInfoDTO> samlResponsesMap = (HashMap<String, SAMLTokenInfoDTO>) getSAML2ConfigCache().get(samlCookieValue);
                String samlIssuer = webAppInfoDTO.getSaml2SsoIssuer();
                
                if (samlResponsesMap != null && !samlResponsesMap.containsKey(samlIssuer)) {
                    samlResponsesMap.put(samlIssuer, samlTokenInfoDTO);
                    getSAML2ConfigCache().put(samlCookieValue, samlResponsesMap);
                    if (sessionIndex != null)
                        getSAML2SessionIndexCache().put(sessionIndex, samlCookieValue);
                } else { //when accessing though my-subscriptions page
                    samlResponsesMap = new HashMap<String, SAMLTokenInfoDTO>();
                    samlResponsesMap.put(samlIssuer, samlTokenInfoDTO);
                    getSAML2ConfigCache().put(samlCookieValue, samlResponsesMap);
                    if (sessionIndex != null)
                        getSAML2SessionIndexCache().put(sessionIndex, samlCookieValue);
                }
                messageContext.setProperty(AppMConstants.APPM_SAML2_COOKIE, samlCookieValue);
            }

            //APISecurityConstants.SUBJECT maps to authenticated userName
            messageContext.setProperty(APISecurityConstants.SUBJECT, samlAttributes.get(APISecurityConstants.SUBJECT));

            // Get the user roles and cache them.
            String roles = getUserRolesFromTheSAMLResponse(samlAttributes);
            getSAML2ConfigCache().put(AppMConstants.USER_ROLES_CACHE_KEY, roles);
            
            // Get the authenticated IDP if there is one.
            AuthenticatedIDP[] authenticatedIDP = getAuthenticatedIDP(idpResponseAttributes, samlAttributes);

            if(authenticatedIDP != null){

                cacheAuthenticatedIDP(samlCookieValue, authenticatedIDP);

                // Set the authenticated IDP in message context, if there is one.
                messageContext.setProperty(APISecurityConstants.AUTHENTICATED_IDP, authenticatedIDP);
            }

            try {
                if(isLogoutRequest(messageContext)){
                    handleLogoutRequest(messageContext);
                    return true;
                } else if (isSubscribed(messageContext)) {  //check for subscription
                    if (isJWTEnabled) {
                        generateJWTAndAddToTransportHeaders(messageContext, samlAttributes, webApp);
                    }
                    if (shouldAddSAMLResponseAsTransportHeader()) {
                        headers.put("AppMgtSAML2Response", samlResponse);
                        messageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
                    }
                    return true;
                } else {
                    handleAuthFailure(messageContext,
                            new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN, "You have not subscribe to this Application"));
                }
            } catch (AppManagementException e) {
                e.printStackTrace();
            } catch (APISecurityException e) {
                log.error("WebApp authentication failure", e);
                handleAuthFailure(messageContext, e);
                return false;
            }
        }

        return false;

    }

    /**
     * Caches the given authenticated IDP.
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
     * @param key
     * @return Cached authenticated IDP if there is one, null otherwise.
     */
    private AuthenticatedIDP[] getCachedAuthenticatedIDP(String key){

        Cache cache = Caching.getCacheManager(AppMConstants.AUTHENTICATED_IDP_CACHE_MANAGER)
                .getCache(AppMConstants.AUTHENTICATED_IDP_CACHE);

        Object cachedObject = cache.get(key);

        if(cachedObject != null && cachedObject instanceof AuthenticatedIDP[]){
            return (AuthenticatedIDP[]) cachedObject;
        }else{
            return null;
        }
    }

    /**
     * Returns transport headers of the message context.
     * @param messageContext
     * @return Transport headers.
     */
    private Map<String, String> getTransportHeaders(MessageContext messageContext){
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        return (Map<String, String>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    /**
     * Returns the attributes + subject on the given IDP response attributes.
     * @param idpResponseAttributes A Map which contains the elements which IDP sends. It contains the SAML response and authenticated IDP.
     * @return
     */
    private Map<String, Object> getAttributesOfSAMLResponse(Map<String, String> idpResponseAttributes) {

        String samlResponse = idpResponseAttributes.get(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_RESPONSE);
        String decodedSAMLResponse = new String(Base64.decode(samlResponse));

        XMLObject responseXmlObj = null;
        try {
            responseXmlObj = SAMLSSOUtil.unmarshall(decodedSAMLResponse);

        }   catch (IdentityException e) {
            e.printStackTrace();
        }
        return getResult(responseXmlObj);
    }

    /**
     * Handles the logout request for an app.
     * @param messageContext
     * @throws SynapseException
     */
    private void handleLogoutRequest(MessageContext messageContext) throws SynapseException{

        String appmSaml2CookieValue = (String) messageContext.getProperty(AppMConstants.APPM_SAML2_COOKIE);

        messageContext.setProperty("isLogoutRequest", true);
        String encodedSAMLResponse = getCachedSAMLResponse(appmSaml2CookieValue);

        if(encodedSAMLResponse!=null){
            try {
//                String decodedSAMLResponse = URLDecoder.decode(encodedSAMLResponse , "UTF-8");
                String decodedSAMLResponse = new String(Base64.decode(encodedSAMLResponse));
                String samlAssertion = getSamlAssetionString(decodedSAMLResponse);
                XMLObject responseXmlObj = SAMLSSOUtil.unmarshall(samlAssertion);

                Assertion assertion = (Assertion) responseXmlObj;

                if (assertion != null) {
                    String subject = assertion.getSubject().getNameID().getValue();

                    AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);
                    String sessionIndex = authnStatement.getSessionIndex();

                    LogoutRequest logoutReq = buildLogoutRequest(subject,sessionIndex, webAppInfoDTO.getSaml2SsoIssuer());

                    String encodedSamlLogOutRequest =  encodeRequestMessage(logoutReq);

                    if(encodedSamlLogOutRequest!=null){
                        getSAML2ConfigCache().remove(appmSaml2CookieValue);
                        getSAML2SessionIndexCache().remove(sessionIndex);
                        sendSAMLRequestToIdP(messageContext, encodedSamlLogOutRequest);
                    } else{
                        throw new SynapseException("Error while sending logout request to IDP.");
                    }
                }

            } catch (IdentityException e) {
                e.printStackTrace();
            }
        }
    }

    private String encodeRequestMessage(RequestAbstractType requestMessage){

        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            log.error("Error while initializing opensaml library", e);
            return null;
        }
        Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(requestMessage);
        Element authDOM = null;
        try {
            authDOM = marshaller.marshall(requestMessage);

            /* Compress the message */
            Deflater deflater = new Deflater(Deflater.DEFLATED, true);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
            StringWriter rspWrt = new StringWriter();
            XMLHelper.writeNode(authDOM, rspWrt);
            deflaterOutputStream.write(rspWrt.toString().getBytes());
            deflaterOutputStream.close();

            /* Encoding the compressed message */
            String encodedRequestMessage = Base64.encodeBytes(byteArrayOutputStream.toByteArray(), Base64.DONT_BREAK_LINES);
            return URLEncoder.encode(encodedRequestMessage, "UTF-8").trim();

        } catch (MarshallingException e) {
            log.error("Error occurred while encoding SAML request", e);
        } catch (UnsupportedEncodingException e) {
            log.error("Error occurred while encoding SAML request", e);
        } catch (IOException e) {
            log.error("Error occurred while encoding SAML request", e);
        }
        return null;
    }


    /**
     * Get the authenticated IDP for this request. User identity is retrieved from samlAttributes and the IDP name is retrieved from idpResponseAttributes.
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
                authenticatedIDPJson = new String(Base64.decode(authenticatedIDPJson));

                JSONObject parsedJson = (JSONObject) JSONValue.parse(authenticatedIDPJson);
                JSONArray idps = (JSONArray) parsedJson.get("idps");
                AuthenticatedIDP[] authenticatedIDPs= new AuthenticatedIDP[idps.size()];

                for(int i=0; i<idps.size();i++){
                    JSONObject authenticatedIDPJSON = (JSONObject) idps.get(i);
                    idpName =  authenticatedIDPJSON.get("idp").toString();

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
            String sessionIndex = assertion.getAuthnStatements().get(0).getSessionIndex();
            results.put(AppMConstants.SAML2_SESSION_INDEX,sessionIndex);
        }
        return results;
    }

    private Cache getSAML2ConfigCache() {
            return Caching.getCacheManager(AppMConstants.SAML2_CONFIG_CACHE_MANAGER)
                    .getCache(AppMConstants.SAML2_CONFIG_CACHE);
    }

    private Cache getSAML2SessionIndexCache() {
        return Caching.getCacheManager(AppMConstants.SAML2_SESSION_INDEX_CACHE_MANAGER)
                .getCache(AppMConstants.SAML2_SESSION_INDEX_CACHE);
    }

    private Cache getAppContextVersionConfigCache() {
        return Caching.getCacheManager(AppMConstants.APP_CONTEXT_VERSION_CACHE_MANAGER)
                .getCache(AppMConstants.APP_CONTEXT_VERSION_CONFIG_CACHE);
    }

    private Cache getUserRolesCacheConfig() {
        return Caching.getCacheManager(AppMConstants.USER_ROLES_CACHE_MANAGER)
                .getCache(AppMConstants.USER_ROLES_CONFIG_CACHE);
    }

    private Cache getSAML2RelayStateCache() {
        return Caching.getCacheManager(AppMConstants.SAML2_RELAY_STATE_CACHE_MANAGER)
                .getCache(AppMConstants.SAML2_RELAY_STATE_CACHE);
    }

    private void redirectToIDPLogin(MessageContext messageContext) {
        RequestAbstractType authnRequest = buildAuthnRequestObject(messageContext);
        String encodedSamlRequest = encodeRequestMessage(authnRequest);
        sendSAMLRequestToIdP(messageContext,encodedSamlRequest);
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
        String authReqRandomId = Integer.toHexString(new Double(Math.random()).intValue());

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

    private void sendSAMLRequestToIdP(MessageContext messageContext, String request) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext). getAxis2MessageContext();
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


        headerMap.put("Location", getIDPUrl() + "?SAMLRequest=" + request);

        if (messageContext.getProperty("error_message_type") != null &&
            messageContext.getProperty("error_message_type").toString().equalsIgnoreCase("application/json")) {
            axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/json");
        }

        headerMap.remove(HttpHeaders.HOST);
        Axis2Sender.sendBack(messageContext);
    }

    private LogoutRequest buildLogoutRequest(String user, String sessionIdx, String saml2Issuer){

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
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/soap+xml");

        int status;
        if (e.getErrorCode() == APISecurityConstants.API_AUTH_GENERAL_ERROR) {
            status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        } else if (e.getErrorCode() == APISecurityConstants.API_AUTH_INCORRECT_API_RESOURCE || e.getErrorCode() == APISecurityConstants.API_AUTH_FORBIDDEN) {
            status = HttpStatus.SC_FORBIDDEN;
        } else {
            status = HttpStatus.SC_UNAUTHORIZED;
            Map<String, String> headers = new HashMap<String, String>();
            headers.put(HttpHeaders.WWW_AUTHENTICATE, authenticator.getChallengeString());
            axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        }

        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload(e));
        } else {
            Utils.setSOAPFault(messageContext, "Client", "Authentication Failure", e.getMessage());
        }
        if (Utils.isCORSEnabled()) {
        	/* For CORS support adding required headers to the fault response */
            Map<String, String> headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            headers.put(AppMConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, Utils.getAllowedOrigin(authenticator.getRequestOrigin()));
            headers.put(AppMConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS, Utils.getAllowedMethods());
            headers.put(AppMConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS, Utils.getAllowedHeaders());
            axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        }

        Utils.sendFault(messageContext, status);
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
     * @return
     */
    private String getIDPUrl() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
                getFirstProperty(AppMConstants.SSO_CONFIGURATION_IDENTITY_PROVIDER_URL);
    }

    /**
     * Returns the status of enabledsaml sso configuraion.
     * @return
     */
    private boolean getSamlSSOConfiguration() {
        return Boolean.parseBoolean(ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
                getFirstProperty(AppMConstants.SSO_CONFIGURATION_ENABLE_SSO_CONFIGURATION));
    }

    /**
     * Checks whether the SAML response should be added as a transport header.
     * @return
     */
    private boolean shouldAddSAMLResponseAsTransportHeader() {
        return Boolean.parseBoolean(ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
                getFirstProperty(AppMConstants.API_CONSUMER_AUTHENTICATION_ADD_SAML_RESPONSE_HEADER_TO_OUT_MSG));
    }

    /**
     * Returns decoded  IDP response attributes.
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
            log.error("Error while retrieving IDP response attributes.",e);
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

            if(idpResponseAttributes.size() == 2){
                break;
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
        String version = (String)messageContext.getProperty("SYNAPSE_REST_API_VERSION");
        
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
                headers.get(HTTP_HEADER_HOST) + messageContext.getProperty(PROPERTY_REST_API_CONTEXT) + "/";

        return assertionConsumerUrl;
    }

    /**
     * This method is used to set the "appmSamlSsoCookie" in the request going to backend app.
     * Note: This method cannot reuse in the response path, since we updating the "Cookie" header.
     * "Set-Cookie" header should use in response path to add new cookies to browser.
     * @param messageContext
     */
    private void setAppmSamlSsoCookie(MessageContext messageContext) {
        String appmSamlSsoCookie = (String) messageContext.getProperty(AppMConstants.APPM_SAML2_COOKIE);
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        Map<String, Object> headers = (Map<String, Object>) axis2MC.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String cookieString = (String) headers.get(HTTPConstants.HEADER_COOKIE);

        if (log.isDebugEnabled()) {
            log.debug("Exisiting cookie string in transport headers : " + cookieString);
        }
        if (cookieString == null) {
            cookieString = AppMConstants.APPM_SAML2_COOKIE + "=" + appmSamlSsoCookie + "; " + "path=" + "/";
        } else {
            cookieString = cookieString + " ;" + AppMConstants.APPM_SAML2_COOKIE + "=" + appmSamlSsoCookie + ";" + " Path=" + "/";
        }
        if (log.isDebugEnabled()) {
            log.debug("Updated cookie string in transport headers : " + cookieString);
        }

        headers.put(HTTPConstants.HEADER_COOKIE, cookieString);
        messageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
    }

    /**
     * Returns the registered Token Generator. Returns the default one if not registered with ServiceReferenceHolder
     * @return an instance of TokenGenerator
     */
    private TokenGenerator getTokenGenerator() {
        TokenGenerator tokenGeneratorFromService = ServiceReferenceHolder.getInstance().getTokenGenerator();
        if(tokenGeneratorFromService != null) {
            return tokenGeneratorFromService;
        }
        return defaultTokenGenerator;
    }
}
