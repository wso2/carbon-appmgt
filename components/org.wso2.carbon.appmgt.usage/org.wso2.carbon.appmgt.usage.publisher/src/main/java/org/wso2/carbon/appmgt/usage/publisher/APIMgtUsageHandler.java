/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.usage.publisher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URL;

import javax.cache.Cache;
import javax.cache.Caching;

import org.apache.axis2.Constants;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.certificatevalidation.cache.CacheManager;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.dto.WebAppInfoDTO;
import org.wso2.carbon.appmgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.appmgt.usage.publisher.internal.APPManagerConfigurationServiceComponent;
import org.wso2.carbon.appmgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.usage.agent.beans.APIManagerRequestStats;
import org.wso2.carbon.usage.agent.util.PublisherUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class APIMgtUsageHandler extends AbstractHandler {

    private static final Log log   = LogFactory.getLog(APIMgtUsageHandler.class);

    private volatile APIMgtUsageDataPublisher publisher;

    private boolean enabled = APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().isEnabled();

    private String publisherClass = APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getPublisherClass();

    public boolean handleRequest(MessageContext mc) {

        try{
            long currentTime = System.currentTimeMillis();

            if (!enabled) {
                return true;
            }

            if (publisher == null) {
                synchronized (this){
                    if (publisher == null) {
                        try {
                            log.debug("Instantiating Data Publisher");
                            publisher = (APIMgtUsageDataPublisher)Class.forName(publisherClass).newInstance();
                            publisher.init();
                        } catch (ClassNotFoundException e) {
                            log.error("Class not found " + publisherClass);
                        } catch (InstantiationException e) {
                            log.error("Error instantiating " + publisherClass);
                        } catch (IllegalAccessException e) {
                            log.error("Illegal access to " + publisherClass);
                        }
                    }
                }
            }

            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) mc).
                    getAxis2MessageContext();

            Map<String, String> headers = (Map<String, String>) axis2MC.getProperty(
                    org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            String referer = headers.get("Referer");
            String contextAndVersion[] = getContextWithVersion(referer);
            String context = "/" + contextAndVersion[0];
            String trackingCode = headers.get("trackingCode");
            WebAppInfoDTO webAppInfoDTO = AppMDAO.getWebAppByTrackingCode(trackingCode);
            String version = webAppInfoDTO.getVersion();
            String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            if (context.contains("/t/")) {
            	tenantDomain = contextAndVersion[1];
            }

            String usageCacheKey = tenantDomain + ":" + context;
            if (version != null) {
                usageCacheKey = usageCacheKey + ":" + version;
            }

            WebApp webApp = null;
            if (getUsageCache().get(usageCacheKey) != null) {
                webApp = (WebApp) getUsageCache().get(usageCacheKey);
            } else {
                if (version != null) {
                    webApp = getWebApp(context, version);
                } else {
                    webApp = getNonVersionedWebApp(context, tenantDomain);
                }
                getUsageCache().put(usageCacheKey, webApp);
            }

            String appName = webApp.getId().getApiName();
            version = webApp.getId().getVersion();
            String appNameWithVersion = appName + ":" + version;

            String hashcode = webApp.getTrackingCode();

            APIIdentifier webAppIdentifier = webApp.getId();
            String cacheKey = webAppIdentifier.getProviderName() + ":" + webAppIdentifier.getApiName() +
                              ":" + webAppIdentifier.getVersion() + ":" + context;

            boolean usagePublishingEnabledForApp;

            if (getUsageConfigCache().get(cacheKey) != null) {
                usagePublishingEnabledForApp = (Boolean) getUsageConfigCache().get(cacheKey);
            } else {
                usagePublishingEnabledForApp = AppMDAO.isUsagePublishingEnabledForApp(webApp);
                getUsageConfigCache().put(cacheKey, usagePublishingEnabledForApp);
            }

            /* Do not publish stats if API level usage tracking is disabled */
            if (!usagePublishingEnabledForApp) {
                return true;
            }
            
            String cookieString = headers.get(HTTPConstants.COOKIE_STRING);
            String saml2CookieValue = getCookieValue(cookieString, AppMConstants.APPM_SAML2_COOKIE);
            boolean isTenantFlowStarted = false;
            String loggedUser = null;
            try {            	
                if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }
                loggedUser = (String) Caching.getCacheManager(AppMConstants.API_MANAGER_CACHE_MANAGER).
            			getCache(AppMConstants.KEY_CACHE_NAME).get(saml2CookieValue);
            } finally {
            	if (isTenantFlowStarted) {
            		PrivilegedCarbonContext.endTenantFlow();
            	}
            }            
           
            URL appURL = new URL(referer);
            String page= appURL.getPath();                  

            String username = "";
            String applicationName = "DefaultApplication";
            String applicationId = "1";
            username = loggedUser;
            String hostName = DataPublisherUtil.getHostAddress();
            if (username == null) {
                username = APIMgtUsagePublisherConstants.ANONYMOUS_USER;
            }

            boolean trackingCodeExist = false;
            String tracking_code = headers.get("trackingCode");    
            if (tracking_code != null) {
            	String[] tracking_code_list = tracking_code.split(",");
            	trackingCodeExist = Arrays.asList(tracking_code_list).contains(hashcode);
            }
            
            String resource = extractResource(mc);
            String method =  (String)((Axis2MessageContext) mc).getAxis2MessageContext().getProperty(
                    Constants.Configuration.HTTP_METHOD);
            
            int tenantId = UsageComponent.getRealmService().getTenantManager().
                    getTenantId(tenantDomain);

            List<Long[]> timeList = UsageComponent.getResponseTime(page);

            long totalTime = 0L;
            long serviceTime = 0L;

            if(timeList != null){
                for (int x = 0; x < timeList.size(); x++) {
                    Long [] timaArray =   timeList.get(x);
                    totalTime = totalTime + timaArray[0];

                }

                serviceTime = totalTime/timeList.size();

            }

            UsageComponent.deleteResponseTime(page,System.currentTimeMillis());

            if (trackingCodeExist)  {

                RequestPublisherDTO requestPublisherDTO = new RequestPublisherDTO();
                requestPublisherDTO.setContext(context);
                requestPublisherDTO.setApi_version(appNameWithVersion);
                requestPublisherDTO.setApi(appName);
                requestPublisherDTO.setVersion(version);
                requestPublisherDTO.setResource(resource);
                requestPublisherDTO.setMethod(method);
                requestPublisherDTO.setRequestTime(currentTime);
                requestPublisherDTO.setUsername(username);
                requestPublisherDTO.setTenantDomain(tenantDomain);
                requestPublisherDTO.setHostName(hostName);
                requestPublisherDTO.setApiPublisher(tenantDomain);
                requestPublisherDTO.setApplicationName(applicationName);
                requestPublisherDTO.setApplicationId(applicationId);
                requestPublisherDTO.setTrackingCode(hashcode);
                requestPublisherDTO.setReferer(referer);
                requestPublisherDTO.setServiceTimeOfPage(serviceTime);

                publisher.publishEvent(requestPublisherDTO);
                //We check if usage metering is enabled for billing purpose
                if (DataPublisherUtil.isEnabledMetering()) {
                    //If usage metering enabled create new usage stat object and publish to bam
                    APIManagerRequestStats stats = new APIManagerRequestStats();
                    stats.setRequestCount(1);
                    stats.setTenantId(tenantId);
                    try {
                        //Publish stat to bam
                        PublisherUtils.publish(stats, tenantId);
                    } catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.debug(e);
                        }
                        log.error("Error occurred while publishing request statistics. Full stacktrace available in debug logs. " + e.getMessage());
                    }
                }

                mc.setProperty(APIMgtUsagePublisherConstants.USER_ID, username);
                mc.setProperty(APIMgtUsagePublisherConstants.CONTEXT, context);
                mc.setProperty(APIMgtUsagePublisherConstants.APP_VERSION, appNameWithVersion);
                mc.setProperty(APIMgtUsagePublisherConstants.API, appName);
                mc.setProperty(APIMgtUsagePublisherConstants.VERSION, version);
                mc.setProperty(APIMgtUsagePublisherConstants.RESOURCE, resource);
                mc.setProperty(APIMgtUsagePublisherConstants.HTTP_METHOD, method);
                mc.setProperty(APIMgtUsagePublisherConstants.REQUEST_TIME, currentTime);
                mc.setProperty(APIMgtUsagePublisherConstants.HOST_NAME,hostName);
                mc.setProperty(APIMgtUsagePublisherConstants.API_PUBLISHER,tenantDomain);
                mc.setProperty(APIMgtUsagePublisherConstants.APPLICATION_NAME, applicationName);
                mc.setProperty(APIMgtUsagePublisherConstants.APPLICATION_ID, applicationId);
                mc.setProperty(APIMgtUsagePublisherConstants.TRACKING_CODE,hashcode);
                mc.setProperty(APIMgtUsagePublisherConstants.REFERER,referer);
                mc.setProperty(APIMgtUsagePublisherConstants.SERVICE_TIME_OF_PAGE,serviceTime);

        }

        }catch (Throwable e){
            log.error("Cannot publish event. " + e.getMessage(), e);
        }
        return true;
    }

    public boolean handleResponse(MessageContext mc) {

        return true; // Should never stop the message flow
    }

    private String extractResource(MessageContext mc){
        String resource = "/";
        Pattern pattern = Pattern.compile("^/.+?/.+?([/?].+)$");
        Matcher matcher = pattern.matcher((String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH));
        if (matcher.find()){
            resource = matcher.group(1);
        }
        return resource;
    }

    public String getCookieValue(String cookieString, String cookieName) {
        if (cookieString != null && cookieString.length() > 0) {
            int cStart = cookieString.indexOf(cookieName + "=");
            int cEnd;
            if (cStart != -1) {
                cStart = cStart + cookieName.length() + 1;
                cEnd = cookieString.indexOf(";", cStart);
                if (cEnd == -1) {
                    cEnd = cookieString.length();
                }
                return cookieString.substring(cStart, cEnd);
            }
        }
        return "";
    }


    public String[] getContextWithVersion(String refer) {
        String webapp[]= new String[3];
        if (refer != null && refer.length() > 0) {
           
        	if (refer.contains("/t/")) {
        		// e.g URL pattern : "http://localhost:8281/t/lakmali.com/united-airline/";
        		String s[] = refer.split("/t/");
        		if (s.length >= 2) {
        			String stringAfterSlashT = s[1];
        			String result[] = stringAfterSlashT.split("/");
        			if (result.length >= 2) {
        				webapp[0] = "t/" + result[0] + "/" + result[1];
        				webapp[1] = result[0];
        			}
        		}
        		
        	} else {
        		// e.g URL pattern : "http://localhost:8281/united-airline/";
                String s[]=refer.split("/");
                if (s.length >= 4) {
	                webapp[0] = s[3];
                }
        	}
             return webapp;
        }
        return webapp;
    }


    public WebApp getWebApp(String context,String version) throws AppManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        WebApp webApp =null;

        String sqlQuery = "SELECT APP_NAME,APP_PROVIDER,TRACKING_CODE FROM APM_APP WHERE CONTEXT=? AND APP_VERSION=?";

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1,context);
            ps.setString(2,version);
            rs = ps.executeQuery();
            while (rs.next()) {
                String webAppname = rs.getString("APP_NAME");
                String provider = rs.getString("APP_PROVIDER");
                String trackingCode = rs.getString("TRACKING_CODE");
                APIIdentifier apiIdentifier = new APIIdentifier(provider,webAppname,version);
                webApp = new WebApp(apiIdentifier);
                webApp.setTrackingCode(trackingCode);
                webApp.setContext(context);

            }

        } catch (SQLException e) {
            log.error("Error when executing the SQL query to read the access key for user :", e);
            throw new AppManagementException("Error when executing the SQL query to read the access key for user :", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
          return webApp;
    }

    /**
     * Get WebApp for default version web apps.
     * @param context
     * @param tenantDomain
     * @return
     * @throws AppManagementException
     */
    public WebApp getNonVersionedWebApp(String context, String tenantDomain) throws AppManagementException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        WebApp webApp = null;

        String sqlQuery = "SELECT APM_APP.APP_NAME, APM_APP.APP_PROVIDER, APM_APP.TRACKING_CODE, " +
                "APM_APP_DEFAULT_VERSION.PUBLISHED_DEFAULT_APP_VERSION FROM APM_APP LEFT JOIN " +
                "APM_APP_DEFAULT_VERSION ON APM_APP_DEFAULT_VERSION.APP_NAME=APM_APP.APP_NAME AND " +
                "APM_APP_DEFAULT_VERSION.APP_PROVIDER=APM_APP.APP_PROVIDER WHERE APM_APP.CONTEXT=? AND APM_APP" +
                ".TENANT_ID=?";
        try {
            connection = APIMgtDBUtil.getConnection();
            preparedStatement = connection.prepareStatement(sqlQuery);
            int tenantId = UsageComponent.getRealmService().getTenantManager().getTenantId(tenantDomain);
            preparedStatement.setString(1,context);
            preparedStatement.setInt(2,tenantId);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                String webAppName = resultSet.getString(AppMConstants.FIELD_API_NAME);
                String provider = resultSet.getString(AppMConstants.FIELD_API_PUBLISHER );
                String trackingCode = resultSet.getString(AppMConstants.FIELD_TRACKING_CODE);
                String version = resultSet.getString(AppMConstants.FIELD_PUBLISHED_DEFAULT_APP_VERSION);
                APIIdentifier apiIdentifier = new APIIdentifier(provider,webAppName,version);
                webApp = new WebApp(apiIdentifier);
                webApp.setTrackingCode(trackingCode);
                webApp.setContext(context);
            }
        } catch (SQLException e) {
            String errorMessage = "Error occurred while reading default versioned web app. Context : " + context +
                    " tenant domain: " + tenantDomain;
            throw new AppManagementException(errorMessage, e);
        } catch (UserStoreException e) {
            String errorMessage = "Error occurred while getting tenant Id for tenant domain: " + tenantDomain;
            throw new AppManagementException(errorMessage, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return webApp;
    }

    private Cache getUsageConfigCache() {
            return Caching.getCacheManager(AppMConstants.USAGE_CONFIG_CACHE_MANAGER)
                    .getCache(AppMConstants.USAGE_CONFIG_CACHE);
    }

    private Cache getUsageCache() {
        return Caching.getCacheManager(AppMConstants.USAGE_CACHE_MANAGER)
                .getCache(AppMConstants.USAGE_CACHE);
    }

   }
