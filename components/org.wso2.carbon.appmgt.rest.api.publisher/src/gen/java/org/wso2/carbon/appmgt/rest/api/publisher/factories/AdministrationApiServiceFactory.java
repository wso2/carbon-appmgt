package org.wso2.carbon.appmgt.rest.api.publisher.factories;

import org.wso2.carbon.appmgt.rest.api.publisher.AdministrationApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.impl.AdministrationApiServiceImpl;

public class AdministrationApiServiceFactory {

   private final static AdministrationApiService service = new AdministrationApiServiceImpl();

   public static AdministrationApiService getAdministrationApi()
   {
      return service;
   }
}
