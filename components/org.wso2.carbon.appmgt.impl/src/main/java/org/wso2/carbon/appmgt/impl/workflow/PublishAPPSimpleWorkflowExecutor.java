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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.APIStatus;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.APIManagerFactory;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.dto.PublishApplicationWorkflowDTO;
import org.wso2.carbon.appmgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.context.CarbonContext;

import java.util.List;

public class PublishAPPSimpleWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(PublishAPPSimpleWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException{
        return null;
    }

    @Override
    public void execute(WorkflowDTO workflowDTO) throws WorkflowException{
        //Update Webapp state to IN-Review till the workflow is approved
        PublishApplicationWorkflowDTO publishAPPDTO = (PublishApplicationWorkflowDTO)workflowDTO;
        try  {
            String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String fullName;
            if(!tenantDomain.equalsIgnoreCase("carbon.super")) {
                fullName = loggedInUser + "@" + tenantDomain;
            }else{
                fullName = loggedInUser;
            }
            APIProvider provider = APIManagerFactory.getInstance().getAPIProvider(fullName);
            APIIdentifier apiId = new APIIdentifier(publishAPPDTO.getAppProvider(), publishAPPDTO.getAppName(), publishAPPDTO.getAppVersion());
            WebApp api = provider.getAPI(apiId);
            APIStatus newStatus = null;
            if (api != null) {
                newStatus = getApiStatus(publishAPPDTO.getNewState());
                provider.changeAPIStatus(api, newStatus, fullName, true);
            }
            if(publishAPPDTO.getLcState().equalsIgnoreCase(AppMConstants.ApplicationStatus.APPLICATION_CREATED)) {
                super.execute(workflowDTO);
            }
        }catch (AppManagementException e){
            log.error("Could not update APP lifecycle state to IN-REVIEW", e);
            throw new WorkflowException("Could not update APP lifecycle state to IN-REVIEW", e);
        }
    }

    @Override
    public void complete(WorkflowDTO workflowDTO) throws WorkflowException{
        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workflowDTO);
        try {
            String reference = workflowDTO.getWorkflowReference();
            String[] arr = decodeValues(reference);
            APIIdentifier apiIdentifier = null;
            String apiName = arr[0];
            String version = arr[1];
            String uId = arr[2];
            String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String fullName;
//            if(!tenantDomain.equalsIgnoreCase("carbon.super")) {
//                fullName = loggedInUser + "@" + tenantDomain;
//            }else{
//                fullName = loggedInUser;
//            }
            fullName = loggedInUser;
            //make Provider Name (Secondary User Store) registry friendly by replacing '/' with ':'
            uId = AppManagerUtil.makeSecondaryUSNameRegFriendly(uId);

            apiIdentifier = new APIIdentifier(uId, apiName, version);
            APIProvider provider = APIManagerFactory.getInstance().getAPIProvider(fullName);
            WebApp app = provider.getAPI(apiIdentifier);
            PublishApplicationWorkflowDTO publishAPPDTO = (PublishApplicationWorkflowDTO)workflowDTO;

            if (app != null) {
                APIStatus newStatus = getApiStatus(publishAPPDTO.getNewState());
                provider.changeAPIStatus(app, newStatus, fullName, true);
            }
        } catch (AppManagementException e) {
            //throw exception
            log.error("Error while publishing API",e);
        }
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

    private String[] decodeValues(String concatString){
        String[] values = concatString.split(":");
        return values;
    }
}
