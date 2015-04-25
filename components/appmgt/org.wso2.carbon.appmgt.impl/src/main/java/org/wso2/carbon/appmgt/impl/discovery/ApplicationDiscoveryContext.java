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

import java.util.HashMap;
import java.util.Map;

/**
 * Context used for subsequent calls to Discovery Handlers by each service call
 * Holds the state information for state-less services
 *
 */
public class ApplicationDiscoveryContext {

    private final Map<String, Object> contextData = new HashMap<String, Object>();

    /**
     * Put a data given key. similar to ServletSession.setAttribute() .
     * @param key
     * @param value
     */
    public void putData(String key, Object value) {
        contextData.put(key, value);
    }

    /**
     * Retrieves data from the context. similar to ServletSession.getAttribute()
     * @param key
     * @return
     */
    public Object getData(String key) {
        return contextData.get(key);
    }

    /**
     * Clears the data given the key. This is a convenient method to putData(key, null)
     * @param key
     */
    public void clear(String key) {
        contextData.remove(key);
    }
}
