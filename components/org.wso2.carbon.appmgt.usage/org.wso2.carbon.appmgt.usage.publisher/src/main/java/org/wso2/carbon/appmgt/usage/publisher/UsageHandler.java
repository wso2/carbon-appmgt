/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.usage.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.appmgt.usage.publisher.internal.APPManagerConfigurationServiceComponent;

import java.util.Map;

public class UsageHandler extends AbstractHandler {

    private static final Log log   = LogFactory.getLog(UsageHandler.class);

    private Map<String, Boolean> enabledEngines = APPManagerConfigurationServiceComponent.
            getApiMgtConfigReaderService().getEnabledAnalyticsEngines();

    public boolean handleRequest(MessageContext messageContext) {
        // call all enabled handlers
        for(Map.Entry<String, Boolean> engine: enabledEngines.entrySet()) {
            if(engine.getValue()) { // engine is enabled
                AbstractHandler analyticsHandler = null;
                try{
                    long currentTime = System.currentTimeMillis();

                    synchronized (this){
                        try {
                            log.debug("Instantiating Usage Handler");
                            analyticsHandler = (AbstractHandler) Class.forName(APIMgtUsagePublisherConstants.ENGINE_TO_CLASS_MAP.get(engine.getKey())).newInstance();
                            analyticsHandler.handleRequest(messageContext);
                        } catch (ClassNotFoundException e) {
                            log.error("Class not found " + APIMgtUsagePublisherConstants.ENGINE_TO_CLASS_MAP.get(engine.getKey()));
                        } catch (InstantiationException e) {
                            log.error("Error instantiating " + APIMgtUsagePublisherConstants.ENGINE_TO_CLASS_MAP.get(engine.getKey()));
                        } catch (IllegalAccessException e) {
                            log.error("Illegal access to " + APIMgtUsagePublisherConstants.ENGINE_TO_CLASS_MAP.get(engine.getKey()));
                        }
                    }
                }catch (Throwable e){
                    log.error("Cannot publish usage data. " + e.getMessage(), e);
                }
            }
        }

        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        // call all enabled handlers
        for(Map.Entry<String, Boolean> engine: enabledEngines.entrySet()) {
            if(engine.getValue()) { // engine is enabled
                AbstractHandler analyticsHandler = null;
                try{
                    long currentTime = System.currentTimeMillis();

                    synchronized (this){
                        try {
                            log.debug("Instantiating Usage Handler");
                            analyticsHandler = (AbstractHandler) Class.forName(APIMgtUsagePublisherConstants.ENGINE_TO_CLASS_MAP.get(engine.getKey())).newInstance();
                            analyticsHandler.handleResponse(messageContext);
                        } catch (ClassNotFoundException e) {
                            log.error("Class not found " + APIMgtUsagePublisherConstants.ENGINE_TO_CLASS_MAP.get(engine.getKey()));
                        } catch (InstantiationException e) {
                            log.error("Error instantiating " + APIMgtUsagePublisherConstants.ENGINE_TO_CLASS_MAP.get(engine.getKey()));
                        } catch (IllegalAccessException e) {
                            log.error("Illegal access to " + APIMgtUsagePublisherConstants.ENGINE_TO_CLASS_MAP.get(engine.getKey()));
                        }
                    }
                }catch (Throwable e){
                    log.error("Cannot publish usage data. " + e.getMessage(), e);
                }
            }
        }

        return true;
    }
}
