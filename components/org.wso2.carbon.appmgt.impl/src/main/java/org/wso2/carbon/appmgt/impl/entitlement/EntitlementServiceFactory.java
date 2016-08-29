/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl.entitlement;

import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.EntitlementService;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;

/**
 * Factory class to returns the relevant entitlement service implementation based on application configuration.
 */
public class EntitlementServiceFactory {

    private static final String SERVER_URL = "EntitlementServiceConfiguration.Parameters.ServerUrl";

    /**
     * NOTE : Only XACML entitlement service is supported as of now.
     * @param configuration
     * @param authorizedAdminCookie Authorized cookie to access IDP admin services
     * @return
     * @throws AppManagementException
     */
    public static EntitlementService getEntitlementService(AppManagerConfiguration configuration,
                                                           String authorizedAdminCookie)
            throws AppManagementException {
        String serverUrl = configuration.getFirstProperty(SERVER_URL);
        EntitlementService entitlementService = new XacmlEntitlementServiceImpl(serverUrl, authorizedAdminCookie);
        entitlementService.init();
        return entitlementService;
    }

    /**
     * NOTE : Only XACML entitlement service is supported as of now.
     *
     * @param configuration
     * @return
     * @throws AppManagementException
     */
    public static EntitlementService getEntitlementService(AppManagerConfiguration configuration)
            throws AppManagementException {
        String serverUrl = configuration.getFirstProperty(SERVER_URL);
        EntitlementService entitlementService = new XacmlEntitlementServiceImpl(serverUrl);
        entitlementService.init();
        return entitlementService;
    }



}
