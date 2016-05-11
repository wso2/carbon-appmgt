package org.wso2.carbon.appmgt.impl;

import org.wso2.carbon.appmgt.api.model.App;
import org.wso2.carbon.appmgt.api.model.EntitlementPolicyGroup;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.registry.core.Registry;

/**
 * The default implementation of DefaultAppRepository which uses RDBMS and Carbon registry for persistence.
 */
public class DefaultAppRepository implements AppRepository{


    private RegistryService registryService;

    public DefaultAppRepository(){
        this.registryService = ServiceReferenceHolder.getInstance().getRegistryService();
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

}
