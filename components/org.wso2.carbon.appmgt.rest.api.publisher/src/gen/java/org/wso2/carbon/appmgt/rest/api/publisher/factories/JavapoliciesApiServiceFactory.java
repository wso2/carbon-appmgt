package org.wso2.carbon.appmgt.rest.api.publisher.factories;

import org.wso2.carbon.appmgt.rest.api.publisher.JavapoliciesApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.impl.JavapoliciesApiServiceImpl;

public class JavapoliciesApiServiceFactory {

   private final static JavapoliciesApiService service = new JavapoliciesApiServiceImpl();

   public static JavapoliciesApiService getJavapoliciesApi()
   {
      return service;
   }
}
