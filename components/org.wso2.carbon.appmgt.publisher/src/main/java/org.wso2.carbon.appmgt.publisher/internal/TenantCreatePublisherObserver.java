/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.appmgt.publisher.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * Load configuration files to tenant's registry.
 */
public class TenantCreatePublisherObserver extends AbstractAxis2ConfigurationContextObserver {
    private static final Log log = LogFactory.getLog(TenantCreatePublisherObserver.class);
    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            //load workflow-extension configuration to the registry.
            AppManagerUtil.loadTenantWorkFlowExtensions(tenantId);
        } catch (AppManagementException e) {
            log.error("Failed to load workflow-extension.xml to tenant " + tenantDomain + "'s registry");
        }

        try {
            //load external-stores configuration to the registry
            AppManagerUtil.loadTenantExternalStoreConfig(tenantId);
        } catch (AppManagementException e) {
            log.error("Failed to load external-stores.xml to tenant " + tenantDomain + "'s registry");
        }

        try {
            //Add the creator & publisher roles if not exists
            //Apply permissons to appmgt collection for creator role
            UserRealm realm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();

            Permission[] creatorPermissions = new Permission[]{
                    new Permission(AppMConstants.Permissions.LOGIN, UserMgtConstants.EXECUTE_ACTION),
                    new Permission(AppMConstants.Permissions.WEB_APP_CREATE, UserMgtConstants.EXECUTE_ACTION),
                    new Permission(AppMConstants.Permissions.WEB_APP_DELETE, UserMgtConstants.EXECUTE_ACTION),
                    new Permission(AppMConstants.Permissions.WEB_APP_UPDATE, UserMgtConstants.EXECUTE_ACTION),
                    new Permission(AppMConstants.Permissions.DOCUMENT_ADD, UserMgtConstants.EXECUTE_ACTION),
                    new Permission(AppMConstants.Permissions.DOCUMENT_DELETE, UserMgtConstants.EXECUTE_ACTION),
                    new Permission(AppMConstants.Permissions.DOCUMENT_EDIT, UserMgtConstants.EXECUTE_ACTION),
                    new Permission(AppMConstants.Permissions.MOBILE_APP_CREATE, UserMgtConstants.EXECUTE_ACTION),
                    new Permission(AppMConstants.Permissions.MOBILE_APP_DELETE, UserMgtConstants.EXECUTE_ACTION),
                    new Permission(AppMConstants.Permissions.MOBILE_APP_UPDATE, UserMgtConstants.EXECUTE_ACTION)};

            AppManagerUtil.addNewRole(AppMConstants.CREATOR_ROLE, creatorPermissions, realm);

            Permission[] publisherPermissions = new Permission[]{
                    new Permission(AppMConstants.Permissions.LOGIN, UserMgtConstants.EXECUTE_ACTION),
                    new Permission(AppMConstants.Permissions.WEB_APP_PUBLISH, UserMgtConstants.EXECUTE_ACTION),
                    new Permission(AppMConstants.Permissions.VIEW_STATS, UserMgtConstants.EXECUTE_ACTION),
                    new Permission(AppMConstants.Permissions.MOBILE_APP_PUBLISH, UserMgtConstants.EXECUTE_ACTION)};

            AppManagerUtil.addNewRole(AppMConstants.PUBLISHER_ROLE,publisherPermissions, realm);

            //Add the store-admin role
            Permission[] storeAdminPermissions = new Permission[]
                    {new Permission(AppMConstants.Permissions.LOGIN, UserMgtConstants.EXECUTE_ACTION)};
            AppManagerUtil.addNewRole(AppMConstants.STORE_ADMIN_ROLE, storeAdminPermissions , realm);
        } catch(AppManagementException e) {
            log.error("App manager configuration service is set to publisher bundle");
        }

        try{
            AppManagerUtil.writeDefinedSequencesToTenantRegistry(tenantId);
        }catch(AppManagementException e){
            log.error("Failed to write defined sequences to tenant " + tenantDomain + "'s registry");
        }
    }
}
