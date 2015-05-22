/*
 * Copyright WSO2 Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appmgt.gateway.handlers.throttling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.Caching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.rest.RESTUtils;
import org.wso2.carbon.appmgt.api.model.URITemplate;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.appmgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.appmgt.gateway.handlers.security.keys.APIKeyDataStore;
import org.wso2.carbon.appmgt.gateway.handlers.security.keys.JDBCAPIKeyDataStore;
import org.wso2.carbon.appmgt.gateway.handlers.security.keys.WSAPIKeyDataStore;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.appmgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.appmgt.impl.dto.ResourceInfoDTO;
import org.wso2.carbon.appmgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class APPResourceManager {

	private static final Log log = LogFactory.getLog(APPResourceManager.class);

	private APIKeyDataStore dataStore;

	public APPResourceManager() {
		super();

		String keyValidatorClientType = APISecurityUtils.getKeyValidatorClientType();
		if (AppMConstants.API_KEY_VALIDATOR_WS_CLIENT.equals(keyValidatorClientType)) {
			this.dataStore = new WSAPIKeyDataStore();
		} else if (AppMConstants.API_KEY_VALIDATOR_THRIFT_CLIENT.equals(keyValidatorClientType)) {
			// this.dataStore = new ThriftAPIDataStore();
			try {
				this.dataStore = new JDBCAPIKeyDataStore();
			} catch (APISecurityException e) {
				e.printStackTrace();
			}
		}
		this.getResourceCache();

	}

	protected Cache getResourceCache() {
		return Caching.getCacheManager(AppMConstants.API_MANAGER_CACHE_MANAGER)
		              .getCache(AppMConstants.RESOURCE_CACHE_NAME);
	}

	private APIInfoDTO doGetAPIInfo(String context, String apiVersion) throws APISecurityException {
		APIInfoDTO apiInfoDTO = new APIInfoDTO();

		ArrayList<URITemplate> uriTemplates = getAllURITemplates(context, apiVersion);

		apiInfoDTO.setApiName(context);
		apiInfoDTO.setContext(context);
		apiInfoDTO.setVersion(apiVersion);
		apiInfoDTO.setResources(new ArrayList<ResourceInfoDTO>());

		Map<String, ResourceInfoDTO> resourceMap = new HashMap<String, ResourceInfoDTO>();
		ResourceInfoDTO resourceInfoDTO = null;
		VerbInfoDTO verbInfoDTO = null;
		for (URITemplate uriTemplate : uriTemplates) {
			if (resourceMap.containsKey(uriTemplate.getUriTemplate())) {
				resourceInfoDTO = resourceMap.get(uriTemplate.getUriTemplate());
				verbInfoDTO = new VerbInfoDTO();
				verbInfoDTO.setHttpVerb(uriTemplate.getHTTPVerb());

				if (!resourceInfoDTO.getHttpVerbs().contains(verbInfoDTO)) {
					verbInfoDTO.setAuthType(uriTemplate.getAuthType());
					verbInfoDTO.setThrottling(uriTemplate.getThrottlingTier());
					verbInfoDTO.setSkipThrottling(uriTemplate.isSkipThrottling());
					resourceInfoDTO.getHttpVerbs().add(verbInfoDTO);
				}
			} else {
				resourceInfoDTO = new ResourceInfoDTO();
				resourceInfoDTO.setUrlPattern(uriTemplate.getUriTemplate());
				verbInfoDTO = new VerbInfoDTO();
				verbInfoDTO.setHttpVerb(uriTemplate.getHTTPVerb());
				verbInfoDTO.setAuthType(uriTemplate.getAuthType());
				verbInfoDTO.setThrottling(uriTemplate.getThrottlingTier());
				verbInfoDTO.setSkipThrottling(uriTemplate.isSkipThrottling());
				List<VerbInfoDTO> httpVerbs = new ArrayList<VerbInfoDTO>();
				httpVerbs.add(verbInfoDTO);
				resourceInfoDTO.setHttpVerbs(httpVerbs);
				resourceMap.put(uriTemplate.getUriTemplate(), resourceInfoDTO);

				apiInfoDTO.getResources().add(resourceInfoDTO);
			}
		}
		return apiInfoDTO;
	}

	public String getApplicationThrottlingTier() {
		return null;
	}

	/**
	 * Put the resource mapping which maps to any of the resource at the end of
	 * the resource list
	 * 
	 * @param appInfoDTO
	 */
	private void sortResources(APIInfoDTO appInfoDTO) {
		Iterator<ResourceInfoDTO> i = appInfoDTO.getResources().iterator();
		ResourceInfoDTO removed = null;
		while (i.hasNext()) {
			ResourceInfoDTO resource = i.next();
			if (resource.getUrlPattern().equals(APIThrottleConstants.URL_MAPPING_ALL)) {
				i.remove();
				removed = resource;
				break;
			}
		}
		if (removed != null) {
			appInfoDTO.getResources().add(removed);
		}
	}

	/**
	 * @param context
	 *            WebApp context of WebApp
	 * @param apiVersion
	 *            Version of WebApp
	 * @param requestPath
	 *            Incoming request path
	 * @param httpMethod
	 *            http method of request
	 * @return verbInfoDTO which contains throttling tier for given resource and
	 *         verb+resource key
	 */
	public VerbInfoDTO getVerbInfo(String context, String apiVersion, String requestPath,
	                               String httpMethod) throws APISecurityException {

		String cacheKey = context + APIThrottleConstants.URL_MAPPING_COLON + apiVersion;
		APIInfoDTO apiInfoDTO = null;
		apiInfoDTO = (APIInfoDTO) getResourceCache().get(cacheKey);

		if (apiInfoDTO == null) {
			apiInfoDTO = doGetAPIInfo(context, apiVersion);
			sortResources(apiInfoDTO);
			getResourceCache().put(cacheKey, apiInfoDTO);
		}

		// Match the case where the direct api context is matched
		if (APIThrottleConstants.URL_MAPPING_SEPERATOR.equals(requestPath)) {
			String requestCacheKey =
			                         context + APIThrottleConstants.URL_MAPPING_SEPERATOR +
			                                 apiVersion + requestPath +
			                                 APIThrottleConstants.URL_MAPPING_COLON + httpMethod;

			// Get decision from cache.
			VerbInfoDTO matchingVerb = null;
			matchingVerb = (VerbInfoDTO) getResourceCache().get(requestCacheKey);
			// On a cache hit
			if (matchingVerb != null) {
				matchingVerb.setRequestKey(requestCacheKey);
				return matchingVerb;
			} else {
				for (ResourceInfoDTO resourceInfoDTO : apiInfoDTO.getResources()) {
					String urlPattern = resourceInfoDTO.getUrlPattern();

					// If the request patch is '/', it can only be matched with
					// a resource whose url-context is '/*'
					if (APIThrottleConstants.URL_MAPPING_ALL.equals(urlPattern)) {
						for (VerbInfoDTO verbDTO : resourceInfoDTO.getHttpVerbs()) {
							if (verbDTO.getHttpVerb().equals(httpMethod)) {
								// Store verb in cache
								getResourceCache().put(requestCacheKey, verbDTO);
								verbDTO.setRequestKey(requestCacheKey);
								return verbDTO;
							}
						}
					}
				}
			}
		}

		// Remove the ending '/' from request
		requestPath = RESTUtils.trimTrailingSlashes(requestPath);

		while (requestPath.length() > 1) {

			String requestCacheKey =
			                         context + APIThrottleConstants.URL_MAPPING_SEPERATOR +
			                                 apiVersion + requestPath +
			                                 APIThrottleConstants.URL_MAPPING_COLON + httpMethod;

			// Get decision from cache.
			VerbInfoDTO matchingVerb = null;
			matchingVerb = (VerbInfoDTO) getResourceCache().get(requestCacheKey);

			// On a cache hit
			if (matchingVerb != null) {
				matchingVerb.setRequestKey(requestCacheKey);
				return matchingVerb;
			}
			// On a cache miss
			else {
				for (ResourceInfoDTO resourceInfoDTO : apiInfoDTO.getResources()) {
					String urlPattern = resourceInfoDTO.getUrlPattern();
					if (urlPattern.endsWith(APIThrottleConstants.URL_MAPPING_ALL)) {
						// Remove the ending '/*'
						urlPattern = urlPattern.substring(0, urlPattern.length() - 2);
					}
					// If the urlPattern ends with a '/', remove that as well.
					urlPattern = RESTUtils.trimTrailingSlashes(urlPattern);

					if (requestPath.endsWith(urlPattern)) {

						for (VerbInfoDTO verbDTO : resourceInfoDTO.getHttpVerbs()) {
							if (verbDTO.getHttpVerb().equals(httpMethod)) {
								// Store verb in cache
								getResourceCache().put(requestCacheKey, verbDTO);
								verbDTO.setRequestKey(requestCacheKey);
								return verbDTO;
							}
						}
					}
				}
			}

			// Remove the section after the last occurrence of the '/' character
			int index = requestPath.lastIndexOf(APIThrottleConstants.URL_MAPPING_SEPERATOR);
			requestPath = requestPath.substring(0, index <= 0 ? 0 : index);
		}
		// nothing found. return the highest level of security
		return null;
	}

	private ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion)
	                                                                                    throws APISecurityException {
		return dataStore.getAllURITemplates(context, apiVersion);
	}

	public AuthenticationContext getAuthenticationContext(String appContext, String appVersion,
	                                                      String user, String accessToken)
	                                                                                      throws APISecurityException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (!tenantDomain.equalsIgnoreCase("carbon.super")) {
            user = user + '@' + tenantDomain;
        }

        APIKeyValidationInfoDTO info = dataStore.getAPPData(appContext, appVersion, user, null);
		if (info == null) {
			log.warn("cannot load application data for the provided context and version");
			return null;
		}
		AuthenticationContext authContext = new AuthenticationContext();
		authContext.setAccessToken(accessToken);

		// Application concept needs to be removed from the app-manager.
		// Therefore only thing that can be identified uniquely is the
		// subscription id.
		authContext.setApplicationId(String.valueOf(info.getSubscriptionId()));
		authContext.setApplicationName(info.getApplicationName());
		authContext.setApplicationTier(info.getApplicationTier());
		authContext.setTier(info.getTier());
		authContext.setConsumerKey(accessToken);
		authContext.setAuthenticated(true);

		getResourceCache().put(authContext.getAccessToken(), authContext);

		return authContext;
	}
}
