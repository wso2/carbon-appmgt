/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.appmgt.impl.internal;

import org.apache.axis2.engine.ListenerManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.AppUsageStatisticsClient;
import org.wso2.carbon.appmgt.api.IdentityApplicationManagementFactory;
import org.wso2.carbon.appmgt.impl.*;
import org.wso2.carbon.appmgt.impl.idp.sso.configurator.IS510IdentityApplicationManagementFactory;
import org.wso2.carbon.appmgt.impl.listners.UserAddListener;
import org.wso2.carbon.appmgt.impl.observers.APIStatusObserverList;
import org.wso2.carbon.appmgt.impl.observers.SignupObserver;
import org.wso2.carbon.appmgt.impl.service.APIMGTSampleService;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.impl.utils.AppMgtDataSourceProvider;
import org.wso2.carbon.appmgt.impl.utils.RemoteAuthorizationManager;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.listener.UserStoreManagerListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.FileUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @scr.component name="org.wso2.appmgt.impl.services.appm" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="user.realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="listener.manager.service"
 * interface="org.apache.axis2.engine.ListenerManager" cardinality="0..1" policy="dynamic"
 * bind="setListenerManager" unbind="unsetListenerManager"
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic"
 * bind="setTenantRegistryLoader"
 * unbind="unsetTenantRegistryLoader"
 * @scr.reference name="tenant.indexloader"
 * interface="org.wso2.carbon.registry.indexing.service.TenantIndexingLoader" cardinality="1..1" policy="dynamic"
 * bind="setIndexLoader" unbind="unsetIndexLoader"
 * @scr.reference name="identity.application.management.component"
 * interface="org.wso2.carbon.identity.application.mgt.ApplicationManagementService" cardinality="1..1" policy="dynamic"
 * bind="setApplicationMgtService" unbind="unsetApplicationMgtService"
 * @scr.reference name="identity.application.management.adapter"
 * interface="org.wso2.carbon.appmgt.api.IdentityApplicationManagementFactory" cardinality="0..n" policy="dynamic"
 * bind="setIdentityApplicationManagementFactory" unbind="unsetIdentityApplicationManagementFactory"
 * @scr.reference name="app.manager.default.stat.usageClient"
 * interface="org.wso2.carbon.appmgt.api.AppUsageStatisticsClient" cardinality="0..n"
 * policy="dynamic" bind="setAppUsageStatisticsClient" unbind="unsetAppUsageStatisticsClient"
 */
public class AppManagerComponent {
    //TODO refactor caching implementation

    private static final Log log = LogFactory.getLog(AppManagerComponent.class);

    private ServiceRegistration registration;
    private static TenantRegistryLoader tenantRegistryLoader;
    private APIMGTSampleService apimgtSampleService;
    private AppUsageStatisticsClient appUsageStatisticsClient;
    private Set<AppUsageStatisticsClient> appUsageStatisticsClients = new HashSet<>();
    private  String appUsageStatisticsClientImplClass;

    protected void activate(ComponentContext componentContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("WebApp manager component activated");
        }

        apimgtSampleService = new APIMGTSampleService();

        try {
            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext.registerService(APIMGTSampleService.class.getName(),apimgtSampleService, null);
            addRxtConfigs();
            addTierPolicies();
            addDefinedSequencesToRegistry();

            AppManagerConfiguration configuration = new AppManagerConfiguration();
            String filePath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "app-manager.xml";
            configuration.load(filePath);

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            AppManagerUtil.loadTenantWorkFlowExtensions(tenantId);
            AppManagerUtil.loadTenantExternalStoreConfig(tenantId);

            //load self sigup configuration to the registry
            AppManagerUtil.loadTenantSelfSignUpConfigurations(tenantId);
            AppManagerUtil.createSelfSignUpRoles(tenantId);
            AppManagerUtil.loadTenantConf(tenantId);
            SignupObserver signupObserver = new SignupObserver();
            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), signupObserver,null);

            AppManagerConfigurationServiceImpl configurationService =
                    new AppManagerConfigurationServiceImpl(configuration);

            //ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(configurationService);
            registration = componentContext.getBundleContext().registerService(
                    AppManagerConfigurationService.class.getName(), configurationService, null);
            APIStatusObserverList.getInstance().init(configuration);

            AuthorizationUtils.addAuthorizeRoleListener(AppMConstants.AM_CREATOR_APIMGT_EXECUTION_ID,
                                                        RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH
                                                                        + AppMConstants.APPMGT_APPLICATION_DATA_LOCATION),
                                                        AppMConstants.Permissions.WEB_APP_CREATE,
                                                        UserMgtConstants.EXECUTE_ACTION, null);

            AuthorizationUtils.addAuthorizeRoleListener(AppMConstants.AM_MOBILE_CREATOR_APIMGT_EXECUTION_ID,
                                                        RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                                                      RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                                                      AppMConstants.APPMGT_MOBILE_REGISTRY_LOCATION),
                                                        AppMConstants.Permissions.MOBILE_APP_CREATE,
                                                        UserMgtConstants.EXECUTE_ACTION, null);

            //Add the creator and publisher roles
            org.wso2.carbon.user.api.UserRealm realm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();

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

            setupImagePermissions();
            RemoteAuthorizationManager authorizationManager = RemoteAuthorizationManager.getInstance();
            authorizationManager.init();
            APIMgtDBUtil.initialize();
            AppMgtDataSourceProvider.initialize();
            //Check User add listener enabled or not
            boolean selfSignInProcessEnabled = Boolean.parseBoolean(
                    configuration.getFirstProperty("WorkFlowExtensions.SelfSignIn.ProcessEnabled"));
            if (selfSignInProcessEnabled) {
                if (bundleContext != null) {
                    bundleContext.registerService(UserStoreManagerListener.class.getName(), new UserAddListener(), null);
                }
            }

            new AppManagerUtil().setupSelfRegistration(configuration, MultitenantConstants.SUPER_TENANT_ID);

            //create mobileapps directory if it does not exists
            AppManagerUtil.createMobileAppsDirectory();

            if(ServiceReferenceHolder.getInstance().getIdentityApplicationManagementFactory() == null) {
                //Sets the default adapter
                unsetIdentityApplicationManagementFactory(null);
            }

            //Find the app usage statistics client which is proffered in the configuration and set instance of it.
            appUsageStatisticsClientImplClass = configuration.getFirstProperty(AppMConstants.APP_STATISTIC_CLIENT_PROVIDER);
            doRegisterAppUsageStatisticsClient();

        } catch (AppManagementException e) {
            log.error("Error while initializing the WebApp manager component", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating WebApp manager component");
        }
        registration.unregister();
        APIManagerFactory.getInstance().clearAll();
        RemoteAuthorizationManager authorizationManager = RemoteAuthorizationManager.getInstance();
        authorizationManager.destroy();
    }

    private void doRegisterAppUsageStatisticsClient() {
        for (AppUsageStatisticsClient tempAppUsageStatisticsClient : appUsageStatisticsClients) {
            if (tempAppUsageStatisticsClient.getClass().getName().equals(appUsageStatisticsClientImplClass)) {
                appUsageStatisticsClient = tempAppUsageStatisticsClient;
            }
        }
        if (appUsageStatisticsClient != null) {
            org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder.getInstance()
                    .setAppUsageStatClient(appUsageStatisticsClient);
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        ServiceReferenceHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceReferenceHolder.getInstance().setRegistryService(null);
    }

    protected void setApplicationMgtService(ApplicationManagementService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Application mgt service initialized.");
        }
    }

    protected void unsetApplicationMgtService(ApplicationManagementService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Application mgt service destroyed.");
        }
    }

    protected void setIndexLoader(TenantIndexingLoader indexLoader) {
        if (indexLoader != null && log.isDebugEnabled()) {
            log.debug("IndexLoader service initialized");
        }
        ServiceReferenceHolder.getInstance().setIndexLoaderService(indexLoader);
    }

    protected void unsetIndexLoader(TenantIndexingLoader registryService) {
        ServiceReferenceHolder.getInstance().setIndexLoaderService(null);
    }

    protected void setRealmService(RealmService realmService) {
        if (realmService != null && log.isDebugEnabled()) {
            log.debug("Realm service initialized");
        }
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(null);
    }

    protected void setListenerManager(ListenerManager listenerManager) {
        // We bind to the listener manager so that we can read the local IP
        // address and port numbers properly.
        log.debug("Listener manager bound to the WebApp manager component");
        AppManagerConfigurationService service = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService();
        if (service != null) {
            service.getAPIManagerConfiguration().reloadSystemProperties();
        }
    }

    protected void unsetListenerManager(ListenerManager listenerManager) {
        log.debug("Listener manager unbound from the WebApp manager component");
    }

    private void addRxtConfigs() throws AppManagementException {
        String rxtDir = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                "resources" + File.separator + "rxts";
        File file = new File(rxtDir);
        //create a FilenameFilter
        FilenameFilter filenameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                //if the file extension is .rxt return true, else false
                return name.endsWith(".rxt");
            }
        };
        String[] rxtFilePaths = file.list(filenameFilter);
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        UserRegistry systemRegistry;
        try {
            systemRegistry = registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        } catch (RegistryException e) {
            throw new AppManagementException("Failed to get registry", e);
        }

        for (String rxtPath : rxtFilePaths) {
            String resourcePath = GovernanceConstants.RXT_CONFIGS_PATH +
                    RegistryConstants.PATH_SEPARATOR + rxtPath;
            try {
                if (systemRegistry.resourceExists(resourcePath)) {
                    continue;
                }
                String rxt = FileUtil.readFileToString(rxtDir + File.separator + rxtPath);
                Resource resource = systemRegistry.newResource();
                resource.setContent(rxt.getBytes());
                resource.setMediaType(AppMConstants.RXT_MEDIA_TYPE);
                systemRegistry.put(resourcePath, resource);
            } catch (IOException e) {
                String msg = "Failed to read rxt files";
                throw new AppManagementException(msg, e);
            } catch (RegistryException e) {
                String msg = "Failed to add rxt to registry ";
                throw new AppManagementException(msg, e);
            }
        }
    }

    private void setupImagePermissions() throws AppManagementException {
        try {
            AuthorizationManager accessControlAdmin = ServiceReferenceHolder.getInstance().
                    getRealmService().getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).
                    getAuthorizationManager();
            String imageLocation = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + AppMConstants.API_IMAGE_LOCATION;
            if (!accessControlAdmin.isRoleAuthorized(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                    imageLocation, ActionConstants.GET)) {
                // Can we get rid of this?
                accessControlAdmin.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                        imageLocation, ActionConstants.GET);
            }
        } catch (UserStoreException e) {
            throw new AppManagementException("Error while setting up permissions for image collection", e);
        }
    }

    private void addTierPolicies() throws AppManagementException {
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        try {
            UserRegistry registry = registryService.getGovernanceSystemRegistry();
            if (registry.resourceExists(AppMConstants.API_TIER_LOCATION)) {
                log.debug("Tier policies already uploaded to the registry");
                return;
            }

            log.debug("Adding WebApp tier policies to the registry");
            InputStream inputStream = AppManagerComponent.class.getResourceAsStream("/tiers/default-tiers.xml");
            byte[] data = IOUtils.toByteArray(inputStream);
            Resource resource = registry.newResource();
            resource.setContent(data);

            //  Properties descriptions = new Properties();
            //   descriptions.load(AppManagerComponent.class.getResourceAsStream(
            //           "/tiers/default-tier-info.properties"));
            //   Set<String> names = descriptions.stringPropertyNames();
            //   for (String name : names) {
            //       resource.setProperty(AppMConstants.TIER_DESCRIPTION_PREFIX + name,
            //              descriptions.getProperty(name));
            //  }
            //  resource.setProperty(AppMConstants.TIER_DESCRIPTION_PREFIX + AppMConstants.UNLIMITED_TIER,
            //         AppMConstants.UNLIMITED_TIER_DESC);
            registry.put(AppMConstants.API_TIER_LOCATION, resource);


        } catch (RegistryException e) {
            throw new AppManagementException("Error while saving policy information to the registry", e);
        } catch (IOException e) {
            throw new AppManagementException("Error while reading policy file content", e);
        }
    }

    private void addDefinedSequencesToRegistry() throws AppManagementException {
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        try {
            UserRegistry registry = registryService.getGovernanceSystemRegistry();
            if (registry.resourceExists(AppMConstants.API_CUSTOM_INSEQUENCE_LOCATION)) {
                if(log.isDebugEnabled()){
                    log.debug("Defined sequences have already been added to the registry");
                }
                return;
            }

            if(log.isDebugEnabled()){
                log.debug("Adding defined sequences to the registry.");
            }

            InputStream inSeqStream =
                    AppManagerComponent.class.getResourceAsStream("/definedsequences/in/log_in_message.xml");
            byte[] inSeqData = IOUtils.toByteArray(inSeqStream);
            Resource inSeqResource = registry.newResource();
            inSeqResource.setContent(inSeqData);

            registry.put(AppMConstants.API_CUSTOM_INSEQUENCE_LOCATION + "log_in_message.xml", inSeqResource);

            InputStream outSeqStream =
                    AppManagerComponent.class.getResourceAsStream("/definedsequences/out/log_out_message.xml");
            byte[] outSeqData = IOUtils.toByteArray(outSeqStream);
            Resource outSeqResource = registry.newResource();
            outSeqResource.setContent(outSeqData);

            registry.put(AppMConstants.API_CUSTOM_OUTSEQUENCE_LOCATION + "log_out_message.xml", outSeqResource);

        } catch (RegistryException e) {
            throw new AppManagementException("Error while saving defined sequences to the registry ", e);
        } catch (IOException e) {
            throw new AppManagementException("Error while reading defined sequence ", e);
        }
    }

    private void setupSelfRegistration(AppManagerConfiguration config) throws
                                                                       AppManagementException {
        boolean enabled = Boolean.parseBoolean(config.getFirstProperty(AppMConstants.SELF_SIGN_UP_ENABLED));
        if (!enabled) {
            return;
        }

        String role = config.getFirstProperty(AppMConstants.SELF_SIGN_UP_ROLE);
        if (role == null) {
            // Required parameter missing - Throw an exception and interrupt startup
            throw new AppManagementException("Required subscriber role parameter missing " +
                    "in the self sign up configuration");
        }

        boolean create = Boolean.parseBoolean(config.getFirstProperty(AppMConstants.SELF_SIGN_UP_CREATE_ROLE));
        if (create) {
            String[] permissions = new String[]{
                    "/permission/admin/login",
                    AppMConstants.Permissions.WEB_APP_SUBSCRIBE
            };
            try {
                RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
                UserRealm realm = realmService.getBootstrapRealm();
                UserStoreManager manager = realm.getUserStoreManager();
                if (!manager.isExistingRole(role)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating subscriber role: " + role);
                    }
                    Permission[] subscriberPermissions = new Permission[]{new Permission("/permission/admin/login",UserMgtConstants.EXECUTE_ACTION),
                            new Permission(AppMConstants.Permissions.WEB_APP_SUBSCRIBE, UserMgtConstants.EXECUTE_ACTION)};
                    String superTenantName = ServiceReferenceHolder.getInstance().getRealmService().getBootstrapRealmConfiguration().getAdminUserName();
                    String[] userList = new String[]{superTenantName};
                    manager.addRole(role, userList, subscriberPermissions);
                }
            } catch (UserStoreException e) {
                throw new AppManagementException("Error while creating subscriber role: " + role + " - " +
                        "Self registration might not function properly.", e);
            }
        }
    }


    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.setContextService(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.setContextService(null);
    }


    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        this.tenantRegistryLoader = tenantRegistryLoader;
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        this.tenantRegistryLoader = null;
    }

    public static TenantRegistryLoader getTenantRegistryLoader(){
        return tenantRegistryLoader;
    }

    public void setIdentityApplicationManagementFactory(
            IdentityApplicationManagementFactory identityApplicationManagementFactory) {
        ServiceReferenceHolder.getInstance()
                .setIdentityApplicationManagementFactory(identityApplicationManagementFactory);
    }

    public void unsetIdentityApplicationManagementFactory(
            IdentityApplicationManagementFactory identityApplicationManagementFactory) {
        ServiceReferenceHolder.getInstance()
                .setIdentityApplicationManagementFactory(new IS510IdentityApplicationManagementFactory());
    }

    protected void setAppUsageStatisticsClient(AppUsageStatisticsClient appUsageStatClient) {
        if (log.isDebugEnabled()) {
            log.debug("App usage stat client bind method is calling");
        }
        appUsageStatisticsClients.add(appUsageStatClient);
        doRegisterAppUsageStatisticsClient();
    }

    protected void unsetAppUsageStatisticsClient(AppUsageStatisticsClient appUsageStatClient) {
        if (log.isDebugEnabled()) {
            log.debug("App usage stat client unbind method is calling");
        }
        appUsageStatisticsClients.remove(appUsageStatClient);
    }
}
