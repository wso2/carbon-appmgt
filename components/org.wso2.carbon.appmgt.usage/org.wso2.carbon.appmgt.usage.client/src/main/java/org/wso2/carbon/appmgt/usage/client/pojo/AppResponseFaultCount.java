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

public class AppResponseFaultCount {
    private String apiName;
    private String apiVersion;
    private String context;
    private long faultCount;
    private String requestTime;

    public AppResponseFaultCount(OMElement row) {
        apiName = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.API)).getText();
        apiVersion = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.VERSION)).getText();
        context = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.CONTEXT)).getText();
        OMElement invocationTimeEle = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.INVOCATION_TIME));
        OMElement faultCountEle = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.FAULT));
        if (invocationTimeEle != null) {
            requestTime = invocationTimeEle.getText();
        }
        if (faultCountEle != null) {
            faultCount = (long) Double.parseDouble(faultCountEle.getText());
        }
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

    public long getFaultCount() {
        return faultCount;
    }

    public void setFaultCount(long faultCount) {
        this.faultCount = faultCount;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }


}
