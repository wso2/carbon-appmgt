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
import org.wso2.carbon.appmgt.mobile.mdm.App;
import org.wso2.carbon.appmgt.mobile.mdm.AppDataLoader;
import org.wso2.carbon.appmgt.mobile.mdm.MDMOperations;
import org.wso2.carbon.appmgt.mobile.utils.MobileConfigurations;
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

public class Operations {

    private static final Log log = LogFactory.getLog(Operations.class);

    public void performAction(String action, int tenantId, String type, String app, String[] params ){
        log.debug("Action: " + action +  ", tenantId: " + tenantId + ", type: " + type + ", app: " + app);
        MobileConfigurations configurations = MobileConfigurations.getInstance();
        String serverUrl = configurations.getMDMServerURL();

        try {

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration().getAdminUserName());

            CarbonContext cCtx = CarbonContext.getThreadLocalCarbonContext();
            Registry registry = cCtx.getRegistry(RegistryType.USER_GOVERNANCE);

            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            GenericArtifactManager artifactManager = new GenericArtifactManager((UserRegistry)registry, "mobileapp");
            GenericArtifact artifact = artifactManager.getGenericArtifact(app);

            Class<MDMOperations> mdmOperationsClass = (Class<MDMOperations>) Class.forName(configurations.getMDMOperationsClass());
            MDMOperations mdmOperations = (MDMOperations) mdmOperationsClass.newInstance();
            App appToInstall = AppDataLoader.load(new App(), artifact);
            mdmOperations.performAction(serverUrl, action, appToInstall, tenantId, type, params);


        } catch (UserStoreException e) {
            e.printStackTrace();
        } catch (GovernanceException e) {
            e.printStackTrace();
        } catch (RegistryException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }


    }

}
