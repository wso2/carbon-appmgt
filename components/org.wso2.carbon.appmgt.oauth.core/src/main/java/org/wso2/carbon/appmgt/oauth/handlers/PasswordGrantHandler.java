/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appmgt.oauth.handlers;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.*;

/**
 * An extension to the existing password grant handler, to validate the scopes.
 */
public class PasswordGrantHandler extends org.wso2.carbon.identity.oauth2.token.handlers.grant.PasswordGrantHandler{


    private static final Log log = LogFactory.getLog(PasswordGrantHandler.class);

    @Override
    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {


        Map<String, String> scopeRoleMapping = ScopesRetriever.getScopeRoleMapping(tokReqMsgCtx.getAuthorizedUser().getTenantDomain());

        String[] authorizedScopes = getAuthorizedScopes(tokReqMsgCtx.getAuthorizedUser(), tokReqMsgCtx.getScope(), scopeRoleMapping);

        tokReqMsgCtx.setScope(authorizedScopes);

        return super.validateScope(tokReqMsgCtx);
    }

    private String[] getAuthorizedScopes(AuthenticatedUser authorizedUser, String[] requestedScopes, Map<String, String> scopeRoleMapping) {

        String[] authorizedScopes = new String[0];

        String[] userRoles = getUserRoles(authorizedUser);
        Map<String, Set<String>> roleScopeMapping = getRoleScopeMapping(scopeRoleMapping);

        Set<String> allowedScopes = new HashSet<String>();

        Set<String> scopesForRole = null;
        for(String role : userRoles){
            scopesForRole = roleScopeMapping.get(role);
            if(scopesForRole != null){
                allowedScopes.addAll(scopesForRole);
            }
        }

        if(requestedScopes.length > 0 && allowedScopes.size() > 0){
            List<String> intersection = ListUtils.intersection(Arrays.asList(requestedScopes), new ArrayList<String>(allowedScopes));
            authorizedScopes = intersection.toArray(new String[0]);
        }

        return authorizedScopes;
    }

    private Map<String, Set<String>> getRoleScopeMapping(Map<String, String> scopeRoleMapping) {

        Map<String, Set<String>> roleScopeMapping = new HashMap<>();

        for(Map.Entry<String,String> entry : scopeRoleMapping.entrySet()){

            String rolesString = entry.getValue();

            String[] roles = rolesString.split(",");

            for(String role : roles){

                Set<String> scopesForRole = roleScopeMapping.get(role);

                if(scopesForRole == null){
                    scopesForRole = new HashSet<String>();
                    roleScopeMapping.put(role, scopesForRole);
                }
                scopesForRole.add(entry.getKey());
            }
        }

        return roleScopeMapping;
    }

    private String[] getUserRoles(AuthenticatedUser authorizedUser) {

        String[] userRoles = new String[0];

        String userNameWithUserStoreDomain = UserCoreUtil.addDomainToName(authorizedUser.getUserName(),
                                                                            authorizedUser.getUserStoreDomain());

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);

        try {
            int tenantId = realmService.getTenantManager().getTenantId(authorizedUser.getTenantDomain());
            UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();

            userRoles = userStoreManager.getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(userNameWithUserStoreDomain));

        } catch (UserStoreException e) {
            log.error(String.format("Can't get the roles list for the user '%s'", MultitenantUtils.getTenantAwareUsername(userNameWithUserStoreDomain)));
        }

        return userRoles;
    }
}
