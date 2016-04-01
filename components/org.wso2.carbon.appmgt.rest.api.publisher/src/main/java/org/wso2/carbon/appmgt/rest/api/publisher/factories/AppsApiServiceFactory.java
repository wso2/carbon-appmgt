package org.wso2.carbon.appmgt.rest.api.publisher.factories;

import org.wso2.carbon.appmgt.rest.api.publisher.AppsApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.impl.AppsApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-03-31T12:45:14.954Z")
public class AppsApiServiceFactory {

   private final static AppsApiService service = new AppsApiServiceImpl();

   public static AppsApiService getAppsApi()
   {
      return service;
   }
}
