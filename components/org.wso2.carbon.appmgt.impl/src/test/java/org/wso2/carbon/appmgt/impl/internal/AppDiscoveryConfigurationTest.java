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

package org.wso2.carbon.appmgt.impl.internal;

import org.testng.annotations.Test;
import org.wso2.carbon.appmgt.api.AppManagementException;

import static org.testng.Assert.*;

/**
 * Tests the AppDiscoveryConfiguration
 */
public class AppDiscoveryConfigurationTest {

    @Test
    public void testGetHandlersMap() throws Exception {
        AppDiscoveryConfiguration appDiscoveryConfiguration = new AppDiscoveryConfiguration();
        appDiscoveryConfiguration.load(this.getClass().getResourceAsStream("AppDiscoveryConfiguration1.xml"));

        assertNotNull(appDiscoveryConfiguration.getHandlersMap());
        assertEquals(appDiscoveryConfiguration.getHandlersMap().size(), 2);
        assertNotNull(appDiscoveryConfiguration.getHandlersMap().get("name1"));
        assertEquals("class2", appDiscoveryConfiguration.getHandlersMap().get("name2"));
    }

    @Test
    public void testGetHandlersMap_Empty() throws Exception {
        AppDiscoveryConfiguration appDiscoveryConfiguration = new AppDiscoveryConfiguration();
        appDiscoveryConfiguration.load(this.getClass().getResourceAsStream("AppDiscoveryConfiguration2.xml"));

        assertNotNull(appDiscoveryConfiguration.getHandlersMap());
        assertEquals(appDiscoveryConfiguration.getHandlersMap().size(), 0);
    }

    @Test
    public void testGetHandlersMap_Malformed() throws Exception {
        AppDiscoveryConfiguration appDiscoveryConfiguration = new AppDiscoveryConfiguration();
        try {
            appDiscoveryConfiguration.load(this.getClass()
                    .getResourceAsStream("AppDiscoveryConfiguration_Malformed.xml"));
            fail("There should be an exception when xml is malformed");
        } catch (AppManagementException e) {
            //This is OK
            assertTrue(true);
        }

    }

    @Test
    public void testGetHandlersMap_WrongAttributes() throws Exception {
        AppDiscoveryConfiguration appDiscoveryConfiguration = new AppDiscoveryConfiguration();
        appDiscoveryConfiguration.load(this.getClass().getResourceAsStream("AppDiscoveryConfiguration_WrongAttributes.xml"));

        assertNotNull(appDiscoveryConfiguration.getHandlersMap());
        assertEquals( appDiscoveryConfiguration.getHandlersMap().size(), 0);
    }

    @Test
    public void testLoadInputStream() throws Exception {
        AppDiscoveryConfiguration appDiscoveryConfiguration = new AppDiscoveryConfiguration();
        appDiscoveryConfiguration.load(this.getClass().getResourceAsStream("AppDiscoveryConfiguration1.xml"));
    }

    @Test
    public void testLoadFile() throws Exception {

    }
}