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
package org.wso2.carbon.appmgt.mobile.beans;

import org.wso2.carbon.appmgt.mobile.utils.User;

import java.util.HashMap;

public class ApplicationOperationDevice {

	private User currentUser;
	private int tenantId;
	private String type;
	private String[] params;
	private String platform;
	private String platformVersion;
	private boolean isSampleDevicesEnabled;
	private HashMap<String, String> configParams;

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

	public int getTenantId() {
		return tenantId;
	}

	public void setTenantId(int tenantId) {
		this.tenantId = tenantId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String[] getParams() {
		return params;
	}

	public void setParams(String[] params) {
		this.params = params;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getPlatformVersion() {
		return platformVersion;
	}

	public void setPlatformVersion(String platformVersion) {
		this.platformVersion = platformVersion;
	}

	public boolean isSampleDevicesEnabled() {
		return isSampleDevicesEnabled;
	}

	public void setSampleDevicesEnabled(boolean sampleDevicesEnabled) {
		isSampleDevicesEnabled = sampleDevicesEnabled;
	}

	public HashMap<String, String> getConfigParams() {
		return configParams;
	}

	public void setConfigParams(HashMap<String, String> configParams) {
		this.configParams = configParams;
	}

}
