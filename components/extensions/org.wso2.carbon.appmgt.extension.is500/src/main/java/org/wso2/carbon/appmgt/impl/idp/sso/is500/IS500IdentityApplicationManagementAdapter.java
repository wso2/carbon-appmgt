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

package org.wso2.carbon.appmgt.impl.idp.sso.is500;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.IdentityApplicationManagementAdapter;
import org.wso2.carbon.appmgt.api.model.SSOProvider;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.identity.application.common.model.xsd.*;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceStub;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceIdentityException;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceStub;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderInfoDTO;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class IS500IdentityApplicationManagementAdapter implements IdentityApplicationManagementAdapter {

    private static Log log = LogFactory.getLog(IS500IdentityApplicationManagementAdapter.class);

    private static String APP_DESC = "WSO2 Application Manager generated service provider.";
    private static String AUTH_TYPE = "samlsso";

    private IdentityApplicationManagementServiceStub appMgtStub;
    private IdentitySAMLSSOConfigServiceStub ssoStub;

    public void init(String backendServerURL) throws AppManagementException {
        String serviceURL = backendServerURL + "/services/IdentitySAMLSSOConfigService";
        try {
            ssoStub = new IdentitySAMLSSOConfigServiceStub(serviceURL);
        } catch (AxisFault axisFault) {
            throw new AppManagementException(axisFault);
        }
        try {
            serviceURL = backendServerURL + "/services/IdentityApplicationManagementService";
            appMgtStub = new IdentityApplicationManagementServiceStub(serviceURL);
        } catch (AxisFault axisFault) {
            throw new AppManagementException(axisFault);
        }
    }

    public void setAuthCookie(String cookie) {
        ServiceClient client = ssoStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(HTTPConstants.COOKIE_STRING, cookie);

        ServiceClient client2 = appMgtStub._getServiceClient();
        Options option2 = client2.getOptions();
        option2.setManageSession(true);
        option2.setProperty(HTTPConstants.COOKIE_STRING, cookie);
    }

    @Override
    public boolean createProvider(SSOProvider provider, String idpName, String authenticationStep) {
        SAMLSSOServiceProviderDTO serviceProviderDTO = generateDTO(provider);
        boolean status = false;
        try {
            status = ssoStub.addRPServiceProvider(serviceProviderDTO);
            String attributeConsumingServiceIndex = getServiceProvider(provider.getIssuerName()).getAttributeConsumingServiceIndex();
            ServiceProvider serviceProvider = generateSPCreate(provider);
            appMgtStub.createApplication(serviceProvider);
            serviceProvider = generateSPUpdate(provider, serviceProvider, attributeConsumingServiceIndex,
                    idpName, authenticationStep);
            appMgtStub.updateApplication(serviceProvider);
        } catch (Exception e) {
            log.error("Error adding a new Service Provider", e);
        }
        return status;
    }

    @Override
    public boolean createProvider(WebApp webApp, String idpName, String authenticationStep, String gatewayUrl) {
        String acsUrl = webApp.getAcsURL().trim();
        SSOProvider ssoProvider = webApp.getSsoProviderDetails();
        boolean status = false;
        if (ssoProvider == null) {
            log.warn("No SSO Configurator details given. Manual setup of SSO Provider required.");
        } else {
            if (acsUrl != null && acsUrl.length() > 0) {
                ssoProvider.setAssertionConsumerURL(acsUrl);
            } else {
                ssoProvider.setAssertionConsumerURL(gatewayUrl);
            }

            ServiceProvider serviceProvider;
            SAMLSSOServiceProviderDTO serviceProviderDTO = generateDTO(ssoProvider);
            try {
                status = ssoStub.addRPServiceProvider(serviceProviderDTO);
                String attributeConsumingServiceIndex = getServiceProvider(ssoProvider.getIssuerName()).getAttributeConsumingServiceIndex();
                serviceProvider = generateSPCreate(ssoProvider);
                appMgtStub.createApplication(serviceProvider);
                serviceProvider = appMgtStub.getApplication(serviceProvider.getApplicationName());
                serviceProvider = generateSPUpdate(ssoProvider, serviceProvider, attributeConsumingServiceIndex,
                        idpName, authenticationStep);
                appMgtStub.updateApplication(serviceProvider);
            } catch (Exception e) {
                log.error("Error adding a new Service Provider", e);
            }
        }
        return status;
    }

    @Override
    public boolean removeProvider(SSOProvider provider) {
        boolean status = false;
        try {
            appMgtStub.deleteApplication(provider.getIssuerName());
            status = true;
        } catch (Exception e) {
            log.error("Error removing Service Provider", e);
        }

        return status;
    }

    @Override
    public boolean updateProvider(WebApp application, String idpName, String authenticationStep, String gatewayUrl) {
        String acsUrl = application.getAcsURL().trim();
        SSOProvider ssoProvider = application.getSsoProviderDetails();
        boolean isUpdated = false;

        if (ssoProvider == null) {
            log.warn("No SSO Configurator details given. Manual setup of SSO Provider required.");
        } else {
            if (acsUrl != null && acsUrl.length() > 0) {
                ssoProvider.setAssertionConsumerURL(acsUrl);
            } else {
                ssoProvider.setAssertionConsumerURL(gatewayUrl);
            }
            SAMLSSOServiceProviderDTO serviceProviderDTO = generateDTO(ssoProvider);
            ServiceProvider serviceProvider = null;
            try {
                serviceProvider = appMgtStub.getApplication(ssoProvider.getIssuerName());
                if (serviceProvider != null) {
                    ssoStub.removeServiceProvider(ssoProvider.getIssuerName());
                    ssoStub.addRPServiceProvider(serviceProviderDTO);
                    updateServiceProvider(ssoProvider, serviceProvider);
                    appMgtStub.updateApplication(serviceProvider);
                    isUpdated = true;
                } else {
                    createProvider(ssoProvider, idpName, authenticationStep);
                }
            } catch (RemoteException e) {
                //An exception is not thrown here in the purpose of continuing in rest of webapp update
                log.error("Error occurred in invoking remote service while updating service provider : " +
                        ssoProvider.getProviderName(), e);
            } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
                //An exception is not thrown here in the purpose of continuing in rest of webapp update
                log.error("Error in invoking IdentityApplicationManagementService while updating the provider : " +
                        ssoProvider.getProviderName(), e);
            } catch (IdentitySAMLSSOConfigServiceIdentityException e) {
                log.error("Error occurred in invoking IdentitySAMLSSOConfigService while updating provider : " +
                        ssoProvider.getIssuerName(), e);
            }
        }
        return isUpdated;
    }

    @Override
    public boolean updateProvider(SSOProvider provider, String idpName, String authenticationStep) {
        SAMLSSOServiceProviderDTO serviceProviderDTO = generateDTO(provider);
        ServiceProvider serviceProvider = null;
        boolean isUpdated = false;

        try {
            serviceProvider = appMgtStub.getApplication(provider.getIssuerName());
            if (serviceProvider != null) {
                ssoStub.removeServiceProvider(provider.getIssuerName());
                ssoStub.addRPServiceProvider(serviceProviderDTO);
                updateServiceProvider(provider, serviceProvider);
                appMgtStub.updateApplication(serviceProvider);
                isUpdated = true;
            } else {
                createProvider(provider, idpName, authenticationStep);
            }
        } catch (RemoteException e) {
            //An exception is not thrown here in the purpose of continuing in rest of webapp update
            log.error("Error occurred in invoking remote service while updating service provider : " +
                    provider.getProviderName(), e);
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            //An exception is not thrown here in the purpose of continuing in rest of webapp update
            log.error("Error in invoking IdentityApplicationManagementService while updating the provider : " +
                    provider.getProviderName(), e);
        } catch (IdentitySAMLSSOConfigServiceIdentityException e) {
            log.error("Error occurred in invoking IdentitySAMLSSOConfigService while updating provider : " +
                    provider.getIssuerName(), e);
        }
        return isUpdated;
    }

    @Override
    public SSOProvider getProvider(String issuerName) {
        try {
            SSOProvider ssoProvider = new SSOProvider();
            ServiceProvider sp = getApplication(issuerName);
            ClaimConfig claimConfig = sp.getClaimConfig();
            if(claimConfig.getClaimMappings() != null) {
                ClaimMapping[] cms = sp.getClaimConfig().getClaimMappings();
                List<String> claims = new ArrayList<String>();
                for(ClaimMapping cm : cms) {
                    claims.add(cm.getLocalClaim().getClaimUri());
                }
                ssoProvider.setClaims(claims.toArray(new String[claims.size()]));
            }
            SAMLSSOServiceProviderDTO samlSPDTO = getIssuer(issuerName);
            ssoProvider.setAssertionConsumerURL(samlSPDTO.getAssertionConsumerUrl());
            ssoProvider.setNameIdFormat(samlSPDTO.getNameIDFormat());
            ssoProvider.setIssuerName(samlSPDTO.getIssuer());

            return ssoProvider;

        } catch (Exception e) {
            log.error("Could not find SSO Provider for issuer '" + issuerName + "'.");
        }

        return null;
    }

    /**
     * Returns IDPs in 'Local & Outbound Authentication Configuration' section of the service provider.
     * @param serviceProviderId
     * @return
     */
    @Override
    public String[] getIdentityProvidersInServiceProvider(String serviceProviderId) {


        try {
            ServiceProvider serviceProvider = appMgtStub.getApplication(serviceProviderId);

            AuthenticationStep[] authenticationSteps = serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();

            List<String> identityProviders = new ArrayList<String>();

            IdentityProvider[] providers;
            if(authenticationSteps != null){
                for(AuthenticationStep step : authenticationSteps){

                    providers = step.getFederatedIdentityProviders();

                    if(providers != null){
                        for(IdentityProvider provider : providers){
                            identityProviders.add(provider.getIdentityProviderName());
                        }
                    }
                }
            }

            String[] identityProvidersArray = new String[identityProviders.size()];
            return identityProviders.toArray(identityProvidersArray);
        } catch (Exception e) {
            log.error(String.format("Error retrieving identity providers for the service provider : '%s'", serviceProviderId));
        }

        return null;
    }

    @Override
    public String[] getAllLocalClaimUris() {
        try {
            return appMgtStub.getAllLocalClaimUris();
        } catch (Exception e) {
            log.error("Error retrieving claims from Service Provider", e);
        }

        return null;
    }

    private ServiceProvider updateServiceProvider(SSOProvider ssoProvider,
            ServiceProvider serviceProvider) {
        serviceProvider.setApplicationName(ssoProvider.getIssuerName());
        updateClaimConfiguration(ssoProvider, serviceProvider);

        return serviceProvider;
    }

    private SAMLSSOServiceProviderDTO getServiceProvider(String issuer) throws AxisFault {
        try {
            SAMLSSOServiceProviderInfoDTO dto = ssoStub.getServiceProviders();
            SAMLSSOServiceProviderDTO[] sps = dto.getServiceProviders();
            for(SAMLSSOServiceProviderDTO sp : sps) {
                if(sp.getIssuer().equals(issuer)) {
                    return sp;
                }
            }
        } catch (Exception e) {
            log.error("Error retrieving service provider information", e);
            throw new AxisFault(e.getMessage(), e);
        }
        return null;

    }

    private ServiceProvider generateSPUpdate(SSOProvider ssoProvider,
            ServiceProvider serviceProvider,
            String attrConsumServiceIndex,
            String idpName,
            String authenticationStep) {
        serviceProvider.setSaasApp(true);
        updateClaimConfiguration(ssoProvider, serviceProvider);

        InboundAuthenticationConfig iac = new InboundAuthenticationConfig();
        InboundAuthenticationRequestConfig iarc = new InboundAuthenticationRequestConfig();
        iarc.setInboundAuthKey(serviceProvider.getApplicationName());
        iarc.setInboundAuthType(AUTH_TYPE);
        if (attrConsumServiceIndex != null && !attrConsumServiceIndex.isEmpty()) {
            Property property = new Property();
            property.setName("attrConsumServiceIndex");
            property.setValue(attrConsumServiceIndex);
            Property[] properties = { property };
            iarc.setProperties(properties);
        }
        iac.addInboundAuthenticationRequestConfigs(iarc);
        serviceProvider.setInboundAuthenticationConfig(iac);

        setLocalAndOutBoundAuthentication(serviceProvider, idpName, authenticationStep);
        return serviceProvider;
    }

    private void setLocalAndOutBoundAuthentication(ServiceProvider serviceProvider, String idpName,
            String authenticationStep) {

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

    private ServiceProvider updateClaimConfiguration(SSOProvider ssoProvider,
            ServiceProvider serviceProvider) {
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

    private ServiceProvider generateSPCreate(SSOProvider provider) {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(provider.getIssuerName());
        serviceProvider.setDescription(APP_DESC);

        return serviceProvider;
    }

    /**
     * Translates the SSOProvider to SAMLSSOServiceProviderDTO.
     *
     * @param provider the Single Sig On Provider.
     * @return translated SAMLSSOServiceProviderDTO
     */
    protected SAMLSSOServiceProviderDTO generateDTO(SSOProvider provider) {
        SAMLSSOServiceProviderDTO dto = new SAMLSSOServiceProviderDTO();

        dto.setIssuer(provider.getIssuerName());
        dto.setAssertionConsumerUrl(provider.getAssertionConsumerURL());
        dto.setCertAlias(null);

        dto.setNameIDFormat(provider.getNameIdFormat());
        if (dto.getNameIDFormat() != null) {
            dto.setNameIDFormat(dto.getNameIDFormat().replace(":", "/"));
        }

        dto.setDoSingleLogout(true);
        dto.setRequestedClaims(provider.getClaims());
        dto.setEnableAttributesByDefault(true);


//        dto.setEnableAttributeProfile(true);

        dto.setIdPInitSSOEnabled(true);

        return dto;
    }

    private SAMLSSOServiceProviderDTO getIssuer(String issuerName) {
        try {
            SAMLSSOServiceProviderInfoDTO dto = ssoStub.getServiceProviders();
            SAMLSSOServiceProviderDTO[] sps = dto.getServiceProviders();
            for(SAMLSSOServiceProviderDTO sp : sps) {
                if(sp.getIssuer().equals(issuerName)) {
                    return sp;
                }
            }
        } catch (Exception e) {
            log.error("Error retrieving SSO Issuer information", e);
        }
        return null;
    }

    private ServiceProvider getApplication(String appName) throws Exception {
        try {
            return appMgtStub.getApplication(appName);
        } catch (Exception e) {
            throw e;
        }
    }
}
