package org.wso2.carbon.appmgt.rest.api.publisher.factories;

import org.wso2.carbon.appmgt.rest.api.publisher.PolicygroupsApiService;
import org.wso2.carbon.appmgt.rest.api.publisher.impl.PolicygroupsApiServiceImpl;

public class PolicygroupsApiServiceFactory {

   private final static PolicygroupsApiService service = new PolicygroupsApiServiceImpl();

   public static PolicygroupsApiService getPolicygroupsApi()
   {
      return service;
   }
}
