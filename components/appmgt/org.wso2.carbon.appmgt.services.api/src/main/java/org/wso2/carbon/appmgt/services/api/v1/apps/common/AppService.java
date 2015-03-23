package org.wso2.carbon.appmgt.services.api.v1.apps.common;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

public  interface AppService  {

    public AppListResponse getApplicationList(@Context final HttpServletResponse servletResponse, @Context HttpHeaders headers, @PathParam("tenantDomain") String tenantDomain, @QueryParam("limit") int limit, @QueryParam("offset") int offset);

}
