package org.wso2.carbon.appmgt.mobile.mdm;

import org.json.simple.JSONArray;

/**
 * Created by dilan on 3/11/15.
 */
public interface MDMOperations {

    public void performAction(String ServerUrl, String action, App app, int tenantId, String type, String[] params);

    public JSONArray getDevices(String serverURL, int tenantId, String type, String[] params, String platform, String platformVersion, boolean isSampleDevicesEnabled);

}
