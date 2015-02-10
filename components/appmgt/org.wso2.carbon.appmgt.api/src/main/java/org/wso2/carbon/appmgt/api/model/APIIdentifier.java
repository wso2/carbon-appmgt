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

import org.wso2.carbon.appmgt.api.AppManagementException;

/**
 * An WebApp can be uniquely identified by a combination of providerName,apiName & version.
 * This class represents this unique identifier.
 */
@SuppressWarnings("unused")
public class APIIdentifier {

    private String providerName;
    private String apiName;
    private String version;
    private String tier;
    private String applicationId;
    private Integer policyGroupId; //Policy Group Id

    public APIIdentifier(Integer policyGroupId) {
        this.policyGroupId = policyGroupId;
    }

    public void setPolicyGroupId(Integer policyGroupId) {
        this.policyGroupId = policyGroupId;
    }

    public Integer getPolicyGroupId() {
        return policyGroupId;
    }


    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public APIIdentifier(String providerName, String apiName, String version) {
        this.providerName = providerName;
        this.apiName = apiName;
        this.version = version;
    }

    public APIIdentifier(String apiId) throws AppManagementException {
        //eg: apiId = "P1_API1_v1.0.0"
        String[] split = apiId.split("_");
        if (split.length == 3) {
            this.providerName = split[0];
            this.apiName = split[1];
            this.version = split[2];
        } else {
            throw new AppManagementException("Invalid WebApp ID : " + apiId);
        }
    }

    public String getProviderName() {
        return providerName;
    }

    public String getApiName() {
        return apiName;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        APIIdentifier that = (APIIdentifier) o;

        return apiName.equals(that.apiName) && providerName.equals(that.providerName) &&
                version.equals(that.version);
    }

    @Override
    public int hashCode() {
        int result = providerName.hashCode();
        result = 31 * result + apiName.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "APIIdentifier{" +
                "providerName='" + providerName + '\'' +
                ", apiName='" + apiName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
