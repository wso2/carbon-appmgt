/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
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

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

import org.wso2.carbon.appmgt.api.model.SSOProvider;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.idp.sso.SSOConfiguratorUtil;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceStub;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderInfoDTO;

public class IS460SAMLSSOConfigurator extends ISBaseSAMLSSOConfigurator implements SSOConfigurator {

    private static Log log = LogFactory.getLog(IS460SAMLSSOConfigurator.class);

    private IdentitySAMLSSOConfigServiceStub stub;
    private Map<String, String> parameters;
    private String cookie;

    @Override
    public void init(Map<String, String> configuration) {
        this.parameters = configuration;
        try {
            cookie = login();
            String serviceURL = parameters.get(SERVER_URL) + "/services/IdentitySAMLSSOConfigService";
            stub = new IdentitySAMLSSOConfigServiceStub(serviceURL);
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (Exception ex) {
            log.error("Error initializing WSO2 SAML SSO Configurator", ex);
        }
    }

    @Override
    public boolean createProvider(SSOProvider provider) {
        SAMLSSOServiceProviderDTO serviceProviderDTO = generateDTO(provider);
        boolean status = false;
        try {
            status = stub.addRPServiceProvider(serviceProviderDTO);
        } catch (Exception e) {
            log.error("Error adding a new Service Provider", e);
        }
        return status;
    }

    @Override
    public boolean removeProvider(SSOProvider provider) {
        boolean status = false;
        try {
            status = stub.removeServiceProvider(provider.getIssuerName());
        } catch (Exception e) {
            log.error("Error removing Service Provider", e);
        }

        return status;
    }

    @Override
    public boolean updateProvider(SSOProvider provider) {
        removeProvider(provider);
        return createProvider(provider);
    }

    @Override
    public String[] getAllClaims() {
        try {
            return stub.getClaimURIs();
        } catch (Exception e) {
            log.error("Error retrieving claims from Service Provider", e);
        }

        return null;
    }

    @Override
    public boolean isAvailable() throws Exception {
        String host = parameters.get(IS460SAMLSSOConfigurator.SERVER_URL);
        host = host.substring(host.indexOf("/") + 2);
        int port = Integer.valueOf(host.split(":")[1]);
        host = host.split(":")[0];
        return SSOConfiguratorUtil.isUp(host, port);
    }

    @Override
    public SSOProvider getProvider(String issuerName) {
        try {
            SSOProvider ssoProvider = new SSOProvider();
            SAMLSSOServiceProviderDTO samlSPDTO = getIssuer(issuerName);
            ssoProvider.setClaims(samlSPDTO.getRequestedClaims());
            ssoProvider.setAssertionConsumerURL(samlSPDTO.getAssertionConsumerUrl());
            ssoProvider.setNameIdFormat(samlSPDTO.getNameIDFormat());
            ssoProvider.setLogoutUrl(samlSPDTO.getSloResponseURL());
            ssoProvider.setIssuerName(samlSPDTO.getIssuer());

            return ssoProvider;

        } catch (Exception e) {
            log.error("Could not find SSO Provider for issuer '" + issuerName + "'.");
        }

        return null;
    }

    @Override
    public String[] getIdentityProvidersInServiceProvider(String serviceProviderId) {
        throw new UnsupportedOperationException("This operation is not supported for IS 4.6.0");
    }

    private SAMLSSOServiceProviderDTO getIssuer(String issuerName) {
        try {
            SAMLSSOServiceProviderInfoDTO dto = stub.getServiceProviders();
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

    private String login() throws AxisFault {
        if (parameters.get(SERVER_URL) == null || parameters.get(USERNAME) == null || parameters.get(PASSWORD) == null) {
            throw new AxisFault("SSO Configurator authentication details unspecified");
        }

        String host;
        try {
            host = new URL(parameters.get(SERVER_URL)).getHost();
        } catch (MalformedURLException e) {
            throw new AxisFault("SSO Configurator provider URL is malformed", e);
        }

        AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(null, parameters.get(SERVER_URL) + "/services/AuthenticationAdmin");
        ServiceClient client = authAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            authAdminStub.login(parameters.get(USERNAME), parameters.get(PASSWORD), host);
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

    public SAMLSSOServiceProviderInfoDTO getRegisteredServiceProviders() throws AxisFault {
        try {
            SAMLSSOServiceProviderInfoDTO spInfo =  stub.getServiceProviders();
            return spInfo;
        } catch (Exception e) {
            log.error("Error retrieving service provider information", e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    public ArrayList<String> getCertAlias() throws AxisFault {
        ArrayList<String> certAliasList = new ArrayList<String>();
        String[] certAliases;
        try{
            certAliases = stub.getCertAliasOfPrimaryKeyStore();
            for(String alias : certAliases){
                certAliasList.add(alias);
            }
        } catch (Exception e) {
            log.error("Error retrieving Cert Aliases", e);
            throw new AxisFault(e.getMessage(), e);
        }
        return certAliasList;
    }

    public boolean removeServiceProvider(String issuerName) throws AxisFault {
        try {
            return stub.removeServiceProvider(issuerName);
        } catch (Exception e) {
            log.error("Error when removing the service provider", e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    @Override
    public boolean updateProvider(WebApp application) {
        return false;
    }

    @Override
    public boolean createProvider(WebApp webApp) {
        return false;
    }
}
