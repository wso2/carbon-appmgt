package org.wso2.carbon.appmgt.rest.api.storeadmin;

import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.InstallDTO;

import javax.ws.rs.core.Response;

public abstract class AppsApiService {
    public abstract Response appsDownloadPost(String contentType,InstallDTO install);
    public abstract Response appsUninstallationPost(String contentType,InstallDTO install);
}

