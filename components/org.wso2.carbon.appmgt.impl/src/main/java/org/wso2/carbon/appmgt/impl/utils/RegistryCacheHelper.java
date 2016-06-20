/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.service.RegistryCacheInvalidationService;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

/**
 * Helper class to clean the registry resource cache.
 */
public class RegistryCacheHelper {

    private static final Log log = LogFactory.getLog(RegistryCacheHelper.class);

    public static void cleanRegistryResourceCache(RequestContext requestContext) {
        try {
            RegistryCacheInvalidationService registryCacheCleaner = new RegistryCacheInvalidationService();
            registryCacheCleaner.invalidateRegistryCache(requestContext);
        } catch (AppManagementException e) {
            log.error("Unable to invalidate the registry cache. This might cause some stale data in the store/publisher", e);
        }
    }

    public static void cleanRegistryResourceCache(String resourcePath) {
        try {
            RegistryCacheInvalidationService registryCacheCleaner = new RegistryCacheInvalidationService();
            RequestContext requestContext = new RequestContext(null, null, null);
            ResourceImpl resource = new ResourceImpl();
            resource.setPath(resourcePath);
            requestContext.setResource(resource);
            registryCacheCleaner.invalidateRegistryCache(requestContext);
        } catch (AppManagementException e) {
            log.error("Unable to invalidate the registry cache. This might cause some stale data in the store/publisher", e);
        }
    }
}