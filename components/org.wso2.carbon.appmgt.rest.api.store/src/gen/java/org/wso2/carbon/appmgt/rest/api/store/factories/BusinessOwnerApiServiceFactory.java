package org.wso2.carbon.appmgt.rest.api.store.factories;

import org.wso2.carbon.appmgt.rest.api.store.BusinessOwnerApiService;
import org.wso2.carbon.appmgt.rest.api.store.impl.BusinessOwnerApiServiceImpl;

public class BusinessOwnerApiServiceFactory {

    private final static BusinessOwnerApiService service = new BusinessOwnerApiServiceImpl();

    public static BusinessOwnerApiService getBusinessOwnerApi() {
        return service;
    }
}
