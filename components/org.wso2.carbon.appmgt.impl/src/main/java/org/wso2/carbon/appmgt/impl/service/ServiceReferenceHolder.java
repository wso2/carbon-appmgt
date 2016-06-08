/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appmgt.impl.service;

import org.wso2.carbon.appmgt.api.IdentityApplicationManagementFactory;
import org.wso2.carbon.appmgt.impl.AppManagerConfigurationService;
import org.wso2.carbon.appmgt.impl.AppMgtXACMLPolicyTemplateReader;
import org.wso2.carbon.appmgt.api.AppUsageStatisticsClient;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private RegistryService registryService;
    private AppManagerConfigurationService amConfigurationService;
    private RealmService realmService;
    private static UserRealm userRealm;
    private TenantIndexingLoader indexLoader;
    private AppMgtXACMLPolicyTemplateReader xacmlPolicyTemplateReader;
    private IdentityApplicationManagementFactory identityApplicationManagementFactory;
    private AppUsageStatisticsClient appUsageStatClient;

    public static ConfigurationContextService getContextService() {
        return contextService;
    }

    public static void setContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.contextService = contextService;
    }

    private static ConfigurationContextService contextService;

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public AppManagerConfigurationService getAPIManagerConfigurationService() {
        return amConfigurationService;
    }

    public void setAPIManagerConfigurationService(AppManagerConfigurationService amConfigurationService) {
        this.amConfigurationService = amConfigurationService;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public static void setUserRealm(UserRealm realm) {
        userRealm = realm;
    }

    public static UserRealm getUserRealm() {
        return userRealm;
    }

    public void setIndexLoaderService(TenantIndexingLoader indexLoader) {
        this.indexLoader = indexLoader;
    }

    public TenantIndexingLoader getIndexLoaderService() {
        return indexLoader;
    }

    public void setAppMgtXACMLPolicyTemplateReader(AppMgtXACMLPolicyTemplateReader xacmlPolicyTemplateReader) {
        this.xacmlPolicyTemplateReader = xacmlPolicyTemplateReader;
    }

    public AppMgtXACMLPolicyTemplateReader getAppMgtXACMLPolicyTemplateReader() {
        return xacmlPolicyTemplateReader;
    }

    public IdentityApplicationManagementFactory getIdentityApplicationManagementFactory() {
        return identityApplicationManagementFactory;
    }

    public void setIdentityApplicationManagementFactory(
            IdentityApplicationManagementFactory identityApplicationManagementFactory) {
        this.identityApplicationManagementFactory = identityApplicationManagementFactory;
    }

    public AppUsageStatisticsClient getAppUsageStatClient() {
        return appUsageStatClient;
    }

    public void setAppUsageStatClient(AppUsageStatisticsClient appUsageStatClient) {
        this.appUsageStatClient = appUsageStatClient;
    }
}
