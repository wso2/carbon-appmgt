package org.wso2.carbon.appmgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.App;
import org.wso2.carbon.appmgt.api.model.EntitlementPolicyGroup;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

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
        return null;
    }

    private long saveAppToRDMS(App app){
        return -1;
    }

    private void saveServiceProvider(App app){

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
