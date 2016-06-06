package org.wso2.carbon.appmgt.rest.api.store;

import org.wso2.carbon.appmgt.rest.api.store.dto.InstallDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.ScheduleDTO;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class AppsApiService {
    public abstract Response appsDownloadPost(String contentType, InstallDTO install);

    public abstract Response appsMobileBinariesFileNameGet(String fileName);

    public abstract Response appsUninstallationPost(String contentType, InstallDTO install);

    public abstract Response appsAppTypeGet(String appType, String query, String fieldFilter, Integer limit,
                                            Integer offset, String accept, String ifNoneMatch);

    public abstract Response appsAppTypeIdAppIdGet(String appType, String appId, String accept, String ifNoneMatch,
                                                   String ifModifiedSince);

    public abstract Response appsMobileScheduleInstallPost(String contentType, ScheduleDTO schedule,
                                                           SecurityContext securityContext);

    public abstract Response appsMobileScheduleUpdatePost(String contentType, ScheduleDTO schedule,
                                                          SecurityContext securityContext);
}

