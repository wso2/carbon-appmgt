/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.rest.api.store.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.mobile.store.Operations;
import org.wso2.carbon.appmgt.rest.api.store.AppsApiService;
import org.wso2.carbon.appmgt.rest.api.store.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.AppListDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.InstallDTO;
import org.wso2.carbon.appmgt.rest.api.store.utils.mappings.APPMappingUtil;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.util.List;

public class AppsApiServiceImpl extends AppsApiService {

    private static final Log log = LogFactory.getLog(AppsApiServiceImpl.class);


    @Override
    public Response appsDownloadPost(String contentType, InstallDTO install) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomainName = MultitenantUtils.getTenantDomain(username);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainName);
            String tenantUserName = MultitenantUtils.getTenantAwareUsername(username);
            String appId = install.getAppId();
            Operations mobileOperation = new Operations();
            String action = "install";
            String[] parameters = null;

            if ("user".equals(install.getType())) {
                parameters[0] = tenantDomainName;
            } else if ("device".equals(install.getType())) {
                parameters = (String[]) install.getDeviceIds();
                if (parameters == null) {
                    RestApiUtil.handleBadRequest("Device IDs should be provided to perform device app installation",
                                                 log);
                }
            } else {
                RestApiUtil.handleBadRequest("Invalid installation type.", log);
            }

            //TODO:Operations.performAction expects the user to be passed as a stringified object, so that
            //TODO:We are prviding a stringified user here
            JSONObject user = new JSONObject();
            user.put("username", tenantUserName);
            user.put("tenantDomain", tenantDomainName);
            user.put("tenantId", tenantId);

            appProvider.subscribeMobileApp(username, appId);
            mobileOperation.performAction(user.toString(), action, tenantId, appId, install.getType(), parameters);

        } catch (AppManagementException e) {
            RestApiUtil.handleInternalServerError("Error occurred while installing", e, log);
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("Error occurred while installing", e, log);
        } catch (JSONException e) {
            RestApiUtil.handleInternalServerError("Error occurred while installing", e, log);
        }
        return Response.ok().build();

    }

    @Override
    public Response appsUninstallationPost(String contentType, InstallDTO install) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String username = RestApiUtil.getLoggedInUsername();
        try {

            APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomainName = MultitenantUtils.getTenantDomain(username);
            int tenantId = 0;
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainName);

            String tenantUserName = MultitenantUtils.getTenantAwareUsername(username);
            String appId = install.getAppId();
            Operations mobileOperation = new Operations();
            String action = "uninstall";
            String[] parameters = null;

            if ("user".equals(install.getType())) {
                parameters = new String[1];
                parameters[0] = tenantDomainName;
            } else if ("device".equals(install.getType())) {
                parameters = (String[]) install.getDeviceIds();
                if (parameters == null) {
                    RestApiUtil.handleBadRequest("Device IDs should be provided to perform device app installation",
                                                 log);
                }
            } else {
                RestApiUtil.handleBadRequest("Invalid installation type.", log);
            }

            //TODO:Operations.performAction expects the user to be passed as a stringified object, so that
            //TODO:We are prviding a stringified user here
            JSONObject user = new JSONObject();
            user.put("username", tenantUserName);
            user.put("tenantDomain", tenantDomainName);
            user.put("tenantId", tenantId);

            boolean isUnSubscribed = appProvider.unSubscribeMobileApp(username, appId);
            if (!isUnSubscribed) {
                RestApiUtil.handlePreconditionFailedRequest(
                        "Application is not installed yet. Application with id : " + appId +
                                "must be installed prior to uninstall.", log);
            }
            mobileOperation.performAction(user.toString(), action, tenantId, appId, install.getType(), parameters);
        } catch (AppManagementException e) {
            RestApiUtil.handleInternalServerError("Error occurred while uninstalling", e, log);
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("Error occurred while uninstalling", e, log);
        } catch (JSONException e) {
            RestApiUtil.handleInternalServerError("Error occurred while uninstalling", e, log);
        }

        return Response.ok().build();
    }

    @Override
    public Response appsAppTypeGet(String appType, String query, Integer limit, Integer offset, String accept,
                                   String ifNoneMatch) {
        List<WebApp> allMatchedApps;
        AppListDTO appListDTO = null;

        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        try {
            //check if a valid asset type is provided
            if (!appType.equalsIgnoreCase(AppMConstants.APP_TYPE) &&
                    !appType.equalsIgnoreCase(AppMConstants.MOBILE_ASSET_TYPE)) {
                String errorMessage = "Invalid Asset Type : " + appType;
                return RestApiUtil.buildBadRequestException(errorMessage).getResponse();
            }

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            //if query parameter is not specified, This will search by name
            String searchType = AppMConstants.SEARCH_CONTENT_NAME;
            String searchContent = "";
            if (!StringUtils.isBlank(query)) {
                String[] querySplit = query.split(":");
                if (querySplit.length == 2 && StringUtils.isNotBlank(querySplit[0]) && StringUtils
                        .isNotBlank(querySplit[1])) {
                    searchType = querySplit[0];
                    searchContent = querySplit[1];
                } else if (querySplit.length == 1) {
                    searchContent = query;
                } else {
                    RestApiUtil.handleBadRequest("Provided query parameter '" + query + "' is invalid", log);
                }
            }

            //We should send null as the provider, Otherwise searchAPIs will return all APIs of the provider
            // instead of looking at type and query
            allMatchedApps = apiProvider.searchAppsWithOptionalType(searchContent, searchType, null, appType);
            if (allMatchedApps.isEmpty()) {
                String errorMessage = "No result found.";
                return RestApiUtil.buildNotFoundException(errorMessage, null).getResponse();
            }
            appListDTO = APPMappingUtil.fromAPIListToDTO(allMatchedApps, offset, limit);
            APPMappingUtil.setPaginationParams(appListDTO, query, offset, limit, allMatchedApps.size());
        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving Apps";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(appListDTO).build();
    }

    @Override
    public Response appsAppTypeIdAppIdGet(String appType, String appId, String accept, String ifNoneMatch,
                                          String ifModifiedSince) {
        AppDTO appToReturn = null;
        try {
            //currently supports only mobile apps
            if (!appType.equals("mobileapp")) {
                String errorMessage = "Type not supported.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String searchContent = appId;
            String searchType = "id";
            List<WebApp> allMatchedApps = apiProvider.searchAppsWithOptionalType(searchContent, searchType, null,
                                                                                 appType);
            if (allMatchedApps.isEmpty()) {
                String errorMessage = "Could not find requested application.";
                return RestApiUtil.buildNotFoundException(errorMessage, appId).getResponse();
            }
            appToReturn = APPMappingUtil.fromAPItoDTO(allMatchedApps.get(0));

        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
            } else {
                String errorMessage = "Error while retrieving App : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return Response.ok().entity(appToReturn).build();
    }


}
