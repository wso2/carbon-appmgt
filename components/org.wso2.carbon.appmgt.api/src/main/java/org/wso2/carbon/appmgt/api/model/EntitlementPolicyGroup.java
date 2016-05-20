/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.api.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * EntitlementPolicyGroup class contains properties related to Policy Groups
 */
public class EntitlementPolicyGroup {

    public static final String _KEY_POLICY_PARTIAL_ID = "POLICY_PARTIAL_ID";

    private int policyGroupId;
    private String policyGroupName;
    private String throttlingTier;
    private String userRoles;
    private boolean allowAnonymous;
    private JSONArray policyPartials; //XACML policies
    private List<String> xacmlPolicyNames;

    public List<String> getXacmlPolicyNames() {
        return xacmlPolicyNames;
    }

    public void setXacmlPolicyNames(List<String> xacmlPolicyNames) {
        this.xacmlPolicyNames = xacmlPolicyNames;
    }

    private String policyDescription;

    public void setPolicyGroupId(int policyGroupId) {
        this.policyGroupId = policyGroupId;
    }

    public int getPolicyGroupId() {
        return policyGroupId;
    }

    public void setPolicyGroupName(String policyGroupName) {

        this.policyGroupName = policyGroupName;
    }

    public String getPolicyGroupName() {
        return policyGroupName;
    }

    public void setThrottlingTier(String throttlingTier) {
        this.throttlingTier = throttlingTier;
    }

    public String getThrottlingTier() {
        return throttlingTier;
    }

    public void setUserRoles(String userRoles) {
        this.userRoles = userRoles;
    }

    public String getUserRoles() {
        return userRoles;
    }

    public void setAllowAnonymous(boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    public boolean isAllowAnonymous() {
        return allowAnonymous;
    }

    public void setPolicyPartials(JSONArray policyPartials) {
        this.policyPartials = policyPartials;
    }

    public JSONArray getPolicyPartials() {
        return policyPartials;
    }

    public void setPolicyDescription(String policyDescription) {
        this.policyDescription = policyDescription;
    }

    public String getPolicyDescription() {
        return policyDescription;
    }

    public int getFirstEntitlementPolicyId(){
        if(getPolicyPartials() != null){
            return (int) ((JSONObject) getPolicyPartials().get(0)).get(_KEY_POLICY_PARTIAL_ID);
        }else{
            return -1;
        }
    }

    public void setEntitlementPolicyId(int entitlementPolicyId){

        if(policyPartials != null){
            policyPartials.clear();
        }else {
            policyPartials = new JSONArray();
        }

        JSONObject entitlementPolicyIdObject = new JSONObject();
        entitlementPolicyIdObject.put(_KEY_POLICY_PARTIAL_ID, entitlementPolicyId);

        policyPartials.add(entitlementPolicyIdObject);
    }

    public List<String> getUserRolesAsList(){

        List<String> roles = new ArrayList<String>();

        if(userRoles != null){

            for(String role : userRoles.split(",")){
                roles.add(role);
            }

        }

        return roles;
    }
}
