/*
 *
 *   Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.mobile.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.mobile.beans.ApplicationOperationAction;
import org.wso2.carbon.appmgt.mobile.interfaces.ApplicationOperations;
import org.wso2.carbon.appmgt.mobile.mdm.App;
import org.wso2.carbon.appmgt.mobile.mdm.AppDataLoader;
import org.wso2.carbon.appmgt.mobile.mdm.MDMServiceReferenceHolder;
import org.wso2.carbon.appmgt.mobile.utils.MobileApplicationException;
import org.wso2.carbon.appmgt.mobile.utils.MobileConfigurations;
import org.wso2.carbon.appmgt.mobile.utils.User;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;

/**
 * Class responsible for perform actions on MDM
 */
public class Operations {

    private static final Log log = LogFactory.getLog(Operations.class);

    /**
     * Performs action on MDM
     * @param currentUser User who perform the action
     * @param tenantId Tenant Id
     * @param type Type of the resource (device, user or role)
     * @param params Collection of ids of the type
     */
    public void performAction(String currentUser, String action, int tenantId, String type, String app, String[] params, String schedule )
            throws MobileApplicationException {
        if(log.isDebugEnabled()) log.debug("Action: " + action +  ", tenantId: " + tenantId + ", type: " + type + ", app: " + app);
        MobileConfigurations configurations = MobileConfigurations.getInstance();

        ApplicationOperationAction applicationOperationAction = new ApplicationOperationAction();
        User user = new User();
        JSONObject userObj = (JSONObject) new JSONValue().parse(currentUser);
        user.setUsername((String) userObj.get("username"));
        user.setTenantDomain((String) userObj.get("tenantDomain"));
        user.setTenantId(Integer.valueOf(String.valueOf(userObj.get("tenantId"))));
        applicationOperationAction.setUser(user);
        applicationOperationAction.setAction(action);
        applicationOperationAction.setTenantId(tenantId);
        applicationOperationAction.setType(type);
        applicationOperationAction.setParams(params);
        applicationOperationAction.setConfigParams(configurations.getActiveMDMProperties());
        applicationOperationAction.setSchedule(schedule);

        try {

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration().getAdminUserName());

            CarbonContext cCtx = CarbonContext.getThreadLocalCarbonContext();
            Registry registry = cCtx.getRegistry(RegistryType.USER_GOVERNANCE);

            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            GenericArtifactManager artifactManager = new GenericArtifactManager((UserRegistry)registry, "mobileapp");
            GenericArtifact artifact = artifactManager.getGenericArtifact(app);

            ApplicationOperations applicationOperations =  MDMServiceReferenceHolder.getInstance().getMDMOperation();
            App appToInstall = AppDataLoader.load(new App(), artifact, action, tenantId);
            applicationOperationAction.setApp(appToInstall);
            applicationOperations.performAction(applicationOperationAction);


        } catch (UserStoreException e) {
            log.error("error occurred at the user store", e);
        } catch (GovernanceException e) {
            log.error("error occurred from governance registry", e);
        } catch (RegistryException e) {
            log.error("error occurred from registry", e);
        }  finally {
            PrivilegedCarbonContext.endTenantFlow();
        }


    }

}
