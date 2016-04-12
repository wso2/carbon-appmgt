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

package org.wso2.carbon.appmgt.mdm.osgiconnector.mdmmgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;

/**
 * MDMServiceAPIUtils class provides utility function.
 */
public class MDMServiceAPIUtils {

	private static Log log = LogFactory.getLog(MDMServiceAPIUtils.class);

	/**
	 * Returns the DeviceManagementProviderService osgi service.
	 *
	 * @param tenantId tenant id
	 * @return DeviceManagementProviderService
	 */
	public static DeviceManagementProviderService getDeviceManagementService(int tenantId) {
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		ctx.setTenantId(tenantId, true);
		DeviceManagementProviderService deviceManagementProviderService =
				(DeviceManagementProviderService) ctx
						.getOSGiService(DeviceManagementProviderService.class, null);
		if (deviceManagementProviderService == null) {
			String msg = "Device Management provider service has not initialized.";
			log.error(msg);
			throw new IllegalStateException(msg);
		}
		return deviceManagementProviderService;
	}

	/**
	 * Returns the ApplicationManagementProviderService osgi service.
	 *
	 * @param tenantId tenant id
	 * @return ApplicationManagementProviderService
	 */
	public static ApplicationManagementProviderService getAppManagementService(int tenantId) {
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		ctx.setTenantId(tenantId, true);
		ApplicationManagementProviderService applicationManagementProviderService =
				(ApplicationManagementProviderService) ctx.
						                                          getOSGiService(
								                                          ApplicationManagementProviderService.class,
								                                          null);
		if (applicationManagementProviderService == null) {
			String msg = "Application management service has not initialized.";
			log.error(msg);
			throw new IllegalStateException(msg);
		}
		return applicationManagementProviderService;
	}

}
