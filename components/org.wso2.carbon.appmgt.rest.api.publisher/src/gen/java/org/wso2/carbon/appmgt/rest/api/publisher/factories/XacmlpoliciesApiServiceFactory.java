package org.wso2.carbon.appmgt.rest.api.publisher.factories;

import org.wso2.carbon.appmgt.rest.api.publisher.XacmlpoliciesApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.impl.XacmlpoliciesApiServiceImpl;

public class XacmlpoliciesApiServiceFactory {

   private final static XacmlpoliciesApiService service = new XacmlpoliciesApiServiceImpl();

   public static XacmlpoliciesApiService getXacmlpoliciesApi()
   {
      return service;
   }
}
