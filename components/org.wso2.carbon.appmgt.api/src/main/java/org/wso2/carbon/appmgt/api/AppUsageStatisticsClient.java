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

package org.wso2.carbon.appmgt.api;

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


public interface AppUsageStatisticsClient {

    public void initialize(String username) throws AppUsageQueryServiceClientException;

    /**
     * Returns a list of AppUsageDTO objects that contain information related to Apps that
     * belong to a particular provider and the number of total WebApp calls each WebApp has processed
     * up to now. This method does not distinguish between different WebApp versions. That is all
     * versions of a single WebApp are treated as one, and their individual request counts are summed
     * up to calculate a grand total per each WebApp.
     *
     * @param providerName Name of the WebApp provider
     * @param fromDate
     * @param toDate
     * @param limit Number of entries to return
     * @param tenantDomainName
     * @return a List of AppUsageDTO objects - possibly empty
     * @throws org.wso2.carbon.appmgt.api.exception.AppUsageQueryServiceClientException if an error occurs while contacting backend services
     */
    public List<AppUsageDTO> getUsageByAPIs(String providerName, String fromDate, String toDate,
                                            int limit, String tenantDomainName)
            throws AppUsageQueryServiceClientException;

    /**
     * Returns a list of AppVersionUsageDTO objects that contain information related to a
     * particular WebApp of a specified provider, along with the number of WebApp calls processed
     * by each version of that WebApp.
     *
     * @param providerName Name of the WebApp provider
     * @param apiName      Name of th WebApp
     * @return a List of AppVersionUsageDTO objects, possibly empty
     * @throws org.wso2.carbon.appmgt.api.exception.AppUsageQueryServiceClientException on error
     */
    public List<AppVersionUsageDTO> getUsageByAPIVersions(String providerName,
                                                          String apiName)
            throws AppUsageQueryServiceClientException;


    /**
     * Returns a list of AppVersionUsageDTO objects that contain information related to a
     * particular WebApp of a specified provider, along with the number of WebApp calls processed
     * by each version of that WebApp for a particular time preriod.
     *
     * @param providerName
     * @param apiName
     * @param fromDate
     * @param toDate
     * @return AppVersionUsageDTO
     * @throws org.wso2.carbon.appmgt.api.exception.AppUsageQueryServiceClientException
     */
    public List<AppVersionUsageDTO> getUsageByAPIVersions(String providerName, String apiName,
                                                          String fromDate, String toDate)
            throws AppUsageQueryServiceClientException;


    /**
     * Returns a list of AppResourcePathUsageDTO objects that contain information related to a
     * particular WebApp of a specified provider, along with the number of WebApp calls processed
     * by each resource path of that WebApp.
     *
     * @param providerName Name of the WebApp provider
     * @param fromDate
     * @param toDate
     * @return a List of AppResourcePathUsageDTO objects, possibly empty
     * @throws org.wso2.carbon.appmgt.api.exception.AppUsageQueryServiceClientException on error
     */
    public List<AppResourcePathUsageDTO> getAPIUsageByResourcePath(String providerName, String fromDate,
                                                                   String toDate)
            throws AppUsageQueryServiceClientException;

    public List<AppPageUsageDTO> getAPIUsageByPage(String providerName, String fromDate, String toDate,
                                                   String tenantDomainName)
            throws AppUsageQueryServiceClientException;


    /**
     * Returns a list of AppUsageByUserDTO objects that contain information related to
     * User wise WebApp Usage, along with the number of invocations, and WebApp Version
     *
     * @param providerName Name of the WebApp provider
     * @param fromDate
     * @param toDate
     * @param tenantDomainName
     * @return a List of AppUsageByUserDTO objects, possibly empty
     * @throws org.wso2.carbon.appmgt.api.exception.AppUsageQueryServiceClientException on error
     */
    public List<AppUsageByUserDTO> getAPIUsageByUser(String providerName, String fromDate, String toDate,
                                                     String tenantDomainName)
            throws AppUsageQueryServiceClientException;

    /**
     * Get no of app hits over time.
     *
     * @param fromDate String.
     * @param toDate   String.
     * @param tenantId int.
     * @return AppHitsStatsDTO App hits stats list.
     * @throws org.wso2.carbon.appmgt.api.exception.AppUsageQueryServiceClientException
     */
    public List<AppHitsStatsDTO> getAppHitsOverTime(String fromDate, String toDate, int tenantId)
            throws AppUsageQueryServiceClientException;

    /**
     * Get cache hit summary.
     *
     * @param providerName Name of the WebApp provider
     * @param fromDate String
     * @param toDate   String
     * @return AppMCacheCountDTO App cache hits stats list.
     * @throws org.wso2.carbon.appmgt.api.exception.AppUsageQueryServiceClientException
     */
    public List<AppMCacheCountDTO> getCacheHitCount(String providerName, String fromDate, String toDate)
            throws AppUsageQueryServiceClientException, SQLException, XMLStreamException;

    /**
     * Gets a list of AppResponseTimeDTO objects containing information related to Apps belonging
     * to a particular provider along with their average response times.
     *
     * @param providerName Name of the WebApp provider
     * @param fromDate
     * @param toDate
     * @param limit Number of entries to return
     * @param tenantDomain
     * @return a List of AppResponseTimeDTO objects, possibly empty
     * @throws org.wso2.carbon.appmgt.api.exception.AppUsageQueryServiceClientException on error
     */
    public List<AppResponseTimeDTO> getResponseTimesByAPIs(String providerName, String fromDate, String toDate,
                                                           int limit, String tenantDomain)
            throws AppUsageQueryServiceClientException;

    /**
     * Returns a list of AppVersionLastAccessTimeDTO objects for all the APIs belonging to the
     * specified provider. Last access times are calculated without taking WebApp versions into
     * account. That is all the versions of an WebApp are treated as one.
     *
     * @param providerName Name of the WebApp provider
     * @param fromDate
     * @param toDate
     * @param limit Number of entries to return
     * @param tenantDomainName
     * @return a list of AppVersionLastAccessTimeDTO objects, possibly empty
     * @throws org.wso2.carbon.appmgt.api.exception.AppUsageQueryServiceClientException on error
     */
    public List<AppVersionLastAccessTimeDTO> getLastAccessTimesByAPI(String providerName, String fromDate,
                                                                     String toDate, int limit, String tenantDomainName)
            throws AppUsageQueryServiceClientException;

    /**
     * Returns a sorted list of PerUserAPIUsageDTO objects related to a particular WebApp. The returned
     * list will only have at most limit + 1 entries. This method does not differentiate between
     * WebApp versions.
     *
     * @param providerName WebApp provider name
     * @param apiName      Name of the WebApp
     * @param limit        Number of sorted entries to return
     * @return a List of PerUserAPIUsageDTO objects - Possibly empty
     * @throws org.wso2.carbon.appmgt.api.exception.AppUsageQueryServiceClientException on error
     */
    public List<PerUserAPIUsageDTO> getUsageBySubscribers(String providerName, String apiName, int limit)
            throws AppUsageQueryServiceClientException;

    public List<AppResponseFaultCountDTO> getAPIResponseFaultCount(String providerName, String fromDate,
                                                                   String toDate)
            throws AppUsageQueryServiceClientException;

    public List<AppResponseFaultCountDTO> getAPIFaultyAnalyzeByTime(String providerName)
            throws AppUsageQueryServiceClientException;

    public List<PerUserAPIUsageDTO> getUsageBySubscribers(String providerName, String apiName,
                                                          String apiVersion, int limit)
            throws AppUsageQueryServiceClientException;

    public List<AppVersionUserUsageDTO> getUsageBySubscriber(String subscriberName, String period)
            throws Exception;

    public List<String> getFirstAccessTime(String providerName, int limit) throws AppUsageQueryServiceClientException;
}