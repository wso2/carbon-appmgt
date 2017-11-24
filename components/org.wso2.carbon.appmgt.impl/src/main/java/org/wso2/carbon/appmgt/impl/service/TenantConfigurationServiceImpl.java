/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl.service;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.config.TenantConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The registry based implementation of TenantConfigurationService.
 */
public class TenantConfigurationServiceImpl implements TenantConfigurationService{

    private static final Log log = LogFactory.getLog(TenantConfigurationServiceImpl.class);

    private Map<Integer, TenantConfiguration> tenantConfigurations;
    public TenantConfigurationServiceImpl(){
        tenantConfigurations = new HashMap<Integer, TenantConfiguration>();
    }

    public void addTenantConfiguration(TenantConfiguration tenantConfiguration){

        synchronized (this){
            if(tenantConfigurations.get(tenantConfiguration.getTenantID()) == null){
                tenantConfigurations.put(tenantConfiguration.getTenantID(), tenantConfiguration);
            }
        }
    }

    @Override
    public String getFirstProperty(String key) {
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return getFirstProperty(key, tenantID);
    }

    @Override
    public String getFirstProperty(String key, int tenantID) {
        return tenantConfigurations.get(tenantID).getFirstProperty(key);
    }

    @Override
    public List<String> getProperties(String key) {
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return tenantConfigurations.get(tenantID).getProperties(key);
    }

    @Override
    public List<String> getProperties(String key, int tenantID) {
        return tenantConfigurations.get(tenantID).getProperties(key);
    }
}
