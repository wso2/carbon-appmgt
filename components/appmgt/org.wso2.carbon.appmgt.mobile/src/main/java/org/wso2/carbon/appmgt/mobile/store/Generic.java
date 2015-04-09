package org.wso2.carbon.appmgt.mobile.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.ActionConstants;

/**
 * Created by dilan on 4/9/15.
 */
public class Generic {

    private static final Log log = LogFactory.getLog(Generic.class);

    public boolean showAppVisibilityToUser(String appPath, String username, String opType){


        String userRole = "Internal/private_" + username;

        try {
            if(opType.equalsIgnoreCase("ALLOW")) {
                org.wso2.carbon.user.api.UserRealm realm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
                realm.getAuthorizationManager().authorizeRole(userRole, appPath, ActionConstants.GET);
                return true;
            }else if(opType.equalsIgnoreCase("DENY")){
                org.wso2.carbon.user.api.UserRealm realm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
                realm.getAuthorizationManager().denyRole(userRole, appPath, ActionConstants.GET);
                return true;
            }
            return false;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while updating visibility of mobile app at " + appPath);
            log.debug("Error : " + e);
            return false;
        }
    }
}
