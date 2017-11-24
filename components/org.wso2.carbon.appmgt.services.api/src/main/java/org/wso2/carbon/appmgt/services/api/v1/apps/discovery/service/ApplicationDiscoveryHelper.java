/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.services.api.v1.apps.discovery.service;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.impl.discovery.ApplicationDiscoveryContext;
import org.wso2.carbon.appmgt.impl.discovery.ApplicationDiscoveryHandler;
import org.wso2.carbon.appmgt.impl.discovery.ApplicationDiscoveryServiceFactory;
import org.wso2.carbon.appmgt.impl.discovery.DiscoverySearchCriteria;
import org.wso2.carbon.appmgt.impl.discovery.UserNamePasswordCredentials;
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationDTO;
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationListDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

/**
 * Discovery client which handles application discovery
 */
public class ApplicationDiscoveryHelper {

    private static final Log log = LogFactory.getLog(ApplicationDiscoveryHelper.class);

    private static final String NAME_WSO2_AS = "WSO2-AS";
    private static final String CONTEXT_DATA_APP_SERVER_URL = "appServerUrl";
    private static final String CONTEXT_DATA_APP_SERVER_USER_NAME = "userName";

    /**
     * Returns the full application information so that an application can be created with the
     * information.
     *
     * @return
     */
    public DiscoveryResult<DiscoveredApplicationDTO> getDiscoveredApplication(
            final HttpServletRequest servletRequest, final HttpServletResponse servletResponse,
            String tenantDomain, DiscoveryInfoRequest discoverInfoRequest) {

        DiscoveryResult<DiscoveredApplicationDTO> result = new DiscoveryResult<DiscoveredApplicationDTO>();

        ApplicationDiscoveryServiceFactory applicationDiscoveryServiceFactory = (ApplicationDiscoveryServiceFactory) PrivilegedCarbonContext
                .getThreadLocalCarbonContext()
                .getOSGiService(ApplicationDiscoveryServiceFactory.class);
        ApplicationDiscoveryHandler handler = applicationDiscoveryServiceFactory
                .getHandler(NAME_WSO2_AS);

        APIIdentifier apiIdentifier = new APIIdentifier(null, null, null);
        apiIdentifier.setApplicationId(discoverInfoRequest.getApplicationId());

        ApplicationDiscoveryContext applicationDiscoveryContext = (ApplicationDiscoveryContext) servletRequest
                .getSession().getAttribute(ApplicationDiscoveryContext.class.getName());
        if (applicationDiscoveryContext == null) {
            applicationDiscoveryContext = new ApplicationDiscoveryContext();
            servletRequest.getSession().setAttribute(ApplicationDiscoveryContext.class.getName(),
                    applicationDiscoveryContext);
        }

        ConfigurationContext configurationContext = getClientConfigurationContext();

        try {

            result.setData(handler.readApplicationInfo(applicationDiscoveryContext, apiIdentifier,
                    configurationContext));
            result.setStatus(Response.Status.OK.getStatusCode());
        } catch (AppManagementException e) {
            String message = String
                    .format("Error while reading the application info from the backend server Reason: %s ",
                            e.getMessage());
            log.debug(message, e);
            result.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            result.setDescription(message);
        }

        return result;
    }

    /**
     * Returns the list of applications, number of pages.
     * This will return only the minimal required fields for listing to conserve memory and bandwidth.
     *
     * sample request
     *  {"credentials" : {"userName" : "admin", "appServerUrl" : "local", "password" : "admin" ,
     *  "loggedInUsername" : "admin"},
     *  "searchCriteria" : {"applicationName" : "", "status" : "New", "pageNumber" : 4 }
     *  }
     *
     * @return
     */
    public DiscoveryResult<DiscoveredApplicationListDTO> getDiscoveredApplications(
            final HttpServletRequest servletRequest, final HttpServletResponse servletResponse,
            String tenantDomain, DiscoverRequest discoverRequest) {

        DiscoveryResult result = new DiscoveryResult<DiscoveredApplicationListDTO>();
        ApplicationDiscoveryServiceFactory applicationDiscoveryServiceFactory = (ApplicationDiscoveryServiceFactory) PrivilegedCarbonContext
                .getThreadLocalCarbonContext()
                .getOSGiService(ApplicationDiscoveryServiceFactory.class);
        ApplicationDiscoveryHandler handler = applicationDiscoveryServiceFactory
                .getHandler(NAME_WSO2_AS);

        UserNamePasswordCredentials userNamePasswordCredentials = discoverRequest.getCredentials();
        if (userNamePasswordCredentials == null) {
            log.error("getDiscoveredApplications called without credentials");
            result.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            result.setDescription("getDiscoveredApplications called without credentials");
            return result;
        }

        DiscoverySearchCriteria discoverySearchCriteria = discoverRequest.getSearchCriteria();
        if (discoverySearchCriteria == null) {
            discoverySearchCriteria = new DiscoverySearchCriteria();
        }

        ApplicationDiscoveryContext applicationDiscoveryContext = (ApplicationDiscoveryContext) servletRequest
                .getSession().getAttribute(ApplicationDiscoveryContext.class.getName());
        if (applicationDiscoveryContext == null ||
                !(userNamePasswordCredentials.getAppServerUrl()
                        .equals(applicationDiscoveryContext.getData(CONTEXT_DATA_APP_SERVER_URL)))
                ||
                !(userNamePasswordCredentials.getUserName().equals(applicationDiscoveryContext
                        .getData(CONTEXT_DATA_APP_SERVER_USER_NAME)))) {
            applicationDiscoveryContext = new ApplicationDiscoveryContext();
            servletRequest.getSession().setAttribute(ApplicationDiscoveryContext.class.getName(),
                    applicationDiscoveryContext);

            applicationDiscoveryContext.putData(CONTEXT_DATA_APP_SERVER_URL,
                    userNamePasswordCredentials.getAppServerUrl());
            applicationDiscoveryContext.putData(CONTEXT_DATA_APP_SERVER_USER_NAME,
                    userNamePasswordCredentials.getUserName());
        }

        ConfigurationContext configurationContext = getClientConfigurationContext();

        try {
            DiscoveredApplicationListDTO applicationListDTO = handler
                    .discoverApplications(applicationDiscoveryContext, userNamePasswordCredentials,
                            discoverySearchCriteria, Locale.ENGLISH, configurationContext);
            result.setStatus(Response.Status.OK.getStatusCode());
            result.setData(applicationListDTO);
        } catch (AppManagementException e) {
            String message = String
                    .format("Error while discovering the application from the backend server Server[%s], User[%s], Reason: %s",
                            userNamePasswordCredentials.getAppServerUrl(),
                            userNamePasswordCredentials.getUserName(), e.getMessage());
            log.debug(message, e);
            result.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            result.setDescription(message);
        }

        return result;

    }

    /**
     * Returns the list of supported application servers types which is supported by the
     * system and which is configured per the tenant.
     *
     * @return
     */
    public List<String> getSupportedApplicationServerTypes(final HttpServletRequest servletRequest,
            String tenantDomain) {

        ApplicationDiscoveryServiceFactory applicationDiscoveryServiceFactory = (ApplicationDiscoveryServiceFactory) PrivilegedCarbonContext
                .getThreadLocalCarbonContext()
                .getOSGiService(ApplicationDiscoveryServiceFactory.class);

        return applicationDiscoveryServiceFactory.getAvailableHandlerNames();
    }

    /**
     * Returns the client configuration context
     * @return
     */
    private ConfigurationContext getClientConfigurationContext() {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                .getThreadLocalCarbonContext();
        ConfigurationContextService configurationContextService = (ConfigurationContextService) carbonContext
                .getOSGiService(ConfigurationContextService.class);
        return configurationContextService.getClientConfigContext();
    }
}
