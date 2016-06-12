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

/**
 * This class holds all the constants used for IOS and Android.
 */
public class MDMAppConstants {

	public static final String USER = "user";
	public static final String ROLE = "role";
	public static final String IOS = "ios";
	public static final String ANDROID = "android";
	public static final String WEBAPP = "webapp";
	public static final String INSTALL = "install";
	public static final String UPDATE = "update";
	public static final String ACTIVE = "active";
	public static final String ENTERPRISE = "enterprise";
	public static final String DEVICE = "device";
	public static final String MOBILE_DEVICE = "mobileDevice";
	public static final String NEXUS = "nexus";
	public static final String IPHONE = "iphone";
	public static final String NONE = "none";
	public static final String IMAGE_URL = "ImageURL";
	public static final String TYPE = "type";
	public static final String ID = "id";

	public class IOSConstants {

		private IOSConstants() {
			throw new AssertionError();
		}

		public static final String IS_REMOVE_APP = "isRemoveApp";
		public static final String IS_PREVENT_BACKUP = "isPreventBackup";
		public static final String I_TUNES_ID = "iTunesId";
		public static final String LABEL = "label";
		public static final String PUBLIC = "public";
		public static final String OPCODE_INSTALL_ENTERPRISE_APPLICATION =
				"INSTALL_ENTERPRISE_APPLICATION";
		public static final String OPCODE_INSTALL_STORE_APPLICATION = "INSTALL_STORE_APPLICATION";
		public static final String OPCODE_INSTALL_WEB_APPLICATION = "WEB_CLIP";
		public static final String OPCODE_REMOVE_APPLICATION = "REMOVE_APPLICATION";
	}

	public class AndroidConstants {
		private AndroidConstants() {
			throw new AssertionError();
		}

		public static final String OPCODE_INSTALL_APPLICATION = "INSTALL_APPLICATION";
		public static final String OPCODE_UPDATE_APPLICATION = "UPDATE_APPLICATION";
		public static final String OPCODE_UNINSTALL_APPLICATION = "UNINSTALL_APPLICATION";
	}

	public class RegistryConstants {
		private RegistryConstants() {
			throw new AssertionError();
		}

		public static final String GENERAL_CONFIG_RESOURCE_PATH = "general";
	}

	public class APPManagerConstants {
		private static final String APP_MANAGER_MDM_SERVICE_NAME =
				"org.wso2.carbon.appmgt.mobile.interfaces.MDMOperations";
	}

}
