package org.wso2.carbon.appmgt.rest.api.publisher.factories;

import org.wso2.carbon.appmgt.rest.api.publisher.RolesApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.impl.RolesApiServiceImpl;

public class RolesApiServiceFactory {

   private final static RolesApiService service = new RolesApiServiceImpl();

   public static RolesApiService getRolesApi()
   {
      return service;
   }
}
