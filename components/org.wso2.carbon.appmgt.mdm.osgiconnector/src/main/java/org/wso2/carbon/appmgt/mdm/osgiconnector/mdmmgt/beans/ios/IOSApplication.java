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
package org.wso2.carbon.appmgt.mdm.osgiconnector.mdmmgt.beans.ios;

import com.google.gson.Gson;
import org.wso2.carbon.appmgt.mdm.osgiconnector.mdmmgt.common.DeviceApplicationException;

public class IOSApplication {

	private String identifier;
	private boolean removeAppUponMDMProfileRemoval;
	private boolean preventBackupOfAppData;
	private String bundleId;
	private String UUID;

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String UUID) {
		this.UUID = UUID;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public boolean isRemoveAppUponMDMProfileRemoval() {
		return removeAppUponMDMProfileRemoval;
	}

	public void setRemoveAppUponMDMProfileRemoval(boolean removeAppUponMDMProfileRemoval) {
		this.removeAppUponMDMProfileRemoval = removeAppUponMDMProfileRemoval;
	}

	public boolean isPreventBackupOfAppData() {
		return preventBackupOfAppData;
	}

	public void setPreventBackupOfAppData(boolean preventBackupOfAppData) {
		this.preventBackupOfAppData = preventBackupOfAppData;
	}

	public String getBundleId() {
		return bundleId;
	}

	public void setBundleId(String bundleId) {
		this.bundleId = bundleId;
	}

	public String toJSON() throws DeviceApplicationException {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

}
