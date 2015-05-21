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
import org.wso2.carbon.appmgt.impl.utils.AppContextCacheUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class RefererRewriteMediator extends AbstractMediator {

    /**
     *
     * @param messageContext
     * @return
     */
    public boolean mediate(MessageContext messageContext) {

        Map<String, String> contextVersionMap = AppContextCacheUtil.getTenantContextVersionUrlMap();
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        Map map = (Map) axis2MC.getProperty(AppMConstants.TRANSPORT_HEADERS);
        String host = (String) map.get(AppMConstants.HOST);
        String referer = (String) map.get(AppMConstants.REFERER);

        //Identify http/https
        String transport = referer.split(AppMConstants.URL_DELIMITER)[0];
        String endpoint;
        String restFullRequestPath = (String) messageContext.getProperty("REST_FULL_REQUEST_PATH");
        for (String contextVersion : contextVersionMap.keySet()) {
            //TODO: This iteration is too slow. we need a better data structure for this.
            if (referer.contains(contextVersion)) {
                endpoint = contextVersionMap.get(contextVersion);
                try {
                    URL epUrl = new URL(endpoint);
                    if (restFullRequestPath.startsWith(epUrl.getPath())) {
                        referer = transport + AppMConstants.URL_DELIMITER + host + contextVersion +
                                "/" +
                                stripLeadingSlash(
                                        restFullRequestPath.substring(epUrl.getPath().length()));
                    } else {
                        referer = transport + AppMConstants.URL_DELIMITER + host + contextVersion +
                                "/" + stripLeadingSlash(restFullRequestPath);
                    }
                    break;
                } catch (MalformedURLException e) {
                    log.error("Error while parsing endpoint url : " + endpoint, e);
                }
            }
        }

        map.put(AppMConstants.REFERER, referer);
        axis2MC.setProperty(AppMConstants.TRANSPORT_HEADERS, map);

        return true;
    }

    private String stripLeadingSlash(String urlString) {
        if (urlString == null || urlString.length() == 0) {
            return urlString;
        }

        if (urlString.startsWith("/")) {
            urlString = urlString.substring(1, urlString.length());
        }
        return urlString;
    }
}
