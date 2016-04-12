package org.wso2.carbon.appmgt.impl.utils;

import org.apache.axis2.AxisFault;
import org.apache.xalan.xsltc.dom.Axis;
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

public class AppGatewayAdminClient extends AbstractAPIGatewayAdminClient{
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
     * Adds versioned web app configuration to the gateway
     *
     * @param builder
     * @param tenantDomain
     * @throws AxisFault
     */
    public void addVersionedWebApp(APITemplateBuilder builder, APIIdentifier apiId, String tenantDomain)
            throws AppManagementException {
        try {
            String apiConfig = builder.getConfigStringForVersionedWebAppTemplate(environment);
            if (tenantDomain != null && !("").equals(tenantDomain)
                    && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.addVersionedWebAppForTenant(apiId.getProviderName(),apiId.getApiName(), apiId
                        .getVersion(), apiConfig, tenantDomain);
              //  apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(),tenantDomain);
            } else {
                appGatewayAdminStub.addVersionedWebApp(apiId.getProviderName(),apiId.getApiName(), apiId
                        .getVersion(), apiConfig);
            }
        } catch (RemoteException e) {
            throw new AppManagementException("Error while adding new WebApp", e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException("Error while adding new WebApp", e);
        } catch (APITemplateException e) {
            throw new AppManagementException("Error while adding new WebApp", e);
        }
    }

    /**
     * Returns versioned web app configuration from the gateway
     *
     * @param tenantDomain
     * @return
     * @throws AxisFault
     */
    public AppData getVersionedWebApp(APIIdentifier apiId, String tenantDomain) throws AppManagementException {
        try {
            AppData appData;
            if (tenantDomain != null && !("").equals(tenantDomain)
                    && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                appData = appGatewayAdminStub.getVersionedWebAppForTenant(apiId.getProviderName(),apiId.getApiName(), apiId
                        .getVersion(), tenantDomain);
            } else {
                appData = appGatewayAdminStub.getVersionedWebApp(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion());
            }
            return appData;
        } catch (RemoteException e) {
            throw new AppManagementException("Error while obtaining WebApp information from gateway", e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException("Error while obtaining WebApp information from gateway", e);
        }
    }

    /**
     * Updates versioned web app configuration in the gateway
     *
     * @param builder
     * @param tenantDomain
     * @throws AxisFault
     */
    public void updateVersionedWebApp(APITemplateBuilder builder, APIIdentifier apiId, String tenantDomain)
            throws AppManagementException {
        try {
            String apiConfig = builder.getConfigStringForVersionedWebAppTemplate(environment);
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {

                appGatewayAdminStub.updateVersionedWebAppForTenant(apiId.getProviderName(),apiId.getApiName(), apiId
                        .getVersion(), apiConfig, tenantDomain);
            } else {
                appGatewayAdminStub.updateVersionedWebApp(apiId.getProviderName(),apiId.getApiName(), apiId
                        .getVersion(), apiConfig);
            }
        } catch (RemoteException e) {
            throw new AppManagementException("Error while updating WebApp", e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException("Error while updating WebApp", e);
        } catch (APITemplateException e) {
            throw new AppManagementException("Error while updating WebApp", e);
        }
    }

    /**
     * Deletes versioned web app configuration from the gateway
     *
     * @param tenantDomain
     * @throws AxisFault
     */
    public void deleteVersionedWebApp(APIIdentifier apiId, String tenantDomain) throws AppManagementException {
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.deleteVersionedWebAppForTenant(apiId.getProviderName(),apiId.getApiName(), apiId
                        .getVersion(), tenantDomain);
            } else {
                appGatewayAdminStub.deleteVersionedWebApp(apiId.getProviderName(),apiId.getApiName(), apiId
                        .getVersion());
            }
        } catch (RemoteException e) {
            throw new AppManagementException("Error while updating WebApp", e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException("Error while updating WebApp", e);
        }
    }

    /**
     * Adds non-versioned web app configuration to the gateway
     *
     * @param builder
     * @param tenantDomain
     * @throws AxisFault
     */
    public void addNonVersionedWebApp(APITemplateBuilder builder, APIIdentifier apiId, String tenantDomain)
            throws AppManagementException {

        try {
            String apiConfig = builder.getConfigStringForNonVersionedWebAppTemplate();
            if (tenantDomain != null && !("").equals(tenantDomain)
                    && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.addNonVersionedWebAppForTenant(apiId.getProviderName(),apiId.getApiName(), apiId
                        .getVersion(),apiConfig, tenantDomain);
            } else {
                appGatewayAdminStub.addNonVersionedWebApp(apiId.getProviderName(),apiId.getApiName(), apiId
                        .getVersion(),apiConfig);
            }
        } catch (RemoteException e) {
            throw new AppManagementException("Error publishing non-versioned web app to the gateway", e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException("Error publishing non-versioned web app to the gateway", e);
        } catch (APITemplateException e) {
            throw new AppManagementException("Error publishing non-versioned web app to the gatewayp", e);
        }
    }

    /**
     * Updates non-versioned web app configuration in the gateway
     *
     * @param builder
     * @param tenantDomain
     * @throws AxisFault
     */
    public void updateNonVersionedWebApp(APITemplateBuilder builder, APIIdentifier apiId, String tenantDomain)
            throws AppManagementException {
        try {
            String apiConfig = builder.getConfigStringForNonVersionedWebAppTemplate();
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {

                appGatewayAdminStub.updateNonVersionedWebAppForTenant (apiId.getProviderName(),apiId.getApiName(), apiId
                        .getVersion(),apiConfig, tenantDomain);
            } else {
                appGatewayAdminStub.updateNonVersionedWebApp(apiId.getProviderName(),apiId.getApiName(), apiId
                        .getVersion(),apiConfig);
            }
        } catch (RemoteException e) {
            throw new AppManagementException("Error while updating non-versioned web app in the gateway", e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException("Error while updating non-versioned web app in the gateway", e);
        } catch (APITemplateException e) {
            throw new AppManagementException("Error while updating non-versioned web app in the gateway", e);
        }
    }

    /**
     * Deletes non-versioned web app configuration form the gateway
     *
     * @param tenantDomain
     * @throws AxisFault
     */
    public void deleteNonVersionedWebApp(APIIdentifier apiId, String tenantDomain) throws AppManagementException {
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                appGatewayAdminStub.deleteNonVersionedWebAppForTenant (apiId.getProviderName(),apiId.getApiName(), apiId
                        .getVersion(), tenantDomain);
            } else {
                appGatewayAdminStub.deleteNonVersionedWebApp(apiId.getProviderName(),apiId.getApiName(), apiId
                        .getVersion());
            }
        } catch (RemoteException e) {
            throw new AppManagementException("Error while deleting non-versioned web app from the gateway", e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException("Error while deleting non-versioned web app from the gateway", e);
        }
    }

    /**
     * Returns the non-versioned web app configuration from the gateway
     *
     * @param tenantDomain
     * @return
     * @throws AxisFault
     */
    public AppData getNonVersionedWebAppData(APIIdentifier apiId, String tenantDomain) throws AppManagementException {
        try {
            AppData appData;
            if (tenantDomain != null && !("").equals(tenantDomain)
                    && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                appData = appGatewayAdminStub.getNonVersionedWebAppDataForTenant (apiId.getProviderName(),apiId.getApiName(),
                                                                                  apiId.getVersion(), tenantDomain);
            } else {
                appData = appGatewayAdminStub.getNonVersionedWebAppData(apiId.getProviderName(),apiId.getApiName(), apiId
                        .getVersion());
            }
            return appData;
        } catch (RemoteException e) {
            throw new AppManagementException(
                    "Error while obtaining non-versioned web app information from gateway", e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException("Error while obtaining non-versioned web app information from gateway", e);
        }
    }

    /**
     * Deploy the sequence to the gateway
     *
     * @param sequence     - The sequence element , which to be deployed in synapse
     * @param tenantDomain - The Tenant Domain
     * @throws AxisFault
     */
    public void addSequence(OMElement sequence, String tenantDomain) throws AppManagementException {
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
            throw new AppManagementException("Error while adding new sequence", e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException("Error while adding new sequence", e);
        } catch (XMLStreamException e) {
            throw new AppManagementException("Error while adding new sequence", e);
        }
    }

    /**
     * Undeploy the sequence from gateway
     *
     * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration
     * @param tenantDomain - The Tenant Domain
     * @throws AxisFault
     */
    public void deleteSequence(String sequenceName, String tenantDomain) throws AppManagementException {
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                appGatewayAdminStub.deleteSequenceForTenant(sequenceName, tenantDomain);
            } else {
                appGatewayAdminStub.deleteSequence(sequenceName);
            }

        } catch (RemoteException e) {
            throw new AppManagementException("Error while deleting sequence", e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException("Error while deleting sequence", e);
        }
    }

    /**
     * get the sequence from gateway
     *
     * @param sequenceName - The sequence name,
     * @param tenantDomain - The Tenant Domain
     * @throws AxisFault
     */
    public OMElement getSequence(String sequenceName, String tenantDomain) throws AppManagementException {
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return (OMElement) appGatewayAdminStub.getSequenceForTenant(sequenceName, tenantDomain);
            } else {
                return (OMElement) appGatewayAdminStub.getSequence(sequenceName);
            }

        } catch (RemoteException e) {
            throw new AppManagementException("Error while retriving the sequence", e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException("Error while retriving the sequence", e);
        }
    }

    public boolean isExistingSequence(String sequenceName, String tenantDomain) throws AppManagementException {
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return appGatewayAdminStub.isExistingSequenceForTenant(sequenceName, tenantDomain);
            } else {
                return appGatewayAdminStub.isExistingSequence(sequenceName);
            }
        } catch (RemoteException e) {
            throw new AppManagementException("Error while checking for existence of sequence : " + sequenceName +
                                        " in tenant " + tenantDomain, e);
        } catch (AppGatewayAdminAppManagementExceptionException e) {
            throw new AppManagementException("Error while checking for existence of sequence : " + sequenceName +
                                                     " in tenant " + tenantDomain, e);
        }
    }

    /**
     * Store the encrypted password into the registry with the unique property name.
     * Property name is constructed as "Provider+ ApiName +Version"
     *
     * @param api
     * @param tenantDomain
     * @throws APIManagementException
     */
    public void addSecureVaultProperty(WebApp api, String tenantDomain) throws AppManagementException {

        UserRegistry registry;
        try {
            String encryptedPassword = doEncryption(api.getEndpointUTPassword());
            String secureVaultAlias = api.getId().getProviderName() +
                    "--" + api.getId().getApiName() + api.getId().getVersion();
            registry = getRegistry(tenantDomain);
            Resource resource = registry.get(AppMConstants.API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION);
            //add the property to the resource then put the resource
            resource.addProperty(secureVaultAlias, encryptedPassword);
            registry.put(resource.getPath(), resource);
            resource.discard();
        } catch (AppManagementException e) {
            String msg = "Failed to get registry secure vault property for the tenant : " + tenantDomain + e.getMessage();
            throw new AppManagementException(msg, e);
        } catch (RegistryException e) {
            String msg = "Failed to get registry secure vault property for the tenant : " + tenantDomain + e.getMessage();
            throw new AppManagementException(msg, e);
        }
    }


    /**
     * Store the encrypted password into the registry with the unique property name.
     * Property name is constructed as "Provider+ ApiName +Version"
     *
     * @param api
     * @param tenantDomain
     * @throws APIManagementException
     */
    public void deleteSecureVaultProperty(WebApp api, String tenantDomain) throws AppManagementException {

        UserRegistry registry;
        try {

            String secureVaultAlias = api.getId().getProviderName() +
                    "--" + api.getId().getApiName() + api.getId().getVersion();
            registry = getRegistry(tenantDomain);
            Resource resource = registry.get(AppMConstants.API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION);
            resource.removeProperty(secureVaultAlias);
            registry.put(resource.getPath(), resource);
            resource.discard();

        } catch (RegistryException e) {
            String msg = "Failed to delete the property. " + e.getMessage();
            throw new AppManagementException(msg, e);
        }
    }


    /**
     * Update the encrypted password into the registry with the unique property
     * name. Property name is constructed as "Provider+ ApiName +Version"
     *
     * @param api
     * @param tenantDomain
     * @throws APIManagementException
     */
    public void updateSecureVaultProperty(WebApp api, String tenantDomain) throws AppManagementException {
        UserRegistry registry;

        try {
            String encryptedPassword = doEncryption(api.getEndpointUTPassword());

            String secureVaultAlias = api.getId().getProviderName() +
                    "--" + api.getId().getApiName() + api.getId().getVersion();
            registry = getRegistry(tenantDomain);
            Resource resource = registry.get(AppMConstants.API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION);
            resource.setProperty(secureVaultAlias, encryptedPassword);
            registry.put(resource.getPath(), resource);
            resource.discard();
        } catch (RegistryException e) {
            String msg = "Failed to update the property. " + e.getMessage();
            throw new AppManagementException(msg, e);
        }
    }
    

    /**
     * Get the config system registry for tenants
     *
     * @param tenantDomain
     * @return
     * @throws APIManagementException
     */
    private UserRegistry getRegistry(String tenantDomain) throws AppManagementException {
        PrivilegedCarbonContext.startTenantFlow();
        if (tenantDomain != null && !tenantDomain.equals("")) {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
                                                                                  true);
        } else {
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
                                     true);
        }

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        UserRegistry registry = null;
        try {
            registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getConfigSystemRegistry(tenantId);
        } catch (RegistryException e) {
            String msg = "Failed to get registry instance for the tenant : " + tenantDomain + e.getMessage();
            throw new AppManagementException(msg, e);
        }
        return registry;
    }


    /**
     * encrypt the plain text password
     *
     * @param cipher        init cipher
     * @param plainTextPass plain text password
     * @return encrypted password
     * @throws APIManagementException
     */
    private String doEncryption(String plainTextPass) throws AppManagementException {
        String encodedValue = null;
        try {
            encodedValue = appGatewayAdminStub.doEncryption(plainTextPass);

        } catch (AppGatewayAdminAppManagementExceptionException e) {
            String msg = "Failed to encrypt the secured endpoint password, " + e.getMessage();
            throw new AppManagementException(msg, e);
        } catch (RemoteException e) {
            String msg = "Failed to encrypt the secured endpoint password, " + e.getMessage();
            throw new AppManagementException(msg, e);
        }
        return encodedValue;
    }


}