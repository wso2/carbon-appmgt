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
import org.wso2.carbon.appmgt.impl.AppManagerConfigurationService;
import org.wso2.carbon.appmgt.impl.service.TenantConfigurationService;
import org.wso2.carbon.sequences.services.SequenceAdminService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 *
 * Application manager gateway component
 *
 * @scr.component name="org.wso2.carbon.appmgt.gateway.services" immediate="true"
 *
 * @scr.reference name="arg.wso2.appmgt.impl.services.appm"
 * interface="org.wso2.carbon.appmgt.impl.AppManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAppManagerConfigurationService" unbind="unsetAppManagerConfigurationService"
 *
 * @scr.reference name="org.wso2.carbon.sequences" immediate="true"
 * interface="org.wso2.carbon.sequences.services.SequenceAdminService" cardinality="1..1"
 * policy="dynamic" bind="setSequenceAdminService" unbind="unsetSequenceAdminService"
 *
 * @scr.reference name="org.wso2.carbon.appmgt.impl.service.TenantConfigurationService"
 * interface="org.wso2.carbon.appmgt.impl.service.TenantConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setTenantConfigurationService" unbind="unsetTenantConfigurationService"
 *
 */
public class AppManagerGatewayComponent {

    private static final Log log = LogFactory.getLog(AppManagerGatewayComponent.class);

    protected void activate(ComponentContext componentContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Gateway manager component activated");
        }
        BundleContext bundleContext = componentContext.getBundleContext();

        //Register Tenant service creator to deploy tenant specific common synapse configurations
        TenantCreateGatewayObserver listener = new TenantCreateGatewayObserver();
        bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), listener, null);

    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating Gateway manager component");
        }
    }

    protected void setAppManagerConfigurationService(AppManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("App manager configuration service is set to gateway bundle");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAppManagerConfigurationService(AppManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("App manager configuration service is unset to gateway bundle");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }

    protected void setTenantConfigurationService(TenantConfigurationService tenantConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting tenant configuration service implementation - " + tenantConfigurationService.getClass().getName());
        }
        ServiceReferenceHolder.getInstance().setTenantConfigurationService(tenantConfigurationService);
    }

    protected void unsetTenantConfigurationService(TenantConfigurationService tenantConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Un-setting tenant configuration service implementation - " + tenantConfigurationService.getClass().getName());
        }
        ServiceReferenceHolder.getInstance().setTenantConfigurationService(null);
    }

    protected void setSequenceAdminService(SequenceAdminService sequenceAdminService) {
        if (log.isDebugEnabled()) {
            log.debug("Sequence service is set to gateway bundle");
        }
        ServiceReferenceHolder.getInstance().setSequenceAdminService(sequenceAdminService);
    }

    protected void unsetSequenceAdminService(SequenceAdminService sequenceAdminService) {
        if (log.isDebugEnabled()) {
            log.debug("Sequence service is unset to gateway bundle");
        }
        ServiceReferenceHolder.getInstance().setSequenceAdminService(null);
    }

}
