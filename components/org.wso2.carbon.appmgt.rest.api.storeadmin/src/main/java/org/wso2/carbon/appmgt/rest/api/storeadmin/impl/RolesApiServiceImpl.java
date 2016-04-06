package org.wso2.carbon.appmgt.rest.api.storeadmin.impl;

import org.wso2.carbon.appmgt.rest.api.storeadmin.RolesApiService;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.RoleInfoDTO;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.RoleListDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.utils.mappings.RolesMappingUtil;
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

public class RolesApiServiceImpl extends RolesApiService {
    @Override
    public Response rolesGet(Integer limit, Integer offset, String accept, String ifNoneMatch) {


        List<RoleInfoDTO> allMatchedRoles = new ArrayList<>();
        RoleListDTO roleListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;


        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);

        String[] roleNames = null;
        try {
            UserRealm realm = realmService.getTenantUserRealm(-1234);
            UserStoreManager manager = realm.getUserStoreManager();
            roleNames = manager.getRoleNames();
            if (roleNames == null) {
                return RestApiUtil.buildNotFoundException("Roles not found", null).getResponse();
            }
        } catch (UserStoreException e) {
            return RestApiUtil.buildInternalServerErrorException().getResponse();
        }

        for (int i = 0; i < roleNames.length; i++) {
            String roleName = roleNames[i];
            if (roleName.indexOf("Internal/") <= -1) {
                RoleInfoDTO roleInfoDTO = new RoleInfoDTO();
                roleInfoDTO.setName(roleName);
                allMatchedRoles.add(roleInfoDTO);
            }
        }


        if (allMatchedRoles.isEmpty()) {
            String errorMessage = "No result found.";
            return RestApiUtil.buildNotFoundException(errorMessage, null).getResponse();
        }

        roleListDTO = RolesMappingUtil.fromAPIListToDTO(allMatchedRoles, offset, limit);
        RolesMappingUtil.setPaginationParams(roleListDTO, offset, limit, allMatchedRoles.size());
        return Response.ok().entity(roleListDTO).build();
    }


}
