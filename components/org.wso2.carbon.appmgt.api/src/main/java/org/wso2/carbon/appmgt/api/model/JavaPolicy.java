/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.HashMap;
import java.util.Map;

public class JavaPolicy {
    private Integer policyID;
    private String policyName;
    private String fullQualifiName; //full qualified name
    private Integer order; //display order
    private Map<String, String> properties = new HashMap<>();

    public void setPolicyID(Integer policyID) {
        this.policyID = policyID;
    }

    public Integer getPolicyID() {
        return policyID;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setFullQualifiName(String fullQualifiName) {
        this.fullQualifiName = fullQualifiName;
    }

    public String getFullQualifiName() {
        return fullQualifiName;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer getOrder() {
        return order;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
