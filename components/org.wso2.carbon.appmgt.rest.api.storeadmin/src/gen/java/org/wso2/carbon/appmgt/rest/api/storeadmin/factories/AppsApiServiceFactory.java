package org.wso2.carbon.appmgt.rest.api.storeadmin.factories;

import org.wso2.carbon.appmgt.rest.api.storeadmin.AppsApiService;
import org.wso2.carbon.appmgt.rest.api.storeadmin.impl.AppsApiServiceImpl;

public class AppsApiServiceFactory {

   private final static AppsApiService service = new AppsApiServiceImpl();

   public static AppsApiService getAppsApi()
   {
      return service;
   }
}
