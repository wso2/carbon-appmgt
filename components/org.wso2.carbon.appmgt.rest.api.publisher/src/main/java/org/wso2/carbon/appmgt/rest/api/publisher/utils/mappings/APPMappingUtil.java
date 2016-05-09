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

package org.wso2.carbon.appmgt.rest.api.publisher.utils.mappings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.*;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppAppmetaDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppInfoDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppListDTO;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

import java.math.BigDecimal;
import java.util.*;

public class APPMappingUtil {

    private static final Log log = LogFactory.getLog(APPMappingUtil.class);

    /**
     * Converts a List object of APIs into a DTO
     *
     * @param appList List of Apps
     * @param limit   maximum number of APIs returns
     * @param offset  starting index
     * @return APIListDTO object containing AppInfoDTOs
     */
    public static AppListDTO fromAPIListToDTO(List<App> appList, int offset, int limit) {
        AppListDTO appListDTO = new AppListDTO();
        List<AppInfoDTO> appInfoDTOs = appListDTO.getList();
        if (appInfoDTOs == null) {
            appInfoDTOs = new ArrayList<>();
            appListDTO.setList(appInfoDTOs);
        }

        //add the required range of objects to be returned
        int start = offset < appList.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= appList.size() - 1 ? offset + limit - 1 : appList.size() - 1;
        for (int i = start; i <= end; i++) {
            appInfoDTOs.add(fromAppToInfoDTO(appList.get(i)));
        }
        appListDTO.setCount(appInfoDTOs.size());
        return appListDTO;
    }

    /**
     * Creates a minimal DTO representation of an API object
     *
     * @param app WebApp object
     * @return a minimal representation DTO
     */
    public static AppInfoDTO fromAPIToInfoDTO(WebApp app) {
        AppInfoDTO appInfoDTO = new AppInfoDTO();
        appInfoDTO.setDescription(app.getDescription());
        String context = app.getContext();
        if (context != null) {
            if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
                context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
            }
            appInfoDTO.setContext(context);
        }
        appInfoDTO.setId(app.getUUID());
        APIIdentifier apiId = app.getId();
        appInfoDTO.setName(apiId.getApiName());
        appInfoDTO.setVersion(apiId.getVersion());
        String providerName = app.getId().getProviderName();
        appInfoDTO.setProvider(AppManagerUtil.replaceEmailDomainBack(providerName));
        appInfoDTO.setLifecycleState(app.getLifeCycleStatus().getStatus());
        return appInfoDTO;
    }

    public static AppInfoDTO fromAppToInfoDTO(App app){

        if(AppMConstants.WEBAPP_ASSET_TYPE.equals(app.getType())){
            return fromWebAppToInfoDTO((WebApp) app);
        }else if(AppMConstants.MOBILE_ASSET_TYPE.equals(app.getType())){
            return fromMobileAppToInfoDTO((MobileApp) app);
        }

        return null;
    }

    private static AppInfoDTO fromMobileAppToInfoDTO(MobileApp app) {

        AppInfoDTO appInfoDTO = new AppInfoDTO();
        appInfoDTO.setId(app.getUUID());
        appInfoDTO.setName(app.getAppName());
        appInfoDTO.setVersion(app.getVersion());
        appInfoDTO.setProvider(AppManagerUtil.replaceEmailDomainBack(app.getAppProvider()));
        appInfoDTO.setDescription(app.getDescription());
        appInfoDTO.setLifecycleState(app.getLifecycleStatus().getStatus());
        appInfoDTO.setRating(BigDecimal.valueOf(app.getRating()));
        return appInfoDTO;

    }

    private static AppInfoDTO fromWebAppToInfoDTO(WebApp app) {

        AppInfoDTO appInfoDTO = new AppInfoDTO();

        appInfoDTO.setId(app.getUUID());

        APIIdentifier apiId = app.getId();
        appInfoDTO.setName(apiId.getApiName());
        appInfoDTO.setVersion(apiId.getVersion());

        String providerName = app.getId().getProviderName();
        appInfoDTO.setProvider(AppManagerUtil.replaceEmailDomainBack(providerName));

        appInfoDTO.setDescription(app.getDescription());

        String context = app.getContext();
        if (context != null) {
            if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
                context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
            }
            appInfoDTO.setContext(context);
        }

        appInfoDTO.setLifecycleState(app.getLifeCycleStatus().getStatus());
        appInfoDTO.setRating(BigDecimal.valueOf(app.getRating()));
        return appInfoDTO;

    }


    /**
     * Sets pagination urls for a APIListDTO object given pagination parameters and url parameters
     *
     * @param appListDTO a APIListDTO object
     * @param query      search condition
     * @param limit      max number of objects returned
     * @param offset     starting index
     * @param size       max offset
     */
    public static void setPaginationParams(AppListDTO appListDTO, String query, int offset, int limit, int size) {

        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                                        paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                                        paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        appListDTO.setNext(paginatedNext);
        appListDTO.setPrevious(paginatedPrevious);
    }

    /**
     * Returns the WebApp given the uuid or the id in {provider}-{api}-{version} format
     *
     * @param appId uuid or the id in {provider}-{api}-{version} format
     * @return API which represents the given id
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static WebApp getAPIFromApiIdOrUUID(String appId)
            throws AppManagementException {
        //modify this method to support mobile apps
        APIProvider appProvider = RestApiUtil.getLoggedInUserProvider();
        if (RestApiUtil.isUUID(appId)) {
            WebApp webapp = appProvider.getAppDetailsFromUUID(appId);
            appId = webapp.getId().getProviderName() + "-" + webapp.getId().getApiName() + "-" +
                    webapp.getId().getVersion();
        }

        APIIdentifier appIdentifier = getAppIdentifierFromApiId(appId);
        WebApp webapp = appProvider.getAPI(appIdentifier);
        return webapp;
    }

    public static APIIdentifier getAppIdentifierFromApiId(String appID) {
        //if appID contains -AT-, that need to be replaced before splitting
        appID = AppManagerUtil.replaceEmailDomainBack(appID);
        String[] appIdDetails = appID.split(RestApiConstants.API_ID_DELIMITER);

        if (appIdDetails.length < 3) {
            RestApiUtil.handleBadRequest("Provided API identifier '" + appID + "' is invalid", log);
        }

        // appID format: provider-apiName-version
        String providerName = appIdDetails[0];
        String apiName = appIdDetails[1];
        String version = appIdDetails[2];
        String providerNameEmailReplaced = AppManagerUtil.replaceEmailDomain(providerName);
        return new APIIdentifier(providerNameEmailReplaced, apiName, version);
    }


    public static AppDTO fromAPItoDTO(WebApp model) throws AppManagementException {
        AppDTO dto = new AppDTO();
        dto.setName(model.getId().getApiName());
        dto.setVersion(model.getId().getVersion());
        String providerName = model.getId().getProviderName();
        dto.setProvider(AppManagerUtil.replaceEmailDomainBack(providerName));
        dto.setId(model.getUUID());
        String context = model.getContext();
        if (context != null) {
            if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
                context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
            }
            dto.setContext(context);
        }
        dto.setDescription(model.getDescription());
        dto.setIsDefaultVersion(model.isDefaultVersion());
        dto.setIsSite(model.getTreatAsASite());
        dto.setThumbnailUrl(model.getThumbnailUrl());
        dto.setLifecycleState(model.getLifeCycleStatus().getStatus());
        Set<String> apiTags = model.getTags();
        List<String> tagsToReturn = new ArrayList<>();
        tagsToReturn.addAll(apiTags);
        dto.setTags(tagsToReturn);
        Set<Tier> apiTiers = model.getAvailableTiers();
        List<String> tiersToReturn = new ArrayList<>();
        for (Tier tier : apiTiers) {
            tiersToReturn.add(tier.getName());
        }
        if (model.getTransports() != null) {
            dto.setTransport(Arrays.asList(model.getTransports().split(",")));
        }
        if (model.getVisibleRoles() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleRoles().split(",")));
        }
        if (model.getLifeCycleName() != null) {
            dto.setLifecycle(model.getLifeCycleName());
        }

        dto.setType(model.getType());

        dto.setDisplayName(model.getDisplayName());
        dto.setCreatedtime(model.getDisplayName());

        AppAppmetaDTO appAppmetaDTO = new AppAppmetaDTO();
        appAppmetaDTO.setPath(model.getPath());
        appAppmetaDTO.setVersion(model.getId().getVersion());
        dto.setAppmeta(appAppmetaDTO);

        dto.setMediaType(model.getMediaType());
        dto.setCreatedTime(model.getCreatedTime());


        return dto;
    }


    public static AppDTO fromAppToDTO(App app){

        if(AppMConstants.WEBAPP_ASSET_TYPE.equals(app.getType())){
            return fromWebAppToDTO((WebApp) app) ;
        }else if(AppMConstants.MOBILE_ASSET_TYPE.equals(app.getType())){
            return fromMobileAppToDTO((MobileApp) app);
        }

        return null;

    }

    private static AppDTO fromWebAppToDTO(WebApp webapp){

        AppDTO dto = new AppDTO();
        dto.setName(webapp.getId().getApiName());
        dto.setVersion(webapp.getId().getVersion());
        String providerName = webapp.getId().getProviderName();
        dto.setProvider(AppManagerUtil.replaceEmailDomainBack(providerName));
        dto.setId(webapp.getUUID());
        String context = webapp.getContext();
        if (context != null) {
            if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
                context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
            }
            dto.setContext(context);
        }
        dto.setDescription(webapp.getDescription());
        dto.setIsDefaultVersion(webapp.isDefaultVersion());
        dto.setIsSite(webapp.getTreatAsASite());
        dto.setThumbnailUrl(webapp.getThumbnailUrl());
        dto.setLifecycleState(webapp.getLifeCycleStatus().getStatus());
        dto.setRating(BigDecimal.valueOf(webapp.getRating()));
        Set<String> apiTags = webapp.getTags();
        List<String> tagsToReturn = new ArrayList<>();
        tagsToReturn.addAll(apiTags);
        dto.setTags(tagsToReturn);
        Set<Tier> apiTiers = webapp.getAvailableTiers();
        List<String> tiersToReturn = new ArrayList<>();
        for (Tier tier : apiTiers) {
            tiersToReturn.add(tier.getName());
        }
        if (webapp.getTransports() != null) {
            dto.setTransport(Arrays.asList(webapp.getTransports().split(",")));
        }
        if (webapp.getVisibleRoles() != null) {
            dto.setVisibleRoles(Arrays.asList(webapp.getVisibleRoles().split(",")));
        }
        if (webapp.getLifeCycleName() != null) {
            dto.setLifecycle(webapp.getLifeCycleName());
        }

        dto.setType(webapp.getType());

        dto.setDisplayName(webapp.getDisplayName());
        dto.setCreatedtime(webapp.getDisplayName());

        AppAppmetaDTO appAppmetaDTO = new AppAppmetaDTO();
        appAppmetaDTO.setPath(webapp.getPath());
        appAppmetaDTO.setVersion(webapp.getId().getVersion());
        dto.setAppmeta(appAppmetaDTO);

        dto.setMediaType(webapp.getMediaType());
        dto.setCreatedTime(webapp.getCreatedTime());


        return dto;


    }

    private static AppDTO fromMobileAppToDTO(MobileApp mobileApp){

        AppDTO dto = new AppDTO();

        dto.setId(mobileApp.getUUID());
        dto.setName(mobileApp.getAppName());
        dto.setVersion(mobileApp.getVersion());
        dto.setDescription(mobileApp.getDescription());
        dto.setRating(BigDecimal.valueOf(mobileApp.getRating()));

        Set<String> apiTags = mobileApp.getTags();
        List<String> tagsToReturn = new ArrayList<>();
        tagsToReturn.addAll(apiTags);
        dto.setTags(tagsToReturn);


        dto.setType(mobileApp.getType());
        dto.setMarketType(mobileApp.getMarketType());
        dto.setBundleversion(mobileApp.getBundleVersion());
        dto.setCategory(mobileApp.getCategory());
        dto.setDisplayName(mobileApp.getDisplayName());
        if (mobileApp.getScreenShots() != null) {
            dto.setScreenshots(mobileApp.getScreenShots());
        }
        dto.setPlatform(mobileApp.getPlatform());
        dto.setCreatedtime(mobileApp.getDisplayName());
        dto.setBanner(mobileApp.getBanner());
        dto.setRecentChanges(mobileApp.getRecentChanges());

        AppAppmetaDTO appAppmetaDTO = new AppAppmetaDTO();
        appAppmetaDTO.setPackage(mobileApp.getPackageName());
        appAppmetaDTO.setWeburl(mobileApp.getAppUrl());
        dto.setAppmeta(appAppmetaDTO);

        dto.setIcon(mobileApp.getThumbnail());
        dto.setAppType(mobileApp.getAppType());
        dto.setRecentChanges(mobileApp.getRecentChanges());
        dto.setCreatedTime(mobileApp.getCreatedTime());

        return dto;

    }


    /**
     * This method validates and converts AppDTO into a MobileApp
     * @param appDTO AppDTO
     * @return if appDTO is valid, returns the converted MobileApp, else throws a BadRequestException
     * @throws AppManagementException
     */
    public static MobileApp fromDTOtoMobileApp(AppDTO appDTO){

        String providerName = RestApiUtil.getLoggedInUsername();

        MobileApp mobileAppModel = new MobileApp();
        AppAppmetaDTO appAppmetaDTO = appDTO.getAppmeta();

        mobileAppModel.setAppProvider(providerName);
        //Validate Mandatory fields

        validateMandatoryField("platform", appDTO.getPlatform());
        mobileAppModel.setPlatform(appDTO.getPlatform());

        validateMandatoryField("markettype", appDTO.getMarketType());
        mobileAppModel.setMarketType(appDTO.getMarketType());

        if (validateMandatoryField("appmeta", appAppmetaDTO)) {
            if (AppMConstants.MOBILE_APPS_PLATFORM_ANDROID.equals(appDTO.getPlatform()) ||
                    AppMConstants.MOBILE_APPS_PLATFORM_IOS.equals(appDTO.getPlatform())) {

                if ("enterprise".equals(appDTO.getMarketType())) {
                    validateMandatoryField("path", appAppmetaDTO.getPath());
                    mobileAppModel.setAppUrl(appAppmetaDTO.getPath());
                    validateMandatoryField("package", appAppmetaDTO.getPackage());
                    mobileAppModel.setPackageName(appAppmetaDTO.getPackage());
                    validateMandatoryField("version", appAppmetaDTO.getVersion());
                    mobileAppModel.setBundleVersion(appAppmetaDTO.getVersion());
                    mobileAppModel.setVersion(appDTO.getVersion());
                } else if ("public".equals(appDTO.getMarketType())) {
                    validateMandatoryField("package", appAppmetaDTO.getPackage());
                    mobileAppModel.setPackageName(appAppmetaDTO.getPackage());
                    validateMandatoryField("version", appAppmetaDTO.getVersion());
                    mobileAppModel.setBundleVersion(appAppmetaDTO.getVersion());
                    mobileAppModel.setVersion(appDTO.getVersion());
                } else {
                    RestApiUtil.handleBadRequest("Unsupported market type '" + appDTO.getMarketType() +
                            "' is provided for platform : " + appDTO.getPlatform(), log);
                }
            } else if (AppMConstants.MOBILE_APPS_PLATFORM_WEBAPP.equals(appDTO.getPlatform())) {
                if ("webapp".equals(appDTO.getMarketType())) {
                    validateMandatoryField("weburl", appAppmetaDTO.getWeburl());
                    mobileAppModel.setAppUrl(appAppmetaDTO.getWeburl());
                    validateMandatoryField("version", appAppmetaDTO.getVersion());
                    mobileAppModel.setVersion(appAppmetaDTO.getVersion());
                } else {
                    RestApiUtil.handleBadRequest("Unsupported market type '" + appDTO.getMarketType() +
                            "' is provided for platform : " + appDTO.getPlatform(), log);
                }
            } else {
                RestApiUtil.handleBadRequest("Unsupported platform '" + appDTO.getPlatform() + "' is provided.", log);
            }
        }
        mobileAppModel.setAppName(appDTO.getName());
        mobileAppModel.setDisplayName(appDTO.getDisplayName());
        validateMandatoryField("description", appDTO.getDescription());
        mobileAppModel.setDescription(appDTO.getDescription());
        validateMandatoryField("category", appDTO.getCategory());
        mobileAppModel.setCategory(appDTO.getCategory());
        validateMandatoryField("banner", appDTO.getBanner());
        mobileAppModel.setBanner(appDTO.getBanner());
        validateMandatoryField("iconFile", appDTO.getIcon());
        mobileAppModel.setThumbnail(appDTO.getIcon());
        List<String> screenShots = appDTO.getScreenshots();
        validateMandatoryField("screenshots", screenShots);
        if(screenShots.size() > 3){
            RestApiUtil.handleBadRequest("Attached screenshots count exceeds the maximum number of allowed screenshots",
                    log);
        }
        while(screenShots.size() < 3){
            screenShots.add("");
        }
        mobileAppModel.setScreenShots(appDTO.getScreenshots());
        mobileAppModel.setRecentChanges(appDTO.getRecentChanges());

        if (appDTO.getTags() != null) {
            Set<String> apiTags = new HashSet<>(appDTO.getTags());
            mobileAppModel.addTags(apiTags);
        }
        List<String> visibleRoleList = new ArrayList<String>();
        visibleRoleList = appDTO.getVisibleRoles();
        if (visibleRoleList != null) {
            String[] visibleRoles = new String[visibleRoleList.size()];
            visibleRoles = visibleRoleList.toArray(visibleRoles);
            mobileAppModel.setAppVisibility(visibleRoles);
        }
        return mobileAppModel;
    }

    public static WebApp fromDTOToWebapp(AppDTO appDTO){
        String providerName = RestApiUtil.getLoggedInUsername();
        String appName = appDTO.getName();
        String appVersion = appDTO.getVersion();
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, appName, appVersion);
        WebApp webApp = new WebApp(apiIdentifier);
        webApp.setUrl(appDTO.getAppUrL());
        webApp.setContext(appDTO.getContext());
        webApp.setDisplayName(appDTO.getDisplayName());
        webApp.setStatus(APIStatus.CREATED);
        webApp.setTransports("http");
        webApp.setTreatAsASite("FALSE");
        return webApp;
    }

    private static boolean validateMandatoryField(String fieldName, Object fieldValue) {

        if (fieldValue == null) {
            RestApiUtil.handleBadRequest("Mandatory field  '" + fieldName + "' is not provided.", log);
        }
        return true;
    }

}
