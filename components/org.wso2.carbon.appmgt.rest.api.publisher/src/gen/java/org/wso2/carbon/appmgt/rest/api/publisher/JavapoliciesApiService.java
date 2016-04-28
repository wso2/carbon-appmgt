package org.wso2.carbon.appmgt.rest.api.publisher;

import org.wso2.carbon.appmgt.rest.api.publisher.*;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.*;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.JavaPolicyDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class JavapoliciesApiService {
    public abstract Response javapoliciesAppidAppIdGet(String appId,String accept,String ifNoneMatch,String ifModifiedSince);
}

