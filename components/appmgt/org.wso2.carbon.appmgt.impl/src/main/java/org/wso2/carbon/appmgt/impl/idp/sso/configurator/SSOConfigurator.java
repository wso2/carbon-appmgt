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

package org.wso2.carbon.appmgt.impl.idp.sso.configurator;

import org.wso2.carbon.appmgt.api.model.SSOProvider;
import org.wso2.carbon.appmgt.api.model.WebApp;

import java.util.Map;

public interface SSOConfigurator {

    /**
     * Initialise method for configurators. This method gets passed a map containing key/value pairs that come from
     * <parameters> tag in SSO Configurator configuration.
     * @param configuration
     */
    public void init(Map<String, String> configuration);

    /**
     * Used to create a SSO provider in 3rd party IDP provider.
     * @param provider
     * @return true/false - successful execution
     */
    public boolean createProvider(SSOProvider provider);

    /**
     * Used to create a SSO provider in 3rd party IDP provider.
     *
     * @param application
     * @return true/false - successful execution
     */
    public boolean createProvider(WebApp application);

    /**
     * Used to update an already existing SSO provider in 3rd party IDP provider.
     * @param provider
     * @return true/false - successful execution
     */
    public boolean updateProvider(SSOProvider provider);

    /**
     * Used to update an already existing SSO provider in 3rd party IDP provider.
     *
     * @param application
     * @return true/false - successful execution
     */
    public boolean updateProvider(WebApp application);

    /**
     * Used to remove an existing SSO Provider in 3rd party IDP.
     * @param provider
     * @return true/false - successful execution
     */
    public boolean removeProvider(SSOProvider provider);

    /**
     * Used to retrieve a list of claims provided by the 3rd party IDP provider.
     * @return Array of claims.
     */
    public String[] getAllClaims();

    /**
     * Used to query 3rd Party IDP to check if IDP provider is accessible.
     * @return true/false - availability of 3rd party IDP
     */
    public boolean isAvailable() throws Exception;


    /**
     * Returns SSOProvider object for given issuer name by querying 3rd party SSO Provider.
     * @param issuerName
     * @return SSOProvider object
     */
    public SSOProvider getProvider(String issuerName);

    /**
     * Returns the IDPs for the given service provider.
     * @param serviceProviderId
     * @return The list of IDPs.
     */
    public String[] getIdentityProvidersInServiceProvider(String serviceProviderId);
}