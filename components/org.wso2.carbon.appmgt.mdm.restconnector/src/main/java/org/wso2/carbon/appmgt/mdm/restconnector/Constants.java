/*
 *
 *   Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.appmgt.mdm.restconnector;

public class Constants {

    public static final String MDM_NAME = "WSO2MDM";
    public static final String API_DEVICE_LIST_OF_USER = "/devices/user/%s/%s";
    public static final String API_DEVICE_LIST_OF_ROLES = "/devices/roles/%s";
    public static final String API_DEVICE_LIST_OF_USERS = "/devices/users/%s";
    public static final String API_INSTALL_APP = "/operations/installApp/%s";
    public static final String API_UNINSTALL_APP = "/operations/uninstallApp/%s";

    public static final String PROPERTY_SERVER_URL = "ServerURL";
    public static final String PROPERTY_TOKEN_API_URL = "TokenApiURL";
    public static final String PROPERTY_CLIENT_KEY = "ClientKey";
    public static final String PROPERTY_CLIENT_SECRET = "ClientSecret";
    public static final String PROPERTY_AUTH_USER = "AuthUser";
    public static final String PROPERTY_AUTH_PASS = "AuthPass";

    public static final String USER = "user";
    public static final String ROLE = "role";
    public static final String ANDROID = "android";
    public static final String PLATFORM = "platform";
    public static final String PLATFORM_VERSION = "platformVersion";
    public static final String PROPERTIES = "properties";
    public static final String TYPE = "type";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String USER_LIST = "userList";
    public static final String DEVICE_IDENTIFIER = "deviceIdentifier";
    public static final String DEVICE_IDENTIFIERS = DEVICE_IDENTIFIER + "s";
    public static final String APPLICATION = "application";
    public static final String NEXUS = "nexus";
    public static final String IPHONE = "iphone";
    public static final String NONE = "none";
    public static final String IMAGE_URL = "ImageURL";
    public static final String INSTALL = "install";
    public static final String MOBILE_DEVICE = "mobileDevice";


    public class IOSConstants {

        private IOSConstants() {
            throw new AssertionError();
        }

        public static final String IOS = "ios";
        public static final String IS_REMOVE_APP = "isRemoveApp";
        public static final String IS_PREVENT_BACKUP = "isPreventBackup";
        public static final String I_TUNES_ID = "iTunesId";
        public static final String LABEL = "label";
        public static final String PUBLIC = "public";
        public static final String ENTERPRISE = "enterprise";
        public static final String TYPE = "type";
        public static final String WEBAPP = "webapp";
        public static final String IDENTIFIER = "identifier";
        public static final String OPCODE_INSTALL_ENTERPRISE_APPLICATION =
                "INSTALL_ENTERPRISE_APPLICATION";
        public static final String OPCODE_INSTALL_STORE_APPLICATION = "INSTALL_STORE_APPLICATION";
        public static final String OPCODE_INSTALL_WEB_APPLICATION = "WEB_CLIP";
        public static final String OPCODE_REMOVE_APPLICATION = "REMOVE_APPLICATION";
    }

    public class WebAppConstants {
        public static final String WEBAPP = "webapp";
        public static final String LABEL = "label";
        public static final String IS_REMOVE_APP = "isRemoveApp";
        public static final String NAME = "name";
    }

    public class RestConstants {
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String AUTHORIZATION = "Authorization";
        public static final String BEARER = "Bearer ";
        public static final String BASIC = "Basic ";
        public static final String COLON = ":";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String ACCEPT = "Accept";
        public static final String APPLICATION_JSON = "application/json";
        public static final String APPLICATION_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    }

}
