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

package org.wso2.carbon.appmgt.mobile.mdm;


import org.wso2.carbon.appmgt.mobile.beans.DeviceIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which has sample data
 */
public class Sample {

    public static List<Device> getSampleDevices(){
        List<Device> devices = new ArrayList<Device>();

        Device device1 = new Device();
        device1.setDeviceIdentifier(new DeviceIdentifier("11", ""));
        device1.setName("My Nexus");
        device1.setPlatform("android");
        device1.setPlatformVersion("5.0");
        device1.setImage("/store/extensions/assets/mobileapp/resources/models/nexus.png");
        device1.setModel("Nexus");
        device1.setType("phone");

        Device device2 = new Device();
        device2.setDeviceIdentifier(new DeviceIdentifier("12", ""));
        device2.setName("My iPhone");
        device2.setPlatform("ios");
        device2.setPlatformVersion("8.0");
        device2.setImage("/store/extensions/assets/mobileapp/resources/models/iphone.png");
        device2.setModel("iPhone");
        device2.setType("phone");


        devices.add(device1);
        devices.add(device2);
        return devices;
    }

}
