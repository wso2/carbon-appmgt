package org.wso2.carbon.appmgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.*;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * The default implementation of DefaultAppRepository which uses RDBMS and Carbon registry for persistence.
 */
public class DefaultAppRepository implements AppRepository {


    private RegistryService registryService;
    private Registry registry;
    private int tenantId;
    private String tenantDomain;
    private String username;
    private Log log = LogFactory.getLog(getClass());

    public DefaultAppRepository() throws AppManagementException {
        try {
            String loggedInUsername = CarbonContext.getThreadLocalCarbonContext().getUsername();

            this.tenantDomain = MultitenantUtils.getTenantDomain(username);
            this.username = MultitenantUtils.getTenantAwareUsername(loggedInUsername);
            ;
            this.tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(username);
            AppManagerUtil.loadTenantRegistry(tenantId);
            this.registry = ServiceReferenceHolder.getInstance().
                    getRegistryService().getGovernanceUserRegistry(username, tenantId);

        } catch (RegistryException e) {
            handleException("Error while instantiating Registry for user : " + username, e);
        } catch (UserStoreException e) {
            handleException("Error while obtaining user registry for user:" + username, e);
        }
    }

    @Override
    public String saveApp(App app) {

        savePolicyGroups(app);
        saveRegistryArtifact(app);
        saveAppToRDMS(app);
        saveServiceProvider(app);

        return null;
    }

    private void savePolicyGroups(App app) {

    }

    private long savePolicyGroup(EntitlementPolicyGroup policyGroup){
        return -1;
    }

    private String saveRegistryArtifact(App app){
        String appUUID = null;
        if(AppMConstants.WEBAPP_ASSET_TYPE.equals(app.getType())){
            appUUID = saveWebAppRegistryArtifact((WebApp) app);
        }

    }

    private long saveAppToRDMS(App app){
        return -1;
    }

    private void saveServiceProvider(App app){

    }

    private String saveWebAppRegistryArtifact(WebApp webApp) throws AppManagementException {
        String artifactId = null;

        GenericArtifactManager artifactManager = null;
        try {
            final String webAppName = webApp.getId().getApiName();
            Map<String, List<String>> attributeListMap = new HashMap<String, List<String>>();

            artifactManager = AppManagerUtil.getArtifactManager(registry,
                    AppMConstants.WEBAPP_ASSET_TYPE);
            registry.beginTransaction();
            GenericArtifact genericArtifact =
                    artifactManager.newGovernanceArtifact(new QName(webApp.getId().getApiName()));
            GenericArtifact artifact = AppManagerUtil.createWebAppArtifactContent(genericArtifact, webApp);
            artifactManager.addGenericArtifact(artifact);
            artifactId = artifact.getId();
            changeLifeCycleStatus(AppMConstants.WEBAPP_LIFE_CYCLE, artifactId, APPLifecycleActions.CREATE.getStatus());
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());

            Set<String> tagSet = webApp.getTags();
            if (tagSet != null) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }
            if (webApp.getAppVisibility() != null) {
                AppManagerUtil.setResourcePermissions(webApp.getId().getProviderName(),
                        AppMConstants.API_RESTRICTED_VISIBILITY, webApp.getAppVisibility(), artifactPath);
            }
            String providerPath = AppManagerUtil.getAPIProviderPath(webApp.getId());
            //provider ------provides----> WebApp
            registry.addAssociation(providerPath, artifactPath, AppMConstants.PROVIDER_ASSOCIATION);
            registry.commitTransaction();
        } catch (RegistryException e) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException re) {
                handleException(
                        "Error while rolling back the transaction for web application: "
                                + webApp.getId().getApiName(), re);
            }
            handleException("Error occurred while creating the web application : " + webApp.getId().getApiName(), e);
        }
        return artifactId;

    }

    private String saveMobileAppRegistryArtifact(MobileApp mobileApp){
        return null;

    }
    protected void handleException(String msg, Exception e) throws AppManagementException {
        log.error(msg, e);
        throw new AppManagementException(msg, e);
    }

    protected void handleException(String msg) throws AppManagementException {
        log.error(msg);
        throw new AppManagementException(msg);
    }


}
