/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.appmgt.keymgt.service;

import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.appmgt.impl.dto.APIKeyInfoDTO;
import org.wso2.carbon.appmgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.base.IdentityException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides an WebApp for the required for implementing the functionality required by
 * the WebApp providers. This include getting the list of keys issued for a given WebApp, get the list of APIs
 * provided by him where a user has subscribed and perform different actions on issued keys like activating,
 * revoking, etc.
 */
public class APIKeyMgtProviderService extends AbstractAdmin {

    /**
     * Get the issued keys for a given WebApp. This method returns the set of users who have subscribed
     * for the given WebApp and the status of the Key, whether it is ACTIVE, BLOCKED OR REVOKED.
     *
     * @param apiInfoDTO Information about the WebApp. Provider Name, WebApp Name and Version uniquely identifies an WebApp.
     * @return An array of APIKeyInfoDTO. Each APIKeyInfoDTO contains the user id and the status of the key.
     * @throws APIKeyMgtException Error has occurred when processing reading WebApp Key Info from the database.
     */
    public APIKeyInfoDTO[] getIssuedKeyInfo(APIInfoDTO apiInfoDTO) throws APIKeyMgtException,
                                                                          AppManagementException {
        AppMDAO appMDAO = new AppMDAO();
        return appMDAO.getSubscribedUsersForAPI(apiInfoDTO);
    }

    /**
     * Get the list of APIs a user has subscribed for a given provider.
     *
     * @param userId     User Id
     * @param providerId Provider Id
     * @return Array of APIInfoDTO objects for each WebApp that the user has subscribed for a given provider.
     * @throws APIKeyMgtException Error has occurred when processing reading WebApp Info from the database.
     */
    public APIInfoDTO[] getAPIsOfUser(String userId, String providerId) throws APIKeyMgtException,
                                                                               AppManagementException, IdentityException {
        AppMDAO appMDAO = new AppMDAO();
        APIInfoDTO[] apiInfoDTOs = appMDAO.getSubscribedAPIsOfUser(userId);
        // Filter by Provider
        List<APIInfoDTO> apiInfoDTOList = new ArrayList<APIInfoDTO>();
        for (APIInfoDTO apiInfoDTO : apiInfoDTOs) {
            if (apiInfoDTO.getProviderId().equalsIgnoreCase(providerId)) {
                apiInfoDTOList.add(apiInfoDTO);
            }
        }
        return apiInfoDTOList.toArray(new APIInfoDTO[apiInfoDTOList.size()]);
    }

    /**
     * Activate the keys of the set of users subscribed for the given WebApp
     * @param users Subscribed Users whose keys will be activated
     * @param apiInfoDTO WebApp Information
     * @throws APIKeyMgtException Error has occurred when processing updating the key Info from the database.
     */
    public void activateAccessTokens(String[] users, APIInfoDTO apiInfoDTO) throws APIKeyMgtException,
                                                                                   AppManagementException, IdentityException {
        AppMDAO appMDAO = new AppMDAO();
        for (String userId : users) {
            appMDAO.changeAccessTokenStatus(userId, apiInfoDTO, AppMConstants.TokenStatus.ACTIVE);
        }
    }

    /**
     * Block the keys of the set of users subscribed for the given WebApp
     * @param users Subscribed Users whose keys will be blocked
     * @param apiInfoDTO WebApp Information
     * @throws APIKeyMgtException Error has occurred when processing updating the key Info from the database.
     */
    public void BlockAccessTokens(String[] users, APIInfoDTO apiInfoDTO) throws APIKeyMgtException,
                                                                                AppManagementException, IdentityException {
        AppMDAO appMDAO = new AppMDAO();
        for (String userId : users) {
            appMDAO.changeAccessTokenStatus(userId, apiInfoDTO, AppMConstants.TokenStatus.BLOCKED);
        }
    }

    /**
     * Revoke the keys of the set of users subscribed for the given WebApp
     * @param users Subscribed Users whose keys will be revoked.
     * @param apiInfoDTO WebApp Information
     * @throws APIKeyMgtException Error has occurred when processing updating the key Info from the database.
     */
    public void revokeAccessTokens(String[] users, APIInfoDTO apiInfoDTO) throws APIKeyMgtException,
                                                                                 AppManagementException, IdentityException {
        AppMDAO appMDAO = new AppMDAO();
        for (String userId : users) {
            appMDAO.changeAccessTokenStatus(userId, apiInfoDTO, AppMConstants.TokenStatus.REVOKED);
        }
    }

}
