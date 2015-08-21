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

package org.wso2.carbon.appmgt.mobile.interfaces;

import org.wso2.carbon.appmgt.mobile.mdm.App;
import org.wso2.carbon.appmgt.mobile.mdm.Device;
import org.wso2.carbon.appmgt.mobile.utils.User;

import java.util.HashMap;
import java.util.List;

/**
 * Interface for MDM Operations which connects with MDM components
 *
 */
public interface MDMOperations {

    /**
     *
     * @param currentUser User who perform the action
     * @param action Name of the action
     * @param app Instance of the mobile app
     * @param tenantId Tenant Id
     * @param type Type of the resource (device, user or role)
     * @param params Collection of ids of the type
     * @param configParams Configuration belongs to the MDM which is defined in app-manager.xml
     */
    public void performAction(User currentUser,String action, App app, int tenantId, String type, String[] params,
                              HashMap<String, String> configParams);

    /**
     *
     * @param currentUser User who perform the action
     * @param tenantId Tenant Id
     * @param type Type of the resource (device, user or role)
     * @param params Collection of ids of the type
     * @param platform Platform of the devices
     * @param platformVersion Platform version of the devices
     * @param isSampleDevicesEnabled
     * @param configParams
     * @return List of devices
     */
    public List<Device> getDevices(User currentUser, int tenantId, String type, String[] params, String platform,
                                   String platformVersion, boolean isSampleDevicesEnabled, HashMap<String, String> configParams);

}
