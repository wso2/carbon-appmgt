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

package org.wso2.carbon.appmgt.gateway.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.AppManagerConfigurationService;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.gateway.internal.TenantServiceCreator;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

import javax.cache.Cache;
import java.util.List;

/**
 *
 * Application manager gateway component
 *
 * @scr.component name="org.wso2.apimgt.impl.services.gateway" immediate="true"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.appmgt.impl.AppManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 */
public class AppManagerGatewayComponent {

    private static final Log log = LogFactory.getLog(AppManagerGatewayComponent.class);

    private static AppManagerConfiguration configuration = null;

    protected void activate(ComponentContext componentContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Gateway manager component activated");
        }
        BundleContext bundleContext = componentContext.getBundleContext();

        //Register Tenant service creator to deploy tenant specific common synapse configurations
        TenantServiceCreator listener = new TenantServiceCreator();
        bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), listener, null);

        //Load initially available api contexts at the server startup. This Cache is only use by the products other than the app-manager
        /* TODO: Load Config values from apimgt.core*/
        boolean apiManagementEnabled = AppManagerUtil.isAPIManagementEnabled();
        boolean loadAPIContextsAtStartup = AppManagerUtil.isLoadAPIContextsAtStartup();
        try {
            if (apiManagementEnabled && loadAPIContextsAtStartup) {
                List<String> contextList = AppMDAO.getAllAvailableContexts();
                Cache contextCache = AppManagerUtil.getAPIContextCache();
                for (String context : contextList) {
                    contextCache.put(context, true);
                }
            }
        } catch (Exception e) {
            //TODO: This is another quick hack. Need to move the code to better startup on gateway.
            log.debug("Error occurred loading Existing API contexts:" + e.getMessage());
        }

    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating Gateway manager component");
        }
    }

    protected void setAPIManagerConfigurationService(AppManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("Gateway manager configuration service bound to the WebApp host objects");
        }
        configuration = amcService.getAPIManagerConfiguration();
        org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(AppManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("Gateway manager configuration service unbound from the WebApp host objects");
        }
        configuration = null;
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }

}
