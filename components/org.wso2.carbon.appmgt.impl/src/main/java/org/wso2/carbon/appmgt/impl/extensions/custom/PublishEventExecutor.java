/**
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appmgt.impl.extensions.custom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.dto.PublishApplicationWorkflowDTO;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.impl.workflow.*;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.jaggery.scxml.management.DynamicValueInjector;
import org.wso2.jaggery.scxml.management.StateExecutor;
import org.wso2.jaggery.scxml.threading.JaggeryThreadLocalMediator;
import org.wso2.jaggery.scxml.threading.contexts.JaggeryThreadContext;

import java.util.Map;

/*
Description:The executor parses the parameter map defined in the
            registry.xml transition element and creates a JaggeryThreadContext
            which contains information on the permissions to be changed.
            The actual permission change logic runs in the JaggeryExecutorHandler.
Filename: GenericExecutor.java
Created Date: 26/8/2013
 */
public class PublishEventExecutor implements Execution
{
    private static final Log log=LogFactory.getLog(PublishEventExecutor.class);

    private UserRealm userRealm;
    private int tenantId;
    private StateExecutor stateExecutor;

    @Override
    public void init(Map map) {

        obtainTenantId();
        obtainUserRealm();
        this.stateExecutor=new StateExecutor(map);
    }


    /*
    The method performs some logic when ever a state transition takes place
    @requestContext: Contains context data about the transition
    e.g. Registry and Resource
    s: From state
    s2: To state
    @return: True if the execution took place correctly
     */
    @Override
    public boolean execute(RequestContext requestContext, String s, String s2){
        String resourceID = requestContext.getResource().getUUID();
        String appName = null;
        String appVersion = null;
        String appProvider = null;
        String lcState = null;
        String tenantDomain = null;
        String workflowRef= null;
        String newState = null;

        AppMDAO appMDAO;
        APIIdentifier apiIdentifier;

        try{
            //Get the registry
            Registry registry = RegistryCoreServiceComponent.getRegistryService().getGovernanceUserRegistry(CurrentSession.getUser(), CurrentSession.getTenantId());
            //Load Gov Artifacts
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);

            GenericArtifactManager artifactManager = new GenericArtifactManager(registry, AppMConstants.API_KEY);

            GenericArtifact webappArtifact = artifactManager.getGenericArtifact(resourceID);

            appName = webappArtifact.getAttribute("overview_name");
            appVersion = webappArtifact.getAttribute("overview_version");
            tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            tenantId = CurrentSession.getTenantId();

            appProvider = webappArtifact.getAttribute("overview_provider");
            workflowRef = concatStrings(appName,appVersion,appProvider,tenantDomain);

            newState = s2;

            lcState = webappArtifact.getLifecycleState();

        }catch (RegistryException e){
            //Change the interface impl to thorw exception
            log.error("Error while trying to retrieve registry artifact.", e);
            return false;
        }

        WorkflowExecutor appPublishWFExecutor = null;
        try {
            appPublishWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APP_PUBLISH);
        } catch (WorkflowException e) {
            log.error("Error while executing workflow.", e);
            return false;
        }

        PublishApplicationWorkflowDTO workflowDTO = new PublishApplicationWorkflowDTO();
        //This is the status of the workflow, and not the APP
        workflowDTO.setStatus(WorkflowStatus.CREATED);
        workflowDTO.setCreatedTime(System.currentTimeMillis());
        workflowDTO.setExternalWorkflowReference(appPublishWFExecutor.generateUUID());
        workflowDTO.setWorkflowReference(String.valueOf(workflowRef));
        workflowDTO.setWorkflowType(WorkflowConstants.WF_TYPE_AM_APP_PUBLISH);
        workflowDTO.setCallbackUrl(appPublishWFExecutor.getCallbackURL());
        workflowDTO.setAppName(appName);
        workflowDTO.setLcState(lcState);
        workflowDTO.setNewState(newState);
        workflowDTO.setAppVersion(appVersion);
        workflowDTO.setAppProvider(appProvider);
        workflowDTO.setTenantId(tenantId);
        workflowDTO.setTenantDomain(tenantDomain);

        try {
            appPublishWFExecutor.execute(workflowDTO);
        } catch (WorkflowException e) {
            log.error("Could not execute Application Publish Workflow", e);
            //throw new AppManagementException("Could not execute Application Publish Workflow", e);
            return false;
        }


        if(s2.equalsIgnoreCase(AppMConstants.ApplicationStatus.APPLICATION_RETIRED)) {
            appMDAO = new AppMDAO();
            apiIdentifier = new APIIdentifier(appProvider, appName, appVersion);

            try {
                appMDAO.removeAPISubscription(apiIdentifier);
            } catch (AppManagementException e) {
                log.error("Could not remove subscription when Unpublishing", e);
                return false;
            }
        }

        JaggeryThreadContext jaggeryThreadContext=new JaggeryThreadContext();

        //The path of the asset
        String path=requestContext.getResource().getPath();

        //Used to inject asset specific information to a permission instruction

        DynamicValueInjector dynamicValueInjector=new DynamicValueInjector();

        boolean isEmailEnabled = Boolean.parseBoolean(CarbonUtils.getServerConfiguration().getFirstProperty("EnableEmailUserName"));
        String provider = requestContext.getResource().getAuthorUserName();
//        TODO: Check email enabled case and remove or uncomment the following
//        if (provider != null && !isEmailEnabled && provider.contains("-AT-")) {
//            provider = provider.substring(0, provider.indexOf("-AT-"));
//
//        }

        //Set the asset author key
        dynamicValueInjector.setDynamicValue(DynamicValueInjector.ASSET_AUTHOR_KEY, provider);

        //Execute all permissions for the current state
        //this.stateExecutor.executePermissions(this.userRealm,dynamicValueInjector,path,s2);

        jaggeryThreadContext.setFromState(s);
        jaggeryThreadContext.setToState(s2);
        jaggeryThreadContext.setAssetPath(path);
        jaggeryThreadContext.setDynamicValueInjector(dynamicValueInjector);
        jaggeryThreadContext.setUserRealm(userRealm);
        jaggeryThreadContext.setStateExecutor(stateExecutor);

        JaggeryThreadLocalMediator.set(jaggeryThreadContext);

        return true;
    }

    private String concatStrings(String appName, String appVersion, String appProvider, String tenantDomain){
        StringBuilder sb = new StringBuilder();
        sb.append(appName.concat(":"));
        sb.append(appVersion.concat(":"));
        //replace ':' with '/'
        String provider = AppManagerUtil.makeSecondaryUSNameDBFriendly(appProvider);
        sb.append(provider.concat(":"));
        sb.append(tenantDomain);
        return sb.toString();
    }


    /*
    The method obtains the tenant id from a string tenant id
     */
    private void obtainTenantId(){
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    /*
    The method is used to obtain the User Realm from the RealmContext
     */
    private void obtainUserRealm(){
        this.userRealm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
    }




}
