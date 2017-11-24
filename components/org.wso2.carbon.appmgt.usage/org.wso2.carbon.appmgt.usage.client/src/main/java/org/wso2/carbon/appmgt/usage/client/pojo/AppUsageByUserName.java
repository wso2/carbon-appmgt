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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.usage.client.pojo;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.appmgt.usage.client.APIUsageStatisticsClientConstants;

import javax.xml.namespace.QName;

public class AppUsageByUserName {
    private String apiName;
    private String apiVersion;
    private String context;
    private String userID;
    private String apiPublisher;
    private long requestCount;
    private String accessTime;
    private String requestDate;

    public AppUsageByUserName(OMElement row) {
        apiName = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.API)).getText();
        apiVersion = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.VERSION)).getText();
        userID = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.USER_ID)).getText();
        context = row.getFirstChildWithName(new QName(APIUsageStatisticsClientConstants.CONTEXT)) .getText();
        requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.REQUEST)).getText());
        accessTime =row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.TIME)).getText();
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

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getApiPublisher() {
        return apiPublisher;
    }

    public void setApiPublisher(String apiPublisher) {
        this.apiPublisher = apiPublisher;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(String accessTime) {
        this.accessTime = accessTime;
    }

}
