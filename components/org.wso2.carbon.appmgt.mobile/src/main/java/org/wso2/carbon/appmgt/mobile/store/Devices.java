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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.mobile.beans.ApplicationOperationDevice;
import org.wso2.carbon.appmgt.mobile.interfaces.ApplicationOperations;
import org.wso2.carbon.appmgt.mobile.mdm.Device;
import org.wso2.carbon.appmgt.mobile.mdm.MDMServiceReferenceHolder;
import org.wso2.carbon.appmgt.mobile.utils.MobileApplicationException;
import org.wso2.carbon.appmgt.mobile.utils.MobileConfigurations;
import org.wso2.carbon.appmgt.mobile.utils.User;

import java.util.List;

/**
 * Provides device information to the store
 */
public class Devices {

    private static final Log log = LogFactory.getLog(Devices.class);

    /**
     *
     * @param currentUser Current user who ask for devices
     * @param tenantId Tenant Id of the user
     * @param type type of the resource (device, user or role)
     * @param params Collection of ids of the type
     * @param platform Platform of the devices
     * @param platformVersion Platform version of the devices
     * @return JSON List of devices
     */
    public String getDevicesList(String currentUser, int tenantId, String type, String[] params, String platform, String platformVersion)
            throws MobileApplicationException {

        User user = setUserData(new User(), currentUser);

        MobileConfigurations configurations = MobileConfigurations.getInstance();
        ApplicationOperations applicationOperations = getMDMOperationsInstance();
        ApplicationOperationDevice applicationOperationDevice = new ApplicationOperationDevice();
        applicationOperationDevice.setCurrentUser(user);
        applicationOperationDevice.setTenantId(tenantId);
        applicationOperationDevice.setType(type);
        applicationOperationDevice.setParams(params);
        applicationOperationDevice.setPlatform(platform);
        applicationOperationDevice.setPlatformVersion(platformVersion);
        applicationOperationDevice.setConfigParams(configurations.getActiveMDMProperties());
        List<Device> devices = applicationOperations.getDevices(applicationOperationDevice);
        return convertDevicesToJSON(devices).toJSONString();
    }

    /**
     *
     * @param currentUser Current user who ask for devices
     * @param tenantId Tenant Id of the user
     * @param type type of the resource (device, user or role)
     * @param params Collection of ids of the type
     * @param platform Platform of the devices
     * @return JSON List of devices
     */
    public String getDevicesList(String currentUser, int tenantId, String type, String[] params, String platform)
            throws MobileApplicationException {

        User user = setUserData(new User(), currentUser);

        MobileConfigurations configurations = MobileConfigurations.getInstance();
        ApplicationOperations applicationOperations = getMDMOperationsInstance();
        ApplicationOperationDevice applicationOperationDevice = new ApplicationOperationDevice();
        applicationOperationDevice.setCurrentUser(user);
        applicationOperationDevice.setTenantId(tenantId);
        applicationOperationDevice.setType(type);
        applicationOperationDevice.setParams(params);
        applicationOperationDevice.setPlatform(platform);
        applicationOperationDevice.setConfigParams(configurations.getActiveMDMProperties());
        List<Device> devices = applicationOperations.getDevices(applicationOperationDevice);
        return convertDevicesToJSON(devices).toJSONString();
    }

    /**
     *
     * @param currentUser Current user who ask for devices
     * @param tenantId Tenant Id of the user
     * @param type type of the resource (device, user or role)
     * @param params Collection of ids of the type
     * @return JSON List of devices
     */
    public String getDevicesList(String currentUser, int tenantId, String type, String[] params)
            throws MobileApplicationException {

        User user = setUserData(new User(), currentUser);

        MobileConfigurations configurations = MobileConfigurations.getInstance();
        ApplicationOperations applicationOperations = getMDMOperationsInstance();
        ApplicationOperationDevice applicationOperationDevice = new ApplicationOperationDevice();
        applicationOperationDevice.setCurrentUser(user);
        applicationOperationDevice.setTenantId(tenantId);
        applicationOperationDevice.setType(type);
        applicationOperationDevice.setParams(params);
        applicationOperationDevice.setConfigParams(configurations.getActiveMDMProperties());
        List<Device> devices = applicationOperations.getDevices(applicationOperationDevice);
        return convertDevicesToJSON(devices).toJSONString();
    }

    private ApplicationOperations getMDMOperationsInstance(){
        ApplicationOperations applicationOperations =  MDMServiceReferenceHolder.getInstance().getMDMOperation();
        return applicationOperations;
    }

    private User setUserData(User user, String userString){
        JSONObject userObj = (JSONObject) new JSONValue().parse(userString);
        user.setUsername((String) userObj.get("username"));
        user.setTenantDomain((String) userObj.get("tenantDomain"));
        user.setTenantId(Integer.valueOf(String.valueOf(userObj.get("tenantId"))));
        return user;
    }

    private JSONArray convertDevicesToJSON(List<Device> devices){
            JSONArray jsonArrayDevices = new JSONArray();
            for(Device device : devices){
                JSONObject deviceObj = new JSONObject();
                deviceObj.put("id", device.getDeviceIdentifier().getId());
                deviceObj.put("name", device.getName());
                deviceObj.put("platform", device.getPlatform());
                deviceObj.put("platform_version", device.getPlatformVersion());
                deviceObj.put("image", device.getImage());
                deviceObj.put("model", device.getModel());
                deviceObj.put("type", device.getType());
                jsonArrayDevices.add(deviceObj);
            }

        return jsonArrayDevices;
    }




}
