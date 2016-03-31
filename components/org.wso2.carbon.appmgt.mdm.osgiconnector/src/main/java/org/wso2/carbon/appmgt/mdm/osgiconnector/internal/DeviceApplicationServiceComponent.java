/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appmgt.mdm.osgiconnector.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appmgt.mdm.osgiconnector.ApplicationOperationsImpl;
import org.wso2.carbon.appmgt.mobile.interfaces.ApplicationOperations;

/**
 * @scr.component name="org.wso2.carbon.appmgt.mdm.osgiconnector" immediate="true"
 */

public class DeviceApplicationServiceComponent {

	private static final Log log = LogFactory.getLog(DeviceApplicationServiceComponent.class);

	private ServiceRegistration mdmServiceRegistration;

	protected void activate(ComponentContext context) {
		BundleContext bundleContext = context.getBundleContext();
		mdmServiceRegistration = bundleContext
				.registerService(ApplicationOperations.class.getName(), new ApplicationOperationsImpl(), null);
		if (log.isDebugEnabled()) {
			log.debug("Device Application Service Component activated");
		}

	}

	protected void deactivate(ComponentContext context) {
		if (mdmServiceRegistration != null) {
			mdmServiceRegistration.unregister();
			mdmServiceRegistration = null;
		}
		if (log.isDebugEnabled()) {
			log.debug("Device Application Service Component deactivated");
		}

	}

}


