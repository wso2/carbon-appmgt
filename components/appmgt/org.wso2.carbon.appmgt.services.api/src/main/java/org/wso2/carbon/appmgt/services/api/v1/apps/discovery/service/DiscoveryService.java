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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.impl.discovery.*;
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationDTO;
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationListDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * The Discovery service.
 * The discovery api will be interfaced with REST model web services.
 */
@Produces({ "application/json" })
@Consumes({ "application/json" })
@Path("discovery")
public class DiscoveryService {

    private static final Log log = LogFactory.getLog(DiscoveryService.class);

    private static final String SUPER_TENANT_DOMAIN = "carbon.super";
    private static final String NAME_WSO2_AS = "WSO2-AS";
    private static final String CONTEXT_DATA_APP_SERVER_URL = "appServerUrl";
    private static final String CONTEXT_DATA_APP_SERVER_USER_NAME = "userName";

    /**
     * Returns the full application information so that an application can be created with the
     * information.
     *
     * @return
     */
    @POST
    @Path("app/info/{tenantDomain}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DiscoveredApplicationDTO getDiscoveredApplication(
            @Context final HttpServletRequest servletRequest,
            @Context final HttpServletResponse servletResponse, @Context HttpHeaders headers,
            @PathParam("tenantDomain") String tenantDomain,
            DiscoveryInfoRequest discoverInfoRequest) {

        DiscoveredApplicationDTO result = null;

        ApplicationDiscoveryServiceFactory applicationDiscoveryServiceFactory = (ApplicationDiscoveryServiceFactory) PrivilegedCarbonContext
                .getThreadLocalCarbonContext()
                .getOSGiService(ApplicationDiscoveryServiceFactory.class);
        ApplicationDiscoveryHandler handler = applicationDiscoveryServiceFactory
                .getHandler(NAME_WSO2_AS);

        APIIdentifier apiIdentifier = new APIIdentifier(null, null, null);
        apiIdentifier.setApplicationId(discoverInfoRequest.getApplicationId());

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                .getThreadLocalCarbonContext();
        ApplicationDiscoveryContext applicationDiscoveryContext = (ApplicationDiscoveryContext) servletRequest
                .getSession().getAttribute(ApplicationDiscoveryContext.class.getName());
        if (applicationDiscoveryContext == null) {
            applicationDiscoveryContext = new ApplicationDiscoveryContext();
            servletRequest.getSession().setAttribute(ApplicationDiscoveryContext.class.getName(),
                    applicationDiscoveryContext);
        }
        try {
            result = handler
                    .readApplicationInfo(applicationDiscoveryContext, apiIdentifier, carbonContext);
        } catch (AppManagementException e) {
            String message = "Error while discovering the application from the backend server Server[%s], User[%s], Reason: ";
            log.error(message);
            log.debug(message, e);
            safeSendError(servletResponse, Response.Status.INTERNAL_SERVER_ERROR);
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
    @POST
    @Path("app/list/{tenantDomain}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DiscoveredApplicationListDTO getDiscoveredApplications(
            @Context final HttpServletRequest servletRequest,
            @Context final HttpServletRequest request,
            @Context final HttpServletResponse servletResponse, @Context HttpHeaders headers,
            @PathParam("tenantDomain") String tenantDomain, DiscoverRequest discoverRequest) {

        ApplicationDiscoveryServiceFactory applicationDiscoveryServiceFactory = (ApplicationDiscoveryServiceFactory) PrivilegedCarbonContext
                .getThreadLocalCarbonContext()
                .getOSGiService(ApplicationDiscoveryServiceFactory.class);
        ApplicationDiscoveryHandler handler = applicationDiscoveryServiceFactory
                .getHandler(NAME_WSO2_AS);

        UserNamePasswordCredentials userNamePasswordCredentials = discoverRequest.getCredentials();
        if (userNamePasswordCredentials == null) {
            log.error("getDiscoveredApplications called without credentials");
            safeSendError(servletResponse, Response.Status.INTERNAL_SERVER_ERROR);
            return null;
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

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                .getThreadLocalCarbonContext();

        try {
            DiscoveredApplicationListDTO result = handler
                    .discoverApplications(applicationDiscoveryContext, userNamePasswordCredentials,
                            discoverySearchCriteria, Locale.ENGLISH, carbonContext);
            return result;
        } catch (AppManagementException e) {
            String message = "Error while discovering the application from the backend server Server[%s], User[%s], Reason: ";
            log.error(message);
            log.debug(message, e);
            safeSendError(servletResponse, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return null;

    }

    private void safeSendError(HttpServletResponse servletResponse, Response.Status status) {
        try {
            servletResponse.sendError(status.getStatusCode());
        } catch (IOException e1) {
            log.error("Error not sent to client.", e1);
        }
    }

    /**
     * Returns the list of supported application servers types which is supported by the
     * system and which is configured per the tenant.
     *
     * @return
     */
    @GET
    @Path("server/type/list/{tenantDomain}")
    public List<String> getSupportedApplicationServerTypes(
            @Context final HttpServletResponse servletResponse, @Context HttpHeaders headers,
            @PathParam("tenantDomain") String tenantDomain) {

        if (tenantDomain == null)
            tenantDomain = SUPER_TENANT_DOMAIN;

        ApplicationDiscoveryServiceFactory applicationDiscoveryServiceFactory = (ApplicationDiscoveryServiceFactory) PrivilegedCarbonContext
                .getThreadLocalCarbonContext()
                .getOSGiService(ApplicationDiscoveryServiceFactory.class);

        return applicationDiscoveryServiceFactory.getAvailableHandlerNames();
    }
}
