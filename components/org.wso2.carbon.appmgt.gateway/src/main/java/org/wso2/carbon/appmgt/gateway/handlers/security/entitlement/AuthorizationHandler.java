/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   you may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.appmgt.gateway.handlers.security.entitlement;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.URITemplate;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.gateway.handlers.security.Session;
import org.wso2.carbon.appmgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.DefaultAppRepository;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class handles built-in authorization.
 */
public class AuthorizationHandler extends AbstractHandler implements ManagedLifecycle {

	private static final Log log = LogFactory.getLog(AuthorizationHandler.class);
	
    private AppManagerConfiguration configuration;
    private WebApp webApp;

    public void init(SynapseEnvironment synapseEnvironment) {
        configuration = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
    }

    public boolean handleRequest(MessageContext messageContext) {

        GatewayUtils.logRequest(log, messageContext);

        String fullResourceURL = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);

    	if (isHandlerApplicable(messageContext)) {
            String webAppContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
            String webAppVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

            // Fetch the web app for the requested context and version.
            int appTenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            try {
                if(webApp == null){
                    webApp = new DefaultAppRepository(null).getWebAppByContextAndVersion(webAppContext, webAppVersion, appTenantId);
                }
            } catch (AppManagementException e) {
                String errorMessage = String.format("Can't fetch the web for '%s' from the repository.", fullResourceURL);
                GatewayUtils.logAndThrowException(log, errorMessage, e);
            }

            Session session = GatewayUtils.getSession(messageContext);
            List<String> roles = session.getAuthenticationContext().getRoles();
            List<String> visibleRoles = webApp.getVisibleRoleList();

            String appTenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String userTenantDomain = session.getAuthenticationContext().getTenantDomain();

            //Check for app visibility
            if(!visibleRoles.isEmpty()){
                if (!isUserInCurrentTenantDomain(userTenantDomain)) {
                    GatewayUtils.send401(messageContext, null);
                    if (log.isDebugEnabled()) {
                        GatewayUtils.logWithRequestInfo(log, messageContext,
                                String.format("User tenant domain '%s' and app tenant domain '%s' mismatch", userTenantDomain, appTenantDomain));
                        return false;
                    }
                }
                String appTenantAdminRole = null;
                try {
                    appTenantAdminRole = getAdminRole();
                } catch (UserStoreException e) {
                    GatewayUtils.logAndThrowException(log,
                            String.format("Error occurred while retrieving realm admin for tenant domain '%s' ",appTenantDomain), e);
                }
                if(ListUtils.intersection(roles, visibleRoles).isEmpty() && !roles.contains(appTenantAdminRole)){
                    if(log.isDebugEnabled()){
                        GatewayUtils.logWithRequestInfo(log, messageContext, String.format("'%s' doesn't have required roles to access '%s'",
                                session.getAuthenticationContext().getSubject(), fullResourceURL));
                    }
                    GatewayUtils.send401(messageContext, "You don't have required user role(s) to access this resource.");
                    return false;
                }
            }

            // Check role based access.

            URITemplate matchedTemplate = (URITemplate) messageContext.getProperty(AppMConstants.MESSAGE_CONTEXT_PROPERTY_MATCHED_URI_TEMPLATE);

            if(matchedTemplate != null && matchedTemplate.isRoleRestricted()){

                if(log.isDebugEnabled()){
                    GatewayUtils.logWithRequestInfo(log, messageContext, String.format("Resource '%s' is role restricted", fullResourceURL));
                }

                if (!isUserInCurrentTenantDomain(session.getAuthenticationContext().getTenantDomain())) {
                    GatewayUtils.send401(messageContext, null);
                    if (log.isDebugEnabled()) {
                        GatewayUtils.logWithRequestInfo(log, messageContext,
                                String.format("User tenant domain '%s' and app tenant domain '%s' mismatch", userTenantDomain, appTenantDomain));
                        return false;
                    }
                }

                List<String> allowedRoles  = matchedTemplate.getAllowedRoles();
                if(!ListUtils.intersection(roles, allowedRoles).isEmpty()){

                    if(log.isDebugEnabled()){
                        GatewayUtils.logWithRequestInfo(log, messageContext, String.format("'%s' has required roles to access '%s'",
                                session.getAuthenticationContext().getSubject(), fullResourceURL));
                    }

                    return true;
                }else {

                    if(log.isDebugEnabled()){
                        GatewayUtils.logWithRequestInfo(log, messageContext, String.format("'%s' doesn't have required roles to access '%s'",
                                session.getAuthenticationContext().getSubject(), fullResourceURL));
                    }

                    GatewayUtils.send401(messageContext, "You don't have required user role(s) to access this resource.");
                    return false;
                }
            }else{

                // This requested is not role restricted.
                return true;
            }
        } else {
        	return true;
        }
    }

    private boolean isUserInCurrentTenantDomain(String userDomain){
        String appTenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        return appTenantDomain.equals(userDomain);
    }

    private String getAdminRole() throws UserStoreException {
       return CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration().getAdminRoleName();
    }

	public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    public void destroy() {

    }

    private boolean isHandlerApplicable(MessageContext messageContext) {
		return !GatewayUtils.shouldSkipSecurity(messageContext);
	}

}
