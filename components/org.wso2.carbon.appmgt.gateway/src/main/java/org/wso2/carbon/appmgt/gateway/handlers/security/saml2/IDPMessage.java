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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.*;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.NodeList;
import org.wso2.carbon.appmgt.api.model.AuthenticatedIDP;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.idp.sso.SSOConfiguratorUtil;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.sso.saml.exception.IdentitySAML2SSOException;

import java.util.List;

/**
 * Represents the call back request from the IDP.
 */
public class IDPMessage {

    private static final Log log = LogFactory.getLog(IDPMessage.class);

    private RequestAbstractType samlRequest;
    private StatusResponseType samlResponse;
    private List<AuthenticatedIDP> authenticatedIDPs;
    private String relayState;
    private String rawSAMLResponse;
    private String rawSAMLRequest;
    private int timeStampSkewInSeconds = 300;

    public RequestAbstractType getSAMLRequest() {
        return samlRequest;
    }

    public void setSAMLRequest(RequestAbstractType samlRequest) {
        this.samlRequest = samlRequest;
    }

    public StatusResponseType getSAMLResponse() {
        return samlResponse;
    }

    public void setSAMLResponse(StatusResponseType samlResponse) {
        this.samlResponse = samlResponse;
    }

    public List<AuthenticatedIDP> getAuthenticatedIDPs() {
        return authenticatedIDPs;
    }

    public void setAuthenticatedIDPs(List<AuthenticatedIDP> authenticatedIDPs) {
        this.authenticatedIDPs = authenticatedIDPs;
    }

    public void setRelayState(String relayState) {
        this.relayState = relayState;
    }

    public String getRelayState() {
        return relayState;
    }

    public void setRawSAMLResponse(String rawSAMLResponse) {
        this.rawSAMLResponse = rawSAMLResponse;
    }

    public String getRawSAMLResponse() {
        return rawSAMLResponse;
    }

    public String getRawSAMLRequest() {
        return rawSAMLRequest;
    }

    public void setRawSAMLRequest(String rawSAMLRequest) {
        this.rawSAMLRequest = rawSAMLRequest;
    }

    public boolean isSLOResponse() {
        return samlResponse != null && samlResponse instanceof LogoutResponse;
    }

    public boolean isSLORequest() {
        return samlRequest != null && samlRequest instanceof LogoutRequest;
    }

    /**
     * Validate SAML response signature, assertion signature and audiences.
     * @param samlResponse
     * @param webapp
     * @param configuration
     * @return
     */
    public boolean validateSignatureAndAudienceRestriction(StatusResponseType samlResponse, WebApp webapp,
                                                           AppManagerConfiguration configuration) {

        Assertion assertion = null;
        if(samlResponse != null){
            Response response = (Response) samlResponse;
            List<Assertion> assertions = response.getAssertions();

            if (CollectionUtils.isEmpty(assertions)) {
                if (log.isDebugEnabled()) {
                    log.debug("SAML Response does not have assertions.");
                }
                return false;
            }

            assertion = assertions.get(0);
        }

        // validate audience restriction
        if (!validateAudienceRestriction(assertion, webapp)) {
            return false;
        }

        boolean responseSigningEnabled = SSOConfiguratorUtil.isResponseSigningEnabled();
        boolean assertionSigningEnabled = SSOConfiguratorUtil.isAssertionSigningEnabled();

        Credential certificate = null;

        if (responseSigningEnabled || assertionSigningEnabled) {

            // validate signature
            String responseSigningKeyAlias = configuration.getFirstProperty(AppMConstants.SSO_CONFIGURATION_RESPONSE_SIGNING_KEY_ALIAS);

            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            if(!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
                responseSigningKeyAlias = tenantDomain;
            }

            // User the certificate of the super tenant since the responses are signed by the super tenant.
            try {
                certificate = GatewayUtils.getIDPCertificate(tenantDomain, responseSigningKeyAlias);
            } catch (IdentitySAML2SSOException e) {
                String errorMessage = "Error while getting IdP Certificate";
                GatewayUtils.logAndThrowException(log, errorMessage, e);
            }
        }

        //validate SAML Response signature
        if (responseSigningEnabled && !validateResponseSignature(certificate)) {
            return false;
        }

        //validate SAML Assertion signature
        if (assertionSigningEnabled && !validateAssertionSignature(certificate)) {
            return false;
        }

        return true;
    }

    /**
     * Validate SAML Response signature.
     * @param credential
     * @return
     */
    private boolean validateResponseSignature(Credential credential) {

        // Get the SAML response signature
        Signature responseSignature = null;
        if(isResponse()){
            responseSignature = getSAMLResponse().getSignature();
        }else if(isRequest()){
            responseSignature = getSAMLRequest().getSignature();
        }

        return validateSignature(credential, responseSignature);
    }

    /**
     * Validate SAML Assertion signature.
     * @param credential
     * @return
     */
    private boolean validateAssertionSignature(Credential credential) {

        // Get the SAML response signature and assertion signature
        Signature assertionSignature = null;
        if(isResponse()){
            assertionSignature = ((Response)getSAMLResponse()).getAssertions().get(0).getSignature();
        }

        return validateSignature(credential, assertionSignature);
    }

    /**
     * Signature validation helper method.
     * @param credential
     * @param signature
     * @return
     */
    private boolean validateSignature(Credential credential, Signature signature) {

        SignatureValidator signatureValidator = new SignatureValidator(credential);
        SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();

        //Validate signature : Could be either SAML response signature or SAML Assertion signature
        if(signature != null){
            try {
                signatureProfileValidator.validate(signature);
            } catch (ValidationException e) {
                log.error("SAML Response signature or Aseertion signature do not confirm to SAML signature profile. " +
                        "Possible XML Signature Wrapping Attack");
                if (log.isDebugEnabled()) {
                    log.debug("SAML signature do not confirm to SAML signature profile.", e);
                }
                return false;
            }

            try {
                signatureValidator.validate(signature);
            } catch (ValidationException e) {
                log.error("Response signature or Assertion signature of the SAML message can't be validated.");
                if (log.isDebugEnabled()) {
                    log.debug("Response signature or Assertion signature of the SAML message can't be validated.", e);
                }
                return false;
            }
        }else{
            if(log.isDebugEnabled()){
                log.debug("SAML message has not been singed.");
            }
            return false;
        }

        return true;
    }

    /**
     * Validates the 'Not Before' and 'Not On Or After' conditions of the SAML Assertion.
     *
     */
    public boolean validateAssertionValidityPeriod() {

        boolean validateAssertionExpiry = SSOConfiguratorUtil.isValidateAssertionValidityPeriod();

        if (validateAssertionExpiry) {
            Assertion assertion = null;
            Response response = (Response) samlResponse;
            List<Assertion> assertions = response.getAssertions();

            if (CollectionUtils.isEmpty(assertions)) {
                if (log.isDebugEnabled()) {
                    log.debug("SAML Response does not have assertions.");
                }
                return false;
            }

            assertion = assertions.get(0);

            DateTime validFrom = assertion.getConditions().getNotBefore();
            DateTime validTill = assertion.getConditions().getNotOnOrAfter();

            //skew time in seconds
            String timeStampSkewInConfig = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                    getAPIManagerConfiguration().getFirstProperty(AppMConstants.SSO_CONFIGURATION_RESPONSE_VALIDITY_TIME_STAMP_SKEW);

            if (timeStampSkewInConfig != null) {
                timeStampSkewInSeconds = Integer.parseInt(timeStampSkewInConfig);
            }

            if (validFrom != null && validFrom.minusSeconds(timeStampSkewInSeconds).isAfterNow()) {
                log.error("Failed to meet SAML Assertion Condition 'Not Before'");
                return false;
            }

            if (validTill != null && validTill.plusSeconds(timeStampSkewInSeconds).isBeforeNow()) {
                log.error("Failed to meet SAML Assertion Condition 'Not On Or After'");
                return false;
            }

            if (validFrom != null && validTill != null && validFrom.isAfter(validTill)) {
                log.error("SAML Assertion Condition 'Not Before' must be less than the value of 'Not On Or After'");
                return false;
            }
        }

        return true;
    }

    /**
     * Validate the AudienceRestriction of SAML2 Response.
     *
     * @param assertion SAML2 Assertion
     * @param webApp Web App
     * @return validity
     */
    private boolean validateAudienceRestriction(Assertion assertion, WebApp webApp) {

        if (assertion != null) {
            Conditions conditions = assertion.getConditions();
            if (conditions != null) {
                List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
                if (audienceRestrictions != null && !audienceRestrictions.isEmpty()) {
                    for (AudienceRestriction audienceRestriction : audienceRestrictions) {
                        if (CollectionUtils.isNotEmpty(audienceRestriction.getAudiences())) {
                            boolean audienceFound = false;
                            for (Audience audience : audienceRestriction.getAudiences()) {
                                if (webApp.getSaml2SsoIssuer().equals(audience.getAudienceURI())) {
                                    audienceFound = true;
                                    break;
                                }
                            }
                            if (!audienceFound) {
                                log.error("SAML Assertion Audience Restriction validation failed");
                                return false;
                            }
                        } else {
                            log.error("SAML Response's AudienceRestriction doesn't contain Audiences");
                            return false;
                        }
                    }
                } else {
                    log.error("SAML Response doesn't contain AudienceRestrictions");
                    return false;
                }
            } else {
                log.error("SAML Response doesn't contain Conditions");
                return false;
            }
        }

        return true;
    }

    private boolean isRequest() {
        return samlRequest != null;
    }

    private boolean isResponse() {
        return samlResponse != null;
    }
}
