package org.wso2.carbon.appmgt.rest.api.store;

import javax.ws.rs.core.Response;

public abstract class BusinessOwnerApiService {
    public abstract Response businessOwnerBusinessOwnerIdGet(Integer businessOwnerId, String accept,
                                                             String ifNoneMatch);


}
