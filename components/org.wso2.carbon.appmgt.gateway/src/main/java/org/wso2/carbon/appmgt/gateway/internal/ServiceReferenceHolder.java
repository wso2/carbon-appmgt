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

package org.wso2.carbon.appmgt.gateway.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.AppManagerConfigurationService;
import org.wso2.carbon.appmgt.gateway.token.TokenGenerator;
import org.wso2.carbon.appmgt.impl.service.TenantConfigurationService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.sequences.services.SequenceAdminService;

public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private ConfigurationContextService cfgCtxService;
    private AppManagerConfigurationService amConfigService;
    private TokenGenerator tokenGenerator;
    private SequenceAdminService sequenceAdminService;
    private TenantConfigurationService tenantConfigurationService;

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        this.cfgCtxService = cfgCtxService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return cfgCtxService;
    }

    public ConfigurationContext getServerConfigurationContext() {
        return cfgCtxService.getServerConfigContext();
    }

    public AppManagerConfiguration getAPIManagerConfiguration() {
        return amConfigService.getAPIManagerConfiguration();
    }

    public void setAPIManagerConfigurationService(AppManagerConfigurationService amConfigService) {
        this.amConfigService = amConfigService;
    }

    public TokenGenerator getTokenGenerator() {
        return tokenGenerator;
    }

    public void setTokenGenerator(TokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    public SequenceAdminService getSequenceAdminService() {
        return sequenceAdminService;
    }

    public void setSequenceAdminService(SequenceAdminService sequenceAdminService) {
        this.sequenceAdminService = sequenceAdminService;
    }

    public void setTenantConfigurationService(TenantConfigurationService tenantConfigurationService) {
        this.tenantConfigurationService = tenantConfigurationService;
    }

    public TenantConfigurationService getTenantConfigurationService() {
        return tenantConfigurationService;
    }
}
