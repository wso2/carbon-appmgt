package org.wso2.carbon.appmgt.mobile.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.ActionConstants;

/**
 * Class which has Generic operations for store
 */
public class Generic {

    private static final Log log = LogFactory.getLog(Generic.class);

    /**
     * Shows application visibility to the user
     * @param appPath Path of the application
     * @param username Username of the user
     * @param opType Op type (ALLOW OR DENY)
     * @return whether it is success
     */
    public boolean showAppVisibilityToUser(String appPath, String username, String opType){


        String userRole = "Internal/private_" + username;

        try {
            if("ALLOW".equalsIgnoreCase(opType)) {
                org.wso2.carbon.user.api.UserRealm realm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
                realm.getAuthorizationManager().authorizeRole(userRole, appPath, ActionConstants.GET);
                return true;
            }else if("DENY".equalsIgnoreCase(opType)){
                org.wso2.carbon.user.api.UserRealm realm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
                realm.getAuthorizationManager().denyRole(userRole, appPath, ActionConstants.GET);
                return true;
            }
            return false;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while updating visibility of mobile app at " + appPath, e);
            return false;
        }
    }
}
