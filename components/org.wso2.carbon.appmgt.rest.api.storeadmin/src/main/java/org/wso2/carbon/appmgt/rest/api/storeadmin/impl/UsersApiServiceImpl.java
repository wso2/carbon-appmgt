package org.wso2.carbon.appmgt.rest.api.storeadmin.impl;

import org.wso2.carbon.appmgt.rest.api.storeadmin.*;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.*;


import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.UserListDTO;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class UsersApiServiceImpl extends UsersApiService {
    @Override
    public Response usersGet(Integer limit,Integer offset,String accept,String ifNoneMatch){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
