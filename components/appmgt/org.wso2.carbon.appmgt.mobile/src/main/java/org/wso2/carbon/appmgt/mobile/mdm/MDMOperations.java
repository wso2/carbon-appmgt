package org.wso2.carbon.appmgt.mobile.mdm;

import org.wso2.carbon.appmgt.mobile.utils.User;

import java.util.HashMap;
import java.util.List;

/**
 * Created by dilan on 3/11/15.
 */
public interface MDMOperations {

    public void performAction(User currentUser,String action, App app, int tenantId, String type, String[] params, HashMap<String, String> configParams);

    public List<Device> getDevices(User currentUser, int tenantId, String type, String[] params, String platform, String platformVersion, boolean isSampleDevicesEnabled, HashMap<String, String> configParams);

}
