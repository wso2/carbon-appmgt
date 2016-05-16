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

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.appmgt.api.APIConsumer;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.App;
import org.wso2.carbon.appmgt.api.model.Tag;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.mobile.store.Operations;
import org.wso2.carbon.appmgt.mobile.utils.MobileApplicationException;
import org.wso2.carbon.appmgt.rest.api.store.AppsApiService;
import org.wso2.carbon.appmgt.rest.api.store.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.AppListDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.AppRatingInfoDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.AppRatingListDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.EventsDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.FavouritePageDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.InstallDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.TagListDTO;
import org.wso2.carbon.appmgt.rest.api.store.utils.mappings.APPMappingUtil;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.appmgt.rest.api.util.validation.BeanValidator;
import org.wso2.carbon.appmgt.usage.publisher.APPMgtUiActivitiesBamDataPublisher;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.social.core.SocialActivityException;
import org.wso2.carbon.social.core.service.SocialActivityService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AppsApiServiceImpl extends AppsApiService {

    private static final Log log = LogFactory.getLog(AppsApiServiceImpl.class);
    BeanValidator beanValidator;

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

            appProvider.subscribeMobileApp(username, appId);
            mobileOperation.performAction(user.toString(), action, tenantId, appId, install.getType(), parameters);

        } catch (AppManagementException | MobileApplicationException e) {
            RestApiUtil.handleInternalServerError("Internal Error occurred while installing", e, log);
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("User store related Error occurred while installing", e, log);
        } catch (JSONException e) {
            RestApiUtil.handleInternalServerError("Json casting Error occurred while installing", e, log);
        }
        return Response.ok().build();

    }

    @Override
    public Response appsEventPublishPost(EventsDTO events, String contentType) {
        beanValidator = new BeanValidator();
        //Validate common mandatory fields for mobile and webapp
        beanValidator.validate(events);

        if (events.getEvents().size() == 0) {
            RestApiUtil.handleBadRequest("Invalid event stream", log);
        }
        APPMgtUiActivitiesBamDataPublisher appMgtBAMPublishObj = new APPMgtUiActivitiesBamDataPublisher();
        //Pass data to java class to save
        appMgtBAMPublishObj.processUiActivityObject(events.getEvents().toArray());
        return Response.accepted().build();
    }

    @Override
    public Response appsFavouritePageGet(String accept, String ifNoneMatch) {
        FavouritePageDTO favouritePageDTO = new FavouritePageDTO();
        boolean isTenantFlowStarted = false;
        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomainOfUser = RestApiUtil.getLoggedInUserTenantDomain();
            int tenantIdOfUser = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainOfUser);
            if (tenantDomainOfUser != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(
                    tenantDomainOfUser)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomainOfUser, true);
            }
            int tenantIdOFStore = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            boolean hasFavouritePage = apiConsumer.hasFavouritePage(username, tenantIdOfUser, tenantIdOFStore);
            favouritePageDTO.setIsDefaultPage(hasFavouritePage);
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("User Store Error occurred while retrieving Favourite page details",
                                                  e, log);
        } catch (AppManagementException e) {
            RestApiUtil.handleInternalServerError("Internal Error occurred while retrieving Favourite page details", e,
                                                  log);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return Response.ok().entity(favouritePageDTO).build();
    }

    @Override
    public Response appsFavouritePagePost() {
        boolean isTenantFlowStarted = false;
        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomainOfUser = RestApiUtil.getLoggedInUserTenantDomain();
            int tenantIdOfUser = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainOfUser);
            if (tenantDomainOfUser != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(
                    tenantDomainOfUser)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomainOfUser, true);
            }
            int tenantIdOFStore = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            apiConsumer.setFavouritePage(username, tenantIdOfUser, tenantIdOFStore);
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("User Store Error occurred while saving Favourite page", e, log);
        } catch (AppManagementException e) {
            RestApiUtil.handleInternalServerError("Internal Error occurred while saving Favourite page", e, log);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return Response.ok().build();
    }

    @Override
    public Response appsFavouritePageDelete() {
        boolean isTenantFlowStarted = false;
        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomainOfUser = RestApiUtil.getLoggedInUserTenantDomain();
            int tenantIdOfUser = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainOfUser);
            if (tenantDomainOfUser != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(
                    tenantDomainOfUser)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomainOfUser, true);
            }
            int tenantIdOFStore = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            apiConsumer.removeFavouritePage(username, tenantIdOfUser, tenantIdOFStore);
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("User Store Error occurred while removing Favourite page", e, log);
        } catch (AppManagementException e) {
            RestApiUtil.handleInternalServerError("Internal Error occurred while removing Favourite page", e, log);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
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
        } catch (AppManagementException | MobileApplicationException e) {
            RestApiUtil.handleInternalServerError("Internal Error occurred while uninstalling", e, log);
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("User Store related Error occurred while uninstalling", e, log);
        } catch (JSONException e) {
            RestApiUtil.handleInternalServerError("JSON casting related Error occurred while uninstalling", e, log);
        }

        return Response.ok().build();
    }

    @Override
    public Response appsAppTypeGet(String appType, String query, String fieldFilter, Integer limit, Integer offset,
                                   String accept, String ifNoneMatch) {
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
            if (appListDTO.getCount() == 0) {
                String errorMessage = "No result found.";
                return RestApiUtil.buildNotFoundException(errorMessage, null).getResponse();
            }

            APPMappingUtil.setPaginationParams(appListDTO, query, offset, limit, result.size());
            return Response.ok().entity(appListDTO).build();
        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving Apps";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }


    @Override
    public Response appsAppTypeIdAppIdGet(String appType, String appId, String accept, String ifNoneMatch,
                                          String ifModifiedSince) {
        AppDTO appToReturn = null;
        try {
            //currently supports only mobile apps
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
                return RestApiUtil.buildNotFoundException(errorMessage, appId).getResponse();
            }
            appToReturn = APPMappingUtil.fromAppToDTO(result.get(0));
            if (appToReturn == null) {
                String errorMessage = "Could not find requested application.";
                return RestApiUtil.buildNotFoundException(errorMessage, appId).getResponse();
            }

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

    @Override
    public Response appsAppTypeIdAppIdDocsFileNameGet(String appType, String appId, String fileName, String ifMatch,
                                                      String ifUnmodifiedSince) {
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdFavouriteAppPost(String appType, String appId, String contentType) {
        boolean isTenantFlowStarted = false;
        try {
            //check if a valid asset type is provided. Currently support only webapps
            if (!appType.equalsIgnoreCase(AppMConstants.APP_TYPE)) {
                String errorMessage = "Invalid Asset Type : " + appType;
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            //logged user's username
            String username = RestApiUtil.getLoggedInUsername();
            //logged user's tenant domain
            String tenantDomainOfUser = RestApiUtil.getLoggedInUserTenantDomain();
            //logged user's tenant id
            int tenantIdOfUser = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainOfUser);
            if (tenantDomainOfUser != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(
                    tenantDomainOfUser)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomainOfUser, true);
            }
            //tenant id of the store
            int tenantIdOFStore = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            Map<String, String> searchTerms = new HashMap<String, String>();
            searchTerms.put("id", appId);
            //get app details by id
            List<App> result = apiProvider.searchApps(appType, searchTerms);
            //check if it's valid app
            if (result.isEmpty() || result.size() == 0) {
                String errorMessage = "Could not find requested application.";
                return RestApiUtil.buildNotFoundException(errorMessage, appId).getResponse();
            }
            AppDTO appDTO = APPMappingUtil.fromAppToDTO(result.get(0));
            if (appDTO == null) {
                String errorMessage = "Could not find requested application.";
                return RestApiUtil.buildNotFoundException(errorMessage, appId).getResponse();
            }
            String providerName = appDTO.getProvider();
            String apiName = appDTO.getName();
            String version = appDTO.getVersion();

            APIIdentifier identifier = new APIIdentifier(providerName, apiName, version);
            boolean isFavouriteApp = apiConsumer.isFavouriteApp(identifier, username, tenantIdOfUser, tenantIdOFStore);

            //add to favourite if it is not already added
            if (!isFavouriteApp) {
                apiConsumer.addToFavouriteApps(identifier, username, tenantIdOfUser, tenantIdOFStore);
            }
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("User Store Error occurred while adding as Favourite", e, log);
        } catch (AppManagementException e) {
            RestApiUtil.handleInternalServerError("Internal Error occurred while adding as Favourite", e, log);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return Response.ok().build();
    }

    @Override
    public Response appsAppTypeIdAppIdFavouriteAppDelete(String appType, String appId, String contentType) {
        boolean isTenantFlowStarted = false;
        try {
            //check if a valid asset type is provided. Currently support only webapps
            if (!appType.equalsIgnoreCase(AppMConstants.APP_TYPE)) {
                String errorMessage = "Invalid Asset Type : " + appType;
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            //logged user's username
            String username = RestApiUtil.getLoggedInUsername();
            //logged user's tenant domain
            String tenantDomainOfUser = RestApiUtil.getLoggedInUserTenantDomain();
            //logged user's tenant id
            int tenantIdOfUser = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainOfUser);
            if (tenantDomainOfUser != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(
                    tenantDomainOfUser)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomainOfUser, true);
            }
            //tenant id of the store
            int tenantIdOFStore = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            Map<String, String> searchTerms = new HashMap<String, String>();
            searchTerms.put("id", appId);
            //get app details by id
            List<App> result = apiProvider.searchApps(appType, searchTerms);
            //check if it's valid app
            if (result.isEmpty() || result.size() == 0) {
                String errorMessage = "Could not find requested application.";
                return RestApiUtil.buildNotFoundException(errorMessage, appId).getResponse();
            }
            AppDTO appDTO = APPMappingUtil.fromAppToDTO(result.get(0));
            if (appDTO == null) {
                String errorMessage = "Could not find requested application.";
                return RestApiUtil.buildNotFoundException(errorMessage, appId).getResponse();
            }
            String providerName = appDTO.getProvider();
            String apiName = appDTO.getName();
            String version = appDTO.getVersion();

            APIIdentifier identifier = new APIIdentifier(providerName, apiName, version);
            boolean isFavouriteApp = apiConsumer.isFavouriteApp(identifier, username, tenantIdOfUser, tenantIdOFStore);

            //remove from favourite if is it a favourite app
            if (isFavouriteApp) {
                apiConsumer.removeFromFavouriteApps(identifier, username, tenantIdOfUser, tenantIdOFStore);
            }
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("User Store Error occurred while adding as Favourite", e, log);
        } catch (AppManagementException e) {
            RestApiUtil.handleInternalServerError("Internal Error occurred while adding as Favourite", e, log);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return Response.ok().build();
    }

    @Override
    public Response appsAppTypeIdAppIdRateGet(String appType, String appId, Integer limit, Integer offset,
                                              String accept, String ifNoneMatch, String ifModifiedSince) {
        AppRatingListDTO appRatingListDTO = new AppRatingListDTO();
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        try {
            //check App Type validity
            if ((AppMConstants.MOBILE_ASSET_TYPE.equalsIgnoreCase(appType) ||
                    AppMConstants.WEBAPP_ASSET_TYPE.equalsIgnoreCase(appType) ||
                    AppMConstants.SITE_ASSET_TYPE.equalsIgnoreCase(appType)) == false) {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }

            //check App Id validity
            Map<String, String> searchTerms = new HashMap<String, String>();
            searchTerms.put("id", appId);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            List<App> result = apiProvider.searchApps(appType, searchTerms);
            if (result.isEmpty()) {
                String errorMessage = "Could not find requested application.";
                return RestApiUtil.buildNotFoundException(errorMessage, appId).getResponse();
            }

            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            SocialActivityService socialActivityService = (SocialActivityService) carbonContext.getOSGiService(
                    org.wso2.carbon.social.core.service.SocialActivityService.class, null);
            JsonObject rating = socialActivityService.getRating(appType + ":" + appId);
            if (rating != null && rating.get("rating") != null) {
                appRatingListDTO.setOverallRating(rating.get("rating").getAsBigDecimal());

                JSONObject socialObj;
                socialObj = new JSONObject(socialActivityService.getSocialObjectJson(appType + ":" + appId, "asc",
                                                                                     offset, limit));
                org.json.JSONArray socialArr = socialObj.getJSONArray("attachments");
                List<AppRatingInfoDTO> appRatingInfoDTOList = new ArrayList<>();
                for (int i = 0; i < socialArr.length(); i++) {
                    AppRatingInfoDTO appRatingInfoDTO = new AppRatingInfoDTO();
                    JSONObject ratingObj = (JSONObject) ((JSONObject) socialArr.get(i)).get("object");
                    appRatingInfoDTO.setRating(Integer.parseInt(ratingObj.get("rating").toString()));
                    appRatingInfoDTO.setId(Integer.parseInt(ratingObj.get("id").toString()));
                    appRatingInfoDTO.setReview(ratingObj.get("content").toString());
                    appRatingInfoDTO.setLikes(Integer.parseInt(((JSONObject) (ratingObj.get("likes"))).get("totalItems")
                                                                       .toString()));
                    appRatingInfoDTO.setDislikes(Integer.parseInt(((JSONObject) (ratingObj.get("dislikes"))).get(
                            "totalItems").toString()));
                    appRatingInfoDTOList.add(appRatingInfoDTO);
                }
                int totalRecords = rating.get("count").getAsInt();
                APPMappingUtil.setAppRatingPaginationParams(appRatingListDTO, offset, limit, totalRecords);
                appRatingListDTO.setRatingDetails(appRatingInfoDTOList);
                appRatingListDTO.setCount(appRatingInfoDTOList.size());
            } else {
                return RestApiUtil.buildNotFoundException("Rating", appId).getResponse();
            }
        } catch (SocialActivityException e) {
            String errorMessage = String.format("Can't get the rating for the app '%s:%s'", appType, appId);
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (AppManagementException e) {
            String errorMessage = String.format("Internal error while retrieving the rating for the app '%s:%s'",
                                                appType, appId);
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (JSONException e) {
            String errorMessage = String.format(
                    "JSONException occurred while casting. Can't get the rating for the app '%s:%s'", appType, appId);
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(appRatingListDTO).build();
    }

    @Override
    public Response appsAppTypeIdAppIdRatePut(String appType, String appId, AppRatingInfoDTO rating, String contentType,
                                              String ifMatch, String ifUnmodifiedSince) {
        AppRatingInfoDTO appRatingInfoDTO = new AppRatingInfoDTO();
        try {
            //check App Type validity
            if ((AppMConstants.MOBILE_ASSET_TYPE.equalsIgnoreCase(appType) ||
                    AppMConstants.WEBAPP_ASSET_TYPE.equalsIgnoreCase(appType) ||
                    AppMConstants.SITE_ASSET_TYPE.equalsIgnoreCase(appType)) == false) {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }

            //check App Id validity
            Map<String, String> searchTerms = new HashMap<String, String>();
            searchTerms.put("id", appId);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            List<App> result = apiProvider.searchApps(appType, searchTerms);
            if (result.isEmpty()) {
                String errorMessage = "Could not find requested application.";
                return RestApiUtil.buildNotFoundException(errorMessage, appId).getResponse();
            }
            String tenantUserName = RestApiUtil.getLoggedInUsername() + "@" + RestApiUtil.getLoggedInUserTenantDomain();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            SocialActivityService socialActivityService = (SocialActivityService) carbonContext.getOSGiService(
                    org.wso2.carbon.social.core.service.SocialActivityService.class, null);
            String activity =
                    "{\"verb\":\"post\",\"object\":{\"objectType\":\"review\",\"content\":" + rating.getReview() + "," +
                            "\"rating\":" + rating.getRating() +
                            ",\"likes\":{\"totalItems\":0},\"dislikes\":{\"totalItems\":0}}," + "\"target\":{\"id\":" +
                            "\"" + appType + ":" + appId + "\"" + "},\"actor\":{\"id\":" + tenantUserName + "\"," +
                            "objectType\":\"person\"}}";


            long id = socialActivityService.publish(activity);
            appRatingInfoDTO.setId((int) id);
            appRatingInfoDTO.setRating(rating.getRating());
            appRatingInfoDTO.setReview(rating.getReview());

        } catch (AppManagementException e) {
            String errorMessage = String.format("Internal error while saving the rating for the app '%s:%s'",
                                                appType, appId);
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (SocialActivityException e) {
            String errorMessage = String.format("Social component error while saving the rating for the app '%s:%s'",
                                                appType, appId);
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(appRatingInfoDTO).build();
    }


    @Override
    public Response appsAppTypeIdAppIdStorageFileNameGet(String appType, String appId, String fileName, String ifMatch,
                                                         String ifUnmodifiedSince) {
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdSubscriptionGet(String appType, String appId, String accept, String ifNoneMatch,
                                                      String ifModifiedSince) {
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdSubscriptionPost(String appType, String appId, String contentType) {
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdSubscriptionWorkflowPost(String appType, String appId, String contentType) {
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdSubscriptionUsersGet(String appType, String appId, String accept,
                                                           String ifNoneMatch, String ifModifiedSince) {
        return null;
    }

    @Override
    public Response appsAppTypeIdAppIdUnsubscriptionPost(String appType, String appId, String contentType) {
        return null;
    }


    @Override
    public Response appsAppTypeTagsGet(String appType, String accept, String ifNoneMatch) {
        TagListDTO tagListDTO = new TagListDTO();
        try {
            if ((AppMConstants.MOBILE_ASSET_TYPE.equalsIgnoreCase(appType) ||
                    AppMConstants.WEBAPP_ASSET_TYPE.equalsIgnoreCase(appType) ||
                    AppMConstants.SITE_ASSET_TYPE.equalsIgnoreCase(appType)) == false) {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }
            APIConsumer appConsumer = RestApiUtil.getLoggedInUserConsumer();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            Map<String, String> attributeMap = new HashMap<>();
            if (AppMConstants.MOBILE_ASSET_TYPE.equalsIgnoreCase(appType)) {
                attributeMap.put(AppMConstants.APP_OVERVIEW_TREAT_AS_A_SITE, "true");
            }

            List<String> tagList = new ArrayList<>();
            Iterator tagIterator = appConsumer.getAllTags(tenantDomain, appType, attributeMap).iterator();
            if (!tagIterator.hasNext()) {
                return RestApiUtil.buildNotFoundException("Tags", null).getResponse();
            }
            while (tagIterator.hasNext()) {
                Tag tag = (Tag) tagIterator.next();
                tagList.add(tag.getName());
            }
            tagListDTO.setTags(tagList);
        } catch (AppManagementException e) {
            String errorMessage = "Error retrieving tags for " + appType + "s.";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(tagListDTO).build();
    }


}
