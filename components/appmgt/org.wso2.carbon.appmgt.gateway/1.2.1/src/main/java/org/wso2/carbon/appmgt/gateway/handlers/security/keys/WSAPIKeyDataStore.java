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
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.dto.APIKeyValidationInfoDTO;

import java.util.ArrayList;

/**
 * Provides a web service interface for the WebApp key data store. This implementation
 * acts as a client stub for the APIKeyValidationService in the WebApp key manager. Using
 * this stub, one may query the key manager to authenticate and authorize WebApp keys.
 * All service invocations are secured using BasicAuth over TLS. Therefore this class
 * may incur a significant overhead on the key validation process.
 */
public class WSAPIKeyDataStore implements APIKeyDataStore {

    private static final APIKeyValidatorClientPool clientPool = APIKeyValidatorClientPool.getInstance();

    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion,
                                                 String apiKey, String clientDomain) throws APISecurityException {
        APIKeyValidatorClient client = null;
        try {
            client = clientPool.get();
            return client.getAPIKeyData(context, apiVersion, apiKey, AppMConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN, clientDomain);
        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for WebApp key validation", e);
        } finally {
            try {
                if (client != null) {
                    clientPool.release(client);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion,
                                                 String apiKey,String requiredAuthenticationLevel, String clientDomain)
            throws APISecurityException {
        APIKeyValidatorClient client = null;
        try {
            client = clientPool.get();
            return client.getAPIKeyData(context, apiVersion, apiKey,requiredAuthenticationLevel, clientDomain);
        }catch (APISecurityException ex) {
            throw new APISecurityException(ex.getErrorCode(),
                    "Resource forbidden", ex);
       }catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for WebApp key validation", e);
        } finally {
            try {
                if (client != null) {
                    clientPool.release(client);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion
    )
            throws APISecurityException {
        APIKeyValidatorClient client = null;
        try {
            client = clientPool.get();
            return client.getAllURITemplates(context, apiVersion);
        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                           "Error while accessing backend services for WebApp key validation", e);
        } finally {
            try {
                if (client != null) {
                    clientPool.release(client);
                }
            } catch (Exception ignored) {
            }
        }
    }


    public void cleanup() {

    }

	public APIKeyValidationInfoDTO getAPPData(String apiContext, String apiVersion, String consumer, AuthenticatedIDP[] authenticatedIDPs) throws APISecurityException {
	    return null;
    }
}
