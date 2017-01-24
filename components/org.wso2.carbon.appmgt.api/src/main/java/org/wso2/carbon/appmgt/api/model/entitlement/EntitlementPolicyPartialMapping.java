/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Represents an policy partial mapping which is applied to a URL mapping.
 */
public class EntitlementPolicyPartialMapping {

    private int entitlementPolicyPartialId;
    private String effect;

    /**
     * Get policy partial id of mapping
     * @return entitlement policy partial id
     */
    public int getEntitlementPolicyPartialId() {
        return entitlementPolicyPartialId;
    }

    /**
     * Set policy partial id of mapping
     * @param entitlementPolicyPartialId entitlement policy partial id
     */
    public void setEntitlementPolicyPartialId(int entitlementPolicyPartialId) {
        this.entitlementPolicyPartialId = entitlementPolicyPartialId;
    }

    /**
     * Get the policy effect of mapping
     * @return effect ('Permit' or 'Deny')
     */
    public String getEffect() {
        return effect;
    }

    /**
     * Set the policy effect of mapping
     * @param effect effect ('Permit' or 'Deny')
     */
    public void setEffect(String effect) {
        this.effect = effect;
    }
}
