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

package org.wso2.carbon.appmgt.impl.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

;

/**
 * Represents the set of configurations (properties) for a tenant.
 */
public class TenantConfiguration {

    private static final Log log = LogFactory.getLog(TenantConfiguration.class);

    private int tenantID;
    private Map<String, List<String>> properties;

    public TenantConfiguration(int tenantID){
        this.tenantID = tenantID;
        properties = new HashMap<String, List<String>>();
    }

    /**
     *
     * Populates the properties using the configuration file in the given registry location.
     *
     * @param configResourcePath
     * @param registry
     */
    public void populate(String configResourcePath, Registry registry){

        try{

            if(registry.resourceExists(configResourcePath)) {
                Resource configurationsResource = registry.get(configResourcePath);

                StAXOMBuilder builder = new StAXOMBuilder(configurationsResource.getContentStream());
                OMElement root = builder.getDocumentElement();
                populate(root);
            }
        } catch (XMLStreamException e) {
            String errorMessage = "Can't load the tenant configuration for the tenant " + tenantID;
            log.error(errorMessage, e);
        } catch (RegistryException e) {
            String errorMessage = "Can't load the tenant configuration for the tenant " + tenantID;
            log.error(errorMessage, e);
        }
    }

    /**
     * Adds the given property under the given property name.
     * @param key
     * @param value
     */
    public void addProperty(String key, String value) {

        List<String> valueHolder = (List<String>) properties.get(key);

        if(valueHolder == null){
            valueHolder = new ArrayList<String>();
            properties.put(key, valueHolder);
        }

        valueHolder.add(value);

    }

    /**
     * Returns the property for the given property name
     * @param key
     * @return
     */
    public String getFirstProperty(String key) {

        List<String> propertyHolder = properties.get(key);

        if(propertyHolder != null && propertyHolder.size() > 0){
            return propertyHolder.get(0);
        }else{
            return null;
        }
    }

    public String getFirstPropertyAsString(String key){
        return (String) getFirstProperty(key);
    }

    public List<String> getProperties(String key){
        return properties.get(key);
    }

    /**
     * Returns the tenant ID which the configurations belong to.
     * @return
     */
    public int getTenantID() {
        return tenantID;
    }

    private void populate(OMElement configRoot) {

        Iterator<OMElement> rootLevelChildren = configRoot.getChildElements();
        while(rootLevelChildren.hasNext()){
            addProperties(rootLevelChildren.next(), null);
        }
    }

    private void addProperties(OMElement node, Object cumulativePropertyKey) {

        Iterator<OMElement> children = node.getChildElements();

        if(!children.hasNext()){

            String key = node.getLocalName();

            if(cumulativePropertyKey != null){
                key = cumulativePropertyKey + "." + key;
            }

            addProperty(key, node.getText());
        }else{

            String newCumulativePropertyKey = node.getLocalName();

            if(cumulativePropertyKey != null){
                newCumulativePropertyKey = cumulativePropertyKey + "." + newCumulativePropertyKey;
            }

            while(children.hasNext()){
                addProperties(children.next(), newCumulativePropertyKey);
            }
        }
    }

    // Flattened property names of the tenant specific configurations.
    public static class PropertyNames{

        public static final String IS_SELF_SUBSCRIPTION_ENABLED = "Subscriptions.EnableSelfSubscription";
        public static final String IS_ENTERPRISE_SUBSCRIPTION_ENABLED = "Subscriptions.EnableEnterpriseSubscription";
        public static final String ENABLED_ASSET_TYPES = "EnabledAssetTypes.Type";
    }
}
