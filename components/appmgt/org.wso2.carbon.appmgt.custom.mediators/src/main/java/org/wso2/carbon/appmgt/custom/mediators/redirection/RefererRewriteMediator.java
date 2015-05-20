/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.appmgt.custom.mediators.redirection;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.appmgt.impl.AppMConstants;

import javax.cache.Cache;
import javax.cache.Caching;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RefererRewriteMediator extends AbstractMediator{

    /**
     *
     * @param messageContext
     * @return
     */
    public boolean mediate(MessageContext messageContext) {

        Cache cache = this.getAppContextVersionConfigCache();
        Map<String,String> contextVersionMap = (HashMap<String, String>) cache.get
                (AppMConstants.APP_CONTEXT_VERSION_CACHE_KEY);
        //ToDo: handle if cache has expired

        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext). getAxis2MessageContext();
        Map map = (Map)axis2MC.getProperty(AppMConstants.TRANSPORT_HEADERS);
                String host = (String) map.get(AppMConstants.HOST);
        String referer = (String)map.get(AppMConstants.REFERER);

        //Identify http/https
        String transport = referer.split(AppMConstants.URL_DELIMITER)[0];
        String endpoint;
        String restFullRequestPath = (String) messageContext.getProperty("REST_FULL_REQUEST_PATH");
        for (String contextVersion : contextVersionMap.keySet()){
            if (referer.contains(contextVersion)){
                endpoint = contextVersionMap.get(contextVersion);
                try {
                    URL epUrl = new URL(endpoint);
                    if (restFullRequestPath.startsWith(epUrl.getPath())) {
                        referer = transport + AppMConstants.URL_DELIMITER + host + contextVersion +
                                  "/" + restFullRequestPath.substring(epUrl.getPath().length());
                    } else {
                        referer = transport + AppMConstants.URL_DELIMITER + host + contextVersion +
                                "/" + restFullRequestPath;
                    }
                    break;
                } catch (MalformedURLException e) {
                    log.error("Error while parsing endpoint url : " + endpoint, e);
                }
            }
        }

        map.put(AppMConstants.REFERER,referer);
        axis2MC.setProperty(AppMConstants.TRANSPORT_HEADERS , map);

        return true;
    }

    /**
     * get the cache manager
     * @return
     */
    private Cache getAppContextVersionConfigCache() {
        return Caching.getCacheManager(AppMConstants.APP_CONTEXT_VERSION_CACHE_MANAGER)
                .getCache(AppMConstants.APP_CONTEXT_VERSION_CONFIG_CACHE);
    }
}
