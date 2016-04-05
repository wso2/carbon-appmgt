package org.wso2.carbon.appmgt.rest.api.store.impl;

import org.wso2.carbon.appmgt.rest.api.store.DevicesApiService;

import javax.ws.rs.core.Response;

public class DevicesApiServiceImpl extends DevicesApiService {
    @Override
    public Response devicesGet(String query,Integer limit,Integer offset,String accept,String ifNoneMatch){
        // do some magic!
        return null;//Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
