package org.wso2.carbon.appmgt.rest.api.storeadmin;

import org.wso2.carbon.appmgt.rest.api.storeadmin.*;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.*;

import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.UserListDTO;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class UsersApiService {
    public abstract Response usersGet(Integer limit,Integer offset,String accept,String ifNoneMatch);
}

