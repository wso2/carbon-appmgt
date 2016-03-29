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
package org.wso2.carbon.appmgt.mdm.osgiconnector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.mdm.osgiconnector.mdmmgt.beans.MobileApp;
import org.wso2.carbon.appmgt.mdm.osgiconnector.mdmmgt.beans.MobileAppTypes;
import org.wso2.carbon.appmgt.mdm.osgiconnector.mdmmgt.common.DeviceApplicationException;
import org.wso2.carbon.appmgt.mdm.osgiconnector.mdmmgt.util.AndroidApplicationOperationUtil;
import org.wso2.carbon.appmgt.mdm.osgiconnector.mdmmgt.util.MDMAppConstants;
import org.wso2.carbon.appmgt.mdm.osgiconnector.mdmmgt.util.IOSApplicationOperationUtil;
import org.wso2.carbon.appmgt.mdm.osgiconnector.mdmmgt.util.MDMServiceAPIUtils;
import org.wso2.carbon.appmgt.mobile.interfaces.ApplicationOperations;
import org.wso2.carbon.appmgt.mobile.mdm.App;
import org.wso2.carbon.appmgt.mobile.mdm.Device;
import org.wso2.carbon.appmgt.mobile.utils.MobileApplicationException;
import org.wso2.carbon.appmgt.mobile.utils.User;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Platform;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class ApplicationOperationImpl implements ApplicationOperations {

	private static final Log log = LogFactory.getLog(ApplicationOperationImpl.class);

	/**
	 * @param action   action of the operation. Eg. install, uninstall, update
	 * @param app      application object
	 * @param tenantId tenantId
	 * @param type     type of the resource. Eg: role, user, device
	 * @param params   ids of the resources which belong to type
	 */

	public void performAction(User currentUser, String action, App app, int tenantId, String type,
	                          String[] params, HashMap<String, String> configProperties)
			throws MobileApplicationException {

		Operation operation = null;
		List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
		List<org.wso2.carbon.device.mgt.common.Device> deviceList;
		if (MDMAppConstants.USER.equals(type)) {
			String userName;
			for (String param : params) {
				userName = param;
				try {
					deviceList = MDMServiceAPIUtils.getDeviceManagementService(tenantId).
							getDevicesOfUser(userName);
				} catch (DeviceManagementException devEx) {
					String errorMsg = "Error occurred fetch device for user " + userName + " at app installation";
					log.error(errorMsg, devEx);
					throw new MobileApplicationException(errorMsg, devEx);
				}
				for (org.wso2.carbon.device.mgt.common.Device device : deviceList) {
					deviceIdentifiers.add(getDeviceIdentifierByDevice(device));
				}
			}
		} else if (MDMAppConstants.ROLE.equals(type)) {
			String userRole;
			for (String param : params) {
				userRole = param;
				try {
					deviceList = MDMServiceAPIUtils.getDeviceManagementService(tenantId).
							getAllDevices();
				} catch (DeviceManagementException devMgtEx) {
					String errorMsg = "Error occurred fetch device for user role " + userRole + " at app installation";
					log.error(errorMsg, devMgtEx);
					throw new MobileApplicationException(errorMsg, devMgtEx);
				}
				for (org.wso2.carbon.device.mgt.common.Device device : deviceList) {
					deviceIdentifiers.add(getDeviceIdentifierByDevice(device));
				}
			}
		} else {
			DeviceIdentifier deviceIdentifier;
			for (String param : params) {
				deviceIdentifier = new DeviceIdentifier();
				String[] paramDevices = param.split("---");
				deviceIdentifier.setId(paramDevices[0]);
				deviceIdentifier.setType(paramDevices[1]);
				deviceIdentifiers.add(deviceIdentifier);
			}
		}
		MobileApp mobileApp = new MobileApp();
		mobileApp.setId(app.getId());
		mobileApp.setType(MobileAppTypes.valueOf(app.getType().toUpperCase()));
		mobileApp.setAppIdentifier(app.getAppIdentifier());
		mobileApp.setIconImage(app.getIconImage());
		mobileApp.setIdentifier(app.getIdentifier());
		mobileApp.setLocation(app.getLocation());
		mobileApp.setName(app.getName());
		mobileApp.setPackageName(app.getPackageName());
		mobileApp.setPlatform(app.getPlatform());
		mobileApp.setVersion(app.getVersion());
		Properties properties = new Properties();

		if (MDMAppConstants.IOS.equals(app.getPlatform())) {
			if (MDMAppConstants.ENTERPRISE.equals(app.getType())) {
				properties.put(MDMAppConstants.IOSConstants.IS_REMOVE_APP, true);
				properties.put(MDMAppConstants.IOSConstants.IS_PREVENT_BACKUP, true);
			} else if (MDMAppConstants.IOSConstants.PUBLIC.equals(app.getType())) {
				properties.put(MDMAppConstants.IOSConstants.I_TUNES_ID, Integer.parseInt(app.getIdentifier()));
				properties.put(MDMAppConstants.IOSConstants.IS_REMOVE_APP, true);
				properties.put(MDMAppConstants.IOSConstants.IS_PREVENT_BACKUP, true);
			} else if (MDMAppConstants.WEBAPP.equals(app.getType())) {
				properties.put(MDMAppConstants.IOSConstants.LABEL, app.getName());
				properties.put(MDMAppConstants.IOSConstants.IS_REMOVE_APP, true);
			}
		} else if (MDMAppConstants.WEBAPP.equals(app.getPlatform())) {
			properties.put(MDMAppConstants.IOSConstants.LABEL, app.getName());
			properties.put(MDMAppConstants.IOSConstants.IS_REMOVE_APP, true);
		}
		mobileApp.setProperties(properties);
		try {
			for (DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
				if (deviceIdentifier.getType().equals(Platform.android.toString())) {
					if (MDMAppConstants.INSTALL.equals(action)) {
						operation = AndroidApplicationOperationUtil
								.createInstallAppOperation(mobileApp);
					} else {
						operation = AndroidApplicationOperationUtil
								.createAppUninstallOperation(mobileApp);
					}
				} else if (deviceIdentifier.getType().equals(Platform.ios.toString())) {
					if (MDMAppConstants.INSTALL.equals(action)) {
						operation = IOSApplicationOperationUtil.createInstallAppOperation(mobileApp);
					} else {
						operation = IOSApplicationOperationUtil
								.createAppUninstallOperation(mobileApp);
					}
				}
				MDMServiceAPIUtils.getAppManagementService(tenantId).
						installApplicationForDevices(operation, deviceIdentifiers);
			}
		} catch (DeviceApplicationException mdmExce) {
			log.error("Error in creating operation object using app", mdmExce);
			throw new MobileApplicationException(mdmExce);
		} catch (ApplicationManagementException appMgtExce) {
			log.error("Error in app installation", appMgtExce);
			throw new MobileApplicationException(appMgtExce);
		}

	}

	private static DeviceIdentifier getDeviceIdentifierByDevice(
			org.wso2.carbon.device.mgt.common.Device device) {
		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(device.getDeviceIdentifier());
		deviceIdentifier.setType(device.getType());

		return deviceIdentifier;
	}

	/**
	 * @param tenantId               tenantId
	 * @param type                   type of the resource. Eg: role, user, device
	 * @param params                 ids of the resources which belong to type
	 * @param platform               platform of the devices
	 * @param platformVersion        platform version of the devices
	 * @param isSampleDevicesEnabled if MDM is not connected, enable this to display sample devices.
	 * @return list of Devices
	 */

	public List<Device> getDevices(User currentUser, int tenantId, String type, String[] params,
	                               String platform, String platformVersion,
	                               boolean isSampleDevicesEnabled,
	                               HashMap<String, String> configProperties)
			throws MobileApplicationException {

		List<Device> devices;
		try {
			List<org.wso2.carbon.device.mgt.common.Device> deviceList =
					MDMServiceAPIUtils.getDeviceManagementService(tenantId).
							getDevicesOfUser(currentUser.getUsername());
			devices = new ArrayList<>(deviceList.size());
			for (org.wso2.carbon.device.mgt.common.Device commondevice : deviceList) {
				if (MDMAppConstants.ACTIVE.equals(commondevice.getEnrolmentInfo().getStatus().toString().
						toLowerCase())) {
					Device device = new Device();
					device.setId(
							commondevice.getDeviceIdentifier() + "---" + commondevice.getType());
					device.setName(commondevice.getName());
					device.setModel(commondevice.getName());
					device.setType(MDMAppConstants.MOBILE_DEVICE);
					device.setImage("/store/extensions/assets/mobileapp/resources/models/none.png");
					device.setPlatform(commondevice.getType());
					devices.add(device);
				}
			}
		} catch (DeviceManagementException e) {
			log.error("Error While retrieving Device List", e);
			throw new MobileApplicationException(e);

		}
		return devices;
	}

}

