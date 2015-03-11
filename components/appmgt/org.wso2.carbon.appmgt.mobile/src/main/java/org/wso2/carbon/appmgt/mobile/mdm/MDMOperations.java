package org.wso2.carbon.appmgt.mobile.mdm;

/**
 * Created by dilan on 3/11/15.
 */
public interface MDMOperations {

    public void installApplication(String ServerUrl, String action, App app, int tenantId, String type, String[] params);
}
