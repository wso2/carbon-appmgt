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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for application discovery service.
 *
 */
public class ApplicationDiscoveryServiceFactory {
    private Map<String, ApplicationDiscoveryHandler> applicationDiscoveryHandlerMap =
            new HashMap<String, ApplicationDiscoveryHandler>();


    public void addHandler(String name, ApplicationDiscoveryHandler applicationDiscoveryHandler) {
        applicationDiscoveryHandlerMap.put(name, applicationDiscoveryHandler);
    }

    /**
     * Returns a list of available discovery handlers.
     *
     * @return
     */
    public List<String> getAvailableHandlerNames() {
        return new ArrayList<String>(applicationDiscoveryHandlerMap.keySet());
    }

    /**
     * Returns the application discovery handler given the handler name.
     * @param name
     * @return
     */
    public ApplicationDiscoveryHandler getHandler(String name) {
        return applicationDiscoveryHandlerMap.get(name);
    }
}
