package org.wso2.carbon.appmgt.rest.api.publisher;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public abstract class AppsApiService {

    public abstract Response appsAppTypeChangeLifecyclePost(String appType, String action, String appId, String ifMatch,
                                                            String ifUnmodifiedSince, SecurityContext securityContext)
            throws NotFoundException;

    public abstract Response appsAppTypeGet(String appType, String query, Integer limit, Integer offset, String accept,
                                            String ifNoneMatch, SecurityContext securityContext)
            throws NotFoundException;

    public abstract Response appsAppTypeIdAppIdDelete(String appType, String appId, String ifMatch,
                                                      String ifUnmodifiedSince, SecurityContext securityContext)
            throws NotFoundException;

    public abstract Response appsAppTypeIdAppIdGet(String appType, String appId, String accept, String ifNoneMatch,
                                                   String ifModifiedSince, SecurityContext securityContext)
            throws NotFoundException;

    public abstract Response appsAppTypeIdAppIdPut(String appType, String appId, AppDTO body, String contentType,
                                                   String ifMatch, String ifUnmodifiedSince,
                                                   SecurityContext securityContext)
            throws NotFoundException;

    public abstract Response appsAppTypePost(String appType, AppDTO body, String contentType, String ifModifiedSince,
                                             SecurityContext securityContext)
            throws NotFoundException;

    public abstract Response appsMobileBinariesPost(byte[] body, String ifMatch, String ifUnmodifiedSince,
                                                    SecurityContext securityContext)
            throws NotFoundException;

}
