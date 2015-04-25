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

package org.wso2.carbon.appmgt.impl.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.discovery.ApplicationDiscoveryHandler;
import org.wso2.carbon.appmgt.impl.discovery.ApplicationDiscoveryServiceFactory;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.Map;

/**
 * Application discovery service component. This will allow discover api to be exposed via factory
 * so that many kind of back-ends could be adopted.
 *
 * @scr.component name="org.wso2.appmgt.impl.services" immediate="true"
 */
public class AppDiscoveryComponent {

    private static final Log log = LogFactory.getLog(AppDiscoveryComponent.class);

    private ServiceRegistration registration;

    protected void activate(ComponentContext componentContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("WebApp Discovery component is being activated");
        }

        AppDiscoveryConfiguration configuration = loadConfig();
        ApplicationDiscoveryServiceFactory applicationDiscoveryServiceFactory = createFactory(
                configuration);

        registration = componentContext.getBundleContext()
                .registerService(ApplicationDiscoveryServiceFactory.class,
                        applicationDiscoveryServiceFactory, null);

        log.info("WebApp Discovery component activated");

    }

    /**
     * Loads the configuration related to application discovery
     * Configurations are read from system-wide app-manager.xml.
     *
     * @return
     */
    private AppDiscoveryConfiguration loadConfig() {
        String filePath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf" + File.separator + "app-manager.xml";
        AppDiscoveryConfiguration discoveryConfiguration = new AppDiscoveryConfiguration();
        try {
            discoveryConfiguration.load(filePath);
        } catch (AppManagementException e) {
            log.error("Error occurred while initializing App Manager Discovery"
                    + " configuration Service Component from file path : " +
                    filePath, e);
        }

        return discoveryConfiguration;
    }

    /**
     * Creates the application discovery factory given the discovery configuration
     *
     * @param configuration
     * @return
     */
    private ApplicationDiscoveryServiceFactory createFactory(
            AppDiscoveryConfiguration configuration) {
        ApplicationDiscoveryServiceFactory applicationDiscoveryServiceFactory = new ApplicationDiscoveryServiceFactory();

        Map<String, String> handlerMap = configuration.getHandlersMap();
        for (String name : handlerMap.keySet()) {
            String value = handlerMap.get(name);
            try {
                Class clazz = Class.forName(value);
                Object o = clazz.newInstance();
                ApplicationDiscoveryHandler handler = (ApplicationDiscoveryHandler) o;
                applicationDiscoveryServiceFactory.addHandler(name, handler);
            } catch (Exception e) {
                log.error("Could not load create the handler for the handler class: " + value, e);
            }
        }

        return applicationDiscoveryServiceFactory;
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating WebApp manager component");
        }
        if (registration != null) {
            registration.unregister();
        }
    }
}
