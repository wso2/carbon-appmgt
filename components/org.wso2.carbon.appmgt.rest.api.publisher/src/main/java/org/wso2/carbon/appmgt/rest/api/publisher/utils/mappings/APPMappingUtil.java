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
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.MobileApp;
import org.wso2.carbon.appmgt.api.model.Tier;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppAppmetaDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppInfoDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppListDTO;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.Arrays;
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
    public static AppListDTO fromAPIListToDTO(List<WebApp> appList, int offset, int limit) {
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
            appInfoDTOs.add(fromAPIToInfoDTO(appList.get(i)));
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
        dto.setMarketType(model.getMarketType());
        dto.setBundleversion(model.getBundleVersion());
        dto.setCategory(model.getCategory());
        dto.setDisplayName(model.getDisplayName());
        if (model.getScreenShots() != null) {
            dto.setScreenshots(model.getScreenShots());
        }
        dto.setPlatform(model.getPlatform());
        dto.setCreatedtime(model.getDisplayName());
        dto.setBanner(model.getBanner());
        dto.setRecentChanges(model.getRecentChanges());

        AppAppmetaDTO appAppmetaDTO = new AppAppmetaDTO();
        appAppmetaDTO.setPackage(model.getPackageName());
        appAppmetaDTO.setPath(model.getPath());
        appAppmetaDTO.setVersion(model.getId().getVersion());
        appAppmetaDTO.setWeburl(model.getAppUrl());
        dto.setAppmeta(appAppmetaDTO);

        dto.setIcon(model.getIcon());
        dto.setAppType(model.getAppType());
        dto.setMediaType(model.getMediaType());
        dto.setRecentChanges(model.getRecentChanges());
        dto.setCreatedTime(model.getCreatedTime());


        return dto;
    }

    public static MobileApp fromDTOtoMobileApp(AppDTO dto, String provider) throws AppManagementException {


        MobileApp mobileAppModel = new MobileApp();
        AppAppmetaDTO appAppmetaDTO = dto.getAppmeta();
        mobileAppModel.setBanner(dto.getBanner());
        mobileAppModel.setThumbnail(dto.getThumbnailUrl());
        mobileAppModel.setScreenShots(dto.getScreenshots());
        mobileAppModel.setAppName(dto.getName());
        mobileAppModel.setDisplayName(dto.getDisplayName());
        mobileAppModel.setDescription(dto.getDescription());
        mobileAppModel.setAppVersion(appAppmetaDTO.getVersion());
        mobileAppModel.setAppVisibility(dto.getVisibleRoles());
        mobileAppModel.setAppProvider(dto.getProvider());
        mobileAppModel.setPlatform(dto.getPlatform());
        mobileAppModel.setAppVersion(dto.getVersion());
        mobileAppModel.setCategory(dto.getCategory());
        mobileAppModel.setRecentChanges(dto.getRecentChanges());
        mobileAppModel.setPackageName(appAppmetaDTO.getPackage());
        mobileAppModel.setPlatform(dto.getPlatform());
        mobileAppModel.setMarketType(dto.getMarketType());
        if ("webapp".equals(dto.getPlatform())) {
            mobileAppModel.setAppUrl(appAppmetaDTO.getWeburl());

        } else {
            mobileAppModel.setAppUrl(appAppmetaDTO.getPath());
        }
        mobileAppModel.setAppUrl(appAppmetaDTO.getPath());
        mobileAppModel.setAppProvider(provider);


        return mobileAppModel;
    }

}
