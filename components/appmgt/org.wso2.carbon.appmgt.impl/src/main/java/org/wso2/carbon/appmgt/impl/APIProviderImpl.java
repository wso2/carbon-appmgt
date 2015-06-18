/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.Constants;
import org.json.simple.JSONObject;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.EntitlementService;
import org.wso2.carbon.appmgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.appmgt.api.model.*;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicy;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicyPartial;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicyValidationResult;
import org.wso2.carbon.appmgt.api.model.entitlement.XACMLPolicyTemplateContext;
import org.wso2.carbon.appmgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.appmgt.impl.entitlement.EntitlementServiceFactory;
import org.wso2.carbon.appmgt.impl.idp.sso.SSOConfiguratorUtil;
import org.wso2.carbon.appmgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.observers.APIStatusObserverList;
import org.wso2.carbon.appmgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.appmgt.impl.template.APITemplateBuilderImpl;
import org.wso2.carbon.appmgt.impl.utils.APINameComparator;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.*;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides the core WebApp provider functionality. It is implemented in a very
 * self-contained and 'pure' manner, without taking requirements like security into account,
 * which are subject to frequent change. Due to this 'pure' nature and the significance of
 * the class to the overall WebApp management functionality, the visibility of the class has
 * been reduced to package level. This means we can still use it for internal purposes and
 * possibly even extend it, but it's totally off the limits of the users. Users wishing to
 * programmatically access this functionality should use one of the extensions of this
 * class which is visible to them. These extensions may add additional features like
 * security to this class.
 */
class APIProviderImpl extends AbstractAPIManager implements APIProvider {

    public APIProviderImpl(String username) throws AppManagementException {
        super(username);
    }

    /**
     * Returns a list of all #{@link org.wso2.carbon.apimgt.api.model.Provider} available on the system.
     *
     * @return Set<Provider>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get Providers
     */
    public Set<Provider> getAllProviders() throws AppManagementException {
        Set<Provider> providerSet = new HashSet<Provider>();
        GenericArtifactManager artifactManager = AppManagerUtil.getArtifactManager(registry,
                                                                            AppMConstants.PROVIDER_KEY);
        try {
            GenericArtifact[] genericArtifact = artifactManager.getAllGenericArtifacts();
            if (genericArtifact == null || genericArtifact.length == 0) {
                return providerSet;
            }
            for (GenericArtifact artifact : genericArtifact) {
                Provider provider =
                        new Provider(artifact.getAttribute(AppMConstants.PROVIDER_OVERVIEW_NAME));
                provider.setDescription(AppMConstants.PROVIDER_OVERVIEW_DESCRIPTION);
                provider.setEmail(AppMConstants.PROVIDER_OVERVIEW_EMAIL);
                providerSet.add(provider);
            }
        } catch (GovernanceException e) {
            handleException("Failed to get all providers", e);
        }
        return providerSet;
    }

    /**
     * Get a list of APIs published by the given provider. If a given WebApp has multiple APIs,
     * only the latest version will
     * be included in this list.
     *
     * @param providerId , provider id
     * @return set of WebApp
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get set of WebApp
     */
    public List<WebApp> getAPIsByProvider(String providerId) throws AppManagementException {

        List<WebApp> apiSortedList = new ArrayList<WebApp>();

        try {
            providerId = AppManagerUtil.replaceEmailDomain(providerId);
            String providerPath = AppMConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                                  providerId;
            GenericArtifactManager artifactManager = AppManagerUtil.getArtifactManager(registry,
                                                                                AppMConstants.API_KEY);
            Association[] associations = registry.getAssociations(providerPath,
                                                                  AppMConstants.PROVIDER_ASSOCIATION);
            for (Association association : associations) {
                String apiPath = association.getDestinationPath();
                Resource resource = registry.get(apiPath);
                String apiArtifactId = resource.getUUID();
                if (apiArtifactId != null) {
                    GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiArtifactId);
                    apiSortedList.add(AppManagerUtil.getAPI(apiArtifact, registry));
                } else {
                    throw new GovernanceException("artifact id is null of " + apiPath);
                }
            }

        } catch (RegistryException e) {
            handleException("Failed to get APIs for provider : " + providerId, e);
        }
        Collections.sort(apiSortedList, new APINameComparator());

        return apiSortedList;

    }


    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<Subscriber>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get subscribed APIs of given provider
     */
    public Set<Subscriber> getSubscribersOfProvider(String providerId)
            throws AppManagementException {

        Set<Subscriber> subscriberSet = null;
        try {
            subscriberSet = appMDAO.getSubscribersOfProvider(providerId);
        } catch (AppManagementException e) {
            handleException("Failed to get Subscribers for : " + providerId, e);
        }
        return subscriberSet;
    }

    /**
     * get details of provider
     *
     * @param providerName name of the provider
     * @return Provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get Provider
     */
    public Provider getProvider(String providerName) throws AppManagementException {
        Provider provider = null;
        String providerPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                              AppMConstants.PROVIDERS_PATH + RegistryConstants.PATH_SEPARATOR + providerName;
        try {
            GenericArtifactManager artifactManager = AppManagerUtil.getArtifactManager(registry,
                                                                                AppMConstants.PROVIDER_KEY);
            Resource providerResource = registry.get(providerPath);
            String artifactId =
                    providerResource.getUUID();
            if (artifactId == null) {
                throw new AppManagementException("artifact it is null");
            }
            GenericArtifact providerArtifact = artifactManager.getGenericArtifact(artifactId);
            provider = AppManagerUtil.getProvider(providerArtifact);

        } catch (RegistryException e) {
            handleException("Failed to get Provider form : " + providerName, e);
        }
        return provider;
    }

    /**
     * Return Usage of given APIIdentifier
     *
     * @param apiIdentifier APIIdentifier
     * @return Usage
     */
    public Usage getUsageByAPI(APIIdentifier apiIdentifier) {
        return null;
    }

    /**
     * Return Usage of given provider and WebApp
     *
     * @param providerId if of the provider
     * @param apiName    name of the WebApp
     * @return Usage
     */
    public Usage getAPIUsageByUsers(String providerId, String apiName) {
        return null;
    }

    /**
     * Returns usage details of all APIs published by a provider
     *
     * @param providerName Provider Id
     * @return UserApplicationAPIUsages for given provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to get UserApplicationAPIUsage
     */
    public UserApplicationAPIUsage[] getAllAPIUsageByProvider(
            String providerName) throws AppManagementException {
        return appMDAO.getAllAPIUsageByProvider(providerName);
    }

    /**
     * Shows how a given consumer uses the given WebApp.
     *
     * @param apiIdentifier APIIdentifier
     * @param consumerEmail E-mal Address of consumer
     * @return Usage
     */
    public Usage getAPIUsageBySubscriber(APIIdentifier apiIdentifier, String consumerEmail) {
        return null;
    }

    /**
     * Returns full list of Subscribers of an WebApp
     *
     * @param identifier APIIdentifier
     * @return Set<Subscriber>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get Subscribers
     */
    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier)
            throws AppManagementException {

        Set<Subscriber> subscriberSet = null;
        try {
            subscriberSet = appMDAO.getSubscribersOfAPI(identifier);
        } catch (AppManagementException e) {
            handleException("Failed to get subscribers for WebApp : " + identifier.getApiName(), e);
        }
        return subscriberSet;
    }

    /**
     * this method returns the Set<APISubscriptionCount> for given provider and api
     *
     * @param identifier APIIdentifier
     * @return Set<APISubscriptionCount>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get APISubscriptionCountByAPI
     */
    public long getAPISubscriptionCountByAPI(APIIdentifier identifier)
            throws AppManagementException {
        long count = 0L;
        try {
            count = appMDAO.getAPISubscriptionCountByAPI(identifier);
        } catch (AppManagementException e) {
            handleException("Failed to get APISubscriptionCount for: " + identifier.getApiName(), e);
        }
        return count;
    }

    public Map<String, List> getSubscribedAPPsByUsers(String fromDate, String toDate)
            throws AppManagementException {
        Map<String, List> users = new HashMap<String, List>();
        try {
            users = appMDAO.getSubscribedAPPsByUsers(fromDate, toDate, tenantId);
        } catch (AppManagementException e) {
            handleException("Failed to get subscribed apps by users for the period " + fromDate + "to " +
                    toDate, e);
        }
        return users;
    }


    public void addTier(Tier tier) throws AppManagementException {
        addOrUpdateTier(tier, false);
    }

    public void updateTier(Tier tier) throws AppManagementException {
        addOrUpdateTier(tier, true);
    }

    private void addOrUpdateTier(Tier tier, boolean update) throws AppManagementException {
        if (AppMConstants.UNLIMITED_TIER.equals(tier.getName())) {
            throw new AppManagementException("Changes on the '" + AppMConstants.UNLIMITED_TIER + "' " +
                                             "tier are not allowed");
        }

        Set<Tier> tiers = getTiers();
        if (update && !tiers.contains(tier)) {
            throw new AppManagementException("No tier exists by the name: " + tier.getName());
        }

        Set<Tier> finalTiers = new HashSet<Tier>();
        for (Tier tet : tiers) {
            if (!tet.getName().equals(tier.getName())) {
                finalTiers.add(tet);
            }
        }
        finalTiers.add(tier);
        saveTiers(finalTiers);
    }

    private void saveTiers(Collection<Tier> tiers) throws AppManagementException {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement root = fac.createOMElement(AppMConstants.POLICY_ELEMENT);
        OMElement assertion = fac.createOMElement(AppMConstants.ASSERTION_ELEMENT);
        try {
            Resource resource = registry.newResource();
            for (Tier tier : tiers) {
                String policy = new String(tier.getPolicyContent());
                assertion.addChild(AXIOMUtil.stringToOM(policy));
                // if (tier.getDescription() != null && !"".equals(tier.getDescription())) {
                //     resource.setProperty(AppMConstants.TIER_DESCRIPTION_PREFIX + tier.getName(),
                //              tier.getDescription());
                //  }
            }
            //resource.setProperty(AppMConstants.TIER_DESCRIPTION_PREFIX + AppMConstants.UNLIMITED_TIER,
            //        AppMConstants.UNLIMITED_TIER_DESC);
            root.addChild(assertion);
            resource.setContent(root.toString());
            registry.put(AppMConstants.API_TIER_LOCATION, resource);
        } catch (XMLStreamException e) {
            handleException("Error while constructing tier policy file", e);
        } catch (RegistryException e) {
            handleException("Error while saving tier configurations to the registry", e);
        }
    }

    public void removeTier(Tier tier) throws AppManagementException {
        if (AppMConstants.UNLIMITED_TIER.equals(tier.getName())) {
            throw new AppManagementException("Changes on the '" + AppMConstants.UNLIMITED_TIER + "' " +
                                             "tier are not allowed");
        }

        Set<Tier> tiers = getTiers();
        if (tiers.remove(tier)) {
            saveTiers(tiers);
        } else {
            throw new AppManagementException("No tier exists by the name: " + tier.getName());
        }
    }

    /**
     * Adds a new WebApp to the Store
     *
     * @param app WebApp
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     *          if failed to add WebApp
     */
    public void addWebApp(WebApp app) throws AppManagementException {
        try {           
            createAPI(app);
            appMDAO.addWebApp(app);
            if (AppManagerUtil.isAPIManagementEnabled()) {
            	Cache contextCache = AppManagerUtil.getAPIContextCache();
            	Boolean apiContext = null;
            	if (contextCache.get(app.getContext()) != null) {
            		apiContext = Boolean.parseBoolean(contextCache.get(app.getContext()).toString());
            	} 
            	if (apiContext == null) {
                    contextCache.put(app.getContext(), true);
                }
            }
        } catch (AppManagementException e) {
            throw new AppManagementException("Error in adding WebApp :"+app.getId().getApiName(),e);
        }
    }

    /**
     * Generates entitlement policies for the given app.
     *
     * @param apiIdentifier@throws AppManagementException
     */
    @Override
    public void generateEntitlementPolicies(APIIdentifier apiIdentifier) throws
                                                                         AppManagementException {

        AppManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        List<XACMLPolicyTemplateContext> xacmlPolicyTemplateContexts =
                appMDAO.getEntitlementPolicyTemplateContexts(apiIdentifier);

        EntitlementService entitlementService = EntitlementServiceFactory.getEntitlementService(config);
        entitlementService.generateAndSaveEntitlementPolicies(xacmlPolicyTemplateContexts);

        // Update URL mapping => XACML partial mapping with the generated policy IDs.
        appMDAO.updateURLEntitlementPolicyPartialMappings(xacmlPolicyTemplateContexts);
    }

    /**
     * Updates given entitlement policies.
     *
     * @param policies Entitlement policies to be updated.
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    @Override
    public void updateEntitlementPolicies(List<EntitlementPolicy> policies) throws
                                                                            AppManagementException {

        if (policies == null || policies.isEmpty()) {
            return;
        }

        AppManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        EntitlementService entitlementService = EntitlementServiceFactory.getEntitlementService(config);

        for (EntitlementPolicy policy : policies) {
            entitlementService.updatePolicy(policy);
        }
    }

    /**
     * Get entitlement policy content from policy id
     *
     * @param policyId Entitlement policy id
     * @return entitlement policy content
     */
    @Override
    public String getEntitlementPolicy(String policyId) {
        if (policyId == null) {
            return null;
        }
        AppManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        EntitlementService entitlementService = EntitlementServiceFactory.getEntitlementService(config);
        return entitlementService.getPolicyContent(policyId);
    }

    @Override
    public int getWebAppId(String uuid) throws AppManagementException {
        return appMDAO.getWebAppId(uuid);
    }

    @Override
    public int saveEntitlementPolicyPartial(String policyPartialName, String policyPartial, boolean isSharedPartial,
                                            String policyAuthor,String policyPartialDesc) throws AppManagementException {
        return appMDAO.saveEntitlementPolicyPartial(policyPartialName, policyPartial, isSharedPartial, policyAuthor,
                policyPartialDesc, tenantId);
    }

    @Override
    public boolean updateEntitlementPolicyPartial(int policyPartialId, String policyPartial,
                                                  String author, boolean isShared,String policyPartialDesc) throws
                                                                                   AppManagementException {
        appMDAO.updateEntitlementPolicyPartial(policyPartialId, policyPartial, author, isShared, policyPartialDesc);
        
        // Regenerate XACML policies of the apps which are using the updated policy partial.
        List<APIIdentifier> associatedApps = getAssociatedApps(policyPartialId);
        
        for(APIIdentifier associatedApp : associatedApps){
        	generateEntitlementPolicies(associatedApp);
        }
        
        return true;
    }

    @Override
    public EntitlementPolicyPartial getPolicyPartial(int policyPartialId) throws
                                                                          AppManagementException {
        return appMDAO.getPolicyPartial(policyPartialId);
    }

    @Override
    public List<APIIdentifier> getAssociatedApps(int policyPartialId) throws AppManagementException {
        return appMDAO.getAssociatedApps(policyPartialId);
    }

    @Override
    public boolean deleteEntitlementPolicyPartial(int policyPartialId, String author) throws
                                                                                      AppManagementException {
        return appMDAO.deletePolicyPartial(policyPartialId, author);
    }

    @Override
    public List<EntitlementPolicyPartial> getApplicationPolicyPartialList(int applicationId) throws
                                                                                             AppManagementException {
        return appMDAO.getApplicationsEntitlementPolicyPartialsList(applicationId);
    }

    @Override
    public List<EntitlementPolicyPartial> getSharedPolicyPartialsList() throws
                                                                        AppManagementException {
        return appMDAO.getSharedEntitlementPolicyPartialsList(tenantId);
    }


    /**
     * Get Policy Groups Application wise
     *
     * @param appId Application Id
     * @return List of policy groups
     * @throws AppManagementException
     */
    @Override
    public List<EntitlementPolicyGroup> getPolicyGroupListByApplication(int appId) throws
            AppManagementException {
        return appMDAO.getPolicyGroupListByApplication(appId);
    }

    /**
     * Retrieves TRACKING_CODE sequences from APM_APP Table
     *@param uuid : Application UUID
     *@return TRACKING_CODE
     *@throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    @Override
    public String getTrackingID(String uuid) throws AppManagementException {
        return appMDAO.getTrackingID(uuid);
    }


    @Override
    public EntitlementPolicyValidationResult validateEntitlementPolicyPartial(String policyPartial) throws
                                                                                                    AppManagementException {

        AppManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        EntitlementService entitlementService = EntitlementServiceFactory.getEntitlementService(config);
        return entitlementService.validatePolicyPartial(policyPartial);
    }

    /**
     * Persist WebApp Status into a property of WebApp Registry resource
     *
     * @param artifactId WebApp artifact ID
     * @param apiStatus Current status of the WebApp
     * @throws org.wso2.carbon.appmgt.api.AppManagementException on error
     */
    private void saveAPIStatus(String artifactId, String apiStatus) throws AppManagementException {
        try{
            Resource resource = registry.get(artifactId);
            if (resource != null) {
                String propValue = resource.getProperty(AppMConstants.API_STATUS);
                if (propValue == null) {
                    resource.addProperty(AppMConstants.API_STATUS, apiStatus);
                } else {
                    resource.setProperty(AppMConstants.API_STATUS, apiStatus);
                }
                registry.put(artifactId,resource);
            }
        }catch (RegistryException e) {
            handleException("Error while adding WebApp", e);
        }
    }

    /**
     * Updates an existing WebApp
     *
     * @param api WebApp
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to update WebApp
     */
    public void updateAPI(WebApp api) throws AppManagementException {
        WebApp oldApi = getAPI(api.getId());
        if (oldApi.getStatus().equals(api.getStatus())) {
            try {
               
                //boolean updatePermissions = false;
                /*if(!oldApi.getVisibility().equals(api.getVisibility()) || (oldApi.getVisibility().equals(AppMConstants.API_RESTRICTED_VISIBILITY) && !api.getVisibleRoles().equals(oldApi.getVisibleRoles()))){
                    updatePermissions = true;
                }*/
                //updateApiArtifact(api, true,updatePermissions);
                if (!oldApi.getContext().equals(api.getContext())) {
                    api.setApiHeaderChanged(true);
                }

                appMDAO.updateAPI(api);

                AppManagerConfiguration config = ServiceReferenceHolder.getInstance().
                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
                boolean gatewayExists = config.getApiGatewayEnvironments().size() > 0;
                String gatewayType = config.getFirstProperty(AppMConstants.API_GATEWAY_TYPE);
                boolean isAPIPublished = false;
                // gatewayType check is required when WebApp Management is deployed on other servers to avoid synapse
                if (gatewayType.equalsIgnoreCase(AppMConstants.API_GATEWAY_TYPE_SYNAPSE)) {
                    isAPIPublished = isAPIPublished(api);
                    if (gatewayExists) {
                        if (isAPIPublished) {
                            WebApp apiPublished = getAPI(api.getId());
                            apiPublished.setOldInSequence(oldApi.getInSequence());
                            apiPublished.setOldOutSequence(oldApi.getOutSequence());

                            //publish to gateway if skipGateway is disabled only
                            if (!api.getSkipGateway()) {
                                publishToGateway(apiPublished);
                            }
                        }
                    } else {
                        log.debug("Gateway is not existed for the current WebApp Provider");
                    }
                }
               
                /*Boolean gatewayKeyCacheEnabled=false;
                String gatewayKeyCacheEnabledString = config.getFirstProperty(AppMConstants.API_GATEWAY_KEY_CACHE_ENABLED);
                //If gateway key cache enabled
                if (gatewayKeyCacheEnabledString != null) {
                    gatewayKeyCacheEnabled = Boolean.parseBoolean(gatewayKeyCacheEnabledString);
                }
                //If resource paths being saved are on permission cache, remove them.
                if (gatewayExists && gatewayKeyCacheEnabled) {
                    if (isAPIPublished && !oldApi.getUriTemplates().equals(api.getUriTemplates())) {
                        Set<URITemplate> resourceVerbs = api.getUriTemplates();

                        List<Environment> gatewayEnvs = config.getApiGatewayEnvironments();
                        for(Environment environment : gatewayEnvs){
                            APIAuthenticationAdminClient client =
                                    new APIAuthenticationAdminClient(environment);
                            if(resourceVerbs != null){
                                for(URITemplate resourceVerb : resourceVerbs){
                                    String resourceURLContext = resourceVerb.getUriTemplate();
                                    //If url context ends with the '*' character.
                                    if(resourceURLContext.endsWith("*")){
                                        //Remove the ending '*'
                                        resourceURLContext = resourceURLContext.substring(0, resourceURLContext.length() - 1);
                                    }
                                    client.invalidateResourceCache(api.getContext(),api.getId().getVersion(),resourceURLContext,resourceVerb.getHTTPVerb());
                                    if (log.isDebugEnabled()) {
                                        log.debug("Calling invalidation cache");
                                    }
                                }
                            }
                        }

                    }
                }*/
                /* Update WebApp Definition for Swagger
                createUpdateAPIDefinition(api);*/

                //update apiContext cache
                if (AppManagerUtil.isAPIManagementEnabled()) {
                    Cache contextCache = AppManagerUtil.getAPIContextCache();
                    contextCache.remove(oldApi.getContext());
                    contextCache.put(api.getContext(), true);
                }

            } catch (AppManagementException e) {
            	handleException("Error while updating the WebApp :" +api.getId().getApiName(),e);
            }

        } else {
            // We don't allow WebApp status updates via this method.
            // Use changeAPIStatus for that kind of updates.
            throw new AppManagementException("Invalid WebApp update operation involving WebApp status changes");
        }
    }



    private void updateApiArtifact(WebApp api, boolean updateMetadata,boolean updatePermissions) throws
                                                                                                 AppManagementException {

        //Validate Transports
        validateAndSetTransports(api);

        try {
        	registry.beginTransaction();
            String apiArtifactId = registry.get(AppManagerUtil.getAPIPath(api.getId())).getUUID();
            GenericArtifactManager artifactManager = AppManagerUtil.getArtifactManager(registry,
                                                                                AppMConstants.API_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
            GenericArtifact updateApiArtifact = AppManagerUtil.createAPIArtifactContent(artifact, api);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, updateApiArtifact.getId());
            org.wso2.carbon.registry.core.Tag[] oldTags = registry.getTags(artifactPath);
            if (oldTags != null) {
                for (org.wso2.carbon.registry.core.Tag tag : oldTags) {
                    registry.removeTag(artifactPath, tag.getTagName());
                }
            }

            Set<String> tagSet = api.getTags();
            if (tagSet != null) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }
          

            if (updateMetadata) {
            	
                if (api.getWsdlUrl() != null && !"".equals(api.getWsdlUrl())) {
                    String path = AppManagerUtil.createWSDL(registry, api);
                    if (path != null) {
                        registry.addAssociation(artifactPath, path, CommonConstants.ASSOCIATION_TYPE01);
                        updateApiArtifact.setAttribute(AppMConstants.API_OVERVIEW_WSDL, api.getWsdlUrl()); //reset the wsdl path to permlink
                    }
                }

                if (api.getUrl() != null && !"".equals(api.getUrl())){
                    String path = AppManagerUtil.createEndpoint(api.getUrl(), registry);
                    if (path != null) {
                        registry.addAssociation(artifactPath, path, CommonConstants.ASSOCIATION_TYPE01);
                    }
                }
            }
            
            artifactManager.updateGenericArtifact(updateApiArtifact);
            
            //write WebApp Status to a separate property. This is done to support querying APIs using custom query (SQL)
            //to gain performance
            String apiStatus = api.getStatus().getStatus();
            saveAPIStatus(artifactPath, apiStatus);
            if(updatePermissions){
                clearResourcePermissions(artifactPath, api.getId());
                String visibleRolesList = api.getVisibleRoles();
                String[] visibleRoles = new String[0];
                if (visibleRolesList != null) {
                    visibleRoles = visibleRolesList.split(",");
                }
                AppManagerUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),visibleRoles,artifactPath);
            }
            registry.commitTransaction();
        } catch (Exception e) {
        	 try {
                 registry.rollbackTransaction();
             } catch (RegistryException re) {
                 handleException("Error while rolling back the transaction for WebApp: " +
                                 api.getId().getApiName(), re);
             }
             handleException("Error while performing registry transaction operation", e);
           
        }
    }
    
    /**
     * Create WebApp Definition in JSON and save in the registry
     *
     * @param api WebApp
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to generate the content and save
     */
    private void createUpdateAPIDefinition(WebApp api) throws AppManagementException {
    	APIIdentifier identifier = api.getId(); 
    	
    	try{
    		String jsonText = AppManagerUtil.createSwaggerJSONContent(api);
    		
    		String resourcePath = AppManagerUtil.getAPIDefinitionFilePath(identifier.getApiName(), identifier.getVersion());
    		
    		Resource resource = registry.newResource();
    		    		
    		resource.setContent(jsonText);
    		resource.setMediaType("application/json");
    		registry.put(resourcePath, resource);
    		
    		/*Set permissions to anonymous role */
    		AppManagerUtil.setResourcePermissions(api.getId().getProviderName(), null, null, resourcePath);
    			    
    	} catch (RegistryException e) {
    		handleException("Error while adding WebApp Definition for " + identifier.getApiName() + "-" + identifier.getVersion(), e);
		} catch (AppManagementException e) {
			handleException("Error while adding WebApp Definition for " + identifier.getApiName() + "-" + identifier.getVersion(), e);
		}
    }
    
    
    @Override
    public void changeAPIStatus(WebApp api, APIStatus status, String userId,
                                boolean updateGatewayConfig) throws AppManagementException {
        APIStatus currentStatus = api.getStatus();
        if (!currentStatus.equals(status)) {
            api.setStatus(status);
            try {                
            	//                updateApiArtifact(api, false,false);
            	//                appMDAO.recordAPILifeCycleEvent(api.getId(), currentStatus, status, userId);

                APIStatusObserverList observerList = APIStatusObserverList.getInstance();
                observerList.notifyObservers(currentStatus, status, api);
                AppManagerConfiguration config = ServiceReferenceHolder.getInstance().
                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
                String gatewayType = config.getFirstProperty(AppMConstants.API_GATEWAY_TYPE);

                if (gatewayType.equalsIgnoreCase(AppMConstants.API_GATEWAY_TYPE_SYNAPSE) && updateGatewayConfig) {
                    if (status.equals(APIStatus.PUBLISHED) || status.equals(APIStatus.DEPRECATED) ||
                        status.equals(APIStatus.BLOCKED)) {

                        //publish to gateway if skipGateway is disabled only
                        if (!api.getSkipGateway()) {
                            publishToGateway(api);
                        }
                    } else {
                        removeFromGateway(api);
                    }
                }
               
            } catch (AppManagementException e) {
            	handleException("Error occured in the status change : " + api.getId().getApiName() , e);
            }
        }
    }

    public void updateWebAppSynapse(WebApp api) throws AppManagementException {
       removeFromGateway(api);
    }

    private void publishToGateway(WebApp api) throws AppManagementException {
        APITemplateBuilder builder = null;
        String tenantDomain = null;
//        if (api.getId().getProviderName().contains("AT")) {
            String provider = api.getId().getProviderName().replace("-AT-", "@");
            tenantDomain = MultitenantUtils.getTenantDomain( provider);
//        }

        try{
            builder = getAPITemplateBuilder(api);
        }catch(Exception e){
            handleException("Error while publishing to Gateway ", e);
        }


        APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
        try {
            gatewayManager.publishToGateway(api, builder, tenantDomain);
        } catch (Exception e) {
            handleException("Error while publishing to Gateway ", e);
        }
    }

    private void validateAndSetTransports(WebApp api) throws AppManagementException {
        String transports = api.getTransports();
        if(transports != null && !("null".equalsIgnoreCase(transports))){
            if (transports.contains(",")) {
                StringTokenizer st = new StringTokenizer(transports, ",");
                while (st.hasMoreTokens()) {
                    checkIfValidTransport(st.nextToken());
                }
            }else{
                checkIfValidTransport(transports);
            }
        }else{
            api.setTransports(Constants.TRANSPORT_HTTP+","+Constants.TRANSPORT_HTTPS);
            return;
        }
    }

    private void checkIfValidTransport(String transport) throws AppManagementException {
        if(!Constants.TRANSPORT_HTTP.equalsIgnoreCase(transport) && !Constants.TRANSPORT_HTTPS.equalsIgnoreCase(transport)){
            handleException("Unsupported Transport [" + transport + "]");
        }
    }

    private void removeFromGateway(WebApp api) throws AppManagementException {
        String tenantDomain = null;
        if (api.getId().getProviderName().contains("@")) {
            tenantDomain = MultitenantUtils.getTenantDomain( api.getId().getProviderName());
        }

        APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
        try {
            gatewayManager.removeFromGateway(api, tenantDomain);
        } catch (Exception e) {
            handleException("Error while removing WebApp from Gateway ", e);
        }
    }

    private boolean isAPIPublished(WebApp api) throws AppManagementException {
        try {
            String tenantDomain = null;
			if (api.getId().getProviderName().contains("AT")) {
				String provider = api.getId().getProviderName().replace("-AT-", "@");
				tenantDomain = MultitenantUtils.getTenantDomain( provider);			
			}
            APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
            return gatewayManager.isAPIPublished(api, tenantDomain);
        } catch (Exception e) {
            handleException("Error while checking WebApp status", e);
        }
		return false;
    }

    /**
     * This method dynamically returns the mandatory and selected java policy handlers list for given app
     *
     * @param api :WebApp class which contains details about web applications
     * @return :handlers list with properties to be applied
     * @throws AppManagementException on error
     */
    private APITemplateBuilder getAPITemplateBuilder(WebApp api) throws AppManagementException {
        APITemplateBuilderImpl velocityTemplateBuilder = new APITemplateBuilderImpl(api);

        //List of JavaPolicy class which contains policy related details
        List<JavaPolicy> policies = new ArrayList<JavaPolicy>();
        //contains properties related to relevant policy and will be used to generate the synapse api config file
        Map<String, String> properties;
        int counterPolicies; //counter :policies

        try {
            //fetch all the java policy handlers details which need to be included to synapse api config file
            policies = appMDAO.getMappedJavaPolicyList(api.getUUID(),true);
            //loop through each policy
            for (counterPolicies = 0; counterPolicies < policies.size(); counterPolicies++) {
                if (policies.get(counterPolicies).getProperties() == null) {
                    //if policy doesn't contain any properties assign an empty map and add java policy as a handler
                    velocityTemplateBuilder.addHandler(policies.get(counterPolicies).getFullQualifiName(),
                            Collections.EMPTY_MAP);
                } else {
                    //contains properties related to all the policies
                    JSONObject objPolicyProperties;
                    properties = new HashMap<String, String>();

                    //get property JSON object related to current policy in the loop
                    objPolicyProperties = policies.get(counterPolicies).getProperties();

                    //if policy contains any properties, run a loop and assign them
                    Set<String> keys = objPolicyProperties.keySet();
                    for (String key : keys) {
                        properties.put(key, objPolicyProperties.get(key).toString());
                    }
                    //add policy as a handler and also the relevant properties
                    velocityTemplateBuilder.addHandler(policies.get(counterPolicies).getFullQualifiName(), properties);
                }
            }

        } catch (AppManagementException e) {
            handleException("Error occurred while adding java policy handlers to Application : " +
                    api.getId().toString(), e);
        }
        return velocityTemplateBuilder;
    }

    /**
     * @param webapp     origin web application
     * @param newVersion The version of the new WebApp
     * @throws AppManagementException
     */
    @Override
    public void copyWebappDocumentations(WebApp webapp, String newVersion) throws AppManagementException {

        try {

            // Retain the docs
            List<Documentation> docs = getAllDocumentation(webapp.getId());
            APIIdentifier newId = new APIIdentifier(webapp.getId().getProviderName(),
                    webapp.getId().getApiName(), newVersion);
            WebApp newAPI = getAPI(newId, webapp.getId());

            if (log.isDebugEnabled()) {
                log.debug("Copying documenatation of the web application - " + webapp.getApiName() +
                        "with the new version - " + newVersion);
            }

            for (Documentation doc : docs) {

			    /* copying the file in registry for new api */
                Documentation.DocumentSourceType sourceType = doc.getSourceType();
                if (sourceType == Documentation.DocumentSourceType.FILE) {
                    String absoluteSourceFilePath = doc.getFilePath();
                    // extract the prepend
                    // ->/registry/resource/_system/governance/ and for
                    // tenant
                    // /t/my.com/registry/resource/_system/governance/
                    int prependIndex =
                            absoluteSourceFilePath.indexOf(AppMConstants.API_LOCATION);
                    String prependPath = absoluteSourceFilePath.substring(0, prependIndex);
                    // get the file name from absolute file path
                    int fileNameIndex =
                            absoluteSourceFilePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR);
                    String fileName = absoluteSourceFilePath.substring(fileNameIndex + 1);
                    // create relative file path of old location
                    String sourceFilePath = absoluteSourceFilePath.substring(prependIndex);
                    // create the relative file path where file should be
                    // copied
                    String targetFilePath =
                            AppMConstants.API_LOCATION +
                                    RegistryConstants.PATH_SEPARATOR +
                                    newId.getProviderName() +
                                    RegistryConstants.PATH_SEPARATOR +
                                    newId.getApiName() +
                                    RegistryConstants.PATH_SEPARATOR +
                                    newId.getVersion() +
                                    RegistryConstants.PATH_SEPARATOR +
                                    AppMConstants.DOC_DIR +
                                    RegistryConstants.PATH_SEPARATOR +
                                    AppMConstants.DOCUMENT_FILE_DIR +
                                    RegistryConstants.PATH_SEPARATOR + fileName;
                    // copy the file from old location to new location(for
                    // new api)

                    registry.copy(sourceFilePath, targetFilePath);

                    // update the filepath attribute in doc artifact to
                    // create new doc artifact for new version of api
                    doc.setFilePath(prependPath + targetFilePath);
                }

                createDocumentation(newAPI.getId(), doc);
                String content = getDocumentationContent(webapp.getId(), doc.getName());
                if (content != null) {
                    addDocumentationContent(newAPI.getId(), doc.getName(), content);
                }
            }
        } catch (RegistryException e) {
            handleException("Error occurred while copying web application : " + webapp.getApiName());
        }
    }

    /**
     * Removes a given documentation
     * @param apiId   APIIdentifier
     * @param docName name of the document
     * @param docType the type of the documentation
     * @throws AppManagementException
     */
    @Override
    public void removeDocumentation(APIIdentifier apiId, String docName, String docType)
            throws AppManagementException {
        String docPath = AppManagerUtil.getAPIDocPath(apiId) + docName;

        try {
            String apiArtifactId = registry.get(docPath).getUUID();
            GenericArtifactManager artifactManager = AppManagerUtil.getArtifactManager(registry,
                                                                                AppMConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
            String docFilePath =  artifact.getAttribute(AppMConstants.DOC_FILE_PATH);

            if(docFilePath!=null)
            {
                File tempFile = new File(docFilePath);
                String fileName = tempFile.getName();
                docFilePath = AppManagerUtil.getDocumentationFilePath(apiId,fileName);
                if(registry.resourceExists(docFilePath))
                {
                    registry.delete(docFilePath);
                }
            }

            Association[] associations = registry.getAssociations(docPath,
                                                                  AppMConstants.DOCUMENTATION_KEY);
            for (Association association : associations) {
                registry.delete(association.getDestinationPath());
            }
            String docContentPath = AppManagerUtil.getAPIDocContentPath(apiId, docName);

            //Remove Inline-documentation contents
            if (registry.resourceExists(docContentPath)) {
                registry.delete(docContentPath);
            }

        } catch (RegistryException e) {
            handleException("Failed to delete documentation", e);
        }
    }

    /**
     * Adds Documentation to an WebApp
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add documentation
     */
    @Override
    public void addDocumentation(APIIdentifier apiId, Documentation documentation)
            throws AppManagementException {
        createDocumentation(apiId, documentation);
    }

    /**
     * This method used to save the documentation content
     *
     * @param identifier,        WebApp identifier
     * @param documentationName, name of the inline documentation
     * @param text,              content of the inline documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add the document as a resource to registry
     */
    public void addDocumentationContent(APIIdentifier identifier, String documentationName, String text)
            throws AppManagementException {

        String documentationPath = AppManagerUtil.getAPIDocPath(identifier) + documentationName;
        String contentPath = AppManagerUtil.getAPIDocPath(identifier) + AppMConstants.INLINE_DOCUMENT_CONTENT_DIR +
                             RegistryConstants.PATH_SEPARATOR + documentationName;
        try {
            Resource docContent;
            if (!registry.resourceExists(contentPath)) {
            	docContent = registry.newResource();
            } else {
            	docContent = registry.get(contentPath);
            }

            /* This is a temporary fix for doc content replace issue. We need to add
             * separate methods to add inline content resource in document update */
            if (!AppMConstants.NO_CONTENT_UPDATE.equals(text)) {
            	docContent.setContent(text);
            }

            docContent.setMediaType(AppMConstants.DOCUMENTATION_INLINE_CONTENT_TYPE);
            registry.put(contentPath, docContent);
            registry.addAssociation(documentationPath, contentPath,
                                    AppMConstants.DOCUMENTATION_CONTENT_ASSOCIATION);
            String[] authorizedRoles = getAuthorizedRoles(documentationPath);
            String apiPath = AppManagerUtil.getAPIPath(identifier);
            AppManagerUtil.setResourcePermissions(getAPI(apiPath).getId().getProviderName(),
            		getAPI(apiPath).getVisibility(),authorizedRoles,contentPath);
        } catch (RegistryException e) {
            String msg = "Failed to add the documentation content of : "
                         + documentationName + " of WebApp :" + identifier.getApiName();
            handleException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Failed to add the documentation content of : "
                         + documentationName + " of WebApp :" + identifier.getApiName();
            handleException(msg, e);
        }
    }

    /**
     * This method used to update the WebApp definition content - Swagger
     *
     * @param identifier,        WebApp identifier
     * @param documentationName, name of the inline documentation
     * @param text,              content of the inline documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add the document as a resource to registry
     */
    public void addAPIDefinitionContent(APIIdentifier identifier, String documentationName, String text) 
    					throws AppManagementException {
    	String contentPath = AppManagerUtil.getAPIDefinitionFilePath(identifier.getApiName(), identifier.getVersion());
    	
    	try {
            Resource docContent = registry.newResource();
            docContent.setContent(text);
            docContent.setMediaType("text/plain");
            registry.put(contentPath, docContent);
            
            String apiPath = AppManagerUtil.getAPIPath(identifier);
            WebApp api = getAPI(apiPath);
            String visibleRolesList = api.getVisibleRoles();
            String[] visibleRoles = new String[0];
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }
    		AppManagerUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(), visibleRoles, contentPath);
    	} catch (RegistryException e) {
            String msg = "Failed to add the WebApp Definition content of : "
                         + documentationName + " of WebApp :" + identifier.getApiName();
            handleException(msg, e);
        } 
    }

    /**
     * Updates a given documentation
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     *          if failed to update docs
     */
    public void updateDocumentation(APIIdentifier apiId, Documentation documentation)
            throws AppManagementException {

        String docPath = AppMConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                         apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
                         RegistryConstants.PATH_SEPARATOR + apiId.getVersion() + RegistryConstants.PATH_SEPARATOR +
                         AppMConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR + documentation.getName();
        try {
            String apiArtifactId = registry.get(docPath).getUUID();
            GenericArtifactManager artifactManager = AppManagerUtil.getArtifactManager(registry,
                                                                                AppMConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
            String apiPath = AppManagerUtil.getAPIPath(apiId);
            GenericArtifact updateApiArtifact = AppManagerUtil.createDocArtifactContent(artifact, apiId, documentation);
            artifactManager.updateGenericArtifact(updateApiArtifact);
            clearResourcePermissions(docPath, apiId);

            WebApp api=getAPI(apiPath);
            String visibleRolesList = api.getVisibleRoles();
            String[] visibleRoles = new String[0];
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }

            AppManagerUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),visibleRoles,artifact.getPath());
            
            String docFilePath = artifact.getAttribute(AppMConstants.DOC_FILE_PATH);
            if(docFilePath != null && !docFilePath.equals("")) {
                //The docFilePatch comes as /t/tenanatdoman/registry/resource/_system/governance/apimgt/applicationdata..
                //We need to remove the /t/tenanatdoman/registry/resource/_system/governance section to set permissions.
                int startIndex = docFilePath.indexOf("governance") + "governance".length();
                String filePath = docFilePath.substring(startIndex, docFilePath.length());
                AppManagerUtil.setResourcePermissions(getAPI(apiPath).getId().getProviderName(),
                        getAPI(apiPath).getVisibility(), visibleRoles, filePath);
            }

        } catch (RegistryException e) {
            handleException("Failed to update documentation", e);
        } 

    }

    /**
     * Copies current Documentation into another version of the same WebApp.
     *
     * @param toVersion Version to which Documentation should be copied.
     * @param apiId     id of the APIIdentifier
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to copy docs
     */
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion)
            throws AppManagementException {

        String oldVersion = AppManagerUtil.getAPIDocPath(apiId);
        String newVersion = AppMConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                            apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
                            RegistryConstants.PATH_SEPARATOR + toVersion + RegistryConstants.PATH_SEPARATOR +
                            AppMConstants.DOC_DIR;

        try {
            Resource resource = registry.get(oldVersion);
            if (resource instanceof org.wso2.carbon.registry.core.Collection) {
                String[] docsPaths = ((org.wso2.carbon.registry.core.Collection) resource).getChildren();
                for (String docPath : docsPaths) {
                    registry.copy(docPath, newVersion);
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to copy docs to new version : " + newVersion, e);
        }
    }

    /**
     * Create an Api
     *
     * @param api WebApp
     * @throws org.wso2.carbon.appmgt.api.AppManagementException if failed to create WebApp
     */
    private void createAPI(WebApp api) throws AppManagementException {
        GenericArtifactManager artifactManager = AppManagerUtil.getArtifactManager(registry,
                                                                            AppMConstants.API_KEY);

        //Validate Transports
        validateAndSetTransports(api);
        try {
            registry.beginTransaction();
            GenericArtifact genericArtifact =
                    artifactManager.newGovernanceArtifact(new QName(api.getId().getApiName()));
            GenericArtifact artifact = AppManagerUtil.createAPIArtifactContent(genericArtifact, api);
            artifactManager.addGenericArtifact(artifact);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            String providerPath = AppManagerUtil.getAPIProviderPath(api.getId());
            //provider ------provides----> WebApp
            registry.addAssociation(providerPath, artifactPath, AppMConstants.PROVIDER_ASSOCIATION);
            Set<String> tagSet = api.getTags();
            if (tagSet != null && tagSet.size() > 0) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }           
            if (api.getWsdlUrl() != null && !"".equals(api.getWsdlUrl())) {
                String path = AppManagerUtil.createWSDL(registry, api);
                if (path != null) {
                    registry.addAssociation(artifactPath, path, CommonConstants.ASSOCIATION_TYPE01);
                    artifact.setAttribute(AppMConstants.API_OVERVIEW_WSDL, api.getWsdlUrl()); //reset the wsdl path to permlink
                    artifactManager.updateGenericArtifact(artifact); //update the  artifact
                }
            }

            if (api.getUrl() != null && !"".equals(api.getUrl())){
                String path = AppManagerUtil.createEndpoint(api.getUrl(), registry);
                if (path != null) {
                    registry.addAssociation(artifactPath, path, CommonConstants.ASSOCIATION_TYPE01);
                }
            }
            //write WebApp Status to a separate property. This is done to support querying APIs using custom query (SQL)
            //to gain performance
            String apiStatus = api.getStatus().getStatus();
            saveAPIStatus(artifactPath, apiStatus);
            String visibleRolesList = api.getVisibleRoles();
            String[] visibleRoles = new String[0];
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }
            AppManagerUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(), visibleRoles, artifactPath);
            registry.commitTransaction();

            /* Generate WebApp Definition for Swagger */
            createUpdateAPIDefinition(api);

        } catch (Exception e) {
        	 try {
                 registry.rollbackTransaction();
             } catch (RegistryException re) {
                 handleException("Error while rolling back the transaction for WebApp: " +
                                 api.getId().getApiName(), re);
             }
             handleException("Error while performing registry transaction operation", e);
        }
        
    }

    /**
     * This function is to set resource permissions based on its visibility
     *
     * @param artifactPath WebApp resource path
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Throwing exception
     */
    private void clearResourcePermissions(String artifactPath, APIIdentifier apiId)
            throws AppManagementException {
        try {
            String resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH
                            + artifactPath);
            String tenantDomain = MultitenantUtils.getTenantDomain(
                    AppManagerUtil.replaceEmailDomainBack(apiId.getProviderName()));
            if (!tenantDomain.equals(org.wso2.carbon.utils.multitenancy.
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                AuthorizationManager authManager = ServiceReferenceHolder.getInstance().
                        getRealmService().getTenantUserRealm(((UserRegistry) registry).getTenantId()).
                        getAuthorizationManager();
                authManager.clearResourceAuthorizations(resourcePath);
            } else {
                RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager(ServiceReferenceHolder.getUserRealm());
                authorizationManager.clearResourceAuthorizations(resourcePath);
            }
        } catch (UserStoreException e) {
            handleException("Error while adding role permissions to WebApp", e);
        }
    }
    /**
     * Create a documentation
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws org.wso2.carbon.appmgt.api.AppManagementException if failed to add documentation
     */
    private void createDocumentation(APIIdentifier apiId, Documentation documentation)
            throws AppManagementException {
        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry,
                                                                                AppMConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact =
                    artifactManager.newGovernanceArtifact(new QName(documentation.getName()));
            artifactManager.addGenericArtifact(
                    AppManagerUtil.createDocArtifactContent(artifact, apiId, documentation));
            String apiPath = AppManagerUtil.getAPIPath(apiId);
            //Adding association from api to documentation . (WebApp -----> doc)
            registry.addAssociation(apiPath, artifact.getPath(),
                                    AppMConstants.DOCUMENTATION_ASSOCIATION);
            String[] authorizedRoles=getAuthorizedRoles(apiPath);
            AppManagerUtil.setResourcePermissions(getAPI(apiPath).getId().getProviderName(),
            		getAPI(apiPath).getVisibility(), authorizedRoles, artifact.getPath());
            
            String docFilePath = artifact.getAttribute(AppMConstants.DOC_FILE_PATH);
            if(docFilePath != null && !docFilePath.equals("")){
                //The docFilePatch comes as /t/tenanatdoman/registry/resource/_system/governance/apimgt/applicationdata..
                //We need to remove the /t/tenanatdoman/registry/resource/_system/governance section to set permissions.
                int startIndex = docFilePath.indexOf("governance") + "governance".length();
                String filePath = docFilePath.substring(startIndex, docFilePath.length());
                AppManagerUtil.setResourcePermissions(getAPI(apiPath).getId().getProviderName(),
                        getAPI(apiPath).getVisibility(), authorizedRoles, filePath);
            }
        } catch (RegistryException e) {
            handleException("Failed to add documentation", e);
        } catch (UserStoreException e) {
            handleException("Failed to add documentation", e);
        }
    }

    private String[] getAuthorizedRoles(String artifactPath) throws UserStoreException {
        String  resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                             RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH
                                                             + artifactPath);
        RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager
                (ServiceReferenceHolder.getUserRealm());
        return authorizationManager.getAllowedRolesForResource(resourcePath,ActionConstants.GET);
    }

    /**
     * Returns the details of all the life-cycle changes done per api
     *
     * @param apiId WebApp Identifier
     * @return List of lifecycle events per given api
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to get Lifecycle Events
     */
    public List<LifeCycleEvent> getLifeCycleEvents(APIIdentifier apiId) throws
                                                                        AppManagementException {
        return appMDAO.getLifeCycleEvents(apiId);
    }

    /**
     * Update the subscription status
     *
     * @param apiId WebApp Identifier
     * @param subStatus Subscription Status
     * @param appId Application Id              *
     * @return int value with subscription id
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */
    public void updateSubscription(APIIdentifier apiId,String subStatus,int appId) throws
                                                                                   AppManagementException {
        appMDAO.updateSubscription(apiId,subStatus,appId);
    }

	/**
	 * Moves subscriptions of one app to another app
	 *
	 * @param fromIdentifier subscriptions of this app
	 * @param toIdentifier   will be moved into this app
	 * @return number of subscriptions moved
	 * @throws AppManagementException
	 */
	@Override
	public int moveSubscriptions(APIIdentifier fromIdentifier, APIIdentifier toIdentifier) throws
																						   AppManagementException {
		return appMDAO.moveSubscriptions(fromIdentifier, toIdentifier);
	}

    /**
     * Delete applicatoion
     * @param identifier AppIdentifier
     * @param ssoProvider SSO provider
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public boolean deleteApp(APIIdentifier identifier, SSOProvider ssoProvider) throws
                                                                                AppManagementException {

        SSOConfiguratorUtil ssoConfiguratorUtil;
        String path = AppMConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                      identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                      identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();

        String appArtifactPath = AppManagerUtil.getAPIPath(identifier);
        boolean isAppDeleted = false;

        try {
            long subsCount = appMDAO.getAPISubscriptionCountByAPI(identifier);
            Resource appArtifactResource = registry.get(appArtifactPath);
            String applicationStatus = appArtifactResource.getProperty(AppMConstants.APP_RESOURCE_STATUS);
            if (subsCount > 0 && !applicationStatus.equals("Retired")) {
               return isAppDeleted;
            }

            //If SSOProvider exists, remove it
            if (ssoProvider != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Removing the SSO Provider with name : " + ssoProvider.getProviderName());
                }
                ssoConfiguratorUtil = new SSOConfiguratorUtil();
                ssoConfiguratorUtil.deleteSSOProvider(ssoProvider);
            }

            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            GenericArtifactManager artifactManager = AppManagerUtil.getArtifactManager(registry,
                                                                                AppMConstants.API_KEY);
            Resource appResource = registry.get(path);
            String artifactId = appResource.getUUID();


            String appArtifactResourceId = appArtifactResource.getUUID();
            if (artifactId == null) {
                throw new AppManagementException("artifact id is null for : " + path);
            }

            GenericArtifact appArtifact = artifactManager.getGenericArtifact(appArtifactResourceId);
            String inSequence = appArtifact.getAttribute(AppMConstants.API_OVERVIEW_INSEQUENCE);
            String outSequence = appArtifact.getAttribute(AppMConstants.API_OVERVIEW_OUTSEQUENCE);

            //Delete the dependencies associated  with the api artifact
            GovernanceArtifact[] dependenciesArray = appArtifact.getDependencies();

            if (dependenciesArray.length > 0) {
                for (int i = 0; i < dependenciesArray.length; i++) {
                    registry.delete(dependenciesArray[i].getPath());
                }
            }

            artifactManager.removeGenericArtifact(artifactId);

            String thumbPath = AppManagerUtil.getIconPath(identifier);
            if (registry.resourceExists(thumbPath)) {
                registry.delete(thumbPath);
            }

            AppManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            boolean gatewayExists = config.getApiGatewayEnvironments().size() > 0;
            String gatewayType = config.getFirstProperty(AppMConstants.API_GATEWAY_TYPE);

            WebApp webapp = new WebApp(identifier);
            // gatewayType check is required when WebApp Management is deployed on other servers to avoid synapse
            if (gatewayExists && gatewayType.equals("Synapse")) {
                webapp.setInSequence(inSequence); //need to remove the custom sequences
                webapp.setOutSequence(outSequence);
                removeFromGateway(webapp);
            } else {
                if(log.isDebugEnabled()) {
                    log.debug("Gateway is not existed for the current applications Provider");
                }
            }
            appMDAO.deleteAPI(identifier);

            /*remove empty directories*/
            String appCollectionPath = AppMConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                                       identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                                       identifier.getApiName();
            if (registry.resourceExists(appCollectionPath)) {
                Resource appCollection = registry.get(appCollectionPath);
                CollectionImpl collection = (CollectionImpl) appCollection;
                //if there is no other versions of applications delete the directory of the applications
                if (collection.getChildCount() == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("No more versions of the applications found, removing applications collection from registry");
                    }
                    registry.delete(appCollectionPath);
                }
            }

            String appProviderPath = AppMConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    identifier.getProviderName();
            if (registry.resourceExists(appProviderPath)) {
                Resource providerCollection = registry.get(appProviderPath);
                CollectionImpl collection = (CollectionImpl) providerCollection;
                //if there is no applications for given provider delete the provider directory
                if (collection.getChildCount() == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("No more Applications from the provider " + identifier.getProviderName() + " found. " +
                                "Removing provider collection from registry");
                    }
                    registry.delete(appProviderPath);
                }
            }
            isAppDeleted = true;
        } catch (RegistryException e) {
            handleException("Failed to remove the WebApp from : " + path, e);
        }

        return isAppDeleted;
    }

    public List<WebApp> searchAPIs(String searchTerm, String searchType, String providerId) throws
                                                                                            AppManagementException {
        List<WebApp> apiSortedList = new ArrayList<WebApp>();
        String regex = "(?i)[\\w.|-]*" + searchTerm.trim() + "[\\w.|-]*";
		      
        Pattern pattern;
        Matcher matcher;
        try {
            List<WebApp> apiList;
            if(providerId!=null){
                apiList= getAPIsByProvider(providerId);
            }else{
                apiList= getAllAPIs();
            }
            if (apiList == null || apiList.size() == 0) {
                return apiSortedList;
            }
            pattern = Pattern.compile(regex);
            for (WebApp api : apiList) {

                if (searchType.equalsIgnoreCase("Name")) {
                    String api1 = api.getId().getApiName();
                    matcher = pattern.matcher(api1);
                }else if (searchType.equalsIgnoreCase("Provider")) {
                    String api1 = api.getId().getProviderName();
                    matcher = pattern.matcher(api1);
                } else if (searchType.equalsIgnoreCase("Version")) {
                    String api1 = api.getId().getVersion();
                    matcher = pattern.matcher(api1);
                } else if (searchType.equalsIgnoreCase("Context")) {
                    String api1 = api.getContext();
                    matcher = pattern.matcher(api1);
                } else {
                    String apiName = api.getId().getApiName();
                    matcher = pattern.matcher(apiName);
                }
                
                if (matcher.find()) {                 	
                    apiSortedList.add(api);
                }

            }
        } catch (AppManagementException e) {
            handleException("Failed to search APIs with type", e);
        }
        Collections.sort(apiSortedList, new APINameComparator());
        return apiSortedList;
    }

    /**
     * Update the Tier Permissions
     *
     * @param tierName Tier Name
     * @param permissionType Permission Type
     * @param roles Roles
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */
    public void updateTierPermissions(String tierName, String permissionType, String roles) throws
                                                                                            AppManagementException {
        appMDAO.updateTierPermissions(tierName, permissionType, roles, tenantId);
    }

	@Override
	public Set<TierPermissionDTO> getTierPermissions() throws AppManagementException {
		Set<TierPermissionDTO> tierPermissions = appMDAO.getTierPermissions(tenantId);
		return tierPermissions;
	}

	/**
	 * Get stored custom inSequences from governanceSystem registry
	 *
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 */

	public List<String> getCustomInSequences() throws AppManagementException {

		List<String> sequenceList = new ArrayList<String>();
		try {
			UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
			                                              .getGovernanceSystemRegistry(tenantId);
			if (registry.resourceExists(AppMConstants.API_CUSTOM_INSEQUENCE_LOCATION)) {
	            org.wso2.carbon.registry.api.Collection inSeqCollection =
	                                                                      (org.wso2.carbon.registry.api.Collection) registry.get(AppMConstants.API_CUSTOM_INSEQUENCE_LOCATION);
	            if (inSeqCollection != null) {
	             //   SequenceMediatorFactory factory = new SequenceMediatorFactory();
	                String[] inSeqChildPaths = inSeqCollection.getChildren();
	                for (int i = 0; i < inSeqChildPaths.length; i++) {
		                Resource inSequence = registry.get(inSeqChildPaths[i]);
		                OMElement seqElment = AppManagerUtil.buildOMElement(inSequence.getContentStream());
		                sequenceList.add(seqElment.getAttributeValue(new QName("name")));		               
	                }
                }
            }

		} catch (Exception e) {
			handleException("Issue is in getting custom InSequences from the Registry", e);
		}
		return sequenceList;
	}

	/**
	 * Get stored custom outSequences from governanceSystem registry
	 *
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 */

	public List<String> getCustomOutSequences() throws AppManagementException {

		List<String> sequenceList = new ArrayList<String>();
		try {
			UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
			                                              .getGovernanceSystemRegistry(tenantId);
			if (registry.resourceExists(AppMConstants.API_CUSTOM_OUTSEQUENCE_LOCATION)) {
	            org.wso2.carbon.registry.api.Collection outSeqCollection =
	                                                                       (org.wso2.carbon.registry.api.Collection) registry.get(AppMConstants.API_CUSTOM_OUTSEQUENCE_LOCATION);
	            if (outSeqCollection !=null) {
	                String[] outSeqChildPaths = outSeqCollection.getChildren();
	                for (int i = 0; i < outSeqChildPaths.length; i++) {
		                Resource outSequence = registry.get(outSeqChildPaths[i]);
		                OMElement seqElment = AppManagerUtil.buildOMElement(outSequence.getContentStream());
		         
		                sequenceList.add(seqElment.getAttributeValue(new QName("name")));		               
	                }
                }
            }

		} catch (Exception e) {
			handleException("Issue is in getting custom OutSequences from the Registry", e);
		}
		return sequenceList;
	}

	@Override
	public boolean isSynapseGateway() throws AppManagementException {
		AppManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
		String gatewayType = config.getFirstProperty(AppMConstants.API_GATEWAY_TYPE);
        if (!gatewayType.equalsIgnoreCase(AppMConstants.API_GATEWAY_TYPE_SYNAPSE)) {
        	return false;
        }
		return true;
	}

    @Override
    public List<WebApp> getAllWebApps() throws AppManagementException {
        return appMDAO.getAllWebApps();
    }

    @Override
    public List<WebApp> getAllWebApps(String tenantDomain) throws AppManagementException {
        return appMDAO.getAllWebApps(tenantDomain);
    }

    @Override
    public Map<String, Long> getSubscriptionCountByAPPs(String provider, String fromDate, String toDate)
            throws AppManagementException {
        Map<String,Long> subscriptions = null;
        try {
            subscriptions = appMDAO.GetSubscriptionCountByApp(provider, fromDate, toDate, tenantId);
        } catch (AppManagementException e) {
            handleException("Failed to get subscriptionCount by apps for provider :" + provider + "for the period "
                    + fromDate + "to" + toDate, e);
        }
        return subscriptions;
    }

    public List<WebApp> getAppsWithEndpoint(String tenantDomain) throws AppManagementException {
        List<WebApp> appSortedList = appMDAO.getAllWebApps(tenantDomain);
        Collections.sort(appSortedList, new APINameComparator());
        return appSortedList;
    }

}