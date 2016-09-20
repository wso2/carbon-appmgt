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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.APIStatus;
import org.wso2.carbon.appmgt.api.model.App;
import org.wso2.carbon.appmgt.api.model.CustomProperty;
import org.wso2.carbon.appmgt.api.model.EntitlementPolicyGroup;
import org.wso2.carbon.appmgt.api.model.MobileApp;
import org.wso2.carbon.appmgt.api.model.Tier;
import org.wso2.carbon.appmgt.api.model.URITemplate;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppAppmetaDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppSummaryDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.CustomPropertyDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyGroupsDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.UriTemplateDTO;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        List<AppSummaryDTO> appSummaryDTOs = appListDTO.getAppSummaryList();
        if (appSummaryDTOs == null) {
            appSummaryDTOs = new ArrayList<>();
            appListDTO.setAppSummaryList(appSummaryDTOs);
        }

        //add the required range of objects to be returned
        int start = offset < appList.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= appList.size() - 1 ? offset + limit - 1 : appList.size() - 1;
        for (int i = start; i <= end; i++) {
            appSummaryDTOs.add(fromAppToInfoDTO(appList.get(i)));
        }
        appListDTO.setCount(appSummaryDTOs.size());
        return appListDTO;
    }

    /**
     * Create and returns an AppListDTO with basic fields in the given apps.
     *
     * @param appList List of Apps
     * @param limit   maximum number of APIs returns
     * @param offset  starting index
     * @return APIListDTO
     */
    public static AppListDTO getAppListDTOWithBasicFields(List<App> appList, int offset, int limit) {
        AppListDTO appListDTO = new AppListDTO();
        List<AppSummaryDTO> appSummaryDTOs = appListDTO.getAppSummaryList();
        if (appSummaryDTOs == null) {
            appSummaryDTOs = new ArrayList<>();
            appListDTO.setAppSummaryList(appSummaryDTOs);
        }

        //add the required range of objects to be returned
        int start = offset < appList.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= appList.size() - 1 ? offset + limit - 1 : appList.size() - 1;
        for (int i = start; i <= end; i++) {
            AppSummaryDTO appSummaryDTO = fromAppToInfoDTO(appList.get(i));
            if (appSummaryDTO != null) {
                appSummaryDTOs.add(appSummaryDTO);
            }
        }
        appListDTO.setCount(appSummaryDTOs.size());
        return appListDTO;
    }

    /**
     * Create and returns an AppListDTO with all fields in the given apps.
     *
     * @param appList List of Apps
     * @param limit   maximum number of APIs returns
     * @param offset  starting index
     * @return AppListDTO
     */
    public static AppListDTO getAppListDTOWithAllFields(List<App> appList, int offset, int limit) {

        AppListDTO appListDTO = new AppListDTO();
        List<AppDTO> appDTOs = appListDTO.getAppList();
        if (appDTOs == null) {
            appDTOs = new ArrayList<>();
            appListDTO.setAppList(appDTOs);
        }

        //add the required range of objects to be returned
        int start = offset < appList.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= appList.size() - 1 ? offset + limit - 1 : appList.size() - 1;
        for (int i = start; i <= end; i++) {
            AppDTO appDTO = fromAppToDTO(appList.get(i));
            if(appDTO != null){
                appDTOs.add(appDTO);
            }
        }
        appListDTO.setCount(appDTOs.size());
        return appListDTO;
    }

    /**
     * Creates a minimal DTO representation of an API object
     *
     * @param app WebApp object
     * @return a minimal representation DTO
     */
    public static AppSummaryDTO fromAPIToInfoDTO(WebApp app) {
        AppSummaryDTO appSummaryDTO = new AppSummaryDTO();
        appSummaryDTO.setDescription(app.getDescription());
        String context = app.getContext();
        if (context != null) {
            if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
                context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
            }
            appSummaryDTO.setContext(context);
        }
        appSummaryDTO.setId(app.getUUID());
        APIIdentifier apiId = app.getId();
        appSummaryDTO.setName(apiId.getApiName());
        appSummaryDTO.setVersion(apiId.getVersion());
        String providerName = app.getId().getProviderName();
        appSummaryDTO.setProvider(AppManagerUtil.replaceEmailDomainBack(providerName));
        appSummaryDTO.setLifecycleState(app.getLifeCycleStatus().getStatus());
        return appSummaryDTO;
    }

    public static AppSummaryDTO fromAppToInfoDTO(App app) {

        if (AppMConstants.WEBAPP_ASSET_TYPE.equals(app.getType())) {
            return fromWebAppToInfoDTO((WebApp) app);
        } else if (AppMConstants.MOBILE_ASSET_TYPE.equals(app.getType())) {
            return fromMobileAppToInfoDTO((MobileApp) app);
        }

        return null;
    }

    private static AppSummaryDTO fromMobileAppToInfoDTO(MobileApp app) {

        AppSummaryDTO appSummaryDTO = new AppSummaryDTO();
        appSummaryDTO.setId(app.getUUID());
        appSummaryDTO.setName(app.getAppName());
        appSummaryDTO.setVersion(app.getVersion());
        appSummaryDTO.setProvider(AppManagerUtil.replaceEmailDomainBack(app.getAppProvider()));
        appSummaryDTO.setDescription(app.getDescription());
        appSummaryDTO.setLifecycleState(app.getLifeCycleStatus().getStatus());
        appSummaryDTO.setRating(BigDecimal.valueOf(app.getRating()));
        return appSummaryDTO;

    }

    private static AppSummaryDTO fromWebAppToInfoDTO(WebApp app) {

        AppSummaryDTO appSummaryDTO = new AppSummaryDTO();

        appSummaryDTO.setId(app.getUUID());

        APIIdentifier apiId = app.getId();
        appSummaryDTO.setName(apiId.getApiName());
        appSummaryDTO.setVersion(apiId.getVersion());

        String providerName = app.getId().getProviderName();
        appSummaryDTO.setProvider(AppManagerUtil.replaceEmailDomainBack(providerName));

        appSummaryDTO.setDescription(app.getDescription());

        String context = app.getContext();
        if (context != null) {
            if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
                context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
            }
            appSummaryDTO.setContext(context);
        }

        appSummaryDTO.setLifecycleState(app.getLifeCycleStatus().getStatus());
        appSummaryDTO.setRating(BigDecimal.valueOf(app.getRating()));
        return appSummaryDTO;

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
            dto.setTransport(model.getTransports());
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


    public static AppDTO fromAppToDTO(App app) {

        AppDTO appDTO = null;

        if (AppMConstants.WEBAPP_ASSET_TYPE.equals(app.getType())) {
            appDTO = fromWebAppToDTO((WebApp) app);
        } else if (AppMConstants.MOBILE_ASSET_TYPE.equals(app.getType())) {
            appDTO = fromMobileAppToDTO((MobileApp) app);
        }

        if(appDTO != null && app.getCustomProperties() != null){

            List<CustomPropertyDTO> customPropertyDTOs = new ArrayList<CustomPropertyDTO>();

            CustomPropertyDTO customPropertyDTO = null;
            for(CustomProperty customProperty : app.getCustomProperties()){
                customPropertyDTO = new CustomPropertyDTO();
                customPropertyDTO.setName(customProperty.getName());
                customPropertyDTO.setValue(customProperty.getValue());
                customPropertyDTOs.add(customPropertyDTO);
            }
            appDTO.setCustomProperties(customPropertyDTOs);
        }

        return appDTO;
    }

    private static AppDTO fromWebAppToDTO(WebApp webapp) {

        AppDTO dto = new AppDTO();

        dto.setType(webapp.getType());
        dto.setId(webapp.getUUID());
        dto.setName(webapp.getId().getApiName());
        dto.setDisplayName(webapp.getDisplayName());
        dto.setDescription(webapp.getDescription());
        dto.setVersion(webapp.getId().getVersion());

        String providerName = webapp.getId().getProviderName();
        dto.setProvider(AppManagerUtil.replaceEmailDomainBack(providerName));


        String context = webapp.getContext();
        if (context != null) {
            if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
                context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
            }
            dto.setContext(context);
        }

        dto.setBusinessOwnerId(webapp.getBusinessOwner());
        dto.setAppUrL(webapp.getUrl());
        dto.setIsDefaultVersion(webapp.isDefaultVersion());
        dto.setIsSite(webapp.getTreatAsASite());
        dto.setThumbnailUrl(webapp.getThumbnailUrl());
        dto.setBanner(webapp.getBanner());
        dto.setScreenshots(null);
        dto.setTrackingCode(webapp.getTrackingCode());
        dto.setLifecycleState(webapp.getLifeCycleStatus().getStatus());
        dto.setRating(BigDecimal.valueOf(webapp.getRating()));

        Set<String> apiTags = webapp.getTags();
        dto.setTags(new ArrayList<String>(apiTags));

        List<String> tiers = new ArrayList<>();
        for (Tier tier : webapp.getAvailableTiers()) {
            tiers.add(tier.getName());
        }

        dto.setTransport(webapp.getTransports());

        if (webapp.getVisibleRoles() != null) {
            dto.setVisibleRoles(Arrays.asList(webapp.getVisibleRoles().split(",")));
        }

        if (webapp.getLifeCycleName() != null) {
            dto.setLifecycle(webapp.getLifeCycleName());
        }

        dto.setCreatedTime(webapp.getCreatedTime());
        dto.setMediaType(webapp.getMediaType());

        // Set policy groups.
        List<PolicyGroupsDTO> policyGroupsDTOs = new ArrayList<PolicyGroupsDTO>();

        List<EntitlementPolicyGroup> policyGroups = webapp.getAccessPolicyGroups();
        if(policyGroups != null){

            for(EntitlementPolicyGroup policyGroup : policyGroups){
                PolicyGroupsDTO policyGroupsDTO = new PolicyGroupsDTO();
                policyGroupsDTO.setPolicyGroupId(policyGroup.getPolicyGroupId());
                policyGroupsDTO.setPolicyGroupName(policyGroup.getPolicyGroupName());
                policyGroupsDTO.setDescription(policyGroup.getPolicyDescription());
                policyGroupsDTO.setUserRoles(policyGroup.getUserRolesAsList());
                policyGroupsDTO.setAllowAnonymousAccess(String.valueOf(policyGroup.isAllowAnonymous()));
                policyGroupsDTO.setThrottlingTier(policyGroup.getThrottlingTier());

                int entitlementPolicyId = policyGroup.getFirstEntitlementPolicyId();
                if(entitlementPolicyId > 0){
                    policyGroupsDTO.setPolicyPartialMapping(Arrays.asList(new String[]{String.valueOf(entitlementPolicyId)}));
                }

                policyGroupsDTOs.add(policyGroupsDTO);
            }
        }
        dto.setPolicyGroups(policyGroupsDTOs);


        // Set URI Templates
        List<UriTemplateDTO> uriTemplateDTOs = new ArrayList<UriTemplateDTO>();
        Set<URITemplate> uriTemplates = webapp.getUriTemplates();

        if(uriTemplates != null){
            for(URITemplate uriTemplate : uriTemplates){
                UriTemplateDTO uriTemplateDTO = new UriTemplateDTO();
                uriTemplateDTO.setId(uriTemplate.getId());
                uriTemplateDTO.setUrlPattern(uriTemplate.getUriTemplate());
                uriTemplateDTO.setHttpVerb(uriTemplate.getHTTPVerb());
                uriTemplateDTO.setPolicyGroupName(uriTemplate.getPolicyGroupName());
                uriTemplateDTO.setPolicyGroupId(uriTemplate.getPolicyGroupId());

                uriTemplateDTOs.add(uriTemplateDTO);
            }
        }
        dto.setUriTemplates(uriTemplateDTOs);

        return dto;
    }

    private static AppDTO fromMobileAppToDTO(MobileApp mobileApp) {

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
     * Converts AppDTO into a MobileApp
     *
     * @param appDTO AppDTO
     * @return if appDTO is valid, returns the converted MobileApp, else throws a BadRequestException
     * @throws AppManagementException
     */
    public static MobileApp fromDTOtoMobileApp(AppDTO appDTO) {

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
        if (screenShots.size() > 3) {
            RestApiUtil.handleBadRequest("Attached screenshots count exceeds the maximum number of allowed screenshots",
                    log);
        }
        while (screenShots.size() < 3) {
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

    /**
     * Convert AppDTO to WebApp
     *
     * @param appDTO application data transfer object
     * @return WebApp
     */
    public static WebApp fromDTOToWebapp(AppDTO appDTO) {

        String providerName = AppManagerUtil.replaceEmailDomainBack(RestApiUtil.getLoggedInUsername());
        String appName = appDTO.getName();
        String appVersion = appDTO.getVersion();
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, appName, appVersion);
        WebApp webApp = new WebApp(apiIdentifier);
        webApp.setUUID(appDTO.getId());
        webApp.setType(AppMConstants.WEBAPP_ASSET_TYPE);

        if(appDTO.getIsDefaultVersion() != null){
            webApp.setDefaultVersion(appDTO.getIsDefaultVersion());
        }else {
            webApp.setDefaultVersion(false);
        }

        webApp.setUrl(appDTO.getAppUrL());
        webApp.setContext(appDTO.getContext());
        webApp.setDisplayName(appDTO.getDisplayName());
        webApp.setStatus(APIStatus.CREATED);
        webApp.setTransports(appDTO.getTransport());
        webApp.setTreatAsASite(appDTO.getIsSite());
        webApp.setDescription(appDTO.getDescription());
        webApp.setThumbnailUrl(appDTO.getThumbnailUrl());
        webApp.setBanner(appDTO.getBanner());
        webApp.setTrackingCode(appDTO.getTrackingCode());
        webApp.setLogoutURL(appDTO.getLogoutURL());
        webApp.setBusinessOwner(appDTO.getBusinessOwnerId());
        webApp.setVisibleTenants(StringUtils.join(appDTO.getVisibleTenants(), ","));
        webApp.setSkipGateway(Boolean.parseBoolean(appDTO.getSkipGateway()));

		if(appDTO.isServiceProviderCreationEnabled() != null){
			webApp.setServiceProviderCreationEnabled(appDTO.isServiceProviderCreationEnabled());
		}else{
			// Default behaviour is creating the service provider.
			webApp.setServiceProviderCreationEnabled(true);
		}

        webApp.setServiceProviderCreationEnabled(appDTO.isServiceProviderCreationEnabled());
        webApp.setAllowAnonymous(Boolean.parseBoolean(appDTO.getAllowAnonymousAccess()));
        webApp.setAcsURL(appDTO.getAcsUrl());
        webApp.setSsoProviderDetails(AppManagerUtil.getDefaultSSOProvider());
        webApp.setSaml2SsoIssuer(getSaml2SsoIssuer(appName, appVersion));

        List<PolicyGroupsDTO> policyGroupsDTOs = appDTO.getPolicyGroups();
        List<EntitlementPolicyGroup> accessPolicyGroups = new ArrayList<EntitlementPolicyGroup>();

        //Set Policy groups
        for (PolicyGroupsDTO policyGroupsDTO : policyGroupsDTOs) {

            EntitlementPolicyGroup entitlementPolicyGroup = new EntitlementPolicyGroup();

            if(policyGroupsDTO.getPolicyGroupId() != null){
                entitlementPolicyGroup.setPolicyGroupId(policyGroupsDTO.getPolicyGroupId());
            }

            entitlementPolicyGroup.setPolicyGroupName(policyGroupsDTO.getPolicyGroupName());
            entitlementPolicyGroup.setPolicyDescription(policyGroupsDTO.getDescription());
            entitlementPolicyGroup.setAllowAnonymous(Boolean.parseBoolean(policyGroupsDTO.getAllowAnonymousAccess()));
            entitlementPolicyGroup.setThrottlingTier(policyGroupsDTO.getThrottlingTier());
            entitlementPolicyGroup.setUserRoles(StringUtils.join(policyGroupsDTO.getUserRoles(), ","));
            entitlementPolicyGroup.setXacmlPolicyNames(policyGroupsDTO.getPolicyPartialMapping());

            if(policyGroupsDTO.getPolicyPartialMapping() != null && policyGroupsDTO.getPolicyPartialMapping().size() > 0){
                entitlementPolicyGroup.setEntitlementPolicyId(Integer.parseInt(policyGroupsDTO.getPolicyPartialMapping().get(0)));
            }

            accessPolicyGroups.add(entitlementPolicyGroup);
        }
        webApp.setAccessPolicyGroups(accessPolicyGroups);

        //Set URITemplates
        List<UriTemplateDTO> uriTemplateDTOs = appDTO.getUriTemplates();
        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        for (UriTemplateDTO uriTemplateDTO : uriTemplateDTOs) {
            URITemplate uriTemplate = new URITemplate();

            if(uriTemplateDTO.getId() != null){
                uriTemplate.setId(uriTemplateDTO.getId());
            }

            uriTemplate.setHTTPVerb(uriTemplateDTO.getHttpVerb());
            uriTemplate.setUriTemplate(uriTemplateDTO.getUrlPattern());
            uriTemplate.setPolicyGroupName(uriTemplateDTO.getPolicyGroupName());

            if(uriTemplateDTO.getPolicyGroupId() != null){
                uriTemplate.setPolicyGroupId(uriTemplateDTO.getPolicyGroupId());
            }

            uriTemplates.add(uriTemplate);
        }
        webApp.setUriTemplates(uriTemplates);
        if (appDTO.getTags() != null) {
            Set<String> apiTags = new HashSet<>(appDTO.getTags());
            webApp.addTags(apiTags);
        }

        List<String>  visibleRoleList = appDTO.getVisibleRoles();
        if (visibleRoleList != null) {
            String[] visibleRoles = new String[visibleRoleList.size()];
            visibleRoles = visibleRoleList.toArray(visibleRoles);
            webApp.setAppVisibility(visibleRoles);
            webApp.setVisibleRoles(StringUtils.join(visibleRoleList, ","));
        }else{
            webApp.setVisibleRoles("");
        }
        List<String> claimsList = appDTO.getClaims();
        if(claimsList != null){
            webApp.setClaims(claimsList);
        }

        if(appDTO.getCustomProperties() != null && !appDTO.getCustomProperties().isEmpty()){

            for(CustomPropertyDTO customPropertyDTO : appDTO.getCustomProperties()){
                webApp.addCustomProperty(customPropertyDTO.getName(), customPropertyDTO.getValue());
            }
        }

        return webApp;
    }

    private static boolean validateMandatoryField(String fieldName, Object fieldValue) {

        if (fieldValue == null) {
            RestApiUtil.handleBadRequest("Mandatory field  '" + fieldName + "' is not provided.", log);
        }
        return true;
    }

    public static String getSaml2SsoIssuer(String appName, String appVersion) {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String saml2SsoIssuer;
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            saml2SsoIssuer = appName + "-" + appVersion;
        } else {
            saml2SsoIssuer = appName + "-" + tenantDomain + "-" + appVersion;
        }
        return saml2SsoIssuer;
    }
}
