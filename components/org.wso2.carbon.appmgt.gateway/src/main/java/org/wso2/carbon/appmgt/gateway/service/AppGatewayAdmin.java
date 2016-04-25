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
package org.wso2.carbon.appmgt.gateway.service;


import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.gateway.dto.AppData;
import org.wso2.carbon.appmgt.gateway.dto.ResourceData;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.gateway.utils.MediationSecurityAdminServiceClient;
import org.wso2.carbon.appmgt.gateway.utils.RESTAPIAdminClient;
import org.wso2.carbon.appmgt.gateway.utils.SequenceAdminServiceClient;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;

import java.util.ArrayList;
import java.util.List;

public class AppGatewayAdmin extends AbstractAdmin {
    private static Log log = LogFactory.getLog(AppGatewayAdmin.class);

    /**
     * Add versioned webapp configuration to the gateway for tenant users.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @param appConfig String.
     * @param tenantDomain String.
     * @throws AppManagementException on Errors.
     */
    public void addVersionedWebAppForTenant(String appProviderName, String appName, String version, String appConfig,
                                            String tenantDomain)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.addVersionedWebAppForTenant(appConfig, tenantDomain);
    }

    /**
     * Add versioned webapp configuration to the gateway for super tenant users.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @param appConfig String.
     * @throws AppManagementException on Errors.
     */
    public void addVersionedWebApp(String appProviderName, String appName, String version, String appConfig)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.addVersionedWebApp(appConfig);
    }

    /**
     * Return versioned webapp configuration from the gateway for tenant user.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @param tenantDomain String.
     * @return versioned webapp configuration data.
     * @throws AppManagementException on errors.
     */
    public AppData getVersionedWebAppForTenant(String appProviderName, String appName, String version,
                                               String tenantDomain) throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        APIData apiData = restClient.getVersionedWebAppForTenant(tenantDomain);
        return convert(apiData);
    }

    /**
     * Return versioned webapp configuration from the gateway for super tenant user.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @return versioned webapp configuration data.
     * @throws AppManagementException on errors.
     */
    public AppData getVersionedWebApp(String appProviderName, String appName, String version)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        APIData apiData = restClient.getVersionedWebApp();
        return convert(apiData);
    }

    /**
     * Update versioned webapp configuration in the gateway for tenant users.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @param appConfig String.
     * @param tenantDomain String.
     * @throws AppManagementException on errors.
     */
    public void updateVersionedWebAppForTenant(String appProviderName, String appName, String version, String appConfig,
                                               String tenantDomain) throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.updateVersionedWebAppForTenant(appConfig, tenantDomain);
    }

    /**
     * Update versioned webapp configuration in the gateway for super tenant users.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @param appConfig String.
     * @throws AppManagementException on errors.
     */
    public void updateVersionedWebApp(String appProviderName, String appName, String version, String appConfig)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.updateVersionedWebApp(appConfig);
    }

    /**
     * Delete versioned webapp configuration from the gateway for tenant users.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @param tenantDomain String.
     * @throws AppManagementException on errors.
     */
    public void deleteVersionedWebAppForTenant(String appProviderName, String appName, String version,
                                               String tenantDomain) throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.deleteVersionedWebAppForTenant(tenantDomain);
    }

    /**
     * Delete versioned webapp configuration from the gateway for super tenant users.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @throws AppManagementException on errors.
     */
    public void deleteVersionedWebApp(String appProviderName, String appName, String version)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.deleteVersionedWebApp();
    }

    /**
     * Add non versioned webapp configuration to the gateway for tenant users.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @param appConfig String.
     * @param tenantDomain String.
     * @throws AppManagementException on errors.
     */
    public void addNonVersionedWebAppForTenant(String appProviderName, String appName, String version, String appConfig,
                                               String tenantDomain) throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.addNonVersionedWebAppForTenant(appConfig, tenantDomain);
    }

    /**
     * Add non versioned webapp configuration to the gateway for super tenant users.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @param appConfig String.
     * @throws AppManagementException on errors.
     */
    public void addNonVersionedWebApp(String appProviderName, String appName, String version, String appConfig)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.addNonVersionedWebApp(appConfig);
    }

    /**
     * Update non versioned webapp configuration in the gateway for tenant users.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @param appConfig String.
     * @param tenantDomain String.
     * @throws AppManagementException on errors.
     */
    public void updateNonVersionedWebAppForTenant(String appProviderName, String appName, String version,
                                                  String appConfig, String tenantDomain) throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.updateNonVersionedWebAppForTenant(appConfig, tenantDomain);
    }

    /**
     * Update non versioned webapp configuration in the gateway for super tenant users.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @param appConfig String.
     * @throws AppManagementException on errors.
     */
    public void updateNonVersionedWebApp(String appProviderName, String appName, String version, String appConfig)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.updateNonVersionedWebApp(appConfig);
    }

    /**
     * Delete non versioned webapp configuration from the gateway for tenant users.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @param tenantDomain String.
     * @throws AppManagementException on errors.
     */
    public void deleteNonVersionedWebAppForTenant(String appProviderName, String appName, String version,
                                                  String tenantDomain)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.deleteNonVersionedWebAppForTenant(tenantDomain);
    }

    /**
     * Delete non versioned webapp configuration from the gateway for super tenant users.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @throws AppManagementException on errors.
     */
    public void deleteNonVersionedWebApp(String appProviderName, String appName, String version)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.deleteNonVersionedWebApp();
    }

    /**
     * Get non versioned webapp configuration from the gateway for tenant users.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @param tenantDomain String.
     * @return non versioned webapp configuration data.
     * @throws AppManagementException on errors.
     */
    public AppData getNonVersionedWebAppDataForTenant(String appProviderName, String appName, String version,
                                                      String tenantDomain) throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        APIData apiData = restClient.getNonVersionedWebAppDataForTenant(tenantDomain);
        return convert(apiData);
    }

    /**
     * Get non versioned webapp configuration from the gateway for super tenant users.
     * @param appProviderName String.
     * @param appName String.
     * @param version String.
     * @return non versioned webapp configuration data.
     * @throws AppManagementException on errors.
     */
    public AppData getNonVersionedWebAppData(String appProviderName, String appName, String version)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        APIData apiData = restClient.getNonVersionedWebAppData();
        return convert(apiData);
    }

    private AppData convert(APIData data) {
        if (data == null) {
            return null;
        }
        AppData apiData = new AppData();
        apiData.setContext(data.getContext());
        apiData.setFileName(data.getFileName());
        apiData.setHost(data.getHost());
        apiData.setName(data.getName());
        apiData.setPort(data.getPort());
        org.wso2.carbon.rest.api.stub.types.carbon.ResourceData[] resources = data.getResources();
        List<ResourceData> resList = new ArrayList<ResourceData>();
        if (resources != null && resources.length > 0) {
            for (org.wso2.carbon.rest.api.stub.types.carbon.ResourceData res : resources) {
                if (res == null) {
                    continue;
                }
                ResourceData resource = convert(res);
                resList.add(resource);
            }
            apiData.setResources(resList.toArray(new ResourceData[0]));
        }
        return apiData;
    }

    private ResourceData convert(org.wso2.carbon.rest.api.stub.types.carbon.ResourceData data) {
        ResourceData resource = new ResourceData();
        resource.setContentType(data.getContentType());
        resource.setFaultSequenceKey(data.getFaultSequenceKey());
        resource.setFaultSeqXml(data.getFaultSeqXml());
        resource.setInSequenceKey(data.getInSequenceKey());
        resource.setInSeqXml(data.getInSeqXml());
        resource.setMethods(data.getMethods());
        resource.setOutSequenceKey(data.getOutSequenceKey());
        resource.setOutSeqXml(data.getOutSeqXml());
        resource.setProtocol(data.getProtocol());
        resource.setUriTemplate(data.getUriTemplate());
        resource.setUrlMapping(data.getUrlMapping());
        resource.setUserAgent(data.getUserAgent());
        return resource;
    }

    /**
     * Deploy the sequence to the gateway for super tenant users.
     *
     * @param sequence - The sequence element , which to be deployed in synapse.
     * @throws AppManagementException on errors.
     */
    public void addSequence(String sequence) throws AppManagementException {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        if (sequence != null && !sequence.isEmpty()) {
            OMElement element = null;
            try {
                element = AXIOMUtil.stringToOM(sequence);
                client.addSequence(element);
            } catch (Exception e) {
                log.error("Exception occurred while converting String to an OM.");
            }
        }
    }

    /**
     * Deploy the sequence to the gateway for tenant users.
     *
     * @param sequence - The sequence element , which to be deployed in synapse.
     * @param tenantDomain the tenant domain of the user.
     * @throws AppManagementException on errors.
     */
    public void addSequenceForTenant(String sequence, String tenantDomain) throws AppManagementException {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        if (sequence != null && !sequence.isEmpty()) {
            OMElement element = null;
            try {
                element = AXIOMUtil.stringToOM(sequence);
                client.addSequenceForTenant(element, tenantDomain);
            } catch (Exception e) {
                log.error("Exception occurred while converting String to an OM.");
            }
        }
    }

    /**
     * Undeploy the sequence from gateway for super tenant users.
     *
     * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration.
     * @throws AppManagementException on errors.
     */
    public void deleteSequence(String sequenceName) throws AppManagementException {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        client.deleteSequence(sequenceName);
    }

    /**
     * Undeploy the sequence from gateway for tenant users.
     *
     * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration.
     * @param tenantDomain the tenant domain of the user.
     * @throws AppManagementException on errors.
     */
    public void deleteSequenceForTenant(String sequenceName, String tenantDomain) throws AppManagementException {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        client.deleteSequenceForTenant(sequenceName, tenantDomain);
    }

    /**
     * Get the sequence from gateway for super tenant users.
     *
     * @param sequenceName -The sequence name.
     * @throws AppManagementException on errors.
     */
    public OMElement getSequence(String sequenceName) throws AppManagementException {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        return (OMElement) client.getSequence(sequenceName);
    }

    /**
     * Get the sequence from gateway for tenant users.
     *
     * @param sequenceName -The sequence name
     * @param tenantDomain the tenant domain of the user.
     * @throws AppManagementException on errors.
     */
    public OMElement getSequenceForTenant(String sequenceName, String tenantDomain) throws AppManagementException {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        return (OMElement) client.getSequenceForTenant(sequenceName, tenantDomain);
    }

    /**
     * Check whether there is already a sequence in gateway for super tenant users.
     * @param sequenceName the sequence name.
     * @return true or false.
     * @throws AppManagementException
     */
    public boolean isExistingSequence(String sequenceName) throws AppManagementException {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        return client.isExistingSequence(sequenceName);
    }

    /**
     * Check whether there is already a sequence in gateway for tenant users.
     * @param sequenceName the sequence name.
     * @param tenantDomain the tenant domain of the user.
     * @return true or false.
     * @throws AppManagementException
     */
    public boolean isExistingSequenceForTenant(String sequenceName, String tenantDomain) throws AppManagementException {
        SequenceAdminServiceClient client = new SequenceAdminServiceClient();
        return client.isExistingSequenceForTenant(sequenceName, tenantDomain);
    }

    /**
     * Encrypt plain text password.
     * @param plainTextPass plain text password.
     * @return encoded password.
     * @throws AppManagementException on errors.
     */
    public String doEncryption(String plainTextPass) throws AppManagementException {
        MediationSecurityAdminServiceClient client = new MediationSecurityAdminServiceClient();
        String encodedValue = null;
        try {
            encodedValue = client.doEncryption(plainTextPass);
        } catch (AppManagementException e) {
            String msg = "Failed to encrypt the secured endpoint password, " + e.getMessage();
            throw new AppManagementException(msg, e);
        }
        return encodedValue;
    }
}