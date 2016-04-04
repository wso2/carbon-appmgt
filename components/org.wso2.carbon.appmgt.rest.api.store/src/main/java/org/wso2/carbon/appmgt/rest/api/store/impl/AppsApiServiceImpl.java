package org.wso2.carbon.appmgt.rest.api.store.impl;

import org.wso2.carbon.appmgt.rest.api.store.ApiResponseMessage;
import org.wso2.carbon.appmgt.rest.api.store.AppsApiService;
import org.wso2.carbon.appmgt.rest.api.store.NotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class AppsApiServiceImpl extends AppsApiService {
    
    @Override
    public Response appsAppIdGet(String appId, String accept, String ifNoneMatch, String ifModifiedSince, SecurityContext securityContext)
    throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    
    @Override
    public Response appsGet(String query, Integer limit, Integer offset, String accept, String ifNoneMatch, SecurityContext securityContext)
    throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    
}
