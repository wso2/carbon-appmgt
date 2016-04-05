package org.wso2.carbon.appmgt.rest.api.store.impl;

import org.wso2.carbon.appmgt.rest.api.store.*;
import org.wso2.carbon.appmgt.rest.api.store.dto.*;


import org.wso2.carbon.appmgt.rest.api.store.dto.DownloadDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.AppListDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.AppInfoDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class AppsApiServiceImpl extends AppsApiService {
    @Override
    public Response appsDownloadPost(String contentType,DownloadDTO download){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response appsUninstallationPost(String contentType,DownloadDTO download){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response appsAppTypeGet(String appType,String query,Integer limit,Integer offset,String accept,String ifNoneMatch){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response appsAppTypeIdAppIdGet(String appType,String appId,String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
