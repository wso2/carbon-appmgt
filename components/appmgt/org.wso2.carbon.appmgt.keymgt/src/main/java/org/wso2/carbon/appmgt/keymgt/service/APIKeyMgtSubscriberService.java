/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.appmgt.keymgt.service;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.axis2.AxisFault;
import org.apache.axis2.util.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONObject;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.appmgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * This service class exposes the functionality required by the application developers who will be
 * consuming the APIs published in the WebApp Store.
 */
public class APIKeyMgtSubscriberService extends AbstractAdmin {
	
	 private static final Log log = LogFactory.getLog(APIKeyMgtSubscriberService.class);
	 private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
	 private static final String OAUTH_RESPONSE_ACCESSTOKEN = "access_token";
	 private static final String OAUTH_RESPONSE_EXPIRY_TIME = "expires_in";

    /**
     * Get the list of subscribed APIs of a user
     * @param userId User/Developer name
     * @return An array of APIInfoDTO instances, each instance containing information of provider name,
     * api name and version.
     * @throws APIKeyMgtException Error when getting the list of APIs from the persistence store.
     */
    public APIInfoDTO[] getSubscribedAPIsOfUser(String userId) throws APIKeyMgtException,
                                                                      AppManagementException, IdentityException {
        AppMDAO appMDAO = new AppMDAO();
        return appMDAO.getSubscribedAPIsOfUser(userId);
    }
    
	/**
	 * Renew the ApplicationAccesstoken, Call Token endpoint and get parameters.
	 * Revoke old token.
	 * 
	 * @param tokenType
	 * @param oldAccessToken
	 * @param allowedDomains
	 * @param clientId
	 * @param clientSecret
	 * @param validityTime
	 * @return
	 * @throws Exception
	 */

	public String renewAccessToken(String tokenType, String oldAccessToken,
	                               String[] allowedDomains, String clientId, String clientSecret,
	                               String validityTime) throws Exception {
		String newAccessToken = null;
		long validityPeriod = 0;
		// create a post request to getNewAccessToken for client_credentials
		// grant type.
		
		//String tokenEndpoint = OAuthServerConfiguration.getInstance().getTokenEndPoint();
        String tokenEndpointName =  ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(AppMConstants.API_KEY_MANAGER_TOKEN_ENDPOINT_NAME);
        String keyMgtServerURL = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(AppMConstants.API_KEY_MANAGER_URL);
        URL keymgtURL = new URL(keyMgtServerURL);
        int keyMgtPort = keymgtURL.getPort();
        String tokenEndpoint = null;

        if (keyMgtServerURL != null) {
            String[] tmp = keyMgtServerURL.split("services");
            tokenEndpoint = tmp [0] + tokenEndpointName;
        }
        
		String revokeEndpoint = tokenEndpoint.replace("token", "revoke");

        // Below code is to overcome host name verification failure we get in certificate
        // validation due to self-signed certificate.
        X509HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        DefaultHttpClient client = new DefaultHttpClient();
        SchemeRegistry registry = new SchemeRegistry();
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        socketFactory.setHostnameVerifier(hostnameVerifier);
        if (keyMgtPort >= 0) {
            registry.register(new Scheme("https", keyMgtPort, socketFactory));
        } else {
            registry.register(new Scheme("https", 443, socketFactory));
        }
        SingleClientConnManager mgr1 = new SingleClientConnManager(registry);
        SingleClientConnManager mgr2 = new SingleClientConnManager(registry);
        
        HttpClient tokenEPClient = new DefaultHttpClient(mgr1, client.getParams());
        HttpClient revokeEPClient = new DefaultHttpClient(mgr2, client.getParams());
		HttpPost httpTokpost = new HttpPost(tokenEndpoint);
		HttpPost httpRevokepost = new HttpPost(revokeEndpoint);
		
		// Request parameters.
		List<NameValuePair> tokParams = new ArrayList<NameValuePair>(3);
		List<NameValuePair> revokeParams = new ArrayList<NameValuePair>(3);
		
		tokParams.add(new BasicNameValuePair(OAuth.OAUTH_GRANT_TYPE, GRANT_TYPE_CLIENT_CREDENTIALS));
		tokParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_ID, clientId));
		tokParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_SECRET, clientSecret));
		
		revokeParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_ID, clientId));
		revokeParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_SECRET, clientSecret));
		revokeParams.add(new BasicNameValuePair("token", oldAccessToken));
		
		try {
            //Revoke the Old Access Token
            httpRevokepost.setEntity(new UrlEncodedFormEntity(revokeParams, "UTF-8"));
            HttpResponse revokeResponse = tokenEPClient.execute(httpRevokepost);

            if (revokeResponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " +
                        revokeResponse.getStatusLine().getStatusCode());
            } else{
                if(log.isDebugEnabled()){
                    log.debug("Successfully revoked old application access token");
                }
            }

            //Generate New Access Token
            httpTokpost.setEntity(new UrlEncodedFormEntity(tokParams, "UTF-8"));
            HttpResponse tokResponse = revokeEPClient.execute(httpTokpost);
            HttpEntity tokEntity = tokResponse.getEntity();

			if (tokResponse.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " +
						tokResponse.getStatusLine().getStatusCode());
			} else {
				String responseStr = EntityUtils.toString(tokEntity);
				JSONObject obj = new JSONObject(responseStr);
				newAccessToken = obj.get(OAUTH_RESPONSE_ACCESSTOKEN).toString();
				validityPeriod = Long.parseLong(obj.get(OAUTH_RESPONSE_EXPIRY_TIME).toString());

				if (validityTime != null && !"".equals(validityTime)) {
					validityPeriod = Long.parseLong(validityTime);
				}
			}
		} catch (Exception e2) {
			String errMsg = "Error in getting new accessToken";
			log.error(errMsg);
			throw new APIKeyMgtException(errMsg, e2);

		}
		AppMDAO appMDAO = new AppMDAO();
		appMDAO.updateRefreshedApplicationAccessToken(tokenType, newAccessToken,
		    		                                    validityPeriod);
		return newAccessToken;

	}

    public void unsubscribeFromAPI(String userId, APIInfoDTO apiInfoDTO) {

    }

    /**
     * Revoke Access tokens by Access token string.This will change access token status to revoked and
     * remove cached access tokens from memory
     *
     * @param key Access Token String to be revoked
     * @throws org.wso2.carbon.appmgt.api.AppManagementException on error in revoking
     * @throws AxisFault              on error in clearing cached key
     */
    public void revokeAccessToken(String key,String consumerKey,String authorizedUser) throws
                                                                                       AppManagementException, AxisFault {
        AppMDAO dao=new AppMDAO();
        dao.revokeAccessToken(key);
        clearOAuthCache(consumerKey,authorizedUser);
    }

    public void clearOAuthCache(String consumerKey, String authorizedUser) {
        OAuthCache oauthCache;
        CacheKey cacheKey = new OAuthCacheKey(consumerKey + ":" + authorizedUser);
        if (OAuthServerConfiguration.getInstance().isCacheEnabled()) {
            oauthCache = OAuthCache.getInstance();
            oauthCache.clearCacheEntry(cacheKey);
        }
    }
}
