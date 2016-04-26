package org.wso2.carbon.appmgt.rest.api.publisher;

import org.wso2.carbon.appmgt.rest.api.publisher.*;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.*;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyPartialDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class AdministrationApiService {
    public abstract Response administrationXacmlpoliciesPost(PolicyPartialDTO body,String contentType,String ifModifiedSince);
    public abstract Response administrationXacmlpoliciesValidatePost(PolicyPartialDTO body,String contentType,String ifModifiedSince);
    public abstract Response administrationXacmlpoliciesPolicyPartialIdGet(Integer policyPartialId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response administrationXacmlpoliciesPolicyPartialIdPut(Integer policyPartialId,PolicyPartialDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response administrationXacmlpoliciesPolicyPartialIdDelete(Integer policyPartialId,String ifMatch,String ifUnmodifiedSince);
}

