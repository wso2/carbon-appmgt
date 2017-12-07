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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.gateway.dto.stub.WebAppData;
import org.wso2.carbon.appmgt.gateway.stub.AppGatewayAdminAppManagementExceptionException;
import org.wso2.carbon.appmgt.gateway.stub.AppGatewayAdminStub;
import org.wso2.carbon.appmgt.impl.dto.Environment;
import org.wso2.carbon.appmgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.appmgt.impl.template.APITemplateException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;
import java.rmi.RemoteException;

/**
 * Client class to communicate with AppGatewayAdmin service.
 */
public class AppGatewayAdminClient extends AbstractAPIGatewayAdminClient {
    private AppGatewayAdminStub appGatewayAdminStub;
    private String qualifiedName;
    private String qualifiedDefaultApiName;
    private Environment environment;

    /**
     * Constructor.
     *
     * @param apiId       {@link APIIdentifier} object
     * @param environment {@link Environment} object
     * @throws AppManagementException on errors while trying to initiate AppGatewayAdminStub
     */
    public AppGatewayAdminClient(APIIdentifier apiId, Environment environment) throws AppManagementException {
        String providerName = apiId.getProviderName();
        String appName = apiId.getProviderName();
        String appVersion = apiId.getProviderName();
        this.qualifiedName = providerName + "--" + appName + ":v" + appVersion;
        this.qualifiedDefaultApiName = providerName + "--" + appName;
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
     * @param builder      {@link APITemplateBuilder} object
     * @param apiId        {@link APIIdentifier} object
     * @param tenantDomain Tenant domain
     * @throws AppManagementException on errors while trying to add versioned WebApp
     */
    public void addVersionedWebApp(APITemplateBuilder builder, APIIdentifier apiId, String tenantDomain)
            throws AppManagementException {
        String appName = apiId.getApiName();
        String appVersion = apiId.getVersion();
        String appProvider = apiId.getProviderName();
        try {
            String apiConfig = builder.getConfigStringForVersionedWebAppTemplate(environment);
            if (!StringUtils.isEmpty(tenantDomain) && !tenantDomain.equals(MultitenantConstants
                                                                                   .SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.addVersionedWebAppForTenant(appProvider, appName, appVersion, apiConfig,
                                                                tenantDomain);
            } else {
                appGatewayAdminStub.addVersionedWebApp(appProvider, appName, appVersion, apiConfig);
            }
        } catch (RemoteException e) {
            String errorMsg = "Error occurred while adding new WebApp. App Name : " + appName + " App Version: " +
                    appVersion;
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            String errorMsg = "Error while occurred adding new WebApp. App Name : " + appName + " App Version: " +
                    appVersion;
            throw new AppManagementException(errorMsg, e);
        } catch (APITemplateException e) {
            String errorMsg = "Error while occurred adding new WebApp. App Name : " + appName + " App Version: " +
                    appVersion;
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Return versioned web app configuration from the gateway.
     *
     * @param apiId        {@link APIIdentifier} object
     * @param tenantDomain Tenant domain
     * @return {@link WebAppData} object
     * @throws AppManagementException on errors while trying to get versioned WebApps
     */
    public WebAppData getVersionedWebApp(APIIdentifier apiId, String tenantDomain) throws AppManagementException {
        String appName = apiId.getApiName();
        String appVersion = apiId.getVersion();
        String appProvider = apiId.getProviderName();
        WebAppData appData;
        try {
            if (!StringUtils.isEmpty(tenantDomain) && !tenantDomain.equals(MultitenantConstants
                                                                                   .SUPER_TENANT_DOMAIN_NAME)) {
                appProvider = appProvider.replace("-AT-", "@");
                appData = appGatewayAdminStub.getVersionedWebAppForTenant(appProvider, appName, appVersion,
                                                                          tenantDomain);
            } else {
                appData = appGatewayAdminStub.getVersionedWebApp(appProvider, appName, appVersion);
            }
            return appData;
        } catch (RemoteException e) {
            String errorMsg = "Error occurred while obtaining WebApp information from gateway. App Name : " + appName +
                    " App Version : " + appVersion;
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            String errorMsg = "Error occurred while obtaining WebApp information from gateway. App Name : " + appName
                    + " App Version : " + appVersion;
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Update versioned web app configuration in the gateway.
     *
     * @param builder      {@link APITemplateBuilder} object
     * @param apiId        {@link APIIdentifier} object
     * @param tenantDomain Tenant domain
     * @throws AppManagementException on errors while trying to update WebApp
     */
    public void updateVersionedWebApp(APITemplateBuilder builder, APIIdentifier apiId, String tenantDomain)
            throws AppManagementException {
        String appName = apiId.getApiName();
        String appVersion = apiId.getVersion();
        String appProvider = apiId.getProviderName();
        try {
            String apiConfig = builder.getConfigStringForVersionedWebAppTemplate(environment);
            if (!StringUtils.isEmpty(tenantDomain) && !tenantDomain.equals(MultitenantConstants
                                                                                   .SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.updateVersionedWebAppForTenant(appProvider, appName, appVersion, apiConfig,
                                                                   tenantDomain);
            } else {
                appGatewayAdminStub.updateVersionedWebApp(appProvider, appName, appVersion, apiConfig);
            }
        } catch (RemoteException e) {
            String errorMsg = "Error occurred while updating WebApp. App Name : " + appName + " App Version : " +
                    appVersion;
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            String errorMsg = "Error occurred while updating WebApp. App Name : " + appName + " App Version : " +
                    appVersion;
            throw new AppManagementException(errorMsg, e);
        } catch (APITemplateException e) {
            String errorMsg = "Error occurred while updating WebApp. App Name : " + appName + " App Version : " +
                    appVersion;
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Delete versioned web app configuration from the gateway.
     *
     * @param apiId        {@link APIIdentifier} object
     * @param tenantDomain Tenant domain
     * @throws AppManagementException on errors while trying to delete versioned WebApp
     */
    public void deleteVersionedWebApp(APIIdentifier apiId, String tenantDomain) throws AppManagementException {
        String appName = apiId.getApiName();
        String appVersion = apiId.getVersion();
        String appProvider = apiId.getProviderName();
        try {
            if (!StringUtils.isEmpty(tenantDomain) && !tenantDomain.equals(MultitenantConstants
                                                                                   .SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.deleteVersionedWebAppForTenant(appProvider, appName, appVersion, tenantDomain);
            } else {
                appGatewayAdminStub.deleteVersionedWebApp(appProvider, appName, appVersion);
            }
        } catch (RemoteException e) {
            String errorMsg =
                    "Error occurred while updating WebApp. App Name : " + appName + " App Version : " + appVersion;
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            String errorMsg = "Error occurred while updating WebApp. App Name : " + appName + " App Version : " +
                    appVersion;
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Add non-versioned web app configuration to the gateway.
     *
     * @param builder      {@link APITemplateBuilder} object
     * @param apiId        {@link APIIdentifier} object
     * @param tenantDomain Tenant domain
     * @throws AppManagementException on errors while trying to add non versioned WebApps
     */
    public void addNonVersionedWebApp(APITemplateBuilder builder, APIIdentifier apiId, String tenantDomain)
            throws AppManagementException {
        String appName = apiId.getApiName();
        String appVersion = apiId.getVersion();
        String appProvider = apiId.getProviderName();
        try {
            String apiConfig = builder.getConfigStringForNonVersionedWebAppTemplate();
            if (!StringUtils.isEmpty(tenantDomain) && !tenantDomain.equals(MultitenantConstants
                                                                                   .SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.addNonVersionedWebAppForTenant(appProvider, appName, appVersion, apiConfig,
                                                                   tenantDomain);
            } else {
                appGatewayAdminStub.addNonVersionedWebApp(appProvider, appName, appVersion, apiConfig);
            }
        } catch (RemoteException e) {
            String errorMsg = "Error occurred publishing non-versioned web app to the gateway. App Name : " + appName;
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            String errorMsg = "Error occurred publishing non-versioned web app to the gateway. App Name : " + appName;
            throw new AppManagementException(errorMsg, e);
        } catch (APITemplateException e) {
            String errorMsg = "Error occurred publishing non-versioned web app to the gateway. App Name : " + appName;
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Update non-versioned web app configuration in the gateway.
     *
     * @param builder      {@link APITemplateBuilder} object
     * @param apiId        {@link APIIdentifier} object
     * @param tenantDomain Tenant domain
     * @throws AppManagementException on errors while trying to update non versioned WebApps
     */
    public void updateNonVersionedWebApp(APITemplateBuilder builder, APIIdentifier apiId, String tenantDomain)
            throws AppManagementException {
        String appName = apiId.getApiName();
        String appVersion = apiId.getVersion();
        String appProvider = apiId.getProviderName();
        try {
            String apiConfig = builder.getConfigStringForNonVersionedWebAppTemplate();
            if (!StringUtils.isEmpty(tenantDomain) && !tenantDomain.equals(MultitenantConstants
                                                                                   .SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.updateNonVersionedWebAppForTenant(appProvider, appName, appVersion, apiConfig,
                                                                      tenantDomain);
            } else {
                appGatewayAdminStub.updateNonVersionedWebApp(appProvider, appName, appVersion, apiConfig);
            }
        } catch (RemoteException e) {
            String errorMsg = "Error occurred while updating non-versioned web app in the gateway. App Name : " +
                    appName;
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            String errorMsg = "Error occurred while updating non-versioned web app in the gateway. App Name : " +
                    appName;
            throw new AppManagementException(errorMsg, e);
        } catch (APITemplateException e) {
            String errorMsg = "Error occurred while updating non-versioned web app in the gateway. App Name : " +
                    appName;
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Delete non-versioned web app configuration form the gateway.
     *
     * @param apiId        {@link APIIdentifier} object
     * @param tenantDomain Tenant domain
     * @throws AppManagementException on errors while trying to delete non versioned WebApp
     */
    public void deleteNonVersionedWebApp(APIIdentifier apiId, String tenantDomain) throws AppManagementException {
        String appName = apiId.getApiName();
        String appVersion = apiId.getVersion();
        String appProvider = apiId.getProviderName();
        try {
            if (!StringUtils.isEmpty(tenantDomain) && !tenantDomain.equals(MultitenantConstants
                                                                                   .SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.deleteNonVersionedWebAppForTenant(appProvider, appName, appVersion, tenantDomain);
            } else {
                appGatewayAdminStub.deleteNonVersionedWebApp(appProvider, appName, appVersion);
            }
        } catch (RemoteException e) {
            String errorMsg = "Error occurred while deleting non-versioned web app from the gateway. App Name : " +
                    appName + " App Version : " + appVersion;
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            String errorMsg = "Error occurred while deleting non-versioned web app from the gateway. App Name : " +
                    appName + " App Version : " + appVersion;
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Return the non-versioned web app configuration from the gateway.
     *
     * @param apiId        {@link APIIdentifier} object
     * @param tenantDomain Tenant domain
     * @return {@link WebAppData} object
     * @throws AppManagementException on errors while trying to get non versioned WebApp
     */
    public WebAppData getNonVersionedWebAppData(APIIdentifier apiId, String tenantDomain)
            throws AppManagementException {
        String appName = apiId.getApiName();
        String appVersion = apiId.getVersion();
        String appProvider = apiId.getProviderName();
        try {
            WebAppData appData;
            if (!StringUtils.isEmpty(tenantDomain) && !tenantDomain.equals(MultitenantConstants
                                                                                   .SUPER_TENANT_DOMAIN_NAME)) {
                appData = appGatewayAdminStub.getNonVersionedWebAppDataForTenant(appProvider, appName, appVersion,
                                                                                 tenantDomain);
            } else {
                appData = appGatewayAdminStub.getNonVersionedWebAppData(appProvider, appName, appVersion);
            }
            return appData;
        } catch (RemoteException e) {
            String errorMsg = "Error occurred while obtaining non-versioned web app information from gateway. App " +
                    "Name : " + appName;
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            String errorMsg = "Error occurred while obtaining non-versioned web app information from gateway. App " +
                    "Name : " + appName;
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Deploy the sequence to the gateway.
     *
     * @param sequence     - The sequence element , which to be deployed in synapse
     * @param tenantDomain - The Tenant Domain
     * @throws AppManagementException on errors while occurred in deploy sequences
     */
    public void addSequence(OMElement sequence, String tenantDomain) throws AppManagementException {
        try {
            StringWriter writer = new StringWriter();
            sequence.serializeAndConsume(writer);
            String addedSequence = writer.toString();
            if (!StringUtils.isEmpty(tenantDomain) && !tenantDomain.equals(MultitenantConstants
                                                                                   .SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.addSequenceForTenant(addedSequence, tenantDomain);
            } else {
                appGatewayAdminStub.addSequence(addedSequence);
            }
        } catch (RemoteException e) {
            String errorMsg = "Error occurred while adding new sequence. Tenant : " + tenantDomain;
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            String errorMsg = "Error occurred while adding new sequence. Tenant : " + tenantDomain;
            throw new AppManagementException(errorMsg, e);
        } catch (XMLStreamException e) {
            String errorMsg = "Error occurred while adding new sequence. Tenant : " + tenantDomain;
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Undeploy the sequence from gateway.
     *
     * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration
     * @param tenantDomain - The Tenant Domain
     * @throws AppManagementException on errors while trying to undeploy sequence
     */
    public void deleteSequence(String sequenceName, String tenantDomain) throws AppManagementException {
        try {
            if (!StringUtils.isEmpty(tenantDomain) && !tenantDomain.equals(MultitenantConstants
                                                                                   .SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.deleteSequenceForTenant(sequenceName, tenantDomain);
            } else {
                appGatewayAdminStub.deleteSequence(sequenceName);
            }
        } catch (RemoteException e) {
            String errorMsg = "Error occurred while deleting sequence. Sequence Name : " + sequenceName;
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            String errorMsg = "Error occurred while deleting sequence. Sequence Name : " + sequenceName;
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Get the sequence from gateway.
     *
     * @param sequenceName - The sequence name
     * @param tenantDomain - The Tenant Domain
     * @throws AppManagementException on errors while trying to get sequences
     */
    public OMElement getSequence(String sequenceName, String tenantDomain) throws AppManagementException {
        try {
            if (!StringUtils.isEmpty(tenantDomain) && !tenantDomain.equals(MultitenantConstants
                                                                                   .SUPER_TENANT_DOMAIN_NAME)) {
                return (OMElement) appGatewayAdminStub.getSequenceForTenant(sequenceName, tenantDomain);
            } else {
                return (OMElement) appGatewayAdminStub.getSequence(sequenceName);
            }
        } catch (RemoteException e) {
            String errorMsg = "Error occurred while retrieving the sequence. Sequence Name : " + sequenceName;
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            String errorMsg = "Error occurred retrieving the sequence. Sequence Name : " + sequenceName;
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Check whether the sequence is exiting or not.
     *
     * @param sequenceName Sequence name
     * @param tenantDomain Tenant domain
     * @return whether sequence exists or not
     * @throws AppManagementException on errors while trying to check whether sequence exist or not
     */
    public boolean isExistingSequence(String sequenceName, String tenantDomain) throws AppManagementException {
        try {
            if (!StringUtils.isEmpty(tenantDomain) && !tenantDomain.equals(MultitenantConstants
                                                                                   .SUPER_TENANT_DOMAIN_NAME)) {
                return appGatewayAdminStub.isExistingSequenceForTenant(sequenceName, tenantDomain);
            } else {
                return appGatewayAdminStub.isExistingSequence(sequenceName);
            }
        } catch (RemoteException e) {
            String errorMsg =
                    "Error occurred while checking for existence of sequence : " + sequenceName + " in tenant " +
                            tenantDomain;
            throw new AppManagementException(errorMsg, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            String errorMsg =
                    "Error occurred while checking for existence of sequence : " + sequenceName + " in tenant " +
                            tenantDomain;
            throw new AppManagementException(errorMsg, e);
        }
    }
}