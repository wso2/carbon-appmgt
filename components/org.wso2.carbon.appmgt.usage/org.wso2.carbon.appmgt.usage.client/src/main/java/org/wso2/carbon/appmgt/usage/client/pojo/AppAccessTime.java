/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/

package org.wso2.carbon.appmgt.usage.client.pojo;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.appmgt.usage.client.APIUsageStatisticsClientConstants;

import javax.xml.namespace.QName;

public class AppAccessTime {
    private String apiName;
    private String apiVersion;
    private String context;
    private double accessTime;
    private String username;

    public AppAccessTime(OMElement row) {
        String nameVersion = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.APP_VERSION)).getText();
        int index = nameVersion.lastIndexOf(":v");
        apiName = nameVersion.substring(0, index);
        apiVersion = nameVersion.substring(index + 2);
        context = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.CONTEXT)).getText();
        accessTime = Double.parseDouble(row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.REQUEST_TIME)).getText());
        username = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.USER_ID)).getText();
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public double getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(double accessTime) {
        this.accessTime = accessTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
