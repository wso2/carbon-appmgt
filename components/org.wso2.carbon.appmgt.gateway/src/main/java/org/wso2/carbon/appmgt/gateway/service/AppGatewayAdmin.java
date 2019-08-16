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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.gateway.dto.WebAppData;
import org.wso2.carbon.appmgt.gateway.dto.ResourceData;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.gateway.utils.RESTAPIAdminClient;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.wso2.carbon.sequences.common.SequenceEditorException;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

/**
 * This service is used to proxy RESTAPIAdmin service which is exposed by carbon mediation. If Publisher needs to call
 * RESTAPIAdmin service, it should call AppGatewayAdmin service instead. This is introduced to separate Publisher from
 * mediation services.
 */
public class AppGatewayAdmin extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(AppGatewayAdmin.class);

    /**
     * Add versioned webapp configuration to the gateway for tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @param appConfig       App configuration
     * @param tenantDomain    Tenant domain
     * @throws AppManagementException on errors while trying to add versioned webapps for tenant users
     */
    public void addVersionedWebAppForTenant(String appProviderName, String appName, String version, String appConfig,
                                            String tenantDomain)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.addVersionedWebAppForTenant(appConfig, tenantDomain);
    }

    /**
     * Add versioned webapp configuration to the gateway for super tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @param appConfig       App configuration
     * @throws AppManagementException on errors while trying to add versioned webapps for super tenant users
     */
    public void addVersionedWebApp(String appProviderName, String appName, String version, String appConfig)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.addVersionedWebApp(appConfig);
    }

    /**
     * Return versioned webapp configuration from the gateway for tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @param tenantDomain    Tenant domain
     * @return versioned webapp configuration data.
     * @throws AppManagementException on error while trying to get versioned webapps for tenant users
     */
    public WebAppData getVersionedWebAppForTenant(String appProviderName, String appName, String version,
                                                  String tenantDomain) throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        APIData apiData = restClient.getVersionedWebAppForTenant(tenantDomain);
        return convert(apiData);
    }

    /**
     * Return versioned webapp configuration from the gateway for super tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @return versioned webapp configuration data.
     * @throws AppManagementException on error while trying to get versioned webapps for super tenant users
     */
    public WebAppData getVersionedWebApp(String appProviderName, String appName, String version)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        APIData apiData = restClient.getVersionedWebApp();
        return convert(apiData);
    }


    /**
     * Update versioned webapp configuration in the gateway for tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @param appConfig       App configuration
     * @param tenantDomain    Tenant domain
     * @throws AppManagementException on errors while trying to update versioned webapps for tenant users
     */
    public void updateVersionedWebAppForTenant(String appProviderName, String appName, String version, String appConfig,
                                               String tenantDomain) throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.updateVersionedWebAppForTenant(appConfig, tenantDomain);
    }

    /**
     * Update versioned webapp configuration in the gateway for super tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @param appConfig       App configuration
     * @throws AppManagementException on errors while trying to update versioned webapps for super tenant users
     */
    public void updateVersionedWebApp(String appProviderName, String appName, String version, String appConfig)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.updateVersionedWebApp(appConfig);
    }

    /**
     * Delete versioned webapp configuration from the gateway for tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @param tenantDomain    Tenant domain
     * @throws AppManagementException on error while deleting versioned webapp for tenant users
     */
    public void deleteVersionedWebAppForTenant(String appProviderName, String appName, String version,
                                               String tenantDomain) throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.deleteVersionedWebAppForTenant(tenantDomain);
    }

    /**
     * Delete versioned webapp configuration from the gateway for super tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @throws AppManagementException on error while deleting versioned webapp for super tenant users
     */
    public void deleteVersionedWebApp(String appProviderName, String appName, String version)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.deleteVersionedWebApp();
    }

    /**
     * Add non versioned webapp configuration to the gateway for tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @param appConfig       App configuration
     * @param tenantDomain    Tenant domain
     * @throws AppManagementException on error while trying to add non versioned webapp for tenant users
     */
    public void addNonVersionedWebAppForTenant(String appProviderName, String appName, String version, String appConfig,
                                               String tenantDomain) throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.addNonVersionedWebAppForTenant(appConfig, tenantDomain);
    }

    /**
     * Add non versioned webapp configuration to the gateway for super tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @param appConfig       App configuration
     * @throws AppManagementException on error while trying to add non versioned webapp for super tenant users
     */
    public void addNonVersionedWebApp(String appProviderName, String appName, String version, String appConfig)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.addNonVersionedWebApp(appConfig);
    }

    /**
     * Update non versioned webapp configuration in the gateway for tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @param appConfig       App configuration
     * @param tenantDomain    Tenant domain
     * @throws AppManagementException on error while trying to update non versioned webapp for tenant users
     */
    public void updateNonVersionedWebAppForTenant(String appProviderName, String appName, String version,
                                                  String appConfig, String tenantDomain) throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.updateNonVersionedWebAppForTenant(appConfig, tenantDomain);
    }

    /**
     * Update non versioned webapp configuration in the gateway for super tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @param appConfig       App configuration
     * @throws AppManagementException on error while trying to update non versioned webapp for super tenant users
     */
    public void updateNonVersionedWebApp(String appProviderName, String appName, String version, String appConfig)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.updateNonVersionedWebApp(appConfig);
    }

    /**
     * Delete non versioned webapp configuration from the gateway for tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @param tenantDomain    Tenant domain
     * @throws AppManagementException on error while trying to delete non versioned webapp for tenant users
     */
    public void deleteNonVersionedWebAppForTenant(String appProviderName, String appName, String version,
                                                  String tenantDomain)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.deleteNonVersionedWebAppForTenant(tenantDomain);
    }

    /**
     * Delete non versioned webapp configuration from the gateway for super tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @throws AppManagementException on error while trying to delete non versioned webapp for super tenant users
     */
    public void deleteNonVersionedWebApp(String appProviderName, String appName, String version)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        restClient.deleteNonVersionedWebApp();
    }

    /**
     * Get non versioned webapp configuration from the gateway for tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @param tenantDomain    Tenant domain
     * @return Non versioned webapp configuration data.
     * @throws AppManagementException on error while trying to get non versioned webapp for tenant users
     */
    public WebAppData getNonVersionedWebAppDataForTenant(String appProviderName, String appName, String version,
                                                         String tenantDomain) throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        APIData apiData = restClient.getNonVersionedWebAppDataForTenant(tenantDomain);
        return convert(apiData);
    }

    /**
     * Get non versioned webapp configuration from the gateway for super tenant users.
     *
     * @param appProviderName App provider name
     * @param appName         App name
     * @param version         Version
     * @return Non versioned webapp configuration data.
     * @throws AppManagementException on error while trying to get non versioned webapp for super tenant users
     */
    public WebAppData getNonVersionedWebAppData(String appProviderName, String appName, String version)
            throws AppManagementException {
        RESTAPIAdminClient restClient = new RESTAPIAdminClient(appProviderName, appName, version);
        APIData apiData = restClient.getNonVersionedWebAppData();
        return convert(apiData);
    }

    private WebAppData convert(APIData data) {
        if (data == null) {
            return null;
        }
        WebAppData webAppData = new WebAppData();
        webAppData.setContext(data.getContext());
        webAppData.setFileName(data.getFileName());
        webAppData.setHost(data.getHost());
        webAppData.setName(data.getName());
        webAppData.setPort(data.getPort());
        org.wso2.carbon.rest.api.stub.types.carbon.ResourceData[] resources = data.getResources();
        List<ResourceData> resourceDataList = new ArrayList<ResourceData>();
        if (resources != null && resources.length > 0) {
            for (org.wso2.carbon.rest.api.stub.types.carbon.ResourceData resourceData : resources) {
                if (resourceData == null) {
                    continue;
                }
                ResourceData resource = convert(resourceData);
                resourceDataList.add(resource);
            }
            webAppData.setResources(resourceDataList.toArray(new ResourceData[0]));
        }
        return webAppData;
    }

    private ResourceData convert(org.wso2.carbon.rest.api.stub.types.carbon.ResourceData data) {
        ResourceData resource = new ResourceData();
        resource.setContentType(data.getContentType());
        resource.setFaultSequenceXml(processSequenceXml(data.getFaultSeqXml()));
        resource.setFaultSequenceKey(data.getFaultSequenceKey());
        resource.setInSequenceXml(data.getInSequenceKey());
        resource.setInSequenceXml(processSequenceXml(data.getInSeqXml()));
        resource.setOutSequenceKey(data.getOutSequenceKey());
        resource.setOutSequenceXml(processSequenceXml(data.getOutSeqXml()));
        resource.setHttpMethods(data.getMethods());
        resource.setProtocol(data.getProtocol());
        resource.setUriTemplate(data.getUriTemplate());
        resource.setUrlMapping(data.getUrlMapping());
        resource.setUserAgent(data.getUserAgent());
        return resource;
    }

    private String processSequenceXml(String sequence) {
        if (StringUtils.isEmpty(sequence)) {
            return null;
        }
        String processedSequence = StringEscapeUtils.unescapeXml(sequence);
        return processedSequence;

    }

    /**
     * Deploy the sequence to the gateway for super tenant users.
     *
     * @param sequence - The sequence element , which to be deployed in synapse
     * @throws AppManagementException on errors while trying to add sequence
     */
    public void addSequence(String sequence) throws AppManagementException {
        if (!StringUtils.isEmpty(sequence)) {
            OMElement element = null;
            try {
                element = AXIOMUtil.stringToOM(sequence);
                ServiceReferenceHolder.getInstance().getSequenceAdminService().addSequence(element);
            } catch (SequenceEditorException e) {
                String errorMsg = "Error occurred while adding the sequence : " + sequence + " for super tenant.";
                throw new AppManagementException(errorMsg, e);
            } catch (XMLStreamException e) {
                String errorMsg = "Error occurred while streaming the sequence : " + sequence + " for super tenant.";
                throw new AppManagementException(errorMsg, e);
            }
        }
    }

    /**
     * Deploy the sequence to the gateway for tenant users.
     *
     * @param sequence     - The sequence element , which to be deployed in synapse
     * @param tenantDomain the tenant domain of the user
     * @throws AppManagementException on errors while trying to add sequence for tenant users
     */
    public void addSequenceForTenant(String sequence, String tenantDomain) throws AppManagementException {
        if (!StringUtils.isEmpty(sequence)) {
            OMElement element = null;
            try {
                element = AXIOMUtil.stringToOM(sequence);
                ServiceReferenceHolder.getInstance().getSequenceAdminService().addSequenceForTenant(element,
                                                                                                    tenantDomain);
            } catch (SequenceEditorException e) {
                String errorMsg = "Error occurred while adding the sequence : " + sequence + " for tenant : " +
                        tenantDomain;
                throw new AppManagementException(errorMsg, e);
            } catch (XMLStreamException e) {
                String errorMsg = "Error occurred while streaming the sequence : " + sequence + " for super tenant.";
                throw new AppManagementException(errorMsg, e);
            }
        }
    }

    /**
     * Undeploy the sequence from gateway for super tenant users.
     *
     * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration
     * @throws AppManagementException on errors while trying to undeploy sequence for tenant users
     */
    public void deleteSequence(String sequenceName) throws AppManagementException {
        try {
            ServiceReferenceHolder.getInstance().getSequenceAdminService().deleteSequence(sequenceName);
        } catch (SequenceEditorException e) {
            String errorMsg = "Error occurred while deleting the sequence : " + sequenceName + " for super tenant.";
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Undeploy the sequence from gateway for tenant users.
     *
     * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration
     * @param tenantDomain The tenant domain of the user
     * @throws AppManagementException on errors while trying to undeploy sequence for tenant users
     */
    public void deleteSequenceForTenant(String sequenceName, String tenantDomain) throws AppManagementException {
        try {
            ServiceReferenceHolder.getInstance().getSequenceAdminService().deleteSequenceForTenant
                    (sequenceName, tenantDomain);
        } catch (SequenceEditorException e) {
            String errorMsg = "Error occurred while deleting the sequence : " + sequenceName + " for tenant : " +
                    tenantDomain;
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Get the sequence from gateway for super tenant users.
     *
     * @param sequenceName The sequence name
     * @return sequence {@link OMElement} object
     * @throws AppManagementException on errors while trying to get sequence for super tenant users
     */
    public OMElement getSequence(String sequenceName) throws AppManagementException {
        try {
            return ServiceReferenceHolder.getInstance().getSequenceAdminService().getSequence(sequenceName);
        } catch (SequenceEditorException e) {
            String errorMsg = "Error occurred while retrieving the sequence : " + sequenceName + " for super tenant.";
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Get the sequence from gateway for tenant users.
     *
     * @param sequenceName The sequence name
     * @param tenantDomain Tenant domain
     * @return sequence {@link OMElement} object
     * @throws AppManagementException on errors while trying to get sequence for tenant users
     */
    public OMElement getSequenceForTenant(String sequenceName, String tenantDomain) throws AppManagementException {
        try {
            return ServiceReferenceHolder.getInstance().getSequenceAdminService().getSequenceForTenant(sequenceName,
                                                                                                       tenantDomain);
        } catch (SequenceEditorException e) {
            String errorMsg = "Error occurred while retrieving the sequence : " + sequenceName + " for tenant : " +
                    tenantDomain;
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Check whether there is already a sequence in gateway for super tenant users.
     *
     * @param sequenceName Sequence name
     * @return Whether the given sequence is exist or not
     * @throws AppManagementException on error while trying to check whether sequence exist or not
     */
    public boolean isExistingSequence(String sequenceName) throws AppManagementException {
        try {
            return ServiceReferenceHolder.getInstance().getSequenceAdminService().isExistingSequence(sequenceName);
        } catch (SequenceEditorException e) {
            String errorMsg = "Error while checking for existence of sequence : " + sequenceName;
            throw new AppManagementException(errorMsg, e);
        }
    }

    /**
     * Check whether there is already a sequence in gateway for tenant users.
     *
     * @param sequenceName Sequence name
     * @param tenantDomain The tenant domain of the user
     * @return Whether the given sequence is exist or not
     * @throws AppManagementException on error while trying to check whether sequence exist or not
     */
    public boolean isExistingSequenceForTenant(String sequenceName, String tenantDomain) throws AppManagementException {
        try {
            return ServiceReferenceHolder.getInstance().getSequenceAdminService().isExistingSequenceForTenant
                    (sequenceName, tenantDomain);
        } catch (SequenceEditorException e) {
            String errorMsg = "Error occurred while checking for existence of sequence : " + sequenceName + "in " +
                    "tenant : " + tenantDomain;
            throw new AppManagementException(errorMsg, e);
        }
    }
}