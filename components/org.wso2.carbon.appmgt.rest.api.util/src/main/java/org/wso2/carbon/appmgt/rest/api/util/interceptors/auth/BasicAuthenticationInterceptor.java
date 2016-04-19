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

package org.wso2.carbon.appmgt.rest.api.util.interceptors.auth;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.appmgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;

/**
 * A CXF interceptor which secures resources using Basic Auth.
 */
public class BasicAuthenticationInterceptor extends AbstractPhaseInterceptor {

    private static final Log log = LogFactory.getLog(BasicAuthenticationInterceptor.class);

    public BasicAuthenticationInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }
    public void handleMessage(Message inMessage) {

        AuthorizationPolicy authorizationPolicy = inMessage.get(AuthorizationPolicy.class);

        if(authorizationPolicy == null){
            ErrorDTO errorDetail = new ErrorDTO((long)401, "No security headers provided.");
            sendErrorResponse(errorDetail, inMessage);
            return;
        }

        // Get the credentials from the security headers.
        String username = StringUtils.trim(authorizationPolicy.getUserName());
        String password = StringUtils.trim(authorizationPolicy.getPassword());

        // Get the relevant services.
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);

        // Try to authenticate the user
        boolean authenticated = authenticate(username, password, realmService);

        if(authenticated){
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            try {
                int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                carbonContext.setTenantDomain(tenantDomain);
                carbonContext.setTenantId(tenantId);
                carbonContext.setUsername(username);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                log.error(String.format("Can't get the tenant ID for the tenant domain '%s'.", tenantDomain));

                ErrorDTO errorDetail = new ErrorDTO((long)500, "Internal Server Error");
                sendErrorResponse(errorDetail, inMessage);
            }
        }else{
            ErrorDTO errorDetail = new ErrorDTO((long)401, "Invalid credentials");
            sendErrorResponse(errorDetail, inMessage);
        }
    }

    private boolean authenticate(String username, String password, RealmService realmService) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RegistryService registryService = (RegistryService) carbonContext.getOSGiService(RegistryService.class, null);

        String tenantDomain = MultitenantUtils.getTenantDomain(username);

        try {
            UserRealm userRealm = AnonymousSessionUtil.getRealmByTenantDomain(registryService, realmService, tenantDomain);

            if (userRealm == null) {
                log.error(String.format("Can't get the user realm for the tenant domain %s. Invalid domain or unactivated tenant login.", tenantDomain));
                return false;
            }

            String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(username);
            return userRealm.getUserStoreManager().authenticate(tenantAwareUsername, password);
        } catch (CarbonException e) {
            log.error(String.format("Can't get the user realm for the tenant domain %s.", tenantDomain), e);
            return false;
        } catch (UserStoreException e) {
            log.error("Error while authenticating the user against the user store manager", e);
            return false;
        }
    }

    private void sendErrorResponse(ErrorDTO errorDetail, Message inMessage) {

        Response response = Response
                .status(Response.Status.fromStatusCode(errorDetail.getCode().intValue()))
                .entity(errorDetail)
                .build();

        inMessage.getExchange().put(Response.class, response);
    }


}