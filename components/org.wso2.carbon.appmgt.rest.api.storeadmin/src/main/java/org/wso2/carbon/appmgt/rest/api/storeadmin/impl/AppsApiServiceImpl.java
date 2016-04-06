package org.wso2.carbon.appmgt.rest.api.storeadmin.impl;

import org.wso2.carbon.appmgt.rest.api.storeadmin.*;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.*;


import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.InstallDTO;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

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
