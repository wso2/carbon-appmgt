package org.wso2.carbon.appmgt.rest.api.store.factories;

import org.wso2.carbon.appmgt.rest.api.store.AppsApiService;
import org.wso2.carbon.appmgt.rest.api.store.impl.AppsApiServiceImpl;

public class AppsApiServiceFactory {

   private final static AppsApiService service = new AppsApiServiceImpl();

   public static AppsApiService getAppsApi()
   {
      return service;
   }
}
