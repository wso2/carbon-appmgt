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

package org.wso2.carbon.appmgt.mdm.wso2mdm;

public class Constants {

    public static final String MDM_NAME = "WSO2MDM";
    public static final String API_DEVICE_LIST = "/devices?status=ACTIVE&user=%s";
    public static final String API_INSTALL_APP = "/operations/installApp/%s";
    public static final String API_UNINSTALL_APP = "/operations/uninstallApp/%s";

    public static final String PROPERTY_SERVER_URL = "ServerURL";
    public static final String PROPERTY_TOKEN_API_URL = "TokenApiURL";
    public static final String PROPERTY_CLIENT_KEY = "ClientKey";
    public static final String PROPERTY_CLIENT_SECRET = "ClientSecret";
    public static final String PROPERTY_AUTH_USER = "AuthUser";
    public static final String PROPERTY_AUTH_PASS = "AuthPass";
    public static final String PROPERTY_IS_REMOTE = "IsRemote";

}
