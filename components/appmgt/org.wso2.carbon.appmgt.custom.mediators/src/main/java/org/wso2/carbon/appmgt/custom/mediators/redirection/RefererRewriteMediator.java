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
import java.util.ArrayList;
import java.util.Map;

public class RefererRewriteMediator extends AbstractMediator{

    /**
     *
     * @param messageContext
     * @return
     */
    public boolean mediate(MessageContext messageContext) {

        Cache cache = this.getAppContextVersionConfigCache();
        ArrayList<String> contextVersionList = (ArrayList) cache.get
                (AppMConstants.APP_CONTEXT_VERSION_CACHE_KEY);

        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext). getAxis2MessageContext();
        Map map = (Map)axis2MC.getProperty(AppMConstants.TRANSPORT_HEADERS);
                String host = (String) map.get(AppMConstants.HOST);
        String referer = (String)map.get(AppMConstants.REFERER);

        for (String contextVersion : contextVersionList){
            if (referer.contains(contextVersion)){
                referer = AppMConstants.HTTP + host + contextVersion;
                break;
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
