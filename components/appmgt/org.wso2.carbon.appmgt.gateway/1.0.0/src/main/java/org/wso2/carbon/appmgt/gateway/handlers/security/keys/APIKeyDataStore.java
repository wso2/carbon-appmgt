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

package org.wso2.carbon.appmgt.gateway.handlers.security.keys;

import org.wso2.carbon.appmgt.api.model.AuthenticatedIDP;
import org.wso2.carbon.appmgt.api.model.URITemplate;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.appmgt.impl.dto.APIKeyValidationInfoDTO;

import java.util.ArrayList;

/**
 * Represents the interface used by the APIKeyValidator to interact with the WebApp
 * key manager. Different implementations of this interface may employ different
 * techniques/protocols to communicate with the actual key management server.
 */
public interface APIKeyDataStore {

    /**
     * Validate the given WebApp key for the specified WebApp context and version.
     *
     * @param context Context of an WebApp
     * @param apiVersion A valid version of the WebApp
     * @param apiKey An WebApp key string - Not necessarily a valid key
     * @return an APIKeyValidationInfoDTO instance containing key validation data
     * @throws org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException on error
     */
    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion,
                                                 String apiKey, String clientDomain) throws APISecurityException;

    /**
     * Validate the given WebApp key for the specified WebApp context and version.
     *
     * @param context Context of an WebApp
     * @param apiVersion A valid version of the WebApp
     * @param apiKey An WebApp key string - Not necessarily a valid key
     * @param requiredAuthenticationLevel - type of  key. can be one of 'Application or Application_User'
     * @return an APIKeyValidationInfoDTO instance containing key validation data
     * @throws org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException on error
     */
    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion,
                                                 String apiKey, String requiredAuthenticationLevel, String clientDomain) throws APISecurityException;

    /**
     * Get WebApp Resource URI Templates
     *
     * @param context Context of an WebApp
     * @param apiVersion A valid version of the WebApp
     * @return an APIKeyValidationInfoDTO instance containing key validation data
     * @throws org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException on error
     */
    public ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion
    ) throws APISecurityException;


    /**
     * Clean up any resources allocated to this WebApp key data store instance.
     */
    public void cleanup();

	/**
	 * Get application data for the context and apiversion
	 * 
	 * @param apiContext
	 * @param apiVersion
	 * @param consumer 
	 */
	public APIKeyValidationInfoDTO getAPPData(String apiContext, String apiVersion, String consumer, AuthenticatedIDP[] authenticatedIdp) throws APISecurityException;

}
