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

package org.wso2.carbon.appmgt.gateway.handlers.subscription;

import org.apache.axis2.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.AuthenticatedIDP;
import org.wso2.carbon.appmgt.api.model.Subscription;
import org.wso2.carbon.appmgt.gateway.handlers.security.Session;
import org.wso2.carbon.appmgt.gateway.handlers.security.authentication.AuthenticationContext;
import org.wso2.carbon.appmgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.DefaultAppRepository;

import java.util.List;

/**
 * Validates app subscription (individual and enterprise subscriptions)
 */
public class SubscriptionsHandler extends AbstractHandler implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(SubscriptionsHandler.class);

    private Subscription enterpriseSubscription;

    private AppManagerConfiguration configuration;

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        GatewayUtils.logRequest(log, messageContext);

        if(!isHandlerApplicable()){
            return true;
        }

        String webAppContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String webAppVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

        Session session = GatewayUtils.getSession(messageContext);
        AuthenticationContext authenticationContext = session.getAuthenticationContext();

        if(configuration.isEnterpriseSubscriptionEnabled()){

            if(enterpriseSubscription == null){
                try {
                    enterpriseSubscription = new DefaultAppRepository(null).getEnterpriseSubscription(webAppContext, webAppVersion);
                } catch (AppManagementException e) {
                    GatewayUtils.logAndThrowException(log, String.format("Can't find enterprise subscription entry for '%s':'%s'", webAppContext, webAppVersion), e);
                }
            }

            if(hasValidEnterpriseSubscription(authenticationContext)){
                if(log.isDebugEnabled()){

                    StringBuilder authenticatedIDPNames = new StringBuilder();
                    for (AuthenticatedIDP authenticatedIDP : authenticationContext.getAuthenticatedIDPs()){
                        authenticatedIDPNames.append(authenticatedIDP.getIdpName());
                    }

                    if(log.isDebugEnabled()){
                        GatewayUtils.logWithRequestInfo(log, messageContext, String.format("User '%s' has an enterprise subscription (IDP(s) : ['%s']) for '%s':'%s'",
                            authenticationContext.getSubject(), authenticatedIDPNames, webAppContext, webAppVersion));
                    }

                }
                return true;
            }


        }

        if(configuration.isSelfSubscriptionEnabled()){
            // TODO : Validate self subscriptions
            return true;
        }

        if(log.isDebugEnabled()){
            GatewayUtils.logWithRequestInfo(log, messageContext, String.format("User '%s' has no subscriptions for '%s':'%s'",
                    authenticationContext.getSubject(), webAppContext, webAppVersion));
        }

        GatewayUtils.send401(messageContext, "You have no subscriptions for this app.");

        return false;
    }

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        configuration = org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }


    // ----------------------------------------------------------------------------------------------------------

    private boolean hasValidEnterpriseSubscription(AuthenticationContext session) {

        List<AuthenticatedIDP> authenticatedIDPs = session.getAuthenticatedIDPs();

        if(authenticatedIDPs != null){
            AuthenticatedIDP[] authenticatedIDPsArray = new AuthenticatedIDP[authenticatedIDPs.size()];
            authenticatedIDPs.toArray(authenticatedIDPsArray);
            return enterpriseSubscription.isTrustedIdp(authenticatedIDPsArray);
        }else{
            return false;
        }
    }

    private boolean isHandlerApplicable() {
        return configuration.isSelfSubscriptionEnabled() || configuration.isEnterpriseSubscriptionEnabled();
    }
}
