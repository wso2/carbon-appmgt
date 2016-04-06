package org.wso2.carbon.appmgt.rest.api.storeadmin.factories;

import org.wso2.carbon.appmgt.rest.api.storeadmin.RolesApiService;
import org.wso2.carbon.appmgt.rest.api.storeadmin.impl.RolesApiServiceImpl;

public class RolesApiServiceFactory {

   private final static RolesApiService service = new RolesApiServiceImpl();

   public static RolesApiService getRolesApi()
   {
      return service;
   }
}
