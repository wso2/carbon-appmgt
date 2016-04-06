package org.wso2.carbon.appmgt.rest.api.storeadmin.impl;

import org.wso2.carbon.appmgt.rest.api.storeadmin.*;


import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.InstallDTO;

import javax.ws.rs.core.Response;

public class AppsApiServiceImpl extends AppsApiService {
    @Override
    public Response appsDownloadPost(String contentType,InstallDTO install){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response appsUninstallationPost(String contentType,InstallDTO install){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
