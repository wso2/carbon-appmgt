/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appmgt.api;

import org.wso2.carbon.appmgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.appmgt.api.model.*;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicy;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicyPartial;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicyValidationResult;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * APIProvider responsible for providing helper functionality
 */
public interface APIProvider extends APIManager {

    /**
     * Returns a list of all #{@link org.wso2.carbon.apimgt.api.model.Provider} available on the system.
     *
     * @return Set<Provider>
     * @throws AppManagementException if failed to get Providers
     */
    public Set<Provider> getAllProviders() throws AppManagementException;

    /**
     * Get a list of APIs published by the given provider. If a given WebApp has multiple APIs,
     * only the latest version will
     * be included in this list.
     *
     * @param providerId , provider id
     * @return set of WebApp
     * @throws AppManagementException if failed to get set of WebApp
     */
    public List<WebApp> getAPIsByProvider(String providerId) throws AppManagementException;

    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<Subscriber>
     * @throws AppManagementException if failed to get subscribed APIs of given provider
     */
    public Set<Subscriber> getSubscribersOfProvider(String providerId)
            throws AppManagementException;

    /**
     * get details of provider
     *
     * @param providerName name of the provider
     * @return Provider
     * @throws AppManagementException if failed to get Provider
     */
    public Provider getProvider(String providerName) throws AppManagementException;

    /**
     * Return Usage of given APIIdentifier
     *
     * @param apiIdentifier APIIdentifier
     * @return Usage
     */
    public Usage getUsageByAPI(APIIdentifier apiIdentifier);

    /**
     * Return Usage of given provider and WebApp
     *
     * @param providerId if of the provider
     * @param apiName    name of the WebApp
     * @return Usage
     */
    public Usage getAPIUsageByUsers(String providerId, String apiName);

    /**
     * Returns usage details of all APIs published by a provider
     *
     * @param providerId Provider Id
     * @return UserApplicationAPIUsages for given provider
     * @throws AppManagementException If failed to get UserApplicationAPIUsage
     */
    public UserApplicationAPIUsage[] getAllAPIUsageByProvider(String providerId)
            throws AppManagementException;

    /**
     * Shows how a given consumer uses the given WebApp.
     *
     * @param apiIdentifier APIIdentifier
     * @param consumerEmail E-mal Address of consumer
     * @return Usage
     */
    public Usage getAPIUsageBySubscriber(APIIdentifier apiIdentifier, String consumerEmail);

    /**
     * Returns full list of Subscribers of an WebApp
     *
     * @param identifier APIIdentifier
     * @return Set<Subscriber>
     * @throws AppManagementException if failed to get Subscribers
     */
    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier)
            throws AppManagementException;

    /**
     * this method returns the Set<APISubscriptionCount> for given provider and api
     *
     * @param identifier APIIdentifier
     * @return Set<APISubscriptionCount>
     * @throws AppManagementException if failed to get APISubscriptionCountByAPI
     */
    public long getAPISubscriptionCountByAPI(APIIdentifier identifier)
            throws AppManagementException;

    /**
     * This method returns the subscribed apps by users
     * @param fromDate From date
     * @param toDate To date
     * @return list of subscribed apps by users.
     * @throws AppManagementException
     */
    public Map<String, List> getSubscribedAPPsByUsers(String fromDate, String toDate)
            throws AppManagementException;

    public void addTier(Tier tier) throws AppManagementException;

    public void updateTier(Tier tier) throws AppManagementException;

    public void removeTier(Tier tier) throws AppManagementException;

    /**
     * Generates entitlement policies for the given app.
     *
     * @param apiIdentifier ID of the app.
     * @throws AppManagementException when entitlement service implementation is unable to generate policies.
     */
    public void generateEntitlementPolicies(APIIdentifier apiIdentifier) throws
                                                                         AppManagementException;

    /**
     * Updates given entitlement policies.
     * @param policies Entitlement policies to be updated.
     * @throws AppManagementException when entitlement service implementation is unable to update policies.
     */
    void updateEntitlementPolicies(List<EntitlementPolicy> policies) throws AppManagementException;

    /**
     * Get entitlement policy content from policyId
     * @param policyId Entitlement policy id
     * @return Entitlement policy content
     */
    String getEntitlementPolicy(String policyId);

    /**
     * Get web application id
     * @param uuid web application uuid
     * @return web application id
     */
    public int getWebAppId(String uuid) throws AppManagementException;

    /**
     * Save the entitlement policy partial
     *
     * @param policyPartialName Name of the policy partial
     * @param policyPartial     policy content
     * @param isSharedPartial   policy status
     * @param policyAuthor      author of the policy
     * @param policyPartialDesc policy description
     * @return policy id
     * @throws AppManagementException
     */
    int saveEntitlementPolicyPartial(String policyPartialName, String policyPartial, boolean isSharedPartial,
                                     String policyAuthor,String policyPartialDesc) throws AppManagementException;

    /**
     * Update the policy partial
     *
     * @param policyPartialId policy partial id
     * @param policyPartial   policy content
     * @param author          author of the partial
     * @param isShared        policy status
     * @param policyPartialDesc policy description
     * @return if update success return true else false
     * @throws AppManagementException
     */
    public boolean updateEntitlementPolicyPartial(int policyPartialId, String policyPartial,
                                                  String author, boolean isShared, String policyPartialDesc) throws
                                                                                   AppManagementException;

    /**
     *
     * Get policyPartial content
     * @param policyPartialId
     * @return entitlement policy
     * @throws AppManagementException
     */
    public EntitlementPolicyPartial getPolicyPartial(int policyPartialId) throws
                                                                          AppManagementException;

    /**
          * Get the name of apps which use the given policy partial
          *
          * @param policyPartialId policy partial id
          * @return list of apps name
          * @throws AppManagementException
          */
    public List<String> getAssociatedAppNames(int policyPartialId) throws AppManagementException;

    /**
     * Delete entitlement policy partial
     *
     * @param policyPartialId
     * @param author          author of the partial
     * @return true if success else false
     * @throws AppManagementException
     */
    public boolean deleteEntitlementPolicyPartial(int policyPartialId, String author) throws
                                                                                     AppManagementException;

    /**
     * Get the list of policy partials of an given application
     * @param applicationId
     * @return list of application policy partials
     * @throws AppManagementException
     */
    public List<EntitlementPolicyPartial> getApplicationPolicyPartialList(int applicationId) throws
                                                                                             AppManagementException;

    /**
     * Get the list of shared policy partials
     *
     * @return list of shared policy partials
     * @throws AppManagementException
     */
    public List<EntitlementPolicyPartial> getSharedPolicyPartialsList() throws
                                                                        AppManagementException;

    /**
     * Validates the given entitlement policy partial.
     * @param policyPartial
     * @return Result of the validation.
     * @throws AppManagementException
     */
    EntitlementPolicyValidationResult validateEntitlementPolicyPartial(String policyPartial)throws
                                                                                            AppManagementException;

    /**
     * Adds a new WebApp to the Store
     *
     * @param api WebApp
     * @throws AppManagementException if failed to add WebApp
     */
    public void addWebApp(WebApp api) throws AppManagementException;

    /**
     * Updates an existing WebApp. This method must not be used to change WebApp status. Implementations
     * should throw an exceptions when such attempts are made. All life cycle state changes
     * should be carried out using the changeAPIStatus method of this interface.
     *
     * @param api WebApp
     * @throws AppManagementException if failed to update WebApp
     */
    public void updateAPI(WebApp api) throws AppManagementException;

    /**
     * Change the lifecycle state of the specified WebApp
     *
     * @param api The WebApp whose status to be updated
     * @param status New status of the WebApp
     * @param userId User performing the WebApp state change
     * @param updateGatewayConfig Whether the changes should be pushed to the WebApp gateway or not
     * @throws AppManagementException on error
     */
    public void changeAPIStatus(WebApp api, APIStatus status, String userId,
                                boolean updateGatewayConfig) throws AppManagementException;

    /**
     * redeploy the synapse when webapp is being edited
     * @param api The WebApp whose status to be updated
     * @throws AppManagementException on error
     */
    public void updateWebAppSynapse(WebApp api) throws AppManagementException;

    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param api        The WebApp to be copied
     * @param newVersion The version of the new WebApp
     * @throws DuplicateAPIException  If the WebApp trying to be created already exists
     * @throws AppManagementException If an error occurs while trying to create
     *                                the new version of the WebApp
     */
    public void createNewAPIVersion(WebApp api, String newVersion) throws DuplicateAPIException,
                                                                          AppManagementException;

    /**
     * Removes a given documentation
     *
     * @param apiId   APIIdentifier
     * @param docType the type of the documentation
     * @param docName name of the document
     * @throws AppManagementException if failed to remove documentation
     */
    public void removeDocumentation(APIIdentifier apiId,
                                    String docType, String docName) throws AppManagementException;

    /**
     * Adds Documentation to an WebApp
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws AppManagementException if failed to add documentation
     */
    public void addDocumentation(APIIdentifier apiId,
                                 Documentation documentation) throws AppManagementException;

    /**
     * This method used to save the documentation content
     *
     * @param identifier,        WebApp identifier
     * @param documentationName, name of the inline documentation
     * @param text,              content of the inline documentation
     * @throws AppManagementException if failed to add the document as a resource to registry
     */
    public void addDocumentationContent(APIIdentifier identifier, String documentationName, String text)
            throws AppManagementException;

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
    					throws AppManagementException;

    /**
     * Updates a given documentation
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws AppManagementException if failed to update docs
     */
    public void updateDocumentation(APIIdentifier apiId,
                                    Documentation documentation) throws AppManagementException;

    /**
     * Copies current Documentation into another version of the same WebApp.
     *
     * @param toVersion Version to which Documentation should be copied.
     * @param apiId     id of the APIIdentifier
     * @throws AppManagementException if failed to copy docs
     */
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion)
            throws AppManagementException;

    /**
     * Returns the details of all the life-cycle changes done per WebApp.
     *
     * @param apiId     id of the APIIdentifier
     * @return List of life-cycle events per given WebApp
     * @throws AppManagementException if failed to copy docs
     */
    public List<LifeCycleEvent> getLifeCycleEvents(APIIdentifier apiId)
            throws AppManagementException;

    /**
     * Search WebApp
     *
     * @param searchTerm  Search Term
     * @param searchType  Search Type
     * @return   Set of APIs
     * @throws AppManagementException
     */
    public List<WebApp> searchAPIs(String searchTerm, String searchType, String providerId) throws
                                                                                            AppManagementException;
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
    public void updateSubscription(APIIdentifier apiId, String subStatus, int appId) throws
                                                                                   AppManagementException;

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
                                                                                            AppManagementException;

    /**
     * Get the list of Tier Permissions
     *
     * @return Tier Permission Set
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */
    public Set getTierPermissions() throws AppManagementException;

    /**
     * Delete an WebApp
     *
     * @param identifier APIIdentifier
     * @param ssoProvider SSOProvider
     * @throws AppManagementException if failed to remove the WebApp
     */
    public boolean deleteApp(APIIdentifier identifier, SSOProvider ssoProvider) throws
                                                                                AppManagementException;

    /**
     * Get the list of Custom InSequences.
     * @return List of available sequences
     * @throws AppManagementException
     */

    public List<String> getCustomInSequences()  throws AppManagementException;


    /**
     * Get the list of Custom OutSequences.
     * @return List of available sequences
     * @throws AppManagementException
     */

    public List<String> getCustomOutSequences()  throws AppManagementException;

    /**
     * Checks the Gateway Type
     *
     * @return True if gateway is Synpase
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *
     */
    public boolean isSynapseGateway() throws AppManagementException;

    /**
     * Get the all web apps
     *
     * @return web apps
     * @throws AppManagementException
     */
    public List<WebApp> getAllWebApps() throws AppManagementException;

    /**
     * This method returns the subscription count of apps for given period.
     * @param provider provider of app
     * @param fromDate From date
     * @param toDate To date
     * @return subscription count of apps
     * @throws AppManagementException
     */
    public Map<String, Long> getSubscriptionCountByAPPs(String provider, String fromDate, String toDate)
            throws AppManagementException;


    /**
     * Get Application wise policy group list
     *
     * @param appId : Application Id
     * @return list of policy groups
     * @throws AppManagementException on error
     */
    public List<EntitlementPolicyGroup> getPolicyGroupListByApplication(Integer appId) throws
            AppManagementException;



}
