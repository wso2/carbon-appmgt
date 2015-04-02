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

package org.wso2.carbon.appmgt.impl.workflow;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.APIStatus;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.APIManagerFactory;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.dto.PublishApplicationWorkflowDTO;
import org.wso2.carbon.appmgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.appmgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.user.api.UserStoreException;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

public class PublishAPPWSWorkflowExecutor extends WorkflowExecutor{

    private static final Log log = LogFactory.getLog(PublishAPPWSWorkflowExecutor.class);

    private String serviceEndpoint;

    private String username;

    private String password;

    private String contentType;

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }

    @Override
    public void execute(WorkflowDTO workflowDTO) throws WorkflowException{

        WorkflowExecutor appPublishWFExecutor = WorkflowExecutorFactory.getInstance().
                getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APP_PUBLISH);

        //Update Webapp state to IN-Review till the workflow is approved
        PublishApplicationWorkflowDTO publishAPPDTO = (PublishApplicationWorkflowDTO)workflowDTO;
        try  {
            String adminUserUsername = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration().getAdminUserName();
            APIProvider provider = APIManagerFactory.getInstance().getAPIProvider(adminUserUsername);
            APIIdentifier apiId = new APIIdentifier(publishAPPDTO.getAppProvider(), publishAPPDTO.getAppName(), publishAPPDTO.getAppVersion());
            WebApp api = provider.getAPI(apiId);
            if (api != null) {
                APIStatus oldState = getApiStatus(publishAPPDTO.getLcState());
                APIStatus newStatus = getApiStatus("IN-REVIEW");
                api.setStatus(oldState);
                provider.changeAPIStatus(api, newStatus, adminUserUsername, true);
            }
        }catch (AppManagementException e){
            log.error("Could not update APP lifecycle state to IN-REVIEW", e);
            throw new WorkflowException("Could not update APP lifecycle state to IN-REVIEW", e);
        } catch (UserStoreException e) {
            log.error("Error while retrieving user name of administrative user.", e);
            throw new WorkflowException("Could not update APP lifecycle state to IN-REVIEW", e);
        }

        try {
            if(publishAPPDTO.getLcState().equalsIgnoreCase(AppMConstants.ApplicationStatus.APPLICATION_CREATED)) {

                ServiceClient client = new ServiceClient(ServiceReferenceHolder.getInstance()
                                                                 .getContextService().getClientConfigContext(), null);
                Options options = new Options();
                options.setAction("http://workflow.publishapplication.apimgt.carbon.wso2.org/initiate");
                options.setTo(new EndpointReference(serviceEndpoint));
                if (contentType != null) {
                    options.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);
                }

                HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();

                //Consider this as a secured service if username and password are not null. Unsecured if not.
                if (username != null && password != null) {
                    auth.setUsername(username);
                    auth.setPassword(password);
                    auth.setPreemptiveAuthentication(true);
                    List<String> authSchemes = new ArrayList<String>();
                    authSchemes.add(HttpTransportProperties.Authenticator.BASIC);
                    auth.setAuthSchemes(authSchemes);

                    if (contentType == null) {
                        options.setProperty(Constants.Configuration.MESSAGE_TYPE, HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
                    }
                    options.setProperty(HTTPConstants.AUTHENTICATE, auth);
                    options.setManageSession(true);
                }

                client.setOptions(options);

                String payload = "<wor:PublishAppApprovalWorkFlowProcessRequest xmlns:wor=\"http://workflow.publishapplication.apimgt.carbon.wso2.org\">\n" +
                                 "         <wor:appName>$1</wor:appName>\n" +
                                 "         <wor:appVersion>$2</wor:appVersion>\n" +
                                 "         <wor:appProvider>$3</wor:appProvider>\n" +
                                 "         <wor:lcState>$4</wor:lcState>\n" +
                                 "         <wor:workflowExternalRef>$5</wor:workflowExternalRef>\n" +
                                 "         <wor:callBackURL>$6</wor:callBackURL>\n" +
                                 "      </wor:PublishAppApprovalWorkFlowProcessRequest>";

                PublishApplicationWorkflowDTO publishApplicationWorkflowDTO = (PublishApplicationWorkflowDTO) workflowDTO;
                String callBackURL = publishApplicationWorkflowDTO.getCallbackUrl();

                payload = payload.replace("$1", publishApplicationWorkflowDTO.getAppName());
                payload = payload.replace("$2", publishApplicationWorkflowDTO.getAppVersion());
                payload = payload.replace("$3", publishApplicationWorkflowDTO.getAppProvider());
                payload = payload.replace("$4", publishApplicationWorkflowDTO.getLcState());
                payload = payload.replace("$5", publishApplicationWorkflowDTO.getExternalWorkflowReference());
                payload = payload.replace("$6", callBackURL != null ? callBackURL : "?");

                client.fireAndForget(AXIOMUtil.stringToOM(payload));

                super.execute(workflowDTO);
            }
        } catch (AxisFault axisFault) {
            log.error("Error sending out message", axisFault);
            throw new WorkflowException("Error sending out message", axisFault);
        } catch (XMLStreamException e) {
            log.error("Error converting String to OMElement", e);
            throw new WorkflowException("Error converting String to OMElement", e);
        }
    }

    @Override
    public void complete(WorkflowDTO workflowDTO) throws WorkflowException {
        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workflowDTO);
        log.info("Publish API [Complete] Workflow Invoked. Workflow ID : " + workflowDTO.getExternalWorkflowReference() + "Workflow State : "+ workflowDTO.getStatus());

        AppMDAO dao = new AppMDAO();
        if(WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())){
            try {
                WorkflowDTO wfdto = dao.retrieveWorkflow(workflowDTO.getExternalWorkflowReference());
                String reference = wfdto.getWorkflowReference();
                String adminUserUsername = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration().getAdminUserName();

                String[] arr = decodeValues(reference);
                APIIdentifier apiIdentifier = new APIIdentifier(arr[2], arr[0], arr[1]);
                APIProvider provider = APIManagerFactory.getInstance().getAPIProvider(adminUserUsername);
                WebApp app = provider.getAPI(apiIdentifier);
                if (app != null) {
                    APIStatus newStatus = getApiStatus("published");
                    provider.changeAPIStatus(app, newStatus, adminUserUsername, true);
                }

                String apiPath = AppManagerUtil.getAPIPath(apiIdentifier);

                Registry registry = null;
                try {
                    //Need to fix for MT path
                    registry = RegistryCoreServiceComponent.getRegistryService().getGovernanceUserRegistry(arr[2], workflowDTO.getTenantId());

                } catch (RegistryException e) {
                    log.error("Error occured while retrieving registry", e);
                }

                GenericArtifactManager artifactManager = AppManagerUtil.getArtifactManager(registry, AppMConstants.API_KEY);
                try {
                    Resource apiResource = registry.get(apiPath);
                    String artifactId = apiResource.getUUID();
                    GenericArtifact webapp = artifactManager.getGenericArtifact(artifactId);
                    webapp.invokeAction("Approve");
                }catch (RegistryException e){
                    log.error("Error occured while retrieving registry artifact", e);
                }

            } catch (AppManagementException e) {
                log.error("Error while retrieving relevant workflow reference", e);
            } catch (UserStoreException e) {
                log.error("Error while trying to complete workflow. Retrieving admin user, username failed.", e);
            }
        } else if (WorkflowStatus.REJECTED.equals(workflowDTO.getStatus())){              //Web App rejection workflow
            try {
                WorkflowDTO wfdto = dao.retrieveWorkflow(workflowDTO.getExternalWorkflowReference());
                String reference = wfdto.getWorkflowReference();
                String adminUserUsername = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration().getAdminUserName();

                String[] arr = decodeValues(reference);
                APIIdentifier apiIdentifier = new APIIdentifier(arr[2], arr[0], arr[1]);
                APIProvider provider = APIManagerFactory.getInstance().getAPIProvider(adminUserUsername);
                WebApp app = provider.getAPI(apiIdentifier);
                if (app != null) {
                    APIStatus newStatus = getApiStatus("rejected");
                    provider.changeAPIStatus(app, newStatus, adminUserUsername, true);
                }

                String apiPath = AppManagerUtil.getAPIPath(apiIdentifier);

                Registry registry = null;
                try {
                    registry = RegistryCoreServiceComponent.getRegistryService().getGovernanceUserRegistry(arr[2], workflowDTO.getTenantId());

                } catch (RegistryException e) {
                    log.error("Error occured while retrieving registry", e);
                }

                GenericArtifactManager artifactManager = AppManagerUtil.getArtifactManager(registry, AppMConstants.API_KEY);
                try {
                    Resource apiResource = registry.get(apiPath);
                    String artifactId = apiResource.getUUID();
                    GenericArtifact webapp = artifactManager.getGenericArtifact(artifactId);
                    webapp.invokeAction("Reject");
                }catch (RegistryException e){
                    log.error("Error occured while retrieving registry artifact", e);
                }

            } catch (AppManagementException e) {
                log.error("Error while retrieving relevant workflow reference", e);
            } catch (UserStoreException e) {
                log.error("Error while trying to reject workflow. Retrieving admin user, username failed.", e);
            }

        }
    }

    private String[] decodeValues(String concatString){
         String[] values = concatString.split(":");
         return values;
    }

    private static APIStatus getApiStatus(String status) {
        APIStatus apiStatus = null;
        for (APIStatus aStatus : APIStatus.values()) {
            if (aStatus.getStatus().equalsIgnoreCase(status)) {
                apiStatus = aStatus;
            }

        }
        return apiStatus;
    }

    @Override
    public boolean isAsynchronus() {
        return false;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
