/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.rest.api.storeadmin.impl;

import org.json.simple.JSONArray;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.rest.api.storeadmin.UsersApiService;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.UserIdListDTO;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import javax.ws.rs.core.Response;

public class UsersApiServiceImpl extends UsersApiService {
    @Override
    public Response usersGet(Integer limit, Integer offset, String accept, String ifNoneMatch) {
        UserIdListDTO userListDTO = new UserIdListDTO();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);
        String[] userNames = null;

        try {
            String tenantDomainName = RestApiUtil.getLoggedInUserTenantDomain();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainName);
            UserRealm realm = realmService.getTenantUserRealm(tenantId);
            UserStoreManager manager = realm.getUserStoreManager();
            userNames = manager.listUsers("", -1);
            if (userNames == null) {
                return RestApiUtil.buildNotFoundException("Users not found", null).getResponse();
            }
        } catch (UserStoreException e) {
            return RestApiUtil.buildInternalServerErrorException().getResponse();
        }

        JSONArray userNamesArr = new JSONArray();
        for (int i = 0; i < userNames.length; i++) {
            userNamesArr.add(userNames[i]);
        }
        userListDTO.setUserIds(userNamesArr);
        return Response.ok().entity(userListDTO).build();
    }
}
