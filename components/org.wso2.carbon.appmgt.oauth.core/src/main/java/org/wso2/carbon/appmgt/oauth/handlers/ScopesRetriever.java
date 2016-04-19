/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appmgt.oauth.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.HashMap;
import java.util.Map;

/**
 * Retrievers the scope role mapping configurations.
 */
public class ScopesRetriever {

    private static final Log log = LogFactory.getLog(ScopesRetriever.class);

    public static Map<String, String> getScopeRoleMapping(String tenantDomain){

        Map<String, String> scopeRoleMapping = new HashMap<String, String>();

        try {
            String tenantConfigContent = AppManagerUtil.getConfigRegistryResourceContent(tenantDomain, AppMConstants.TENANT_CONF_PATH);

            if(tenantConfigContent!= null) {
                JSONParser parser = new JSONParser();

                JSONObject parsedTenantConfig = (JSONObject) parser.parse(tenantConfigContent);

                if (parsedTenantConfig != null) {

                    JSONObject scopesConfig = (JSONObject) parsedTenantConfig.get(AppMConstants.REST_API_SCOPES_CONFIG);

                    if (scopesConfig != null) {
                        JSONArray scopeMappings = (JSONArray) scopesConfig.get("Scope");

                        String scopeName = null;
                        String mappedRoles = null;

                        for(Object scopeMapping : scopeMappings){
                            scopeName = (String) ((JSONObject)scopeMapping).get("Name");
                            mappedRoles = (String) ((JSONObject)scopeMapping).get("Roles");
                            scopeRoleMapping.put(scopeName, mappedRoles);
                        }

                    }
                }
            }
        } catch (UserStoreException e) {
            log.error(String.format("Can't get the scope -> role mappings from '%s'", AppMConstants.TENANT_CONF_PATH));
        } catch (RegistryException e) {
            log.error(String.format("Can't get the scope -> role mappings from '%s'", AppMConstants.TENANT_CONF_PATH));
        } catch (ParseException e) {
            log.error(String.format("Can't get the scope -> role mappings from '%s'", AppMConstants.TENANT_CONF_PATH));
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error(String.format("Can't get the scope -> role mappings from '%s'", AppMConstants.TENANT_CONF_PATH));
        }

        return scopeRoleMapping;

    }


}
