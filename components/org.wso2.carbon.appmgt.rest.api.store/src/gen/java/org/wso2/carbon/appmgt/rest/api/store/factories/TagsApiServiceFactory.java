package org.wso2.carbon.appmgt.rest.api.store.factories;

import org.wso2.carbon.appmgt.rest.api.store.TagsApiService;
import org.wso2.carbon.appmgt.rest.api.store.impl.TagsApiServiceImpl;

public class TagsApiServiceFactory {

   private final static TagsApiService service = new TagsApiServiceImpl();

   public static TagsApiService getTagsApi()
   {
      return service;
   }
}
