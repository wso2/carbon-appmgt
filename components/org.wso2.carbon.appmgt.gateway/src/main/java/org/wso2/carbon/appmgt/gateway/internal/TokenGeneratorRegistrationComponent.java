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
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.AppManagerConfigurationService;
import org.wso2.carbon.appmgt.impl.token.JWTGenerator;
import org.wso2.carbon.appmgt.impl.token.TokenGenerator;

/**
 *
 * Registers the default token generator. This is the internal JWT generator.
 *
 * @scr.component name="org.wso2.appmgt.impl.services.gateway.tokenGenerator" immediate="true"
 * @scr.reference name="app.manager.config.service"
 * interface="org.wso2.carbon.appmgt.impl.AppManagerConfigurationService"
 * policy="dynamic" bind="setAppManagerConfigurationService" unbind="unsetAppManagerConfigurationService"
 */
public class TokenGeneratorRegistrationComponent {

    private static final Log log = LogFactory.getLog(TokenGeneratorRegistrationComponent.class);

    //This configuration will be used for the TokenGenerator init.
    private static AppManagerConfiguration configuration = null;

    protected void activate(ComponentContext componentContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Gateway token generator registration component activated");
        }
        BundleContext bundleContext = componentContext.getBundleContext();
        //register JWT implementation class as a OSGi service
        bundleContext.registerService(TokenGenerator.class.getName(), new JWTGenerator(), null);
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating Gateway manager component");
        }
    }

    protected void setAppManagerConfigurationService(AppManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("Gateway manager configuration service bound to the WebApp host objects");
        }
        configuration = amcService.getAPIManagerConfiguration();
        org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder.getInstance()
                .setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAppManagerConfigurationService(AppManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("Gateway manager configuration service unbound from the WebApp host objects");
        }
        configuration = null;
        org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder.getInstance()
                .setAPIManagerConfigurationService(null);
    }

}
