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
import org.wso2.carbon.appmgt.mdm.osgiconnector.mdmmgt.util.IOSApplicationOperationUtil;
import org.wso2.carbon.appmgt.mdm.osgiconnector.mdmmgt.util.MDMAppConstants;
import org.wso2.carbon.appmgt.mdm.osgiconnector.mdmmgt.util.MDMServiceAPIUtils;
import org.wso2.carbon.appmgt.mobile.beans.ApplicationOperationAction;
import org.wso2.carbon.appmgt.mobile.beans.ApplicationOperationDevice;
import org.wso2.carbon.appmgt.mobile.interfaces.ApplicationOperations;
import org.wso2.carbon.appmgt.mobile.mdm.App;
import org.wso2.carbon.appmgt.mobile.mdm.Device;
import org.wso2.carbon.appmgt.mobile.utils.MobileApplicationException;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Platform;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ApplicationOperationsImpl implements ApplicationOperations {

	private static final Log log = LogFactory.getLog(ApplicationOperationsImpl.class);

	/**
	 * @param applicationOperationAction holds the information needs to perform an action on mdm
	 * @throws MobileApplicationException
	 */
	public void performAction(ApplicationOperationAction applicationOperationAction)
			throws MobileApplicationException {
		if (log.isDebugEnabled()) {
			log.debug(applicationOperationAction.getAction() + " action is triggered for " +
			          applicationOperationAction.getType());
		}

		Operation operation = null;
		List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
		List<org.wso2.carbon.device.mgt.common.Device> deviceList;
		if (MDMAppConstants.USER.equals(applicationOperationAction.getType())) {
			String userName = null;
			try {
				for (String param : applicationOperationAction.getParams()) {
					userName = param;

					deviceList = MDMServiceAPIUtils
							.getDeviceManagementService(applicationOperationAction.getTenantId()).
									getDevicesOfUser(userName);

					for (org.wso2.carbon.device.mgt.common.Device device : deviceList) {
						deviceIdentifiers.add(getDeviceIdentifierByDevice(device));
					}
				}
			} catch (DeviceManagementException devEx) {
				String errorMsg = "Error occurred fetch device for user " + userName +
				                  " at app installation";
				log.error(errorMsg, devEx);
				throw new MobileApplicationException(errorMsg, devEx);
			}
		} else if (MDMAppConstants.ROLE.equals(applicationOperationAction.getType())) {
			String userRole = null;
			try {
				for (String param : applicationOperationAction.getParams()) {
					userRole = param;

					deviceList = MDMServiceAPIUtils
							.getDeviceManagementService(applicationOperationAction.getTenantId()).
									getAllDevices();

					for (org.wso2.carbon.device.mgt.common.Device device : deviceList) {
						deviceIdentifiers.add(getDeviceIdentifierByDevice(device));
					}
				}
			} catch (DeviceManagementException devMgtEx) {
				String errorMsg = "Error occurred fetch device for user role " + userRole +
				                  " at app installation";
				log.error(errorMsg, devMgtEx);
				throw new MobileApplicationException(errorMsg, devMgtEx);
			}

		} else if (MDMAppConstants.DEVICE.equals(applicationOperationAction.getType())) {
			DeviceIdentifier deviceIdentifier;
			for (String param : applicationOperationAction.getParams()) {
				deviceIdentifier = new DeviceIdentifier();
				String[] paramDevices = param.split("---");
				deviceIdentifier.setId(paramDevices[0]);
				deviceIdentifier.setType(paramDevices[1]);
				deviceIdentifiers.add(deviceIdentifier);
			}
		} else {
			throw new IllegalStateException("invalid type is received from app store.");
		}
		App app = applicationOperationAction.getApp();
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
				properties.put(MDMAppConstants.IOSConstants.I_TUNES_ID,
				               Integer.parseInt(app.getIdentifier()));
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
					if (MDMAppConstants.INSTALL.equals(applicationOperationAction.getAction())) {
						operation = AndroidApplicationOperationUtil
								.createInstallAppOperation(mobileApp);
					} else {
						operation = AndroidApplicationOperationUtil
								.createAppUninstallOperation(mobileApp);
					}
				} else if (deviceIdentifier.getType().equals(Platform.ios.toString())) {
					if (MDMAppConstants.INSTALL.equals(applicationOperationAction.getAction())) {
						operation =
								IOSApplicationOperationUtil.createInstallAppOperation(mobileApp);
					} else {
						operation =
								IOSApplicationOperationUtil.createAppUninstallOperation(mobileApp);
					}
				}
				MDMServiceAPIUtils.getAppManagementService(applicationOperationAction.getTenantId())
				                  .
						                  installApplicationForDevices(operation,
						                                               deviceIdentifiers);
			}
		} catch (DeviceApplicationException mdmExce) {
			log.error("Error in creating operation object using app", mdmExce);
			throw new MobileApplicationException(mdmExce);
		} catch (ApplicationManagementException appMgtExce) {
			log.error("Error in app installation", appMgtExce);
			throw new MobileApplicationException(appMgtExce);
		}

	}

	/**
	 * Create a new device identifier from Device object
	 * @param device device which is to be retrieved type and id
	 * @return created device identifier
	 */
	private static DeviceIdentifier getDeviceIdentifierByDevice(
			org.wso2.carbon.device.mgt.common.Device device) {
		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(device.getDeviceIdentifier());
		deviceIdentifier.setType(device.getType());

		return deviceIdentifier;
	}

	/**
	 * @param applicationOperationDevice holds the information needs to retrieve device list
	 * @return List of devices
	 * @throws MobileApplicationException
	 */
	public List<Device> getDevices(ApplicationOperationDevice applicationOperationDevice)
			throws MobileApplicationException {

		List<Device> devices;
		try {
			List<org.wso2.carbon.device.mgt.common.Device> deviceList = MDMServiceAPIUtils
					.getDeviceManagementService(applicationOperationDevice.getTenantId()).
							getDevicesOfUser(
									applicationOperationDevice.getCurrentUser().getUsername());
			devices = new ArrayList<>(deviceList.size());
			if(log.isDebugEnabled()){
				log.debug("device list got from mdm "+ deviceList.toString());
			}
			for (org.wso2.carbon.device.mgt.common.Device commondevice : deviceList) {
				if (MDMAppConstants.ACTIVE
						.equals(commondevice.getEnrolmentInfo().getStatus().toString().
								toLowerCase())) {
					Device device = new Device();
					org.wso2.carbon.appmgt.mobile.beans.DeviceIdentifier deviceIdentifier =
							new org.wso2.carbon.appmgt.mobile.beans.DeviceIdentifier();
					deviceIdentifier.setId(commondevice.getDeviceIdentifier());
					deviceIdentifier.setType(commondevice.getType());
					device.setDeviceIdentifier(deviceIdentifier);
					device.setName(commondevice.getName());
					device.setModel(commondevice.getName());
					device.setType(MDMAppConstants.MOBILE_DEVICE);
					String imgUrl;
					if ("android".equalsIgnoreCase(commondevice.getType())) {
						imgUrl = String.format(
								applicationOperationDevice.getConfigParams().get("ImageURL"),
								"nexus");
					} else if ("ios".equalsIgnoreCase(commondevice.getType())) {
						imgUrl = String.format(
								applicationOperationDevice.getConfigParams().get("ImageURL"),
								"iphone");
					} else {
						imgUrl = String.format(
								applicationOperationDevice.getConfigParams().get("ImageURL"),
								"none");
					}
					device.setImage(imgUrl);
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

