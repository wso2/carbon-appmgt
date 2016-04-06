package org.wso2.carbon.appmgt.rest.api.storeadmin.factories;

import org.wso2.carbon.appmgt.rest.api.storeadmin.UsersApiService;
import org.wso2.carbon.appmgt.rest.api.storeadmin.impl.UsersApiServiceImpl;

public class UsersApiServiceFactory {

   private final static UsersApiService service = new UsersApiServiceImpl();

   public static UsersApiService getUsersApi()
   {
      return service;
   }
}
