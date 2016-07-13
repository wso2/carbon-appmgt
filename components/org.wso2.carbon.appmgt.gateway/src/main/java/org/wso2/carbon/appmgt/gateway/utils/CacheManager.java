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
import org.wso2.carbon.appmgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.AppMConstants;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.Caching;
import java.util.concurrent.TimeUnit;

// A wrapper to class to get App Manager caches.
public class CacheManager {

    private static CacheManager instance;

    private static final long DEFAULT_GATEWAY_SESSION_TIMEOUT = 1800L;

    private CacheManager(){
        initCaches();
    }

    private void initCaches() {

        String sessionCacheTimeoutPropertyValue = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
                                                    getFirstProperty(AppMConstants.GATEWAY_SESSION_TIMEOUT);

        long sessionCacheTimeout = DEFAULT_GATEWAY_SESSION_TIMEOUT;

        if(sessionCacheTimeoutPropertyValue != null){
            sessionCacheTimeout = Long.parseLong(sessionCacheTimeoutPropertyValue);
        }


        Caching.getCacheManager(AppMConstants.GATEWAY_CACHE_MANAGER).
                createCacheBuilder(AppMConstants.GATEWAY_SESSION_CACHE).
                setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, sessionCacheTimeout)).
                setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.Duration(TimeUnit.SECONDS, sessionCacheTimeout)).
                setStoreByValue(false).build();

        Caching.getCacheManager(AppMConstants.GATEWAY_CACHE_MANAGER).
                createCacheBuilder(AppMConstants.GATEWAY_SESSION_INDEX_MAPPING_CACHE).
                setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, sessionCacheTimeout)).
                setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.Duration(TimeUnit.SECONDS, sessionCacheTimeout)).
                setStoreByValue(false).build();


    }


    public static CacheManager getInstance() {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) {
                    instance = new CacheManager();
                }
            }
        }
        return instance;
    }

    public Cache<String, String> getSessionIndexMappingCache(){
        return Caching.getCacheManager(AppMConstants.GATEWAY_CACHE_MANAGER).getCache(AppMConstants.GATEWAY_SESSION_INDEX_MAPPING_CACHE);
    }

    public Cache<String, Session> getSessionCache(){
        return Caching.getCacheManager(AppMConstants.GATEWAY_CACHE_MANAGER).getCache(AppMConstants.GATEWAY_SESSION_CACHE);
    }


}
