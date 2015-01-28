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

import org.apache.axis2.Constants;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.appmgt.api.EntitlementService;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementDecisionRequest;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.appmgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.entitlement.EntitlementServiceFactory;

/**
 * Handler class to check entitlement.
 */
public class EntitlementHandler extends AbstractHandler implements ManagedLifecycle {

    private AppManagerConfiguration configuration;

    public void init(SynapseEnvironment synapseEnvironment) {
        configuration = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
    }

    public boolean handleRequest(MessageContext messageContext) {
        if ((Boolean) messageContext.getProperty(AppMConstants.API_OVERVIEW_ALLOW_ANONYMOUS) ||
                (Boolean) messageContext.getProperty(AppMConstants.API_URI_ALLOW_ANONYMOUS)) {
            // If anonymous access is allowed to whole app
            // or
            // If anonymous access is allowed to particular URL pattern, skip
            // this Entitlement handler.
            return true;
        } else {
            return isResourcePermitted(messageContext);
        }
    }
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    public void destroy() {

    }

    /**
     * Extracts info related to the resource requests and checked whether the request is permitted.
     * @param messageContext Synapse message context.
     * @return true if the resource is permitted, false otherwise.
     */
    private boolean isResourcePermitted(MessageContext messageContext){
        EntitlementDecisionRequest entitlementDecisionRequest = getEntitlementDecisionRequest(messageContext);
        return isResourcePermitted(entitlementDecisionRequest);
    }

    private boolean isResourcePermitted(EntitlementDecisionRequest request){
        EntitlementService entitlementService = getEntitlementService();
        return entitlementService.isPermitted(request);
    }

    /**
     * Creates and returns the entitlement decision request, from the message context.
     * @param messageContext Synapse message context.
     * @return entitlement decision request.
     */
    private EntitlementDecisionRequest getEntitlementDecisionRequest(MessageContext messageContext) {

        String subject = (String) messageContext.getProperty(APISecurityConstants.SUBJECT);

        String resourcePath = getAxis2MessageContext(messageContext).getProperty(Constants.Configuration.TRANSPORT_IN_URL).toString();

        // Consider HTTP verb as the action of the request.
        String httpVerb = (String) getAxis2MessageContext(messageContext).getProperty(Constants.Configuration.HTTP_METHOD);

        EntitlementDecisionRequest entitlementDecisionRequest = new EntitlementDecisionRequest();
        entitlementDecisionRequest.setSubject(subject);
        entitlementDecisionRequest.setResource(resourcePath);
        entitlementDecisionRequest.setAction(httpVerb);

        return entitlementDecisionRequest;
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
     * TODO : Remove this hack.
     * This hack is here to since, handler init fails when it tries to get IS entitlement service stub, in embedded IS setup.
     * Service endpoint is not available when init is called.
     * @return
     */
    private EntitlementService getEntitlementService() {
        return EntitlementServiceFactory.getEntitlementService(configuration);
    }
}
