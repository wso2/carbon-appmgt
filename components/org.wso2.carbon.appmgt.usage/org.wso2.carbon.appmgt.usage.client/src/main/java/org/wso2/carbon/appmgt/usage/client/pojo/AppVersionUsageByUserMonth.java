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

public class AppVersionUsageByUserMonth {

    private String username;
    private long requestCount;
    private String apiVersion;
    private String apiName;
    private String month;
    private String context;

    public AppVersionUsageByUserMonth(OMElement row) {
        apiName = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.API)).getText();
        context = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.CONTEXT)).getText();
        username = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.USER_ID)).getText();
        requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.REQUEST)).getText());
        apiVersion = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.VERSION)).getText();
        month = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.MONTH)).getText();
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

}
