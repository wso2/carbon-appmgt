package org.wso2.carbon.appmgt.rest.api.publisher.impl;

import org.wso2.carbon.appmgt.rest.api.publisher.*;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.*;


import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyGroupDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class PolicygroupsApiServiceImpl extends PolicygroupsApiService {
    @Override
    public Response policygroupsPost(PolicyGroupDTO body,String contentType,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response policygroupsPolicyGroupIdPut(Integer policyGroupId,PolicyGroupDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response policygroupsPolicyGroupIdAppsGet(Integer policyGroupId,String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
