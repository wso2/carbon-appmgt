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

package org.wso2.carbon.appmgt.gateway.handlers.security.saml2;


import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.*;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.util.Base64;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.gateway.handlers.security.authentication.AuthenticationContext;
import org.wso2.carbon.appmgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * The utility class which provides SAML related operations.
 */
public class SAMLUtils {

    private static final Log log = LogFactory.getLog(SAMLUtils.class);

    private static final String IDP_CALLBACK_ATTRIBUTE_NAME_SAML_RESPONSE = "SAMLResponse";
    private static final String IDP_CALLBACK_ATTRIBUTE_NAME_AUTHENTICATED_IDPS = "AuthenticatedIdPs";
    private static final String IDP_CALLBACK_ATTRIBUTE_NAME_RELAY_STATE = "RelayState";

    /**
     * Builds and returns a SAML authentication request to the IDP.
     *
     * @param messageContext
     * @param webApp
     * @return
     */
    public static AuthnRequest buildAuthenticationRequest(MessageContext messageContext, WebApp webApp) {

        /* Building Issuer object */
        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuerOb = issuerBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:assertion", "Issuer", "samlp");
        issuerOb.setValue(webApp.getSaml2SsoIssuer());

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
        AuthnRequest authRequest = authRequestBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:protocol", "AuthnRequest", "samlp");
        authRequest.setForceAuthn(false);
        authRequest.setIsPassive(false);
        authRequest.setIssueInstant(issueInstant);
        authRequest.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        authRequest.setAssertionConsumerServiceURL(getAssertionConsumerUrl(messageContext));
        authRequest.setIssuer(issuerOb);
        authRequest.setNameIDPolicy(nameIdPolicy);
        authRequest.setRequestedAuthnContext(requestedAuthnContext);
        authRequest.setID(authReqRandomId);
        authRequest.setDestination(GatewayUtils.getIDPUrl());
        authRequest.setVersion(SAMLVersion.VERSION_20);

        return authRequest;
    }

    /**
     * Returns the marshalled and encoded SAML request.
     *
     * @param request
     * @return
     * @throws SAMLException
     */
    public static String marshallAndEncodeSAMLRequest(RequestAbstractType request) throws SAMLException {

        try {
            String marshalledRequest = SAMLSSOUtil.marshall(request);

            Deflater deflater = new Deflater(Deflater.DEFLATED, true);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
            deflaterOutputStream.write(marshalledRequest.getBytes("UTF-8"));
            deflaterOutputStream.close();

            String encodedRequestMessage = Base64.encodeBytes(byteArrayOutputStream.toByteArray(), Base64.DONT_BREAK_LINES);
            return URLEncoder.encode(encodedRequestMessage, "UTF-8").trim();

        } catch (IdentityException e) {
            throw new SAMLException("Can't marshall and encode SAML response", e);
        } catch (IOException e) {
            throw new SAMLException("Can't marshall and encode SAML response", e);
        }
    }

    /**
     *
     * Processes the payload of the request and parse the SAML response if available.
     *
     * @param messageContext
     * @return
     * @throws SAMLException
     */
    public static IDPCallback processIDPCallback(MessageContext messageContext) throws SAMLException {

        IDPCallback idpCallback = new IDPCallback();

        Iterator iterator = messageContext.getEnvelope().getBody().getChildElements();

        while (iterator.hasNext()) {

            OMElement formData = (OMElement) iterator.next();

            OMElement samlResponse = formData.getFirstChildWithName (new QName(IDP_CALLBACK_ATTRIBUTE_NAME_SAML_RESPONSE));

            if(samlResponse != null){
                    XMLObject unmarshalledResponse = decodeAndUnmarshallSAMLResponse(samlResponse.getText());
                    idpCallback.setSAMLResponse((org.opensaml.saml2.core.impl.ResponseImpl) unmarshalledResponse);
                    idpCallback.setRawSAMLResponse(samlResponse.getText());
            }

            OMElement authenticatedIdPs = formData.getFirstChildWithName (new QName(IDP_CALLBACK_ATTRIBUTE_NAME_AUTHENTICATED_IDPS));
            if(authenticatedIdPs != null){
                // Need to decode authenticated IDPs
            }

            OMElement relayState = formData.getFirstChildWithName (new QName(IDP_CALLBACK_ATTRIBUTE_NAME_RELAY_STATE));
            if(relayState != null){
                idpCallback.setRelayState(relayState.getText());
            }

            break;
        }

        return idpCallback;
    }

    /**
     * Returns the decoded and unmarshalled SAML response.
     *
     * @param encodedSAMLResponse
     * @return
     * @throws SAMLException
     */
    public static XMLObject decodeAndUnmarshallSAMLResponse(String encodedSAMLResponse) throws SAMLException {

        try {
            return SAMLSSOUtil.unmarshall(new String(Base64.decode(encodedSAMLResponse), "UTF-8") );
        } catch (IdentityException e) {
            throw new SAMLException("Can't decode and unmarshall SAML response", e);
        } catch (UnsupportedEncodingException e) {
            throw new SAMLException("Can't decode and unmarshall SAML response", e);
        }
    }

    /**
     *
     * Build and returns the authentication context using the given IDP callback.
     *
     * @param idpCallback
     * @return
     */
    public static AuthenticationContext getAuthenticationContext(IDPCallback idpCallback) {

        ResponseImpl response = idpCallback.getSAMLResponse();
        Assertion assertion = response.getAssertions().get(0);

        AuthenticationContext authenticationContext =  new AuthenticationContext();

        // If the 'Subject' is not there the SAML response, it's not an authenticated one.
        if(assertion == null || assertion.getSubject() == null){
            authenticationContext.setAuthenticated(false);
            return authenticationContext;
        }else{
            authenticationContext.setSubject(assertion.getSubject().getNameID().getValue());
       }

       return authenticationContext;
    }

    private static String getAssertionConsumerUrl(MessageContext messageContext){

        String appRootURL = null;
        try {
            appRootURL = GatewayUtils.getAppRootURL(messageContext);
        } catch (MalformedURLException e) {
            log.error("Error while getting app root URL");
            return null;
        }

        //Construct the assertion consumer url by appending gateway endpoint as the host
        String assertionConsumerUrl = appRootURL + AppMConstants.GATEWAY_ACS_RELATIVE_URL;

        return assertionConsumerUrl;
    }

}
