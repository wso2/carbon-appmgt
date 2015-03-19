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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.mobile.mdm.MDMOperations;
import org.wso2.carbon.appmgt.mobile.utils.MobileConfigurations;


public class Devices {

    private static final Log log = LogFactory.getLog(Devices.class);

    public String getDevicesList(int tenantId, String type, String[] params, String platform, String platformVersion){

        MobileConfigurations configurations = MobileConfigurations.getInstance();
        String serverUrl = configurations.getMDMServerURL();
        MDMOperations mdmOperations = getMDMOperationsInstance(configurations);
        return mdmOperations.getDevices(serverUrl, tenantId, type, params, platform, platformVersion, configurations.isSampleDevicesEnabled()).toJSONString();

    }

    public String getDevicesList(int tenantId, String type, String[] params, String platform){

        MobileConfigurations configurations = MobileConfigurations.getInstance();
        String serverUrl = configurations.getMDMServerURL();
        MDMOperations mdmOperations =  getMDMOperationsInstance(configurations);
        return mdmOperations.getDevices(serverUrl, tenantId, type, params, platform, null, configurations.isSampleDevicesEnabled()).toJSONString();

    }

    public String getDevicesList(int tenantId, String type, String[] params){

        MobileConfigurations configurations = MobileConfigurations.getInstance();
        String serverUrl = configurations.getMDMServerURL();
        MDMOperations mdmOperations = getMDMOperationsInstance(configurations);
        return mdmOperations.getDevices(serverUrl, tenantId, type, params, null, null, configurations.isSampleDevicesEnabled()).toJSONString();

    }

    private MDMOperations getMDMOperationsInstance(MobileConfigurations configurations){

        MDMOperations mdmOperations = null;
        try {
            Class<MDMOperations> mdmOperationsClass = (Class<MDMOperations>) Class.forName(configurations.getMDMOperationsClass());
            mdmOperations = (MDMOperations) mdmOperationsClass.newInstance();
        } catch (InstantiationException e) {
            log.error("InstantiationException occurred");
            log.debug("Error: " + e);
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException occurred");
            log.debug("Error: " + e);
        } catch (ClassNotFoundException e) {
            log.error("Error occurred while getting the right class for MDM");
            log.debug("Error: " + e);
        }
        return mdmOperations;
    }




}
