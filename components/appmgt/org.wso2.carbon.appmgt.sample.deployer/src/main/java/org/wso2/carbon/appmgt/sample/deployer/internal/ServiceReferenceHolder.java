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

package org.wso2.carbon.appmgt.sample.deployer.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.AppManagerConfigurationService;
import org.wso2.carbon.utils.ConfigurationContextService;
/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private ConfigurationContextService cfgCtxService;
    private AppManagerConfigurationService amConfigService;

    private ServiceReferenceHolder() {}

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return cfgCtxService;
    }

    public void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        this.cfgCtxService = cfgCtxService;
    }

    public AppManagerConfiguration getAPIManagerConfiguration() {
        return amConfigService.getAPIManagerConfiguration();
    }

    public void setAPIManagerConfigurationService(AppManagerConfigurationService amConfigService) {
        this.amConfigService = amConfigService;
    }
}

