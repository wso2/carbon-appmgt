/*
 *
 *   Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.mobile.store;

import org.wso2.carbon.appmgt.mobile.mdm.MDMOperations;
import org.wso2.carbon.appmgt.mobile.utils.MobileConfigurations;
import org.wso2.carbon.appmgt.mobile.wso2mdm.WSO2MDMOperations;


public class Devices {

    public String getDevicesList(int tenantId, String type, String[] params, String platform, String platformVersion){

        MobileConfigurations configurations = MobileConfigurations.getInstance();
        String serverUrl = configurations.getMDMServerURL();

        MDMOperations mdmOperations = new WSO2MDMOperations();
        return mdmOperations.getDevices(serverUrl, tenantId, type, params, platform, platformVersion).toJSONString();

    }

    public String getDevicesList(int tenantId, String type, String[] params, String platform){

        MobileConfigurations configurations = MobileConfigurations.getInstance();
        String serverUrl = configurations.getMDMServerURL();

        MDMOperations mdmOperations = new WSO2MDMOperations();
        return mdmOperations.getDevices(serverUrl, tenantId, type, params, platform, null).toJSONString();

    }

    public String getDevicesList(int tenantId, String type, String[] params){

        MobileConfigurations configurations = MobileConfigurations.getInstance();
        String serverUrl = configurations.getMDMServerURL();

        MDMOperations mdmOperations = new WSO2MDMOperations();
        return mdmOperations.getDevices(serverUrl, tenantId, type, params, null, null).toJSONString();

    }




}
