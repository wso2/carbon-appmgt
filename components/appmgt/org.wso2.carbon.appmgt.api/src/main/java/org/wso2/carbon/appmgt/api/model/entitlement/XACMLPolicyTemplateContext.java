/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.​
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appmgt.api.model.entitlement;

/**
 * Encapsulate data for XACML policy generation.
 */
public class XACMLPolicyTemplateContext {

    private int urlTemplateId;
    private int policyPartialId;
    private String resource;
    private String action;
    private String effect;
    private String policyPartialContent;
    private String policyId;
    private int policyGroupId; //Policy Group Id

    /**
     * Get urlTemplate id
     * @return urlTemplate id
     */
    public int getUrlTemplateId() {
        return urlTemplateId;
    }

    /**
     * Set urlTemplate id
     * @param urlTemplateId urlTemplate id
     */
    public void setUrlTemplateId(int urlTemplateId) {
        this.urlTemplateId = urlTemplateId;
    }

    /**
     * Get entitlement policy partial id
     * @return entitlement policy partial id
     */
    public int getPolicyPartialId() {
        return policyPartialId;
    }

    /**
     * Set entitlement policy partial id
     * @param policyPartialId entitlement policy partial id
     */
    public void setPolicyPartialId(int policyPartialId) {
        this.policyPartialId = policyPartialId;
    }

    /**
     * Get Resource value of policy template
     * @return resource value
     */
    public String getResource() {
        return resource;
    }

    /**
     * Set 'Resource' value of policy template
     * @param resource resource value
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Get 'Action' value of policy template
     * @return action value
     */
    public String getAction() {
        return action;
    }

    /**
     * Set 'Action' value of policy template
     * @param action action value
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Get 'Effect' value of policy template
     * @return effect value
     */
    public String getEffect() {
        return effect;
    }

    /**
     * Set 'Effect' value of policy template
     * @param effect effect value
     */
    public void setEffect(String effect) {
        this.effect = effect;
    }

    /**
     * Get policy partial condition value of the policy template
     * @return partial condition value
     */
    public String getPolicyPartialContent() {
        return policyPartialContent;
    }

    /**
     * Set policy partial condition value of the policy template
     * @param policyPartialContent partial condition value
     */
    public void setPolicyPartialContent(String policyPartialContent) {
        this.policyPartialContent = policyPartialContent;
    }

    /**
     * Get entitlement policy id
     *
     * @return entitlement policy id
     */
    public String getPolicyId() {
        return policyId;
    }

    /**
     * Set entitlement policy id
     *
     * @param policyId entitlement policy id
     */
    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    /**
     * Set Policy Group Id
     *
     * @param policyGroupId : Policy Group Id
     */
    public void setPolicyGroupId(int policyGroupId) {
        this.policyGroupId = policyGroupId;
    }

    /**
     * Get Policy Group Id
     *
     * @return Policy Group Id
     */
    public int getPolicyGroupId() {
        return policyGroupId;
    }
}
