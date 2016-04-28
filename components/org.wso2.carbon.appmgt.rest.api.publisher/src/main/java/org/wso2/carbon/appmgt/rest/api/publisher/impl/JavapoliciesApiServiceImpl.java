package org.wso2.carbon.appmgt.rest.api.publisher.impl;

import org.wso2.carbon.appmgt.rest.api.publisher.*;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.*;


import org.wso2.carbon.appmgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.JavaPolicyDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class JavapoliciesApiServiceImpl extends JavapoliciesApiService {
    @Override
    public Response javapoliciesAppidAppIdGet(String appId,String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
