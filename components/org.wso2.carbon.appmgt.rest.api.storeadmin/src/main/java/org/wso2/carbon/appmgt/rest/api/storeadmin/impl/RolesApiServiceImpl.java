package org.wso2.carbon.appmgt.rest.api.storeadmin.impl;

import org.json.simple.JSONArray;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.rest.api.storeadmin.RolesApiService;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.RoleIdListDTO;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import javax.ws.rs.core.Response;

public class RolesApiServiceImpl extends RolesApiService {
    @Override
    public Response rolesGet(Integer limit, Integer offset, String accept, String ifNoneMatch) {
        RoleIdListDTO roleListDTO = new RoleIdListDTO();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);
        String[] roleNames = null;

        try {
            String tenantDomainName = RestApiUtil.getLoggedInUserTenantDomain();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                    tenantDomainName);
            UserRealm realm = realmService.getTenantUserRealm(tenantId);
            UserStoreManager manager = realm.getUserStoreManager();
            roleNames = manager.getRoleNames();
            if (roleNames == null) {
                return RestApiUtil.buildNotFoundException("Roles not found", null).getResponse();
            }
        } catch (UserStoreException e) {
            return RestApiUtil.buildInternalServerErrorException().getResponse();
        }

        JSONArray roleNamesArr = new JSONArray();
        for (int i = 0; i < roleNames.length; i++) {
            String roleName = roleNames[i];
            //exclude internal roles
            if (roleName.indexOf("Internal/") <= -1) {
                roleNamesArr.add(roleName);
            }
        }
        roleListDTO.setRoleIds(roleNamesArr);
        return Response.ok().entity(roleListDTO).build();
    }


}
