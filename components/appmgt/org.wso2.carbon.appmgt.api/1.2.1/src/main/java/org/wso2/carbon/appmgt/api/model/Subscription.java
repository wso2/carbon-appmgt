/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a subscription for a web app
 */
public class Subscription {

    public static final String SUBSCRIPTION_TYPE_ENTERPRISE = "ENTERPRISE";
    public static final String SUBSCRIPTION_TYPE_INDIVIDUAL = "INDIVIDUAL";

    private int subscriptionId;
    private int webAppId;
    private int applicationId;
    private String subscriptionType;
    private Set<String> trustedIdps;
    private String subscriptionStatus;
    private String tierId;

    public Subscription() {
        trustedIdps = new HashSet<String>();
    }

    public int getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(int subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public int getWebAppId() {
        return webAppId;
    }

    public void setWebAppId(int webAppId) {
        this.webAppId = webAppId;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(String subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public Set<String> getTrustedIdps() {
        return trustedIdps;
    }

    public void setTrustedIdps(Set<String> trustedIdps) {
        this.trustedIdps = trustedIdps;
    }

    public void addTrustedIdp(String idpName){
        trustedIdps.add(idpName);
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public String getTierId() {
        return tierId;
    }

    public void setTierId(String tierId) {
        this.tierId = tierId;
    }

    /**
     * Checks whether the given IDP is one of the trusted IDPs in this subscription.
     * @param authenticatedIDPs Name of the IDP
     * @return true if the given IDP is a trusted IDP, false otherwise.
     */
    public boolean isTrustedIdp(AuthenticatedIDP[] authenticatedIDPs){
        if(trustedIdps == null || trustedIdps.isEmpty()){
            return false;
        }else{
            boolean containsInAuthenticatedIDPs = false;
            for(AuthenticatedIDP authenticatedIDP:authenticatedIDPs){
                containsInAuthenticatedIDPs = trustedIdps.contains(authenticatedIDP.getIdpName());
                if(containsInAuthenticatedIDPs){
                    break;
                }
            }
            return containsInAuthenticatedIDPs;
        }
    }
}
