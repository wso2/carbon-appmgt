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
package org.wso2.carbon.appmgt.oauth.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.oauth.rest.dto.OAuthApplication;
import org.wso2.carbon.appmgt.oauth.rest.dto.RegistrationProfile;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistrationService {

    private static final Log log = LogFactory.getLog(RegistrationService.class);

    /**
     * Endpoint for registering OAuth apps.
     *
     * Sample request payload
     * {
     * "callbackUrl": "www.google.lk",
     * "clientName": "appm_publisher",
     * "tokenScope": "Production",
     * "owner": "admin",
     * "grantType": "password refresh_token",
     * "saasApp": true
     *}
     */
    @Path("/register")
    @POST
     public Response register(RegistrationProfile registrationProfile) {

        Response response = null;

        CarbonContext carbonContext =  CarbonContext.getThreadLocalCarbonContext();

        String username = carbonContext.getUsername();
        String tenantDomain = carbonContext.getTenantDomain();

        try {
            ServiceProvider serviceProvider = createServiceProvider(registrationProfile, username, tenantDomain);
            OAuthConsumerAppDTO oauthApp = createOAuthApp(registrationProfile);
            addInboundAuthentication(serviceProvider, oauthApp, tenantDomain, username);
            response = getResponse(oauthApp);
        } catch (RegistrationException e) {
            String errorMessage = String.format("Can't register the OAuth client app '%s'", registrationProfile.getClientName());
            log.error(errorMessage, e);
            response = getErrorResponse(registrationProfile, errorMessage);
        }
        return response;
    }

    private Response getErrorResponse(RegistrationProfile registrationProfile, String errorMessage) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    }

    private Response getResponse(OAuthConsumerAppDTO oauthApp) {

        OAuthApplication oAuthApplication = new OAuthApplication();

        oAuthApplication.setClientName(oauthApp.getApplicationName());
        oAuthApplication.setClientId(oauthApp.getOauthConsumerKey());
        oAuthApplication.setClientSecret(oauthApp.getOauthConsumerSecret());
        oAuthApplication.setCallBackURL(oauthApp.getCallbackUrl());

        return Response.status(Response.Status.CREATED).entity(oAuthApplication).build();
    }

    private ServiceProvider createServiceProvider(RegistrationProfile registrationProfile, String username,
                                                    String tenantDomain) throws RegistrationException {

        String applicationName = registrationProfile.getClientName();

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(applicationName);
        serviceProvider.setDescription("Service Provider for application " + applicationName);

        ApplicationManagementService appMgtService = ApplicationManagementService.getInstance();
        try {

            log.debug(String.format("Creating service provider '%s'.", applicationName));

            appMgtService.createApplication(serviceProvider, tenantDomain, username);

            // Workaround : This is a workaround to set the SaaS app property.
            ServiceProvider createdServiceProvider = appMgtService.getApplicationExcludingFileBasedSPs(applicationName, tenantDomain);
            createdServiceProvider.setSaasApp(registrationProfile.isSaasApp());
            appMgtService.updateApplication(createdServiceProvider, tenantDomain, username);

            log.debug(String.format("Created service provider '%s'.", applicationName));

            return appMgtService.getApplicationExcludingFileBasedSPs(applicationName, tenantDomain);

        } catch (IdentityApplicationManagementException e) {
            String errorMessage = String.format("Can't create the service provider for the application name '%s'.", registrationProfile.getClientName());
            log.error(errorMessage, e);
            throw new RegistrationException(errorMessage, e);
        }

    }

    private OAuthConsumerAppDTO createOAuthApp(RegistrationProfile registrationProfile) throws RegistrationException {

        String applicationName = registrationProfile.getClientName();
        String callbackUrl = registrationProfile.getCallbackUrl();

        OAuthAdminService oAuthAdminService = new OAuthAdminService();
        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        oAuthConsumerAppDTO.setApplicationName(applicationName);
        oAuthConsumerAppDTO.setCallbackUrl(callbackUrl);

        String[] allowedGrantTypes = oAuthAdminService.getAllowedGrantTypes();
        // CallbackURL is needed for authorization_code and implicit grant types. If CallbackURL is empty,
        // simply remove those grant types from the list
        StringBuilder grantTypeString = new StringBuilder();

        for (String grantType : allowedGrantTypes) {
            if (callbackUrl == null || callbackUrl.isEmpty()) {
                if ("authorization_code".equals(grantType) || "implicit".equals(grantType)) {
                    continue;
                }
            }
            grantTypeString.append(grantType).append(" ");
        }

        if (grantTypeString.length() > 0) {
            oAuthConsumerAppDTO.setGrantTypes(grantTypeString.toString().trim());
            log.debug("Set grant types : " + grantTypeString);
        }

        oAuthConsumerAppDTO.setOAuthVersion(OAuthConstants.OAuthVersions.VERSION_2);
        log.debug(String.format("Creating OAuth app '%s'.", applicationName));
        try {
            oAuthAdminService.registerOAuthApplicationData(oAuthConsumerAppDTO);
            log.debug(String.format("Created OAuth app '%s'.", applicationName));
            OAuthConsumerAppDTO createdApp = oAuthAdminService.getOAuthApplicationDataByAppName(oAuthConsumerAppDTO
                        .getApplicationName());
            return createdApp;
        } catch (IdentityOAuthAdminException e) {
            String errorMessage = String.format("Can't create the OAuth app for the application name '%s'.", registrationProfile.getClientName());
            log.error(errorMessage, e);
            throw new RegistrationException(errorMessage, e);
        }
    }

    private void addInboundAuthentication(ServiceProvider serviceProvider, OAuthConsumerAppDTO oauthApp, String tenantDomain, String username) throws RegistrationException {

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        InboundAuthenticationRequestConfig[] inboundAuthenticationRequestConfigs = new
                InboundAuthenticationRequestConfig[1];
        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = new
                InboundAuthenticationRequestConfig();

        inboundAuthenticationRequestConfig.setInboundAuthKey(oauthApp.getOauthConsumerKey());
        inboundAuthenticationRequestConfig.setInboundAuthType("oauth2");
        if (oauthApp.getOauthConsumerSecret() != null && !oauthApp.
                getOauthConsumerSecret().isEmpty()) {
            Property property = new Property();
            property.setName("oauthConsumerSecret");
            property.setValue(oauthApp.getOauthConsumerSecret());
            Property[] properties = {property};
            inboundAuthenticationRequestConfig.setProperties(properties);
        }

        inboundAuthenticationRequestConfigs[0] = inboundAuthenticationRequestConfig;
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(inboundAuthenticationRequestConfigs);
        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

        // Update the Service Provider app to add OAuthApp as an Inbound Authentication Config
        try {
            ApplicationManagementService appMgtService = ApplicationManagementService.getInstance();
            log.debug(String.format("Adding inbound authentication for the service provider '%s'.", serviceProvider.getApplicationName()));
            appMgtService.updateApplication(serviceProvider,tenantDomain,username);
            log.debug(String.format("Added inbound authentication for the service provider '%s'.", serviceProvider.getApplicationName()));
        } catch (IdentityApplicationManagementException e) {
            String errorMessage = String.format("Can't add inbound authentication for the service provider '%s'.", serviceProvider.getApplicationName());
            log.error(errorMessage, e);
            throw new RegistrationException(errorMessage, e);
        }
    }



}
