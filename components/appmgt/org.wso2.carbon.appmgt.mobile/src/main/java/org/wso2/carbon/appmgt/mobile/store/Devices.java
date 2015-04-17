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
import org.wso2.carbon.appmgt.mobile.interfaces.MDMOperations;
import org.wso2.carbon.appmgt.mobile.mdm.Device;
import org.wso2.carbon.appmgt.mobile.mdm.MDMServiceReferenceHolder;
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
    public String getDevicesList(String currentUser, int tenantId, String type, String[] params, String platform, String platformVersion){

        User user = setUserData(new User(), currentUser);

        MobileConfigurations configurations = MobileConfigurations.getInstance();
        MDMOperations mdmOperations = getMDMOperationsInstance();
        List<Device> devices =  mdmOperations.getDevices(user, tenantId, type, params, platform, platformVersion, Boolean.valueOf(configurations.getMDMConfigs().get(MobileConfigurations.ENABLE_SAMPLE_DEVICES)), configurations.getActiveMDMProperties());
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
    public String getDevicesList(String currentUser, int tenantId, String type, String[] params, String platform){

        User user = setUserData(new User(), currentUser);


        MobileConfigurations configurations = MobileConfigurations.getInstance();
        MDMOperations mdmOperations =  getMDMOperationsInstance();
        List<Device> devices = mdmOperations.getDevices(user, tenantId, type, params, platform, null, Boolean.valueOf(configurations.getMDMConfigs().get(MobileConfigurations.ENABLE_SAMPLE_DEVICES)), configurations.getActiveMDMProperties());
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
    public String getDevicesList(String currentUser, int tenantId, String type, String[] params){

        User user = setUserData(new User(), currentUser);

        MobileConfigurations configurations = MobileConfigurations.getInstance();
        MDMOperations mdmOperations = getMDMOperationsInstance();
        List<Device> devices = mdmOperations.getDevices(user, tenantId, type, params, null, null, Boolean.valueOf(configurations.getMDMConfigs().get(MobileConfigurations.ENABLE_SAMPLE_DEVICES)), configurations.getActiveMDMProperties());
        return convertDevicesToJSON(devices).toJSONString();
    }

    private MDMOperations getMDMOperationsInstance(){
        MDMOperations mdmOperations =  MDMServiceReferenceHolder.getInstance().getMDMOperation();
        return mdmOperations;
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
                deviceObj.put("id", device.getId());
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
