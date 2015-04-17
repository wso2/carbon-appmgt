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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
    }

    @Test
    public void testGetDisplayName() throws Exception {
        assertEquals("WSO2-AS", handler.getDisplayName());
    }

    @Test
    public void testDiscoverApplications() throws Exception {
        UserNamePasswordCredentials credentials = new UserNamePasswordCredentials();
        credentials.setAppServerUrl("https://localhost:9445/services/");
        credentials.setUserName("reader");
        credentials.setPassword("reader");
        credentials.setLoggedInUsername("admin");

        DiscoverySearchCriteria criteria = new DiscoverySearchCriteria();
        DiscoveredApplicationListDTO applicationListDTO = handler.discoverApplications(credentials, criteria, Locale.ENGLISH);

        assertNotNull(applicationListDTO);

        assertNotNull(applicationListDTO.getApplicationList());

    }

    @Test
    public void testGenerateProxyContext() throws Exception {

    }
}