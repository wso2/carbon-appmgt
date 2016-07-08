/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   you may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.appmgt.gateway.utils;

import org.wso2.carbon.appmgt.gateway.handlers.security.Session;
import org.wso2.carbon.appmgt.impl.AppMConstants;

import javax.cache.Cache;
import javax.cache.Caching;

// A wrapper to class to get App Manager caches.
public class CacheManager {

    public static Cache<String, String> getSessionIndexMappingCache(){
        return Caching.getCacheManager(AppMConstants.GATEWAY_CACHE_MANAGER).getCache(AppMConstants.GATEWAY_SESSION_INDEX_MAPPING_CACHE);
    }

    public static Cache<String, Session> getSessionCache(){
        return Caching.getCacheManager(AppMConstants.GATEWAY_CACHE_MANAGER).getCache(AppMConstants.GATEWAY_SESSION_CACHE);
    }

}
