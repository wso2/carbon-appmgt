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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.dto.AppPageUsageDTO;
import org.wso2.carbon.appmgt.api.dto.AppResponseTimeDTO;
import org.wso2.carbon.appmgt.api.dto.AppUsageByUserDTO;
import org.wso2.carbon.appmgt.api.exception.AppUsageQueryServiceClientException;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.App;
import org.wso2.carbon.appmgt.api.model.BusinessOwner;
import org.wso2.carbon.appmgt.api.model.Documentation;
import org.wso2.carbon.appmgt.api.model.EntitlementPolicyGroup;
import org.wso2.carbon.appmgt.api.model.FileContent;
import org.wso2.carbon.appmgt.api.model.MobileApp;
import org.wso2.carbon.appmgt.api.model.Subscriber;
import org.wso2.carbon.appmgt.api.model.SubscriptionCount;
import org.wso2.carbon.appmgt.api.model.Subscriptions;
import org.wso2.carbon.appmgt.api.model.Tag;
import org.wso2.carbon.appmgt.api.model.Tier;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.AppRepository;
import org.wso2.carbon.appmgt.impl.DefaultAppRepository;
import org.wso2.carbon.appmgt.impl.service.AppUsageStatisticsService;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.rest.api.publisher.AppsApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.BinaryDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.DocumentListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.LifeCycleDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.LifeCycleHistoryDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.LifeCycleHistoryListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.ResponseMessageDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.StatSummaryDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.TagListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.TierDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.TierListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.UserIdListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.RestApiPublisherUtils;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.mappings.APPMappingUtil;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.mappings.DocumentationMappingUtil;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.validation.AppDTOValidator;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.appmgt.rest.api.util.validation.BeanValidator;
import org.wso2.carbon.appmgt.rest.api.util.validation.CommonValidator;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.mobile.utils.utilities.ZipFileReading;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This is the service implementation class for Publisher API related operations
 */
public class AppsApiServiceImpl extends AppsApiService {

    private static final Log log = LogFactory.getLog(AppsApiService.class);
    BeanValidator beanValidator;

    /**
     * Upload binary files into storage
     *
     * @param fileInputStream   Uploading fileInputStream
     * @param fileDetail        Attachment details
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
                    String fileName = fileDetail.getContentDisposition().getParameter("filename");

                    if(!RestApiUtil.isValidFileName(fileName)){
                        RestApiUtil.handleBadRequest("Invalid file '"+fileName +"' has been provided to upload", log);
                    }

                    String fileExtension =
                            FilenameUtils.getExtension(fileName);
                    if (AppMConstants.MOBILE_APPS_ANDROID_EXT.equals(fileExtension) ||
                            AppMConstants.MOBILE_APPS_IOS_EXT.equals(fileExtension)) {

                        //Generate UUID for the uploading file
                        String filename = RestApiPublisherUtils.generateBinaryUUID() + "." + fileExtension;

                        FileContent fileContent = new FileContent();
                        fileContent.setContent(fileInputStream);
                        fileContent.setFileName(filename);
                        String filePath = RestApiPublisherUtils.uploadFileIntoStorage(fileContent);
                        ZipFileReading zipFileReading = new ZipFileReading();

                        String information = null;
                        if (AppMConstants.MOBILE_APPS_ANDROID_EXT.equals(fileExtension)) {
                            information = zipFileReading.readAndroidManifestFile(filePath);
                        } else if (AppMConstants.MOBILE_APPS_IOS_EXT.equals(fileExtension)) {
                            information = zipFileReading.readiOSManifestFile(filePath, filename);
                        }
                        JSONObject binaryObj = new JSONObject(information);
                        binaryDTO.setPackage(binaryObj.getString("package"));
                        binaryDTO.setVersion(binaryObj.getString("version"));

                        binaryDTO.setPath(filename);
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
     * Retrieve mobile binary from storage
     *
     * @param fileName          binary file name
     * @param ifMatch
     * @param ifUnmodifiedSince
     * @return
     */
    @Override
    public Response appsMobileBinariesFileNameGet(String fileName, String ifMatch, String ifUnmodifiedSince) {
        File binaryFile = null;
        String contentType = null;
        try {
            if(!RestApiUtil.isValidFileName(fileName)){
                RestApiUtil.handleBadRequest("Invalid filename '"+fileName +"' is provided", log);
            }

            String fileExtension = FilenameUtils.getExtension(fileName);
            if (AppMConstants.MOBILE_APPS_ANDROID_EXT.equals(fileExtension) ||
                    AppMConstants.MOBILE_APPS_IOS_EXT.equals(fileExtension)) {

                binaryFile = RestApiUtil.readFileFromStorage(fileName);
                contentType = new MimetypesFileTypeMap().getContentType(binaryFile);
                if (!contentType.startsWith("application")) {
                    RestApiUtil.handleBadRequest("Invalid file '" + fileName + "' with unsupported file type requested",
                                                 log);
                }
            } else {
                RestApiUtil.handleBadRequest("Invalid file '" + fileName + "' with unsupported media type is requested",
                                             log);
            }
        } catch (AppManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError("Static Content", fileName, e, log);
            } else {
                RestApiUtil.handleInternalServerError(
                        "Error occurred while retrieving mobile binary : " + fileName + "from storage", e, log);
            }
        }
        Response.ResponseBuilder response = Response.ok((Object) binaryFile);
        response.header(RestApiConstants.HEADER_CONTENT_DISPOSITION, RestApiConstants.CONTENT_DISPOSITION_ATTACHMENT
                + "; " + RestApiConstants.CONTENT_DISPOSITION_FILENAME + "=\"" + fileName + "\"");
        response.header(RestApiConstants.HEADER_CONTENT_TYPE, contentType);
        return response.build();
    }

    @Override
    public Response appsMobileGetplistTenantTenantIdFileFileNameGet(String tenantId, String fileName, String accept,
                                                                    String ifNoneMatch) {
        return null;
    }

    /**
     * Upload static contents like images into storage
     *
     * @param fileInputStream   Upload static content's fileInputStream
     * @param fileDetail        uploading file details
     * @param ifMatch
     * @param ifUnmodifiedSince
     * @return API path of the uploaded static content
     */
    @Override
    public Response appsStaticContentsPost(String appType, InputStream fileInputStream, Attachment fileDetail,
                                           String ifMatch, String ifUnmodifiedSince) {

        CommonValidator.isValidAppType(appType);
        Map<String, String> response = new HashMap<>();

        try {
            if (fileInputStream != null) {
                FileContent fileContent = new FileContent();
                if ("image".equals(fileDetail.getContentType().getType()) ||
                        "application".equals(fileDetail.getContentType().getType())) {
                    String fileName = fileDetail.getContentDisposition().getParameter(
                            RestApiConstants.CONTENT_DISPOSITION_FILENAME);
                    if(!RestApiUtil.isValidFileName(fileName)){
                        RestApiUtil.handleBadRequest("Invalid file '"+fileName +"' has been provided to upload", log);
                    }

                    String fileExtension = FilenameUtils.getExtension(fileName);
                    String filename = RestApiPublisherUtils.generateBinaryUUID() + "." + fileExtension;
                    fileContent.setFileName(filename);
                    fileContent.setContent(fileInputStream);
                    fileContent.setContentType(fileDetail.getContentType().toString());
                    if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType)) {
                        RestApiPublisherUtils.uploadFileIntoStorage(fileContent);
                        response.put("id", filename);
                    } else if (AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                        try {
                            DefaultAppRepository defaultAppRepository = new DefaultAppRepository(null);
                            UUID contentUUID = UUID.randomUUID();
                            fileContent.setUuid(contentUUID.toString());
                            fileContent.setContentLength(fileInputStream.available());
                            defaultAppRepository.persistStaticContents(fileContent);
                            response.put("id", contentUUID.toString() + File.separator + filename);
                        } catch (IOException e) {
                            RestApiUtil.handleInternalServerError("Error occurred while uploading static content", e, log);
                        }
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
        }
        return Response.ok().entity(response).build();
    }

    /**
     * Retrieve a given static content from storage
     *
     * @param fileName          request file name
     * @param ifMatch
     * @param ifUnmodifiedSince
     * @return
     */
    @Override
    public Response appsStaticContentsFileNameGet(String appType, String fileName, String ifMatch, String ifUnmodifiedSince) {
        CommonValidator.isValidAppType(appType);
        File staticContentFile = null;
        String contentType = null;

        try {

            if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType)) {

                staticContentFile = RestApiUtil.readFileFromStorage(fileName);
                if (staticContentFile == null) {
                    RestApiUtil.handleResourceNotFoundError("Static Content", fileName, log);
                }
                contentType = RestApiUtil.readFileContentType(staticContentFile.getAbsolutePath());
            } else if (AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                OutputStream outputStream = null;
                AppRepository appRepository = new DefaultAppRepository(null);
                try {
                    FileContent fileContent = appRepository.getStaticContent(fileName);
                    if (fileContent == null) {
                        RestApiUtil.handleResourceNotFoundError("Static Content", fileName, log);
                    }
                    staticContentFile = File.createTempFile("temp", ".tmp");
                    outputStream = new FileOutputStream(staticContentFile);
                    IOUtils.copy(fileContent.getContent(), outputStream);
                    contentType = fileContent.getContentType();
                } catch (IOException e) {
                    RestApiUtil.handleBadRequest("Error occurred while retrieving static content '" + fileName + "'", log);
                }
            }

            if (contentType != null && !contentType.startsWith("image")) {
                RestApiUtil.handleBadRequest("Invalid file '" + fileName + "'with unsupported file type requested",
                        log);
            }

            Response.ResponseBuilder response = Response.ok((Object) staticContentFile);
            response.header(RestApiConstants.HEADER_CONTENT_DISPOSITION, RestApiConstants.CONTENT_DISPOSITION_ATTACHMENT
                    + "; " + RestApiConstants.CONTENT_DISPOSITION_FILENAME + "=\"" + fileName + "\"");
            response.header(RestApiConstants.HEADER_CONTENT_TYPE, contentType);
            return response.build();
        } catch (AppManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError("Static Content", fileName, e, log);
            } else {
                RestApiUtil.handleInternalServerError(
                        "Error occurred while retrieving static content : " + fileName + "from storage", e, log);
            }
        }
        return null;
    }

    @Override
    public Response appsAppTypeGet(String appType, String query, String fieldFilter, Integer limit, Integer offset,
                                   String accept, String ifNoneMatch) {
        List<WebApp> allMatchedApps;

        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;

        try {
            //check if a valid asset type is provided
            if (!(AppMConstants.WEBAPP_ASSET_TYPE.equalsIgnoreCase(appType) || AppMConstants.MOBILE_ASSET_TYPE.equalsIgnoreCase(appType))) {
                String errorMessage = "Invalid Asset Type : " + appType;
                RestApiUtil.handleBadRequest(errorMessage, log);
            }

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            List<App> result = apiProvider.searchApps(appType, RestApiUtil.getSearchTerms(query));

            AppListDTO appListDTO = null;
            if(fieldFilter == null || "BASIC".equalsIgnoreCase(fieldFilter)){
                appListDTO = APPMappingUtil.getAppListDTOWithBasicFields(result, offset, limit);

            }else{
                appListDTO = APPMappingUtil.getAppListDTOWithAllFields(result, offset, limit);
            }

            APPMappingUtil.setPaginationParams(appListDTO, query, offset, limit, result.size());
            return Response.ok().entity(appListDTO).build();
        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving Apps";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }


    /**
     * Create an application
     *
     * @param appType         application type ie: webapp, mobileapp
     * @param body            Application DTO
     * @param contentType
     * @param ifModifiedSince
     * @return created application id
     */
    @Override
    public Response appsAppTypePost(String appType, AppDTO body, String contentType, String ifModifiedSince) {
        CommonValidator.isValidAppType(appType);
        beanValidator = new BeanValidator();
        //Validate common mandatory fields for mobile and webapp
        beanValidator.validate(body);
        Map<String, String> response = new HashMap<>();
        AppDTOValidator.validateAppDTO(appType, body);
        String applicationId = null;
        try {
            APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
            if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType)) {

                MobileApp mobileApp = APPMappingUtil.fromDTOtoMobileApp(body);
                applicationId = appProvider.createMobileApp(mobileApp);
            } else if (AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {

                WebApp webApp = APPMappingUtil.fromDTOToWebapp(body);
                validateWebApp(webApp, appProvider, true);
                webApp.setCreatedTime(RestApiPublisherUtils.getCreatedTimeEpoch());
                applicationId = appProvider.createWebApp(webApp);
            }
            response.put("AppId", applicationId);
        } catch (AppManagementException e) {
            if (RestApiUtil.isDueToResourceAlreadyExisting(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleConflictException("A duplicate " + appType + " already exists with the name : "
                                                            + body.getName(), log);
            } else {
                RestApiUtil.handleInternalServerError(
                        "Error occurred while creating mobile application : " + body.getName(), e, log);
            }
        }

        return Response.ok().entity(response).build();
    }

    /**
     * @param webApp
     * @param appProvider
     * @param isNewApp    if the app is a new app or existing app
     * @return
     * @throws AppManagementException
     */
    private boolean validateWebApp(WebApp webApp, APIProvider appProvider, boolean isNewApp)
            throws AppManagementException {
        //check if the context is unique
        if (isNewApp) {
            boolean isContextExists = appProvider.isContextExist(webApp.getContext());
            if (isContextExists) {
                throw new AppManagementException("Context - " + webApp.getContext() + " already exists");
            }
        }

        //check if the business owner exists
        if (webApp.getBusinessOwner() != null) {
            int businessOwnerId = Integer.parseInt(webApp.getBusinessOwner());
            BusinessOwner businessOwner = appProvider.getBusinessOwner(businessOwnerId);
            if (businessOwner == null) {
                throw new AppManagementException("Invalid Business Owner - " + businessOwnerId);
            }
        }


        //check if the role/tiers are exists
        //iterate through all groups
        List<EntitlementPolicyGroup> groups = webApp.getAccessPolicyGroups();
        String tenantDomainName = RestApiUtil.getLoggedInUserTenantDomain();
        for (EntitlementPolicyGroup group : groups) {
            //iterate through all roles
            List<String> roles = group.getUserRolesAsList();
            for (String role : roles) {
                try {
                    if (!"".equals(role)) {
                        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class,
                                                                                                null);
                        int tenantId =
                                ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                                        tenantDomainName);
                        UserRealm realm = realmService.getTenantUserRealm(tenantId);
                        UserStoreManager manager = realm.getUserStoreManager();
                        //check if the role is exists
                        if (!manager.isExistingRole(role)) {
                            throw new AppManagementException("Invalid role - " + role);
                        }
                    }

                } catch (UserStoreException e) {
                    throw new AppManagementException("Error while fetching User Store");
                }
            }

            String throttlingTier = group.getThrottlingTier();
            Set<Tier> tiers = appProvider.getTiers(tenantDomainName);
            boolean tierExists = false;
            for (Tier tier : tiers) {
                if (tier.getName().equals(throttlingTier)) {
                    tierExists = true;
                }
            }
            if (!tierExists) {
                throw new AppManagementException("Invalid Throttling Tier - " + throttlingTier);
            }
        }

        return true;
    }


    /**
     * Change lifecycle state of an application
     *
     * @param appType           application type ie: webapp, mobileapp
     * @param action            lifecycle action
     * @param appId             application uuid
     * @param ifMatch
     * @param ifUnmodifiedSince
     * @return status message
     */
    @Override
    public Response appsAppTypeChangeLifecyclePost(String appType, String action, String appId, String ifMatch,
                                                   String ifUnmodifiedSince) {
        CommonValidator.isValidAppType(appType);
        ResponseMessageDTO responseMessageDTO = new ResponseMessageDTO();
        try {

            APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
            String[] allowedLifecycleActions = appProvider.getAllowedLifecycleActions(appId, appType);
            if (!ArrayUtils.contains(allowedLifecycleActions, action)) {
                RestApiUtil.handleBadRequest(
                        "Action '" + action + "' is not allowed to perform on " + appType + " with id: " + appId +
                                ". Allowed actions are " + Arrays.toString(allowedLifecycleActions), log);
            }
            appProvider.changeLifeCycleStatus(appType, appId, action);

            responseMessageDTO.setMessage("Lifecycle action : " + action + " has been accepted for processing ");
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, e, log);
            } else if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailedError("The user is not permitted to perform lifecycle action '" +
                                                                   action + "' on " + appType + " with uuid " + appId,
                                                           e, log);
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
        AppDTO appDTO;
        try {

            Map<String, String> searchTerms = new HashMap<String, String>();
            searchTerms.put("id", appId);

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            List<App> result = apiProvider.searchApps(appType, searchTerms);

            if (result.isEmpty()) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, log);
            }

            App app = result.get(0);
            appDTO = APPMappingUtil.fromAppToDTO(app);

            return Response.ok().entity(appDTO).build();
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, e, log);
            } else {
                String errorMessage = "Error while retrieving App : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Update an application
     *
     * @param appType           appType application type ie: webapp, mobileapp
     * @param appId             application id
     * @param body              Application DTO
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
        } else if(AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)){

            try {
                APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
                body.setId(appId);
                WebApp webApp = APPMappingUtil.fromDTOToWebapp(body);
                validateWebApp(webApp, apiProvider, false);
                apiProvider.updateApp(webApp);

            } catch (AppManagementException e) {
                e.printStackTrace();
            }


        } else{
            RestApiUtil.handleBadRequest("Invalid application type :" + appType, log);
        }
        return Response.ok().build();
    }

    @Override
    public Response appsAppTypeIdAppIdDelete(String appType, String appId, String ifMatch, String ifUnmodifiedSince) {
        try {
            CommonValidator.isValidAppType(appType);

            Map<String, String> searchTerms = new HashMap<String, String>();
            searchTerms.put("id", appId);

            String username = RestApiUtil.getLoggedInUsername();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            List<App> result = apiProvider.searchApps(appType, searchTerms);
            if (result.isEmpty()) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, log);
            }

            App app = result.get(0);

            if (appType.equals(AppMConstants.WEBAPP_ASSET_TYPE)) {

                WebApp webApp = (WebApp) app;

                if (webApp.isAdvertiseOnly()) {
                    removeRegistryArtifact(webApp, username);
                } else {
                    //todo: pass the correct auth cookie
                    String authorizedAdminCookie = null;
                    apiProvider.deleteApp(webApp.getId(), webApp.getSsoProviderDetails(), authorizedAdminCookie);
                }
            } else if (appType.equals(AppMConstants.MOBILE_ASSET_TYPE)) {
                removeRegistryArtifact(app, username);
            }
            return Response.ok().build();
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, e, log);
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
    public Response appsAppTypeIdAppIdCreateNewVersionPost(String appType, String appId, AppDTO body,String contentType,
                                                           String ifModifiedSince){

        APIProvider apiProvider = null;
        try {
            apiProvider = RestApiUtil.getLoggedInUserProvider();

            App app = null;
            if (AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                app = APPMappingUtil.fromDTOToWebapp(body);
            } else if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType)) {
                MobileApp mobileAppModel = new MobileApp();
                mobileAppModel.setVersion(body.getVersion());
                mobileAppModel.setDisplayName(body.getDisplayName());
                mobileAppModel.setType(AppMConstants.MOBILE_ASSET_TYPE);
                app = mobileAppModel;
            } else {
                RestApiUtil.handleBadRequest("Invalid application type :" + appType, log);
            }

            app.setUUID(appId);
            String newUUID = apiProvider.createNewVersion(app);

            Map<String, String> response = new HashMap<>();
            response.put("AppId", newUUID);

            return Response.ok(response).build();
        } catch (AppManagementException e) {
            RestApiUtil.handleInternalServerError(String.format("Error while creating new version for the app '%s':'%s'", appType, appId), e, log);
        }

        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdDiscoverPost(String appType, String appId, String contentType,
                                                   String ifModifiedSince) {
        return null;
    }

    /**
     *  Returns all the documents of the given APP uuid that matches to the search condition
     *
     * @param appType application type ie:webapp,mobileapp
     * @param appId application identifier
     * @param limit
     * @param offset
     * @param accept
     * @param ifNoneMatch
     * @return matched documents as a list if DocumentDTOs
     */
    @Override
    public Response appsAppTypeIdAppIdDocsGet(String appType, String appId, Integer limit, Integer offset, String accept, String ifNoneMatch) {
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        try {
            APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist

            WebApp webApp = appProvider.getWebApp(appId);
            APIIdentifier appIdentifier = webApp.getId();

            List<Documentation> allDocumentation = appProvider.getAllDocumentation(appIdentifier);
            DocumentListDTO documentListDTO = DocumentationMappingUtil.fromDocumentationListToDTO(allDocumentation,
                    offset, limit);
            DocumentationMappingUtil
                    .setPaginationParams(documentListDTO, appId, offset, limit, allDocumentation.size());
            return Response.ok().entity(documentListDTO).build();
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, e, log);
            } else {
                String msg = "Error while retrieving documents of App "+appType +" with appId "+ appId;
                RestApiUtil.handleInternalServerError(msg, e, log);
            }
        }
        return null;
    }

    /**
     * Add documentation to a given app
     * @param appId application uuid
     * @param appType application type
     * @param body
     * @param contentType
     * @return
     */
    @Override
    public Response appsAppTypeIdAppIdDocsPost(String appId, String appType, DocumentDTO body, String contentType) {

        CommonValidator.isValidAppType(appType);
        try {
            if(AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                Documentation documentation = DocumentationMappingUtil.fromDTOtoDocumentation(body);
                String documentName = body.getName();
                String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
                if (body.getType() == DocumentDTO.TypeEnum.OTHER && StringUtils.isBlank(body.getOtherTypeName())) {
                    //check otherTypeName for not null if doc type is OTHER
                    RestApiUtil.handleBadRequest("otherTypeName cannot be empty if type is OTHER.", log);
                }
                String sourceUrl = body.getSourceUrl();
                if (body.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                        (StringUtils.isBlank(sourceUrl) || !RestApiUtil.isURL(sourceUrl))) {
                    RestApiUtil.handleBadRequest("Invalid document sourceUrl Format", log);
                }
                //this will fail if user does not have access to the API or the API does not exist

                WebApp webApp = appProvider.getWebApp(appId);
                APIIdentifier appIdentifier = webApp.getId();
                if (appProvider.isDocumentationExist(appIdentifier, documentName)) {
                    String errorMessage = "Requested document '" + documentName + "' already exists";
                    RestApiUtil.handleConflictException(errorMessage, log);
                }
                appProvider.addDocumentation(appIdentifier, documentation);

                //retrieve the newly added document
                String newDocumentId = documentation.getId();
                documentation = appProvider.getDocumentation(newDocumentId, tenantDomain);
                DocumentDTO newDocumentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
                String uriString = RestApiConstants.RESOURCE_PATH_DOCUMENTS_DOCUMENT_ID
                        .replace(RestApiConstants.APPID_PARAM, appId)
                        .replace(RestApiConstants.DOCUMENTID_PARAM, newDocumentId);
                URI uri = new URI(uriString);
                return Response.created(uri).entity(newDocumentDTO).build();
            }else {
                RestApiUtil.handleBadRequest("App type "+ appType + " not supported", log);
            }
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, e, log);
            } else {
                String errorMessage = "Error while adding the document for "+appType+" with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving location for document " + body.getName() + " of App " + appId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieve documentation for a given documentationId and for a given app id
     * @param appType application type ie: webapp,mobileapp
     * @param appId application uuid
     * @param documentId  documentID
     * @param ifMatch
     * @param ifUnmodifiedSince
     * @return
     */
    @Override
    public Response appsAppTypeIdAppIdDocsDocumentIdGet(String appType, String appId, String documentId, String ifMatch, String ifUnmodifiedSince) {
        Documentation documentation;
        DocumentDTO documentDTO = null;
        try {
            if(AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                //TODO:Check App access prmissions
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
                documentation = appProvider.getDocumentation(documentId, tenantDomain);
                if (documentation == null) {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
                }

                documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
            }else {
                RestApiUtil.handleBadRequest("App type "+ appType + " not supported", log);
            }
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, e, log);
            } else {
                String errorMessage = "Error while retrieving document : " + documentId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return Response.ok().entity(documentDTO).build();
    }

    /**
     * Delete a documentation for a given document id
     * @param appType appType
     * @param appId application id
     * @param documentId document Id
     * @param ifMatch
     * @param ifUnmodifiedSince
     * @return
     */
    @Override
    public Response appsAppTypeIdAppIdDocsDocumentIdDelete(String appType, String appId, String documentId, String ifMatch, String ifUnmodifiedSince) {
        Documentation documentation;
        try {
            if (AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
                WebApp webApp = appProvider.getWebApp(appId);
                APIIdentifier appIdentifier = webApp.getId();
                documentation = appProvider.getDocumentation(documentId, tenantDomain);
                if (documentation == null) {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
                }
                appProvider.removeDocumentation(appIdentifier, documentId);
            }

        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, e, log);
            } else {
                String errorMessage = "Error while retrieving " + appType + " : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return Response.ok().build();
    }

    /**
     * Update a document with a given document id
     * @param appId application id
     * @param documentId documentation id
     * @param appType application type
     * @param body Documentation DTO
     * @param contentType
     * @param ifMatch
     * @param ifUnmodifiedSince
     * @return
     */
    @Override
    public Response appsAppTypeIdAppIdDocsDocumentIdPut(String appId, String documentId, String appType, DocumentDTO body, String contentType, String ifMatch, String ifUnmodifiedSince) {
        try {
            APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            WebApp webApp = appProvider.getWebApp(appId);
            String sourceUrl = body.getSourceUrl();
            Documentation oldDocument = appProvider.getDocumentation(documentId, tenantDomain);

            //validation checks for existence of the document
            if (oldDocument == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
                return null;
            }
            if (body.getType() == DocumentDTO.TypeEnum.OTHER && StringUtils.isBlank(body.getOtherTypeName())) {
                //check otherTypeName for not null if doc type is OTHER
                RestApiUtil.handleBadRequest("otherTypeName cannot be empty if type is OTHER.", log);
                return null;
            }
            if (body.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                    (StringUtils.isBlank(sourceUrl) || !RestApiUtil.isURL(sourceUrl))) {
                RestApiUtil.handleBadRequest("Invalid document sourceUrl Format", log);
                return null;
            }

            //overriding some properties
            body.setName(oldDocument.getName());

            Documentation newDocumentation = DocumentationMappingUtil.fromDTOtoDocumentation(body);
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier appIdentifier = webApp.getId();
            appProvider.updateDocumentation(appIdentifier, newDocumentation);

            //retrieve the updated documentation
            newDocumentation = appProvider.getDocumentation(documentId, tenantDomain);
            return Response.ok().entity(DocumentationMappingUtil.fromDocumentationToDTO(newDocumentation)).build();
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, e, log);
            } else {
                String errorMessage = "Error while updating the document " + documentId + " for API : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Document Content retrieve
     * @param appId  id of the application
     * @param documentId id of the documentation
     * @param appType type of the application
     * @param accept
     * @param ifNoneMatch
     * @param ifModifiedSince
     * @return
     */
    @Override
    public Response appsAppTypeIdAppIdDocsDocumentIdContentGet(String appId, String documentId, String appType, String accept, String ifNoneMatch, String ifModifiedSince) {

        Documentation documentation;
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            WebApp webApp = appProvider.getWebApp(appId);
            APIIdentifier appIdentifier = webApp.getId();
            documentation = appProvider.getDocumentation(documentId, tenantDomain);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
                return null;
            }

            //gets the content depending on the type of the document
            if (documentation.getSourceType().equals(Documentation.DocumentSourceType.FILE)) {
                String resource = documentation.getFilePath();
                Map<String, Object> docResourceMap = AppManagerUtil.getDocument(username, resource, tenantDomain);
                Object fileDataStream = docResourceMap.get(AppMConstants.DOCUMENTATION_RESOURCE_MAP_DATA);
                Object contentType = docResourceMap.get(AppMConstants.DOCUMENTATION_RESOURCE_MAP_CONTENT_TYPE);
                contentType = contentType == null ? RestApiConstants.APPLICATION_OCTET_STREAM : contentType;
                String name = docResourceMap.get(AppMConstants.DOCUMENTATION_RESOURCE_MAP_NAME).toString();
                return Response.ok(fileDataStream)
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, contentType)
                        .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                        .build();
            } else if (documentation.getSourceType().equals(Documentation.DocumentSourceType.INLINE)) {
                String content = appProvider.getDocumentationContent(appIdentifier, documentation.getName());
                return Response.ok(content)
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, AppMConstants.DOCUMENTATION_INLINE_CONTENT_TYPE)
                        .build();
            } else if (documentation.getSourceType().equals(Documentation.DocumentSourceType.URL)) {
                String sourceUrl = documentation.getSourceUrl();
                return Response.seeOther(new URI(sourceUrl)).build();
            }
        } catch (AppManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, e, log);
            } else {
                String errorMessage = "Error while retrieving document " + documentId + " of the " + appType +
                        " with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving source URI location of " + documentId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Document content upload
     *
     * @param appType           application type
     * @param appId             application uuid
     * @param documentId        documentation id
     * @param fileInputStream   document content stream
     * @param fileDetail        document file details
     * @param inlineContent
     * @param ifMatch
     * @param ifUnmodifiedSince
     * @return
     */
    @Override
    public Response appsAppTypeIdAppIdDocsDocumentIdContentPost(String appType, String appId, String documentId,
                                                                InputStream fileInputStream, Attachment fileDetail,
                                                                String inlineContent, String ifMatch, String ifUnmodifiedSince) {

        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            WebApp webApp = appProvider.getWebApp(appId);
            APIIdentifier appIdentifier = webApp.getId();
            if (fileInputStream != null && inlineContent != null) {
                RestApiUtil.handleBadRequest("Only one of 'file' and 'inlineContent' should be specified", log);
            }
            Documentation documentation = appProvider.getDocumentation(documentId, tenantDomain);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
                return null;
            }
            //add content depending on the availability of either input stream or inline content
            if (fileInputStream != null) {
                if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.FILE)) {
                    RestApiUtil.handleBadRequest("Source type of document " + documentId + " is not FILE", log);
                }
                RestApiPublisherUtils.attachFileToDocument(webApp, documentation, fileInputStream, fileDetail);
            } else if (inlineContent != null) {
                if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.INLINE)) {
                    RestApiUtil.handleBadRequest("Source type of document " + documentId + " is not INLINE", log);
                }
                appProvider.addDocumentationContent(appIdentifier, documentation.getName(), inlineContent);
            } else {
                RestApiUtil.handleBadRequest("Either 'file' or 'inlineContent' should be specified", log);
            }

            //retrieving the updated doc and the URI
            Documentation updatedDoc = appProvider.getDocumentation(documentId, tenantDomain);
            DocumentDTO documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(updatedDoc);
            String uriString = RestApiConstants.RESOURCE_PATH_DOCUMENT_CONTENT
                    .replace(RestApiConstants.APPID_PARAM, appId)
                    .replace(RestApiConstants.DOCUMENTID_PARAM, documentId);
            URI uri = new URI(uriString);
            return Response.created(uri).entity(documentDTO).build();
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, e, log);
            } else {
                String errorMessage = "Error while retrieving document " + documentId + " of the API " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving source URI location of " + documentId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }

        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdLifecycleGet(String appType, String appId, String accept, String ifNoneMatch) {
        LifeCycleDTO lifeCycleDTO = new LifeCycleDTO();
        try {
            //Validate App Type
            CommonValidator.isValidAppType(appType);
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomainName = MultitenantUtils.getTenantDomain(username);
            String tenantUserName = MultitenantUtils.getTenantAwareUsername(username);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainName);
            Registry registry = ServiceReferenceHolder.getInstance().
                    getRegistryService().getGovernanceUserRegistry(tenantUserName, tenantId);
            boolean isAsynchronousFlow =
                    org.wso2.carbon.appmgt.impl.workflow.WorkflowExecutorFactory.getInstance().getWorkflowExecutor(
                            "AM_APPLICATION_PUBLISH").isAsynchronus();
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry, appType);
            GenericArtifact artifact = artifactManager.getGenericArtifact(appId);
            //Validate App Id
            if (artifact == null) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, log);
            }

            String state = artifact.getLifecycleState().toUpperCase();
            String[] actions;
            if (AppMConstants.MOBILE_ASSET_TYPE.equalsIgnoreCase(appType)) {
                actions = artifact.getAllLifecycleActions(AppMConstants.MOBILE_LIFE_CYCLE);
            } else {
                actions = artifact.getAllLifecycleActions(AppMConstants.WEBAPP_LIFE_CYCLE);
            }

            lifeCycleDTO.setActions(Arrays.asList(actions));
            lifeCycleDTO.setAsync(isAsynchronousFlow);
            lifeCycleDTO.setState(state);
        } catch (Exception e) {
            String errorMessage = "Error while retrieving lifecycle state of app with id : " + appId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(lifeCycleDTO).build();
    }

    @Override
    public Response appsAppTypeIdAppIdLifecycleHistoryGet(String appType, String appId, String accept,
                                                          String ifNoneMatch) {
        LifeCycleHistoryListDTO lifeCycleHistoryListDTO = new LifeCycleHistoryListDTO();
        try {
            //Validate App Type
            CommonValidator.isValidAppType(appType);
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomainName = MultitenantUtils.getTenantDomain(username);
            String tenantUserName = MultitenantUtils.getTenantAwareUsername(username);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainName);
            Registry registry = ServiceReferenceHolder.getInstance().
                    getRegistryService().getGovernanceUserRegistry(tenantUserName, tenantId);
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry, appType);
            GenericArtifact artifact = artifactManager.getGenericArtifact(appId);
            //Validate App Id
            if (artifact == null) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, log);
            }

            String historyRegPath = getHistoryPath(artifact);
            String historyResourceXMLStr = IOUtils.toString(registry.get(historyRegPath).getContentStream());
            JSONObject historyResourceObj = XML.toJSONObject(historyResourceXMLStr);

            JSONArray historyResourceJsonArray = (historyResourceObj.getJSONObject("lifecycleHistory")).getJSONArray(
                    "item");
            List<LifeCycleHistoryDTO> lifeCycleHistoryDTOList = new ArrayList<>();
            //iterate life cycle history json
            for (int i = 0; i < historyResourceJsonArray.length() - 1; i++) {
                JSONObject lifecycleHistoryStateObj = (JSONObject) historyResourceJsonArray.get(i);
                LifeCycleHistoryDTO lifeCycleHistoryDTO = new LifeCycleHistoryDTO();
                lifeCycleHistoryDTO.setOrder(Integer.parseInt(lifecycleHistoryStateObj.get("order").toString()));
                lifeCycleHistoryDTO.setState((String) lifecycleHistoryStateObj.get("state"));
                lifeCycleHistoryDTO.setTargetState((String) lifecycleHistoryStateObj.get("targetState"));
                lifeCycleHistoryDTO.setTimestamp((String) lifecycleHistoryStateObj.get("timestamp"));
                lifeCycleHistoryDTO.setUser((String) lifecycleHistoryStateObj.get("user"));
                lifeCycleHistoryDTOList.add(lifeCycleHistoryDTO);
            }
            lifeCycleHistoryListDTO.setLifeCycleHistoryList(lifeCycleHistoryDTOList);
        } catch (GovernanceException e) {
            String errorMessage = "GovernanceException while retrieving lifecycle History of app with id : " + appId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (IOException e) {
            String errorMessage = "IOException while retrieving lifecycle History of app with id : " + appId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (UserStoreException e) {
            String errorMessage = "UserStoreException while retrieving lifecycle History of app with id : " + appId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (RegistryException e) {
            String errorMessage = "RegistryException while retrieving lifecycle History of app with id : " + appId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (JSONException e) {
            String errorMessage = "JSONException while retrieving lifecycle History of app with id : " + appId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(lifeCycleHistoryListDTO).build();
    }

    @Override
    public Response appsAppTypeIdAppIdSubscriptionsGet(String appType, String appId, String accept, String ifNoneMatch,
                                                       String ifModifiedSince) {
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
                if (isSelfSubscriptionEnabled || isEnterpriseSubscriptionEnabled) {
                    WebApp webApp = appProvider.getAppDetailsFromUUID(appId);
                    Set<Subscriber> subscriberSet = appProvider.getSubscribersOfAPI(webApp.getId());
                    userIdListDTO.setUserIds(subscriberSet);
                } else {
                    RestApiUtil.handleBadRequest("Subscription is disabled", log);
                }
            } else {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }
        } catch (AppManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, e, log);
            } else {
                String errorMessage = "Error while changing lifecycle state of app with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }

        }
        return Response.ok().entity(userIdListDTO).build();
    }

    @Override
    public Response appsAppTypeIdAppIdTagsGet(String appType, String appId, String accept, String ifNoneMatch) {
        TagListDTO tagListDTO = new TagListDTO();
        List<String> tags = new ArrayList<>();
        try {
            if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType) || AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {

                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                for (Tag tag : appProvider.getAllTags(appType, appId)) {
                    tags.add(tag.getName());
                }
                tagListDTO.setTags(tags);
            } else {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(appType, appId, e, log);
            } else {
                String errorMessage = "Error retrieving tags for " + appType + " with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return Response.ok().entity(tagListDTO).build();
    }

    /**
     * Add a tag to an application
     *
     * @param appType           appType application type ie: webapp, mobileapp
     * @param appId             application uuid
     * @param body              tag list
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
                RestApiUtil.handleResourceNotFoundError(appType, appId, e, log);
            } else {
                String errorMessage = "Error while adding a tag to " + appType + " with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return Response.ok().build();
    }


    @Override
    public Response appsAppTypeIdAppIdTagsDelete(String appType, String appId, TagListDTO body, String ifMatch,
                                                 String ifUnmodifiedSince) {

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
                RestApiUtil.handleResourceNotFoundError(appType, appId, e, log);
            } else {
                String errorMessage = "Error while deleting tags from " + appType + " with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return Response.ok().build();
    }

    @Override
    public Response appsAppTypeThrottlingtiersGet(String appType, String accept,
                                                  String ifNoneMatch) {
        TierListDTO tierListDTO = new TierListDTO();
        try {
            //check appType validity (currently support only webApps)
            if (!AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }

            List<TierDTO> tierDTOList = new ArrayList<>();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            Set<Tier> tiers = apiProvider.getTiers();
            if (tiers.isEmpty()) {
                return RestApiUtil.buildNotFoundException("Tiers", null).getResponse();
            }

            for (Tier tier : tiers) {
                TierDTO tierDTO = new TierDTO();
                tierDTO.setTierName(tier.getName());
                tierDTO.setTierDisplayName(tier.getDisplayName());
                tierDTO.setTierDescription(tier.getDescription() != null ? tier.getDescription() : "");
                tierDTO.setTierSortKey(tier.getRequestPerMinute());
                tierDTOList.add(tierDTO);
            }
            tierListDTO.setTierList(tierDTOList);
        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving Throttling Tier details";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(tierListDTO).build();
    }

    @Override
    public Response appsAppTypeStatsStatTypeGet(String appType, String statType, String startTimeStamp,
                                                String endTimeStamp, Integer limit, String accept, String ifNoneMatch) {
        StatSummaryDTO statSummaryDTO = new StatSummaryDTO();
        try {
            if (AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                String username = RestApiUtil.getLoggedInUsername();
                String tenantDomainName = MultitenantUtils.getTenantDomain(username);
                String providerName = RestApiConstants.STATS_ALL_PROVIDERS;
                // check whether start and end dates are in correct format.
                if (!StringUtils.isEmpty(startTimeStamp) || !StringUtils.isEmpty(endTimeStamp)) {
                    if (!isTimeStampValid(startTimeStamp) || !isTimeStampValid(endTimeStamp)) {
                        String errorMessage = "Start timestamp and end timestamp should be in YYYY-MM-DD HH:MM:SS" +
                                " format";
                        RestApiUtil.handleBadRequest(errorMessage, log);
                    }
                }

                switch (statType) {
                    case "getSubscriptionCountsPerApp":
                        statSummaryDTO = getSubscriptionCountsPerApp(appProvider, providerName, startTimeStamp,
                                                                     endTimeStamp);
                        break;
                    case "getSubscriptionsPerApp":
                        statSummaryDTO = getSubscriptionsPerApp(appProvider, startTimeStamp, endTimeStamp);
                        break;
                    case "getAppUsagePerUser":
                        statSummaryDTO = getAppUsagePerUser(providerName, username, tenantDomainName, startTimeStamp,
                                                            endTimeStamp);
                        break;
                    case "getAppResponseTime":
                        statSummaryDTO = getAppResponseTime(providerName, username, tenantDomainName, startTimeStamp,
                                                            endTimeStamp, limit);
                        break;
                    case "getAppUsagePerPage":
                        statSummaryDTO = getAppUsagePerPage(providerName, username, tenantDomainName, startTimeStamp,
                                                            endTimeStamp);
                        break;
                    default:
                        RestApiUtil.handleBadRequest("Unsupported statistics type '" + statType + "' has provided",
                                                     log);
                }
            } else {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }
        } catch (AppManagementException e) {
            String errorMessage = "Error occurred while retrieving statistics details for " + statType;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(statSummaryDTO).build();
    }

    private boolean isTimeStampValid(String timeStamp)
    {
        SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            format.parse(timeStamp);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private StatSummaryDTO getAppUsagePerUser(String providerName, String userName, String tenantDomainName, String
            startTimeStamp, String endTimeStamp) {
        StatSummaryDTO statSummaryDTO = new StatSummaryDTO();
        try {
            AppUsageStatisticsService appUsageStatisticsService = new
                    AppUsageStatisticsService(userName);
            List<AppUsageByUserDTO> appUsageByUserList = appUsageStatisticsService.getAppUsageByUser
                    (providerName, startTimeStamp, endTimeStamp, tenantDomainName);
            List<Object> appObjectList = new ArrayList<>();
            for (AppUsageByUserDTO appUsageByUser : appUsageByUserList) {
                appObjectList.add(appUsageByUser);
            }
            statSummaryDTO.setResult(appObjectList);
        } catch (AppUsageQueryServiceClientException e) {
            String errorMessage = "Error occurred while retrieving statistics of app usage per users for the period " +
                    startTimeStamp + "to " + endTimeStamp;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return statSummaryDTO;
    }

    private StatSummaryDTO getAppUsagePerPage(String providerName, String userName, String
            tenantDomainName, String startTimeStamp, String endTimeStamp) {
        StatSummaryDTO statSummaryDTO = new StatSummaryDTO();
        try {
            AppUsageStatisticsService appUsageStatisticsService = new
                    AppUsageStatisticsService(userName);
            List<AppPageUsageDTO> appUsageByPageList = appUsageStatisticsService.getAppUsageByPage(providerName,
                                                                                                   startTimeStamp,
                                                                                                   endTimeStamp,
                                                                                                   tenantDomainName);
            List<Object> appObjectList = new ArrayList<>();
            for (AppPageUsageDTO appUsageByPage : appUsageByPageList) {
                appObjectList.add(appUsageByPage);
            }
            statSummaryDTO.setResult(appObjectList);
        } catch (AppUsageQueryServiceClientException e) {
            String errorMessage = "Error occurred while retrieving statistics of app usage per page for the period " +
                    startTimeStamp + "to " + endTimeStamp;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return statSummaryDTO;
    }

    private StatSummaryDTO getAppResponseTime(String providerName, String userName, String
            tenantDomainName, String startTimeStamp, String endTimeStamp, int limit) {
        StatSummaryDTO statSummaryDTO = new StatSummaryDTO();
        try {
            AppUsageStatisticsService appUsageStatisticsService = new
                    AppUsageStatisticsService(userName);
            List<AppResponseTimeDTO> appResponseTimeList = appUsageStatisticsService.getResponseTimesByApps
                    (providerName, startTimeStamp, endTimeStamp, limit, tenantDomainName);
            List<Object> appObjectList = new ArrayList<>();
            for (AppResponseTimeDTO appResponseTime : appResponseTimeList) {
                appObjectList.add(appResponseTime);
            }
            statSummaryDTO.setResult(appObjectList);
        } catch (AppUsageQueryServiceClientException e) {
            String errorMessage = "Error occurred while retrieving statistics of app response time for the period "
                    + startTimeStamp + "to " + endTimeStamp;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return statSummaryDTO;
    }

    private StatSummaryDTO getSubscriptionCountsPerApp(APIProvider appProvider, String providerName, String
            startTimeStamp, String endTimeStamp) {
        StatSummaryDTO statSummaryDTO = new StatSummaryDTO();
        try {
            Boolean isSubscriptionEnabled = RestApiUtil.isSubscriptionEnable();
            Map<String, Long> subscriptionCountMap = appProvider.getSubscriptionCountByAPPs(providerName,
                                                            startTimeStamp, endTimeStamp, isSubscriptionEnabled);
            if (subscriptionCountMap != null) {
                List<Object> appObjectList = new ArrayList<>();
                for (String key : subscriptionCountMap.keySet()) {
                    SubscriptionCount subscription = new SubscriptionCount();
                    // Key contains appName + "/" + appVersion + "&" + appuuid;
                    String appName = key.split("/")[0];
                    String appVersionWithUuid = key.split("/")[1];
                    String appVersion = appVersionWithUuid.split("&")[0];
                    String appId = appVersionWithUuid.split("&")[1];
                    subscription.setAppId(appId);
                    subscription.setAppName(appName);
                    subscription.setAppVersion(appVersion);
                    Long subscriptionCount = subscriptionCountMap.get(key);
                    subscription.setSubscriptionCount(subscriptionCount);
                    appObjectList.add(subscription);
                }
                statSummaryDTO.setResult(appObjectList);
            }
        } catch (AppManagementException e) {
            String errorMessage = "Error occurred while retrieving statistics of subscribers count per apps for the " +
                    "period " + startTimeStamp + "to " + endTimeStamp;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return statSummaryDTO;
    }

    private StatSummaryDTO getSubscriptionsPerApp(APIProvider appProvider, String startTimeStamp,
                                                  String endTimeStamp) {
        StatSummaryDTO statSummaryDTO = new StatSummaryDTO();
        try {
            Map<String, List> subscribedAppsByUserMap = appProvider.getSubscribedAPPsByUsers(startTimeStamp,
                                                                                           endTimeStamp);
            if (subscribedAppsByUserMap != null) {
                List<Object> appObjectList = new ArrayList<>();
                for (String key : subscribedAppsByUserMap.keySet()) {
                    Subscriptions subscriptions = new Subscriptions();
                    // Key contains appName + "/" + appVersion
                    String appName = key.split("/")[0];
                    String appVersion = key.split("/")[1];
                    subscriptions.setAppName(appName);
                    subscriptions.setAppVersion(appVersion);
                    List<Subscriber> subscriptionList = subscribedAppsByUserMap.get(key);
                    subscriptions.setSubscribersList(subscriptionList);
                    appObjectList.add(subscriptions);
                }
                statSummaryDTO.setResult(appObjectList);
            }
        } catch (AppManagementException e) {
            String errorMessage = "Error occurred while retrieving statistics of subscriptions per app for the period" +
                    " " + startTimeStamp + "to " + endTimeStamp;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return statSummaryDTO;
    }

    @Override
    public Response appsAppTypeTagsGet(String appType, String accept, String ifNoneMatch) {
        Set<Tag> tagSet = new HashSet<>();
        try {
            if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType) || AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                tagSet = appProvider.getAllTags(appType);
                if (tagSet.isEmpty()) {
                    return RestApiUtil.buildNotFoundException("Tags", null).getResponse();
                }
            } else {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }
        } catch (AppManagementException e) {
            String errorMessage = "Error retrieving tags for " + appType + "s.";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);

        }
        return Response.ok().entity(tagSet).build();
    }

    /**
     * Validate webapp context
     *
     * @param appType         application type
     * @param appContext      context of the webapp
     * @param contentType
     * @param ifModifiedSince
     * @return whether context is valid or not
     */
    @Override
    public Response appsAppTypeValidateContextPost(String appType, String appContext, String contentType,
                                                   String ifModifiedSince) {
        boolean isContextExists = false;
        Map<String, Boolean> responseMap = new HashMap<>();
        try {
            if (AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                if (StringUtils.isEmpty(appContext)) {
                    RestApiUtil.handleBadRequest("Webapp context is not provided", log);
                }

                if (appContext.indexOf("/") != 0) {
                    appContext = "/" + appContext;
                }
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                isContextExists = appProvider.isContextExist(appContext);
                responseMap.put("IsContextExists", isContextExists);
            } else {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }
        } catch (AppManagementException e) {
            String errorMessage = "Error retrieving tags for " + appType + "s.";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(responseMap).build();
    }

    @Override
    public Response appsAppTypeNameAppNameVersionVersionUuidGet(String appType, String appName, String version,
                                                                String accept, String ifNoneMatch) {
        AppDTO appDTO = new AppDTO();
        if (AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
            try {
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                        RestApiUtil.getLoggedInUserTenantDomain());

                String uuid = appProvider.getAppUUIDbyName(appName, version, tenantId);
                if (log.isDebugEnabled()) {
                    log.debug("UUID of the app: " + appName + ", version: " + version + " is " + uuid);
                }
                appDTO.setId(uuid);
            } catch (AppManagementException e) {
                String errorMessage = "Error while retrieving UUID for app: " + appName + " and version: " + version;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            } catch (UserStoreException e) {
                String errorMessage = "Error while retrieving tenant details";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }

        } else {
            RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
        }
        return Response.ok().entity(appDTO).build();
    }

    //remove artifact from registry
    private void removeRegistryArtifact(App webApp, String username)
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

    private String getHistoryPath(GenericArtifact genericArtifact) throws GovernanceException {
        String assetPath = genericArtifact.getPath();
        //Replace the / in the assetPath
        String partialHistoryPath = assetPath.replace("/", "_");
        String fullPath = RestApiConstants.HISTORY_PATH + "__system_governance" + partialHistoryPath;
        return fullPath;
    }
}
