/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appmgt.impl.observers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.api.model.APIStatus;

public class SimpleLoggingObserver implements APIStatusObserver {

    private static final Log log = LogFactory.getLog(SimpleLoggingObserver.class);

    @Override
    public boolean statusChanged(APIStatus previous, APIStatus current, WebApp api) {
        log.info("WebApp status updated from: " + previous.getStatus() + " to: " +
                current.getStatus() + " for the WebApp: " + api.getId().getApiName() + " (" +
                api.getId().getVersion() + ")");
        return true;
    }
}
