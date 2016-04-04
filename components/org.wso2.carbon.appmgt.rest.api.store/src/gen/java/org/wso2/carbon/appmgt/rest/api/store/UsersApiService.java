package org.wso2.carbon.appmgt.rest.api.store;

import org.wso2.carbon.appmgt.rest.api.store.dto.User;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class UsersApiService {
  
      public abstract Response usersPost(User body,String contentType,String ifModifiedSince,SecurityContext securityContext)
      throws NotFoundException;
  
      public abstract Response usersUserNameLookupGet(String userName,String accept,String ifNoneMatch,String ifModifiedSince,SecurityContext securityContext)
      throws NotFoundException;
  
}
