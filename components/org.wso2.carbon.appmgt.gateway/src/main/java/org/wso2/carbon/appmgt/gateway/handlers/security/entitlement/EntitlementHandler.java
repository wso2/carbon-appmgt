/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.gateway.handlers.security.entitlement;

import java.util.List;
import java.util.Map;

import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.EntitlementService;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementDecisionRequest;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.appmgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.entitlement.EntitlementServiceFactory;

/**
 * Handler class to check entitlement.
 */
public class EntitlementHandler extends AbstractHandler implements ManagedLifecycle {

	private static final Log log = LogFactory.getLog(EntitlementHandler.class);
	
    private AppManagerConfiguration configuration;

    public void init(SynapseEnvironment synapseEnvironment) {
        configuration = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
    }

    public boolean handleRequest(MessageContext messageContext) {

    	// If anonymous access is allowed to whole app or a particular URL pattern, skip this handler.
    	if (isHandlerApplicable(messageContext)) {
    		try {
				if(!isResourcePermitted(messageContext)){
					notifyUser(messageContext);
					return false;
				}else{
					return true;
				}
			} catch (AppManagementException e) {
				String message = "Error while evaluating entitlement policies";
				log.error(message, e);
				throw new SynapseException(message, e);
			}
        } else {
        	return true;
        }
    }

	public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    public void destroy() {

    }

    private boolean isHandlerApplicable(MessageContext messageContext) {
		return doesInUrlHasMatchingResourcePattern(messageContext) && !isAnnoymousAccessAllowed(messageContext);  
	}

	private boolean isAnnoymousAccessAllowed(MessageContext messageContext) {
		return (Boolean) messageContext.getProperty(AppMConstants.API_OVERVIEW_ALLOW_ANONYMOUS) ||
                (Boolean) messageContext.getProperty(AppMConstants.API_URI_ALLOW_ANONYMOUS);
	}
    
	private boolean doesInUrlHasMatchingResourcePattern(MessageContext messageContext) {
		return messageContext.getProperty(AppMConstants.MATCHED_URL_PATTERN_PROERTY_NAME) != null;
	}
    
    /**
     * Extracts info related to the resource requests and checkes whether the request is permitted.
     * @param messageContext Synapse message context.
     * @return true if the resource is permitted, false otherwise.
     * @throws AppManagementException 
     */
    private boolean isResourcePermitted(MessageContext messageContext) throws AppManagementException{
    	
    	
    	// Get the XACML policy ids to be evaluated.
    	List<String> applicablePolicyIds = getApplicableEntitlementPolicyIds(messageContext);
    	
    	// If there are not associated entitlement policies the resource is permitted anyway.
    	if(applicablePolicyIds.isEmpty()){
    		return true;
    	}else{
    		
    		// We only support only one XACML policy as of now.
    		String applicablePolicyId = applicablePolicyIds.get(0);
    		
    		EntitlementDecisionRequest entitlementDecisionRequest = getEntitlementDecisionRequest(messageContext, applicablePolicyId);
    		return isResourcePermitted(entitlementDecisionRequest);
    	}
    }

    private List<String> getApplicableEntitlementPolicyIds(MessageContext messageContext) throws AppManagementException {
		
    	Integer appId = (Integer) messageContext.getProperty(AppMConstants.MATCHED_APP_ID_PROERTY_NAME);
    	String matchedUrlPattern = (String) messageContext.getProperty(AppMConstants.MATCHED_URL_PATTERN_PROERTY_NAME);
    	String httpVerb = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(Constants.Configuration.HTTP_METHOD);
    	
    	AppMDAO appMDAO = new AppMDAO();
    	return appMDAO.getApplicableEntitlementPolicyIds(appId, matchedUrlPattern, httpVerb);
	}

	private boolean isResourcePermitted(EntitlementDecisionRequest request) throws AppManagementException {
        EntitlementService entitlementService = getEntitlementService();
        return entitlementService.isPermitted(request);
    }

	 /**
     * Creates and returns the entitlement decision request, from the message context.
     * @param messageContext Synapse message context.
     * @return entitlement decision request.
     */
    private EntitlementDecisionRequest getEntitlementDecisionRequest(MessageContext messageContext, String applicablePolicyId) {

        String subject = (String) messageContext.getProperty(APISecurityConstants.SUBJECT);

        EntitlementDecisionRequest entitlementDecisionRequest = new EntitlementDecisionRequest();
        entitlementDecisionRequest.setPolicyId(applicablePolicyId);
        entitlementDecisionRequest.setSubject(subject);

        return entitlementDecisionRequest;
    }
    
	/**
	 * Sends "401 Unauthorized" response to the user.
	 * 
	 * @param messageContext
	 */
	private void notifyUser(MessageContext messageContext) {

		org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
		Object headers = axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
		
		if (headers != null && headers instanceof Map) {
            
			@SuppressWarnings("unchecked")
			Map<String, Object> headersMap = (Map<String, Object>) headers;
            
			headersMap.clear();
            axis2MessageContext.setProperty("HTTP_SC", "401");
            axis2MessageContext.setProperty("NO_ENTITY_BODY", new Boolean("true"));
            messageContext.setProperty("RESPONSE", "true");
            messageContext.setTo(null);
            Axis2Sender.sendBack(messageContext);
        }
	}
	
    /**
     * Returns the Axis2 message context from the Synapse message context.
     * @param messageContext
     * @return Axis2 message context.
     */
    @SuppressWarnings("unused")
	private org.apache.axis2.context.MessageContext getAxis2MessageContext(MessageContext messageContext){
        return ((Axis2MessageContext) messageContext).getAxis2MessageContext();
    }

    /**
     * TODO : Remove this hack.
     * This hack is here since, handler init fails when it tries to get IS entitlement service stub, in embedded IS setup.
     * Service endpoint is not available when init is called.
     * @return
     */
    private EntitlementService getEntitlementService() throws AppManagementException {
        return EntitlementServiceFactory.getEntitlementService(configuration);
    }
}
