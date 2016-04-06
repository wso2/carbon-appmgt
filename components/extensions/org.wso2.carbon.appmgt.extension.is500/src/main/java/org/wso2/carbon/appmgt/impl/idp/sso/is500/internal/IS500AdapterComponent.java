/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl.idp.sso.is500.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appmgt.api.IdentityApplicationManagementFactory;
import org.wso2.carbon.appmgt.impl.idp.sso.is500.IdentityApplicationManagementFactoryImpl;

/**
 * @scr.component name="org.wso2.appm.extension.identity.adapter.is500" immediate="true"
 */
public class IS500AdapterComponent {

    private static final Log log = LogFactory.getLog(IS500AdapterComponent.class);

    private ServiceRegistration registration;

    protected void activate(ComponentContext componentContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("AppM WSO2 IS 500 component activated");
        }
        BundleContext bundleContext = componentContext.getBundleContext();
        registration = bundleContext.registerService(IdentityApplicationManagementFactory.class,
                new IdentityApplicationManagementFactoryImpl(), null);
    }

    protected void deactivate(ComponentContext componentContext) {
        if (registration != null) {
            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext.ungetService(registration.getReference());
        }
        if (log.isDebugEnabled()) {
            log.debug("AppM WSO2 IS 500 component de-activated");
        }
    }
}
