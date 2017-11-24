/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.usage.publisher.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.AppManagerConfigurationService;
import org.wso2.carbon.appmgt.impl.AppManagerConfigurationServiceImpl;
import org.wso2.carbon.appmgt.usage.publisher.service.APIMGTConfigReaderService;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @scr.component name="org.wso2.carbon.appmgt.usage.publisher.services" immediate="true"
 * @scr.reference name="tomcat.service.provider"
 * interface="org.wso2.carbon.tomcat.api.CarbonTomcatService"
 * cardinality="1..1" policy="dynamic" bind="setCarbonTomcatService"
 * unbind="unsetCarbonTomcatService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class APPManagerConfigurationServiceComponent {

    private static final Log log = LogFactory.getLog(APPManagerConfigurationServiceComponent.class);

    private static APIMGTConfigReaderService apimgtConfigReaderService;
    private static AppManagerConfigurationService amConfigService;
    private static Map<String, DataPublisher> dataPublisherMap;

    protected void activate(ComponentContext componentContext) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("App Manager usage configuration service component activation started");
        }
        String filePath = null;
        try {
            AppManagerConfiguration configuration = new AppManagerConfiguration();
            filePath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "app-manager.xml";
            configuration.load(filePath);
            amConfigService = new AppManagerConfigurationServiceImpl(configuration);
            apimgtConfigReaderService = new APIMGTConfigReaderService(amConfigService.getAPIManagerConfiguration());
            dataPublisherMap = new ConcurrentHashMap<String, DataPublisher>();

            if (log.isDebugEnabled()) {
                log.debug("WebApp Management Usage Publisher bundle is activated from file path : " + filePath);
            }
        } catch (AppManagementException e) {
            log.error("Error occurred while initializing App Manager usage configuration service component from file path : " +
                    filePath, e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating App Manager usage configuration service component");
        }
        amConfigService = null;
        apimgtConfigReaderService = null;
        dataPublisherMap = null;
    }

    protected void setAPIManagerConfigurationService(AppManagerConfigurationService service) {
        log.debug("WebApp manager configuration service bound to the WebApp usage handler");
        amConfigService = service;
    }

    protected void unsetAPIManagerConfigurationService(AppManagerConfigurationService service) {
        log.debug("WebApp manager configuration service unbound from the WebApp usage handler");
        amConfigService = null;
    }

    public static APIMGTConfigReaderService getApiMgtConfigReaderService() {
        return apimgtConfigReaderService;
    }

    public static Map<String, DataPublisher> getDataPublisherMap() {
        return dataPublisherMap;
    }

    protected void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        UsageComponent.setCarbonTomcatService(carbonTomcatService);
    }


    protected void unsetCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        UsageComponent.setCarbonTomcatService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService configCtx) {
        UsageComponent.setConfigContextService(configCtx);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtx) {
        UsageComponent.setConfigContextService(null);
    }
}

