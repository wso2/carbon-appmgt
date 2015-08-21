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

import org.wso2.carbon.appmgt.api.EntitlementService;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;

/**
 * Factory class to returns the relevant entitlement service implementation based on application configuration.
 */
public class EntitlementServiceFactory {

    private static final String SERVER_URL = "EntitlementServiceConfiguration.Parameters.ServerUrl";
    private static final String USERNAME = "EntitlementServiceConfiguration.Parameters.Username";
    private static final String PASSWORD = "EntitlementServiceConfiguration.Parameters.Password";

    /**
     * NOTE : Only XACML entitlement service is supported as of now.
     * @param configuration
     * @return
     */
    public static EntitlementService getEntitlementService(AppManagerConfiguration configuration){

        String serverUrl = configuration.getFirstProperty(SERVER_URL);
        String username = configuration.getFirstProperty(USERNAME);
        String password = configuration.getFirstProperty(PASSWORD);

        EntitlementService entitlementService =  new XacmlEntitlementServiceImpl(serverUrl, username, password);
        entitlementService.init();

        return entitlementService;
    }


}
