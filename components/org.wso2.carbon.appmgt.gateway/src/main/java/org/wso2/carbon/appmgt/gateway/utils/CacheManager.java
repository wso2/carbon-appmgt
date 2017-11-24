/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.gateway.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.gateway.handlers.security.Session;
import org.wso2.carbon.appmgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.AppMConstants;

import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheConfiguration;
import javax.cache.CacheException;
import javax.cache.Caching;

// A wrapper to class to get App Manager caches.
public class CacheManager {

    public static final Log log = LogFactory.getLog(CacheManager.class);

    private static CacheManager instance;

    private static final long DEFAULT_GATEWAY_SESSION_TIMEOUT = 1800L;
    private static long sessionCacheTimeout;

    private CacheManager(){
        initCaches();
    }

    private void initCaches() {

        String sessionCacheTimeoutPropertyValue = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
                                                    getFirstProperty(AppMConstants.GATEWAY_SESSION_TIMEOUT);

        sessionCacheTimeout = DEFAULT_GATEWAY_SESSION_TIMEOUT;

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
        try {
            CacheBuilder cacheBuilder = Caching.getCacheManager(AppMConstants.GATEWAY_CACHE_MANAGER).
                    createCacheBuilder(AppMConstants.GATEWAY_SESSION_INDEX_MAPPING_CACHE);
            return cacheBuilder.setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, sessionCacheTimeout)).
                    setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.Duration(TimeUnit.SECONDS, sessionCacheTimeout)).
                    setStoreByValue(false).build();
        } catch (CacheException e) {    //Cache already exists
            return Caching.getCacheManager(AppMConstants.GATEWAY_CACHE_MANAGER).getCache(AppMConstants.GATEWAY_SESSION_INDEX_MAPPING_CACHE);
        }
    }

    public Cache<String, Session> getSessionCache(){
        try {
            CacheBuilder cacheBuilder = Caching.getCacheManager(AppMConstants.GATEWAY_CACHE_MANAGER).
                    createCacheBuilder(AppMConstants.GATEWAY_SESSION_CACHE);
             return cacheBuilder.setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, sessionCacheTimeout)).
                     setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.Duration(TimeUnit.SECONDS, sessionCacheTimeout)).
                     setStoreByValue(false).build();
        } catch (CacheException e) {    //Cache already exists
            return Caching.getCacheManager(AppMConstants.GATEWAY_CACHE_MANAGER).getCache(AppMConstants.GATEWAY_SESSION_CACHE);
        }
    }
}
