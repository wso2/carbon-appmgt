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

public class AppResponseTime {
    private String apiName;
    private String apiVersion;
    private String context;
    private double responseTime;
    private long responseCount;
    private String referer;
    private String page;
    private String pageName[];

    public AppResponseTime(OMElement row) {
        referer = "";
        String nameVersion = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.APP_VERSION)).getText();
        int index = nameVersion.lastIndexOf(":");
        apiName = nameVersion.substring(0, index);
        apiVersion = nameVersion.substring(index + 1);
        context = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.CONTEXT)).getText();
        responseTime = Double.parseDouble(row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.SERVICE_TIME)).getText());
        page = row.getFirstChildWithName(new QName(APIUsageStatisticsClientConstants.REFERER)).getText();
        pageName = page.split("//")[1].split("/");
        for(int x = 1;x<pageName.length;x++){
            referer =referer + "/"+pageName[x] ;
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

    public double getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(double responseTime) {
        this.responseTime = responseTime;
    }

    public long getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(long responseCount) {
        this.responseCount = responseCount;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

}
