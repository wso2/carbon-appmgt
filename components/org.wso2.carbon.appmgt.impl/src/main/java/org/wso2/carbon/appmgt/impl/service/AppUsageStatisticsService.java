/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appmgt.impl.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppUsageStatisticsClient;
import org.wso2.carbon.appmgt.api.dto.AppPageUsageDTO;
import org.wso2.carbon.appmgt.api.dto.AppResourcePathUsageDTO;
import org.wso2.carbon.appmgt.api.dto.AppResponseFaultCountDTO;
import org.wso2.carbon.appmgt.api.dto.AppResponseTimeDTO;
import org.wso2.carbon.appmgt.api.dto.AppUsageByUserDTO;
import org.wso2.carbon.appmgt.api.dto.AppUsageDTO;
import org.wso2.carbon.appmgt.api.dto.AppVersionLastAccessTimeDTO;
import org.wso2.carbon.appmgt.api.dto.AppVersionUsageDTO;
import org.wso2.carbon.appmgt.api.dto.AppVersionUserUsageDTO;
import org.wso2.carbon.appmgt.api.dto.AppMCacheCountDTO;
import org.wso2.carbon.appmgt.api.dto.AppHitsStatsDTO;
import org.wso2.carbon.appmgt.api.dto.PerUserAPIUsageDTO;
import org.wso2.carbon.appmgt.api.exception.AppUsageQueryServiceClientException;

import javax.xml.stream.XMLStreamException;
import java.sql.SQLException;
import java.util.List;

public class AppUsageStatisticsService {

    private AppUsageStatisticsClient appUsageStatisticsClient;
    private static final Log log = LogFactory.getLog(AppUsageStatisticsService.class);
    private String userName;

    public AppUsageStatisticsService(String userName) throws AppUsageQueryServiceClientException {
        appUsageStatisticsClient = ServiceReferenceHolder.getInstance().getAppUsageStatClient();
        if (appUsageStatisticsClient == null) {
            throw new AppUsageQueryServiceClientException("Cant find appUsageStatisticsClient.");
        }
        this.userName = userName;
    }

    public List<AppResponseTimeDTO> getResponseTimesByAPIs(String providerName, String fromDate, String toDate,
                                                           int limit, String tenantDomain)
            throws AppUsageQueryServiceClientException {
        synchronized (userName) {
            appUsageStatisticsClient.initialize(userName);
            return appUsageStatisticsClient.getResponseTimesByAPIs(providerName, fromDate, toDate, limit, tenantDomain);
        }
    }

    public List<AppVersionUsageDTO> getUsageByAPIVersions(String providerName,
                                                          String apiName)
            throws AppUsageQueryServiceClientException {
        synchronized (userName) {
            appUsageStatisticsClient.initialize(userName);
            return appUsageStatisticsClient.getUsageByAPIVersions(providerName, apiName);
        }
    }

    public List<AppUsageDTO> getUsageByAPIs(String providerName, String fromDate, String toDate,
                                            int limit, String tenantDomainName)
            throws AppUsageQueryServiceClientException {
        synchronized (userName) {
            appUsageStatisticsClient.initialize(userName);
            return appUsageStatisticsClient.getUsageByAPIs(providerName, fromDate, toDate, limit, tenantDomainName);
        }
    }

    public List<PerUserAPIUsageDTO> getUsageBySubscribers(String providerName, String apiName, int limit)
            throws AppUsageQueryServiceClientException {
        return appUsageStatisticsClient.getUsageBySubscribers(providerName, apiName, limit);
    }

    public List<AppResourcePathUsageDTO> getAPIUsageByResourcePath(String providerName, String fromDate, String toDate)
            throws AppUsageQueryServiceClientException {
        synchronized (userName) {
            appUsageStatisticsClient.initialize(userName);
            return appUsageStatisticsClient.getAPIUsageByResourcePath(providerName, fromDate, toDate);
        }
    }

    public List<AppPageUsageDTO> getAPIUsageByPage(String providerName, String fromDate, String toDate
            , String tenantDomainName)
            throws AppUsageQueryServiceClientException {
        synchronized (userName) {
            appUsageStatisticsClient.initialize(userName);
            return appUsageStatisticsClient.getAPIUsageByPage(providerName, fromDate, toDate, tenantDomainName);
        }
    }

    public List<AppUsageByUserDTO> getAPIUsageByUser(String providerName, String fromDate, String toDate,
                                                     String tenantDomainName)
            throws AppUsageQueryServiceClientException {
        synchronized (userName) {
            appUsageStatisticsClient.initialize(userName);
            return appUsageStatisticsClient.getAPIUsageByUser(providerName, fromDate, toDate, tenantDomainName);
        }
    }

    public List<AppMCacheCountDTO> getCacheHitCount(String providerName, String fromDate, String toDate)
            throws AppUsageQueryServiceClientException, SQLException, XMLStreamException {
        synchronized (userName) {
            appUsageStatisticsClient.initialize(userName);
            return appUsageStatisticsClient.getCacheHitCount(providerName, fromDate, toDate);
        }
    }

    public List<PerUserAPIUsageDTO> getUsageBySubscribers(String providerName, String apiName,
                                                          String apiVersion, int limit)
            throws AppUsageQueryServiceClientException {
        synchronized (userName) {
            appUsageStatisticsClient.initialize(userName);
            return appUsageStatisticsClient.getUsageBySubscribers(providerName, apiName, apiVersion, limit);
        }
    }

    public List<AppVersionLastAccessTimeDTO> getLastAccessTimesByAPI(String providerName, String fromDate, String toDate
            , int limit, String tenantDomainName)
            throws AppUsageQueryServiceClientException {
        synchronized (userName) {
            appUsageStatisticsClient.initialize(userName);
            return appUsageStatisticsClient.getLastAccessTimesByAPI(providerName, fromDate, toDate, limit,
                                                                    tenantDomainName);
        }
    }

    public List<AppResponseFaultCountDTO> getAPIFaultyAnalyzeByTime(String providerName)
            throws AppUsageQueryServiceClientException {
        synchronized (userName) {
            appUsageStatisticsClient.initialize(userName);
            return appUsageStatisticsClient.getAPIFaultyAnalyzeByTime(providerName);
        }
    }

    public List<String> getFirstAccessTime(String providerName, int limit)
            throws AppUsageQueryServiceClientException {
        synchronized (userName) {
            appUsageStatisticsClient.initialize(userName);
            return appUsageStatisticsClient.getFirstAccessTime(providerName, limit);
        }
    }

    public List<AppResponseFaultCountDTO> getAPIResponseFaultCount(String providerName, String fromDate, String toDate)
            throws AppUsageQueryServiceClientException {
        synchronized (userName) {
            appUsageStatisticsClient.initialize(userName);
            return appUsageStatisticsClient.getAPIResponseFaultCount(providerName, fromDate, toDate);
        }
    }

    public List<AppHitsStatsDTO> getAppHitsOverTime(String fromDate, String toDate, int tenantId)
            throws AppUsageQueryServiceClientException {
        synchronized (userName) {
            appUsageStatisticsClient.initialize(userName);
            return appUsageStatisticsClient.getAppHitsOverTime(fromDate, toDate, tenantId);
        }
    }

    public List<AppVersionUserUsageDTO> getUsageBySubscriber(String subscriberName, String period)
            throws Exception {
        synchronized (userName) {
            appUsageStatisticsClient.initialize(userName);
            return appUsageStatisticsClient.getUsageBySubscriber(subscriberName, period);
        }
    }
}
