package org.wso2.carbon.appmgt.rest.api.publisher;

import org.wso2.carbon.appmgt.rest.api.publisher.*;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.*;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyGroupDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class PolicygroupsApiService {
    public abstract Response policygroupsPost(PolicyGroupDTO body,String contentType,String ifModifiedSince);
    public abstract Response policygroupsPolicyGroupIdPut(Integer policyGroupId,PolicyGroupDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
}

