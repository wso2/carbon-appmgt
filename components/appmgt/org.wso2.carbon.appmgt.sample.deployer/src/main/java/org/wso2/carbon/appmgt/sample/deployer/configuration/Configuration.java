package org.wso2.carbon.appmgt.sample.deployer.configuration;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.sample.deployer.internal.ServiceReferenceHolder;
import org.wso2.carbon.utils.CarbonUtils;
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
public class Configuration {
    private static AppManagerConfiguration config;
    private static ConfigurationContextService configContextService = null;

    static {
        try {
            config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
            configContextService = ServiceReferenceHolder.getInstance().getConfigurationContextService();
        } catch (Exception ex) {

        }
    }

    public static String getUserName() {
        return config.getFirstProperty("SSOConfiguration.Configurators.Configurator.parameters.username");
    }

    public static String getPassword() {
        return config.getFirstProperty("SSOConfiguration.Configurators.Configurator.parameters.password");
    }

    public static String getHttpsUrl() {
        try {
            return "https://localhost:" + getBackendPort("https");
        } catch (AppManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getHttpUrl() {
        try {
            return "http://localhost:" + getBackendPort("http");
        } catch (AppManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ConfigurationContext getConfigContext() throws AppManagementException {
        if (configContextService == null) {
            throw new AppManagementException("ConfigurationContextService is null");
        }
        return configContextService.getServerConfigContext();
    }

    public static String getBackendPort(String transport) throws AppManagementException {
        int port;
        String backendPort;

        port = CarbonUtils.getTransportProxyPort(getConfigContext(), transport);
        if (port == -1) {
            port = CarbonUtils.getTransportPort(getConfigContext(), transport);
        }
        backendPort = Integer.toString(port);
        return backendPort;
    }
}
