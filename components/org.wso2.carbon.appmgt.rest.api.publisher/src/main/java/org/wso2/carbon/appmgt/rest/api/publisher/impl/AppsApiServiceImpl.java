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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.AppMgtResourceAlreadyExistsException;
import org.wso2.carbon.appmgt.api.model.MobileApp;
import org.wso2.carbon.appmgt.api.model.Subscriber;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.rest.api.publisher.AppsApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.*;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.RestApiPublisherUtils;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.mappings.APPMappingUtil;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.appmgt.rest.api.util.validation.BeanValidator;
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
import java.util.Set;

/**
 * This is the service implementation class for Publisher API related operations
 */
public class AppsApiServiceImpl extends AppsApiService {

    private static final Log log = LogFactory.getLog(AppsApiService.class);
    BeanValidator beanValidator;

    /**
     * Upload binary files into storage
     * @param fileInputStream Uploading fileInputStream
     * @param fileDetail Attachment details
     * @param ifMatch
     * @param ifUnmodifiedSince
     * @return API path of the uploaded binary
     */
    @Override
    public Response appsMobileBinariesPost(InputStream fileInputStream, Attachment fileDetail, String ifMatch,
                                           String ifUnmodifiedSince) {

        BinaryDTO binaryDTO = new BinaryDTO();
        try {
            if (fileInputStream != null) {
                if ("application".equals(fileDetail.getContentType().getType())) {

                    String fileExtension =
                            FilenameUtils.getExtension(fileDetail.getContentDisposition().getParameter("filename"));
                    if (AppMConstants.MOBILE_APPS_ANDROID_EXT.equals(fileExtension) ||
                            AppMConstants.MOBILE_APPS_IOS_EXT.equals(fileExtension)) {

                        AppManagerConfiguration appManagerConfiguration = ServiceReferenceHolder.getInstance().
                                getAPIManagerConfigurationService().getAPIManagerConfiguration();
                        String directoryLocation = CarbonUtils.getCarbonHome() + File.separator +
                                appManagerConfiguration.getFirstProperty(AppMConstants.MOBILE_APPS_FILE_PRECISE_LOCATION);

                        File binaryFile = new File(directoryLocation);
                        //Generate UUID for the uploading file
                        String filename = RestApiPublisherUtils.generateBinaryUUID() + "." + fileExtension;
                        RestApiUtil.transferFile(fileInputStream, filename, binaryFile.getAbsolutePath());

                        ZipFileReading zipFileReading = new ZipFileReading();
                        String information = null;
                        String filePath = binaryFile.getAbsolutePath() + File.separator + filename;

                        if (AppMConstants.MOBILE_APPS_ANDROID_EXT.equals(fileExtension)) {
                            information = zipFileReading.readAndroidManifestFile(filePath);
                        } else if (AppMConstants.MOBILE_APPS_IOS_EXT.equals(fileExtension)) {
                            information = zipFileReading.readiOSManifestFile(filePath, filename);
                        }
                        JSONObject binaryObj = new JSONObject(information);
                        binaryDTO.setPackage(binaryObj.getString("package"));
                        binaryDTO.setVersion(binaryObj.getString("version"));
                        String fileAPI = appManagerConfiguration.getFirstProperty(
                                AppMConstants.MOBILE_APPS_FILE_API_LOCATION)
                                + filename;
                        binaryDTO.setPath(fileAPI);
                    } else {
                        RestApiUtil.handleBadRequest("Invalid Filetype is provided", log);
                    }
                } else {
                    RestApiUtil.handleBadRequest("Invalid file is provided with unsupported Media type.", log);
                }

            } else {
                RestApiUtil.handleBadRequest("'file' should be specified", log);
            }
        } catch (AppManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error occurred while parsing binary file archive and retrieving information", e, log);
        } catch (JSONException e) {
            RestApiUtil.handleInternalServerError(
                    "Error occurred while parsing metadata of binary and retrieving information", e, log);
        }
        return Response.ok().entity(binaryDTO).build();
    }

    /**
     * Upload static contents like images into storage
     * @param fileInputStream Upload static content's fileInputStream
     * @param fileDetail uploading file details
     * @param ifMatch
     * @param ifUnmodifiedSince
     * @return API path of the uploaded static content
     */
    @Override
    public Response appsStaticContentsPost(InputStream fileInputStream, Attachment fileDetail, String ifMatch,
                                           String ifUnmodifiedSince) {
        StaticContentDTO staticContentDTO = new StaticContentDTO();
        try {
            if (fileInputStream != null) {
                if ("image".equals(fileDetail.getContentType().getType())) {
                    AppManagerConfiguration appManagerConfiguration = ServiceReferenceHolder.getInstance().
                            getAPIManagerConfigurationService().getAPIManagerConfiguration();
                    String directoryLocation = CarbonUtils.getCarbonHome() + File.separator +
                            appManagerConfiguration.getFirstProperty(AppMConstants.MOBILE_APPS_FILE_PRECISE_LOCATION);

                    String fileExtension =
                            FilenameUtils.getExtension(fileDetail.getContentDisposition().getParameter("filename"));
                    File binaryFile = new File(directoryLocation);
                    //Generate UUID for the uploading file
                    String filename = RestApiPublisherUtils.generateBinaryUUID() + "." + fileExtension;
                    RestApiUtil.transferFile(fileInputStream, filename, binaryFile.getAbsolutePath());
                    String fileAPIPath = appManagerConfiguration.getFirstProperty(
                            AppMConstants.MOBILE_APPS_FILE_API_LOCATION)
                            + filename;
                    staticContentDTO.setPath(fileAPIPath);
                } else {
                    RestApiUtil.handleBadRequest("Invalid file is provided with unsupported Media type.", log);
                }
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

    /**
     * Create an application
     * @param appType application type ie: webapp, mobileapp
     * @param body Application DTO
     * @param contentType
     * @param ifModifiedSince
     * @return created application id
     */
    @Override
    public Response appsAppTypePost(String appType, AppDTO body, String contentType, String ifModifiedSince) {
        beanValidator = new BeanValidator();
        //Validate common mandatory fields for mobile and webapp
        beanValidator.validate(body);
        AppDTO appDTO = new AppDTO();

        try {
            APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
            if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType)) {

                MobileApp mobileApp = APPMappingUtil.fromDTOtoMobileApp(body);
                String applicationId = appProvider.createMobileApp(mobileApp);
                appDTO.setId(applicationId);
            } else if (AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                //TODO:Implement webapp logic
            } else {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }

        } catch (AppManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleConflictException("A mobile application already exists with the name : "
                        + body.getName(), log);
            } else {
                RestApiUtil.handleInternalServerError("Error occurred while creating mobile application : " + body.getName(), e, log);
            }
        }

        return Response.ok().entity(appDTO).build();
    }

    @Override
    public Response appsAppTypeAppIdAppIdSubscriptionsGet(String appType, String appId, String accept,
                                                          String ifNoneMatch, String ifModifiedSince) {
        UserIdListDTO userIdListDTO = new UserIdListDTO();
        try {
            APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
            if (AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {

                AppManagerConfiguration appManagerConfiguration = ServiceReferenceHolder.getInstance().
                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
                Boolean isSelfSubscriptionEnabled = Boolean.valueOf(appManagerConfiguration.getFirstProperty(
                        AppMConstants.ENABLE_SELF_SUBSCRIPTION));
                Boolean isEnterpriseSubscriptionEnabled = Boolean.valueOf(appManagerConfiguration.getFirstProperty(
                        AppMConstants.ENABLE_ENTERPRISE_SUBSCRIPTION));
                if(isSelfSubscriptionEnabled || isEnterpriseSubscriptionEnabled) {
                    WebApp webApp = appProvider.getAppDetailsFromUUID(appId);
                    Set<Subscriber> subscriberSet = appProvider.getSubscribersOfAPI(webApp.getId());
                    userIdListDTO.setUserIds(subscriberSet);
                }else{
                    RestApiUtil.handleBadRequest("Subscription is disabled", log);
                }
            } else {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }
        } catch (AppManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
            } else {
                String errorMessage = "Error while changing lifecycle state of app with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }

        }
        return Response.ok().entity(userIdListDTO).build();
    }

    @Override
    public Response appsAppTypeCacheGet(String appType, String query, Integer limit, Integer offset, String accept,
                                        String ifNoneMatch) {
        return null;
    }

    /**
     * Change lifecycle state of an application
     * @param appType application type ie: webapp, mobileapp
     * @param action lifecycle action
     * @param appId application uuid
     * @param ifMatch
     * @param ifUnmodifiedSince
     * @return status message
     */
    @Override
    public Response appsAppTypeChangeLifecyclePost(String appType, String action, String appId, String ifMatch,
                                                   String ifUnmodifiedSince) {
        ResponseMessageDTO responseMessageDTO = new ResponseMessageDTO();
        try {
            if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType) || AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {

                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                String[] allowedLifecycleActions = appProvider.getAllowedLifecycleActions(appId, appType);
                if (!ArrayUtils.contains(allowedLifecycleActions, action)) {
                    RestApiUtil.handleBadRequest(
                            "Action '" + action + "' is not allowed to perform on " + appType + " with id: " + appId +
                                    ". Allowed actions are " + Arrays.toString(allowedLifecycleActions), log);
                }
                appProvider.changeLifeCycleStatus(appType, appId, action);
            } else {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }
            responseMessageDTO.setMessage("Lifecycle status to be changed : " + action);
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
            } else {
                String errorMessage = "Error while changing lifecycle state of app with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return Response.accepted().entity(responseMessageDTO).build();
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

    /**
     * Update an application
     * @param appType appType application type ie: webapp, mobileapp
     * @param appId application id
     * @param body Application DTO
     * @param contentType
     * @param ifMatch
     * @param ifUnmodifiedSince
     * @return
     */
    @Override
    public Response appsAppTypeIdAppIdPut(String appType, String appId, AppDTO body, String contentType, String ifMatch,
                                          String ifUnmodifiedSince) {

        if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType)) {
            try {
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                //TODO:APP Validations
                //TODO:Get provider name from context (Token owner)
                //TODO:Permission check
                MobileApp updatingMobileApp = APPMappingUtil.fromDTOtoMobileApp(body);
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

    @Override
    public Response appsAppTypeIdAppIdCreateNewVersionPost(String appType, String appId, String contentType,
                                                           String ifModifiedSince) {
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdTagsGet(String appType, String appId, String accept, String ifNoneMatch) {
        try {
            if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType) || AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {

                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                appProvider.getAllTags(appType, appId);
            } else {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
            } else {
                String errorMessage = "Error retrieving tags for " + appType + " with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return Response.ok().build();
    }

    /**
     * Add a tag to an application
     * @param appType appType application type ie: webapp, mobileapp
     * @param appId application uuid
     * @param body tag list
     * @param contentType
     * @param ifMatch
     * @param ifUnmodifiedSince
     * @return
     */
    @Override
    public Response appsAppTypeIdAppIdTagsPut(String appType, String appId, TagListDTO body, String contentType,
                                              String ifMatch, String ifUnmodifiedSince) {
        beanValidator = new BeanValidator();
        //Validate common mandatory fields for mobile and webapp
        beanValidator.validate(body);
        try {
            if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType) || AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                List<String> tagList = body.getTags();
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                appProvider.addTags(appType, appId, tagList);
            } else {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
            } else {
                String errorMessage = "Error while adding a tag to " + appType + " with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return Response.ok().build();
    }


    @Override
    public Response appsAppTypeIdAppIdTagsDelete(String appType, String appId, TagListDTO body, String ifMatch, String ifUnmodifiedSince) {

        beanValidator = new BeanValidator();
        //Validate common mandatory fields for mobile and webapp
        beanValidator.validate(body);
        try {
            if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType) || AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                List<String> tags = body.getTags();
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                appProvider.removeTag(appType, appId, tags);
            } else {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
            } else {
                String errorMessage = "Error while deleting tags from " + appType + " with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return Response.ok().build();
    }

    @Override
    public Response appsAppTypeIdAppIdXacmlpoliciesGet(String appType, String appId, String accept,
                                                       String ifNoneMatch) {
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdXacmlpoliciesPost(String appType, String appId, PolicyPartialIdListDTO body,
                                                        String contentType, String ifModifiedSince) {
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdXacmlpoliciesPolicyPartialIdDelete(String appType, String appId,
                                                                         Integer policyPartialId, String ifMatch,
                                                                         String ifUnmodifiedSince) {
        return null;
    }

    @Override
    public Response appsAppTypeTagsGet(String appType, String accept, String ifNoneMatch) {
        try {
            if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType) || AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                appProvider.getAllTags(appType);
            } else {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }
        } catch (AppManagementException e) {
            String errorMessage = "Error retrieving tags for " + appType + "s.";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);

        }
        return Response.ok().build();
    }

    @Override
    public Response appsAppTypeThrottlingtiersGet(String appType, String accept, String ifNoneMatch) {
        return null;
    }

    @Override
    public Response appsAppTypeValidateContextPost(String appType, String appContext, String contentType,
                                                   String ifModifiedSince) {
        boolean isContextExists = false;
        try {
            if (AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                if(StringUtils.isEmpty(appContext)){
                    RestApiUtil.handleBadRequest("Webapp context is not provided", log);
                }

                if(appContext.indexOf("/") != 0){
                    appContext = "/" + appContext;
                }
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                isContextExists = appProvider.isContextExist(appContext);
            } else {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }
        } catch (AppManagementException e) {
            String errorMessage = "Error retrieving tags for " + appType + "s.";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(isContextExists).build();
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
