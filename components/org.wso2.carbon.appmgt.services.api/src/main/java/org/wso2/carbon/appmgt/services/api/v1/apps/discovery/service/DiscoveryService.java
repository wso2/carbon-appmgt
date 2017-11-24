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
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationDTO;
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationListDTO;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The Discovery service.
 * The discovery api will be interfaced with REST model web services.
 */
@Produces({ "application/json" })
@Consumes({ "application/json" })
@Path("discovery")
public class DiscoveryService {

    private static final Log log = LogFactory.getLog(DiscoveryService.class);

    private ApplicationDiscoveryHelper helper = new ApplicationDiscoveryHelper();

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

        DiscoveryResult<DiscoveredApplicationDTO> result = helper
                .getDiscoveredApplication(servletRequest, servletResponse, tenantDomain,
                        discoverInfoRequest);
        if (result.getStatus() == Response.Status.OK.getStatusCode()) {
            return result.getData();
        } else {
            safeSendError(servletResponse, Response.Status.INTERNAL_SERVER_ERROR);
            return null;
        }
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
            @Context final HttpServletResponse servletResponse, @Context HttpHeaders headers,
            @PathParam("tenantDomain") String tenantDomain, DiscoverRequest discoverRequest) {

        DiscoveryResult<DiscoveredApplicationListDTO> result = helper
                .getDiscoveredApplications(servletRequest, servletResponse, tenantDomain,
                        discoverRequest);

        if (result.getStatus() == Response.Status.OK.getStatusCode()) {
            return result.getData();
        } else {
            safeSendError(servletResponse, Response.Status.INTERNAL_SERVER_ERROR);
            return null;
        }
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
            @Context final HttpServletRequest servletRequest, @Context HttpHeaders headers,
            @PathParam("tenantDomain") String tenantDomain) {

        return helper.getSupportedApplicationServerTypes(servletRequest, tenantDomain);
    }
}
