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
package org.wso2.carbon.appmgt.internalconnector.mdm.wso2mdm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.internalconnector.mdm.wso2mdm.mdmmgt.beans.MobileApp;
import org.wso2.carbon.appmgt.internalconnector.mdm.wso2mdm.mdmmgt.beans.MobileAppTypes;
import org.wso2.carbon.appmgt.internalconnector.mdm.wso2mdm.mdmmgt.common.MDMException;
import org.wso2.carbon.appmgt.internalconnector.mdm.wso2mdm.mdmmgt.util.MDMAndroidOperationUtil;
import org.wso2.carbon.appmgt.internalconnector.mdm.wso2mdm.mdmmgt.util.MDMIOSOperationUtil;
import org.wso2.carbon.appmgt.internalconnector.mdm.wso2mdm.mdmmgt.util.MDMServiceAPIUtils;
import org.wso2.carbon.appmgt.mobile.interfaces.MDMOperations;
import org.wso2.carbon.appmgt.mobile.mdm.App;
import org.wso2.carbon.appmgt.mobile.mdm.Device;
import org.wso2.carbon.appmgt.mobile.utils.User;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Platform;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class MDMOperationsImpl implements MDMOperations {

	private static final Log log = LogFactory.getLog(MDMOperationsImpl.class);

	/**
	 * @param action   action of the operation. Eg. install, uninstall, update
	 * @param app      application object
	 * @param tenantId tenantId
	 * @param type     type of the resource. Eg: role, user, device
	 * @param params   ids of the resources which belong to type
	 */

	public void performAction(User currentUser, String action, App app, int tenantId, String type,
	                          String[] params, HashMap<String, String> configProperties) {

		org.wso2.carbon.device.mgt.common.operation.mgt.Operation operation = null;
		List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
		List<org.wso2.carbon.device.mgt.common.Device> deviceList = null;
		if ("user".equals(type)) {
			String userName;
			for (String param : params) {
				userName = param;
				try {
					deviceList = MDMServiceAPIUtils.getDeviceManagementService(tenantId).
							getDevicesOfUser(userName);
				} catch (DeviceManagementException devEx) {
					String errorMsg = "Error occurred fetch device for user " + userName +
					                  devEx.getErrorMessage() + " " + "at app installation";
					log.error(errorMsg, devEx);
				}
				for (org.wso2.carbon.device.mgt.common.Device device : deviceList) {
					deviceIdentifiers.add(getDeviceIdentifierByDevice(device));
				}
			}
		} else if ("role".equals(type)) {
			String userRole;
			for (String param : params) {
				userRole = param;
				try {
					deviceList = MDMServiceAPIUtils.getDeviceManagementService(tenantId).
							getAllDevices();
				} catch (DeviceManagementException devMgtEx) {
					String errorMsg = "Error occurred fetch device for user role " + userRole +
					                  devMgtEx.getErrorMessage() + " " +
					                  "at app installation";
					log.error(errorMsg, devMgtEx);
				}
				for (org.wso2.carbon.device.mgt.common.Device device : deviceList) {
					deviceIdentifiers.add(getDeviceIdentifierByDevice(device));
				}
			}
		} else {
			deviceIdentifiers = new ArrayList<>();
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

		if ("ios".equals(app.getPlatform())) {
			if ("enterprise".equals(app.getType())) {
				properties.put("isRemoveApp", true);
				properties.put("isPreventBackup", true);
			} else if ("public".equals(app.getType())) {
				properties.put("iTunesId", Integer.parseInt(app.getIdentifier()));
				properties.put("isRemoveApp", true);
				properties.put("isPreventBackup", true);
			} else if ("webapp".equals(app.getType())) {
				properties.put("label", app.getName());
				properties.put("isRemoveApp", true);
			}
		} else if ("webapp".equals(app.getPlatform())) {
			properties.put("label", app.getName());
			properties.put("isRemoveApp", true);
		}
		mobileApp.setProperties(properties);
		try {
			for (DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
				if (deviceIdentifier.getType().equals(Platform.android.toString())) {
					if ("install".equals(action)) {
						operation = MDMAndroidOperationUtil.createInstallAppOperation(mobileApp);
					} else {
						operation = MDMAndroidOperationUtil.createAppUninstallOperation(mobileApp);
					}
				} else if (deviceIdentifier.getType().equals(Platform.ios.toString())) {
					if ("install".equals(action)) {
						operation = MDMIOSOperationUtil.createInstallAppOperation(mobileApp);
					} else {
						operation = MDMIOSOperationUtil.createAppUninstallOperation(mobileApp);
					}
				}
				MDMServiceAPIUtils.getAppManagementService(tenantId).
						installApplicationForDevices(operation, deviceIdentifiers);
			}
		} catch (MDMException mdmExce) {
			log.error("Error in creating operation object using app", mdmExce);
		} catch (ApplicationManagementException appMgtExce) {
			log.error("Error in app installation", appMgtExce);
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
	                               HashMap<String, String> configProperties) {

		List<Device> devices = null;
		Device device;
		try {
			List<org.wso2.carbon.device.mgt.common.Device> deviceList =
					MDMServiceAPIUtils.getDeviceManagementService(tenantId).
							getDevicesOfUser(currentUser.getUsername());
			devices = new ArrayList<>(deviceList.size());
			for (org.wso2.carbon.device.mgt.common.Device commondevice : deviceList) {
				if ("active".equals(commondevice.getEnrolmentInfo().getStatus().toString().
						toLowerCase())) {
					device = new Device();
					device.setId(
							commondevice.getDeviceIdentifier() + "---" + commondevice.getType());
					device.setName(commondevice.getName());
					device.setModel(commondevice.getName());
					device.setType("mobileDevice");
					device.setImage("/store/extensions/assets/mobileapp/resources/models/none.png");
					device.setPlatform(commondevice.getType());
					devices.add(device);
				}
			}
		} catch (DeviceManagementException e) {
			log.error("Error While retrieving Device List", e);

		}
		return devices;
	}

}

