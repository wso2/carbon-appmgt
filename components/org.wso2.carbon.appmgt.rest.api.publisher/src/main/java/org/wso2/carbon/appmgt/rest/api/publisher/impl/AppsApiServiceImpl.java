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

package org.wso2.carbon.appmgt.rest.api.publisher.impl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APPLifecycleActions;
import org.wso2.carbon.appmgt.api.model.MobileApp;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.rest.api.publisher.AppsApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.BinaryDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.StaticContentDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.RestApiPublisherUtils;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.mappings.APPMappingUtil;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.mobile.utils.utilities.ZipFileReading;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class AppsApiServiceImpl extends AppsApiService {

    private static final Log log = LogFactory.getLog(AppsApiService.class);

    @Override
    public Response appsMobileBinariesPost(InputStream fileInputStream, Attachment fileDetail, String ifMatch,
                                           String ifUnmodifiedSince) {
        InputStream binaryInputStream = null;
        try {
            BinaryDTO binaryDTO = new BinaryDTO();
            if (fileInputStream != null) {

                AppManagerConfiguration appManagerConfiguration = ServiceReferenceHolder.getInstance().
                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
                String directoryLocation = CarbonUtils.getCarbonHome() + File.separator +
                        appManagerConfiguration.getFirstProperty(AppMConstants.MOBILE_APPS_FILE_PRECISE_LOCATION);
                File binaryFile = new File(directoryLocation);

                ContentDisposition contentDisposition = fileDetail.getContentDisposition();
                String fileExtension = FilenameUtils.getExtension(contentDisposition.getParameter("filename"));
                String filename = RestApiPublisherUtils.generateBinaryUUID() + "." + fileExtension;
                RestApiUtil.transferFile(fileInputStream, filename, binaryFile.getAbsolutePath());

                ZipFileReading zipFileReading = new ZipFileReading();
                String information = null;
                String filePath = binaryFile.getAbsolutePath() + File.separator + filename;

                if (AppMConstants.MOBILE_APPS_ANDROID_EXT.equals(fileExtension)) {
                    information = zipFileReading.readAndroidManifestFile(filePath);
                } else if (AppMConstants.MOBILE_APPS_IOS_EXT.equals(fileExtension)) {
                    information = zipFileReading.readiOSManifestFile(filePath, null);
                } else {
                    RestApiUtil.handleBadRequest("Invalid Filetype - Uploaded file is not an archive", log);
                }
                JSONObject binaryObj = new JSONObject(information);
                binaryDTO.setPackage(binaryObj.getString("package"));
                binaryDTO.setVersion(binaryObj.getString("version"));
                String fileAPI = appManagerConfiguration.getFirstProperty(
                        AppMConstants.MOBILE_APPS_FILE_API_LOCATION)
                        + filename;
                binaryDTO.setPath(fileAPI);
                return Response.ok().entity(binaryDTO).build();

            } else {
                RestApiUtil.handleBadRequest("'file' should be specified", log);
            }
        } catch (AppManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error occurred while parsing binary file archive and retrieving information", e, log);
        } catch (JSONException e) {
            RestApiUtil.handleInternalServerError(
                    "Error occurred while parsing metadata of binary and retrieving information", e, log);
        } finally {
            IOUtils.closeQuietly(binaryInputStream);
        }
        return null;
    }

    @Override
    public Response appsStaticContentsPost(InputStream fileInputStream, Attachment fileDetail, String ifMatch,
                                           String ifUnmodifiedSince) {
        StaticContentDTO staticContentDTO = new StaticContentDTO();
        try {
            if (fileInputStream != null) {

                AppManagerConfiguration appManagerConfiguration = ServiceReferenceHolder.getInstance().
                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
                //Read file storage location from ap-manager.xml
                String storageLocation = CarbonUtils.getCarbonHome() + File.separator +
                        appManagerConfiguration.getFirstProperty(AppMConstants.MOBILE_APPS_FILE_PRECISE_LOCATION);

                String fileName = RestApiPublisherUtils.uploadFileContent(fileInputStream, fileDetail, storageLocation);
                String fileAPIPath = appManagerConfiguration.getFirstProperty(
                        AppMConstants.MOBILE_APPS_FILE_API_LOCATION) + fileName;
                staticContentDTO.setPath(fileAPIPath);
            } else {
                RestApiUtil.handleBadRequest("'file' should be specified", log);
            }
        } catch (AppManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error occurred while parsing binary file archive and retrieving information", e, log);
        }
        return Response.ok().entity(staticContentDTO).build();
    }

    @Override
    public Response appsAppTypeGet(String appType, String query, Integer limit, Integer offset, String accept,
                                   String ifNoneMatch) {
        List<WebApp> allMatchedApps;
        AppListDTO appListDTO;

        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        try {
            //check if a valid asset type is provided
            if (!appType.equalsIgnoreCase(AppMConstants.APP_TYPE) &&
                    !appType.equalsIgnoreCase(AppMConstants.MOBILE_ASSET_TYPE)) {
                String errorMessage = "Invalid Asset Type : " + appType;
                RestApiUtil.handleBadRequest(errorMessage, log);
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
            return Response.ok().entity(appListDTO).build();
        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving Apps";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response appsAppTypePost(String appType, AppDTO body, String contentType, String ifModifiedSince) {
        AppDTO appDTO = new AppDTO();
        if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType)) {
            try {
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                //TODO:APP Validations
                //TODO:Get provider name from context (Token owner)
                //TODO:Permission check
                MobileApp mobileApp = APPMappingUtil.fromDTOtoMobileApp(body, "admin");
                String applicationId = appProvider.addMobileApp(mobileApp);
                appDTO.setId(applicationId);
            } catch (AppManagementException e) {
                RestApiUtil.handleInternalServerError("Error occurred while ", e, log);
            }
        } else {
            RestApiUtil.handleBadRequest("Invalid application type :" + appType, log);
        }
        return Response.ok().entity(appDTO).build();
    }

    @Override
    public Response appsAppTypeChangeLifecyclePost(String appType, String action, String appId, String ifMatch,
                                                   String ifUnmodifiedSince) {
        try {
            APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
            boolean isValidAction = false;

            String[] allowedLifecycleActions = appProvider.getAllowedLifecycleActions(appId, appType);
            if (!ArrayUtils.contains(allowedLifecycleActions, action)) {
                RestApiUtil.handleBadRequest(
                        "Action '" + action + "' is not allowed to perform on " + appType + " with id: " + appId +
                                ". Allowed actions are " + Arrays.toString(allowedLifecycleActions), log);
            }
            for (APPLifecycleActions appLifecycleAction : APPLifecycleActions.values()) {
                if (appLifecycleAction.getStatus().equalsIgnoreCase(action)) {
                    isValidAction = true;
                    break;
                }
            }
            if (!isValidAction) {
                RestApiUtil.handleBadRequest("Invalid action '" + action + "' performed on a " + appType
                                                     + " with UUID " + appId, log);
            }
            appProvider.changeLifeCycleStatus(appType, appId, action);
            return Response.accepted().build();
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
            } else {
                String errorMessage = "Error while changing lifcycle state of app with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdGet(String appType, String appId, String accept, String ifNoneMatch,
                                          String ifModifiedSince) {
        AppDTO apiToReturn;
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
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            WebApp webApp = allMatchedApps.get(0);
            webApp.setType(appType);
            apiToReturn = APPMappingUtil.fromAPItoDTO(webApp);
            return Response.ok().entity(apiToReturn).build();
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
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdPut(String appType, String appId, AppDTO body, String contentType, String ifMatch,
                                          String ifUnmodifiedSince) {

        if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType)) {
            try {
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                //TODO:APP Validations
                //TODO:Get provider name from context (Token owner)
                //TODO:Permission check
                MobileApp updatingMobileApp = APPMappingUtil.fromDTOtoMobileApp(body, "admin");
                updatingMobileApp.setAppId(appId);
                appProvider.updateMobileApp(updatingMobileApp);

            } catch (AppManagementException e) {
                RestApiUtil.handleInternalServerError("Error occurred while ", e, log);
            }
        } else {
            RestApiUtil.handleBadRequest("Invalid application type :" + appType, log);
        }
        return Response.accepted().build();
    }

    @Override
    public Response appsAppTypeIdAppIdDelete(String appType, String appId, String ifMatch, String ifUnmodifiedSince) {
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            List<WebApp> allMatchedApps = apiProvider.searchAppsWithOptionalType(appId, "id", null,
                                                                                 appType);
            if (allMatchedApps.isEmpty()) {
                String errorMessage = "Could not find requested application.";
                return RestApiUtil.buildNotFoundException(errorMessage, appId).getResponse();
            }
            WebApp webApp = allMatchedApps.get(0);
            if (appType.equals(AppMConstants.APP_TYPE)) {
                if (webApp.isAdvertiseOnly()) {
                    removeArtifactOnly(webApp, username);
                } else {
                    apiProvider.deleteApp(webApp.getId(), webApp.getSsoProviderDetails());
                }
            } else if (appType.equals(AppMConstants.MOBILE_ASSET_TYPE)) {
                removeArtifactOnly(webApp, username);
            }
            return Response.ok().build();
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
            } else {
                String errorMessage = "Error while deleting App : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (RegistryException e) {
            RestApiUtil.handleInternalServerError("Error while initializing registry", e, log);
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("Error while initializing UserStore", e, log);
        }
        return null;
    }

    //remove artifact from registry
    private void removeArtifactOnly(WebApp webApp, String username)
            throws RegistryException, AppManagementException, UserStoreException {
        String tenantDomainName = MultitenantUtils.getTenantDomain(username);
        String tenantUserName = MultitenantUtils.getTenantAwareUsername(username);
        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                tenantDomainName);
        Registry registry = ServiceReferenceHolder.getInstance().
                getRegistryService().getGovernanceUserRegistry(tenantUserName, tenantId);

        GenericArtifactManager artifactManager = AppManagerUtil.getArtifactManager(registry,
                                                                                   AppMConstants.MOBILE_ASSET_TYPE);
        artifactManager.removeGenericArtifact(webApp.getUUID());
    }
}
