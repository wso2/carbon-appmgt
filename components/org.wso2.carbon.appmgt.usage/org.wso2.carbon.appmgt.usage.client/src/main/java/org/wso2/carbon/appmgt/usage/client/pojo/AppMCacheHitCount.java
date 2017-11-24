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

public class AppMCacheHitCount {

    private String apiName;
    private String version;
    private long cacheHit;
    private String fullRequestPath;
    private long totalRequestCount;
    private String requestDate;

    public AppMCacheHitCount(OMElement row) {
        apiName = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.API)).getText();
        version = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.VERSION)).getText();
        totalRequestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.REQUEST)).getText());
        cacheHit = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.CACHEHIT)).getText());
        requestDate =row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.TIME)).getText();
        fullRequestPath = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.FULLREQUESTPATH)) .getText();
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getCacheHit() {
        return cacheHit;
    }

    public void setCacheHit(long cacheHit) {
        this.cacheHit = cacheHit;
    }

    public String getFullRequestPath() {
        return fullRequestPath;
    }

    public void setFullRequestPath(String fullRequestPath) {
        this.fullRequestPath = fullRequestPath;
    }

    public long getTotalRequestCount() {
        return totalRequestCount;
    }

    public void setTotalRequestCount(long totalRequestCount) {
        this.totalRequestCount = totalRequestCount;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }



}
