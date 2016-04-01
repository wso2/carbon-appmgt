package org.wso2.carbon.appmgt.rest.api.publisher;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-03-31T12:45:14.954Z")
public abstract class AppsApiService {
  
      public abstract Response appsAppIdDelete(String appId,String ifMatch,String ifUnmodifiedSince,SecurityContext securityContext)
      throws NotFoundException;
  
      public abstract Response appsAppIdGet(String appId,String accept,String ifNoneMatch,String ifModifiedSince,SecurityContext securityContext)
      throws NotFoundException;
  
      public abstract Response appsAppIdPut(String appId,AppDTO body,String contentType,String ifMatch,String ifUnmodifiedSince,SecurityContext securityContext)
      throws NotFoundException;
  
      public abstract Response appsChangeLifecyclePost(String action,String appId,String ifMatch,String ifUnmodifiedSince,SecurityContext securityContext)
      throws NotFoundException;
  
      public abstract Response appsGet(String query,Integer limit,Integer offset,String accept,String ifNoneMatch,SecurityContext securityContext)
      throws NotFoundException;
  
      public abstract Response appsMobileBinariesPost(byte[] body,String ifMatch,String ifUnmodifiedSince,SecurityContext securityContext)
      throws NotFoundException;
  
      public abstract Response appsPost(String contentType,String ifModifiedSince,SecurityContext securityContext)
      throws NotFoundException;
  
}
