package org.wso2.carbon.appmgt.rest.api.publisher.impl;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.rest.api.publisher.ApiResponseMessage;
import org.wso2.carbon.appmgt.rest.api.publisher.AppsApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.mappings.APPMappingUtil;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

public class AppsApiServiceImpl extends AppsApiService {

    private static final Log log = LogFactory.getLog(AppsApiService.class);

    @Override
    public Response appsAppIdDelete(String appId, String ifMatch, String ifUnmodifiedSince,
                                    SecurityContext securityContext)
            throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response appsAppIdGet(String appId, String accept, String ifNoneMatch, String ifModifiedSince,
                                 SecurityContext securityContext)
            throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response appsAppIdPut(String appId, AppDTO body, String contentType, String ifMatch,
                                 String ifUnmodifiedSince, SecurityContext securityContext)
            throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response appsChangeLifecyclePost(String action, String appId, String ifMatch, String ifUnmodifiedSince,
                                            SecurityContext securityContext)
            throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response appsGet(String query, Integer limit, Integer offset, String accept, String ifNoneMatch,
                            SecurityContext securityContext)
            throws NotFoundException {
        // do some magic!
        // return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();

        List<WebApp> allMatchedApis;
        AppListDTO appListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            //if query parameter is not specified, This will search by name
            String searchType = AppMConstants.API_NAME;
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
            allMatchedApis = apiProvider.searchAPIs(searchContent, searchType, null);
            appListDTO = APPMappingUtil.fromAPIListToDTO(allMatchedApis, offset, limit);
            APPMappingUtil.setPaginationParams(appListDTO, query, offset, limit, allMatchedApis.size());
            return Response.ok().entity(appListDTO).build();
        } catch (AppManagementException e) {
            String errorMessage = "Error while retrieving APIs";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response appsMobileBinariesPost(byte[] body, String ifMatch, String ifUnmodifiedSince,
                                           SecurityContext securityContext)
            throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response appsPost(String contentType, String ifModifiedSince, SecurityContext securityContext)
            throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

}
