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

package org.wso2.carbon.appmgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.dao.AppMSubscriptionExtensionDAO;
import org.wso2.carbon.appmgt.impl.dto.SubscriptionExpiryDTO;
import org.wso2.carbon.appmgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.appmgt.impl.dto.WorkflowDTO;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SubscriptionCreationExpiryWorkflowExecutor extends SubscriptionCreationSimpleWorkflowExecutor {

    private static final Log log = LogFactory.getLog(SubscriptionCreationExpiryWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }

    @Override
    public void execute(WorkflowDTO workflowDTO) throws WorkflowException {
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        SubscriptionExpiryDTO subscriptionExpiryDTO = new SubscriptionExpiryDTO();
        SubscriptionWorkflowDTO subscriptionWorkflowDTO = null;
        if (workflowDTO instanceof SubscriptionWorkflowDTO) {
            subscriptionWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        } else {
            log.error("Error in casting.....");
        }
        try {
            String subscriptionType = "";
            Date subscriptionDate = new Date();
            int evaluationPeriod = 24 * 7;

            Calendar cal = Calendar.getInstance();
            cal.setTime(subscriptionDate);
            cal.add(Calendar.HOUR, evaluationPeriod);
            Date expireOn = cal.getTime();

            subscriptionExpiryDTO.setApiProvider(subscriptionWorkflowDTO.getApiProvider());
            subscriptionExpiryDTO.setApiName(subscriptionWorkflowDTO.getApiName());
            subscriptionExpiryDTO.setApiVersion(subscriptionWorkflowDTO.getApiVersion());
            subscriptionExpiryDTO.setSubscriber(subscriptionWorkflowDTO.getSubscriber());
            subscriptionExpiryDTO.setSubscriptionType(subscriptionType);
            subscriptionExpiryDTO.setSubscriptionTime(subscriptionDate);
            subscriptionExpiryDTO.setEvaluationPeriod(evaluationPeriod);
            subscriptionExpiryDTO.setExpireOn(expireOn);
            subscriptionExpiryDTO.setWorkflowReference(subscriptionWorkflowDTO.getWorkflowReference());

            complete(subscriptionExpiryDTO);
        } catch (Exception e) {
            log.error("Could not complete subscription creation workflow", e);
            throw new WorkflowException("Could not complete subscription creation workflow", e);
        }
    }

    @Override
    public void complete(WorkflowDTO workflowDTO) throws WorkflowException {
        super.complete(workflowDTO);
        AppMSubscriptionExtensionDAO appMSubscriptionExtentionDAO = new AppMSubscriptionExtensionDAO();
        try {
            appMSubscriptionExtentionDAO.addSubscription(workflowDTO);
        } catch (AppManagementException e) {
            log.error("Could not complete subscription creation workflow", e);
            throw new WorkflowException("Could not complete subscription creation workflow", e);
        }
    }
}
