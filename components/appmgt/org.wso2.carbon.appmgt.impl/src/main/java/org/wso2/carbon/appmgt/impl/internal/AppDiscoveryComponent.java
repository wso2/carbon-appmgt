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

import org.apache.axis2.engine.ListenerManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.*;
import org.wso2.carbon.appmgt.impl.discovery.ApplicationDiscoveryServiceFactory;
import org.wso2.carbon.appmgt.impl.discovery.Wso2AppServerDiscoveryHandler;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.FileUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

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
            log.debug("WebApp Discovery component activated");
        }

        ApplicationDiscoveryServiceFactory applicationDiscoveryServiceFactory =
                new ApplicationDiscoveryServiceFactory();

        Wso2AppServerDiscoveryHandler wso2AppServerDiscoveryHandler = new Wso2AppServerDiscoveryHandler();
        applicationDiscoveryServiceFactory.addHandler(wso2AppServerDiscoveryHandler.getDisplayName(),
                wso2AppServerDiscoveryHandler);

        registration = componentContext.getBundleContext().registerService(
                ApplicationDiscoveryServiceFactory.class,
                applicationDiscoveryServiceFactory, null);


        log.info("WebApp Discovery component activated");

    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating WebApp manager component");
        }

    }
}
