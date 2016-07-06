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

package org.wso2.carbon.appmgt.usage.client.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.AppUsageStatisticsClient;
import org.wso2.carbon.appmgt.api.dto.AppHitsStatsDTO;
import org.wso2.carbon.appmgt.api.dto.AppPageUsageDTO;
import org.wso2.carbon.appmgt.api.dto.AppResourcePathUsageDTO;
import org.wso2.carbon.appmgt.api.dto.AppResponseFaultCountDTO;
import org.wso2.carbon.appmgt.api.dto.AppResponseTimeDTO;
import org.wso2.carbon.appmgt.api.dto.AppUsageByUserDTO;
import org.wso2.carbon.appmgt.api.dto.AppUsageDTO;
import org.wso2.carbon.appmgt.api.dto.AppVersionLastAccessTimeDTO;
import org.wso2.carbon.appmgt.api.dto.AppVersionUsageDTO;
import org.wso2.carbon.appmgt.api.dto.AppVersionUserUsageDTO;
import org.wso2.carbon.appmgt.api.dto.PerUserAPIUsageDTO;
import org.wso2.carbon.appmgt.api.dto.UserHitsPerAppDTO;
import org.wso2.carbon.appmgt.api.exception.AppUsageQueryServiceClientException;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.APIManagerFactory;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.appmgt.usage.client.APIUsageStatisticsClientConstants;
import org.wso2.carbon.appmgt.usage.client.billing.PaymentPlan;
import org.wso2.carbon.appmgt.usage.client.internal.AppMUsageClientServiceComponent;
import org.wso2.carbon.appmgt.usage.client.pojo.AppAccessTime;
import org.wso2.carbon.appmgt.usage.client.pojo.AppFirstAccess;
import org.wso2.carbon.appmgt.usage.client.pojo.AppMCacheHitCount;
import org.wso2.carbon.appmgt.usage.client.pojo.AppResponseFaultCount;
import org.wso2.carbon.appmgt.usage.client.pojo.AppResponseTime;
import org.wso2.carbon.appmgt.usage.client.pojo.AppUsage;
import org.wso2.carbon.appmgt.usage.client.pojo.AppUsageByPage;
import org.wso2.carbon.appmgt.usage.client.pojo.AppUsageByResourcePath;
import org.wso2.carbon.appmgt.usage.client.pojo.AppUsageByUser;
import org.wso2.carbon.appmgt.usage.client.pojo.AppUsageByUserName;
import org.wso2.carbon.appmgt.usage.client.pojo.AppVersionUsageByUser;
import org.wso2.carbon.appmgt.usage.client.pojo.AppVersionUsageByUserMonth;
import org.wso2.carbon.bam.presentation.stub.QueryServiceStub;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.CarbonUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AppUsageStatisticsRdbmsClient implements AppUsageStatisticsClient {

    private APIProvider apiProviderImpl;
    private static volatile DataSource dataSource = null;
    private static PaymentPlan paymentPlan;
    private static String errorMsg = "DAS data source hasn't been initialized. Ensure that the data source is " +
            "properly configured in the APIUsageTracker configuration.";

    public AppUsageStatisticsRdbmsClient() {
    }

    public void initialize(String username) throws AppUsageQueryServiceClientException {
        initializeDataSource();
        OMElement element = null;
        AppManagerConfiguration config;
        try {
            config = AppMUsageClientServiceComponent.getAPIManagerConfiguration();
            String billingConfig = config.getFirstProperty("EnableBillingAndUsage");
            boolean isBillingEnabled = Boolean.parseBoolean(billingConfig);
            if (isBillingEnabled) {
                String filePath = (new StringBuilder()).append(CarbonUtils.getCarbonHome()).append(File.separator)
                        .append("repository").append(File.separator).append("conf").append(File.separator).append(
                                "billing-conf.xml").toString();
                element = buildOMElement(new FileInputStream(filePath));
                paymentPlan = new PaymentPlan(element);
            }
            String targetEndpoint = config.getFirstProperty("Analytics.DASServerURL");
            if (StringUtils.isBlank(targetEndpoint)) {
                throw new AppUsageQueryServiceClientException("Required DAS server URL parameter unspecified");
            }
            synchronized (this) {
                apiProviderImpl = APIManagerFactory.getInstance().getAPIProvider(username);
            }

        } catch (Exception e) {
            throw new AppUsageQueryServiceClientException(
                    "Exception while instantiating AppUsageStatisticsRdbmsClient", e);
        }
    }

    private void initializeDataSource() throws AppUsageQueryServiceClientException {
        if (dataSource != null) {
            return;
        }
        AppManagerConfiguration config = AppMUsageClientServiceComponent.getAPIManagerConfiguration();
        String dataSourceName = config.getFirstProperty(AppMConstants.DATA_SOURCE_NAME);

        if (dataSourceName != null) {
            try {
                Context ctx = new InitialContext();
                dataSource = (DataSource) ctx.lookup(dataSourceName);
            } catch (NamingException e) {
                throw new AppUsageQueryServiceClientException("Error while looking up the data " +
                                                                      "source: " + dataSourceName);
            }
        }
    }

    public List<AppUsageDTO> getUsageByApps(String providerName, String fromDate, String toDate,
                                            int limit, String tenantDomainName)
            throws AppUsageQueryServiceClientException {

        OMElement omElement = this.queryBetweenTwoDays(
                APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY, fromDate, toDate, null,
                tenantDomainName, limit);
        Collection<AppUsage> usageData = getUsageData(omElement);
        List<WebApp> providerAPIs = getAPIsByProvider(providerName, tenantDomainName);
        Map<String, AppUsageDTO> usageByAPIs = new TreeMap<String, AppUsageDTO>();
        for (AppUsage usage : usageData) {
            for (WebApp providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) &&
                        providerAPI.getId().getVersion().equals(usage.getApiVersion()) &&
                        providerAPI.getContext().equals(usage.getContext())) {
                    String apiName = usage.getApiName() + " (" + providerAPI.getId().getProviderName() + ")";
                    AppUsageDTO usageDTO = usageByAPIs.get(apiName);
                    if (usageDTO != null) {
                        usageDTO.setCount(usageDTO.getCount() + usage.getRequestCount());
                    } else {
                        usageDTO = new AppUsageDTO();
                        usageDTO.setApiName(apiName);
                        usageDTO.setCount(usage.getRequestCount());
                        usageByAPIs.put(apiName, usageDTO);
                    }
                }
            }
        }
        return getAPIUsageTopEntries(new ArrayList<AppUsageDTO>(usageByAPIs.values()), limit);
    }

    public List<AppVersionUsageDTO> getUsageByAppVersions(String providerName, String apiName,
                                                          String fromDate, String toDate)
            throws AppUsageQueryServiceClientException {

        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("api");
        compositeIndex[0].setRangeFirst(apiName);
        compositeIndex[0].setRangeLast(getNextStringInLexicalOrder(apiName));
        OMElement omElement = this.queryBetweenTwoDaysForAPIUsageByVersion(
                APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY, fromDate, toDate, compositeIndex);
        Collection<AppUsage> usageData = getUsageData(omElement);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        List<WebApp> providerAPIs = getAPIsByProvider(providerName, tenantDomain);
        Map<String, AppVersionUsageDTO> usageByVersions = new TreeMap<String, AppVersionUsageDTO>();

        for (AppUsage usage : usageData) {
            for (WebApp providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) &&
                        providerAPI.getId().getVersion().equals(usage.getApiVersion()) &&
                        providerAPI.getContext().equals(usage.getContext())) {

                    AppVersionUsageDTO usageDTO = new AppVersionUsageDTO();
                    usageDTO.setVersion(usage.getApiVersion());
                    usageDTO.setCount(usage.getRequestCount());
                    usageByVersions.put(usage.getApiVersion(), usageDTO);
                }
            }
        }
        return new ArrayList<AppVersionUsageDTO>(usageByVersions.values());
    }

    public List<AppVersionUsageDTO> getUsageByAppVersions(String providerName,
                                                          String apiName) throws AppUsageQueryServiceClientException {

        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("api");
        compositeIndex[0].setRangeFirst(apiName);
        compositeIndex[0].setRangeLast(getNextStringInLexicalOrder(apiName));
        OMElement omElement = this.queryBetweenTwoDaysForAPIUsageByVersion(
                APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY, null, null, compositeIndex);
        Collection<AppUsage> usageData = getUsageData(omElement);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        List<WebApp> providerAPIs = getAPIsByProvider(providerName, tenantDomain);
        Map<String, AppVersionUsageDTO> usageByVersions = new TreeMap<String, AppVersionUsageDTO>();

        for (AppUsage usage : usageData) {
            for (WebApp providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) &&
                        providerAPI.getId().getVersion().equals(usage.getApiVersion()) &&
                        providerAPI.getContext().equals(usage.getContext())) {

                    AppVersionUsageDTO usageDTO = new AppVersionUsageDTO();
                    usageDTO.setVersion(usage.getApiVersion());
                    usageDTO.setCount(usage.getRequestCount());
                    usageByVersions.put(usage.getApiVersion(), usageDTO);
                }
            }
        }

        return new ArrayList<AppVersionUsageDTO>(usageByVersions.values());
    }

    public List<AppResourcePathUsageDTO> getAppUsageByResourcePath(String providerName, String fromDate, String toDate)
            throws AppUsageQueryServiceClientException {

        OMElement omElement = this.queryToGetAPIUsageByResourcePath(
                APIUsageStatisticsClientConstants.API_Resource_Path_USAGE_SUMMARY, fromDate, toDate, null);
        Collection<AppUsageByResourcePath> usageData = getUsageDataByResourcePath(omElement);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        List<WebApp> providerAPIs = getAPIsByProvider(providerName, tenantDomain);
        List<AppResourcePathUsageDTO> usageByResourcePath = new ArrayList<AppResourcePathUsageDTO>();

        for (AppUsageByResourcePath usage : usageData) {
            for (WebApp providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) &&
                        providerAPI.getId().getVersion().equals(usage.getApiVersion()) &&
                        providerAPI.getContext().equals(usage.getContext())) {

                    AppResourcePathUsageDTO usageDTO = new AppResourcePathUsageDTO();
                    usageDTO.setApiName(usage.getApiName());
                    usageDTO.setVersion(usage.getApiVersion());
                    usageDTO.setMethod(usage.getMethod());
                    usageDTO.setContext(usage.getContext());
                    usageDTO.setCount(usage.getRequestCount());
                    usageByResourcePath.add(usageDTO);
                }
            }
        }
        return usageByResourcePath;
    }

    public List<AppPageUsageDTO> getAppUsageByPage(String providerName, String fromDate, String toDate
            , String tenantDomainName)
            throws AppUsageQueryServiceClientException {

        OMElement omElement = this.queryToGetAPIUsageByPage(
                APIUsageStatisticsClientConstants.API_PAGE_USAGE_SUMMARY, fromDate, toDate, null, tenantDomainName);
        Collection<AppUsageByPage> usageData = getUsageDataByPage(omElement);
        List<WebApp> providerAPIs = getAPIsByProvider(providerName, tenantDomainName);
        List<AppPageUsageDTO> usageByResourcePath = new ArrayList<AppPageUsageDTO>();

        for (AppUsageByPage usage : usageData) {
            for (WebApp providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) &&
                        providerAPI.getId().getVersion().equals(usage.getApiVersion()) &&
                        providerAPI.getContext().equals(usage.getContext())) {

                    AppPageUsageDTO usageDTO = new AppPageUsageDTO();
                    String apiName = usage.getApiName() + "(v" + providerAPI.getId().getVersion() + ")";
                    usageDTO.setApiName(apiName);
                    usageDTO.setVersion(usage.getApiVersion());
                    usageDTO.setUserId(usage.getUserId());

                    String referer = usage.getReferer();
                    String refererArray[] = referer.split("//")[1].split("/");
                    referer = "";
                    for (int y = 1; y < refererArray.length; y++) {
                        referer = referer + "/" + refererArray[y];
                    }
                    usageDTO.setReferer(referer);
                    usageDTO.setContext(usage.getContext());
                    usageDTO.setCount(usage.getRequestCount());
                    usageByResourcePath.add(usageDTO);
                }
            }
        }
        return usageByResourcePath;
    }

    public List<AppUsageByUserDTO> getAppUsageByUser(String providerName, String fromDate, String toDate,
                                                     String tenantDomainName)
            throws AppUsageQueryServiceClientException {

        OMElement omElement = this.queryBetweenTwoDaysForAPIUsageByUser(fromDate, toDate, null, tenantDomainName);
        Collection<AppUsageByUserName> usageData = getUsageDataByAPIName(omElement);
        List<WebApp> providerAPIs = getAPIsByProvider(providerName, tenantDomainName);
        List<AppUsageByUserDTO> usageByName = new ArrayList<AppUsageByUserDTO>();

        for (AppUsageByUserName usage : usageData) {
            for (WebApp providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.getApiName()) &&
                        providerAPI.getId().getVersion().equals(usage.getApiVersion())) {

                    AppUsageByUserDTO usageDTO = new AppUsageByUserDTO();
                    String apiName = usage.getApiName() + "(v" + providerAPI.getId().getVersion() + ")";
                    usageDTO.setApiName(apiName);
                    usageDTO.setVersion(usage.getApiVersion());
                    usageDTO.setUserID(usage.getUserID());
                    usageDTO.setContext(usage.getContext());
                    usageDTO.setCount(usage.getRequestCount());
                    usageDTO.setRequestDate(usage.getRequestDate());
                    usageByName.add(usageDTO);
                }
            }
        }
        return usageByName;
    }

    public List<AppHitsStatsDTO> getAppHitsOverTime(String fromDate, String toDate, int tenantId)
            throws AppUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new AppUsageQueryServiceClientException(errorMsg);
        }

        Map<String, AppHitsStatsDTO> appHitsStatsMap = new HashMap<String, AppHitsStatsDTO>();
        Connection connection = null;
        PreparedStatement getAppHitsStatement = null;
        ResultSet appInfoResult = null;
        List<AppHitsStatsDTO> appHitsStatsList = null;
        try {
            connection = dataSource.getConnection();
            String queryToGetAppsHits = "SELECT APP_NAME, CONTEXT, COUNT(*) " +
                    "AS TOTAL_HITS_COUNT, VERSION FROM APM_APP_HITS WHERE TENANT_ID = ? AND HIT_TIME " +
                    "BETWEEN ? AND ? GROUP BY APP_NAME, CONTEXT,VERSION ORDER BY TOTAL_HITS_COUNT";
            getAppHitsStatement = connection.prepareStatement(queryToGetAppsHits);
            getAppHitsStatement.setInt(1, tenantId);
            getAppHitsStatement.setString(2, fromDate);
            getAppHitsStatement.setString(3, toDate);
            appInfoResult = getAppHitsStatement.executeQuery();

            boolean noData = true;
            String queryToGetUserHits = "SELECT APP_NAME, CONTEXT, USER_ID, COUNT(*) AS USER_HITS_COUNT, VERSION " +
                    "FROM APM_APP_HITS WHERE CONTEXT IN ( ";

            while (appInfoResult.next()) {
                noData = false;
                AppHitsStatsDTO appHitsStats = new AppHitsStatsDTO();
                String context = appInfoResult.getString(APIUsageStatisticsClientConstants.CONTEXT);
                appHitsStats.setContext(context);
                queryToGetUserHits += "'" + context + "'";
                if (!appInfoResult.isLast()) {
                    queryToGetUserHits += ",";
                }
                String appNameWithVersion = appInfoResult.getString("APP_NAME") + "(v" + appInfoResult.getString(
                        "VERSION") + ")";
                appHitsStats.setAppName(appNameWithVersion);
                appHitsStats.setTotalHitCount(appInfoResult.getInt(
                        APIUsageStatisticsClientConstants.TOTAL_HITS_COUNT));
                appHitsStatsMap.put(appHitsStats.getAppName(), appHitsStats);
            }
            queryToGetUserHits += ") GROUP BY APP_NAME, CONTEXT, USER_ID, VERSION ORDER BY USER_ID";
            if (!noData) {
                appHitsStatsList = getUserHitsStats(connection, appHitsStatsMap, queryToGetUserHits);
            }
        } catch (SQLException e) {
            throw new AppUsageQueryServiceClientException("SQL Exception is occurred when " +
                                                                  "reading apps hits from SQL table" +
                                                                  e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(getAppHitsStatement, connection, appInfoResult);
        }
        return appHitsStatsList;
    }

    public List<AppResponseTimeDTO> getResponseTimesByApps(String providerName, String fromDate, String toDate,
                                                           int limit, String tenantDomain)
            throws AppUsageQueryServiceClientException {

        OMElement omElement = this.queryBetweenTwoDays(
                APIUsageStatisticsClientConstants.API_VERSION_SERVICE_TIME_SUMMARY, fromDate, toDate, null,
                tenantDomain, limit);
        Collection<AppResponseTime> responseTimes = getResponseTimeData(omElement);
        List<WebApp> providerAPIs = getAPIsByProvider(providerName, tenantDomain);

        List<AppResponseTimeDTO> list = new ArrayList<AppResponseTimeDTO>();
        int x = 0;

        for (AppResponseTime responseTime : responseTimes) {
            AppResponseTimeDTO responseTimeDTO = null;
            for (WebApp providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(responseTime.getApiName()) &&
                        providerAPI.getId().getVersion().equals(responseTime.getApiVersion()) &&
                        providerAPI.getContext().equals(responseTime.getContext())) {
                    String apiName = responseTime.getApiName() + "(v" + providerAPI.getId().getVersion() + ")";
                    responseTimeDTO = new AppResponseTimeDTO();
                    responseTimeDTO.setApiName(apiName);
                    responseTimeDTO.setContext(responseTime.getContext());
                    responseTimeDTO.setVersion(responseTime.getApiVersion());
                    responseTimeDTO.setReferer(responseTime.getReferer());
                    responseTimeDTO.setServiceTime(responseTime.getResponseTime());
                    list.add(x, responseTimeDTO);
                    x++;
                    break;
                }
            }
        }

        return list;
    }

    public List<AppVersionLastAccessTimeDTO> getLastAccessTimesByApps(String providerName, String fromDate,
                                                                      String toDate
            , int limit, String tenantDomainName)
            throws AppUsageQueryServiceClientException {

        OMElement omElement = this.queryBetweenTwoDays(
                APIUsageStatisticsClientConstants.API_VERSION_KEY_LAST_ACCESS_SUMMARY, fromDate, toDate, null,
                tenantDomainName, limit);
        Collection<AppAccessTime> accessTimes = getAccessTimeData(omElement);
        List<WebApp> providerAPIs = getAPIsByProvider(providerName, tenantDomainName);
        Map<String, AppAccessTime> lastAccessTimes = new TreeMap<String, AppAccessTime>();
        for (AppAccessTime accessTime : accessTimes) {
            for (WebApp providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(accessTime.getApiName()) &&
                        providerAPI.getId().getVersion().equals(accessTime.getApiVersion()) &&
                        providerAPI.getContext().equals(accessTime.getContext())) {

                    String apiName = accessTime.getApiName() + " (" + providerAPI.getId().getProviderName() + ")";
                    AppAccessTime lastAccessTime = lastAccessTimes.get(apiName);
                    if (lastAccessTime == null || lastAccessTime.getAccessTime() < accessTime.getAccessTime()) {
                        lastAccessTimes.put(apiName, accessTime);
                        break;
                    }
                }
            }
        }
        Map<String, AppVersionLastAccessTimeDTO> accessTimeByAPI = new TreeMap<String, AppVersionLastAccessTimeDTO>();
        List<AppVersionLastAccessTimeDTO> accessTimeDTOs = new ArrayList<AppVersionLastAccessTimeDTO>();
        DateFormat dateFormat = new SimpleDateFormat();
        for (Map.Entry<String, AppAccessTime> entry : lastAccessTimes.entrySet()) {
            AppVersionLastAccessTimeDTO accessTimeDTO = new AppVersionLastAccessTimeDTO();
            accessTimeDTO.setApiName(entry.getKey());
            AppAccessTime lastAccessTime = entry.getValue();
            accessTimeDTO.setApiVersion(lastAccessTime.getApiVersion());
            accessTimeDTO.setLastAccessTime(dateFormat.format(lastAccessTime.getAccessTime()));
            accessTimeDTO.setUser(lastAccessTime.getUsername());
            accessTimeByAPI.put(entry.getKey(), accessTimeDTO);
        }
        return getLastAccessTimeTopEntries(new ArrayList<AppVersionLastAccessTimeDTO>(accessTimeByAPI.values()), limit);

    }

    public List<PerUserAPIUsageDTO> getUsageBySubscribers(String providerName, String apiName, int limit)
            throws AppUsageQueryServiceClientException {

        OMElement omElement = this.queryDatabase(
                APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY, null);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        Collection<AppUsageByUser> usageData = getUsageBySubscriber(omElement);
        Map<String, PerUserAPIUsageDTO> usageByUsername = new TreeMap<String, PerUserAPIUsageDTO>();
        List<WebApp> apiList = getAPIsByProvider(providerName, tenantDomain);
        for (AppUsageByUser usageEntry : usageData) {
            for (WebApp api : apiList) {
                if (api.getContext().equals(usageEntry.getContext()) &&
                        api.getId().getApiName().equals(apiName)) {
                    PerUserAPIUsageDTO usageDTO = usageByUsername.get(usageEntry.getUsername());
                    if (usageDTO != null) {
                        usageDTO.setCount(usageDTO.getCount() + usageEntry.getRequestCount());
                    } else {
                        usageDTO = new PerUserAPIUsageDTO();
                        usageDTO.setUsername(usageEntry.getUsername());
                        usageDTO.setCount(usageEntry.getRequestCount());
                        usageByUsername.put(usageEntry.getUsername(), usageDTO);
                    }
                    break;
                }
            }
        }

        return getTopEntries(new ArrayList<PerUserAPIUsageDTO>(usageByUsername.values()), limit);
    }

    public List<AppResponseFaultCountDTO> getAppResponseFaultCount(String providerName, String fromDate, String toDate)
            throws AppUsageQueryServiceClientException {

        OMElement omElement = this.queryBetweenTwoDaysForFaulty(
                APIUsageStatisticsClientConstants.API_FAULT_SUMMARY, fromDate, toDate, null);
        Collection<AppResponseFaultCount> faultyData = getAPIResponseFaultCount(omElement);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        List<WebApp> providerAPIs = getAPIsByProvider(providerName, tenantDomain);
        List<AppResponseFaultCountDTO> faultyCount = new ArrayList<AppResponseFaultCountDTO>();
        List<AppVersionUsageDTO> apiVersionUsageList;
        AppVersionUsageDTO apiVersionUsageDTO;
        for (AppResponseFaultCount fault : faultyData) {
            for (WebApp providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(fault.getApiName()) &&
                        providerAPI.getId().getVersion().equals(fault.getApiVersion()) &&
                        providerAPI.getContext().equals(fault.getContext())) {

                    AppResponseFaultCountDTO faultyDTO = new AppResponseFaultCountDTO();
                    faultyDTO.setApiName(fault.getApiName());
                    faultyDTO.setVersion(fault.getApiVersion());
                    faultyDTO.setContext(fault.getContext());
                    faultyDTO.setCount(fault.getFaultCount());

                    apiVersionUsageList = getUsageByAppVersions(providerName, fault.getApiName(), fromDate, toDate);
                    for (int i = 0; i < apiVersionUsageList.size(); i++) {
                        apiVersionUsageDTO = apiVersionUsageList.get(i);
                        if (apiVersionUsageDTO.getVersion().equals(fault.getApiVersion())) {
                            double requestCount = apiVersionUsageDTO.getCount();
                            double faultPercentage = (requestCount - fault.getFaultCount()) / requestCount * 100;
                            DecimalFormat twoDForm = new DecimalFormat("#.##");
                            faultPercentage = 100 - Double.valueOf(twoDForm.format(faultPercentage));
                            faultyDTO.setFaultPercentage(faultPercentage);
                            break;
                        }
                    }
                    faultyCount.add(faultyDTO);
                }
            }
        }
        return faultyCount;
    }

    public List<AppResponseFaultCountDTO> getAppFaultyAnalyzeByTime(String providerName)
            throws AppUsageQueryServiceClientException {

        OMElement omElement = this.queryDatabase(
                APIUsageStatisticsClientConstants.API_REQUEST_TIME_FAULT_SUMMARY, null);
        Collection<AppResponseFaultCount> faultyData = getAPIResponseFaultCount(omElement);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        List<WebApp> providerAPIs = getAPIsByProvider(providerName, tenantDomain);
        List<AppResponseFaultCountDTO> faultyInvocations = new ArrayList<AppResponseFaultCountDTO>();

        for (AppResponseFaultCount fault : faultyData) {
            for (WebApp providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(fault.getApiName()) &&
                        providerAPI.getId().getVersion().equals(fault.getApiVersion()) &&
                        providerAPI.getContext().equals(fault.getContext())) {

                    AppResponseFaultCountDTO faultyDTO = new AppResponseFaultCountDTO();
                    faultyDTO.setApiName(fault.getApiName() + ":" + providerAPI.getId().getProviderName());
                    faultyDTO.setVersion(fault.getApiVersion());
                    faultyDTO.setContext(fault.getContext());
                    faultyDTO.setRequestTime(fault.getRequestTime());
                    faultyInvocations.add(faultyDTO);
                }
            }
        }
        return faultyInvocations;
    }

    public List<PerUserAPIUsageDTO> getUsageBySubscribers(String providerName, String apiName,
                                                          String apiVersion, int limit)
            throws AppUsageQueryServiceClientException {

        OMElement omElement = this.queryDatabase(
                APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY, null);

        Collection<AppUsageByUser> usageData = getUsageBySubscriber(omElement);
        Map<String, PerUserAPIUsageDTO> usageByUsername = new TreeMap<String, PerUserAPIUsageDTO>();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        List<WebApp> apiList = getAPIsByProvider(providerName, tenantDomain);
        for (AppUsageByUser usageEntry : usageData) {
            for (WebApp api : apiList) {
                if (api.getContext().equals(usageEntry.getContext()) &&
                        api.getId().getApiName().equals(apiName) &&
                        api.getId().getVersion().equals(apiVersion) &&
                        apiVersion.equals(usageEntry.getApiVersion())) {
                    PerUserAPIUsageDTO usageDTO = usageByUsername.get(usageEntry.getUsername());
                    if (usageDTO != null) {
                        usageDTO.setCount(usageDTO.getCount() + usageEntry.getRequestCount());
                    } else {
                        usageDTO = new PerUserAPIUsageDTO();
                        usageDTO.setUsername(usageEntry.getUsername());
                        usageDTO.setCount(usageEntry.getRequestCount());
                        usageByUsername.put(usageEntry.getUsername(), usageDTO);
                    }
                    break;
                }
            }
        }

        return getTopEntries(new ArrayList<PerUserAPIUsageDTO>(usageByUsername.values()), limit);
    }

    public List<AppVersionUserUsageDTO> getUsageBySubscriber(String subscriberName, String period) throws Exception {
        OMElement omElement;
        List<AppVersionUserUsageDTO> apiUserUsages = new ArrayList<AppVersionUserUsageDTO>();

        Calendar cal = Calendar.getInstance();
        int year = cal.get(cal.YEAR);
        int month = cal.get(cal.MONTH) + 1;
        if (!period.equals("" + year + "-" + month)) {
            omElement = this.queryDatabase(
                    APIUsageStatisticsClientConstants.KEY_USAGE_MONTH_SUMMARY, null);
            Collection<AppVersionUsageByUserMonth> usageData = getUsageAPIBySubscriberMonthly(omElement);
            int i = 0;
            for (AppVersionUsageByUserMonth usageEntry : usageData) {

                if (usageEntry.getUsername().equals(subscriberName) && usageEntry.getMonth().equals(period)) {

                    AppVersionUserUsageDTO userUsageDTO = new AppVersionUserUsageDTO();
                    userUsageDTO.setApiname(usageEntry.getApiName());
                    userUsageDTO.setContext(usageEntry.getContext());
                    userUsageDTO.setVersion(usageEntry.getApiVersion());
                    userUsageDTO.setCount(usageEntry.getRequestCount());
                    String cost = evaluate(usageEntry.getApiName(), (int) usageEntry.getRequestCount()).get("total")
                            .toString();
                    String costPerAPI = evaluate(usageEntry.getApiName(), (int) usageEntry.getRequestCount()).get(
                            "cost").toString();
                    userUsageDTO.setCost(cost);
                    userUsageDTO.setCostPerAPI(costPerAPI);
                    apiUserUsages.add(userUsageDTO);
                    i++;
                }
            }

        } else {
            omElement = this.queryDatabase(
                    APIUsageStatisticsClientConstants.KEY_USAGE_MONTH_SUMMARY, null);
            Collection<AppVersionUsageByUser> usageData = getUsageAPIBySubscriber(omElement);
            int i = 0;
            for (AppVersionUsageByUser usageEntry : usageData) {

                if (usageEntry.getUsername().equals(subscriberName)) {

                    AppVersionUserUsageDTO userUsageDTO = new AppVersionUserUsageDTO();
                    userUsageDTO.setApiname(usageEntry.getApiName());
                    userUsageDTO.setContext(usageEntry.getContext());
                    userUsageDTO.setVersion(usageEntry.getApiVersion());
                    userUsageDTO.setCount(usageEntry.getRequestCount());
                    String cost = evaluate(usageEntry.getApiName(), (int) usageEntry.getRequestCount()).get("total")
                            .toString();
                    String costPerAPI = evaluate(usageEntry.getApiName() + i, (int) usageEntry.getRequestCount()).get(
                            "cost").toString();
                    userUsageDTO.setCost(cost);
                    userUsageDTO.setCostPerAPI(costPerAPI);
                    apiUserUsages.add(userUsageDTO);
                    i++;
                }
            }
        }

        return apiUserUsages;
    }

    private OMElement queryBetweenTwoDays(String columnFamily, String fromDate, String toDate,
                                          QueryServiceStub.CompositeIndex[] compositeIndex, String tenantDomain,
                                          int limit)
            throws AppUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new AppUsageQueryServiceClientException(errorMsg);
        }

        String selectRowsByColumnName = null;
        String selectRowsByColumnValue = null;
        if (compositeIndex != null) {
            selectRowsByColumnName = compositeIndex[0].getIndexName();
            selectRowsByColumnValue = compositeIndex[0].getRangeFirst();
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query;

            query = "SELECT * FROM " + columnFamily + " WHERE " + APIUsageStatisticsClientConstants.API_PUBLISHER + " = ?";

            if (selectRowsByColumnName != null) {
                query = query + " AND " + selectRowsByColumnName + " = ? ";
            }

            //TODO: API_FAULT_COUNT need to populate according to match with given time range
            if (!columnFamily.equals(APIUsageStatisticsClientConstants.API_FAULT_SUMMARY) &&
                    selectRowsByColumnName != null && (toDate != null && fromDate != null)) {
                query += addRangeCondition(APIUsageStatisticsClientConstants.TIME, "AND", fromDate, toDate);
            }
            if (limit != Integer.MIN_VALUE) {
                if ((connection.getMetaData().getDriverName()).contains("Oracle")) {
                    query += " ROWNUM <= " + limit;
                } else {
                    query += " LIMIT " + limit;
                }
            }
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, tenantDomain);
            if (selectRowsByColumnName != null) {
                preparedStatement.setString(2, selectRowsByColumnValue);
            }

            rs = preparedStatement.executeQuery();
            StringBuilder returnStringBuilder = new StringBuilder("<omElement><rows>");
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                returnStringBuilder.append("<row>");
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String columnValue = rs.getString(columnName);
                    String xmlEscapedValue = StringEscapeUtils.escapeXml(columnValue);
                    returnStringBuilder.append("<" + columnName.toLowerCase() + ">" + xmlEscapedValue +
                            "</" + columnName.toLowerCase() + ">");
                }
                returnStringBuilder.append("</row>");
            }
            returnStringBuilder.append("</rows></omElement>");
            String returnString = returnStringBuilder.toString();
            return AXIOMUtil.stringToOM(returnString);

        } catch (Exception e) {
            throw new AppUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {

                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {

                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    private OMElement queryToGetAPIUsageByResourcePath(String columnFamily, String fromDate, String toDate,
                                                       QueryServiceStub.CompositeIndex[] compositeIndex)
            throws AppUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new AppUsageQueryServiceClientException(errorMsg);
        }

        String selectRowsByColumnName = null;
        String selectRowsByColumnValue = null;
        if (compositeIndex != null) {
            selectRowsByColumnName = compositeIndex[0].getIndexName();
            selectRowsByColumnValue = compositeIndex[0].getRangeFirst();
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query;
            if (selectRowsByColumnName != null) {
                query =
                        String.format("SELECT " +
                                APIUsageStatisticsClientConstants.API + "," +
                                APIUsageStatisticsClientConstants.VERSION + "," +
                                APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                                APIUsageStatisticsClientConstants.CONTEXT + "," +
                                APIUsageStatisticsClientConstants.METHOD + "," +
                                "SUM("+ APIUsageStatisticsClientConstants.REQUEST +") as " +
                                APIUsageStatisticsClientConstants.REQUEST +" FROM  %s WHERE %s " +
                                " = ? AND " + APIUsageStatisticsClientConstants.TIME +
                                " BETWEEN " +
                                "? AND ?" +
                                " GROUP BY "+
                                APIUsageStatisticsClientConstants.API + "," +
                                APIUsageStatisticsClientConstants.VERSION + "," +
                                APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                                APIUsageStatisticsClientConstants.CONTEXT + "," +
                                APIUsageStatisticsClientConstants.METHOD, columnFamily, selectRowsByColumnName);
                statement = connection.prepareStatement(query);
                statement.setString(1, selectRowsByColumnValue);
                statement.setString(2, fromDate);
                statement.setString(3, toDate);

            } else {
                query =
                        String.format("SELECT " +
                                APIUsageStatisticsClientConstants.API + "," +
                                APIUsageStatisticsClientConstants.VERSION + "," +
                                APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                                APIUsageStatisticsClientConstants.CONTEXT + "," +
                                APIUsageStatisticsClientConstants.METHOD + "," +
                                "SUM(" + APIUsageStatisticsClientConstants.REQUEST + ") as " +
                                APIUsageStatisticsClientConstants.REQUEST + " FROM  " +
                                " %s WHERE " + APIUsageStatisticsClientConstants.TIME + " BETWEEN ? AND ? " +
                                " GROUP BY " +
                                APIUsageStatisticsClientConstants.API + "," +
                                APIUsageStatisticsClientConstants.VERSION + "," +
                                APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                                APIUsageStatisticsClientConstants.CONTEXT + "," +
                                APIUsageStatisticsClientConstants.METHOD, columnFamily);
                statement = connection.prepareStatement(query);
                statement.setString(1, fromDate);
                statement.setString(2, toDate);

            }
            rs = statement.executeQuery();
            StringBuilder returnStringBuilder = new StringBuilder("<omElement><rows>");
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                returnStringBuilder.append("<row>");
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String columnValue = rs.getString(columnName);
                    String xmlEscapedValue = StringEscapeUtils.escapeXml(columnValue);
                    returnStringBuilder.append("<" + columnName.toLowerCase() + ">" + xmlEscapedValue +
                            "</" + columnName.toLowerCase() + ">");
                }
                returnStringBuilder.append("</row>");
            }
            returnStringBuilder.append("</rows></omElement>");
            String returnString = returnStringBuilder.toString();
            return AXIOMUtil.stringToOM(returnString);

        } catch (Exception e) {
            throw new AppUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {

                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    private OMElement queryBetweenTwoDaysForAPIUsageByVersion(String columnFamily, String fromDate, String toDate,
                                                              QueryServiceStub.CompositeIndex[] compositeIndex)
            throws AppUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new AppUsageQueryServiceClientException(errorMsg);
        }

        String selectRowsByColumnName = null;
        String selectRowsByColumnValue = null;
        if (compositeIndex != null) {
            selectRowsByColumnName = compositeIndex[0].getIndexName();
            selectRowsByColumnValue = compositeIndex[0].getRangeFirst();
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query;
            if (fromDate != null && toDate != null) {
                if (selectRowsByColumnName != null) {

                    query = String.format("SELECT " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            " SUM(" + APIUsageStatisticsClientConstants.REQUEST + ") as " + APIUsageStatisticsClientConstants.REQUEST +
                            " FROM  %s " +
                            " WHERE %s = ? " +
                            " AND " + APIUsageStatisticsClientConstants.TIME +
                            " BETWEEN ? AND ? " +
                            " GROUP BY " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                            APIUsageStatisticsClientConstants.CONTEXT, columnFamily, selectRowsByColumnName);
                    statement = connection.prepareStatement(query);
                    statement.setString(1, selectRowsByColumnValue);
                    statement.setString(2, fromDate);
                    statement.setString(3, toDate);
                } else {

                    query = String.format("SELECT " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            " SUM(" + APIUsageStatisticsClientConstants.REQUEST + ") as " + APIUsageStatisticsClientConstants.REQUEST +
                            " FROM  %s " +
                            " WHERE " +
                            APIUsageStatisticsClientConstants.TIME +
                            " BETWEEN ? AND ? " +
                            " GROUP BY " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                            APIUsageStatisticsClientConstants.CONTEXT, columnFamily);
                    statement = connection.prepareStatement(query);
                    statement.setString(1, fromDate);
                    statement.setString(2, toDate);
                }
            } else {
                if (selectRowsByColumnName != null) {

                    query = String.format("SELECT " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            " SUM(" + APIUsageStatisticsClientConstants.REQUEST + ") as " + APIUsageStatisticsClientConstants.REQUEST +
                            " FROM  %s " +
                            " WHERE %s = ? " +
                            " GROUP BY " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                            APIUsageStatisticsClientConstants.CONTEXT, columnFamily, selectRowsByColumnName);
                    statement = connection.prepareStatement(query);
                    statement.setString(1, selectRowsByColumnValue);

                } else {
                    query = "SELECT api,version,apiPublisher,context,SUM(total_request_count) as total_request_count " +
                            " FROM" + columnFamily +
                            " GROUP BY api,version,apiPublisher,context";
                    query = String.format("SELECT " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            " SUM(" + APIUsageStatisticsClientConstants.REQUEST + ") as " + APIUsageStatisticsClientConstants.REQUEST +
                            " FROM  %s " +
                            " GROUP BY " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                            APIUsageStatisticsClientConstants.CONTEXT, columnFamily);
                    statement = connection.prepareStatement(query);
                }
            }
            rs = statement.executeQuery();
            StringBuilder returnStringBuilder = new StringBuilder("<omElement><rows>");
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                returnStringBuilder.append("<row>");
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String columnValue = rs.getString(columnName);
                    String xmlEscapedValue = StringEscapeUtils.escapeXml(columnValue);
                    returnStringBuilder.append("<" + columnName.toLowerCase() + ">" + xmlEscapedValue +
                                                       "</" + columnName.toLowerCase() + ">");
                }
                returnStringBuilder.append("</row>");
            }
            returnStringBuilder.append("</rows></omElement>");
            String returnString = returnStringBuilder.toString();
            return AXIOMUtil.stringToOM(returnString);

        } catch (Exception e) {
            throw new AppUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {

                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    private OMElement queryBetweenTwoDaysForAPIUsageByUser(String fromDate, String toDate, Integer limit,
                                                           String tenantDomain)
            throws AppUsageQueryServiceClientException {
        if (dataSource == null) {
            throw new AppUsageQueryServiceClientException(errorMsg);
        }

        int resultsLimit = APIUsageStatisticsClientConstants.DEFAULT_RESULTS_LIMIT;
        if (limit != null) {
            resultsLimit = limit.intValue();
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query;
            String oracleQuery;
            if (fromDate != null && toDate != null) {

                if ((connection.getMetaData().getDriverName()).contains("Oracle")) {
                    query = "SELECT " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.API_VERSION + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.USERID + "," +
                            " SUM(" + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS " +
                            APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ", " +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            APIUsageStatisticsClientConstants.TIME +
                            " FROM " + APIUsageStatisticsClientConstants.API_PAGE_USAGE_SUMMARY +
                            " WHERE " + APIUsageStatisticsClientConstants.API_PUBLISHER + " = ? AND " +
                            APIUsageStatisticsClientConstants.TIME + " BETWEEN ? AND ? AND ROWNUM <= ? " +
                            " GROUP BY " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.API_VERSION + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.USERID + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            APIUsageStatisticsClientConstants.TIME +
                            " ORDER BY " +
                            APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + " DESC ";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, tenantDomain);
                    statement.setString(2, fromDate);
                    statement.setString(3, toDate);
                    statement.setInt(4, resultsLimit);

                } else {
                    query = "SELECT " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.API_VERSION + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.USERID + "," +
                            " SUM(" + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS " +
                            APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ", " +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            APIUsageStatisticsClientConstants.TIME +
                            " FROM " + APIUsageStatisticsClientConstants.API_PAGE_USAGE_SUMMARY +
                            " WHERE " + APIUsageStatisticsClientConstants.API_PUBLISHER + " = ? AND " +
                            APIUsageStatisticsClientConstants.TIME + " BETWEEN ? AND ? " +
                            " GROUP BY " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.API_VERSION + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.USERID + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            APIUsageStatisticsClientConstants.TIME +
                            " ORDER BY " +
                            APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + " DESC ";

                    statement = connection.prepareStatement(query);
                    statement.setString(1, tenantDomain);
                    statement.setString(2, fromDate);
                    statement.setString(3, toDate);
                }
            } else {

                if ((connection.getMetaData().getDriverName()).contains("Oracle")) {
                    query = "SELECT " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.API_VERSION + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.USERID + "," +
                            " SUM(" + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS " +
                            APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ", " +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            APIUsageStatisticsClientConstants.TIME +
                            " FROM " + APIUsageStatisticsClientConstants.API_PAGE_USAGE_SUMMARY +
                            " WHERE " + APIUsageStatisticsClientConstants.API_PUBLISHER + " = ? AND ROWNUM <= ?" +
                            " GROUP BY " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.API_VERSION + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.USERID + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            APIUsageStatisticsClientConstants.TIME +
                            " ORDER BY " +
                            APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + " DESC ";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, tenantDomain);
                    statement.setInt(2, resultsLimit);
                }else{
                    query = "SELECT " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.API_VERSION + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.USERID + "," +
                            " SUM(" + APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ") AS " +
                            APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + ", " +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            APIUsageStatisticsClientConstants.TIME +
                            " FROM " + APIUsageStatisticsClientConstants.API_PAGE_USAGE_SUMMARY +
                            " WHERE " + APIUsageStatisticsClientConstants.API_PUBLISHER + " = ?" +
                            " GROUP BY " +
                            APIUsageStatisticsClientConstants.API + "," +
                            APIUsageStatisticsClientConstants.API_VERSION + "," +
                            APIUsageStatisticsClientConstants.VERSION + "," +
                            APIUsageStatisticsClientConstants.USERID + "," +
                            APIUsageStatisticsClientConstants.CONTEXT + "," +
                            APIUsageStatisticsClientConstants.TIME +
                            " ORDER BY " +
                            APIUsageStatisticsClientConstants.TOTAL_REQUEST_COUNT + " DESC ";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, tenantDomain);
                }
            }

            rs = statement.executeQuery();
            StringBuilder returnStringBuilder = new StringBuilder("<omElement><rows>");
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                returnStringBuilder.append("<row>");
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String columnValue = rs.getString(columnName);
                    String xmlEscapedValue = StringEscapeUtils.escapeXml(columnValue);
                    returnStringBuilder.append("<" + columnName.toLowerCase() + ">" + xmlEscapedValue +
                            "</" + columnName.toLowerCase() + ">");
                }
                returnStringBuilder.append("</row>");
            }
            returnStringBuilder.append("</rows></omElement>");
            String returnString = returnStringBuilder.toString();
            return AXIOMUtil.stringToOM(returnString);

        } catch (Exception e) {
            throw new AppUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {

                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    private List<AppHitsStatsDTO> getUserHitsStats(Connection connection,
                                                   Map<String, AppHitsStatsDTO> appHitsStatsMap,
                                                   String queryToGetUserHits)
            throws AppUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new AppUsageQueryServiceClientException(errorMsg);
        }
        PreparedStatement getAppHitsStatement = null;
        ResultSet appInfoResult = null;

        try {
            getAppHitsStatement = connection.prepareStatement(queryToGetUserHits);
            appInfoResult = getAppHitsStatement.executeQuery();
            while (appInfoResult.next()) {
                UserHitsPerAppDTO userHitsPerApp = new UserHitsPerAppDTO();
                userHitsPerApp.setUserName(appInfoResult.getString("USER_ID"));
                userHitsPerApp.setUserHitsCount(
                        appInfoResult.getInt(APIUsageStatisticsClientConstants.USER_HITS_COUNT));
                String context = appInfoResult.getString(APIUsageStatisticsClientConstants.CONTEXT);
                userHitsPerApp.setContext(context);
                String appNameWithVersion = appInfoResult.getString("APP_NAME") + "(v" + appInfoResult.getString(
                        "VERSION") + ")";
                AppHitsStatsDTO appHitsStats = appHitsStatsMap.get(appNameWithVersion);
                List<UserHitsPerAppDTO> userHitsStatsList = appHitsStats.getUserHitsList();
                userHitsStatsList.add(userHitsPerApp);
            }
            List<AppHitsStatsDTO> appHitsStatsList =
                    new ArrayList<AppHitsStatsDTO>(appHitsStatsMap.values());
            return appHitsStatsList;
        } catch (SQLException e) {
            throw new AppUsageQueryServiceClientException("SQL Exception is occurred when " +
                    "reading user hits from SQL table" +
                    e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(getAppHitsStatement, null, appInfoResult);
        }
    }

    private OMElement queryBetweenTwoDaysForFaulty(String columnFamily, String fromDate, String toDate,
                                                   QueryServiceStub.CompositeIndex[] compositeIndex)
            throws AppUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new AppUsageQueryServiceClientException(errorMsg);
        }

        String selectRowsByColumnName = null;
        String selectRowsByColumnValue = null;
        if (compositeIndex != null) {
            selectRowsByColumnName = compositeIndex[0].getIndexName();
            selectRowsByColumnValue = compositeIndex[0].getRangeFirst();
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query;
            if (selectRowsByColumnName != null) {

                query = String.format("SELECT " +
                        APIUsageStatisticsClientConstants.API + "," +
                        APIUsageStatisticsClientConstants.VERSION + "," +
                        APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                        APIUsageStatisticsClientConstants.CONTEXT + "," +
                        "SUM(" + APIUsageStatisticsClientConstants.FAULT + ") as " +
                        APIUsageStatisticsClientConstants.FAULT + " FROM %s "  +
                        " WHERE %s = ? AND " + APIUsageStatisticsClientConstants.TIME +
                        " BETWEEN ? AND ? " +
                        " GROUP BY " +
                        APIUsageStatisticsClientConstants.API + "," +
                        APIUsageStatisticsClientConstants.VERSION + "," +
                        APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                        APIUsageStatisticsClientConstants.CONTEXT, columnFamily, selectRowsByColumnName);
                statement = connection.prepareStatement(query);
                statement.setString(1, selectRowsByColumnValue);
                statement.setString(2, fromDate);
                statement.setString(3, toDate);
            } else {
                query = String.format("SELECT "+
                        APIUsageStatisticsClientConstants.API + "," +
                        APIUsageStatisticsClientConstants.VERSION + "," +
                        APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                        APIUsageStatisticsClientConstants.CONTEXT + "," +
                        "SUM(" + APIUsageStatisticsClientConstants.FAULT + ") as " +
                        APIUsageStatisticsClientConstants.FAULT + " FROM %s " +
                        " WHERE " + APIUsageStatisticsClientConstants.TIME +
                        " BETWEEN ? AND ? " +
                        " GROUP BY " +
                        APIUsageStatisticsClientConstants.API + "," +
                        APIUsageStatisticsClientConstants.VERSION + "," +
                        APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                        APIUsageStatisticsClientConstants.CONTEXT, columnFamily);
                statement = connection.prepareStatement(query);
                statement.setString(1, fromDate);
                statement.setString(2, toDate);
            }
            rs = statement.executeQuery();
            StringBuilder returnStringBuilder = new StringBuilder("<omElement><rows>");
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                returnStringBuilder.append("<row>");
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String columnValue = rs.getString(columnName);
                    String xmlEscapedValue = StringEscapeUtils.escapeXml(columnValue);
                    returnStringBuilder.append("<" + columnName.toLowerCase() + ">" + xmlEscapedValue +
                            "</" + columnName.toLowerCase() + ">");
                }
                returnStringBuilder.append("</row>");
            }
            returnStringBuilder.append("</rows></omElement>");
            String returnString = returnStringBuilder.toString();
            return AXIOMUtil.stringToOM(returnString);

        } catch (Exception e) {
            throw new AppUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {

                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    private OMElement queryToGetAPIUsageByPage(String columnFamily, String fromDate, String toDate,
                                               QueryServiceStub.CompositeIndex[] compositeIndex,
                                               String tenantDomainName)
            throws AppUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new AppUsageQueryServiceClientException(errorMsg);
        }

        String selectRowsByColumnName = null;
        String selectRowsByColumnValue = null;
        if (compositeIndex != null) {
            selectRowsByColumnName = compositeIndex[0].getIndexName();
            selectRowsByColumnValue = compositeIndex[0].getRangeFirst();
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query;
            if (selectRowsByColumnName != null) {
                if (fromDate != null && toDate != null) {
                    query =
                            String.format("SELECT " +
                                    APIUsageStatisticsClientConstants.API + "," +
                                    APIUsageStatisticsClientConstants.VERSION + "," +
                                    APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                                    APIUsageStatisticsClientConstants.CONTEXT + "," +
                                    APIUsageStatisticsClientConstants.REFERER + "," +
                                    APIUsageStatisticsClientConstants.USER_ID + "," +
                                    "SUM(" + APIUsageStatisticsClientConstants.REQUEST + ") as " +
                                    APIUsageStatisticsClientConstants.REQUEST + " FROM %s " +
                                    "WHERE " + APIUsageStatisticsClientConstants.API_PUBLISHER + " = ? AND %s = ? " +
                                    "AND " + APIUsageStatisticsClientConstants.TIME + " BETWEEN ? AND ? " +
                                    " GROUP BY " + APIUsageStatisticsClientConstants.API + "," +
                                    APIUsageStatisticsClientConstants.VERSION + "," +
                                    APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                                    APIUsageStatisticsClientConstants.CONTEXT + "," +
                                    APIUsageStatisticsClientConstants.USER_ID + "," +
                                    APIUsageStatisticsClientConstants.REFERER, columnFamily, selectRowsByColumnName);

                    statement = connection.prepareStatement(query);
                    statement.setString(1, tenantDomainName);
                    statement.setString(2, selectRowsByColumnValue);
                    statement.setString(3, fromDate);
                    statement.setString(4, toDate);
                } else {
                    query =
                            String.format("SELECT " +
                                    APIUsageStatisticsClientConstants.API + "," +
                                    APIUsageStatisticsClientConstants.VERSION + "," +
                                    APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                                    APIUsageStatisticsClientConstants.CONTEXT + "," +
                                    APIUsageStatisticsClientConstants.REFERER + "," +
                                    APIUsageStatisticsClientConstants.USER_ID + "," +
                                    "SUM(" + APIUsageStatisticsClientConstants.REQUEST + ") as " +
                                    APIUsageStatisticsClientConstants.REQUEST + " FROM %s " +
                                    "WHERE " + APIUsageStatisticsClientConstants.API_PUBLISHER + " = ? AND %s = ? " +
                                    " GROUP BY " + APIUsageStatisticsClientConstants.API + "," +
                                    APIUsageStatisticsClientConstants.VERSION + "," +
                                    APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                                    APIUsageStatisticsClientConstants.CONTEXT + "," +
                                    APIUsageStatisticsClientConstants.USER_ID + "," +
                                    APIUsageStatisticsClientConstants.REFERER, columnFamily, selectRowsByColumnName);

                    statement = connection.prepareStatement(query);
                    statement.setString(1, tenantDomainName);
                    statement.setString(2, selectRowsByColumnValue);

                }

            } else {

                if (fromDate != null && toDate != null) {
                    query =
                            String.format("SELECT " +
                                    APIUsageStatisticsClientConstants.API + "," +
                                    APIUsageStatisticsClientConstants.VERSION + "," +
                                    APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                                    APIUsageStatisticsClientConstants.CONTEXT + "," +
                                    APIUsageStatisticsClientConstants.REFERER + "," +
                                    APIUsageStatisticsClientConstants.USER_ID + "," +
                                    "SUM(" + APIUsageStatisticsClientConstants.REQUEST + ") as " +
                                    APIUsageStatisticsClientConstants.REQUEST + " FROM %s " +
                                    "WHERE " + APIUsageStatisticsClientConstants.API_PUBLISHER + " = ? " +
                                    "AND " + APIUsageStatisticsClientConstants.TIME + " BETWEEN ? AND ? " +
                                    " GROUP BY " + APIUsageStatisticsClientConstants.API + "," +
                                    APIUsageStatisticsClientConstants.VERSION + "," +
                                    APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                                    APIUsageStatisticsClientConstants.CONTEXT + "," +
                                    APIUsageStatisticsClientConstants.USER_ID + "," +
                                    APIUsageStatisticsClientConstants.REFERER, columnFamily);

                    statement = connection.prepareStatement(query);
                    statement.setString(1, tenantDomainName);
                    statement.setString(2, fromDate);
                    statement.setString(3, toDate);
                }else{

                }
                query =
                        String.format("SELECT " +
                                APIUsageStatisticsClientConstants.API + "," +
                                APIUsageStatisticsClientConstants.VERSION + "," +
                                APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                                APIUsageStatisticsClientConstants.CONTEXT + "," +
                                APIUsageStatisticsClientConstants.REFERER + "," +
                                APIUsageStatisticsClientConstants.USER_ID + "," +
                                "SUM(" + APIUsageStatisticsClientConstants.REQUEST + ") as " +
                                APIUsageStatisticsClientConstants.REQUEST + " FROM %s " +
                                "WHERE " + APIUsageStatisticsClientConstants.API_PUBLISHER + " = ? " +
                                " GROUP BY " + APIUsageStatisticsClientConstants.API + "," +
                                APIUsageStatisticsClientConstants.VERSION + "," +
                                APIUsageStatisticsClientConstants.API_PUBLISHER + "," +
                                APIUsageStatisticsClientConstants.CONTEXT + "," +
                                APIUsageStatisticsClientConstants.USER_ID + "," +
                                APIUsageStatisticsClientConstants.REFERER, columnFamily);
                statement = connection.prepareStatement(query);
                statement.setString(1, tenantDomainName);
            }
            rs = statement.executeQuery();
            StringBuilder returnStringBuilder = new StringBuilder("<omElement><rows>");
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                returnStringBuilder.append("<row>");
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String columnValue = rs.getString(columnName);
                    String xmlEscapedValue = StringEscapeUtils.escapeXml(columnValue);
                    returnStringBuilder.append("<" + columnName.toLowerCase() + ">" + xmlEscapedValue +
                            "</" + columnName.toLowerCase() + ">");
                }
                returnStringBuilder.append("</row>");
            }
            returnStringBuilder.append("</rows></omElement>");
            String returnString = returnStringBuilder.toString();
            return AXIOMUtil.stringToOM(returnString);

        } catch (Exception e) {
            throw new AppUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {

                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    private Collection<AppFirstAccess> getFirstAccessTime(OMElement data) {
        List<AppFirstAccess> usageData = new ArrayList<AppFirstAccess>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        OMElement rowElement = rowsElement.getFirstChildWithName(new QName(APIUsageStatisticsClientConstants.ROW));
        usageData.add(new AppFirstAccess(rowElement));
        return usageData;
    }

    public List<String> getFirstAccessTime(String providerName, int limit)
            throws AppUsageQueryServiceClientException {

        OMElement omElement = this.queryFirstAccess(
                APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY, null);
        Collection<AppFirstAccess> usageData = getFirstAccessTime(omElement);
        List<String> APIFirstAccessList = new ArrayList<String>();

        for (AppFirstAccess usage : usageData) {
            APIFirstAccessList.add(usage.getYear());
            APIFirstAccessList.add(usage.getMonth());
            APIFirstAccessList.add(usage.getDay());
        }
        return APIFirstAccessList;
    }

    private OMElement queryFirstAccess(String columnFamily,
                                       QueryServiceStub.CompositeIndex[] compositeIndex)
            throws AppUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new AppUsageQueryServiceClientException(errorMsg);
        }

        String selectRowsByColumnName = null;
        String selectRowsByColumnValue = null;
        if (compositeIndex != null) {
            selectRowsByColumnName = compositeIndex[0].getIndexName();
            selectRowsByColumnValue = compositeIndex[0].getRangeFirst();
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query;
            if (connection != null && connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("oracle")) {
                if (selectRowsByColumnName != null) {
                    //select time,year,month,day from API_REQUEST_SUMMARY order by time ASC limit 1
                    query = String.format("SELECT time,year,month,day FROM  %s WHERE %s = ? " +
                            " AND ROWNUM <= 1 order by time ASC",columnFamily, selectRowsByColumnName);
                    statement = connection.prepareStatement(query);
                    statement.setString(1, selectRowsByColumnValue);
                } else {
                    query = String.format("SELECT time,year,month,day FROM  %s WHERE ROWNUM <= 1 order by time ASC", columnFamily);
                    statement = connection.prepareStatement(query);
                }

            } else {
                if (selectRowsByColumnName != null) {
                    //select time,year,month,day from API_REQUEST_SUMMARY order by time ASC limit 1
                    query = String.format("SELECT time,year,month,day FROM  %s WHERE %s " +
                            "= ? order by time ASC limit 1", columnFamily, selectRowsByColumnName);
                    statement = connection.prepareStatement(query);
                    statement.setString(1, selectRowsByColumnValue);
                } else {
                    query = String.format("SELECT time,year,month,day FROM  %s order by time ASC limit 1", columnFamily);
                    statement = connection.prepareStatement(query);

                }
            }
            rs = statement.executeQuery();
            StringBuilder returnStringBuilder = new StringBuilder("<omElement><rows>");
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                returnStringBuilder.append("<row>");
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String columnValue = rs.getString(columnName);
                    String xmlEscapedValue = StringEscapeUtils.escapeXml(columnValue);
                    returnStringBuilder.append("<" + columnName.toLowerCase() + ">" + xmlEscapedValue +
                            "</" + columnName.toLowerCase() + ">");
                }
                returnStringBuilder.append("</row>");
            }
            returnStringBuilder.append("</rows></omElement>");
            String returnString = returnStringBuilder.toString();
            return AXIOMUtil.stringToOM(returnString);

        } catch (Exception e) {
            throw new AppUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {

                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    private Collection<AppUsage> getUsageData(OMElement data) {
        List<AppUsage> usageData = new ArrayList<AppUsage>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                usageData.add(new AppUsage(rowElement));
            }
        }
        return usageData;
    }

    private String addRangeCondition(String rangeField, String preFix, String fromDate, String toDate) {
        String query = preFix + " " + rangeField + " BETWEEN '" + fromDate + "' AND '" + toDate + "'";
        return query;
    }

    private List<AppUsageDTO> getAPIUsageTopEntries(List<AppUsageDTO> usageData, int limit) {
        Collections.sort(usageData, new Comparator<AppUsageDTO>() {
            public int compare(AppUsageDTO o1, AppUsageDTO o2) {
                // Note that o2 appears before o1
                // This is because we need to sort in the descending order
                return (int) (o2.getCount() - o1.getCount());
            }
        });
        if (usageData.size() > limit) {
            AppUsageDTO other = new AppUsageDTO();
            other.setApiName("[Other]");
            for (int i = limit; i < usageData.size(); i++) {
                other.setCount(other.getCount() + usageData.get(i).getCount());
            }
            while (usageData.size() > limit) {
                usageData.remove(limit);
            }
            usageData.add(other);
        }

        return usageData;
    }

    private List<WebApp> getAPIsByProvider(String providerId, String tenantDomain)
            throws AppUsageQueryServiceClientException {
        try {
            if (APIUsageStatisticsClientConstants.ALL_PROVIDERS.equals(providerId)) {
                return apiProviderImpl.getAllWebApps(tenantDomain);
            } else {
                return apiProviderImpl.getAPIsByProvider(providerId);
            }
        } catch (AppManagementException e) {
            throw new AppUsageQueryServiceClientException("Error while retrieving APIs by " + providerId, e);
        }
    }

    private Collection<AppUsageByResourcePath> getUsageDataByResourcePath(OMElement data) {
        List<AppUsageByResourcePath> usageData = new ArrayList<AppUsageByResourcePath>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                usageData.add(new AppUsageByResourcePath(rowElement));
            }
        }
        return usageData;
    }

    private Collection<AppUsageByPage> getUsageDataByPage(OMElement data) {
        List<AppUsageByPage> usageData = new ArrayList<AppUsageByPage>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                usageData.add(new AppUsageByPage(rowElement));
            }
        }
        return usageData;
    }

    private Collection<AppUsageByUserName> getUsageDataByAPIName(OMElement data) {
        List<AppUsageByUserName> usageData = new ArrayList<AppUsageByUserName>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                usageData.add(new AppUsageByUserName(rowElement));
            }
        }
        return usageData;
    }
    //cacheHit

    private Collection<AppMCacheHitCount> getCacheHitCount(OMElement data) {
        List<AppMCacheHitCount> usageData = new ArrayList<AppMCacheHitCount>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                usageData.add(new AppMCacheHitCount(rowElement));
            }
        }
        return usageData;

    }


    private Collection<AppResponseFaultCount> getAPIResponseFaultCount(OMElement data) {
        List<AppResponseFaultCount> faultyData = new ArrayList<AppResponseFaultCount>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                faultyData.add(new AppResponseFaultCount(rowElement));
            }
        }
        return faultyData;
    }


    private Collection<AppResponseTime> getResponseTimeData(OMElement data) {
        List<AppResponseTime> responseTimeData = new ArrayList<AppResponseTime>();

        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));

        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                if (rowElement.getFirstChildWithName(new QName(
                        APIUsageStatisticsClientConstants.SERVICE_TIME)) != null) {
                    responseTimeData.add(new AppResponseTime(rowElement));
                }
            }
        }
        return responseTimeData;
    }

    private Collection<AppAccessTime> getAccessTimeData(OMElement data) {
        List<AppAccessTime> accessTimeData = new ArrayList<AppAccessTime>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                accessTimeData.add(new AppAccessTime(rowElement));
            }
        }
        return accessTimeData;
    }

    private Collection<AppUsageByUser> getUsageBySubscriber(OMElement data) {
        List<AppUsageByUser> usageData = new ArrayList<AppUsageByUser>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                usageData.add(new AppUsageByUser(rowElement));
            }
        }
        return usageData;
    }

    private Collection<AppVersionUsageByUser> getUsageAPIBySubscriber(OMElement data) {
        List<AppVersionUsageByUser> usageData = new ArrayList<AppVersionUsageByUser>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                for (int i = 0; i < usageData.size(); i++) {
                    if (usageData.get(i).getApiName().equals(rowElement.getFirstChildWithName(new QName(
                            APIUsageStatisticsClientConstants.API))
                            .getText()) && usageData.get(i).getApiVersion()
                            .equals(
                                    rowElement.getFirstChildWithName(new QName(
                                            APIUsageStatisticsClientConstants.VERSION)).getText())) {
                        usageData.get(i).setRequestCount(usageData.get(i).getRequestCount() + Long.parseLong(
                                rowElement.getFirstChildWithName(new QName(
                                        APIUsageStatisticsClientConstants.REQUEST)).getText()));
                        //    return usageData;
                    }

                }
                usageData.add(new AppVersionUsageByUser(rowElement));
            }
        }
        return usageData;
    }

    private Collection<AppVersionUsageByUserMonth> getUsageAPIBySubscriberMonthly(OMElement data) {
        List<AppVersionUsageByUserMonth> usageData = new ArrayList<AppVersionUsageByUserMonth>();
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        if (rowIterator != null) {
            while (rowIterator.hasNext()) {
                OMElement rowElement = (OMElement) rowIterator.next();
                for (int i = 0; i < usageData.size(); i++) {
                    if (usageData.get(i).getApiName().equals(rowElement.getFirstChildWithName(new QName(
                            APIUsageStatisticsClientConstants.API)).getText()) && usageData.get(i).getApiVersion()
                            .equals(rowElement.getFirstChildWithName(
                                    new QName(
                                            APIUsageStatisticsClientConstants.VERSION)).getText())) {
                        usageData.get(i).setRequestCount(usageData.get(i).getRequestCount() + (long) Double.parseDouble(
                                rowElement.getFirstChildWithName(new QName(
                                        APIUsageStatisticsClientConstants.REQUEST)).getText()));
                        return usageData;
                    }

                }
                usageData.add(new AppVersionUsageByUserMonth(rowElement));
            }
        }
        return usageData;
    }


    private List<AppResponseTimeDTO> getResponseTimeTopEntries(List<AppResponseTimeDTO> usageData,
                                                               int limit) {
        Collections.sort(usageData, new Comparator<AppResponseTimeDTO>() {
            public int compare(AppResponseTimeDTO o1, AppResponseTimeDTO o2) {
                // Note that o2 appears before o1
                // This is because we need to sort in the descending order
                return (int) (o2.getServiceTime() - o1.getServiceTime());
            }
        });
        if (usageData.size() > limit) {
            while (usageData.size() > limit) {
                usageData.remove(limit);
            }
        }
        return usageData;
    }

    private List<AppVersionLastAccessTimeDTO> getLastAccessTimeTopEntries(
            List<AppVersionLastAccessTimeDTO> usageData, int limit) {
        Collections.sort(usageData, new Comparator<AppVersionLastAccessTimeDTO>() {
            public int compare(AppVersionLastAccessTimeDTO o1, AppVersionLastAccessTimeDTO o2) {
                // Note that o2 appears before o1
                // This is because we need to sort in the descending order
                return o2.getLastAccessTime().compareToIgnoreCase(o1.getLastAccessTime());
            }
        });
        if (usageData.size() > limit) {
            while (usageData.size() > limit) {
                usageData.remove(limit);
            }
        }
        return usageData;
    }

    private String getNextStringInLexicalOrder(String str) {
        if ((str == null) || (str.equals(""))) {
            return str;
        }
        byte[] bytes = str.getBytes();
        byte last = bytes[bytes.length - 1];
        last = (byte) (last + 1);        // Not very accurate. Need to improve this more to handle overflows.
        bytes[bytes.length - 1] = last;
        return new String(bytes);
    }

    private OMElement queryDatabase(String columnFamily,
                                    QueryServiceStub.CompositeIndex[] compositeIndex)
            throws AppUsageQueryServiceClientException {

        if (dataSource == null) {
            throw new AppUsageQueryServiceClientException(errorMsg);
        }

        String selectRowsByColumnName = null;
        String selectRowsByColumnValue = null;
        if (compositeIndex != null) {
            selectRowsByColumnName = compositeIndex[0].getIndexName();
            selectRowsByColumnValue = compositeIndex[0].getRangeFirst();
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            String query;
            StringBuilder returnStringBuilder = new StringBuilder("<omElement><rows>");
            //check whether table exist first
            if (isTableExist(columnFamily, connection)) {//Table Exist
                if (selectRowsByColumnName != null) {
                    query = "SELECT * FROM  " + columnFamily + " WHERE " + selectRowsByColumnName +
                            " = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, selectRowsByColumnValue);
                } else {
                    query = "SELECT * FROM  " + columnFamily;
                    statement = connection.prepareStatement(query);
                }
                rs = statement.executeQuery(query);
                int columnCount = rs.getMetaData().getColumnCount();

                while (rs.next()) {
                    returnStringBuilder.append("<row>");
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = rs.getMetaData().getColumnName(i);
                        String columnValue = rs.getString(columnName);
                        String xmlEscapedValue = StringEscapeUtils.escapeXml(columnValue);
                        returnStringBuilder.append("<" + columnName.toLowerCase() + ">" + xmlEscapedValue +
                                "</" + columnName.toLowerCase() + ">");
                    }
                    returnStringBuilder.append("</row>");
                }
            }
            returnStringBuilder.append("</rows></omElement>");
            String returnString = returnStringBuilder.toString();
            return AXIOMUtil.stringToOM(returnString);

        } catch (Exception e) {
            throw new AppUsageQueryServiceClientException("Error occurred while querying from JDBC database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {

                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    private List<PerUserAPIUsageDTO> getTopEntries(List<PerUserAPIUsageDTO> usageData, int limit) {
        Collections.sort(usageData, new Comparator<PerUserAPIUsageDTO>() {
            public int compare(PerUserAPIUsageDTO o1, PerUserAPIUsageDTO o2) {
                // Note that o2 appears before o1
                // This is because we need to sort in the descending order
                return (int) (o2.getCount() - o1.getCount());
            }
        });
        if (usageData.size() > limit) {
            PerUserAPIUsageDTO other = new PerUserAPIUsageDTO();
            other.setUsername("[Other]");
            for (int i = limit; i < usageData.size(); i++) {
                other.setCount(other.getCount() + usageData.get(i).getCount());
            }
            while (usageData.size() > limit) {
                usageData.remove(limit);
            }
            usageData.add(other);
        }

        return usageData;
    }

    private boolean isTableExist(String tableName, Connection connection) throws SQLException {
        //This return all tables,use this because it is not db specific, Passing table name doesn't
        //work with every database
        ResultSet tables = null;
        try {
            tables = connection.getMetaData().getTables(null, null, "%", null);
            while (tables.next()) {
                if (tables.getString(3).equalsIgnoreCase(tableName)) {
                    return true;
                }
            }
        } finally {
            APIMgtDBUtil.closeAllConnections(null, null, tables);
        }
        return false;
    }

    private static OMElement buildOMElement(InputStream inputStream) throws Exception {
        XMLStreamReader parser;
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        } catch (XMLStreamException e) {
            String msg = "Error in initializing the parser to build the OMElement.";
            throw new Exception(msg, e);
        } finally {
        }
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        return builder.getDocumentElement();
    }

    private Map<String, Object> evaluate(String param, int calls) throws Exception {
        return paymentPlan.evaluate(param, calls);
    }
}