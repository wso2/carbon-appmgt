package org.wso2.carbon.appmgt.rest.api.publisher.impl;

import org.wso2.carbon.appmgt.rest.api.publisher.*;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.*;


import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class XacmlpoliciesApiServiceImpl extends XacmlpoliciesApiService {
    @Override
    public Response xacmlpoliciesGet(String accept,String ifNoneMatch){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response xacmlpoliciesPolicyPartialIdAppsGet(Integer policyPartialId,Integer limit,Integer offset,String accept,String ifNoneMatch){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
