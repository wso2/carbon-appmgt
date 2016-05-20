package org.wso2.carbon.appmgt.rest.api.store;

import org.wso2.carbon.appmgt.rest.api.store.*;
import org.wso2.carbon.appmgt.rest.api.store.dto.*;

import org.wso2.carbon.appmgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.AdminInstallDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.RoleIdListDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.UserIdListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class AdministrationApiService {
    public abstract Response administrationAppsDownloadPost(String contentType,AdminInstallDTO install);
    public abstract Response administrationAppsUninstallationPost(String contentType,AdminInstallDTO install);
    public abstract Response administrationRolesGet(Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract Response administrationUsersGet(Integer limit,Integer offset,String accept,String ifNoneMatch);
}

