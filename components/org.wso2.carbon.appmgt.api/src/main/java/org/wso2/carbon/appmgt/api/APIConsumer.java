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

import org.wso2.carbon.appmgt.api.model.*;

import java.util.Map;
import java.util.Set;

/**
 * APIConsumer responsible for providing helper functionality
 */
public interface APIConsumer extends APIManager {

    /**
     * @param subscriberId id of the Subscriber
     * @return Subscriber
     * @throws AppManagementException if failed to get Subscriber
     */
    public Subscriber getSubscriber(String subscriberId) throws AppManagementException;

    /**
     * Returns a list of #{@link org.wso2.carbon.apimgt.api.model.API} bearing the selected tag
     *
     * @param tag name of the tag
     * @return set of WebApp having the given tag name
     * @throws AppManagementException if failed to get set of WebApp
     */
    public Set<WebApp> getAPIsWithTag(String tag) throws AppManagementException;

    /**
     * Returns a paginated list of #{@link org.wso2.carbon.apimgt.api.model.API} bearing the selected tag
     *
     * @param tag name of the tag
     * @param start starting number
     * @param end ending number
     * @return set of WebApp having the given tag name
     * @throws AppManagementException if failed to get set of WebApp
     */
    public Map<String,Object> getPaginatedAPIsWithTag(String tag,int start,int end) throws
                                                                                    AppManagementException;

    /**
     * Returns a list of all published APIs. If a given WebApp has multiple APIs,
     * only the latest version will be included
     * in this list.
     *
     * @return set of WebApp
     * @throws AppManagementException if failed to WebApp set
     */
    public Set<WebApp> getAllPublishedAPIs(String tenantDomain) throws AppManagementException;
 /**
     * Returns a paginated list of all published APIs. If a given WebApp has multiple APIs,
     * only the latest version will be included
     * in this list.
     * @param tenantDomain tenant domain
     * @param start starting number
     * @param end ending number
     * @return set of WebApp
     * @throws AppManagementException if failed to WebApp set
     */
    public Map<String,Object> getAllPaginatedPublishedAPIs(String tenantDomain,int start,int end) throws
                                                                                                  AppManagementException;

    /**
     * Returns top rated APIs
     *
     * @param limit if -1, no limit. Return everything else, limit the return list to specified value.
     * @return Set of WebApp
     * @throws AppManagementException if failed to get top rated APIs
     */
    public Set<WebApp> getTopRatedAPIs(int limit) throws AppManagementException;

    /**
     * Get recently added APIs to the store
     *
     * @param limit if -1, no limit. Return everything else, limit the return list to specified value.
     * @return set of WebApp
     * @throws AppManagementException if failed to get recently added APIs
     */
    public Set<WebApp> getRecentlyAddedAPIs(int limit,String tenantDomain) throws
                                                                           AppManagementException;

    /**
     * Get all tags of published Apps
     *
     * @return a list of all tags applied to all apps published.
     * @throws AppManagementException if failed to get all the tags
     */
    public Set<Tag> getAllTags(String tenantDomain) throws AppManagementException;


    /**
     * Get all tags of published Apps
     *
     * @return a list of all tags applied to all apps published.
     * @throws AppManagementException if failed to get all the tags
     */
    public Set<Tag> getAllTags(String tenantDomain, String assetType, String assetFlag) throws AppManagementException;

    /**
     * Rate a particular WebApp. This will be called when subscribers rate an WebApp
     *
     * @param apiId  The WebApp identifier
     * @param rating The rating provided by the subscriber
     * @param user Username of the subscriber providing the rating
     * @throws AppManagementException If an error occurs while rating the WebApp
     */
    public void rateAPI(APIIdentifier apiId, APIRating rating, String user) throws
                                                                            AppManagementException;
    /**
     * Remove an user rating of a particular WebApp. This will be called when subscribers remove their rating on an WebApp
     *
     * @param apiId  The WebApp identifier
     * @param user Username of the subscriber providing the rating
     * @throws AppManagementException If an error occurs while rating the WebApp
     */
    public void removeAPIRating(APIIdentifier apiId,  String user) throws AppManagementException;

    /**
     * Returns a set of SubscribedAPI purchased by the given Subscriber
     *
     * @param subscriber Subscriber
     * @return Set<WebApp>
     * @throws AppManagementException if failed to get WebApp for subscriber
     */
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber) throws
                                                                       AppManagementException;

    /**
     * Returns a set of SubscribedAPIs filtered by the given application name.
     *
     * @param subscriber Subscriber
     * @return Set<WebApp>
     * @throws AppManagementException if failed to get WebApp for subscriber
     */
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber, String applicationName) throws
                                                                                               AppManagementException;

    /**
     * Returns true if a given user has subscribed to the WebApp
     *
     * @param apiIdentifier APIIdentifier
     * @param userId        user id
     * @return true, if giving api identifier is already subscribed
     * @throws AppManagementException if failed to check the subscribed state
     */
    public boolean isSubscribed(APIIdentifier apiIdentifier, String userId) throws
                                                                            AppManagementException;

    /**
     * Add new Subscriber
     *
     * @param identifier    APIIdentifier
     * @param subscriptionType Type of the subscription. E.g. Individual, Enterprise
     * @param userId        id of the user
     * @param applicationId Application Id   @return String subscription status
     * @throws AppManagementException if failed to add subscription details to database
     */
    public String addSubscription(APIIdentifier identifier, String subscriptionType, String userId, int applicationId, String trustedIdp)
            throws AppManagementException;


    /**
     * Get the subscription for given search criteria.
     * @param apiIdentifier APIIdentifier
     * @param applicationId Application Id
     * @return Subscription if there is one, null otherwise.
     * @throws AppManagementException If an error occurred while getting the subscription.
     */
    public Subscription getSubscription(APIIdentifier apiIdentifier, int applicationId, String subscriptionType) throws
                                                                                                                 AppManagementException;

    /**
     * Unsubscribe the specified user from the specified WebApp in the given application
     *
     * @param identifier    APIIdentifier
     * @param userId        id of the user
     * @param applicationId Application Id
     * @throws AppManagementException if failed to add subscription details to database
     */
    public void removeSubscription(APIIdentifier identifier, String userId, int applicationId)
            throws AppManagementException;


    public void removeAPISubscription(APIIdentifier identifier, String userId, String applicationName)
            throws AppManagementException;


    /**
     * Remove a Subscriber
     *
     * @param identifier APIIdentifier
     * @param userId     id of the user
     * @throws AppManagementException if failed to add subscription details to database
     */
    public void removeSubscriber(APIIdentifier identifier, String userId)
            throws AppManagementException;

    /**
     * This method is to update the subscriber.
     *
     * @param identifier    APIIdentifier
     * @param userId        user id
     * @param applicationId Application Id
     * @throws AppManagementException if failed to update subscription
     */
    public void updateSubscriptions(APIIdentifier identifier, String userId, int applicationId)
            throws AppManagementException;

    /**
     * @param identifier Api identifier
     * @param comment comment text
     * @param user Username of the comment author                        
     * @throws AppManagementException if failed to add comment for WebApp
     */
    public void addComment(APIIdentifier identifier, String comment, 
                           String user) throws AppManagementException;

    /**
     * @param identifier Api identifier
     * @return Comments
     * @throws AppManagementException if failed to get comments for identifier
     */
    public Comment[] getComments(APIIdentifier identifier) throws AppManagementException;

    /**
     * Adds an application
     *
     * @param application Application
     * @param userId      User Id
     * @throws AppManagementException if failed to add Application
     */
    public String addApplication(Application application, String userId) throws
                                                                         AppManagementException;

    /**
     * Updates the details of the specified user application.
     *
     * @param application Application object containing updated data
     * @throws AppManagementException If an error occurs while updating the application
     */
    public void updateApplication(Application application) throws AppManagementException;

    public Set<SubscribedAPI> getSubscribedIdentifiers(Subscriber subscriber,
                                                       APIIdentifier identifier) throws
                                                                                 AppManagementException;
    
    public Set<WebApp> searchAPI(String searchTerm, String searchType,String tenantDomain) throws
                                                                                           AppManagementException;
    public Map<String,Object> searchPaginatedAPIs(String searchTerm, String searchType,String tenantDomain,int start,int end) throws
                                                                                                                              AppManagementException;
    public int getUserRating(APIIdentifier apiId, String user) throws AppManagementException;

    /**
     * Get a list of published APIs by the given provider.
     *
     * @param providerId , provider id
     * @param loggedUser logged user
     * @param limit Maximum number of results to return. Pass -1 to get all.
     * @return set of WebApp
     * @throws AppManagementException if failed to get set of WebApp
     */
    public Set<WebApp> getPublishedAPIsByProvider(String providerId,String loggedUser, int limit) throws
                                                                                                  AppManagementException;/**

     /** Get a list of published APIs by the given provider.
     *
     * @param providerId , provider id
     * @param limit Maximum number of results to return. Pass -1 to get all.
     * @return set of WebApp
     * @throws AppManagementException if failed to get set of WebApp
     */
    public Set<WebApp> getPublishedAPIsByProvider(String providerId, int limit) throws
                                                                                AppManagementException;

    /**
     * Check whether an application access token is already persist in database.
     * @param accessToken
     * @return
     * @throws AppManagementException
     */
    public boolean isApplicationTokenExists(String accessToken) throws AppManagementException;

    /**
     * Returns a list of Tiers denied for the current user
     *
     * @return Set<String>
     * @throws AppManagementException if failed to get the tiers
     */
    public Set<String> getDeniedTiers()throws AppManagementException;
    
    /**
     * Check whether given Tier is denied for the user
     * @param tierName
     * @return 
     * @throws AppManagementException if failed to get the tiers
     */
    public boolean isTierDeneid(String tierName)throws AppManagementException;

    /**
     * Returns a list of applications for a given subscriber
     *
     * @param subscriber Subscriber
     * @return Applications
     * @throws AppManagementException if failed to applications for given subscriber
     */
    public Application[] getApplications(Subscriber subscriber) throws AppManagementException;

}
