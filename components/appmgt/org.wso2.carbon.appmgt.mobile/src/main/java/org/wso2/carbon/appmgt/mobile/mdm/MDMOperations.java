package org.wso2.carbon.appmgt.mobile.mdm;

import org.json.simple.JSONArray;
import org.wso2.carbon.appmgt.mobile.utils.User;

/**
 * Created by dilan on 3/11/15.
 */
public interface MDMOperations {

    public void performAction(User currentUser, String ServerUrl, String action, App app, int tenantId, String type, String[] params);

    public JSONArray getDevices(User currentUser, String serverURL, int tenantId, String type, String[] params, String platform, String platformVersion, boolean isSampleDevicesEnabled);

}
