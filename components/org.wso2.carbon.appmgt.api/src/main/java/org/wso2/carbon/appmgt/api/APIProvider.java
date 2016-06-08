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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * APIProvider responsible for providing helper functionality
 */
public interface APIProvider extends APIManager {


    /**
     * This methode is to delete a given business owner
     *
     * @param businessOwnerId ID of the owner.
     * @throws AppManagementException
     */
    public boolean deleteBusinessOwner(String businessOwnerId) throws AppManagementException;

    /**
     * update a Business Owner.
     * @return Integer
     * @throws AppManagementException
     */
    public boolean updateBusinessOwner(BusinessOwner businessOwner) throws AppManagementException;


    /**
     * @return
     * @throws AppManagementException
     */
    public List<BusinessOwner> getBusinessOwners() throws AppManagementException;

    /**
     * Return the owner properties of the given owner Id.
     *
     * @param businessOwnerId Business owner Id.
     * @return
     * @throws AppManagementException if failed to get business owner.
     */
    public BusinessOwner getBusinessOwner(int businessOwnerId) throws AppManagementException;

    /**
     * Search the business owners with page limitation.
     * @param startIndex
     * @param pageSize
     * @param searchValue
     * @return
     * @throws AppManagementException
     */
    public List<BusinessOwner> searchBusinessOwners(int startIndex, int pageSize, String searchValue) throws
                                                                                              AppManagementException;

    /**
     * Get the count of business owners.
     * @return
     * @throws AppManagementException
     */
    public  int getBusinessOwnersCount() throws AppManagementException;
    /**
     * Save a Business Owner.
     * @return Integer
     * @throws AppManagementException
     */
    public int saveBusinessOwner(BusinessOwner businessOwner) throws AppManagementException;
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

    public List<WebApp> getAppsWithEndpoint(String tenantDomain) throws AppManagementException;

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
     * @param policyPartialDescription policy description
     * @return policy id
     * @throws AppManagementException
     */
    int saveEntitlementPolicyPartial(String policyPartialName, String policyPartial, boolean isSharedPartial,
                                     String policyAuthor,String policyPartialDescription) throws AppManagementException;

    /**
     * Update the policy partial
     *
     * @param policyPartialId policy partial id
     * @param policyPartial   policy content
     * @param author          author of the partial
     * @param isShared        policy status
     * @param policyPartialDescription policy description
     * @return if update success return true else false
     * @throws AppManagementException
     */
    public boolean updateEntitlementPolicyPartial(int policyPartialId, String policyPartial,
                                                  String author, boolean isShared, String policyPartialDescription) throws
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
          * Get the apps which use the given policy partial
          *
          * @param policyPartialId policy partial id
          * @return list of apps
          * @throws AppManagementException
          */
    public List<APIIdentifier> getAssociatedApps(int policyPartialId) throws AppManagementException;

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
     * Adds a new Mobile Application to the Store
     *
     * @param mobileApp Mobile application
     * @throws AppManagementException if failed to add MobileApp
     */
    public String createMobileApp(MobileApp mobileApp) throws AppManagementException;

    /**
     * Add new webapp
     * @param webApp
     * @return
     * @throws AppManagementException
     */
    public String createWebApp(WebApp webApp) throws AppManagementException;

    /**
     *
     * Creates a new versions using the attributes (inlcuding the new version number) of the given app.
     *
     * @param app
     * @return The UUID of the newly created version.
     * @throws AppManagementException
     */
    public String createNewVersion(App app)throws AppManagementException;

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
     * Updates an existing Mobile Application. This method must not be used to change Mobile App status. Implementations
     * should throw an exceptions when such attempts are made. All life cycle state changes
     * should be carried out using the changeAPIStatus method of this interface.
     *
     * @param mobileApp Mobile App
     * @throws AppManagementException if failed to update WebApp
     */
    public void updateMobileApp(MobileApp mobileApp) throws AppManagementException;

    /**
     * Change the lifecycle state of the specified WebApp
     *
     * @param api The WebApp whose status to be updated
     * @param status New status of the WebApp
     * @param userId User performing the WebApp state change
     * @param updateGatewayConfig Whether the changes should be pushed to the WebApp gateway or not
     * @throws AppManagementException on error
     */

    /**
     * Returns details of an WebApp
     *
     * @param uuid uuid of the A
     * @return An WebApp object related to the given identifier or null
     * @throws AppManagementException if failed get WebApp from APIIdentifier
     */
    public WebApp getWebApp(String uuid) throws AppManagementException;

    public void changeAPIStatus(WebApp api, APIStatus status, String userId,
                                boolean updateGatewayConfig) throws AppManagementException;

    /**
     * redeploy the synapse when webapp is being edited
     * @param api The WebApp whose status to be updated
     * @throws AppManagementException on error
     */
    public void updateWebAppSynapse(WebApp api) throws AppManagementException;

    /**
     * Copy web applications documentations
     *
     * @param api        The WebApp id of the copying docs
     * @param newVersion The version of the new WebApp
     * @throws AppManagementException If an error occurs while trying to create
     *                                the new version of the WebApp
     */
    public void copyWebappDocumentations(WebApp api, String newVersion) throws AppManagementException;

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
     * Removes a given documentation
     *
     * @param appId   APIIdentifier
     * @param documentId UUID of the doc
     * @throws AppManagementException if failed to remove documentation
     */
    public void removeDocumentation(APIIdentifier appId, String documentId)throws AppManagementException;

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
     * Add a file to a document of source type FILE
     *
     * @param appId App identifier the document belongs to
     * @param documentation document
     * @param filename name of the file
     * @param content content of the file as an Input Stream
     * @param contentType content type of the file
     * @throws AppManagementException if failed to add the file
     */
    void addFileToDocumentation(WebApp appId, Documentation documentation, String filename, InputStream content,
                                String contentType) throws AppManagementException;


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
     * @throws AppManagementException
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
     *
     * Searches and returns the apps for the given search terms.
     *
     * @param appType
     * @param searchTerms
     * @return
     * @throws AppManagementException
     */
    public List<App> searchApps(String appType, Map<String, String> searchTerms) throws AppManagementException;


    /**
     * Update the subscription status
     *
     * @param apiId WebApp Identifier
     * @param subStatus Subscription Status
     * @param appId Application Id              *
     * @return int value with subscription id
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
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
     * Moves subscriptions from one app (@{code fromApp}) to another app ({@code toApp})
     *
     * @param fromIdentifier subscriptions of this app
     * @param toIdentifier   will be moved into this app
     * @return number of subscriptions moved
     * @throws AppManagementException
     */
    public int moveSubscriptions(APIIdentifier fromIdentifier, APIIdentifier toIdentifier)
            throws AppManagementException;

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
     * Get the all web apps of tenant
     *
     * @return web apps
     * @throws AppManagementException
     */
    public List<WebApp> getAllWebApps(String tenantDomain) throws AppManagementException;


    /**
     * Get the all web apps
     *
     * @return web apps
     * @throws AppManagementException
     */
    public List<WebApp> getAllWebApps() throws AppManagementException;

    /**
     * This method returns the subscription count of apps for given period.
     *
     * @param provider         provider of app
     * @param fromDate         From date
     * @param toDate           To date
     * @param isSubscriptionOn if any subscription(self or enterprise) model is on or off
     * @return subscription count of apps
     * @throws AppManagementException
     */

    public Map<String, Long> getSubscriptionCountByAPPs(String provider, String fromDate, String toDate,
                                                        boolean isSubscriptionOn) throws AppManagementException;


    /**
     * Get Application wise policy group list
     *
     * @param appId Application Id
     * @return List of policy groups
     * @throws AppManagementException on error
     */
    public List<EntitlementPolicyGroup> getPolicyGroupListByApplication(int appId) throws
            AppManagementException;

    /**
     * Retrieves TRACKING_CODE sequences from APM_APP Table
     *@param uuid : Application UUID
     *@return TRACKING_CODE
     *@throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public String getTrackingID(String uuid)throws AppManagementException;



    /**
     * Update (add/delete) the the app in external stores.
     * @param webApp Web App
     * @param appStores External App Stores
     * @throws AppManagementException
     */
    public void updateAppsInExternalAppStores(WebApp webApp, Set<AppStore> appStores)
            throws AppManagementException;

    /**
     * Get the external app stores for given identifier.
     * @param identifier WebApp Identifier
     * @return Set of App Store
     * @throws AppManagementException
     */
    public Set<AppStore> getExternalAppStores(APIIdentifier identifier) throws AppManagementException;

    /**
     * Get WebApp default version details.
     *
     * @param appName
     * @param providerName
     * @param appStatus
     * @return Default WebApp Version
     * @throws AppManagementException
     */
    public String getDefaultVersion(String appName, String providerName, AppDefaultVersion appStatus)
            throws AppManagementException;

    /**
     * Check if the given WebApp version is the default version.
     *
     * @param identifier
     * @return true if given app is the default version
     * @throws AppManagementException
     */
    public boolean isDefaultVersion(APIIdentifier identifier) throws AppManagementException;

    /**
     * Check if the given WebApp has any other versions in any lifecycle state.
     *
     * @param identifier
     * @return true if given app has more versions
     * @throws AppManagementException
     */
    public boolean hasMoreVersions(APIIdentifier identifier) throws AppManagementException;

    /**
     * Get WebApp basic details by app uuid.
     *
     * @param uuid
     * @return WebApp details corresponding to the given Id
     * @throws AppManagementException
     */
    public WebApp getAppDetailsFromUUID(String uuid) throws AppManagementException;

    /**
     * Change the lifecycle status of a given application
     * @param appType application type
     * @param appId application type
     * @param action lifecycle action perform on the application
     * @throws AppManagementException
     */
    public void changeLifeCycleStatus(String appType, String appId, String action) throws AppManagementException;

    /**
     * Get allowed lifecycle actions to perform on a given application
     * @param appType application type
     * @param appId application type
     * @return list of allowed lifecycle actions perform on the app
     */
    public String[] getAllowedLifecycleActions(String appType, String appId) throws AppManagementException;

    /**
     * Add mobile application subscription for a given user
     * @param userId userId
     * @param appId application id
     * @return
     * @throws AppManagementException
     */
    public boolean subscribeMobileApp(String userId, String appId) throws AppManagementException;

    /**
     * Remove mobile application subscription for a given user
     * @param userId username
     * @param appId application id
     * @return
     * @throws AppManagementException
     */
    public boolean unSubscribeMobileApp(String userId, String appId) throws AppManagementException;

    /**
     * Add tags to a given application
     * @param appType application type ie: webapp or mobileapp
     * @param appId application uuid
     * @param tags tag list to be added
     * @return
     * @throws AppManagementException
     */
    public void addTags(String appType,String appId, List<String> tags) throws AppManagementException;

    /**
     * Remove tag from a given application
     * @param appType application type ie: webapp or mobileapp
     * @param appId application uuid
     * @param tags tag list to be deleted
     * @throws AppManagementException
     */
    public void removeTag(String appType, String appId, List<String> tags) throws AppManagementException;

    /**
     * Retrieve all tags for a given application type
     * @param appType application type ie: webapp or mobileapp
     * @throws AppManagementException
     */
    public Set<Tag> getAllTags(String appType) throws AppManagementException;

    /**
     * Retrieve all tags for a given application
     * @param appType application type ie: webapp or mobileapp
     * @param appId application uuid
     * @throws AppManagementException
     */
    public Set<Tag> getAllTags(String appType, String appId) throws AppManagementException;

    /**
     * Updates the given app.
     *
     * @param app
     * @throws AppManagementException
     */
    void updateApp(App app)throws AppManagementException;

    /**
     * Remove mobile application binary file from storage
     * @param fileName
     * @throws AppManagementException
     */
    public void removeBinaryFromStorage(String fileName) throws AppManagementException;
}
