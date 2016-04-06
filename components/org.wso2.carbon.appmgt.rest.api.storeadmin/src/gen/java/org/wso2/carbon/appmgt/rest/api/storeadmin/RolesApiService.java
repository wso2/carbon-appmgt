package org.wso2.carbon.appmgt.rest.api.storeadmin;

import javax.ws.rs.core.Response;

public abstract class RolesApiService {
    public abstract Response rolesGet(Integer limit,Integer offset,String accept,String ifNoneMatch);
}

