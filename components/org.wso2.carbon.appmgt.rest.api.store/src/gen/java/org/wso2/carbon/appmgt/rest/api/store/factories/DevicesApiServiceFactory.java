package org.wso2.carbon.appmgt.rest.api.store.factories;

import org.wso2.carbon.appmgt.rest.api.store.DevicesApiService;
import org.wso2.carbon.appmgt.rest.api.store.impl.DevicesApiServiceImpl;

public class DevicesApiServiceFactory {

   private final static DevicesApiService service = new DevicesApiServiceImpl();

   public static DevicesApiService getDevicesApi()
   {
      return service;
   }
}
