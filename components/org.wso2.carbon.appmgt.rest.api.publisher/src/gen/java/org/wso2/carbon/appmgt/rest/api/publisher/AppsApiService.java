package org.wso2.carbon.appmgt.rest.api.publisher;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;

import javax.ws.rs.core.Response;
import java.io.InputStream;

public abstract class AppsApiService {
    public abstract Response appsMobileBinariesPost(InputStream fileInputStream, Attachment fileDetail, String ifMatch, String ifUnmodifiedSince);
    public abstract Response appsAppTypeGet(String appType,String query,Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract Response appsAppTypePost(String appType,AppDTO body,String contentType,String ifModifiedSince);
    public abstract Response appsAppTypeChangeLifecyclePost(String appType,String action,String appId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdGet(String appType,String appId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response appsAppTypeIdAppIdPut(String appType,String appId,AppDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdDelete(String appType,String appId,String ifMatch,String ifUnmodifiedSince);
}

