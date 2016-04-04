package org.wso2.carbon.appmgt.rest.api.store.impl;

import org.wso2.carbon.appmgt.rest.api.store.ApiResponseMessage;
import org.wso2.carbon.appmgt.rest.api.store.NotFoundException;
import org.wso2.carbon.appmgt.rest.api.store.UsersApiService;
import org.wso2.carbon.appmgt.rest.api.store.dto.User;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class UsersApiServiceImpl extends UsersApiService {
    
    @Override
    public Response usersPost(User body, String contentType, String ifModifiedSince, SecurityContext securityContext)
    throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    
    @Override
    public Response usersUserNameLookupGet(String userName, String accept, String ifNoneMatch, String ifModifiedSince, SecurityContext securityContext)
    throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    
}
