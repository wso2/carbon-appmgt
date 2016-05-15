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
import org.wso2.carbon.appmgt.api.model.*;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.DefaultAppRepository;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.rest.api.publisher.AppsApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.*;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.RestApiPublisherUtils;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.mappings.APPMappingUtil;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.validation.AppDTOValidator;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.appmgt.rest.api.util.validation.BeanValidator;
import org.wso2.carbon.appmgt.rest.api.util.validation.CommonValidator;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.mobile.utils.utilities.ZipFileReading;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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

                    String fileExtension =
                            FilenameUtils.getExtension(fileDetail.getContentDisposition().getParameter("filename"));
                    if (AppMConstants.MOBILE_APPS_ANDROID_EXT.equals(fileExtension) ||
                            AppMConstants.MOBILE_APPS_IOS_EXT.equals(fileExtension)) {

                        //Generate UUID for the uploading file
                        String filename = RestApiPublisherUtils.generateBinaryUUID() + "." + fileExtension;
                        String filePath = RestApiPublisherUtils.uploadFileIntoStorage(fileInputStream, filename);
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
        File staticContentFile = null;
        String contentType = null;
        try {
            String fileExtension = FilenameUtils.getExtension(fileName);
            if (AppMConstants.MOBILE_APPS_ANDROID_EXT.equals(fileExtension) ||
                    AppMConstants.MOBILE_APPS_IOS_EXT.equals(fileExtension)) {

                staticContentFile = RestApiUtil.readFileFromStorage(fileName);

                contentType = RestApiUtil.readFileContentType(staticContentFile.getAbsolutePath());
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
        Response.ResponseBuilder response = Response.ok((Object) staticContentFile);
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
        Map<String,String> response = new HashMap<>();
        try {
            if (fileInputStream != null) {
                if ("image".equals(fileDetail.getContentType().getType())) {
                    String fileExtension = FilenameUtils.getExtension(fileDetail.getContentDisposition().getParameter(
                            RestApiConstants.CONTENT_DISPOSITION_FILENAME));
                    String filename = RestApiPublisherUtils.generateBinaryUUID() + "." + fileExtension;
                    if(AppMConstants.MOBILE_ASSET_TYPE.equals(appType)) {
                        RestApiPublisherUtils.uploadFileIntoStorage(fileInputStream, filename);
                        response.put("id", filename);
                    }else if(AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)){
                        File tempFile = null;
                        try {
                            tempFile = File.createTempFile("temp", ".tmp");
                            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                                IOUtils.copy(fileInputStream, outputStream);
                            }

                        DefaultAppRepository defaultAppRepository = new DefaultAppRepository();
                        UUID contentUUID = UUID.randomUUID();
                        defaultAppRepository.storeStaticContents(contentUUID.toString(), filename, (int) tempFile.length(),
                                fileDetail.getContentType().getType(), fileInputStream);
                            response.put("id", contentUUID.toString());
                        } catch (IOException e) {
                            RestApiUtil.handleInternalServerError("Error occurred while uploading static content", e, log);
                        }finally {
                            tempFile.delete();
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
    public Response appsStaticContentsFileNameGet(String fileName, String ifMatch, String ifUnmodifiedSince) {
        try {
            File staticContentFile = RestApiUtil.readFileFromStorage(fileName);
            String contentType = RestApiUtil.readFileContentType(staticContentFile.getAbsolutePath());
            if (!contentType.startsWith("image")) {
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

            List<App> result = apiProvider.searchApps(appType, RestApiUtil.getSearchTerms(query));


            if (result.isEmpty()) {
                String errorMessage = "No result found.";
                return RestApiUtil.buildNotFoundException(errorMessage, null).getResponse();
            }
            appListDTO = APPMappingUtil.fromAPIListToDTO(result, offset, limit);
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

            responseMessageDTO.setMessage("Lifecycle status to be changed : " + action);
        } catch (AppManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
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
            if (!appType.equalsIgnoreCase(AppMConstants.MOBILE_ASSET_TYPE)) {
                String errorMessage = "Invalid Asset Type : " + appType;
                RestApiUtil.handleBadRequest(errorMessage, log);
            }

            Map<String, String> searchTerms = new HashMap<String, String>();
            searchTerms.put("id", appId);

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            List<App> result = apiProvider.searchApps(appType, searchTerms);

            if (result.isEmpty()) {
                String errorMessage = "Could not find requested application.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }

            App app = result.get(0);
            appDTO = APPMappingUtil.fromAppToDTO(app);

            return Response.ok().entity(appDTO).build();
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
        } else {
            RestApiUtil.handleBadRequest("Invalid application type :" + appType, log);
        }
        return Response.accepted().build();
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
                String errorMessage = "Could not find requested application.";
                return RestApiUtil.buildNotFoundException(errorMessage, appId).getResponse();
            }

            App app = result.get(0);

            if (appType.equals(AppMConstants.WEBAPP_ASSET_TYPE)) {

                WebApp webApp = (WebApp) app;

                if (webApp.isAdvertiseOnly()) {
                    removeRegistryArtifact(webApp, username);
                } else {
                    apiProvider.deleteApp(webApp.getId(), webApp.getSsoProviderDetails());
                }
            } else if (appType.equals(AppMConstants.MOBILE_ASSET_TYPE)) {
                removeRegistryArtifact(app, username);
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
    public Response appsAppTypeIdAppIdDiscoverPost(String appType, String appId, String contentType,
                                                   String ifModifiedSince) {
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdDocsPost(String appType, String appId, InputStream fileInputStream,
                                               Attachment fileDetail, String ifMatch, String ifUnmodifiedSince) {
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdDocsFileNameGet(String appType, String appId, String fileName, String ifMatch,
                                                      String ifUnmodifiedSince) {
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdDocsFileNameDelete(String appType, String appId, String fileName, String ifMatch,
                                                         String ifUnmodifiedSince) {
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
                RestApiUtil.handleBadRequest("Invalid App Id.", log);
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
                RestApiUtil.handleBadRequest("Invalid App Id.", log);
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
                lifeCycleHistoryDTO.setState(lifecycleHistoryStateObj.get("state").toString());
                lifeCycleHistoryDTO.setTargetState(lifecycleHistoryStateObj.get("targetState").toString());
                lifeCycleHistoryDTO.setTimestamp(lifecycleHistoryStateObj.get("timestamp").toString());
                lifeCycleHistoryDTO.setUser(lifecycleHistoryStateObj.get("user").toString());
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
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
            } else {
                String errorMessage = "Error while changing lifecycle state of app with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }

        }
        return Response.ok().entity(userIdListDTO).build();
    }

    @Override
    public Response appsAppTypeIdAppIdTagsGet(String appType, String appId, String accept, String ifNoneMatch) {
        List<String> tags = new ArrayList<>();
        try {
            if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType) || AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {

                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                for (Tag tag : appProvider.getAllTags(appType, appId)) {
                    tags.add(tag.getName());
                }
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
        return Response.ok().entity(tags).build();
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
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
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
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, appId, e, log);
            } else {
                String errorMessage = "Error while deleting tags from " + appType + " with id : " + appId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return Response.ok().build();
    }

    @Override
    public Response appsAppTypeIdAppIdThrottlingtiersGet(String appType, String appId, String accept,
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
    public Response appsAppTypeStatsStatTypeGet(String appType, String statType, String accept, String ifNoneMatch) {
        return null;
    }

    @Override
    public Response appsAppTypeTagsGet(String appType, String accept, String ifNoneMatch) {
        Set<Tag> tagSet = new HashSet<>();
        try {
            if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType) || AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
                APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
                tagSet = appProvider.getAllTags(appType);
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
