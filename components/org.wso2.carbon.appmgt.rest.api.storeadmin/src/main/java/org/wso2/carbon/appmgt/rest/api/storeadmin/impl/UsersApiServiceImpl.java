package org.wso2.carbon.appmgt.rest.api.storeadmin.impl;

import org.wso2.carbon.appmgt.rest.api.storeadmin.UsersApiService;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.UserInfoDTO;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.UserListDTO;
import org.wso2.carbon.appmgt.rest.api.storeadmin.utils.mappings.UsersMappingUtil;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class UsersApiServiceImpl extends UsersApiService {
    @Override
    public Response usersGet(Integer limit, Integer offset, String accept, String ifNoneMatch) {
        List<UserInfoDTO> allMatchedUsers = new ArrayList<>();
        UserListDTO userListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;


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

        for (int i = 0; i < userNames.length; i++) {
            String userName = userNames[i];
            UserInfoDTO userInfoDTO = new UserInfoDTO();
            userInfoDTO.setUsername(userName);
            allMatchedUsers.add(userInfoDTO);
        }


        if (allMatchedUsers.isEmpty()) {
            String errorMessage = "No result found.";
            return RestApiUtil.buildNotFoundException(errorMessage, null).getResponse();
        }

        userListDTO = UsersMappingUtil.fromAPIListToDTO(allMatchedUsers, offset, limit);
        UsersMappingUtil.setPaginationParams(userListDTO, offset, limit, allMatchedUsers.size());
        return Response.ok().entity(userListDTO).build();
    }
}
