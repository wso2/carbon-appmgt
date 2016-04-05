package org.wso2.carbon.appmgt.rest.api.store.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.rest.api.store.DevicesApiService;

import javax.ws.rs.core.Response;

public class DevicesApiServiceImpl extends DevicesApiService {
    private static final Log log = LogFactory.getLog(DevicesApiServiceImpl.class);

    @Override
    public Response devicesGet(String query,Integer limit,Integer offset,String accept,String ifNoneMatch){
      /*
        Devices devices = new Devices();

        String user = RestApiUtil.getLoggedInUsername();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        int tenantId = 1;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("Error while initializing UserStore", e, log);
        }
        String[] users = {user};
        devices.getDevicesList(user, tenantId, "user", users);
        */return null;
    }
}
