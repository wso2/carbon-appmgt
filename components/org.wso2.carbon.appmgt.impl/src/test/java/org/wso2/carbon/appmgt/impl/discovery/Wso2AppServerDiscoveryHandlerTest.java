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

package org.wso2.carbon.appmgt.impl.discovery;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.AppManagerConfigurationServiceImpl;
import org.wso2.carbon.appmgt.impl.dao.test.TestRealmService;
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationListDTO;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;

import java.util.Locale;

import static org.testng.Assert.*;

/**
 * Tests the WSO2 App server discovery handler
 */
public class Wso2AppServerDiscoveryHandlerTest {

    private Wso2AppServerDiscoveryHandler handler;

    @BeforeClass
    public void setup() {
        handler = new Wso2AppServerDiscoveryHandler();
        AppManagerConfiguration appManagerConfiguration = new AppManagerConfiguration();
        AppManagerConfigurationServiceImpl appManagerConfigurationService = new AppManagerConfigurationServiceImpl(appManagerConfiguration);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(appManagerConfigurationService);
        TestRealmService testRealmService = new org.wso2.carbon.appmgt.impl.dao.test.TestRealmService();

        ServiceReferenceHolder.getInstance().setRealmService(testRealmService);
        System.setProperty("carbon.home", "/usr/local/wso2appm/wso2appm");
    }

    @Test
    public void testGetDisplayName() throws Exception {
        assertEquals("WSO2-AS", handler.getDisplayName());
    }

    @Test(enabled = false)
    public void testDiscoverApplications() throws Exception {
        UserNamePasswordCredentials credentials = new UserNamePasswordCredentials();
        credentials.setAppServerUrl("http://localhost:9763/services/");
        credentials.setAppServerUrl("http://localhost:8080/MockWebappAdmin/services/");
        credentials.setUserName("reader");
        credentials.setPassword("reader");
        credentials.setLoggedInUsername("admin");

        ConfigurationContext configurationContext =  ConfigurationContextFactory
                .createConfigurationContextFromFileSystem((String) null, (String) null);
        DiscoverySearchCriteria criteria = new DiscoverySearchCriteria();
        ApplicationDiscoveryContext discoveryContext = new ApplicationDiscoveryContext();
        DiscoveredApplicationListDTO applicationListDTO = handler.discoverApplications(
                discoveryContext, credentials, criteria, Locale.ENGLISH, configurationContext);

        assertNotNull(applicationListDTO);

        assertNotNull(applicationListDTO.getApplicationList());

    }

    @Test
    public void testGenerateProxyContext() throws Exception {

    }

}