package org.wso2.carbon.appmgt.rest.api.publisher;

import org.wso2.carbon.appmgt.rest.api.publisher.*;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.*;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.JavaPolicyListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class JavapoliciesApiService {
    public abstract Response javapoliciesGet(Boolean isGlobal,String accept,String ifNoneMatch,String ifModifiedSince);
}

