/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.wso2.carbon.appmgt.rest.api.storeadmin.utils.mappings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.user.api.UserStoreException;

public class APPMappingUtil {

    private static final Log log = LogFactory.getLog(APPMappingUtil.class);

    public static void subscribeApp(Registry registry, String userId, String appId)
            throws org.wso2.carbon.registry.api.RegistryException {
        String path = "users/" + userId + "/subscriptions/mobileapp/" + appId;
        Resource resource = null;
        try {
            resource = registry.get(path);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("RegistryException occurred", e);
        }
        if (resource == null) {
            resource = registry.newResource();
            resource.setContent("");
            registry.put(path, resource);
        }
    }


    public static void unSubscribeApp(Registry registry, String userId, String appId) throws RegistryException {
        String path = "users/" + userId + "/subscriptions/mobileapp/" + appId;
        try {
            registry.delete(path);
        } catch (RegistryException e) {
            log.error("Error while deleting registry path: " + path, e);
            throw e;
        }
    }

    public static boolean showAppVisibilityToUser(String appPath, String username, String opType)
            throws UserStoreException {
        String userRole = "Internal/private_" + username;

        try {
            if ("ALLOW".equalsIgnoreCase(opType)) {
                org.wso2.carbon.user.api.UserRealm realm =
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
                realm.getAuthorizationManager().authorizeRole(userRole, appPath, ActionConstants.GET);
                return true;
            } else if ("DENY".equalsIgnoreCase(opType)) {
                org.wso2.carbon.user.api.UserRealm realm =
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
                realm.getAuthorizationManager().denyRole(userRole, appPath, ActionConstants.GET);
                return true;
            }
            return false;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while updating visibility of mobile app at " + appPath, e);
            throw e;
        }
    }

}
