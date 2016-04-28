package org.wso2.carbon.appmgt.rest.api.publisher;

import org.wso2.carbon.appmgt.rest.api.publisher.*;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.*;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.BinaryDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.StaticContentDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.UserIdListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.ResponseMessageDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppInfoDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.TagListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.TierListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialIdListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class AppsApiService {
    public abstract Response appsMobileBinariesPost(InputStream fileInputStream,Attachment fileDetail,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsStaticContentsPost(InputStream fileInputStream,Attachment fileDetail,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeGet(String appType,String query,Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract Response appsAppTypePost(String appType,AppDTO body,String contentType,String ifModifiedSince);
    public abstract Response appsAppTypeAppIdAppIdSubscriptionsGet(String appType,String appId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response appsAppTypeCacheGet(String appType,String query,Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract Response appsAppTypeChangeLifecyclePost(String appType,String action,String appId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdGet(String appType,String appId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response appsAppTypeIdAppIdPut(String appType,String appId,AppDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdDelete(String appType,String appId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdCreateNewVersionPost(String appType,String appId,String contentType,String ifModifiedSince);
    public abstract Response appsAppTypeIdAppIdTagsGet(String appType,String appId,String accept,String ifNoneMatch);
    public abstract Response appsAppTypeIdAppIdTagsPut(String appType,String appId,TagListDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdTagsDelete(String appType,String appId,TagListDTO body,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeIdAppIdThrottlingtiersGet(String appType,String appId,String accept,String ifNoneMatch);
    public abstract Response appsAppTypeIdAppIdXacmlpoliciesGet(String appType,String appId,String accept,String ifNoneMatch);
    public abstract Response appsAppTypeIdAppIdXacmlpoliciesPost(String appType,String appId,PolicyPartialIdListDTO body,String contentType,String ifModifiedSince);
    public abstract Response appsAppTypeIdAppIdXacmlpoliciesPolicyPartialIdDelete(String appType,String appId,Integer policyPartialId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response appsAppTypeTagsGet(String appType,String accept,String ifNoneMatch);
    public abstract Response appsAppTypeValidateContextPost(String appType,String appContext,String contentType,String ifModifiedSince);
}

