package org.wso2.carbon.appmgt.rest.api.store;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class AppsApiService {
  
      public abstract Response appsAppIdGet(String appId,String accept,String ifNoneMatch,String ifModifiedSince,SecurityContext securityContext)
      throws NotFoundException;
  
      public abstract Response appsGet(String query,Integer limit,Integer offset,String accept,String ifNoneMatch,SecurityContext securityContext)
      throws NotFoundException;
  
}
