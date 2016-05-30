package org.wso2.carbon.appmgt.rest.api.store.factories;

import org.wso2.carbon.appmgt.rest.api.store.AdministrationApiService;
import org.wso2.carbon.appmgt.rest.api.store.impl.AdministrationApiServiceImpl;

public class AdministrationApiServiceFactory {

   private final static AdministrationApiService service = new AdministrationApiServiceImpl();

   public static AdministrationApiService getAdministrationApi()
   {
      return service;
   }
}
