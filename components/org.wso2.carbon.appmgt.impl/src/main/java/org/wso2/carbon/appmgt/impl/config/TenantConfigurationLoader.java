/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   you may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.appmgt.impl.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * This class loads the tenant configurations for a given tenant.
 */
public class TenantConfigurationLoader {

    private static final Log log = LogFactory.getLog(TenantConfigurationLoader.class);

    /**
     *
     * Loads and returns the configurations for the given tenant.
     *
     * @param tenantID
     * @return
     * @throws ConfigurationException
     */
    public TenantConfiguration load(int tenantID) throws ConfigurationException {

        try {
            // Read the config file from the registry
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
            Registry registry = registryService.getConfigSystemRegistry(tenantID);

            String tenantConfRegistryPath = AppMConstants.APPMGT_APPLICATION_DATA_LOCATION + "/" + AppMConstants.TENANT_CONF_FILENAME;

            TenantConfiguration tenantConfiguration = new TenantConfiguration(tenantID);
            tenantConfiguration.populate(tenantConfRegistryPath, registry);

            return tenantConfiguration;
        } catch (RegistryException e) {
            String errorMessage = "Can't load the properties from the tenant configuration for the tenant ID - ." + tenantID;
            log.error(errorMessage, e);
            throw new ConfigurationException(errorMessage, e);
        }
    }
}
