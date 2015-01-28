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

import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.AuthenticatedIDP;
import org.wso2.carbon.appmgt.api.model.URITemplate;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.dto.APIKeyValidationInfoDTO;

import java.util.ArrayList;

/**
 * A JDBC interface for the WebApp key data store. This implementation directly
 * interacts with the WebApp Manager database to validate and authenticate WebApp
 * keys.
 */
public class JDBCAPIKeyDataStore implements APIKeyDataStore {

    private AppMDAO dao;

    public JDBCAPIKeyDataStore() throws APISecurityException {
        dao = new AppMDAO();
    }

    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion,
                                                 String apiKey, String clientDomain) throws APISecurityException {
        try {
            return dao.validateKey(context, apiVersion, apiKey, AppMConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
        } catch (AppManagementException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while looking up WebApp key data in the database", e);
        }
    }

    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion,
                                                 String apiKey,String requiredAuthenticationLevel, String clientDomain) throws APISecurityException {
        try {
            return dao.validateKey(context, apiVersion, apiKey,requiredAuthenticationLevel);
        } catch (AppManagementException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while looking up WebApp key data in the database", e);
        }
    }
    public ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion
    ) throws APISecurityException {
        try {
            return AppMDAO.getAllURITemplates(context, apiVersion);
        } catch (AppManagementException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                           "Error while looking up WebApp resource URI templates in the database", e);
        }
    }

    public void cleanup() {

    }

	public APIKeyValidationInfoDTO getAPPData(String appContext, String appVersion, String consumer, AuthenticatedIDP[] authenticatedIDPs) throws APISecurityException {
		try {
			return dao.getApplicationData(appContext, appVersion, consumer, authenticatedIDPs);
		} catch (AppManagementException e) {
			throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
			                               e.getMessage(), e);
		}
	}
}
