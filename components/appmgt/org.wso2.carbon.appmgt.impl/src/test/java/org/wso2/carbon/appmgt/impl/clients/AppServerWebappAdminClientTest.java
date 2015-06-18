/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl.clients;


import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappsWrapper;

import java.util.Locale;

import static junit.framework.Assert.*;

/**
 * Tests the webapp application client
 */
public class AppServerWebappAdminClientTest {

    public void testConstructor() throws Exception {
        AppServerWebappAdminClient appServerWebappAdminClient = getAppServerWebappAdminClient();

    }

    public void testLogin() throws Exception {
        AppServerWebappAdminClient appServerWebappAdminClient = getAppServerWebappAdminClient();

        appServerWebappAdminClient.login("admin", "admin");
    }

    public void testLogout() throws Exception {
        AppServerWebappAdminClient appServerWebappAdminClient = getAppServerWebappAdminClient();

        try {
            appServerWebappAdminClient.logout();
            assertTrue("The logout on unauthenticatied client should fail", false);
        } catch (AppManagementException e) {
            assertTrue(true);
        }
        
        appServerWebappAdminClient.login("admin", "admin");

        appServerWebappAdminClient.logout();
    }


    public void testGetPagedWebappsSummary() throws AppManagementException, AxisFault {
        AppServerWebappAdminClient appServerWebappAdminClient = getAppServerWebappAdminClient();

        appServerWebappAdminClient.login("admin", "admin");
        WebappsWrapper webappsWrapper = appServerWebappAdminClient.getPagedWebappsSummary("", "all", "all", 0);

        assertNotNull(webappsWrapper);
        assertNotNull(webappsWrapper.getWebapps());
    }

    private AppServerWebappAdminClient getAppServerWebappAdminClient()
            throws AxisFault, AppManagementException {
        System.setProperty("javax.net.ssl.trustStore", "product-app-manager/modules/integration/tests-ui/src/test/resources/keystores/products/wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        return new AppServerWebappAdminClient("https://localhost:9545/services/", null,
                Locale.ENGLISH);
    }
}