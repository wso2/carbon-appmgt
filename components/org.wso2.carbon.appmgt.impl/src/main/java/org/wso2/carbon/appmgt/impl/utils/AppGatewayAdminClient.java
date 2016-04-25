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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.appmgt.impl.utils;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.gateway.dto.stub.AppData;
import org.wso2.carbon.appmgt.gateway.stub.AppGatewayAdminAppManagementExceptionException;
import org.wso2.carbon.appmgt.gateway.stub.AppGatewayAdminStub;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.appmgt.impl.template.APITemplateException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.appmgt.impl.dto.Environment;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;

import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;
import java.rmi.RemoteException;

public class AppGatewayAdminClient extends AbstractAPIGatewayAdminClient {
    private AppGatewayAdminStub appGatewayAdminStub;
    private String qualifiedName;
    private String qualifiedDefaultApiName;
    private Environment environment;

    public AppGatewayAdminClient(APIIdentifier apiId, Environment environment) throws AppManagementException {
        this.qualifiedName = apiId.getProviderName() + "--" + apiId.getApiName() + ":v" + apiId.getVersion();
        this.qualifiedDefaultApiName = apiId.getProviderName() + "--" + apiId.getApiName();
        String providerDomain = apiId.getProviderName();
        try {
            appGatewayAdminStub = new AppGatewayAdminStub(null, environment.getServerURL() + "AppGatewayAdmin");
            setup(appGatewayAdminStub, environment);
            this.environment = environment;
        } catch (AxisFault ex) {
            throw new AppManagementException("Exception is occurred in app gateway admin client.", ex);
        }
    }

    /**
     * Add versioned web app configuration to the gateway.
     *
     * @param builder
     * @param tenantDomain
     * @throws AppManagementException on errors.
     */
    public void addVersionedWebApp(APITemplateBuilder builder, APIIdentifier apiId, String tenantDomain)
            throws AppManagementException {
        String errorMsg = "Error while adding new WebApp.";
        try {
            String apiConfig = builder.getConfigStringForVersionedWebAppTemplate(environment);
            if (tenantDomain != null && !("").equals(tenantDomain)
                    && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.addVersionedWebAppForTenant(apiId.getProviderName(), apiId.getApiName(), apiId
                        .getVersion(), apiConfig, tenantDomain);
            } else {
                appGatewayAdminStub.addVersionedWebApp(apiId.getProviderName(), apiId.getApiName(), apiId
                        .getVersion(), apiConfig);
            }
        } catch (RemoteException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (APITemplateException e) {
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Return versioned web app configuration from the gateway.
     *
     * @param tenantDomain
     * @return
     * @throws AppManagementException on errors.
     */
    public AppData getVersionedWebApp(APIIdentifier apiId, String tenantDomain) throws AppManagementException {
        String errorMsg = "Error while obtaining WebApp information from gateway.";
        try {
            AppData appData;
            if (tenantDomain != null && !("").equals(tenantDomain)
                    && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                appData = appGatewayAdminStub.getVersionedWebAppForTenant(apiId.getProviderName(), apiId.getApiName(),
                                                                          apiId.getVersion(), tenantDomain);
            } else {
                appData = appGatewayAdminStub.getVersionedWebApp(apiId.getProviderName(), apiId.getApiName(),
                                                                 apiId.getVersion());
            }
            return appData;
        } catch (RemoteException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Update versioned web app configuration in the gateway
     *
     * @param builder
     * @param tenantDomain
     * @throws AxisFault
     */
    public void updateVersionedWebApp(APITemplateBuilder builder, APIIdentifier apiId, String tenantDomain)
            throws AppManagementException {
        String errorMsg = "Error while updating WebApp";
        try {
            String apiConfig = builder.getConfigStringForVersionedWebAppTemplate(environment);
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {

                appGatewayAdminStub.updateVersionedWebAppForTenant(apiId.getProviderName(), apiId.getApiName(), apiId
                        .getVersion(), apiConfig, tenantDomain);
            } else {
                appGatewayAdminStub.updateVersionedWebApp(apiId.getProviderName(), apiId.getApiName(), apiId
                        .getVersion(), apiConfig);
            }
        } catch (RemoteException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (APITemplateException e) {
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Delete versioned web app configuration from the gateway
     *
     * @param tenantDomain
     * @throws AppManagementException on errors.
     */
    public void deleteVersionedWebApp(APIIdentifier apiId, String tenantDomain) throws AppManagementException {
        String errorMsg = "Error while updating WebApp";
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.deleteVersionedWebAppForTenant(apiId.getProviderName(), apiId.getApiName(), apiId
                        .getVersion(), tenantDomain);
            } else {
                appGatewayAdminStub.deleteVersionedWebApp(apiId.getProviderName(), apiId.getApiName(), apiId
                        .getVersion());
            }
        } catch (RemoteException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Add non-versioned web app configuration to the gateway.
     *
     * @param builder
     * @param tenantDomain
     * @throws AppManagementException on errors.
     */
    public void addNonVersionedWebApp(APITemplateBuilder builder, APIIdentifier apiId, String tenantDomain)
            throws AppManagementException {
        String errorMsg = "Error publishing non-versioned web app to the gateway";
        try {
            String apiConfig = builder.getConfigStringForNonVersionedWebAppTemplate();
            if (tenantDomain != null && !("").equals(tenantDomain)
                    && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.addNonVersionedWebAppForTenant(apiId.getProviderName(), apiId.getApiName(), apiId
                        .getVersion(), apiConfig, tenantDomain);
            } else {
                appGatewayAdminStub.addNonVersionedWebApp(apiId.getProviderName(), apiId.getApiName(), apiId
                        .getVersion(), apiConfig);
            }
        } catch (RemoteException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (APITemplateException e) {
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Update non-versioned web app configuration in the gateway
     *
     * @param builder
     * @param tenantDomain
     * @throws AppManagementException on errors.
     */
    public void updateNonVersionedWebApp(APITemplateBuilder builder, APIIdentifier apiId, String tenantDomain)
            throws AppManagementException {
        String errorMsg = "Error while updating non-versioned web app in the gateway";
        try {
            String apiConfig = builder.getConfigStringForNonVersionedWebAppTemplate();
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.updateNonVersionedWebAppForTenant(apiId.getProviderName(), apiId.getApiName(), apiId
                        .getVersion(), apiConfig, tenantDomain);
            } else {
                appGatewayAdminStub.updateNonVersionedWebApp(apiId.getProviderName(), apiId.getApiName(), apiId
                        .getVersion(), apiConfig);
            }
        } catch (RemoteException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (APITemplateException e) {
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Delete non-versioned web app configuration form the gateway
     *
     * @param tenantDomain
     * @throws AppManagementException on errors.
     */
    public void deleteNonVersionedWebApp(APIIdentifier apiId, String tenantDomain) throws AppManagementException {
        String errorMsg = "Error while deleting non-versioned web app from the gateway";
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.deleteNonVersionedWebAppForTenant(apiId.getProviderName(), apiId.getApiName(), apiId
                        .getVersion(), tenantDomain);
            } else {
                appGatewayAdminStub.deleteNonVersionedWebApp(apiId.getProviderName(), apiId.getApiName(), apiId
                        .getVersion());
            }
        } catch (RemoteException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Return the non-versioned web app configuration from the gateway.
     *
     * @param tenantDomain
     * @return
     * @throws AppManagementException on errors.
     */
    public AppData getNonVersionedWebAppData(APIIdentifier apiId, String tenantDomain) throws AppManagementException {
        String errorMsg = "Error while obtaining non-versioned web app information from gateway";
        try {
            AppData appData;
            if (tenantDomain != null && !("").equals(tenantDomain)
                    && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                appData = appGatewayAdminStub.getNonVersionedWebAppDataForTenant(apiId.getProviderName(),
                                                                                 apiId.getApiName(),
                                                                                 apiId.getVersion(), tenantDomain);
            } else {
                appData = appGatewayAdminStub.getNonVersionedWebAppData(apiId.getProviderName(), apiId.getApiName(),
                                                                        apiId.getVersion());
            }
            return appData;
        } catch (RemoteException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Deploy the sequence to the gateway.
     *
     * @param sequence     - The sequence element , which to be deployed in synapse
     * @param tenantDomain - The Tenant Domain
     * @throws AppManagementException on errors.
     */
    public void addSequence(OMElement sequence, String tenantDomain) throws AppManagementException {
        String errorMsg = "Error while adding new sequence";
        try {
            StringWriter writer = new StringWriter();
            sequence.serializeAndConsume(writer);
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                appGatewayAdminStub.addSequenceForTenant(writer.toString(), tenantDomain);
            } else {
                appGatewayAdminStub.addSequence(writer.toString());
            }
        } catch (RemoteException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (XMLStreamException e) {
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Undeploy the sequence from gateway.
     *
     * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration
     * @param tenantDomain - The Tenant Domain
     * @throws AppManagementException on errors.
     */
    public void deleteSequence(String sequenceName, String tenantDomain) throws AppManagementException {
        String errorMsg = "Error while deleting sequence";
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                appGatewayAdminStub.deleteSequenceForTenant(sequenceName, tenantDomain);
            } else {
                appGatewayAdminStub.deleteSequence(sequenceName);
            }
        } catch (RemoteException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Get the sequence from gateway.
     *
     * @param sequenceName - The sequence name,
     * @param tenantDomain - The Tenant Domain
     * @throws AppManagementException on errors.
     */
    public OMElement getSequence(String sequenceName, String tenantDomain) throws AppManagementException {
        String errorMsg = "Error while retrieving the sequence";
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return (OMElement) appGatewayAdminStub.getSequenceForTenant(sequenceName, tenantDomain);
            } else {
                return (OMElement) appGatewayAdminStub.getSequence(sequenceName);
            }
        } catch (RemoteException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Check whether the sequence is exiting or not.
     * @param sequenceName
     * @param tenantDomain
     * @return
     * @throws AppManagementException on errors.
     */
    public boolean isExistingSequence(String sequenceName, String tenantDomain) throws AppManagementException {
        String errorMsg = "Error while checking for existence of sequence : " + sequenceName + " in tenant " +
                tenantDomain;
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return appGatewayAdminStub.isExistingSequenceForTenant(sequenceName, tenantDomain);
            } else {
                return appGatewayAdminStub.isExistingSequence(sequenceName);
            }
        } catch (RemoteException e) {
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Encrypt the plain text password.
     *
     * @param plainTextPass plain text password
     * @return encrypted password
     * @throws AppManagementException
     */
    private String doEncryption(String plainTextPass) throws AppManagementException {
        String errorMsg = "Failed to encrypt the secured endpoint password ,";
        String encodedValue = null;
        try {
            encodedValue = appGatewayAdminStub.doEncryption(plainTextPass);

        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException(errorMsg + e.getMessage(), e);
        } catch (RemoteException e) {
            throw new AppManagementException(errorMsg + e.getMessage(), e);
        }
        return encodedValue;
    }
}