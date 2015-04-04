/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.impl.entitlement;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.wso2.carbon.appmgt.api.EntitlementService;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementDecisionRequest;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicy;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicyValidationResult;
import org.wso2.carbon.appmgt.api.model.entitlement.XACMLPolicyTemplateContext;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.proxy.Attribute;
import org.wso2.carbon.identity.entitlement.proxy.PEPProxy;
import org.wso2.carbon.identity.entitlement.proxy.PEPProxyConfig;
import org.wso2.carbon.identity.entitlement.proxy.ProxyConstants;
import org.wso2.carbon.identity.entitlement.proxy.exception.EntitlementProxyException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceStub;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XACML based implementation of entitlement service.
 */
public class XacmlEntitlementServiceImpl implements EntitlementService {

    private static final String XACML_ATTRIBUTE_ID_SUBJECT_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";

	private static final String XACML_ATTRIBUTE_CATEGORY_SUBJECT = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";

	private static final Log log = LogFactory.getLog(XacmlEntitlementServiceImpl.class);

    private static final String DECISION_DENY = "DENY";
    private static final String XML_ELEMENT_RESULT = "Result";
    private static final String XML_ELEMENT_DECISION = "Decision";
    private static final String XML_NS_XACML_RESULT = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";
    
    private static final String XACML_ATTRIBUTE_CATEGORY_CUSTOM = "urn:wso2:appm:xacml:attribute-category:custom";
    private static final String XACML_ATTRIBUTE_ID_POLICY_ID = "urn:wso2:appm:xacml:custom:policy-id";

    private String serverUrl;
    private String username;
    private String password;

    private EntitlementPolicyAdminServiceStub entitlementPolicyAdminServiceStub;

    public XacmlEntitlementServiceImpl(String serverUrl, String username, String password) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
    }

    public void init(){
        try {
            String cookie = login(serverUrl, username, password);

            entitlementPolicyAdminServiceStub = new EntitlementPolicyAdminServiceStub(serverUrl +
                    "/services/EntitlementPolicyAdminService");

            ServiceClient client = entitlementPolicyAdminServiceStub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);
            options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault axisFault) {
            String errorMessage = "Cannot initialize XACML entitlement service.";
            log.error(errorMessage, axisFault);
            throw new SynapseException(errorMessage, axisFault);
        }
    }

    @Override
    public void savePolicy(EntitlementPolicy policy) {

        try {

            if (!policy.isValid()) {
                return;
            }

            // If there is an existing policy, update it.
            PolicyDTO existingPolicy = getExistingPolicy(policy.getPolicyId());

            if (existingPolicy != null) {
                doUpdatePolicy(existingPolicy, policy);
            } else {
                doSavePolicy(policy);
            }
        } catch (RemoteException e) {
            log.error("Error occurred while saving or publishing XACML policy with id : " + policy.getPolicyId(), e);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error("Cannot save or publish XACML policy with id : " + policy.getPolicyId() +
                    ". Error occurred in EntitlementPolicyAdminService", e);
        }

    }

    @Override
    public void updatePolicy(EntitlementPolicy policy) {

        EntitlementPolicy processedPolicy = preProcess(policy);

        if (processedPolicy == null) {
            return;
        }
        try {
            PolicyDTO existingPolicy = getExistingPolicy(policy.getPolicyId());
            doUpdatePolicy(existingPolicy, processedPolicy);
        } catch (RemoteException e) {
            log.error("Error occurred while updating XACML policy with id : " + policy.getPolicyId(), e);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error("Cannot update XACML policy with id : " + policy.getPolicyId() +
                    ". Error occurred in EntitlementPolicyAdminService", e);
        }

    }

    @Override
    public EntitlementPolicyValidationResult validatePolicyPartial(String partial) {

        // Generate mock policy from the given partial.
        org.wso2.carbon.identity.entitlement.dto.PolicyDTO policyDTO = new org.wso2.carbon.identity.entitlement.dto.PolicyDTO();
        policyDTO.setPolicy(generateMockPolicy(partial));

        EntitlementPolicyValidationResult entitlementPolicyValidationResult = new EntitlementPolicyValidationResult();
        boolean result = EntitlementUtil.validatePolicy(policyDTO);
        entitlementPolicyValidationResult.setValid(result);

        return entitlementPolicyValidationResult;
    }

    @Override
    public EntitlementPolicyValidationResult validatePolicy(EntitlementPolicy policy) {
        org.wso2.carbon.identity.entitlement.dto.PolicyDTO policyDTO = new org.wso2.carbon.identity.entitlement.dto.PolicyDTO();
        policyDTO.setPolicy(policy.getPolicyContent());

        EntitlementPolicyValidationResult entitlementPolicyValidationResult = new EntitlementPolicyValidationResult();
        //Validate policy
        boolean result = EntitlementUtil.validatePolicy(policyDTO);
        entitlementPolicyValidationResult.setValid(result);

        return entitlementPolicyValidationResult;
    }

    @Override
    public void generateAndSaveEntitlementPolicies(List<XACMLPolicyTemplateContext> xacmlPolicyTemplateContexts) {
        XACMLTemplateBuilder xacmlTemplateBuilder = new XACMLTemplateBuilder();

        for(XACMLPolicyTemplateContext context : xacmlPolicyTemplateContexts){
            generateAndSaveEntitlementPolicy(context, xacmlTemplateBuilder);
        }
    }

    @Override
    public boolean isPermitted(EntitlementDecisionRequest request) {

        PEPProxy pepProxy = getPepProxy();

        if(pepProxy == null){
            throw new SynapseException("Cannot create PEP proxy.");
        }

        String decisionResult = null;
        try {

            decisionResult = pepProxy.getDecision(getEntitlementAttributes(request));

            OMElement decisionElement = AXIOMUtil.stringToOM(decisionResult);
            String decision = decisionElement.getFirstChildWithName(new QName(XML_NS_XACML_RESULT, XML_ELEMENT_RESULT)).
                    getFirstChildWithName(new QName(XML_NS_XACML_RESULT, XML_ELEMENT_DECISION)).getText();

            if(decision != null && DECISION_DENY.equals(decision.toUpperCase())){
                return false;
            }else{
                return true;
            }

        } catch (Exception e) {
            String errorMessage = String.format("Error while evaluating entitlement for the policy id '%s'.", request.getPolicyId());
            log.error(errorMessage, e);
            throw new SynapseException(errorMessage, e);
        }

    }

    @Override
    public String getPolicyContent(String policyId) {
        try {
            PolicyDTO existingPolicy = getPolicy(policyId);
            return existingPolicy.getPolicy();

        } catch (RemoteException e) {
            log.error("Error occurred while retrieving policy with policy id : " + policyId, e);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error("Error occurred while retrieving policy with id : " + policyId +
                    " from EntitlementPolicyAdmin Service ", e);
        }
        return null;
    }

    @Override
    public void removePolicy(String policyId){
        try {
            if(getExistingPolicy(policyId) != null) {
                deletePolicy(policyId);
            }
        } catch (RemoteException e) {
            log.error("Error occurred while deleting policy with policy id : " + policyId, e);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error("Error occurred while removing policy from EntitlementPolicyAdmin Service ", e);
        }
    }

    private void generateAndSaveEntitlementPolicy(XACMLPolicyTemplateContext xacmlPolicyTemplateContext,
            XACMLTemplateBuilder templateBuilder){

		// Generate the policy
		EntitlementPolicy entitlementPolicy = templateBuilder.generatePolicy(xacmlPolicyTemplateContext);
		savePolicy(entitlementPolicy);
	}
    
    private PolicyDTO getExistingPolicy(String policyId) throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {
        return entitlementPolicyAdminServiceStub.getLightPolicy(policyId);
    }

    private PolicyDTO getPolicy(String policyId) throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {
        return entitlementPolicyAdminServiceStub.getPolicy(policyId, false);
    }

    private void doUpdatePolicy(PolicyDTO existingPolicy, EntitlementPolicy newPolicy) throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {
        existingPolicy.setPolicy(newPolicy.getPolicyContent());
        entitlementPolicyAdminServiceStub.updatePolicy(existingPolicy);

        // Publish the policy to the PDP.
        entitlementPolicyAdminServiceStub.publishToPDP(new String[]{existingPolicy.getPolicyId()}, "UPDATE", null, true, 0);
    }

    private void deletePolicy(String policyId) throws RemoteException, EntitlementPolicyAdminServiceEntitlementException{
        entitlementPolicyAdminServiceStub.removePolicy(policyId, false);
    }

    private void doSavePolicy(EntitlementPolicy policy) throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {

        PolicyDTO policyDTO = createNewPolicyDTO(policy);
        entitlementPolicyAdminServiceStub.addPolicy(policyDTO);

        // Publish the policy to the PDP.
        entitlementPolicyAdminServiceStub.publishToPDP(new String[]{policyDTO.getPolicyId()}, "CREATE", null, true, 0);

    }

    private PolicyDTO createNewPolicyDTO(EntitlementPolicy policy) {

        PolicyDTO policyDTO = new PolicyDTO();

        policyDTO.setPolicyId(policy.getPolicyId());
        policyDTO.setPolicy(policy.getPolicyContent());
        policyDTO.setPolicyEditor("XML");
        return policyDTO;
    }

    private String generateMockPolicy(String partial){
        XACMLTemplateBuilder templateBuilder = new XACMLTemplateBuilder();
        return templateBuilder.generateMockPolicy(partial);
    }

    private PEPProxy getPepProxy() {

        Map<String, Map<String, String>> appToPDPClientConfigMap = new HashMap<String, Map<String, String>>();
        Map<String, String> clientConfigMap = new HashMap<String, String>();

        clientConfigMap.put("client", "soap");
        clientConfigMap.put("serverUrl", this.serverUrl+"/services");
        clientConfigMap.put("userName", this.username);
        clientConfigMap.put("password", this.password);
        clientConfigMap.put("reuseSession", "yes");

        appToPDPClientConfigMap.put("AppManagerGateway", clientConfigMap);
        PEPProxyConfig config = new PEPProxyConfig(appToPDPClientConfigMap, "AppManagerGateway", "simple", 0, 0);

        PEPProxy pepProxy = null;
        try {
            pepProxy = new PEPProxy(config);
        } catch (EntitlementProxyException e) {
            log.error("Cannot create PEP proxy");
        }

        return pepProxy;

    }

    private EntitlementPolicy preProcess(EntitlementPolicy policy){

        String policyContent = policy.getPolicyContent();

        try {
            OMElement contentElement = AXIOMUtil.stringToOM(policyContent);

            // Replace PolicyId attribute with the generated Id.
            contentElement.addAttribute("PolicyId", policy.getPolicyId(), null);
            policy.setPolicyContent(contentElement.toStringWithConsume());

            return policy;

        } catch (XMLStreamException e) {
            log.error("Cannot process XACML policy content.");
            return null;
        }

    }

    private String login(String url, String username, String password) throws AxisFault {

        String host;

        try {
            host = new URL(url).getHost();
        } catch (MalformedURLException e) {
            throw new AxisFault("Entitlement service URL is malformed.", e);
        }

        AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(null, url + "/services/AuthenticationAdmin");
        ServiceClient client = authAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);

        try {
            authAdminStub.login(username, password, host);
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            return sessionCookie;
        } catch (RemoteException e) {
            throw new AxisFault("Error while contacting the authentication admin services", e);
        } catch (LoginAuthenticationExceptionException e) {
            throw new AxisFault("Error while authenticating against the entitlement service", e);
        }
    }
    
    private Attribute[] getEntitlementAttributes(EntitlementDecisionRequest request){
    
    	Attribute policyIdAttribute = new Attribute(XACML_ATTRIBUTE_CATEGORY_CUSTOM, XACML_ATTRIBUTE_ID_POLICY_ID, ProxyConstants.DEFAULT_DATA_TYPE, request.getPolicyId());
    	Attribute subjectIdAttribute = new Attribute(XACML_ATTRIBUTE_CATEGORY_SUBJECT, XACML_ATTRIBUTE_ID_SUBJECT_ID, ProxyConstants.DEFAULT_DATA_TYPE, request.getSubject());
    	
    	return new Attribute[]{policyIdAttribute, subjectIdAttribute};
    }
}
