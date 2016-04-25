/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.Map;

public class BusinessOwner {

    private int businessOwnerId;
    private String businessOwnerName;
    private String businessOwnerEmail;
    private String businessOwnereDescription;
    private String businessOwnerSite;
    private Map<String, String> businessOwnerDetails;



    public BusinessOwner() {

    }

    public void setBusinessOwnerDetails(Map<String, String> businessOwnerDetails) {
        this.businessOwnerDetails = businessOwnerDetails;
    }

    public void setBusinessOwnerId(int businessOwnerId) {
        this.businessOwnerId = businessOwnerId;
    }

    public void setBusinessOwnerName(String businessOwnerName) {
        this.businessOwnerName = businessOwnerName;
    }

    public void setBusinessOwnerEmail(String businessOwnerEmail) {
        this.businessOwnerEmail = businessOwnerEmail;
    }

    public void setBusinessOwnerSite(String businessOwnerSite) {
        this.businessOwnerSite = businessOwnerSite;
    }

    public void setBusinessOwnereDescription(String businessOwnereDescription) {
        this.businessOwnereDescription = businessOwnereDescription;
    }

    public int getBusinessOwnerId() {
        return businessOwnerId;
    }

    public String getBusinessOwnerName() {
        return businessOwnerName;
    }

    public String getBusinessOwnerEmail() {
        return businessOwnerEmail;
    }

    public String getBusinessOwnereDescription() {
        return businessOwnereDescription;
    }

    public String getBusinessOwnerSite() {
        return businessOwnerSite;
    }

    public Map<String, String> getBusinessOwnerDetails() {
        return businessOwnerDetails;
    }
}
