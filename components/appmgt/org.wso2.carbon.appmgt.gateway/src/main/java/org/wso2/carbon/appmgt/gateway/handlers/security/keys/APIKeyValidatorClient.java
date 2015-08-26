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

import org.wso2.carbon.appmgt.api.model.URITemplate;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.appmgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.dto.APIKeyValidationInfoDTO;

import java.util.ArrayList;

/**
 * @Deprecated API Key validation is a legacy code taken from API Manager. Need to remove this
 */
@Deprecated
public class APIKeyValidatorClient {

    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

    private String username;
    private String password;

    public APIKeyValidatorClient() throws APISecurityException {
        AppManagerConfiguration config = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfiguration();
        String serviceURL = config.getFirstProperty(AppMConstants.API_KEY_MANAGER_URL);
        username = config.getFirstProperty(AppMConstants.API_KEY_MANAGER_USERNAME);
        password = config.getFirstProperty(AppMConstants.API_KEY_MANAGER_PASSWORD);
        if (serviceURL == null || username == null || password == null) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Required connection details for the key management server not provided");
        }
    }

    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion, String apiKey,
            String requiredAuthenticationLevel, String clientDomain) throws APISecurityException {

        throw new APISecurityException(403, "API Key validation is not supposed to be used.");
    }

    public ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion)
            throws APISecurityException {

        throw new APISecurityException(403, "API Key validation is not supposed to be used.");
    }
}
