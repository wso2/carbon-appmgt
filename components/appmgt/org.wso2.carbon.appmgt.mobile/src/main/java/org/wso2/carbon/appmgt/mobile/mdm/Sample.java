package org.wso2.carbon.appmgt.mobile.mdm;


import java.util.ArrayList;
import java.util.List;

public class Sample {

    public static List<Device> getSampleDevices(){
        List<Device> devices = new ArrayList<Device>();

        Device device1 = new Device();
        device1.setId("11");
        device1.setName("My Nexus");
        device1.setPlatform("android");
        device1.setPlatformVersion("5.0");
        device1.setImage("/store/extensions/assets/mobileapp/resources/models/nexus.png");
        device1.setModel("Nexus");
        device1.setType("phone");

        Device device2 = new Device();
        device2.setId("12");
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
