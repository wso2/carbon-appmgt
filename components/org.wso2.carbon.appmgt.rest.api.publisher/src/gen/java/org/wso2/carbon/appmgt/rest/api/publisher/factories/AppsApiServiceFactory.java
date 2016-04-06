package org.wso2.carbon.appmgt.rest.api.publisher.factories;

import org.wso2.carbon.appmgt.rest.api.publisher.AppsApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.impl.AppsApiServiceImpl;

public class AppsApiServiceFactory {

   private final static AppsApiService service = new AppsApiServiceImpl();

   public static AppsApiService getAppsApi()
   {
      return service;
   }
}
