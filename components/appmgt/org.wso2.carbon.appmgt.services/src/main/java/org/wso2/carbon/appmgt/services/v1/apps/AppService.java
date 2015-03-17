package org.wso2.carbon.appmgt.services.v1.apps;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

public  interface AppService  {

    public AppListResponse getApplicationList(@PathParam("tenantId") int tenantId, @QueryParam("limit") int limit, @QueryParam("offset") int offset);

}
