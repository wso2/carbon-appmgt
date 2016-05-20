package org.wso2.carbon.appmgt.rest.api.publisher;

import org.wso2.carbon.appmgt.rest.api.publisher.*;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.*;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class XacmlpoliciesApiService {
    public abstract Response xacmlpoliciesGet(Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract Response xacmlpoliciesPolicyPartialIdAppsAppTypeGet(Integer policyPartialId,String appType,Integer limit,Integer offset,String accept,String ifNoneMatch);
}

