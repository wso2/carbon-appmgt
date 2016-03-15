/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.impl.idp.sso.configurator;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.IdentityApplicationManagementAdapter;
import org.wso2.carbon.appmgt.api.model.SSOProvider;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.idp.sso.SSOConfiguratorUtil;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.application.common.model.xsd.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Identity server v5.x.x based SAML SSO configurator.
 * This supports dynamic discovery of remote IS server based on available OSGI bundles.
 * @see  IdentityApplicationManagementAdapter
 */
public class IS5xxSAMLSSOConfigurator extends ISBaseSAMLSSOConfigurator implements SSOConfigurator {

    private static Log log = LogFactory.getLog(IS5xxSAMLSSOConfigurator.class);

    private static String IDP_NAME = "idpName";
    private static String AUTHENTICATION_STEP = "authenticationStep";
    private IdentityApplicationManagementAdapter appIdpMgt;
    private Map<String, String> parameters;
    private String backendServerURL;
    private String user;
    private String pass;
    private String cookie;
    private String idpName;
    private String authenticationStep;

    @Override
    public void init(Map<String, String> configuration) {
        this.parameters = configuration;
        this.backendServerURL = configuration.get(SERVER_URL);
        this.user = configuration.get(USERNAME);
        this.pass = configuration.get(PASSWORD);
        this.idpName = configuration.get(IDP_NAME);
        this.authenticationStep = configuration.get(AUTHENTICATION_STEP);

        try {
            cookie = login();

            appIdpMgt = ServiceReferenceHolder.getInstance().getIdentityApplicationManagementFactory().createAdapter(backendServerURL, cookie);
        } catch (Exception ex) {
            log.error("Error initializing WSO2 SAML SSO Configurator", ex);
        }
    }

    @Override
    public boolean createProvider(SSOProvider provider) {
        return appIdpMgt.createProvider(provider, idpName, authenticationStep);
    }

    @Override
    public boolean createProvider(WebApp webApp) {
        return appIdpMgt.createProvider(webApp, idpName, authenticationStep, SSOConfiguratorUtil.getGatewayUrl(webApp));
    }

    @Override
    public boolean removeProvider(SSOProvider provider) {
        return appIdpMgt.removeProvider(provider);
    }

    @Override
    public boolean updateProvider(SSOProvider provider) {
        return appIdpMgt.updateProvider(provider, idpName, authenticationStep);
    }

    @Override
    public boolean updateProvider(WebApp application) {
        return appIdpMgt.updateProvider(application, idpName, authenticationStep,
                SSOConfiguratorUtil.getGatewayUrl(application));
    }

    @Override
    public String[] getAllClaims() {
        return appIdpMgt.getAllLocalClaimUris();
    }

    @Override
    public boolean isAvailable() throws Exception {
        String host = parameters.get(IS5xxSAMLSSOConfigurator.SERVER_URL);
        host = host.substring(host.indexOf("/") + 2);
        int port = Integer.valueOf(host.split(":")[1]);
        host = host.split(":")[0];
        return SSOConfiguratorUtil.isUp(host, port);
    }

    @Override
    public SSOProvider getProvider(String issuerName) {
        return appIdpMgt.getProvider(issuerName);
    }

    /**
     * Returns IDPs in 'Local & Outbound Authentication Configuration' section of the service provider.
     * @param serviceProviderId
     * @return
     */
    @Override
    public String[] getIdentityProvidersInServiceProvider(String serviceProviderId) {
        return appIdpMgt.getIdentityProvidersInServiceProvider(serviceProviderId);
    }

    private String login() throws AxisFault {
        if (backendServerURL == null || user == null || pass == null) {
            throw new AxisFault("SSO Configurator authentication details unspecified");
        }

        String host;
        try {
            host = new URL(backendServerURL).getHost();
        } catch (MalformedURLException e) {
            throw new AxisFault("SSO Configurator provider URL is malformed", e);
        }

        AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(null,
                backendServerURL + "/services/AuthenticationAdmin");
        ServiceClient client = authAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            authAdminStub.login(user, pass, host);
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            return sessionCookie;
        } catch (RemoteException e) {
            throw new AxisFault("Error while contacting the authentication admin services", e);
        } catch (LoginAuthenticationExceptionException e) {
            throw new AxisFault("Error while authenticating against the SSO IDP admin", e);
        }
    }

    private ServiceProvider updateClaimConfiguration(SSOProvider ssoProvider, ServiceProvider serviceProvider) {
        ClaimConfig claimConfig = new ClaimConfig();
        List<ClaimMapping> claimMappings = new ArrayList<ClaimMapping>();

        for (String claim : ssoProvider.getClaims()) {
            Claim localClaim = new Claim();
            Claim remoteClaim = new Claim();
            localClaim.setClaimUri(claim);
            remoteClaim.setClaimUri(claim);

            ClaimMapping claimMapping = new ClaimMapping();
            claimMapping.setLocalClaim(localClaim);
            claimMapping.setRemoteClaim(remoteClaim);
            claimMapping.setRequested(true);
            claimMappings.add(claimMapping);

        }

        claimConfig.setLocalClaimDialect(true);
        claimConfig.setClaimMappings(claimMappings.toArray(new ClaimMapping[claimMappings.size()]));
        serviceProvider.setClaimConfig(claimConfig);

        return serviceProvider;
    }

    private void setLocalAndOutBoundAuthentication(ServiceProvider serviceProvider) {

        // Available authentication types => default, local, federated or advanced.
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(new LocalAndOutboundAuthenticationConfig());

        // Start with setting the authentication type as default.
        // And override it if the configurations instructions are there to set another authentication type.
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationType("default");

        // Set federated authentication type if the relevant configurations are available.
        if (idpName != null && authenticationStep != null && authenticationStep.equalsIgnoreCase("federated")) {
            if (log.isDebugEnabled()) {
                log.debug("Adding federated authentication step. Added IDP named: " + idpName);
            }

            //Following code will set external IDP as authentication EP
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationType("federated");
            InboundProvisioningConfig inBoundProConfig = new InboundProvisioningConfig();
            inBoundProConfig.setProvisioningUserStore("");
            serviceProvider.setInboundProvisioningConfig(inBoundProConfig);
            serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
            serviceProvider.setRequestPathAuthenticatorConfigs(null);
            AuthenticationStep step = new AuthenticationStep();
            AuthenticationStep[] steps = new AuthenticationStep[1];
            IdentityProvider idp = new IdentityProvider();
            idp.setDisplayName(idpName);
            idp.setIdentityProviderName(idpName);
            step.addFederatedIdentityProviders(idp);
            steps[0] = step;
            serviceProvider.setPermissionAndRoleConfig(new PermissionsAndRoleConfig());
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(steps);

        } else {
            if (log.isDebugEnabled()) {
                log.debug("Adding default authentication step to SP");
            }
            InboundProvisioningConfig inBoundProConfig = new InboundProvisioningConfig();
            inBoundProConfig.setProvisioningUserStore("");
            serviceProvider.setInboundProvisioningConfig(inBoundProConfig);
            serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
            serviceProvider.setRequestPathAuthenticatorConfigs(null);
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(null);
            serviceProvider.setPermissionAndRoleConfig(new PermissionsAndRoleConfig());
        }
    }
}
