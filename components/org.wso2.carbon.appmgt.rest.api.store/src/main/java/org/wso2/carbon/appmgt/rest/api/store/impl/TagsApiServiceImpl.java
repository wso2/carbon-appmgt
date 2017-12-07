package org.wso2.carbon.appmgt.rest.api.store.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.APIConsumer;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.rest.api.store.TagsApiService;
import org.wso2.carbon.appmgt.rest.api.store.dto.AppIdListDTO;
import org.wso2.carbon.appmgt.rest.api.store.utils.mappings.APPMappingUtil;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.appmgt.rest.api.util.validation.CommonValidator;

import javax.ws.rs.core.Response;
import java.util.*;

public class TagsApiServiceImpl extends TagsApiService {
    private static final Log log = LogFactory.getLog(TagsApiServiceImpl.class);


    @Override
    public Response tagsTagNameAppsAppTypeGet(String tagName, String appType, Integer limit, Integer offset,
                                              String accept, String ifNoneMatch, String ifModifiedSince) {
        AppIdListDTO appIDListDTO = new AppIdListDTO();
        try {
            //check App Type validity
            CommonValidator.isValidAppType(appType);

            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            Map<String, String> attributeMap = new HashMap<>();
            if (AppMConstants.SITE_ASSET_TYPE.equalsIgnoreCase(appType)) {
                attributeMap.put(AppMConstants.APP_OVERVIEW_TREAT_AS_A_SITE, "TRUE");
            } else if (AppMConstants.WEBAPP_ASSET_TYPE.equalsIgnoreCase(appType)){
                attributeMap.put(AppMConstants.APP_OVERVIEW_TREAT_AS_A_SITE, "FALSE");
            }

            //Make the asset type as 'webapp'.
            appType = APPMappingUtil.updateAssetType(appType);

            //all tags
            apiConsumer.getAllTags(tenantDomain, appType, attributeMap);
            Map<String, Set<WebApp>> allTaggedApps = apiConsumer.getTaggedAPIs();
            if (allTaggedApps == null) {
                return RestApiUtil.buildNotFoundException("Apps (with Tag:" + tagName + ")", null).getResponse();
            }

            //filter by specific tag
            Set<WebApp> tempTaggedAppsSet = allTaggedApps.get(tagName);
            if (tempTaggedAppsSet == null) {
                return RestApiUtil.buildNotFoundException("Apps (with Tag:" + tagName + ")", null).getResponse();
            }

            List<WebApp> filteredTaggedApps = new ArrayList<>();
            filteredTaggedApps.addAll(tempTaggedAppsSet);

            List<String> appIds = new ArrayList<>();
            for (WebApp taggedApp : filteredTaggedApps) {
                appIds.add(taggedApp.getUUID());
            }
            appIDListDTO.setAppIds(appIds);
        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving Apps by Tag:" + tagName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(appIDListDTO).build();
    }
}
