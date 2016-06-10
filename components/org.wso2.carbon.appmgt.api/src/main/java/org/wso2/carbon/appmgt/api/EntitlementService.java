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

package org.wso2.carbon.appmgt.api;

import java.util.List;

import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementDecisionRequest;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicy;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicyValidationResult;
import org.wso2.carbon.appmgt.api.model.entitlement.XACMLPolicyTemplateContext;

/**
 * This is the service contract for entitlement service implementations.
 */
public interface EntitlementService {

    /**
     * Initializes the service.
     */
    void init() throws AppManagementException;

    /**
     * Saves the given policy.
     * @param policy
     */
    void savePolicy(EntitlementPolicy policy);

    /**
     * Checks whether the request is permitted.
     * @param request Request to be checked.
     * @return true if the request is permitted, false otherwise.
     */
    boolean isPermitted(EntitlementDecisionRequest request) throws AppManagementException;

    /**
     * Get the policy content from the policy id
     * @param policyId policyId
     * @return policy content
     */
    public String getPolicyContent(String policyId);

    /**
     * Updates the given policy
     * @param policy
     */
    void updatePolicy(EntitlementPolicy policy);

    /**
     * Remove a given policy
     * @param policyId
     */
    void removePolicy(String policyId);

    /**
     * Validates the given entitlement policy partial.
     * @param partial The entitlement policy partial to be validated.
     * @return The result of the validation.
     */
    EntitlementPolicyValidationResult validatePolicyPartial(String partial);

    /**
     * Validates the given entitlement policy.
     * @param policy The entitlement policy to be validated.
     * @return The result of the validation.
     */
    EntitlementPolicyValidationResult validatePolicy(EntitlementPolicy policy);

    /**
     * Generates entitlement policies using the context given, and saves them in the entitlement service implementation.
     * @param xacmlPolicyTemplateContexts
     */
    void generateAndSaveEntitlementPolicies(List<XACMLPolicyTemplateContext> xacmlPolicyTemplateContexts);
}
