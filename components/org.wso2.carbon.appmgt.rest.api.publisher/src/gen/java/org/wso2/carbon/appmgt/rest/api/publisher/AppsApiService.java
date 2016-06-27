package org.wso2.carbon.appmgt.rest.api.publisher;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.TagListDTO;

import javax.ws.rs.core.Response;
import java.io.InputStream;

public abstract class AppsApiService {
    public abstract Response appsMobileBinariesPost(InputStream fileInputStream,Attachment fileDetail,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsMobileBinariesFileNameGet(String fileName,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsMobileGetplistTenantTenantIdFileFileNameGet(String tenantId,String fileName,String accept,String ifNoneMatch);
    public abstract Response appsStaticContentsPost(String appType,InputStream fileInputStream,Attachment fileDetail,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsStaticContentsFileNameGet(String appType,String fileName,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeGet(String appType,String query,String fieldFilter,Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract Response appsAppTypePost(String appType,AppDTO body,String contentType,String ifModifiedSince);
    public abstract Response appsAppTypeChangeLifecyclePost(String appType,String action,String appId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdGet(String appType,String appId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response appsAppTypeIdAppIdPut(String appType,String appId,AppDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdDelete(String appType,String appId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdCreateNewVersionPost(String appType,String appId,AppDTO body,String contentType,String ifModifiedSince);
    public abstract Response appsAppTypeIdAppIdDiscoverPost(String appType,String appId,String contentType,String ifModifiedSince);
    public abstract Response appsAppTypeIdAppIdDocsGet(String appType,String appId,Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract Response appsAppTypeIdAppIdDocsPost(String appId,String appType,DocumentDTO body,String contentType);
    public abstract Response appsAppTypeIdAppIdDocsDocumentIdGet(String appType,String appId,String documentId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdDocsDocumentIdDelete(String appType,String appId,String documentId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdDocsDocumentIdPut(String appId,String documentId,String appType,DocumentDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdDocsDocumentIdContentGet(String appId,String documentId,String appType,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response appsAppTypeIdAppIdDocsDocumentIdContentPost(String appType,String appId,String documentId,InputStream fileInputStream,Attachment fileDetail,String inlineContent,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdLifecycleGet(String appType,String appId,String accept,String ifNoneMatch);
    public abstract Response appsAppTypeIdAppIdLifecycleHistoryGet(String appType,String appId,String accept,String ifNoneMatch);
    public abstract Response appsAppTypeIdAppIdSubscriptionsGet(String appType,String appId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response appsAppTypeIdAppIdTagsGet(String appType,String appId,String accept,String ifNoneMatch);
    public abstract Response appsAppTypeIdAppIdTagsPut(String appType,String appId,TagListDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdTagsDelete(String appType,String appId,TagListDTO body,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeThrottlingtiersGet(String appType, String accept, String ifNoneMatch);
    public abstract Response appsAppTypeStatsStatTypeGet(String appType,String statType,String startTimeStamp,String endTimeStamp,Integer limit,String accept,String ifNoneMatch);
    public abstract Response appsAppTypeTagsGet(String appType,String accept,String ifNoneMatch);
    public abstract Response appsAppTypeValidateContextPost(String appType,String appContext,String contentType,String ifModifiedSince);
}

