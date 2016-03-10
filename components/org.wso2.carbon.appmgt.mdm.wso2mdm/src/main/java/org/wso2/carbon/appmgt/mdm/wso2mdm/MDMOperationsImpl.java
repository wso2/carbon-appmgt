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
 *
 */
package org.wso2.carbon.appmgt.mdm.wso2mdm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.mobile.interfaces.MDMOperations;
import org.wso2.carbon.appmgt.mobile.mdm.App;
import org.wso2.carbon.appmgt.mobile.mdm.Device;
import org.wso2.carbon.appmgt.mobile.utils.User;

import java.util.HashMap;
import java.util.List;

public class MDMOperationsImpl implements MDMOperations {

    private static final Log log = LogFactory.getLog(MDMOperationsImpl.class);

    /**
     * @param action   action of the operation. Eg. install, uninstall, update
     * @param app      application object
     * @param tenantId tenantId
     * @param type     type of the resource. Eg: role, user, device
     * @param params   ids of the resources which belong to type
     */

    public void performAction(User currentUser, String action, App app, int tenantId, String type, String[] params,
                              HashMap<String, String> configProperties) {
        OperationHandler operationHandler = MDMOperationFactory.getOperationHandler(configProperties);
        operationHandler.performAction(currentUser, action, app, tenantId, type, params, configProperties);

    }

    /**
     * @param tenantId               tenantId
     * @param type                   type of the resource. Eg: role, user, device
     * @param params                 ids of the resources which belong to type
     * @param platform               platform of the devices
     * @param platformVersion        platform version of the devices
     * @param isSampleDevicesEnabled if MDM is not connected, enable this to display sample devices.
     * @return
     */

    public List<Device> getDevices(User currentUser, int tenantId, String type, String[] params, String platform,
                                   String platformVersion, boolean isSampleDevicesEnabled,
                                   HashMap<String, String> configProperties) {
        OperationHandler operationHandler = MDMOperationFactory.getOperationHandler(configProperties);
        return operationHandler.getDevices(currentUser, tenantId, type, params, platform, platformVersion,
                                           isSampleDevicesEnabled, configProperties);
    }


}
