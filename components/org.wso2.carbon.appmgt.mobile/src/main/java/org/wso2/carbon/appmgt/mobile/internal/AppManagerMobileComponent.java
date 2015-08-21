/*
 *
 *   Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.appmgt.mobile.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appmgt.mobile.interfaces.MDMOperations;
import org.wso2.carbon.appmgt.mobile.mdm.MDMServiceReferenceHolder;
import org.wso2.carbon.appmgt.mobile.utils.MobileConfigurations;

/**
 * @scr.component name="org.wso2.carbon.appmgt.mobile" immediate="true"
 * @scr.reference name="mdm.service"
 * interface="org.wso2.carbon.appmgt.mobile.interfaces.MDMOperations" cardinality="1..n"
 * policy="dynamic" bind="setMDMService" unbind="unsetMDMService"
 */
public class AppManagerMobileComponent {

    private static final Log log = LogFactory.getLog(AppManagerMobileComponent.class);
    private static final String MDM_OPERATIONS_CLASS = "MDMOperationsImpl";

    protected void activate(ComponentContext context) {
        log.info("App Manger Mobile Component activated");
    }

    protected void deactivate(ComponentContext context) {
        log.info("App Manger Mobile Component deactivated");
    }

    protected void setMDMService(MDMOperations operations) {
        if((MobileConfigurations.getInstance().getActiveMDMBundle() + "." + MDM_OPERATIONS_CLASS).equals(operations.getClass().getName())){
            MDMServiceReferenceHolder.getInstance().setMDMOperation(operations);
            log.info(MobileConfigurations.getInstance().getMDMConfigs().get(MobileConfigurations.ACTIVE_MDM) + " MDM is bound to App Manager");
        }

    }

    protected void unsetMDMService(MDMOperations operations) {
        log.info("App Manger MDM is unbound");
    }

}
