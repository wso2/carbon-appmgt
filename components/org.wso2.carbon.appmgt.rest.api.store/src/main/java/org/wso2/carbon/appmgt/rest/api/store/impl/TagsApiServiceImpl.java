package org.wso2.carbon.appmgt.rest.api.store.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.APIConsumer;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.rest.api.store.TagsApiService;
import org.wso2.carbon.appmgt.rest.api.store.dto.AppIdListDTO;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TagsApiServiceImpl extends TagsApiService {
    private static final Log log = LogFactory.getLog(TagsApiServiceImpl.class);


    @Override
    public Response tagsTagNameAppsAppTypeGet(String tagName, String appType, Integer limit, Integer offset,
                                              String accept, String ifNoneMatch, String ifModifiedSince) {
        AppIdListDTO appIDListDTO = new AppIdListDTO();
        try {
            //check App Type validity
            if ((AppMConstants.MOBILE_ASSET_TYPE.equalsIgnoreCase(appType) ||
                    AppMConstants.WEBAPP_ASSET_TYPE.equalsIgnoreCase(appType) ||
                    AppMConstants.SITE_ASSET_TYPE.equalsIgnoreCase(appType)) == false) {
                RestApiUtil.handleBadRequest("Unsupported application type '" + appType + "' provided", log);
            }
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            //all tags
            apiConsumer.getAllTags(tenantDomain, appType, null);
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
            String errorMessage = "Error while retrieving Apps by tag:" + tagName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(appIDListDTO).build();
    }
}
