package org.wso2.carbon.appmgt.rest.api.storeadmin.impl;

import org.json.simple.JSONArray;
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
            UserRealm realm = realmService.getTenantUserRealm(-1234);
            UserStoreManager manager = realm.getUserStoreManager();
            userNames = manager.listUsers("", -1);
            if (userNames == null) {
                return RestApiUtil.buildNotFoundException("Users not found", null).getResponse();
            }
        } catch (UserStoreException e) {
            return RestApiUtil.buildInternalServerErrorException().getResponse();
        }

        //JSONArray userNamesArr = new JSONArray(Array);
        JSONArray userNamesArr = new JSONArray();


        for (int i = 0; i < userNames.length; i++) {
            userNamesArr.add(userNames[i]);
        }

        userListDTO.setUserIds(userNamesArr);
        return Response.ok().entity(userListDTO).build();
    }
}
