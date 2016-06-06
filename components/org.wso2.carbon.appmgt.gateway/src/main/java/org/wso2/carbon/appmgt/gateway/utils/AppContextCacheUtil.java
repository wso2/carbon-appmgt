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
import org.apache.synapse.SynapseException;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.utils.APIMgtDBUtil;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility class for accessing Application contexts cache.
 * This is primarily used in mediators/handlers in gateway.
 *
 * TODO: Usage of the cache makes the caller thread to delay significantly when cache miss.
 * We might need better handling of this functionality in order to get a good performance.
 */
public class AppContextCacheUtil {

    private static final Log log = LogFactory.getLog(AppContextCacheUtil.class);

    /**
     * Returns the </t/tenantdomain/context/version> or </context/version> to its URL map.
     * @return
     */
    public static Map<String, String> getTenantContextVersionUrlMap() {
        Cache cache = getAppContextVersionConfigCache();
        Map<String, String> contextVersionMap = (HashMap<String, String>) cache
                .get(AppMConstants.APP_CONTEXT_VERSION_CACHE_KEY);
        if (contextVersionMap == null) {
            //Set the app context+version list in cache
            contextVersionMap = getAppContextWithVersion();
            getAppContextVersionConfigCache()
                    .put(AppMConstants.APP_CONTEXT_VERSION_CACHE_KEY, contextVersionMap);
        }
        return contextVersionMap;
    }

    /**
     * get the cache manager
     * @return
     */
    private static Cache getAppContextVersionConfigCache() {
        return Caching.getCacheManager(AppMConstants.APP_CONTEXT_VERSION_CACHE_MANAGER)
                .getCache(AppMConstants.APP_CONTEXT_VERSION_CONFIG_CACHE);
    }

    /**
     * retrieving context+version string list from database.
     * @return context+version list
     */
    private static Map<String, String> getAppContextWithVersion() {
        if (log.isDebugEnabled()) {
            log.debug("Calling getAppContextWithVersion");
        }
        try {
            APIMgtDBUtil.initialize();
            AppMDAO dao = new AppMDAO();
            Map<String, String> contextVersion = new HashMap<String, String>();
            Iterator listI = dao.getAllWebApps().listIterator();

            while (listI.hasNext()) {
                WebApp app = (WebApp) listI.next();
                String appVersion = app.isDefaultVersion() ? "/" + app.getId().getVersion() : "";
                contextVersion.put((app.getContext().startsWith("/") ?
                        app.getContext() :
                        "/" + app.getContext()) + appVersion, app.getUrl());
            }
            return contextVersion;
        } catch (AppManagementException e) {
            throw new SynapseException(
                    "Error while retrieving context+version list from database\n" + "Error : " + e
                            .getMessage());
        } catch (Exception e) {
            throw new SynapseException(
                    "Error while initialize database util\n" + "Error : " + e.getMessage());
        }
    }
}
