/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.Gson;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.URL;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.doc.model.APIDefinition;
import org.wso2.carbon.appmgt.api.doc.model.APIResource;
import org.wso2.carbon.appmgt.api.doc.model.Operation;
import org.wso2.carbon.appmgt.api.doc.model.Parameter;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.APIStatus;
import org.wso2.carbon.appmgt.api.model.AppDefaultVersion;
import org.wso2.carbon.appmgt.api.model.AppStore;
import org.wso2.carbon.appmgt.api.model.Documentation;
import org.wso2.carbon.appmgt.api.model.DocumentationType;
import org.wso2.carbon.appmgt.api.model.ExternalAppStorePublisher;
import org.wso2.carbon.appmgt.api.model.MobileApp;
import org.wso2.carbon.appmgt.api.model.Provider;
import org.wso2.carbon.appmgt.api.model.SSOProvider;
import org.wso2.carbon.appmgt.api.model.Tier;
import org.wso2.carbon.appmgt.api.model.URITemplate;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.dto.Environment;
import org.wso2.carbon.appmgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.appmgt.impl.idp.sso.model.SSOEnvironment;
import org.wso2.carbon.appmgt.impl.internal.AppManagerComponent;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.commons.stub.loggeduserinfo.ExceptionException;
import org.wso2.carbon.core.commons.stub.loggeduserinfo.LoggedUserInfo;
import org.wso2.carbon.core.commons.stub.loggeduserinfo.LoggedUserInfoAdminStub;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.FileUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * This class contains the utility methods used by the implementations of
 * APIManager, APIProvider
 * and APIConsumer interfaces.
 */
public final class AppManagerUtil {

	private static final Log log = LogFactory.getLog(AppManagerUtil.class);

	private static boolean isContextCacheInitialized = false;

	private static Set<Integer> registryInitializedTenants = new HashSet<Integer>();

	/**
	 * This method used to get WebApp from governance artifact
	 * 
	 * @param artifact
	 *            WebApp artifact
	 * @param registry
	 *            Registry
	 * @return WebApp
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             if failed to get WebApp from artifact
	 */
	public static WebApp getAPI(GovernanceArtifact artifact, Registry registry)
	                                                                           throws
                                                                               AppManagementException {

		WebApp api;
		try {
			String providerName = artifact.getAttribute(AppMConstants.API_OVERVIEW_PROVIDER);
			String apiName = artifact.getAttribute(AppMConstants.API_OVERVIEW_NAME);
			String apiVersion = artifact.getAttribute(AppMConstants.API_OVERVIEW_VERSION);
			APIIdentifier apiId = new APIIdentifier(AppManagerUtil.replaceEmailDomainBack(providerName), apiName, apiVersion);
			api = new WebApp(apiId);
			// set rating
			String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
			// BigDecimal bigDecimal = new BigDecimal(getAverageRating(apiId));
			// BigDecimal res = bigDecimal.setScale(1, RoundingMode.HALF_UP);

			// TODO revert this once proper db saving is done
			api.setRating(1f);
            //set name
            api.setApiName(apiName);
			// set description

			api.setDescription(artifact.getAttribute(AppMConstants.API_OVERVIEW_DESCRIPTION));
			// set last access time
			api.setLastUpdated(registry.get(artifactPath).getLastModified());
			// set url
			api.setUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_URL));
            api.setLogoutURL(artifact.getAttribute(AppMConstants.API_OVERVIEW_LOGOUT_URL));
			api.setDisplayName(artifact.getAttribute(AppMConstants.API_OVERVIEW_DISPLAY_NAME));
			api.setSandboxUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_SANDBOX_URL));
			api.setStatus(getApiStatus(artifact.getLifecycleState().toUpperCase()));
            api.setThumbnailUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_THUMBNAIL_URL));
			api.setWsdlUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_WSDL));
			api.setWadlUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_WADL));
			api.setTechnicalOwner(artifact.getAttribute(AppMConstants.API_OVERVIEW_TEC_OWNER));
			api.setTechnicalOwnerEmail(artifact.getAttribute(AppMConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
			api.setBusinessOwner(artifact.getAttribute(AppMConstants.API_OVERVIEW_BUSS_OWNER));
			api.setBusinessOwnerEmail(artifact.getAttribute(AppMConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));
			api.setVisibility(artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBILITY));
			api.setVisibleRoles(artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBLE_ROLES));
			api.setVisibleTenants(artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBLE_TENANTS));
			api.setEndpointSecured(Boolean.parseBoolean(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_SECURED)));
			api.setEndpointUTUsername(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_USERNAME));
			api.setEndpointUTPassword(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_PASSWORD));
			api.setTransports(artifact.getAttribute(AppMConstants.API_OVERVIEW_TRANSPORTS));
			api.setInSequence(artifact.getAttribute(AppMConstants.API_OVERVIEW_INSEQUENCE));
			api.setOutSequence(artifact.getAttribute(AppMConstants.API_OVERVIEW_OUTSEQUENCE));
			api.setResponseCache(artifact.getAttribute(AppMConstants.API_OVERVIEW_RESPONSE_CACHING));
            api.setSsoEnabled(artifact.getAttribute("sso_singleSignOn"));
            api.setUUID(artifact.getId());
            api.setThumbnailUrl(artifact.getAttribute(AppMConstants.APP_IMAGES_THUMBNAIL));
            api.setSkipGateway(Boolean.parseBoolean(artifact.getAttribute(AppMConstants.API_OVERVIEW_SKIP_GATEWAY)));
            api.setTreatAsASite(artifact.getAttribute(AppMConstants.APP_OVERVIEW_TREAT_AS_A_SITE));
            api.setAllowAnonymous(Boolean.parseBoolean(artifact.getAttribute(AppMConstants.API_OVERVIEW_ALLOW_ANONYMOUS)));
            
            int cacheTimeout = AppMConstants.API_RESPONSE_CACHE_TIMEOUT;
			try {
				cacheTimeout =
				               Integer.parseInt(artifact.getAttribute(AppMConstants.API_OVERVIEW_CACHE_TIMEOUT));
			} catch (NumberFormatException e) {
				// ignore
			}

			api.setCacheTimeout(cacheTimeout);

			api.setEndpointConfig(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_CONFIG));

			api.setRedirectURL(artifact.getAttribute(AppMConstants.API_OVERVIEW_REDIRECT_URL));
            api.setAppOwner(artifact.getAttribute(AppMConstants.API_OVERVIEW_OWNER));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(AppMConstants.API_OVERVIEW_ADVERTISE_ONLY)));
            api.setAppTenant(artifact.getAttribute(AppMConstants.API_OVERVIEW_TENANT));
            api.setDisplayName(artifact.getAttribute(AppMConstants.API_OVERVIEW_DISPLAY_NAME));
			api.setSubscriptionAvailability(artifact.getAttribute(AppMConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
			api.setSubscriptionAvailableTenants(artifact.getAttribute(AppMConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

			String tenantDomainName =
			                          MultitenantUtils.getTenantDomain(replaceEmailDomainBack(providerName));
			int tenantId =
			               ServiceReferenceHolder.getInstance().getRealmService()
			                                     .getTenantManager().getTenantId(tenantDomainName);

			Set<Tier> availableTier = new HashSet<Tier>();
			String tiers = artifact.getAttribute(AppMConstants.API_OVERVIEW_TIER);

			Map<String, Tier> definedTiers = getTiers(tenantId);
			if (tiers != null && !"".equals(tiers)) {
				String[] tierNames = tiers.split(",");
				for (String tierName : tierNames) {
					Tier definedTier = definedTiers.get(tierName);
					if (definedTier != null) {
						availableTier.add(definedTier);
					} else {
						log.warn("Unknown tier: " + tierName + " found on WebApp: " + apiName);
					}
				}
			}
			api.addAvailableTiers(availableTier);
            if(tenantId==-1234) {
                api.setContext(artifact.getAttribute(AppMConstants.API_OVERVIEW_CONTEXT));
            }else{
                String context = artifact.getAttribute(AppMConstants.API_OVERVIEW_CONTEXT);
                if(!context.startsWith(RegistryConstants.PATH_SEPARATOR)){
                    context = RegistryConstants.PATH_SEPARATOR + context;
                }
                api.setContext(RegistryConstants.PATH_SEPARATOR + "t" + RegistryConstants.PATH_SEPARATOR + tenantDomainName + context);
            }
			api.setLatest(Boolean.valueOf(artifact.getAttribute(AppMConstants.API_OVERVIEW_IS_LATEST)));

			Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
			List<String> uriTemplateNames = new ArrayList<String>();

			List<URLMapping> urlPatterns = AppMDAO.getURITemplatesPerAPIAsString(apiId);

			for (URLMapping urlMapping : urlPatterns) {
				URITemplate uriTemplate = new URITemplate();
				String uTemplate = urlMapping.getUrlPattern();
				String method = urlMapping.getHttpMethod();
				String authType = urlMapping.getHttpMethod();
				String throttlingTier = urlMapping.getThrottlingTier();
                String userRoles = urlMapping.getUserRoles();

				uriTemplate.setHTTPVerb(method);
				uriTemplate.setAuthType(authType);
				uriTemplate.setThrottlingTier(throttlingTier);
				uriTemplate.setHttpVerbs(method);
				uriTemplate.setAuthTypes(authType);
				uriTemplate.setUriTemplate(uTemplate);
				uriTemplate.setResourceURI(api.getUrl());
				uriTemplate.setResourceSandboxURI(api.getSandboxUrl());
				uriTemplate.setThrottlingTiers(throttlingTier);
                uriTemplate.setUserRoles(userRoles);
				// Checking for duplicate uri template names
				if (uriTemplateNames.contains(uTemplate)) {
					for (URITemplate tmp : uriTemplates) {
						if (uTemplate.equals(tmp.getUriTemplate())) {
							tmp.setHttpVerbs(method);
							tmp.setAuthTypes(authType);
							tmp.setThrottlingTiers(throttlingTier);
							break;
						}
					}

				} else {
					uriTemplates.add(uriTemplate);
				}

				uriTemplateNames.add(uTemplate);

			}
			api.setUriTemplates(uriTemplates);

			Set<String> tags = new HashSet<String>();
			org.wso2.carbon.registry.core.Tag[] tag = registry.getTags(artifactPath);
			for (Tag tag1 : tag) {
				tags.add(tag1.getTagName());
			}
			api.addTags(tags);
			api.setLastUpdated(registry.get(artifactPath).getLastModified());

            String defaultVersion = AppMDAO.getDefaultVersion(apiName, providerName,
                                                              AppDefaultVersion.APP_IS_ANY_LIFECYCLE_STATE);
            api.setDefaultVersion(apiVersion.equals(defaultVersion));

            //Set Lifecycle status
            if (artifact.getLifecycleState() != null && artifact.getLifecycleState() != "") {
                if (artifact.getLifecycleState().toUpperCase().equalsIgnoreCase(APIStatus.INREVIEW.getStatus())) {
                    api.setLifeCycleStatus(APIStatus.INREVIEW);
                } else {
                    api.setLifeCycleStatus(APIStatus.valueOf(artifact.getLifecycleState().toUpperCase()));
                }
            }
            api.setLifeCycleName(artifact.getLifecycleName());

		} catch (GovernanceException e) {
			String msg = "Failed to get WebApp fro artifact ";
			throw new AppManagementException(msg, e);
		} catch (RegistryException e) {
			String msg = "Failed to get LastAccess time or Rating";
			throw new AppManagementException(msg, e);
		} catch (UserStoreException e) {
			String msg = "Failed to get User Realm of WebApp Provider";
			throw new AppManagementException(msg, e);
		}
		return api;
	}

    /**
     * Generate MobileApp Data Model from a given mobileapp GenericArtifact
     * @param artifact 'mobileapp' GenericArtifact
     * @return MobileApp
     * @throws AppManagementException
     */
    public static MobileApp getMobileApp(GenericArtifact artifact) throws AppManagementException {
        MobileApp mobileApp = new MobileApp();
        try {
            mobileApp.setAppName(artifact.getAttribute(AppMConstants.API_OVERVIEW_NAME));
            mobileApp.setAppUrl(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_URL));
            mobileApp.setBundleVersion(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_BUNDLE_VERSION));
            mobileApp.setPackageName(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_PACKAGE_NAME));
            mobileApp.setCategory(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_CATEGORY));
            mobileApp.setThumbnail(artifact.getAttribute(AppMConstants.MOBILE_APP_IMAGES_THUMBNAIL));
            mobileApp.setDisplayName(artifact.getAttribute(AppMConstants.API_OVERVIEW_DISPLAY_NAME));
            mobileApp.setRecentChanges(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_RECENT_CHANGES));
            mobileApp.setAppProvider(artifact.getAttribute(AppMConstants.API_OVERVIEW_PROVIDER));
            mobileApp.setDescription(artifact.getAttribute(AppMConstants.API_OVERVIEW_DESCRIPTION));
            mobileApp.setThumbnail(artifact.getAttribute(AppMConstants.MOBILE_APP_IMAGES_THUMBNAIL));
            mobileApp.setBanner(artifact.getAttribute(AppMConstants.APP_IMAGES_BANNER));
            mobileApp.setPlatform(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_PLATFORM));
            mobileApp.setType(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_TYPE));
            mobileApp.setCreatedTime(artifact.getAttribute(AppMConstants.API_OVERVIEW_CREATED_TIME));
            mobileApp.setLifeCycleStatus(APIStatus.valueOf(artifact.getLifecycleState().toUpperCase()));
          //  mobileApp.setAppVisibility(artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBILITY));
            String screenShots = artifact.getAttribute(AppMConstants.MOBILE_APP_IMAGES_SCREENSHOTS);
            mobileApp.setScreenShots(Arrays.asList(screenShots.split("\\s*,\\s*")));
        } catch (GovernanceException e) {
           handleException("Failed to get Mobile app with artifact id "+artifact.getId());
        }
        return mobileApp;
    }

    public static WebApp getGenericApp(GovernanceArtifact artifact) throws AppManagementException {

        WebApp api;
        try {
            String providerName = artifact.getAttribute(AppMConstants.API_OVERVIEW_PROVIDER);
            String apiName = artifact.getAttribute(AppMConstants.API_OVERVIEW_NAME);
            String apiVersion = artifact.getAttribute(AppMConstants.API_OVERVIEW_VERSION);
            api = new WebApp(new APIIdentifier(providerName, apiName, apiVersion));
            api.setThumbnailUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_THUMBNAIL_URL));
            api.setStatus(getApiStatus(artifact.getAttribute(AppMConstants.API_OVERVIEW_STATUS)));
            api.setContext(artifact.getAttribute(AppMConstants.API_OVERVIEW_CONTEXT));
            api.setVisibility(artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBILITY));
            api.setVisibleRoles(artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBLE_ROLES));
            api.setVisibleTenants(artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBLE_TENANTS));
            api.setTransports(artifact.getAttribute(AppMConstants.API_OVERVIEW_TRANSPORTS));
            api.setInSequence(artifact.getAttribute(AppMConstants.API_OVERVIEW_INSEQUENCE));
            api.setInSequence(artifact.getAttribute(AppMConstants.API_OVERVIEW_OUTSEQUENCE));
            api.setDescription(artifact.getAttribute(AppMConstants.API_OVERVIEW_DESCRIPTION));
            api.setResponseCache(artifact.getAttribute(AppMConstants.API_OVERVIEW_RESPONSE_CACHING));
            api.setSsoEnabled(artifact.getAttribute("sso_enableSso"));
            api.setUUID(artifact.getId());
            api.setThumbnailUrl(artifact.getAttribute(AppMConstants.APP_IMAGES_THUMBNAIL));


            int cacheTimeout = AppMConstants.API_RESPONSE_CACHE_TIMEOUT;
            try {
                cacheTimeout =
                        Integer.parseInt(artifact.getAttribute(AppMConstants.API_OVERVIEW_CACHE_TIMEOUT));
            } catch (NumberFormatException e) {
                // ignore
            }
            api.setCacheTimeout(cacheTimeout);

            api.setRedirectURL(artifact.getAttribute(AppMConstants.API_OVERVIEW_REDIRECT_URL));
            api.setAppOwner(artifact.getAttribute(AppMConstants.API_OVERVIEW_OWNER));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(
                    AppMConstants.API_OVERVIEW_ADVERTISE_ONLY)));

            api.setEndpointConfig(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_CONFIG));

            api.setSubscriptionAvailability(artifact.getAttribute(
                    AppMConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
            api.setSubscriptionAvailableTenants(artifact.getAttribute(
                    AppMConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

            //Set Lifecycle status
            if (artifact.getLifecycleState() != null && artifact.getLifecycleState() != "") {
                if (artifact.getLifecycleState().toUpperCase().equalsIgnoreCase(APIStatus.INREVIEW.getStatus())) {
                    api.setLifeCycleStatus(APIStatus.INREVIEW);
                } else {
                    api.setLifeCycleStatus(APIStatus.valueOf(artifact.getLifecycleState().toUpperCase()));
                }
            }
            api.setLifeCycleName(artifact.getLifecycleName());

            api.setMediaType(artifact.getMediaType());
            api.setPath(artifact.getPath());
            api.setCreatedTime(artifact.getAttribute(AppMConstants.API_OVERVIEW_CREATED_TIME));

        } catch (GovernanceException e) {
            String msg = "Failed to get WebApp from artifact ";
            throw new AppManagementException(msg, e);
        }
        return api;
    }

    /**
     * This method used to get WebApp from governance artifact
     *
     * @param artifact
     *            WebApp artifact
     * @param registry
     *            Registry
     * @return WebApp
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     *             if failed to get WebApp from artifact
     */
    public static WebApp getGenericApp(GovernanceArtifact artifact, Registry registry)
            throws
            AppManagementException {

        WebApp api;
        try {
            String providerName = artifact.getAttribute(AppMConstants.API_OVERVIEW_PROVIDER);
            String apiName = artifact.getAttribute(AppMConstants.API_OVERVIEW_NAME);
            String apiVersion = artifact.getAttribute(AppMConstants.API_OVERVIEW_VERSION);
            APIIdentifier apiId = new APIIdentifier(AppManagerUtil.replaceEmailDomainBack(providerName), apiName, apiVersion);
            api = new WebApp(apiId);
            // set rating
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            // BigDecimal bigDecimal = new BigDecimal(getAverageRating(apiId));
            // BigDecimal res = bigDecimal.setScale(1, RoundingMode.HALF_UP);

            // TODO revert this once proper db saving is done
            api.setRating(1f);
            //set name
            api.setApiName(apiName);
            // set description

            api.setDescription(artifact.getAttribute(AppMConstants.API_OVERVIEW_DESCRIPTION));
            // set last access time
            api.setLastUpdated(registry.get(artifactPath).getLastModified());
            // set url
            api.setUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_URL));
            api.setLogoutURL(artifact.getAttribute(AppMConstants.API_OVERVIEW_LOGOUT_URL));
            api.setDisplayName(artifact.getAttribute(AppMConstants.API_OVERVIEW_DISPLAY_NAME));
            api.setSandboxUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_SANDBOX_URL));
            api.setStatus(getApiStatus(artifact.getLifecycleState().toUpperCase()));
            api.setThumbnailUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_THUMBNAIL_URL));
            api.setWsdlUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_WSDL));
            api.setWadlUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_WADL));
            api.setTechnicalOwner(artifact.getAttribute(AppMConstants.API_OVERVIEW_TEC_OWNER));
            api.setTechnicalOwnerEmail(artifact.getAttribute(AppMConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
            api.setBusinessOwner(artifact.getAttribute(AppMConstants.API_OVERVIEW_BUSS_OWNER));
            api.setBusinessOwnerEmail(artifact.getAttribute(AppMConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));
            api.setVisibility(artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBILITY));
            api.setVisibleRoles(artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBLE_ROLES));
            api.setVisibleTenants(artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBLE_TENANTS));
            api.setEndpointSecured(Boolean.parseBoolean(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_SECURED)));
            api.setEndpointUTUsername(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_USERNAME));
            api.setEndpointUTPassword(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_PASSWORD));
            api.setTransports(artifact.getAttribute(AppMConstants.API_OVERVIEW_TRANSPORTS));
            api.setInSequence(artifact.getAttribute(AppMConstants.API_OVERVIEW_INSEQUENCE));
            api.setOutSequence(artifact.getAttribute(AppMConstants.API_OVERVIEW_OUTSEQUENCE));
            api.setResponseCache(artifact.getAttribute(AppMConstants.API_OVERVIEW_RESPONSE_CACHING));
            api.setSsoEnabled(artifact.getAttribute("sso_singleSignOn"));
            api.setUUID(artifact.getId());
            api.setThumbnailUrl(artifact.getAttribute(AppMConstants.APP_IMAGES_THUMBNAIL));
            api.setSkipGateway(Boolean.parseBoolean(artifact.getAttribute(AppMConstants.API_OVERVIEW_SKIP_GATEWAY)));
            api.setTreatAsASite(artifact.getAttribute(AppMConstants.APP_OVERVIEW_TREAT_AS_A_SITE));
            api.setAllowAnonymous(Boolean.parseBoolean(artifact.getAttribute(AppMConstants.API_OVERVIEW_ALLOW_ANONYMOUS)));

            int cacheTimeout = AppMConstants.API_RESPONSE_CACHE_TIMEOUT;
            try {
                cacheTimeout =
                        Integer.parseInt(artifact.getAttribute(AppMConstants.API_OVERVIEW_CACHE_TIMEOUT));
            } catch (NumberFormatException e) {
                // ignore
            }

            api.setCacheTimeout(cacheTimeout);

            api.setEndpointConfig(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_CONFIG));

            api.setRedirectURL(artifact.getAttribute(AppMConstants.API_OVERVIEW_REDIRECT_URL));
            api.setAppOwner(artifact.getAttribute(AppMConstants.API_OVERVIEW_OWNER));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(AppMConstants.API_OVERVIEW_ADVERTISE_ONLY)));
            api.setAppTenant(artifact.getAttribute(AppMConstants.API_OVERVIEW_TENANT));
            api.setDisplayName(artifact.getAttribute(AppMConstants.API_OVERVIEW_DISPLAY_NAME));
            api.setSubscriptionAvailability(artifact.getAttribute(AppMConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
            api.setSubscriptionAvailableTenants(artifact.getAttribute(AppMConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

            String tenantDomainName =
                    MultitenantUtils.getTenantDomain(replaceEmailDomainBack(providerName));
            int tenantId =
                    ServiceReferenceHolder.getInstance().getRealmService()
                            .getTenantManager().getTenantId(tenantDomainName);

            Set<Tier> availableTier = new HashSet<Tier>();
            String tiers = artifact.getAttribute(AppMConstants.API_OVERVIEW_TIER);

            Map<String, Tier> definedTiers = getTiers(tenantId);
            if (tiers != null && !"".equals(tiers)) {
                String[] tierNames = tiers.split(",");
                for (String tierName : tierNames) {
                    Tier definedTier = definedTiers.get(tierName);
                    if (definedTier != null) {
                        availableTier.add(definedTier);
                    } else {
                        log.warn("Unknown tier: " + tierName + " found on WebApp: " + apiName);
                    }
                }
            }
            api.addAvailableTiers(availableTier);
            if(tenantId==-1234) {
                api.setContext(artifact.getAttribute(AppMConstants.API_OVERVIEW_CONTEXT));
            }else{
                String context = artifact.getAttribute(AppMConstants.API_OVERVIEW_CONTEXT);
                if(!context.startsWith(RegistryConstants.PATH_SEPARATOR)){
                    context = RegistryConstants.PATH_SEPARATOR + context;
                }
                api.setContext(RegistryConstants.PATH_SEPARATOR + "t" + RegistryConstants.PATH_SEPARATOR + tenantDomainName + context);
            }
            api.setLatest(Boolean.valueOf(artifact.getAttribute(AppMConstants.API_OVERVIEW_IS_LATEST)));

            Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
            List<String> uriTemplateNames = new ArrayList<String>();

            List<URLMapping> urlPatterns = AppMDAO.getURITemplatesPerAPIAsString(apiId);

            for (URLMapping urlMapping : urlPatterns) {
                URITemplate uriTemplate = new URITemplate();
                String uTemplate = urlMapping.getUrlPattern();
                String method = urlMapping.getHttpMethod();
                String authType = urlMapping.getHttpMethod();
                String throttlingTier = urlMapping.getThrottlingTier();
                String userRoles = urlMapping.getUserRoles();

                uriTemplate.setHTTPVerb(method);
                uriTemplate.setAuthType(authType);
                uriTemplate.setThrottlingTier(throttlingTier);
                uriTemplate.setHttpVerbs(method);
                uriTemplate.setAuthTypes(authType);
                uriTemplate.setUriTemplate(uTemplate);
                uriTemplate.setResourceURI(api.getUrl());
                uriTemplate.setResourceSandboxURI(api.getSandboxUrl());
                uriTemplate.setThrottlingTiers(throttlingTier);
                uriTemplate.setUserRoles(userRoles);
                // Checking for duplicate uri template names
                if (uriTemplateNames.contains(uTemplate)) {
                    for (URITemplate tmp : uriTemplates) {
                        if (uTemplate.equals(tmp.getUriTemplate())) {
                            tmp.setHttpVerbs(method);
                            tmp.setAuthTypes(authType);
                            tmp.setThrottlingTiers(throttlingTier);
                            break;
                        }
                    }

                } else {
                    uriTemplates.add(uriTemplate);
                }

                uriTemplateNames.add(uTemplate);

            }
            api.setUriTemplates(uriTemplates);

            Set<String> tags = new HashSet<String>();
            org.wso2.carbon.registry.core.Tag[] tag = registry.getTags(artifactPath);
            for (Tag tag1 : tag) {
                tags.add(tag1.getTagName());
            }
            api.addTags(tags);
            api.setLastUpdated(registry.get(artifactPath).getLastModified());

            String defaultVersion = AppMDAO.getDefaultVersion(apiName, providerName,
                                                              AppDefaultVersion.APP_IS_ANY_LIFECYCLE_STATE);
            api.setDefaultVersion(apiVersion.equals(defaultVersion));

            //Set Lifecycle status
            if (artifact.getLifecycleState() != null && artifact.getLifecycleState() != "") {
                if (artifact.getLifecycleState().toUpperCase().equalsIgnoreCase(APIStatus.INREVIEW.getStatus())) {
                    api.setLifeCycleStatus(APIStatus.INREVIEW);
                } else {
                    api.setLifeCycleStatus(APIStatus.valueOf(artifact.getLifecycleState().toUpperCase()));
                }
            }
            api.setLifeCycleName(artifact.getLifecycleName());

            api.setMediaType(artifact.getMediaType());
            api.setPath(artifact.getPath());
            api.setCreatedTime(artifact.getAttribute(AppMConstants.API_OVERVIEW_CREATED_TIME));

        } catch (GovernanceException e) {
            String msg = "Failed to get WebApp fro artifact ";
            throw new AppManagementException(msg, e);
        } catch (RegistryException e) {
            String msg = "Failed to get LastAccess time or Rating";
            throw new AppManagementException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Failed to get User Realm of WebApp Provider";
            throw new AppManagementException(msg, e);
        }
        return api;
    }



    public static WebApp getAPI(GovernanceArtifact artifact) throws AppManagementException {

		WebApp api;
		try {
			String providerName = artifact.getAttribute(AppMConstants.API_OVERVIEW_PROVIDER);
			String apiName = artifact.getAttribute(AppMConstants.API_OVERVIEW_NAME);
			String apiVersion = artifact.getAttribute(AppMConstants.API_OVERVIEW_VERSION);
			api = new WebApp(new APIIdentifier(providerName, apiName, apiVersion));
			api.setThumbnailUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_THUMBNAIL_URL));
			api.setStatus(getApiStatus(artifact.getAttribute(AppMConstants.API_OVERVIEW_STATUS)));
			api.setContext(artifact.getAttribute(AppMConstants.API_OVERVIEW_CONTEXT));
			api.setVisibility(artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBILITY));
			api.setVisibleRoles(artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBLE_ROLES));
			api.setVisibleTenants(artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBLE_TENANTS));
			api.setTransports(artifact.getAttribute(AppMConstants.API_OVERVIEW_TRANSPORTS));
			api.setInSequence(artifact.getAttribute(AppMConstants.API_OVERVIEW_INSEQUENCE));
			api.setInSequence(artifact.getAttribute(AppMConstants.API_OVERVIEW_OUTSEQUENCE));
			api.setDescription(artifact.getAttribute(AppMConstants.API_OVERVIEW_DESCRIPTION));
			api.setResponseCache(artifact.getAttribute(AppMConstants.API_OVERVIEW_RESPONSE_CACHING));
            api.setSsoEnabled(artifact.getAttribute("sso_enableSso"));
            api.setUUID(artifact.getId());
            api.setThumbnailUrl(artifact.getAttribute(AppMConstants.APP_IMAGES_THUMBNAIL));


            int cacheTimeout = AppMConstants.API_RESPONSE_CACHE_TIMEOUT;
			try {
				cacheTimeout =
				               Integer.parseInt(artifact.getAttribute(AppMConstants.API_OVERVIEW_CACHE_TIMEOUT));
			} catch (NumberFormatException e) {
				// ignore
			}
			api.setCacheTimeout(cacheTimeout);

			api.setRedirectURL(artifact.getAttribute(AppMConstants.API_OVERVIEW_REDIRECT_URL));
            api.setAppOwner(artifact.getAttribute(AppMConstants.API_OVERVIEW_OWNER));
			api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(AppMConstants.API_OVERVIEW_ADVERTISE_ONLY)));

			api.setEndpointConfig(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_CONFIG));

			api.setSubscriptionAvailability(artifact.getAttribute(AppMConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
			api.setSubscriptionAvailableTenants(artifact.getAttribute(AppMConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

            //Set Lifecycle status
            if (artifact.getLifecycleState() != null && artifact.getLifecycleState() != "") {
                if (artifact.getLifecycleState().toUpperCase().equalsIgnoreCase(APIStatus.INREVIEW.getStatus())) {
                    api.setLifeCycleStatus(APIStatus.INREVIEW);
                } else {
                    api.setLifeCycleStatus(APIStatus.valueOf(artifact.getLifecycleState().toUpperCase()));
                }
            }
            api.setLifeCycleName(artifact.getLifecycleName());


		} catch (GovernanceException e) {
			String msg = "Failed to get WebApp from artifact ";
			throw new AppManagementException(msg, e);
		}
		return api;
	}

	/**
	 * This method used to get Provider from provider artifact
	 * 
	 * @param artifact
	 *            provider artifact
	 * @return Provider
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             if failed to get Provider from provider artifact.
	 */
	public static Provider getProvider(GenericArtifact artifact) throws AppManagementException {
		Provider provider;
		try {
			provider = new Provider(artifact.getAttribute(AppMConstants.PROVIDER_OVERVIEW_NAME));
			provider.setDescription(artifact.getAttribute(AppMConstants.PROVIDER_OVERVIEW_DESCRIPTION));
			provider.setEmail(artifact.getAttribute(AppMConstants.PROVIDER_OVERVIEW_EMAIL));

		} catch (GovernanceException e) {
			String msg = "Failed to get provider ";
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		}
		return provider;
	}

	/**
	 * Create Governance artifact from given attributes
	 * 
	 * @param artifact
	 *            initial governance artifact
	 * @param api
	 *            WebApp object with the attributes value
	 * @return GenericArtifact
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             if failed to create WebApp
	 */
	public static GenericArtifact createAPIArtifactContent(GenericArtifact artifact, WebApp api)
	                                                                                            throws
                                                                                                AppManagementException {
		try {
			String apiStatus = api.getStatus().getStatus();
			artifact.setAttribute(AppMConstants.API_OVERVIEW_NAME, api.getId().getApiName());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_VERSION, api.getId().getVersion());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_CONTEXT, api.getContext());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_PROVIDER, api.getId().getProviderName());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_DESCRIPTION, api.getDescription());
            artifact.setAttribute(AppMConstants.APP_OVERVIEW_TREAT_AS_A_SITE, api.getTreatAsASite());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_URL, api.getUrl());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_LOGOUT_URL, api.getLogoutURL());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_SANDBOX_URL, api.getSandboxUrl());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_WSDL, api.getWsdlUrl());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_WADL, api.getWadlUrl());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_DISPLAY_NAME, api.getDisplayName());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_THUMBNAIL_URL, api.getThumbnailUrl());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_STATUS, apiStatus);
			artifact.setAttribute(AppMConstants.API_OVERVIEW_TEC_OWNER, api.getTechnicalOwner());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_TEC_OWNER_EMAIL,
			                      api.getTechnicalOwnerEmail());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_BUSS_OWNER, api.getBusinessOwner());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_BUSS_OWNER_EMAIL,
			                      api.getBusinessOwnerEmail());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_VISIBILITY, api.getVisibility());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_VISIBLE_ROLES, api.getVisibleRoles());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_VISIBLE_TENANTS,
			                      api.getVisibleTenants());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_SECURED,
			                      Boolean.toString(api.isEndpointSecured()));
			artifact.setAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_USERNAME,
			                      api.getEndpointUTUsername());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_PASSWORD,
			                      api.getEndpointUTPassword());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_TRANSPORTS, api.getTransports());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_INSEQUENCE, api.getInSequence());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_OUTSEQUENCE, api.getOutSequence());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_RESPONSE_CACHING,
			                      api.getResponseCache());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_CACHE_TIMEOUT,
			                      Integer.toString(api.getCacheTimeout()));

            artifact.setAttribute("sso_enableSso",
			                      api.isSsoEnabled());

			artifact.setAttribute(AppMConstants.API_OVERVIEW_REDIRECT_URL, api.getRedirectURL());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_OWNER, api.getAppOwner());
			artifact.setAttribute(AppMConstants.API_OVERVIEW_ADVERTISE_ONLY,
			                      Boolean.toString(api.isAdvertiseOnly()));

			artifact.setAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_CONFIG,
			                      api.getEndpointConfig());

			artifact.setAttribute(AppMConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY,
                                  api.getSubscriptionAvailability());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS,
                                  api.getSubscriptionAvailableTenants());
            artifact.setAttribute(AppMConstants.APP_OVERVIEW_MAKE_AS_DEFAULT_VERSION, String.valueOf(
                    api.isDefaultVersion()));

            String tiers = "";
			for (Tier tier : api.getAvailableTiers()) {
				tiers += tier.getName() + "\\|\\|";
                
			}
			if (!"".equals(tiers)) {
				tiers = tiers.substring(0, tiers.length() - 2);
				artifact.setAttribute(AppMConstants.API_OVERVIEW_TIER, tiers);
			}
			if (AppMConstants.PUBLISHED.equals(apiStatus)) {
				artifact.setAttribute(AppMConstants.API_OVERVIEW_IS_LATEST, "true");
			}
			String[] keys = artifact.getAttributeKeys();
			for (String key : keys) {
				if (key.contains("URITemplate")) {
					artifact.removeAttribute(key);
				}
			}

			Set<URITemplate> uriTemplateSet = api.getUriTemplates();
			int i = 0;
			for (URITemplate uriTemplate : uriTemplateSet) {
				artifact.addAttribute(AppMConstants.API_URI_PATTERN + i,
				                      uriTemplate.getUriTemplate());
				artifact.addAttribute(AppMConstants.API_URI_HTTP_METHOD + i,
				                      uriTemplate.getHTTPVerb());
				artifact.addAttribute(AppMConstants.API_URI_AUTH_TYPE + i, uriTemplate.getAuthType());
				i++;

			}

		} catch (GovernanceException e) {
			String msg = "Failed to create WebApp for : " + api.getId().getApiName();
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		}
		return artifact;
	}


	/**
	 * Create Governance artifact from given attributes
	 *
	 * @param artifact
	 *            initial governance artifact
	 * @param mobileApp
	 *            WebApp object with the attributes value
	 * @return GenericArtifact
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             if failed to create WebApp
	 */
	public static GenericArtifact createMobileAppArtifactContent(GenericArtifact artifact, MobileApp mobileApp)
			throws
			AppManagementException {

		try {
            artifact.setAttribute(AppMConstants.API_OVERVIEW_NAME, mobileApp.getAppName());
            artifact.setAttribute(AppMConstants.MOBILE_APP_OVERVIEW_URL, mobileApp.getAppUrl());
            //artifact.setAttribute(AppMConstants.API_OVERVIEW_VISIBILITY, mobileApp.getVisibility());
            artifact.setAttribute(AppMConstants.MOBILE_APP_OVERVIEW_BUNDLE_VERSION, mobileApp.getBundleVersion());
            artifact.setAttribute(AppMConstants.MOBILE_APP_OVERVIEW_PACKAGE_NAME, mobileApp.getPackageName());
            artifact.setAttribute(AppMConstants.MOBILE_APP_OVERVIEW_CATEGORY, mobileApp.getCategory());
            artifact.setAttribute(AppMConstants.MOBILE_APP_IMAGES_THUMBNAIL, mobileApp.getBanner());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_DISPLAY_NAME, mobileApp.getDisplayName());
            artifact.setAttribute(AppMConstants.MOBILE_APP_OVERVIEW_TYPE, mobileApp.getMarketType());
            artifact.setAttribute(AppMConstants.MOBILE_APP_OVERVIEW_RECENT_CHANGES, mobileApp.getRecentChanges());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_VERSION, mobileApp.getVersion());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_PROVIDER, mobileApp.getAppProvider());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_DESCRIPTION, mobileApp.getDescription());
            artifact.setAttribute(AppMConstants.MOBILE_APP_IMAGES_THUMBNAIL, mobileApp.getThumbnail());
            String screenShots = StringUtils.join(mobileApp.getScreenShots(), ",");
            artifact.setAttribute(AppMConstants.MOBILE_APP_IMAGES_SCREENSHOTS, screenShots);
            artifact.setAttribute(AppMConstants.APP_IMAGES_BANNER, mobileApp.getBanner());
            artifact.setAttribute(AppMConstants.MOBILE_APP_OVERVIEW_APP_ID, mobileApp.getAppId());
            artifact.setAttribute(AppMConstants.MOBILE_APP_OVERVIEW_PLATFORM, mobileApp.getPlatform());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_CREATED_TIME, mobileApp.getCreatedTime());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_VISIBILITY, StringUtils.join(mobileApp.getAppVisibility()));


		} catch (GovernanceException e) {
			String msg = "Failed to create WebApp for : " + mobileApp.getAppName();
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		}
		return artifact;
	}

    /**
     * Generate WebApp GenericArtifact content from Webapp
     * @param artifact Webapp GenericArtifact
     * @param webApp WebApp
     * @return GenericArtifact
     * @throws AppManagementException
     */
    public static GenericArtifact createWebAppArtifactContent(GenericArtifact artifact, WebApp webApp)
            throws
            AppManagementException {
        try {
            artifact.setAttribute(AppMConstants.API_OVERVIEW_NAME, webApp.getId().getApiName());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_VERSION, webApp.getId().getVersion());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_CONTEXT, webApp.getContext());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_DISPLAY_NAME, webApp.getDisplayName());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_PROVIDER, AppManagerUtil.replaceEmailDomainBack(webApp.getId().getProviderName()));
            artifact.setAttribute(AppMConstants.API_OVERVIEW_DESCRIPTION, webApp.getDescription());
            artifact.setAttribute(AppMConstants.APP_OVERVIEW_TREAT_AS_A_SITE, webApp.getTreatAsASite());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_URL, webApp.getUrl()); //
            artifact.setAttribute(AppMConstants.APP_IMAGES_THUMBNAIL, ""); //webApp.getThumbnailUrl()
            artifact.setAttribute(AppMConstants.APP_IMAGES_BANNER, "");
            artifact.setAttribute(AppMConstants.API_OVERVIEW_LOGOUT_URL, webApp.getLogoutURL());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_BUSS_OWNER, webApp.getBusinessOwner());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_BUSS_OWNER_EMAIL, webApp.getBusinessOwnerEmail());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_VISIBILITY, StringUtils.join(webApp.getAppVisibility()));
            artifact.setAttribute(AppMConstants.API_OVERVIEW_VISIBLE_TENANTS, webApp.getVisibleTenants());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_TRANSPORTS, webApp.getTransports());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_TIER, "Unlimited");
            artifact.setAttribute(AppMConstants.APP_TRACKING_CODE, webApp.getTrackingCode());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_CREATED_TIME, webApp.getCreatedTime());
            artifact.setAttribute(AppMConstants.API_OVERVIEW_ALLOW_ANONYMOUS, Boolean.toString(webApp.getAllowAnonymous()));
            artifact.setAttribute(AppMConstants.API_OVERVIEW_SKIP_GATEWAY, Boolean.toString(webApp.getSkipGateway()));
            artifact.setAttribute(AppMConstants.APP_OVERVIEW_ACS_URL, webApp.getAcsURL());

        } catch (GovernanceException e) {
            String msg = "Failed to create WebApp for : " + webApp.getId().getApiName();
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        }
        return artifact;
    }

    /**
     * This method used to retrive the Uploaded Documents from publisher
     *
     * @param userName     logged in username
     * @param resourceUrl  resource to be downloaded
     * @param tenantDomain loggedUserTenantDomain
     * @return map that contains Data of the resource
     * @throws AppManagementException
     */
    public static Map<String, Object> getDocument(String userName, String resourceUrl, String tenantDomain)
            throws AppManagementException {
        Map<String, Object> documentMap = new HashMap<String, Object>();

        InputStream inStream = null;
        String[] resourceSplitPath =
                resourceUrl.split(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
        if (resourceSplitPath.length == 2) {
            resourceUrl = resourceSplitPath[1];
        } else {
           handleException("Invalid resource Path " + resourceUrl);
        }
        Resource apiDocResource;
        Registry registryType = null;
        boolean isTenantFlowStarted = false;
        try {
            int tenantId;
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                isTenantFlowStarted = true;

                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            } else {
                tenantId = MultitenantConstants.SUPER_TENANT_ID;
            }

            userName = MultitenantUtils.getTenantAwareUsername(userName);
            registryType = ServiceReferenceHolder
                    .getInstance().
                            getRegistryService().getGovernanceUserRegistry(userName, tenantId);
            if (registryType.resourceExists(resourceUrl)) {
                apiDocResource = registryType.get(resourceUrl);
                inStream = apiDocResource.getContentStream();
                documentMap.put("Data", inStream);
                documentMap.put("contentType", apiDocResource.getMediaType());
                String[] content = apiDocResource.getPath().split("/");
                documentMap.put("name", content[content.length - 1]);
            }
        } catch (RegistryException e) {
            String msg = "Couldn't retrieve registry for User " + userName + " Tenant " + tenantDomain;
            log.error(msg, e);
            handleException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return documentMap;
    }

    /**
	 * Create the Documentation from artifact
	 * 
	 * @param artifact
	 *            Documentation artifact
	 * @return Documentation
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             if failed to create Documentation from artifact
	 */
	public static Documentation getDocumentation(GenericArtifact artifact)
	                                                                      throws
                                                                          AppManagementException {

		Documentation documentation;

		try {
			DocumentationType type;
			String docType = artifact.getAttribute(AppMConstants.DOC_TYPE);

			if (docType.equalsIgnoreCase(DocumentationType.HOWTO.getType())) {
				type = DocumentationType.HOWTO;
			} else if (docType.equalsIgnoreCase(DocumentationType.PUBLIC_FORUM.getType())) {
				type = DocumentationType.PUBLIC_FORUM;
			} else if (docType.equalsIgnoreCase(DocumentationType.SUPPORT_FORUM.getType())) {
				type = DocumentationType.SUPPORT_FORUM;
			} else if (docType.equalsIgnoreCase(DocumentationType.API_MESSAGE_FORMAT.getType())) {
				type = DocumentationType.API_MESSAGE_FORMAT;
			} else if (docType.equalsIgnoreCase(DocumentationType.SAMPLES.getType())) {
				type = DocumentationType.SAMPLES;
			} else {
				type = DocumentationType.OTHER;
			}
			documentation = new Documentation(type, artifact.getAttribute(AppMConstants.DOC_NAME));
            documentation.setId(artifact.getId());
			documentation.setSummary(artifact.getAttribute(AppMConstants.DOC_SUMMARY));

			Documentation.DocumentSourceType docSourceType =
			                                                 Documentation.DocumentSourceType.INLINE;
			String artifactAttribute = artifact.getAttribute(AppMConstants.DOC_SOURCE_TYPE);

			if (artifactAttribute.equals(Documentation.DocumentSourceType.URL.name())) {
				docSourceType = Documentation.DocumentSourceType.URL;
			} else if (artifactAttribute.equals(Documentation.DocumentSourceType.FILE.name())) {
				docSourceType = Documentation.DocumentSourceType.FILE;
			}

			documentation.setSourceType(docSourceType);
			if (artifact.getAttribute(AppMConstants.DOC_SOURCE_TYPE).equals("URL")) {
				documentation.setSourceUrl(artifact.getAttribute(AppMConstants.DOC_SOURCE_URL));
			}

			if (docSourceType == Documentation.DocumentSourceType.FILE) {
				documentation.setFilePath(prependWebContextRoot(artifact.getAttribute(AppMConstants.DOC_FILE_PATH)));
			}

			if (documentation.getType() == DocumentationType.OTHER) {
				documentation.setOtherTypeName(artifact.getAttribute(AppMConstants.DOC_OTHER_TYPE_NAME));
			}

		} catch (GovernanceException e) {
			throw new AppManagementException("Failed to get documentation from artifact", e);
		}
		return documentation;
	}

	/**
	 * Create the Documentation from artifact
	 * 
	 * @param artifact
	 *            Documentation artifact
	 * @return Documentation
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             if failed to create Documentation from artifact
	 */
	public static Documentation getDocumentation(GenericArtifact artifact, String docCreatorName)
	                                                                                             throws
                                                                                                 AppManagementException {

		Documentation documentation;

		try {
			DocumentationType type;
			String docType = artifact.getAttribute(AppMConstants.DOC_TYPE);

			if (docType.equalsIgnoreCase(DocumentationType.HOWTO.getType())) {
				type = DocumentationType.HOWTO;
			} else if (docType.equalsIgnoreCase(DocumentationType.PUBLIC_FORUM.getType())) {
				type = DocumentationType.PUBLIC_FORUM;
			} else if (docType.equalsIgnoreCase(DocumentationType.SUPPORT_FORUM.getType())) {
				type = DocumentationType.SUPPORT_FORUM;
			} else if (docType.equalsIgnoreCase(DocumentationType.API_MESSAGE_FORMAT.getType())) {
				type = DocumentationType.API_MESSAGE_FORMAT;
			} else if (docType.equalsIgnoreCase(DocumentationType.SAMPLES.getType())) {
				type = DocumentationType.SAMPLES;
			} else {
				type = DocumentationType.OTHER;
			}
			documentation = new Documentation(type, artifact.getAttribute(AppMConstants.DOC_NAME));
			documentation.setSummary(artifact.getAttribute(AppMConstants.DOC_SUMMARY));

			Documentation.DocumentSourceType docSourceType =
			                                                 Documentation.DocumentSourceType.INLINE;
			String artifactAttribute = artifact.getAttribute(AppMConstants.DOC_SOURCE_TYPE);

			if (artifactAttribute.equals(Documentation.DocumentSourceType.URL.name())) {
				docSourceType = Documentation.DocumentSourceType.URL;
			} else if (artifactAttribute.equals(Documentation.DocumentSourceType.FILE.name())) {
				docSourceType = Documentation.DocumentSourceType.FILE;
			}

			documentation.setSourceType(docSourceType);
			if (artifact.getAttribute(AppMConstants.DOC_SOURCE_TYPE).equals("URL")) {
				documentation.setSourceUrl(artifact.getAttribute(AppMConstants.DOC_SOURCE_URL));
			}

			if (docSourceType == Documentation.DocumentSourceType.FILE) {
				String filePath =
				                  prependTenantPrefix(artifact.getAttribute(AppMConstants.DOC_FILE_PATH),
				                                      docCreatorName);
				documentation.setFilePath(prependWebContextRoot(filePath));
			}

			if (documentation.getType() == DocumentationType.OTHER) {
				documentation.setOtherTypeName(artifact.getAttribute(AppMConstants.DOC_OTHER_TYPE_NAME));
			}

		} catch (GovernanceException e) {
			throw new AppManagementException("Failed to get documentation from artifact", e);
		}
		return documentation;
	}

	public static APIStatus getApiStatus(String status) throws AppManagementException {
		APIStatus apiStatus = null;
		for (APIStatus aStatus : APIStatus.values()) {
			if (aStatus.getStatus().equals(status)) {
				apiStatus = aStatus;
			}
		}
		return apiStatus;

	}

	/**
	 * Prepends the Tenant Prefix to a registry path. ex: /t/test1.com
	 * 
	 * @param postfixUrl
	 *            path to be prepended.
	 * @return Path prepended with he Tenant domain prefix.
	 */
	public static String prependTenantPrefix(String postfixUrl, String username) {
		String tenantDomain = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(username));
		if (!(tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME))) {
			String tenantPrefix = "/t/";
			if (tenantDomain != null) {

				postfixUrl = tenantPrefix + tenantDomain + postfixUrl;
			}
		}

		return postfixUrl;
	}

	/**
	 * Prepends the webcontextroot to a registry path.
	 * 
	 * @param postfixUrl
	 *            path to be prepended.
	 * @return Path prepended with he WebContext root.
	 */
	public static String prependWebContextRoot(String postfixUrl) {
		String webContext = CarbonUtils.getServerConfiguration().getFirstProperty("WebContextRoot");
		if (webContext != null && !webContext.equals("/")) {

			postfixUrl = webContext + postfixUrl;
		}
		return postfixUrl;
	}

	/**
	 * Utility method for creating storage path for an icon.
	 * 
	 * @param identifier
	 *            APIIdentifier
	 * @return Icon storage path.
	 */
	public static String getIconPath(APIIdentifier identifier) {
		String artifactPath =
		                      AppMConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR +
		                              identifier.getProviderName() +
		                              RegistryConstants.PATH_SEPARATOR + identifier.getApiName() +
		                              RegistryConstants.PATH_SEPARATOR + identifier.getVersion();
		return artifactPath + RegistryConstants.PATH_SEPARATOR + AppMConstants.API_ICON_IMAGE;
	}

	/**
	 * Utility method to generate the path for a file.
	 * 
	 * @param identifier
	 *            APIIdentifier
	 * @return Generated path.
	 * @fileName File name.
	 */
	public static String getDocumentationFilePath(APIIdentifier identifier, String fileName) {
		String contentPath =
		                     AppManagerUtil.getAPIDocPath(identifier) + AppMConstants.DOCUMENT_FILE_DIR +
		                             RegistryConstants.PATH_SEPARATOR + fileName;
		return contentPath;
	}

	public static String getAPIDefinitionFilePath(String apiName, String apiVersion) {
		String resourcePath =
		                      AppMConstants.API_DOC_LOCATION + RegistryConstants.PATH_SEPARATOR +
		                              apiName + "-" + apiVersion +
		                              RegistryConstants.PATH_SEPARATOR +
		                              AppMConstants.API_DOC_RESOURCE_NAME;

		return resourcePath;
	}

	/**
	 * Utility method to get api path from APIIdentifier
	 * 
	 * @param identifier
	 *            APIIdentifier
	 * @return WebApp path
	 */
	public static String getAPIPath(APIIdentifier identifier) {
//        if(identifier.getProviderName().contains("@")){
//            String providerName = identifier.getProviderName().split("@")[0];
//            return AppMConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
//                    providerName + RegistryConstants.PATH_SEPARATOR +
//                    identifier.getApiName() + RegistryConstants.PATH_SEPARATOR +
//                    identifier.getVersion() + AppMConstants.API_RESOURCE_NAME;
//
//        } else {

        // replace '/' with ':'
        String provider = makeSecondaryUSNameRegFriendly(identifier.getProviderName());
        provider = AppManagerUtil.replaceEmailDomain(provider);
        return AppMConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    provider + RegistryConstants.PATH_SEPARATOR +
                    identifier.getApiName() + RegistryConstants.PATH_SEPARATOR +
                    identifier.getVersion() + AppMConstants.API_RESOURCE_NAME;
//        }
	}

    /**
     * In the registry APP is added in the form of WSO2.com:appName
     * When it comes to the workflow reference saved in the APPM DB ':' charrachter
     * is used as a data separator, to avoid the confusion the value read from the DB
     * which is in this case a '/' is replaced with a registry compatible ':'
     *
     * @param provider
     * @return
     */
    public static String makeSecondaryUSNameRegFriendly(String provider) {
        if(provider.contains("/")){
            return provider.replace('/' , ':');
        }
        return provider;
    }

    /**
     * In the registry APP is added in the form of WSO2.com:appName
     * When it comes to the workflow reference saved in the APPM DB ':' charrachter
     * is used as a data separator, to avoid the confusion the value read from the UI/Registry
     * which is in this case a ':' is replaced with a DB compatible '/'
     *
     * @param appProvider
     * @return
     */
    public static String makeSecondaryUSNameDBFriendly(String appProvider) {
        if(appProvider.contains(":")){
            return appProvider.replace(':','/');
        }
        return appProvider;
    }

    /**
	 * Utility method to get WebApp provider path
	 * 
	 * @param identifier
	 *            APIIdentifier
	 * @return WebApp provider path
	 */
	public static String getAPIProviderPath(APIIdentifier identifier) {
		return AppMConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
		       identifier.getProviderName();
	}

	/**
	 * Utility method to get documentation path
	 * 
	 * @param apiId
	 *            APIIdentifier
	 * @return Doc path
	 */
	public static String getAPIDocPath(APIIdentifier apiId) {
		return AppMConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
		       apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
		       RegistryConstants.PATH_SEPARATOR + apiId.getVersion() +
		       RegistryConstants.PATH_SEPARATOR + AppMConstants.DOC_DIR +
		       RegistryConstants.PATH_SEPARATOR;
	}

	/**
	 * Utility method to get documentation content file path
	 * 
	 * @param apiId
	 *            APIIdentifier
	 * @param documentationName
	 *            String
	 * @return Doc content path
	 */
	public static String getAPIDocContentPath(APIIdentifier apiId, String documentationName) {
		return getAPIDocPath(apiId) + AppMConstants.INLINE_DOCUMENT_CONTENT_DIR +
		       RegistryConstants.PATH_SEPARATOR + documentationName;
	}

	/**
	 * This utility method used to create documentation artifact content
	 * 
	 * @param artifact
	 *            GovernanceArtifact
	 * @param apiId
	 *            APIIdentifier
	 * @param documentation
	 *            Documentation
	 * @return GenericArtifact
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             if failed to get GovernanceArtifact from Documentation
	 */
	public static GenericArtifact createDocArtifactContent(GenericArtifact artifact,
	                                                       APIIdentifier apiId,
	                                                       Documentation documentation)
	                                                                                   throws
                                                                                       AppManagementException {
		try {
			artifact.setAttribute(AppMConstants.DOC_NAME, documentation.getName());
			artifact.setAttribute(AppMConstants.DOC_SUMMARY, documentation.getSummary());
			artifact.setAttribute(AppMConstants.DOC_TYPE, documentation.getType().getType());

			Documentation.DocumentSourceType sourceType = documentation.getSourceType();

			switch (sourceType) {
				case INLINE:
					sourceType = Documentation.DocumentSourceType.INLINE;
                    //TODO:Need to fix
                    documentation.setSourceUrl("null");
					break;
				case URL:
					sourceType = Documentation.DocumentSourceType.URL;
					break;
				case FILE: {
					sourceType = Documentation.DocumentSourceType.FILE;
                    //TODO:Need to fix
                    documentation.setSourceUrl("null");
                }
					break;
			}
			artifact.setAttribute(AppMConstants.DOC_SOURCE_TYPE, sourceType.name());
			artifact.setAttribute(AppMConstants.DOC_SOURCE_URL, documentation.getSourceUrl());
			artifact.setAttribute(AppMConstants.DOC_FILE_PATH, documentation.getFilePath());
			artifact.setAttribute(AppMConstants.DOC_OTHER_TYPE_NAME,
			                      documentation.getOtherTypeName());
			String basePath =
			                  apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR +
			                          apiId.getApiName() + RegistryConstants.PATH_SEPARATOR +
			                          apiId.getVersion();
			artifact.setAttribute(AppMConstants.DOC_API_BASE_PATH, basePath);
		} catch (GovernanceException e) {
			String msg = "Filed to create doc artifact content from :" + documentation.getName();
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		}
		return artifact;
	}

	/**
	 * this method used to initialized the ArtifactManager
	 * 
	 * @param registry
	 *            Registry
	 * @param key
	 *            , key name of the key
	 * @return GenericArtifactManager
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             if failed to initialized GenericArtifactManager
	 */
	public static GenericArtifactManager getArtifactManager(Registry registry, String key)
	                                                                                      throws
                                                                                          AppManagementException {
		GenericArtifactManager artifactManager = null;

		try {
			GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
			if (GovernanceUtils.findGovernanceArtifactConfiguration(key, registry) != null) {
				artifactManager = new GenericArtifactManager(registry, key);
			}
		} catch (RegistryException e) {
			String msg = "Failed to initialize GenericArtifactManager";
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		}
		return artifactManager;
	}

	/**
	 * Crate an WSDL from given wsdl url. Reset the endpoint details to gateway
	 * node
	 ** 
	 * @param registry
	 *            - Governance Registry space to save the WSDL
	 * @param api
	 *            -WebApp instance
	 * @return Path of the created resource
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             If an error occurs while adding the WSDL
	 */

	public static String createWSDL(Registry registry, WebApp api) throws RegistryException,
                                                                          AppManagementException {

		try {
			String wsdlResourcePath =
			                          AppMConstants.API_WSDL_RESOURCE_LOCATION +
			                                  api.getId().getProviderName() + "--" +
			                                  api.getId().getApiName() + api.getId().getVersion() +
			                                  ".wsdl";

			APIMWSDLReader wsdlreader = new APIMWSDLReader(api.getWsdlUrl());
			OMElement wsdlContentEle = wsdlreader.readAndCleanWsdl();
			Resource wsdlResource = registry.newResource();
			wsdlResource.setContent(wsdlContentEle.toString());

			registry.put(wsdlResourcePath, wsdlResource);
			// set the anonymous role for wsld resource to avoid basicauth
			// security.
			setResourcePermissions(api.getId().getProviderName(), null, null, wsdlResourcePath);

			// set the wsdl resource permlink as the wsdlURL.
			api.setWsdlUrl(getRegistryResourceHTTPPermlink(wsdlResource.getPath()));

			return wsdlResourcePath;

		} catch (RegistryException e) {
			String msg = "Failed to add WSDL " + api.getWsdlUrl() + " to the registry";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		} catch (AppManagementException e) {
			String msg = "Failed to reset the WSDL : " + api.getWsdlUrl();
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		}
	}

	/**
	 * Read the GateWay Endpoint from the APIConfiguration. If multiple Gateway
	 * environments defined,
	 * take only the production node's Endpoint.
	 * Else, pick what is available as the gateway node.
	 * 
	 * @return {@link String} - Gateway URL
	 */

	public static String getGatewayendpoint() {

		String gatewayURLs = getGatewayendpoints();
		String gatewayURL = extractHTTPSEndpoint(gatewayURLs);

		return gatewayURL;
	}

	/**
	 * Read the GateWay Endpoints (both http and https) from the
	 * APIConfiguration. If multiple Gateway
	 * environments defined,
	 * take only the production node's Endpoint.
	 * Else, pick what is available as the gateway node.
	 * 
	 * @return {@link String} - Gateway URL
	 */
	public static String getGatewayendpoints() {

		String gatewayURLs = null;
		List<Environment> gatewayEnvironments =
		                                        ServiceReferenceHolder.getInstance()
		                                                              .getAPIManagerConfigurationService()
		                                                              .getAPIManagerConfiguration()
		                                                              .getApiGatewayEnvironments();
		if (gatewayEnvironments.size() > 1) {
			for (int i = 0; i < gatewayEnvironments.size(); i++) {
				if ("production".equals(gatewayEnvironments.get(i).getType())) {
					// This might have http,https
					gatewayURLs = gatewayEnvironments.get(i).getApiGatewayEndpoint();
					break;
				}
			}
		} else {
			gatewayURLs = gatewayEnvironments.get(0).getApiGatewayEndpoint();
		}

		return gatewayURLs;
	}

    /**
     *
     * Returns the HTTP URL of the App Gateway
     *
     * @return
     */
    public static String getGatewayHTTPURL(){

        List<Environment> gatewayEnvironments = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                                                    .getAPIManagerConfiguration()
                                                    .getApiGatewayEnvironments();

        // More than one gateway is not supported. So only deal with the first gateway.
        String gatewayURLs = gatewayEnvironments.get(0).getApiGatewayEndpoint();
        String httpGatewayURL = gatewayURLs.split(",")[0];

        return httpGatewayURL;
    }

    /**
     *
     * Returns the HTTPS URL of the App Gateway
     *
     * @return
     */
    public static String getGatewayHTTPSURL(){

        List<Environment> gatewayEnvironments = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration()
                .getApiGatewayEnvironments();

        // More than one gateway is not supported. So only deal with the first gateway.
        String gatewayURLs = gatewayEnvironments.get(0).getApiGatewayEndpoint();
        String httpsGatewayURL = gatewayURLs.split(",")[1];

        return httpsGatewayURL;
    }

	/**
	 * Gateway endpoint has HTTP and HTTPS endpoints.
	 * If both are defined pick HTTPS only. Else, pick whatever available.
	 * eg: <GatewayEndpoint>http://${carbon.local.ip}:${http.nio.port},
	 * https://${carbon.local.ip}:${https.nio.port}</GatewayEndpoint>
	 * 
	 * @param gatewayURLs
	 *            - String contains comma separated gateway urls.
	 * @return {@link String} - Returns HTTPS gateway endpoint
	 */

	private static String extractHTTPSEndpoint(String gatewayURLs) {
		String gatewayURL = null;
		String[] gatewayURLsArray = gatewayURLs.split(",");
		if (gatewayURLsArray.length > 1) {
			for (int j = 0; j < gatewayURLsArray.length; j++) {
				if (gatewayURLsArray[j].toString().startsWith("https")) {
					gatewayURL = gatewayURLsArray[j].toString();
					break;
				}
			}
		} else {
			gatewayURL = gatewayURLs;
		}
		return gatewayURL;
	}

	/**
	 * Create an Endpoint
	 * 
	 * @param endpointUrl
	 *            Endpoint url
	 * @param registry
	 *            Registry space to save the endpoint
	 * @return Path of the created resource
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             If an error occurs while adding the endpoint
	 */
	public static String createEndpoint(String endpointUrl, Registry registry)
	                                                                          throws
                                                                              AppManagementException {
		try {
			EndpointManager endpointManager = new EndpointManager(registry);
			Endpoint endpoint = endpointManager.newEndpoint(endpointUrl);
			endpointManager.addEndpoint(endpoint);
			return GovernanceUtils.getArtifactPath(registry, endpoint.getId());
		} catch (RegistryException e) {
			String msg = "Failed to import endpoint " + endpointUrl + " to registry ";
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		}
	}

	/**
	 * Returns a map of WebApp availability tiers as defined in the underlying
	 * governance
	 * registry.
	 * 
	 * @return a Map of tier names and Tier objects - possibly empty
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             if an error occurs when loading tiers from the registry
	 */
	public static Map<String, Tier> getTiers() throws AppManagementException {
		Map<String, Tier> tiers = new TreeMap<String, Tier>();
		try {
			Registry registry =
			                    ServiceReferenceHolder.getInstance().getRegistryService()
			                                          .getGovernanceSystemRegistry();
			if (registry.resourceExists(AppMConstants.API_TIER_LOCATION)) {
				Resource resource = registry.get(AppMConstants.API_TIER_LOCATION);
				String content = new String((byte[]) resource.getContent());
				OMElement element = AXIOMUtil.stringToOM(content);
				OMElement assertion = element.getFirstChildWithName(AppMConstants.ASSERTION_ELEMENT);
				Iterator policies = assertion.getChildrenWithName(AppMConstants.POLICY_ELEMENT);

				while (policies.hasNext()) {
					OMElement policy = (OMElement) policies.next();
					OMElement id = policy.getFirstChildWithName(AppMConstants.THROTTLE_ID_ELEMENT);
					String displayName = null;
					if (id.getAttribute(AppMConstants.THROTTLE_ID_DISPLAY_NAME_ELEMENT) != null) {
						displayName =
						              id.getAttributeValue(AppMConstants.THROTTLE_ID_DISPLAY_NAME_ELEMENT);
					}
					if (displayName == null) {
						displayName = id.getText();
					}
					Tier tier = new Tier(id.getText());
					tier.setPolicyContent(policy.toString().getBytes());
					tier.setDisplayName(displayName);
					// String desc =
					// resource.getProperty(AppMConstants.TIER_DESCRIPTION_PREFIX
					// + id.getText());
					String desc;
					try {
						desc = APIDescriptionGenUtil.generateDescriptionFromPolicy(policy);
					} catch (AppManagementException ex) {
						desc = AppMConstants.TIER_DESC_NOT_AVAILABLE;
					}
					Map<String, Object> tierAttributes =
					                                     APIDescriptionGenUtil.getTierAttributes(policy);
					if (tierAttributes != null && tierAttributes.size() != 0) {
						tier.setTierAttributes(APIDescriptionGenUtil.getTierAttributes(policy));
					}
					tier.setDescription(desc);
					if (!tier.getName().equalsIgnoreCase("Unauthenticated")) {
						tiers.put(tier.getName(), tier);
					}
				}
			}

			AppManagerConfiguration config =
			                                 ServiceReferenceHolder.getInstance()
			                                                       .getAPIManagerConfigurationService()
			                                                       .getAPIManagerConfiguration();
			if (Boolean.parseBoolean(config.getFirstProperty(AppMConstants.ENABLE_UNLIMITED_TIER))) {
				Tier tier = new Tier(AppMConstants.UNLIMITED_TIER);
				tier.setDescription(AppMConstants.UNLIMITED_TIER_DESC);
				tier.setDisplayName(AppMConstants.UNLIMITED_TIER);
				tiers.put(tier.getName(), tier);
			}
		} catch (RegistryException e) {
			String msg = "Error while retrieving WebApp tiers from registry";
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		} catch (XMLStreamException e) {
			String msg = "Malformed XML found in the WebApp tier policy resource";
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		}
		return tiers;
	}

	/**
	 * Returns a map of WebApp availability tiers of the tenant as defined in
	 * the underlying governance
	 * registry.
	 * 
	 * @return a Map of tier names and Tier objects - possibly empty
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             if an error occurs when loading tiers from the registry
	 */
	public static Map<String, Tier> getTiers(int tenantId) throws AppManagementException {
		Map<String, Tier> tiers = new TreeMap<String, Tier>();
		try {
			Registry registry =
			                    ServiceReferenceHolder.getInstance().getRegistryService()
			                                          .getGovernanceSystemRegistry(tenantId);
			if (registry.resourceExists(AppMConstants.API_TIER_LOCATION)) {
				Resource resource = registry.get(AppMConstants.API_TIER_LOCATION);
				String content = new String((byte[]) resource.getContent());
				OMElement element = AXIOMUtil.stringToOM(content);
				OMElement assertion = element.getFirstChildWithName(AppMConstants.ASSERTION_ELEMENT);
				Iterator policies = assertion.getChildrenWithName(AppMConstants.POLICY_ELEMENT);
				while (policies.hasNext()) {
					String desc;
					int tierMaxCount;
					OMElement policy = (OMElement) policies.next();
					OMElement id = policy.getFirstChildWithName(AppMConstants.THROTTLE_ID_ELEMENT);
					String displayName = null;
					if (id.getAttribute(AppMConstants.THROTTLE_ID_DISPLAY_NAME_ELEMENT) != null) {
						displayName =
						              id.getAttributeValue(AppMConstants.THROTTLE_ID_DISPLAY_NAME_ELEMENT);
					}
					if (displayName == null) {
						displayName = id.getText();
					}
					Tier tier = new Tier(id.getText());
					tier.setPolicyContent(policy.toString().getBytes());
					tier.setDisplayName(displayName);

					try {
						desc = APIDescriptionGenUtil.generateDescriptionFromPolicy(policy);
					} catch (AppManagementException ex) {
						desc = AppMConstants.TIER_DESC_NOT_AVAILABLE;
					}
					try {
						tierMaxCount = APIDescriptionGenUtil.generateMaxCountFromPolicy(policy);
					} catch (AppManagementException ex) {
						tierMaxCount = AppMConstants.TIER_MAX_COUNT;
					}

					Map<String, Object> tierAttributes =
					                                     APIDescriptionGenUtil.getTierAttributes(policy);
					if (tierAttributes != null && tierAttributes.size() != 0) {
						tier.setTierAttributes(APIDescriptionGenUtil.getTierAttributes(policy));
					}
					tier.setDescription(desc);
					tier.setRequestPerMinute(tierMaxCount);
					if (!tier.getName().equalsIgnoreCase("Unauthenticated")) {
						tiers.put(tier.getName(), tier);
					}
				}
			}

			AppManagerConfiguration config =
			                                 ServiceReferenceHolder.getInstance()
			                                                       .getAPIManagerConfigurationService()
			                                                       .getAPIManagerConfiguration();
			if (Boolean.parseBoolean(config.getFirstProperty(AppMConstants.ENABLE_UNLIMITED_TIER))) {
				Tier tier = new Tier(AppMConstants.UNLIMITED_TIER);
				tier.setDescription(AppMConstants.UNLIMITED_TIER_DESC);
				tier.setDisplayName(AppMConstants.UNLIMITED_TIER);
				tier.setRequestPerMinute(AppMConstants.UNLIMITED_TIER_REQUEST_PER_MINUTE);
				tiers.put(tier.getName(), tier);
			}
		} catch (RegistryException e) {
			String msg = "Error while retrieving WebApp tiers from registry";
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		} catch (XMLStreamException e) {
			String msg = "Malformed XML found in the WebApp tier policy resource";
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		}
		return tiers;
	}

	/**
	 * Returns the tier display name for a particular tier
	 * 
	 * @return the relevant tier display name
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             if an error occurs when loading tiers from the registry
	 */
	public static String getTierDisplayName(int tenantId, String tierName)
	                                                                      throws
                                                                          AppManagementException {
		String displayName = null;
		try {
			Registry registry =
			                    ServiceReferenceHolder.getInstance().getRegistryService()
			                                          .getGovernanceSystemRegistry(tenantId);
			if (registry.resourceExists(AppMConstants.API_TIER_LOCATION)) {
				Resource resource = registry.get(AppMConstants.API_TIER_LOCATION);
				String content = new String((byte[]) resource.getContent());
				OMElement element = AXIOMUtil.stringToOM(content);
				OMElement assertion = element.getFirstChildWithName(AppMConstants.ASSERTION_ELEMENT);
				Iterator policies = assertion.getChildrenWithName(AppMConstants.POLICY_ELEMENT);

				while (policies.hasNext()) {
					OMElement policy = (OMElement) policies.next();
					OMElement id = policy.getFirstChildWithName(AppMConstants.THROTTLE_ID_ELEMENT);
					if (id.getText().equals(tierName)) {
						if (id.getAttribute(AppMConstants.THROTTLE_ID_DISPLAY_NAME_ELEMENT) != null) {
							displayName =
							              id.getAttributeValue(AppMConstants.THROTTLE_ID_DISPLAY_NAME_ELEMENT);
						} else if (displayName == null) {
							displayName = id.getText();
						}
					} else if (AppMConstants.UNLIMITED_TIER.equals(tierName)) {
						displayName = AppMConstants.UNLIMITED_TIER;
					}
				}

			}
		} catch (RegistryException e) {
			String msg = "Error while retrieving WebApp tiers from registry";
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		} catch (XMLStreamException e) {
			String msg = "Malformed XML found in the WebApp tier policy resource";
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		}
		return displayName;
	}

    /**
     * This is written as a wrapper to for the checkPermission Method
     * returns a boolean indicating if user is authorized or not
     * @param username
     * @param permission
     * @return
     */
    public static boolean checkPermissionWrapper(String username, String permission) {
        boolean result;
        try {
            checkPermission(username, permission);
            result = true;
        } catch (AppManagementException e) {
            //We catch this exception and return a false
            result = false;
        }
        return result;
    }

	/**
	 * Checks whether the specified user has the specified permission.
	 * 
	 * @param username
	 *            A username
	 * @param permission
	 *            A valid Carbon permission
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             If the user does not have the specified permission or if an
	 *             error occurs
	 */
	public static void checkPermission(String username, String permission)
	                                                                      throws
                                                                          AppManagementException {
		if (username == null) {
			throw new AppManagementException("Attempt to execute privileged operation as"
			                                 + " the anonymous user");
		}
		String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(username));
		PrivilegedCarbonContext.startTenantFlow();
		PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
		boolean authorized;
		try {
			if (!tenantDomain.equals(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
				int tenantId =
				               ServiceReferenceHolder.getInstance().getRealmService()
				                                     .getTenantManager().getTenantId(tenantDomain);
				AuthorizationManager manager =
				                               ServiceReferenceHolder.getInstance()
				                                                     .getRealmService()
				                                                     .getTenantUserRealm(tenantId)
				                                                     .getAuthorizationManager();
				authorized =
				             manager.isUserAuthorized(MultitenantUtils.getTenantAwareUsername(AppManagerUtil.replaceEmailDomainBack(username)),
				                                      permission,
				                                      CarbonConstants.UI_PERMISSION_ACTION);
			} else {
                //Get rid of carbon.super in user name
                username = MultitenantUtils.getTenantAwareUsername(username);
                //email login
                username = AppManagerUtil.replaceEmailDomainBack(username);
				RemoteAuthorizationManager authorizationManager =
				                                                  RemoteAuthorizationManager.getInstance();
				authorized = authorizationManager.isUserAuthorized(username, permission);
			}
			if (!authorized) {
				throw new AppManagementException("User '" + username + "' does not have the " +
				                                 "required permission: " + permission);
			}
		} catch (UserStoreException e) {
			throw new AppManagementException("Error while checking the user:" + username +
			                                 " authorized or not", e);
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}

	/**
	 * Checks whether the specified user has the specified permission without
	 * throwing
	 * any exceptions.
	 * 
	 * @param username
	 *            A username
	 * @param permission
	 *            A valid Carbon permission
	 * @return true if the user has the specified permission and false otherwise
	 */
	public static boolean checkPermissionQuietly(String username, String permission) {
		try {
			checkPermission(username, permission);
			return true;
		} catch (AppManagementException e) {
			return false;
		}
	}

	/**
	 * Gets the information of the logged in User.
	 * 
	 * @param cookie
	 *            Cookie of the previously logged in session.
	 * @param serviceUrl
	 *            Url of the authentication service.
	 * @return LoggedUserInfo object containing details of the logged in user.
	 */
	public static LoggedUserInfo getLoggedInUserInfo(String cookie, String serviceUrl)
	                                                                                  throws RemoteException,
	                                                                                  ExceptionException {
		LoggedUserInfoAdminStub stub =
		                               new LoggedUserInfoAdminStub(null, serviceUrl +
		                                                                 "LoggedUserInfoAdmin");
		ServiceClient client = stub._getServiceClient();
		Options options = client.getOptions();
		options.setManageSession(true);
		options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
		LoggedUserInfo userInfo = stub.getUserInfo();
		return userInfo;
	}

	/**
	 * Retrieves the role list of a user
	 * 
	 * @param username
	 *            A username
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             If an error occurs
	 */
	public static String[] getListOfRoles(String username) throws AppManagementException {
		if (username == null) {
			throw new AppManagementException("Attempt to execute privileged operation as"
			                                 + " the anonymous user");
		}

		RemoteAuthorizationManager authorizationManager = RemoteAuthorizationManager.getInstance();
		return authorizationManager.getRolesOfUser(username);
	}

	/**
	 * Retrieves the list of user roles without throwing any exceptions.
	 * 
	 * @param username
	 *            A username
	 * @return the list of roles to which the user belongs to.
	 */
	public static String[] getListOfRolesQuietly(String username) {
		try {
			return getListOfRoles(username);
		} catch (AppManagementException e) {
			return new String[0];
		}
	}

	/**
	 * Sets permission for uploaded file resource.
	 * 
	 * @param filePath
	 *            Registry path for the uploaded file
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 */

	public static void setFilePermission(String filePath) throws AppManagementException {
		try {
			filePath = filePath.replaceFirst("/registry/resource/", "");
			AuthorizationManager accessControlAdmin =
			                                          ServiceReferenceHolder.getInstance()
			                                                                .getRealmService()
			                                                                .getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID)
			                                                                .getAuthorizationManager();
			if (!accessControlAdmin.isRoleAuthorized(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
			                                         filePath, ActionConstants.GET)) {
				accessControlAdmin.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
				                                 filePath, ActionConstants.GET);
			}
		} catch (UserStoreException e) {
			throw new AppManagementException(
			                                 "Error while setting up permissions for file location",
			                                 e);
		}
	}

	/**
	 * This method used to get WebApp from governance artifact specific to
	 * copyAPI
	 * 
	 * @param artifact
	 *            WebApp artifact
	 * @param registry
	 *            Registry
	 * @return WebApp
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             if failed to get WebApp from artifact
	 */
	public static WebApp getAPI(GovernanceArtifact artifact, Registry registry, APIIdentifier oldId)
	                                                                                                throws
                                                                                                    AppManagementException {

		WebApp api;
		try {
			String providerName = artifact.getAttribute(AppMConstants.API_OVERVIEW_PROVIDER);
			String apiName = artifact.getAttribute(AppMConstants.API_OVERVIEW_NAME);
			String apiVersion = artifact.getAttribute(AppMConstants.API_OVERVIEW_VERSION);
			api = new WebApp(new APIIdentifier(providerName, apiName, apiVersion));
			// set rating
	String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
			BigDecimal bigDecimal = new BigDecimal(registry.getAverageRating(artifactPath));
			BigDecimal res = bigDecimal.setScale(1, RoundingMode.HALF_UP);
			api.setRating(res.floatValue());
			// set description
			api.setDescription(artifact.getAttribute(AppMConstants.API_OVERVIEW_DESCRIPTION));
			// set last access time
			api.setLastUpdated(registry.get(artifactPath).getLastModified());
			// set url
			api.setUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_URL));
            api.setLogoutURL(artifact.getAttribute(AppMConstants.API_OVERVIEW_LOGOUT_URL));
			api.setSandboxUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_SANDBOX_URL));
			api.setStatus(getApiStatus(artifact.getAttribute(AppMConstants.API_OVERVIEW_STATUS)));
			api.setThumbnailUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_THUMBNAIL_URL));
			api.setWsdlUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_WSDL));
			api.setWadlUrl(artifact.getAttribute(AppMConstants.API_OVERVIEW_WADL));
			api.setTechnicalOwner(artifact.getAttribute(AppMConstants.API_OVERVIEW_TEC_OWNER));
			api.setTechnicalOwnerEmail(artifact.getAttribute(AppMConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
			api.setBusinessOwner(artifact.getAttribute(AppMConstants.API_OVERVIEW_BUSS_OWNER));
			api.setBusinessOwnerEmail(artifact.getAttribute(AppMConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));
			api.setEndpointSecured(Boolean.parseBoolean(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_SECURED)));
			api.setEndpointUTUsername(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_USERNAME));
			api.setEndpointUTPassword(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_PASSWORD));
			api.setTransports(artifact.getAttribute(AppMConstants.API_OVERVIEW_TRANSPORTS));

			api.setEndpointConfig(artifact.getAttribute(AppMConstants.API_OVERVIEW_ENDPOINT_CONFIG));

			api.setRedirectURL(artifact.getAttribute(AppMConstants.API_OVERVIEW_REDIRECT_URL));
            api.setAppOwner(artifact.getAttribute(AppMConstants.API_OVERVIEW_OWNER));
			api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(AppMConstants.API_OVERVIEW_ADVERTISE_ONLY)));

			api.setSubscriptionAvailability(artifact.getAttribute(AppMConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
			api.setSubscriptionAvailableTenants(artifact.getAttribute(AppMConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

			api.setResponseCache(artifact.getAttribute(AppMConstants.API_OVERVIEW_RESPONSE_CACHING));

            api.setSsoEnabled(artifact.getAttribute("sso_enableSso"));
            api.setUUID(artifact.getId());
            api.setThumbnailUrl(artifact.getAttribute(AppMConstants.APP_IMAGES_THUMBNAIL));

			int cacheTimeout = AppMConstants.API_RESPONSE_CACHE_TIMEOUT;
			try {
				cacheTimeout =
				               Integer.parseInt(artifact.getAttribute(AppMConstants.API_OVERVIEW_CACHE_TIMEOUT));
			} catch (NumberFormatException e) {
				// ignore
			}

			String tenantDomainName =
			                          MultitenantUtils.getTenantDomain(replaceEmailDomainBack(providerName));
			int tenantId =
			               ServiceReferenceHolder.getInstance().getRealmService()
			                                     .getTenantManager().getTenantId(tenantDomainName);

			Set<Tier> availableTier = new HashSet<Tier>();
			String tiers = artifact.getAttribute(AppMConstants.API_OVERVIEW_TIER);
			Map<String, Tier> definedTiers = getTiers(tenantId);
			if (tiers != null && !"".equals(tiers)) {
				String[] tierNames = tiers.split("\\|\\|");
				for (String tierName : tierNames) {
					Tier definedTier = definedTiers.get(tierName);
					if (definedTier != null) {
						availableTier.add(definedTier);
					} else {
						log.warn("Unknown tier: " + tierName + " found on WebApp: " + apiName);
					}
				}
			}
			api.addAvailableTiers(availableTier);
			api.setContext(artifact.getAttribute(AppMConstants.API_OVERVIEW_CONTEXT));
			api.setLatest(Boolean.valueOf(artifact.getAttribute(AppMConstants.API_OVERVIEW_IS_LATEST)));
			ArrayList<URITemplate> urlPatternsList;

			urlPatternsList = AppMDAO.getAllURITemplates(api.getContext(), oldId.getVersion());
			Set<URITemplate> uriTemplates = new HashSet<URITemplate>(urlPatternsList);

			for (URITemplate uriTemplate : uriTemplates) {
				uriTemplate.setResourceURI(api.getUrl());
				uriTemplate.setResourceSandboxURI(api.getSandboxUrl());

			}
			api.setUriTemplates(uriTemplates);

			Set<String> tags = new HashSet<String>();
			org.wso2.carbon.registry.core.Tag[] tag = registry.getTags(artifactPath);
			for (Tag tag1 : tag) {
				tags.add(tag1.getTagName());
			}
			api.addTags(tags);
			api.setLastUpdated(registry.get(artifactPath).getLastModified());

            //Set Lifecycle status
            if (artifact.getLifecycleState() != null && artifact.getLifecycleState() != "") {
                if (artifact.getLifecycleState().toUpperCase().equalsIgnoreCase(APIStatus.INREVIEW.getStatus())) {
                    api.setLifeCycleStatus(APIStatus.INREVIEW);
                } else {
                    api.setLifeCycleStatus(APIStatus.valueOf(artifact.getLifecycleState().toUpperCase()));
                }
            }
            api.setLifeCycleName(artifact.getLifecycleName());

		} catch (GovernanceException e) {
			String msg = "Failed to get WebApp fro artifact ";
			throw new AppManagementException(msg, e);
		} catch (RegistryException e) {
			String msg = "Failed to get LastAccess time or Rating";
			throw new AppManagementException(msg, e);
		} catch (UserStoreException e) {
			String msg = "Failed to get User Realm of WebApp Provider";
			throw new AppManagementException(msg, e);
		}
		return api;
	}

	public static boolean checkAccessTokenPartitioningEnabled() {
		return OAuthServerConfiguration.getInstance().isAccessTokenPartitioningEnabled();
	}

	public static boolean checkUserNameAssertionEnabled() {
		return OAuthServerConfiguration.getInstance().isUserNameAssertionEnabled();
	}

	public static String[] getAvailableKeyStoreTables() throws AppManagementException {
		String[] keyStoreTables = new String[0];
		Map<String, String> domainMappings = getAvailableUserStoreDomainMappings();
		if (domainMappings != null) {
			keyStoreTables = new String[domainMappings.size()];
			int i = 0;
			for (Map.Entry<String, String> e : domainMappings.entrySet()) {
				String value = e.getValue();
				keyStoreTables[i] = AppMConstants.ACCESS_TOKEN_STORE_TABLE + "_" + value.trim();
				i++;
			}
		}
		return keyStoreTables;
	}

	public static Map<String, String> getAvailableUserStoreDomainMappings()
	                                                                       throws
                                                                           AppManagementException {
		Map<String, String> userStoreDomainMap = new HashMap<String, String>();
		String domainsStr =
		                    OAuthServerConfiguration.getInstance()
		                                            .getAccessTokenPartitioningDomains();
		if (domainsStr != null) {
			String[] userStoreDomainsArr = domainsStr.split(",");
			for (String anUserStoreDomainsArr : userStoreDomainsArr) {
				String[] mapping = anUserStoreDomainsArr.trim().split(":"); // A:foo.com
				                                                            // ,
				                                                            // B:bar.com
				if (mapping.length < 2) {
					throw new AppManagementException("Domain mapping has not defined");
				}
				userStoreDomainMap.put(mapping[1].trim(), mapping[0].trim()); // key=domain
				                                                              // &
				                                                              // value=mapping
			}
		}
		return userStoreDomainMap;
	}

	public static String getAccessTokenStoreTableFromUserId(String userId)
	                                                                      throws
                                                                          AppManagementException {
		String accessTokenStoreTable = AppMConstants.ACCESS_TOKEN_STORE_TABLE;
		String userStore;
		if (userId != null) {
			String[] strArr = userId.split("/");
			if (strArr != null && strArr.length > 1) {
				userStore = strArr[0];
				Map<String, String> availableDomainMappings = getAvailableUserStoreDomainMappings();
				if (availableDomainMappings != null &&
				    availableDomainMappings.containsKey(userStore)) {
					accessTokenStoreTable =
					                        accessTokenStoreTable + "_" +
					                                availableDomainMappings.get(userStore);
				}
			}
		}
		return accessTokenStoreTable;
	}

	public static String getAccessTokenStoreTableFromAccessToken(String apiKey)
	                                                                           throws
                                                                               AppManagementException {
		String userId = getUserIdFromAccessToken(apiKey); // i.e:
		                                                  // 'foo.com/admin' or
		                                                  // 'admin'
		return getAccessTokenStoreTableFromUserId(userId);
	}

	public static String getUserIdFromAccessToken(String apiKey) {
		String userId = null;
		String decodedKey = new String(Base64.decodeBase64(apiKey.getBytes()));
		String[] tmpArr = decodedKey.split(":");
		if (tmpArr != null && tmpArr.length == 2) { // tmpArr[0]=
			                                        // userStoreDomain &
			                                        // tmpArr[1] = userId
			userId = tmpArr[1];
		}
		return userId;
	}

	/**
	 * When an input is having '@',replace it with '-AT-' [This is required to
	 * persist WebApp data in registry,as registry paths don't allow '@' sign.]
	 * 
	 * @param input
	 *            inputString
	 * @return String modifiedString
	 */
	public static String replaceEmailDomain(String input) {
		if (input != null && input.contains(AppMConstants.EMAIL_DOMAIN_SEPARATOR)) {
			input =
			        input.replace(AppMConstants.EMAIL_DOMAIN_SEPARATOR,
			                      AppMConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT);
		}
		return input;
	}

	/**
	 * When an input is having '-AT-',replace it with @ [This is required to
	 * persist WebApp data between registry and database]
	 * 
	 * @param input
	 *            inputString
	 * @return String modifiedString
	 */
	public static String replaceEmailDomainBack(String input) {
		if (input != null){
        if (input.contains(AppMConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT)) {
            input =
                    input.replace(AppMConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT,
                                  AppMConstants.EMAIL_DOMAIN_SEPARATOR);
        }else if (input.contains(AppMConstants.SECONDERY_USER_STORE_SEPERATOR)){
            input =
                    input.replace(AppMConstants.SECONDERY_USER_STORE_SEPERATOR,
                                  AppMConstants.SECONDERY_USER_STORE_DEFAULT_SEPERATOR);
        }
		}
		return input;
	}

	public static void copyResourcePermissions(String username, String sourceArtifactPath,
	                                           String targetArtifactPath)
	                                                                     throws
                                                                         AppManagementException {
		String sourceResourcePath =
		                            RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
		                                                          RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
		                                                                  sourceArtifactPath);

		String targetResourcePath =
		                            RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
		                                                          RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
		                                                                  targetArtifactPath);

		String tenantDomain =
		                      MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(username));

		try {
			int tenantId =
			               ServiceReferenceHolder.getInstance().getRealmService()
			                                     .getTenantManager().getTenantId(tenantDomain);
			AuthorizationManager authManager =
			                                   ServiceReferenceHolder.getInstance()
			                                                         .getRealmService()
			                                                         .getTenantUserRealm(tenantId)
			                                                         .getAuthorizationManager();
			String[] allowedRoles =
			                        authManager.getAllowedRolesForResource(sourceResourcePath,
			                                                               ActionConstants.GET);

			if (allowedRoles != null) {

				for (String allowedRole : allowedRoles) {
					authManager.authorizeRole(allowedRole, targetResourcePath, ActionConstants.GET);
				}
			}

		} catch (UserStoreException e) {
			throw new AppManagementException("Error while adding role permissions to WebApp", e);
		}
	}

	/**
	 * This function is to set resource permissions based on its visibility
	 * 
	 * @param visibility
	 *            WebApp visibility
	 * @param roles
	 *            Authorized roles
	 * @param artifactPath
	 *            WebApp resource path
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             Throwing exception
	 */
	public static void setResourcePermissions(String username, String visibility, String[] roles,
	                                          String artifactPath) throws AppManagementException {
		try {
			String resourcePath =
			                      RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
										  RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
												  artifactPath);

			String tenantDomain =
			                      MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(username));
			if (!tenantDomain.equals(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
				int tenantId =
				               ServiceReferenceHolder.getInstance().getRealmService()
				                                     .getTenantManager().getTenantId(tenantDomain);
				AuthorizationManager authManager =
				                                   ServiceReferenceHolder.getInstance()
				                                                         .getRealmService()
				                                                         .getTenantUserRealm(tenantId)
				                                                         .getAuthorizationManager();
                authManager.clearResourceAuthorizations(resourcePath);

				if (visibility != null &&
				    visibility.equalsIgnoreCase(AppMConstants.API_RESTRICTED_VISIBILITY)) {
					boolean isRoleEveryOne = false;
					/* If no roles have defined, authorize for everyone role */
					if (roles != null && roles.length == 1 && roles[0].equals("")) {
						authManager.authorizeRole(AppMConstants.EVERYONE_ROLE, resourcePath,
						                          ActionConstants.GET);
						isRoleEveryOne = true;
					} else {
						for (String role : roles) {
							if (role.equalsIgnoreCase(AppMConstants.EVERYONE_ROLE)) {
								isRoleEveryOne = true;
							}
							authManager.authorizeRole(role, resourcePath, ActionConstants.GET);

						}
					}
					if (!isRoleEveryOne) {
						authManager.denyRole(AppMConstants.EVERYONE_ROLE, resourcePath,
						                     ActionConstants.GET);
					}
					authManager.denyRole(AppMConstants.ANONYMOUS_ROLE, resourcePath,
					                     ActionConstants.GET);
				} else {
					authManager.authorizeRole(AppMConstants.EVERYONE_ROLE, resourcePath,
					                          ActionConstants.GET);
					authManager.authorizeRole(AppMConstants.ANONYMOUS_ROLE, resourcePath,
					                          ActionConstants.GET);
				}
			} else {
				RegistryAuthorizationManager authorizationManager =
				                                                    new RegistryAuthorizationManager(
				                                                                                     ServiceReferenceHolder.getUserRealm());
                authorizationManager.clearResourceAuthorizations(resourcePath);

				if (visibility != null &&
				    visibility.equalsIgnoreCase(AppMConstants.API_RESTRICTED_VISIBILITY)) {
					boolean isRoleEveryOne = false;
					for (String role : roles) {
						if (role.equalsIgnoreCase(AppMConstants.EVERYONE_ROLE)) {
							isRoleEveryOne = true;
						}
						authorizationManager.authorizeRole(role, resourcePath, ActionConstants.GET);

					}
					if (!isRoleEveryOne) {
						authorizationManager.denyRole(AppMConstants.EVERYONE_ROLE, resourcePath,
						                              ActionConstants.GET);
					}
					authorizationManager.denyRole(AppMConstants.ANONYMOUS_ROLE, resourcePath,
					                              ActionConstants.GET);

				} else {
					authorizationManager.authorizeRole(AppMConstants.EVERYONE_ROLE, resourcePath,
					                                   ActionConstants.GET);
					authorizationManager.authorizeRole(AppMConstants.ANONYMOUS_ROLE, resourcePath,
					                                   ActionConstants.GET);
				}
			}

		} catch (UserStoreException e) {
			throw new AppManagementException("Error while adding role permissions to WebApp", e);
		}
	}

	/**
	 * Load the throttling policy to the registry for tenants
	 * 
	 * @param tenant
	 * @param tenantID
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 */

	public static void loadTenantAPIPolicy(String tenant, int tenantID)
	                                                                   throws
                                                                       AppManagementException {
		try {
			RegistryService registryService =
			                                  ServiceReferenceHolder.getInstance()
			                                                        .getRegistryService();
			// UserRegistry govRegistry =
			// registryService.getGovernanceUserRegistry(tenant, tenantID);
			UserRegistry govRegistry = registryService.getGovernanceSystemRegistry(tenantID);

			if (govRegistry.resourceExists(AppMConstants.API_TIER_LOCATION)) {
				if (log.isDebugEnabled()) {
					log.debug("Tier policies already uploaded to the tenant's registry space");
				}
				return;
			}
			if (log.isDebugEnabled()) {
				log.debug("Adding WebApp tier policies to the tenant's registry");
			}
			InputStream inputStream =
			                          AppManagerComponent.class.getResourceAsStream("/tiers/default-tiers.xml");
			byte[] data = IOUtils.toByteArray(inputStream);
			Resource resource = govRegistry.newResource();
			resource.setContent(data);
			govRegistry.put(AppMConstants.API_TIER_LOCATION, resource);

		} catch (RegistryException e) {
			throw new AppManagementException(
			                                 "Error while saving policy information to the registry",
			                                 e);
		} catch (IOException e) {
			throw new AppManagementException("Error while reading policy file content", e);
		}
	}

	/**
	 * Load tenant sign-up configuration into registry
	 *
	 * @param tenantId tenant is
	 * @throws AppManagementException
	 */
	public static void loadTenantSelfSignUpConfigurations(int tenantId)
			throws AppManagementException {
		try {
			RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
			UserRegistry govRegistry = registryService.getGovernanceSystemRegistry(tenantId);

			if (govRegistry.resourceExists(AppMConstants.SELF_SIGN_UP_CONFIG_LOCATION)) {
				log.debug("Self signup configuration already uploaded to the registry");
				return;
			}
			if (log.isDebugEnabled()) {
				log.debug("Adding Self signup configuration to the tenant's registry");
			}
			InputStream inputStream;
			if (tenantId == org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID) {
				inputStream = AppManagerComponent.class.
						getResourceAsStream("/signupconfigurations/default-sign-up-config.xml");
			} else {
				inputStream = AppManagerComponent.class.
						getResourceAsStream("/signupconfigurations/tenant-sign-up-config.xml");
			}
			byte[] data = IOUtils.toByteArray(inputStream);
			Resource resource = govRegistry.newResource();
			resource.setContent(data);
			resource.setMediaType(AppMConstants.SELF_SIGN_UP_CONFIG_MEDIA_TYPE);
			govRegistry.put(AppMConstants.SELF_SIGN_UP_CONFIG_LOCATION, resource);

		} catch (RegistryException e) {
			throw new AppManagementException("Error while saving Self signup configuration information to the registry",
					e);
		} catch (IOException e) {
			throw new AppManagementException("Error while reading Self signup configuration file content", e);
		}
	}


	/**
	 * Create self signup role under given tenant domain
	 *
	 * @param tenantId
	 * @throws AppManagementException
	 */
	public static void createSelfSignUpRoles(int tenantId) throws AppManagementException {

		UserRegistrationConfigDTO userRegistrationConfigDTO = SelfSignUpUtil.getSignupConfiguration(tenantId);

		String selfSignUpDomain = userRegistrationConfigDTO.getSignUpDomain();
		for (SignUpRole signUpRole : userRegistrationConfigDTO.getSignUpRoles()) {
			String roleName = signUpRole.getRoleName();
			boolean isExternalRole = signUpRole.isExternalRole();
			if (roleName != null) {
				// If isExternalRole==false ;create the subscriber role as an internal role
				if (isExternalRole && selfSignUpDomain != null) {
					roleName = selfSignUpDomain.toUpperCase() + CarbonConstants.DOMAIN_SEPARATOR + roleName;
				} else {
					roleName = UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR + roleName;
				}
				String[] permissions = signUpRole.getPermissionsList();
				Permission[] subscriberPermissions = new Permission[permissions.length];
				for (int i = 0; i < permissions.length; i++) {
					subscriberPermissions[i] = new Permission(permissions[i], UserMgtConstants.EXECUTE_ACTION);

				}
				createSubscriberRole(roleName, tenantId, subscriberPermissions);
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Adding Self signup configuration to the tenant's registry");
		}
	}

	/**
	 * Create Subscriber user role
	 *
	 * @param roleName
	 * @param tenantId
	 * @throws AppManagementException
	 */
	public static void createSubscriberRole(String roleName, int tenantId, Permission[] subscriberPermissions)
			throws AppManagementException {

		String[] permissions = new String[]{
				"/permission/admin/login",
				AppMConstants.Permissions.WEB_APP_SUBSCRIBE
		};
		try {
			RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
			UserRealm realm;
			org.wso2.carbon.user.api.UserRealm tenantRealm;
			UserStoreManager manager;

			if (tenantId < 0) {
				realm = realmService.getBootstrapRealm();
				manager = realm.getUserStoreManager();
			} else {
				tenantRealm = realmService.getTenantUserRealm(tenantId);
				manager = tenantRealm.getUserStoreManager();
			}
			if (!manager.isExistingRole(roleName)) {
				if (log.isDebugEnabled()) {
					log.debug("Creating subscriber role: " + roleName);
				}
				String tenantAdminName = ServiceReferenceHolder.getInstance().
						getRealmService().getTenantUserRealm(tenantId).getRealmConfiguration().getAdminUserName();
				String[] userList = new String[]{tenantAdminName};
				manager.addRole(roleName, userList, subscriberPermissions);
			}
		} catch (UserStoreException e) {
			throw new AppManagementException("Error while creating subscriber role : " + roleName +
					" for tenant id : " + tenantId, e);
		}
	}

	/**
     * Load Workflow Configurations
     * @param tenantID
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static void loadTenantWorkFlowExtensions(int tenantID)
            throws AppManagementException {
        // TODO: Merge different resource loading methods and create a single method.
        try {
            RegistryService registryService =
                    ServiceReferenceHolder.getInstance()
                            .getRegistryService();
            //UserRegistry govRegistry = registryService.getGovernanceUserRegistry(tenant, tenantID);
            UserRegistry govRegistry = registryService.getGovernanceSystemRegistry(tenantID);

            if (govRegistry.resourceExists(AppMConstants.WORKFLOW_EXECUTOR_LOCATION)) {
                log.debug("External Stores configuration already uploaded to the registry");
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Adding External Stores configuration to the tenant's registry");
            }
            InputStream inputStream =
                    AppManagerComponent.class.getResourceAsStream("/workflowextensions/default-workflow-extensions.xml");
            byte[] data = IOUtils.toByteArray(inputStream);
            Resource resource = govRegistry.newResource();
            resource.setContent(data);
            resource.setMediaType(AppMConstants.WORKFLOW_MEDIA_TYPE);
            govRegistry.put(AppMConstants.WORKFLOW_EXECUTOR_LOCATION, resource);

        } catch (RegistryException e) {
            throw new AppManagementException("Error while saving External Stores configuration information to the registry", e);
        } catch (IOException e) {
            throw new AppManagementException("Error while reading External Stores configuration file content", e);
        }
    }

    /**
     * Add permissions to the appmgt/applicationdata collection for given role.
     *
     * @param roleName
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static void addNewRole(String roleName, Permission[] permissions,
                                             org.wso2.carbon.user.api.UserRealm userRealm)
            throws AppManagementException {
        // TODO: Merge different resource loading methods and create a single method.
        try {
            String tenantAdminName = userRealm.getRealmConfiguration().getAdminUserName();
            String[] userList = new String[]{tenantAdminName};
            String[] existingRoles = userRealm.getUserStoreManager().getRoleNames();
            boolean roleExists = false;

            for(String role : existingRoles){
                if(role.equalsIgnoreCase(roleName)){
                    roleExists = true;
                    break;
                }
            }

            if(!roleExists) {
                userRealm.getUserStoreManager().addRole(roleName, userList, permissions);
            }

        } catch (UserStoreException e) {
            throw new AppManagementException("Error while adding new role : " + roleName, e);
        }
    }


    /**
     * Add permissions to the appmgt/applicationdata collection for given role.
     * @param roleName
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static void applyRolePermissionToCollection(String roleName, org.wso2.carbon.user.api.UserRealm userRealm)
            throws AppManagementException {
        // TODO: Merge different resource loading methods and create a single method.
        try {
            userRealm.getAuthorizationManager().authorizeRole(roleName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH
                                                                        + AppMConstants.APPMGT_APPLICATION_DATA_LOCATION, "authorize");
            userRealm.getAuthorizationManager().authorizeRole(roleName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH
                                                                        + AppMConstants.APPMGT_APPLICATION_DATA_LOCATION, ActionConstants.PUT);
            userRealm.getAuthorizationManager().authorizeRole(roleName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH
                                                                        + AppMConstants.APPMGT_APPLICATION_DATA_LOCATION, ActionConstants.DELETE);
            userRealm.getAuthorizationManager().authorizeRole(roleName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH
                                                                        + AppMConstants.APPMGT_APPLICATION_DATA_LOCATION, ActionConstants.GET);
        } catch (UserStoreException e) {
            throw new AppManagementException("Error while adding permissions for appmgt/applicationdata collection for role "+roleName, e);
        }
    }

	public static void writeDefinedSequencesToTenantRegistry(int tenantID)
	                                                                      throws
                                                                          AppManagementException {
		try {
			RegistryService registryService =
			                                  ServiceReferenceHolder.getInstance()
			                                                        .getRegistryService();
			UserRegistry govRegistry = registryService.getGovernanceSystemRegistry(tenantID);

			if (govRegistry.resourceExists(AppMConstants.API_CUSTOM_INSEQUENCE_LOCATION)) {
				if (log.isDebugEnabled()) {
					log.debug("Defined sequences have already been added to the tenant's registry");
				}
				return;
			}

			if (log.isDebugEnabled()) {
				log.debug("Adding defined sequences to the tenant's registry.");
			}

			InputStream inSeqStream =
			                          AppManagerComponent.class.getResourceAsStream("/definedsequences/in/log_in_message.xml");
			byte[] inSeqData = IOUtils.toByteArray(inSeqStream);
			Resource inSeqResource = govRegistry.newResource();
			inSeqResource.setContent(inSeqData);

			govRegistry.put(AppMConstants.API_CUSTOM_INSEQUENCE_LOCATION + "log_in_message.xml",
			                inSeqResource);

			InputStream outSeqStream =
			                           AppManagerComponent.class.getResourceAsStream("/definedsequences/out/log_out_message.xml");
			byte[] outSeqData = IOUtils.toByteArray(outSeqStream);
			Resource outSeqResource = govRegistry.newResource();
			outSeqResource.setContent(outSeqData);

			govRegistry.put(AppMConstants.API_CUSTOM_OUTSEQUENCE_LOCATION + "log_out_message.xml",
			                outSeqResource);

		} catch (RegistryException e) {
			throw new AppManagementException(
			                                 "Error while saving defined sequences to the tenant's registry ",
			                                 e);
		} catch (IOException e) {
			throw new AppManagementException("Error while reading defined sequence ", e);
		}
	}

	/**
	 * Load the WebApp RXT to the registry for tenants
	 * 
	 * @param tenant
	 * @param tenantID
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 */

	public static void loadloadTenantAPIRXT(String tenant, int tenantID)
	                                                                    throws
                                                                        AppManagementException {
		RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
		UserRegistry registry = null;
		try {
			// registry = registryService.getRegistry(tenant, tenantID);
			registry = registryService.getGovernanceSystemRegistry(tenantID);
		} catch (RegistryException e) {
			throw new AppManagementException("Error when create registry instance ", e);
		}

		String rxtDir =
		                CarbonUtils.getCarbonHome() + File.separator + "repository" +
		                        File.separator + "resources" + File.separator + "rxts";
		File file = new File(rxtDir);
		FilenameFilter filenameFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				// if the file extension is .rxt return true, else false
				return name.endsWith(".rxt");
			}
		};
		String[] rxtFilePaths = file.list(filenameFilter);
		for (String rxtPath : rxtFilePaths) {
			String resourcePath =
			                      GovernanceConstants.RXT_CONFIGS_PATH +
			                              RegistryConstants.PATH_SEPARATOR + rxtPath;

			// This is "registry" is a governance registry instance, therefore
			// calculate the relative path to governance.
			String govRelativePath =
			                         RegistryUtils.getRelativePathToOriginal(resourcePath,
			                                                                 RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
			try {
				if (registry.resourceExists(govRelativePath)) {
					continue;
				}
				String rxt = FileUtil.readFileToString(rxtDir + File.separator + rxtPath);
				Resource resource = registry.newResource();
				resource.setContent(rxt.getBytes());
				resource.setMediaType(AppMConstants.RXT_MEDIA_TYPE);
				registry.put(govRelativePath, resource);
			} catch (IOException e) {
				String msg = "Failed to read rxt files";
				throw new AppManagementException(msg, e);
			} catch (RegistryException e) {
				String msg = "Failed to add rxt to registry ";
				throw new AppManagementException(msg, e);
			}
		}

	}

	/**
	 * Converting the user store domain name to uppercase.
	 * 
	 * @param username
	 *            Username to be modified
	 * @return Username with domain name set to uppercase.
	 */
	public static String setDomainNameToUppercase(String username) {
		if (username != null) {
			String[] nameParts = username.split(CarbonConstants.DOMAIN_SEPARATOR);
			if (nameParts.length > 1) {
				username =
				           nameParts[0].toUpperCase() + CarbonConstants.DOMAIN_SEPARATOR +
				                   nameParts[1];
			}
		}

		return username;
	}

	public void setupSelfRegistration(AppManagerConfiguration config, int tenantId)
	                                                                               throws
                                                                                   AppManagementException {
		boolean enabled =
		                  Boolean.parseBoolean(config.getFirstProperty(AppMConstants.SELF_SIGN_UP_ENABLED));
		if (!enabled) {
			return;
		}

		String role = config.getFirstProperty(AppMConstants.SELF_SIGN_UP_ROLE);
		if (role == null) {
			// Required parameter missing - Throw an exception and interrupt
			// startup
			throw new AppManagementException("Required subscriber role parameter missing "
			                                 + "in the self sign up configuration");
		}

		boolean create =
		                 Boolean.parseBoolean(config.getFirstProperty(AppMConstants.SELF_SIGN_UP_CREATE_ROLE));
		if (create) {
			String[] permissions =
			                       new String[] { "/permission/admin/login",
			                                     AppMConstants.Permissions.WEB_APP_SUBSCRIBE};
			try {
				RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
				UserRealm realm;
				org.wso2.carbon.user.api.UserRealm tenantRealm;
				UserStoreManager manager;

				if (tenantId < 0) {
					realm = realmService.getBootstrapRealm();
					manager = realm.getUserStoreManager();
				} else {
					tenantRealm = realmService.getTenantUserRealm(tenantId);
					manager = tenantRealm.getUserStoreManager();
				}
				if (!manager.isExistingRole(role)) {
					if (log.isDebugEnabled()) {
						log.debug("Creating subscriber role: " + role);
					}
					Permission[] subscriberPermissions =
					                                     new Permission[] {
					                                                       new Permission(
					                                                                      "/permission/admin/login",
					                                                                      UserMgtConstants.EXECUTE_ACTION),
					                                                       new Permission(
					                                                                      AppMConstants.Permissions.WEB_APP_SUBSCRIBE,
					                                                                      UserMgtConstants.EXECUTE_ACTION) };
					String tenantAdminName =
					                         ServiceReferenceHolder.getInstance().getRealmService()
					                                               .getTenantUserRealm(tenantId)
					                                               .getRealmConfiguration()
					                                               .getAdminUserName();
					String[] userList = new String[] { tenantAdminName };
					manager.addRole(role, userList, subscriberPermissions);
				}
			} catch (UserStoreException e) {
				throw new AppManagementException("Error while creating subscriber role: " + role +
				                                 " - " +
				                                 "Self registration might not function properly.",
				                                 e);
			}
		}
	}

	public static String removeAnySymbolFromUriTempate(String uriTemplate) {
		if (uriTemplate != null) {
			int anySymbolIndex = uriTemplate.indexOf("/*");
			if (anySymbolIndex != -1) {
				return uriTemplate.substring(0, anySymbolIndex);
			}
		}
		return uriTemplate;
	}

	public static List<Tenant> getAllTenantsWithSuperTenant() throws UserStoreException {
		Tenant[] tenants =
		                   ServiceReferenceHolder.getInstance().getRealmService()
		                                         .getTenantManager().getAllTenants();
		ArrayList<Tenant> tenantArrayList = new ArrayList<Tenant>();
		for (Tenant t : tenants) {
			tenantArrayList.add(t);
		}
		Tenant superAdminTenant = new Tenant();
		superAdminTenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
		superAdminTenant.setId(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID);
		superAdminTenant.setAdminName(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
		tenantArrayList.add(superAdminTenant);
		return tenantArrayList;
	}

	/**
	 * In multi tenant environment, publishers should allow only to revoke the
	 * tokens generated within his domain.
	 * Super tenant should not see the tenant created tokens and vise versa.
	 * This method is used to check the logged in
	 * user have permissions to revoke a given users tokens.
	 * 
	 * @param loggedInUser
	 *            current logged in user to publisher
	 * @param authorizedUser
	 *            access token owner
	 * @return
	 */
	public static boolean isLoggedInUserAuthorizedToRevokeToken(String loggedInUser,
	                                                            String authorizedUser) {
		String loggedUserTenantDomain = MultitenantUtils.getTenantDomain(loggedInUser);
		String authorizedUserTenantDomain = MultitenantUtils.getTenantDomain(authorizedUser);

		if (loggedUserTenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME) &&
		    authorizedUserTenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
			return true;
		} else if (loggedUserTenantDomain.equals(authorizedUserTenantDomain)) {
			return true;
		}

		return false;
	}

	public static int getApplicationId(String appName, String userId) throws
                                                                      AppManagementException {
		return new AppMDAO().getApplicationId(appName, userId);
	}

	public static boolean isAPIManagementEnabled() {
		return Boolean.parseBoolean(CarbonUtils.getServerConfiguration()
		                                       .getFirstProperty("APIManagement.Enabled"));
	}

	public static boolean isLoadAPIContextsAtStartup() {
		return Boolean.parseBoolean(CarbonUtils.getServerConfiguration()
		                                       .getFirstProperty("APIManagement.LoadAPIContextsInServerStartup"));
	}

	public static Set<AppStore> getExternalAPIStores() throws AppManagementException {
		SortedSet<AppStore> apistoreSet = new TreeSet<AppStore>(new APIStoreNameComparator());
		AppManagerConfiguration config =
		                                 ServiceReferenceHolder.getInstance()
		                                                       .getAPIManagerConfigurationService()
		                                                       .getAPIManagerConfiguration();
		apistoreSet.addAll(config.getExternalAPIStores());
		if (apistoreSet.size() != 0) {
			return apistoreSet;
		} else {
			return null;
		}

	}

	public static Set<AppStore> getExternalAPIStores(Set<AppStore> inputStores)
	                                                                           throws
                                                                               AppManagementException {
		SortedSet<AppStore> apiStores = new TreeSet<AppStore>(new APIStoreNameComparator());
		AppManagerConfiguration config =
		                                 ServiceReferenceHolder.getInstance()
		                                                       .getAPIManagerConfigurationService()
		                                                       .getAPIManagerConfiguration();
		apiStores.addAll(config.getExternalAPIStores());
		boolean exists = false;
		if (apiStores.size() != 0) {
			for (AppStore store : apiStores) {
				for (AppStore inputStore : inputStores) {
					if (inputStore.getName().equals(store.getName())) { // If
						                                                // the
						                                                // configured
						                                                // appstore
						                                                // already
						                                                // stored
						                                                // in
						                                                // db,ignore
						                                                // adding
						                                                // it
						                                                // again
						exists = true;
					}
				}
				if (!exists) {
					inputStores.add(store);
				}
				exists = false;
			}

		}
		return inputStores;

	}

	public static boolean isAPIsPublishToExternalAPIStores() throws AppManagementException {

		AppManagerConfiguration config =
		                                 ServiceReferenceHolder.getInstance()
		                                                       .getAPIManagerConfigurationService()
		                                                       .getAPIManagerConfiguration();
		return config.getExternalAPIStores().size() != 0;

	}

	public static boolean isAPIGatewayKeyCacheEnabled() {
		try {
			AppManagerConfiguration config =
			                                 ServiceReferenceHolder.getInstance()
			                                                       .getAPIManagerConfigurationService()
			                                                       .getAPIManagerConfiguration();
			String serviceURL = config.getFirstProperty(AppMConstants.API_GATEWAY_KEY_CACHE_ENABLED);
			return Boolean.parseBoolean(serviceURL);
		} catch (Exception e) {
			log.error("Did not found valid WebApp Validation Information cache configuration. Use default configuration" +
			          e);
		}
		return true;
	}

	public static Cache getAPIContextCache() {
		CacheManager contextCacheManager =
		                                   Caching.getCacheManager(AppMConstants.API_CONTEXT_CACHE_MANAGER)
		                                          .getCache(AppMConstants.API_CONTEXT_CACHE)
		                                          .getCacheManager();
		if (!isContextCacheInitialized) {
			isContextCacheInitialized = true;
			return contextCacheManager.<String, Boolean> createCacheBuilder(AppMConstants.API_CONTEXT_CACHE_MANAGER)
			                          .setExpiry(CacheConfiguration.ExpiryType.MODIFIED,
			                                     new CacheConfiguration.Duration(
			                                                                     TimeUnit.DAYS,
			                                                                     AppMConstants.API_CONTEXT_CACHE_EXPIRY_TIME_IN_DAYS))
			                          .setStoreByValue(false).build();
		} else {
			return Caching.getCacheManager(AppMConstants.API_CONTEXT_CACHE_MANAGER)
			              .getCache(AppMConstants.API_CONTEXT_CACHE);
		}
	}

	/**
	 * Get active tenant domains
	 * 
	 * @return
	 * @throws UserStoreException
	 */
	public static Set<String> getActiveTenantDomains() throws UserStoreException {
		Set<String> tenantDomains = null;
		Tenant[] tenants =
		                   ServiceReferenceHolder.getInstance().getRealmService()
		                                         .getTenantManager().getAllTenants();
		if (tenants == null || tenants.length == 0) {
			return tenantDomains;
		} else {
			tenantDomains = new HashSet<String>();
			for (Tenant tenant : tenants) {
				if (tenant.isActive()) {
					tenantDomains.add(tenant.getDomain());
				}
			}
			if (tenantDomains.size() > 0) {
				tenantDomains.add(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
			}
			return tenantDomains;
		}

	}

    /**
     * Check whether given tenant is active or not.
     *
     * @param tenantDomain Tenant Domain
     * @return true if active false if not active/if no tenant exist
     * @throws UserStoreException
     */
    public static boolean isTenantActive(String tenantDomain) throws UserStoreException {
        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                getTenantManager().getTenantId(tenantDomain);
        boolean isTenantActive =
                ServiceReferenceHolder.getInstance().getRealmService()
                        .getTenantManager().isTenantActive(tenantId);
        return isTenantActive;
    }

	/**
	 * Retrieves the role list of system
	 * 
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             If an error occurs
	 */
	public static String[] getRoleNames(String username) throws AppManagementException {

		String tenantDomain = MultitenantUtils.getTenantDomain(username);
		try {
			if (!tenantDomain.equals(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
				int tenantId =
				               ServiceReferenceHolder.getInstance().getRealmService()
				                                     .getTenantManager().getTenantId(tenantDomain);
				UserStoreManager manager =
				                           ServiceReferenceHolder.getInstance().getRealmService()
				                                                 .getTenantUserRealm(tenantId)
				                                                 .getUserStoreManager();

				return manager.getRoleNames();
			} else {
				RemoteAuthorizationManager authorizationManager =
				                                                  RemoteAuthorizationManager.getInstance();
				return authorizationManager.getRoleNames();
			}
		} catch (UserStoreException e) {
			log.error("Error while getting all the roles", e);
			return null;

		}

	}

	/**
	 * Create WebApp Definition in JSON
	 * 
	 * @param api
	 *            WebApp
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 *             if failed to generate the content and save
	 */
	public static String createSwaggerJSONContent(WebApp api) throws AppManagementException {
		APIIdentifier identifier = api.getId();

		AppManagerConfiguration config =
		                                 ServiceReferenceHolder.getInstance()
		                                                       .getAPIManagerConfigurationService()
		                                                       .getAPIManagerConfiguration();

		Environment environment = config.getApiGatewayEnvironments().get(0);
		String endpoints = environment.getApiGatewayEndpoint();
		String[] endpointsSet = endpoints.split(",");
		String apiContext = api.getContext();
		String version = identifier.getVersion();
		Set<URITemplate> uriTemplates = api.getUriTemplates();
		String description = api.getDescription();
		String urlPrefix = apiContext + "/" + version;

		if (endpointsSet.length < 1) {
			throw new AppManagementException("Error in creating JSON representation of the WebApp" +
			                                 identifier.getApiName());
		}
		if (description == null || description.equals("")) {
			description = "";
		}

		Map<String, List<Operation>> uriTemplateDefinitions =
		                                                      new HashMap<String, List<Operation>>();
		List<APIResource> apis = new ArrayList<APIResource>();
		for (URITemplate template : uriTemplates) {
			List<Operation> ops;
			List<Parameter> parameters = null;
			String path =
			              urlPrefix +
			                      AppManagerUtil.removeAnySymbolFromUriTempate(template.getUriTemplate());
			/* path exists in uriTemplateDefinitions */
			if (uriTemplateDefinitions.get(path) != null) {
				ops = uriTemplateDefinitions.get(path);
				parameters = new ArrayList<Parameter>();
				if (!(template.getAuthType().equals(AppMConstants.AUTH_NO_AUTHENTICATION))) {
					Parameter authParam =
					                      new Parameter(
					                                    AppMConstants.OperationParameter.AUTH_PARAM_NAME,
					                                    AppMConstants.OperationParameter.AUTH_PARAM_DESCRIPTION,
					                                    AppMConstants.OperationParameter.AUTH_PARAM_TYPE,
					                                    true, false, "String");
					parameters.add(authParam);
				}
				String httpVerb = template.getHTTPVerb();
				/* For GET and DELETE Parameter name - Query Parameters */
				if (httpVerb.equals(Constants.Configuration.HTTP_METHOD_GET) ||
				    httpVerb.equals(Constants.Configuration.HTTP_METHOD_DELETE)) {
					Parameter queryParam =
					                       new Parameter(
					                                     AppMConstants.OperationParameter.QUERY_PARAM_NAME,
					                                     AppMConstants.OperationParameter.QUERY_PARAM_DESCRIPTION,
					                                     AppMConstants.OperationParameter.PAYLOAD_PARAM_TYPE,
					                                     false, false, "String");
					parameters.add(queryParam);
				} else {/* For POST and PUT Parameter name - Payload */
					Parameter payLoadParam =
					                         new Parameter(
					                                       AppMConstants.OperationParameter.PAYLOAD_PARAM_NAME,
					                                       AppMConstants.OperationParameter.PAYLOAD_PARAM_DESCRIPTION,
					                                       AppMConstants.OperationParameter.PAYLOAD_PARAM_TYPE,
					                                       false, false, "String");
					parameters.add(payLoadParam);
				}
				Operation op = new Operation(httpVerb, description, description, parameters);
				ops.add(op);
			} else {/* path not exists in uriTemplateDefinitions */
				ops = new ArrayList<Operation>();
				parameters = new ArrayList<Parameter>();
				if (!(template.getAuthType().equals(AppMConstants.AUTH_NO_AUTHENTICATION))) {
					Parameter authParam =
					                      new Parameter(
					                                    AppMConstants.OperationParameter.AUTH_PARAM_NAME,
					                                    AppMConstants.OperationParameter.AUTH_PARAM_DESCRIPTION,
					                                    AppMConstants.OperationParameter.AUTH_PARAM_TYPE,
					                                    true, false, "String");
					parameters.add(authParam);
				}
				String httpVerb = template.getHTTPVerb();
				/* For GET and DELETE Parameter name - Query Parameters */
				if (httpVerb.equals(Constants.Configuration.HTTP_METHOD_GET) ||
				    httpVerb.equals(Constants.Configuration.HTTP_METHOD_DELETE)) {
					Parameter queryParam =
					                       new Parameter(
					                                     AppMConstants.OperationParameter.QUERY_PARAM_NAME,
					                                     AppMConstants.OperationParameter.QUERY_PARAM_DESCRIPTION,
					                                     AppMConstants.OperationParameter.PAYLOAD_PARAM_TYPE,
					                                     false, false, "String");
					parameters.add(queryParam);
				} else {/* For POST and PUT Parameter name - Payload */
					Parameter payLoadParam =
					                         new Parameter(
					                                       AppMConstants.OperationParameter.PAYLOAD_PARAM_NAME,
					                                       AppMConstants.OperationParameter.PAYLOAD_PARAM_DESCRIPTION,
					                                       AppMConstants.OperationParameter.PAYLOAD_PARAM_TYPE,
					                                       false, false, "String");
					parameters.add(payLoadParam);
				}
				Operation op = new Operation(httpVerb, description, description, parameters);
				ops.add(op);
				uriTemplateDefinitions.put(path, ops);
			}
		}

		Set<String> resPaths = uriTemplateDefinitions.keySet();

		for (String resPath : resPaths) {
			APIResource apiResource =
			                          new APIResource(resPath, description,
			                                          uriTemplateDefinitions.get(resPath));
			apis.add(apiResource);
		}

		APIDefinition apidefinition =
		                              new APIDefinition(version, AppMConstants.SWAGGER_VERSION,
		                                                endpointsSet[0], apiContext, apis);

		Gson gson = new Gson();
		return gson.toJson(apidefinition);
	}

	/**
	 * Build OMElement from inputstream
	 * 
	 * @param inputStream
	 * @return OMElement
	 * @throws Exception
	 * @return
	 */
	public static OMElement buildOMElement(InputStream inputStream) throws Exception {
		XMLStreamReader parser;
		StAXOMBuilder builder;
		try {
			parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
			builder = new StAXOMBuilder(parser);
		} catch (XMLStreamException e) {
			String msg = "Error in initializing the parser.";
			log.error(msg, e);
			throw new Exception(msg, e);
		}

		return builder.getDocumentElement();
	}

	/**
	 * Get stored custom sequences from governanceSystem registry
	 * 
	 * @param sequenceName
	 *            -The sequence to be retrieved
	 * @param tenantId
	 * @param direction
	 *            - Direction indicates in Sequence/outSequence. Values would be
	 *            "in" or "out"
	 * @return
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 */
	public static OMElement getCustomSequence(String sequenceName, int tenantId, String direction)
	                                                                                              throws
                                                                                                  AppManagementException {
		org.wso2.carbon.registry.api.Collection seqCollection = null;

		try {
			UserRegistry registry =
			                        ServiceReferenceHolder.getInstance().getRegistryService()
			                                              .getGovernanceSystemRegistry(tenantId);
			if ("in".equals(direction)) {
				seqCollection =
				                (org.wso2.carbon.registry.api.Collection) registry.get(AppMConstants.API_CUSTOM_INSEQUENCE_LOCATION);
			}
			if ("out".equals(direction)) {
				seqCollection =
				                (org.wso2.carbon.registry.api.Collection) registry.get(AppMConstants.API_CUSTOM_OUTSEQUENCE_LOCATION);
			}
			if (seqCollection != null) {
				String[] childPaths = seqCollection.getChildren();

				for (int i = 0; i < childPaths.length; i++) {
					Resource sequence = registry.get(childPaths[i]);
					OMElement seqElment = AppManagerUtil.buildOMElement(sequence.getContentStream());
					if (sequenceName.equals(seqElment.getAttributeValue(new QName("name")))) {
						return seqElment;
					}
				}
			}

		} catch (Exception e) {
			String msg = "Issue is in accessing the Registry";
			log.error(msg);
			throw new AppManagementException(msg, e);
		}
		return null;
	}

	/**
	 * Return the sequence extension name.
	 * eg: admin--testAPi--v1.00
	 * 
	 * @param api
	 * @return
	 */
	public static String getSequenceExtensionName(WebApp api) {

		String seqExt =
		                api.getId().getProviderName() + "--" + api.getId().getApiName() + ":v" +
		                        api.getId().getVersion();

		return seqExt;

	}

	/**
	 * 
	 * @param token
	 * @return
	 */
	public static String decryptToken(String token) throws CryptoException {
		AppManagerConfiguration config =
		                                 ServiceReferenceHolder.getInstance()
		                                                       .getAPIManagerConfigurationService()
		                                                       .getAPIManagerConfiguration();

		if (Boolean.parseBoolean(config.getFirstProperty(AppMConstants.API_KEY_MANAGER_ENCRYPT_TOKENS))) {
			return new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(token));
		}
		return token;
	}

	/**
	 * 
	 * @param token
	 * @return
	 */
	public static String encryptToken(String token) throws CryptoException {
		AppManagerConfiguration config =
		                                 ServiceReferenceHolder.getInstance()
		                                                       .getAPIManagerConfigurationService()
		                                                       .getAPIManagerConfiguration();

		if (Boolean.parseBoolean(config.getFirstProperty(AppMConstants.API_KEY_MANAGER_ENCRYPT_TOKENS))) {
			return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(token.getBytes());
		}
		return token;
	}

	public static void loadTenantRegistry(int tenantId) throws AppManagementException {
		TenantRegistryLoader tenantRegistryLoader = AppManagerComponent.getTenantRegistryLoader();
		ServiceReferenceHolder.getInstance().getIndexLoaderService().loadTenantIndex(tenantId);
		try {
			tenantRegistryLoader.loadTenantRegistry(tenantId);
		} catch (RegistryException e) {
			if (log.isErrorEnabled()) {
				String errorMessage =
						"Could not load the tenant registry for tenant ID :" + tenantId;
				log.error(errorMessage);
				throw new AppManagementException(errorMessage, e);
			}
		}
	}

    public static String getGovernanceRegistryResourceContent(String tenantDomain, final String registryLocation)
            throws UserStoreException, RegistryException {

        String content = null;
        if (tenantDomain == null) {
            tenantDomain = org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);
            loadTenantRegistry(tenantId);

            if (registry.resourceExists(registryLocation)) {
                Resource resource = registry.get(registryLocation);
                content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
            }
        } catch (AppManagementException e) {
            log.error(String.format("Can't get resouce in '%s'", registryLocation));
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        return content;
    }

	/**
	 * This is to get the registry resource's HTTP permlink path.
	 * Once this issue is fixed (https://wso2.org/jira/browse/REGISTRY-2110),
	 * we can remove this method, and get permlink from the resource.
	 * 
	 * @param path
	 *            - Registry resource path
	 * @return {@link String} -HTTP permlink
	 */
	public static String getRegistryResourceHTTPPermlink(String path) {
		String scheme = "http";

		ConfigurationContextService contetxservice = ServiceReferenceHolder.getContextService();

		int port =
		           CarbonUtils.getTransportProxyPort(contetxservice.getServerConfigContext(),
		                                             scheme);

		if (port == -1) {
			port = CarbonUtils.getTransportPort(contetxservice.getServerConfigContext(), scheme);
		}

		String webContext = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");

		if (webContext == null || webContext.equals("/")) {
			webContext = "";
		}
		RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
		String version = "";
		if (registryService == null) {
			log.error("Registry Service has not been set.");
		} else if (path != null) {
			try {
				String[] versions =
				                    registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME,
				                                                CarbonContext.getThreadLocalCarbonContext()
				                                                             .getTenantId())
				                                   .getVersions(path);
				if (versions != null && versions.length > 0) {
					version = versions[0].substring(versions[0].lastIndexOf(";version:"));
				}
			} catch (RegistryException e) {
				log.error("An error occurred while determining the latest version of the " +
				          "resource at the given path: " + path, e);
			}
		}
		if (port != -1 && path != null) {
			String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
			return webContext +
			       ((tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain))
			                                                                                                     ? "/" +
			                                                                                                       MultitenantConstants.TENANT_AWARE_URL_PREFIX +
			                                                                                                       "/" +
			                                                                                                       tenantDomain
			                                                                                                     : "") +
			       "/registry/resource" +
			       org.wso2.carbon.registry.app.Utils.encodeRegistryPath(path) + version;
		}
		return null;
	}

	public static boolean isSandboxEndpointsExists(WebApp api) {
		JSONParser parser = new JSONParser();
		JSONObject config = null;
		try {
			config = (JSONObject) parser.parse(api.getEndpointConfig());

			if (config.containsKey("sandbox_endpoints")) {
				return true;
			}
		} catch (ParseException e) {
			log.error("Unable to parse endpoint config JSON", e);
		} catch (ClassCastException e) {
			log.error("Unable to parse endpoint config JSON", e);
		}
		return false;
	}

	public static boolean isProductionEndpointsExists(WebApp api) {
		JSONParser parser = new JSONParser();
		JSONObject config = null;
		try {
			config = (JSONObject) parser.parse(api.getEndpointConfig());

			if (config.containsKey("production_endpoints")) {
				return true;
			}
		} catch (ParseException e) {
			log.error("Unable to parse endpoint config JSON", e);
		} catch (ClassCastException e) {
			log.error("Unable to parse endpoint config JSON", e);
		}
		return false;
	}

    public static String getXACMLPolicyTemplate() {
        return ServiceReferenceHolder.getInstance().getAppMgtXACMLPolicyTemplateReader().
                getConfiguration();
    }

    /**
     * This method creates mobileapps directory if it does not exists
     */
    public static void createMobileAppsDirectory() {
    	File mobileAppDirectory = new File (AppMConstants.MOBILE_APPS_DIRECTORY_PATH , 
    			AppMConstants.MOBILE_APPS_DIRECTORY_NAME);
    	if (!mobileAppDirectory.exists()) {
    		mobileAppDirectory.mkdir();
    	}
    	
    }

    /**
     * Helper method to get tenantId from userName
     *
     * @param userName
     * @return tenantId
     */
    public static int getTenantId(String userName) {
        //get tenant domain from user name
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();

        if (realmService == null) {
            return MultitenantConstants.SUPER_TENANT_ID;
        }

        try {
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            return tenantId;
        } catch (UserStoreException e) {

            log.error(e);
        }

        return -1;
    }

    /**
     * Check whether UIActivityPublishEnabled is set or not.
     * @return boolean value.
     */

    public static boolean isUIActivityDASPublishEnabled() {
        AppManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String isEnabled = configuration
                .getFirstProperty(AppMConstants.APP_USAGE_DAS_UI_ACTIVITY_ENABLED);
        return isEnabled != null && Boolean.parseBoolean(isEnabled);
    }

    /**
     * Returns a set of External APP Stores as defined in the underlying governance
     * registry.
     *
     * @return APP Store set
     * @throws AppManagementException if an error occurs when loading app stores from the registry
     */
    public static Set<AppStore> getExternalStores(int tenantId) throws AppManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Getting configured external store details from registry for tenant :" + tenantId);
        }

        Set<AppStore> externalAPIStores = new HashSet<AppStore>();
        try {
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(AppMConstants.EXTERNAL_APP_STORES_LOCATION)) {
                Resource resource = registry.get(AppMConstants.EXTERNAL_APP_STORES_LOCATION);
                String content = new String((byte[]) resource.getContent());
                OMElement element = AXIOMUtil.stringToOM(content);
                Iterator appStoreIterator = element.getChildrenWithLocalName(AppMConstants.EXTERNAL_APP_STORE);

                while (appStoreIterator.hasNext()) {
                    AppStore store = new AppStore();
                    OMElement storeElem = (OMElement) appStoreIterator.next();

                    String type = storeElem.getAttributeValue(new QName(AppMConstants.EXTERNAL_APP_STORE_TYPE));
                    String className =
                            storeElem.getAttributeValue(new QName(AppMConstants.EXTERNAL_APP_STORE_CLASS_NAME));
                    String name = storeElem.getAttributeValue(new QName(AppMConstants.EXTERNAL_APP_STORE_ID));
                    OMElement configDisplayName = storeElem.getFirstChildWithName
                            (new QName(AppMConstants.EXTERNAL_APP_STORE_DISPLAY_NAME));
                    OMElement endPoint = storeElem.getFirstChildWithName(
                            new QName(AppMConstants.EXTERNAL_APP_STORE_ENDPOINT));
                    OMElement password = storeElem.getFirstChildWithName(new QName(
                            AppMConstants.EXTERNAL_APP_STORE_PASSWORD));
                    OMElement username = storeElem.getFirstChildWithName(
                            new QName(AppMConstants.EXTERNAL_APP_STORE_USERNAME));

                    store.setType(type); //Set Store type [eg:wso2]
                    store.setPublisherClassName(className);
                    store.setName(name); //Set store name
                    store.setDisplayName(configDisplayName.getText());//Set store display name
                    store.setEndpoint(endPoint.getText());
                    store.setPassword(password.getText());
                    store.setUsername(username.getText());
                    externalAPIStores.add(store);
                    store.setPublished(false);
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while retrieving External Stores Configuration from registry : "
                    + AppMConstants.EXTERNAL_APP_STORES_LOCATION;
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Malformed XML found in the External Stores Configuration resource : "
                    + AppMConstants.EXTERNAL_APP_STORES_LOCATION;
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        } catch (OMException e) {
            String msg = "Malformed XML found in the External Stores Configuration resource "
                    + AppMConstants.EXTERNAL_APP_STORES_LOCATION;
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        }
        return externalAPIStores;
    }


    /**
     * Get the APP store from registry configuration for given store name
     *
     * @param appStoreName App Store Name
     * @return App Store
     * @throws AppManagementException
     */
    public static AppStore getExternalAppStore(String appStoreName, int tenantId) throws AppManagementException {
        AppStore appStore = null;
        Set<AppStore> externalAppStoresConfig = AppManagerUtil.getExternalStores(tenantId);
        if (externalAppStoresConfig != null && externalAppStoresConfig.size() > 0) {
            validateStoreName(externalAppStoresConfig);
            for (AppStore appStoreConfig : externalAppStoresConfig) {
                if (appStoreConfig.getName().equals(appStoreName)) {
                    appStore = appStoreConfig;
                }
            }
        }
        return appStore;
    }

    /**
     * Check whether given app stores have store name(id) and display name.
     *
     * @param appStores App Stores
     * @throws AppManagementException if name or display name is null for any appstore
     */
    public static void validateStoreName(Set<AppStore> appStores) throws AppManagementException {
        for (AppStore appStore : appStores) {
            String name = appStore.getName();
            String displayName = appStore.getDisplayName();
            if (name == null) {
                String msg = "Store id is not defined for one of the App Store  in configuration file :"
                        + AppMConstants.EXTERNAL_APP_STORES_LOCATION;
                log.error(msg);
                throw new AppManagementException(msg);
            }

            if (displayName == null) {
                String msg = "Store display name is not defined for the App Store with id : " + name + " " +
                        "in configuration file :"
                        + AppMConstants.EXTERNAL_APP_STORES_LOCATION;
                log.error(msg);
                throw new AppManagementException(msg);
            }
        }

    }

    public static ExternalAppStorePublisher getExternalStorePublisher(String className) throws AppManagementException{
        try{
            return (ExternalAppStorePublisher) Class.forName(className).newInstance();
        }catch (ClassNotFoundException e) {
            String msg = "External store publisher cannot be found, class :" + className;
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        } catch (InstantiationException e) {
            String msg = className + " class object cannot be instantiated";
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "External store publisher  cannot be access, class :" + className;
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        }
    }

    /**
     * Load the External APP Store Configuration  to the super user registry, in the server startup
     *
     * @param tenantID
     * @throws AppManagementException
     */

    public static void loadTenantExternalStoreConfig(int tenantID)
            throws AppManagementException {
        try {
            RegistryService registryService =
                    ServiceReferenceHolder.getInstance()
                            .getRegistryService();
            UserRegistry govRegistry = registryService.getGovernanceSystemRegistry(tenantID);

            if (govRegistry.resourceExists(AppMConstants.EXTERNAL_APP_STORES_LOCATION)) {
                log.debug("External Stores configuration already uploaded to the registry");
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Adding External Stores configuration to the tenant's registry");
            }
            InputStream inputStream =
                    AppManagerComponent.class.getResourceAsStream("/externalstores/default-external-app-stores.xml");
            byte[] data = IOUtils.toByteArray(inputStream);
            Resource resource = govRegistry.newResource();
            resource.setContent(data);
            govRegistry.put(AppMConstants.EXTERNAL_APP_STORES_LOCATION, resource);

			/*set resource permission*/
            AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantID).getAuthorizationManager();
            String resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                    AppManagerUtil.getMountedPath(RegistryContext.getBaseInstance(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
                            + AppMConstants.EXTERNAL_APP_STORES_LOCATION);
            authManager.denyRole(AppMConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);

        } catch (RegistryException e) {
            throw new AppManagementException("Error while saving External Stores configuration information to the registry", e);
        } catch (IOException e) {
            throw new AppManagementException("Error while reading External Stores configuration file content", e);
        } catch (UserStoreException e) {
            throw new AppManagementException("Error while setting permission to External Stores configuration file", e);
        }
    }


    /**
     * This method will return mounted path of the path if the path
     * is mounted. Else path will be returned.
     *
     * @param registryContext Registry Context instance which holds path mappings
     * @param path            default path of the registry
     * @return mounted path or path
     */
    public static String getMountedPath(RegistryContext registryContext, String path) {
        if (registryContext != null && path != null) {
            List<Mount> mounts = registryContext.getMounts();
            if (mounts != null) {
                for (Mount mount : mounts) {
                    if (path.equals(mount.getPath())) {
                        return mount.getTargetPath();
                    }
                }
            }
        }
        return path;
    }

	/**
	 * This get the basic authentication header as a input and decode it and gives username, password in return
	 *
	 * @param basicAuthHeader
	 * @return
	 */
	public static String[] getCredentialsFromBasicAuthHeader(String basicAuthHeader) {
		if (basicAuthHeader != null) {
			String base64Credentials = basicAuthHeader.substring("Basic".length()).trim();
			String credentialsString = new String(org.apache.commons.ssl.Base64.decodeBase64(base64Credentials.getBytes()));
			final String[] credentials = credentialsString.split(":", 2);
			if (credentials.length == 2) {
				return credentials;
			}
		}

		return null;
	}

    /**
     * @param tenantID
     * @throws AppManagementException
     * @deprecated Use the method 'createTenantConfInRegistry' instead of this method.
     *
     * TODO : Merge the configuration files created in this method, with the unified tenant configuration file. See the method "createTenantConfInRegistry()"
     *
     *
     */
    @Deprecated
    public static void createTenantSpecificConfigurationFilesInRegistry(int tenantID) throws AppManagementException {
        loadOAuthScopeRoleMapping(tenantID);
        loadCustomAppPropertyDefinitions(tenantID);
    }

    /**
     *
     * Creates the tenant specific master configuration file in the tenant registry.
     *
     * @param tenantID
     * @throws AppManagementException
     */
    public static void createTenantConfInRegistry(int tenantID) throws AppManagementException{

        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        try {

            UserRegistry registry = registryService.getGovernanceSystemRegistry(tenantID);

            String tenantConfRegistryPath = AppMConstants.APPMGT_APPLICATION_DATA_LOCATION + "/" + AppMConstants.TENANT_CONF_FILENAME;

            if(!registry.resourceExists(tenantConfRegistryPath)){

                String tenantConfFilePath = CarbonUtils.getCarbonHome() + File.separator +
                                                AppMConstants.RESOURCE_FOLDER_LOCATION + File.separator +
                                                AppMConstants.TENANT_CONF_FILENAME;

                File tenantConfFile = new File(tenantConfFilePath);

                byte[] data;

                if (tenantConfFile.exists()) {
                    FileInputStream fileInputStream = new FileInputStream(tenantConfFile);
                    data = IOUtils.toByteArray(fileInputStream);

                    Resource resource = registry.newResource();
                    resource.setMediaType(AppMConstants.APPLICATION_JSON_MEDIA_TYPE);
                    resource.setContent(data);

                    registry.put(tenantConfRegistryPath, resource);

                    log.debug(String.format("Added tenant configuration for the tenant %d to the tenant registry (%s) ", tenantID, tenantConfRegistryPath));

                }else{
                    String tenantConfFileRelativePath = String.format("CARBON_SERVER/%s/%s", AppMConstants.RESOURCE_FOLDER_LOCATION, AppMConstants.TENANT_CONF_FILENAME);
                    log.warn(String.format("Can't find tenant configuration file ('%s') to be added to the registry of the tenant (tenant id - %d)", tenantConfFileRelativePath, tenantID));
                }
            }

        } catch (RegistryException e) {
            throw new AppManagementException("Error while saving tenant conf to the registry", e);
        } catch (IOException e) {
            throw new AppManagementException("Error while reading tenant conf file content", e);
        }

    }

    private static void loadCustomAppPropertyDefinitions(int tenantID) throws AppManagementException {

        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        try {
            UserRegistry registry = registryService.getGovernanceSystemRegistry(tenantID);

            loadCustomAppPropertyDefinitionsForAppType(AppMConstants.WEBAPP_ASSET_TYPE, registry);
            loadCustomAppPropertyDefinitionsForAppType(AppMConstants.MOBILE_ASSET_TYPE, registry);
        } catch (RegistryException e) {
            throw new AppManagementException("Error while saving tenant conf to the registry", e);
        } catch (IOException e) {
            throw new AppManagementException("Error while reading tenant conf file content", e);
        }
    }

    private static void loadCustomAppPropertyDefinitionsForAppType(String appType, UserRegistry registry) throws RegistryException, IOException {

        if(!registry.resourceExists(getCustomPropertyDefinitionsResourcePath(appType))){

            String customPropertyDefinitions = CarbonUtils.getCarbonHome() + File.separator +
                                                AppMConstants.RESOURCE_FOLDER_LOCATION + File.separator +
                                                AppMConstants.CUSTOM_PROPERTY_DEFINITIONS_PATH + File.separator +
                                                appType + ".json";

            File customPropertyDefinitionsFile = new File(customPropertyDefinitions);

            byte[] data;

            if (customPropertyDefinitionsFile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(customPropertyDefinitionsFile);
                data = IOUtils.toByteArray(fileInputStream);

                Resource resource = registry.newResource();
                resource.setMediaType(AppMConstants.APPLICATION_JSON_MEDIA_TYPE);
                resource.setContent(data);

                String customPropertyDefinitionsRegistryPath = getCustomPropertyDefinitionsResourcePath(appType);
                registry.put(customPropertyDefinitionsRegistryPath, resource);

                log.debug(String.format("Added custom property mapping ('%s') for '%s'", customPropertyDefinitionsRegistryPath, appType));

            }else{
                log.warn(String.format("Can't find custom property definitions file ('%s') for '%s'", customPropertyDefinitionsFile, AppMConstants.WEBAPP_ASSET_TYPE));
            }
        }
    }

    private static String getCustomPropertyDefinitionsResourcePath(String appType) {
        return String.format("%s/%s/%s.json", AppMConstants.APPMGT_APPLICATION_DATA_LOCATION, AppMConstants.CUSTOM_PROPERTY_DEFINITIONS_PATH, appType);
    }

    private static void loadOAuthScopeRoleMapping(int tenantID) throws AppManagementException {

        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        try {
            UserRegistry registry = registryService.getGovernanceSystemRegistry(tenantID);

            if (registry.resourceExists(AppMConstants.OAUTH_SCOPE_ROLE_MAPPING_PATH)) {
                log.debug(String.format("OAuth scope role mapping (%s) registry resource already exists as '%s'",
                                            AppMConstants.OAUTH_SCOPE_ROLE_MAPPING_FILE,
                                            AppMConstants.OAUTH_SCOPE_ROLE_MAPPING_PATH));
                return;
            }

            String oauthScopeRoleMappingFilePath = CarbonUtils.getCarbonHome() + File.separator +
                                            AppMConstants.RESOURCE_FOLDER_LOCATION + File.separator +
                                            AppMConstants.OAUTH_SCOPE_ROLE_MAPPING_FILE;

            File oauthScopeRoleMappingFile = new File(oauthScopeRoleMappingFilePath);

            byte[] data;

            if (oauthScopeRoleMappingFile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(oauthScopeRoleMappingFile);
                data = IOUtils.toByteArray(fileInputStream);

                Resource resource = registry.newResource();
                resource.setMediaType(AppMConstants.APPLICATION_JSON_MEDIA_TYPE);
                resource.setContent(data);

                registry.put(AppMConstants.OAUTH_SCOPE_ROLE_MAPPING_PATH, resource);

                log.debug(String.format("Added OAuth scope role mapping (%s) registry resource to '%s'",
                                            AppMConstants.OAUTH_SCOPE_ROLE_MAPPING_FILE,
                                            AppMConstants.OAUTH_SCOPE_ROLE_MAPPING_PATH));

            }else{
                log.warn(String.format("Can't find OAuth scope role mapping file in '%s'", AppMConstants.OAUTH_SCOPE_ROLE_MAPPING_PATH));
            }
        } catch (RegistryException e) {
            throw new AppManagementException("Error while saving OAuth scope role mapping to the registry", e);
        } catch (IOException e) {
            throw new AppManagementException("Error while reading OAuth scope role mapping file content", e);
        }
    }

    public static boolean isUserAuthorized(String username, String resourcePath) throws AppManagementException {

        boolean isAuthorized = false;
        try {
            String tenantDomain =
                    MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(username));
            int tenantId =
                    ServiceReferenceHolder.getInstance().getRealmService()
                            .getTenantManager().getTenantId(tenantDomain);
            AuthorizationManager authManager = null;

            authManager = ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId).getAuthorizationManager();
            isAuthorized = authManager.isUserAuthorized(username, resourcePath, "authorize");
        } catch (UserStoreException e) {
            throw new AppManagementException("User " + username + " is not authorized to perform lifecycle action");
        }
        return isAuthorized;
    }

    public static void handleException(String msg, Throwable t) throws AppManagementException {
        log.error(msg, t);
        throw new AppManagementException(msg, t);
    }

    private static void handleException(String msg) throws AppManagementException {
        log.error(msg);
        throw new AppManagementException(msg);
    }

    /**
     * Return a http client instance
     *
     * @param port      - server port
     * @param protocol  - service endpoint protocol http/https
     * @return
     */
    public static HttpClient getHttpClient(int port, String protocol) {
        SchemeRegistry registry = new SchemeRegistry();
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        String ignoreHostnameVerification = System.getProperty("org.wso2.ignoreHostnameVerification");
        if (ignoreHostnameVerification != null && "true".equalsIgnoreCase(ignoreHostnameVerification)) {
            X509HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            socketFactory.setHostnameVerifier(hostnameVerifier);
        }
        if (AppMConstants.HTTPS_PROTOCOL.equalsIgnoreCase(protocol)) {
            if (port >= 0) {
                registry.register(new Scheme(AppMConstants.HTTPS_PROTOCOL, port, socketFactory));
            } else {
                registry.register(new Scheme(AppMConstants.HTTPS_PROTOCOL, 443, socketFactory));
            }
        } else if (AppMConstants.HTTP_PROTOCOL.equalsIgnoreCase(protocol)) {
            if (port >= 0) {
                registry.register(new Scheme(AppMConstants.HTTP_PROTOCOL, port, PlainSocketFactory.getSocketFactory()));
            } else {
                registry.register(new Scheme(AppMConstants.HTTP_PROTOCOL, 80, PlainSocketFactory.getSocketFactory()));
            }
        }
        HttpParams params = new BasicHttpParams();
        ThreadSafeClientConnManager tcm = new ThreadSafeClientConnManager(registry);
        return new DefaultHttpClient(tcm, params);
    }

    /**
     * Return a http client instance. This http client is configured according to the
     * org.wso2.ignoreHostnameVerification system property.
     *
     * @param url      - server endpoint
     * @return HttpClient
     */
    public static HttpClient getHttpClient(String url) {
        URL ulrEndpoint = new URL(url);
        int port = ulrEndpoint.getPort();
        String protocol = ulrEndpoint.getProtocol();
        return getHttpClient(port, protocol);
    }

    /**
     * Resolve file path avoiding Potential Path Traversals
     *
     * @param baseDirPath base directory file path
     * @param fileName    filename
     * @return
     */
    public static String resolvePath(String baseDirPath, String fileName) {
        final Path basePath = Paths.get(baseDirPath);
        final Path filePath = Paths.get(fileName);
        if (!basePath.isAbsolute()) {
            throw new IllegalArgumentException("Base directory path '" + baseDirPath + "' must be absolute");
        }
        if (filePath.isAbsolute()) {
            throw new IllegalArgumentException("Invalid file name '" + fileName + "' with an absolute file path is provided");
        }
        // Join the two paths together, then normalize so that any ".." elements
        final Path resolvedPath = basePath.resolve(filePath).normalize();

        // Make sure the resulting path is still within the required directory.
        if (!resolvedPath.startsWith(basePath.normalize())) {
            throw new IllegalArgumentException("File '" + fileName + "' is not within the required directory.");
        }

        return String.valueOf(resolvedPath);
    }

    //Create the default SSOProvider
    public static SSOProvider getDefaultSSOProvider() {

        SSOProvider ssoProvider = new SSOProvider();
        SSOEnvironment defaultSSOEnv = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getSsoEnvironments().get(0);

        ssoProvider.setProviderName(defaultSSOEnv.getName());
        ssoProvider.setProviderVersion(defaultSSOEnv.getVersion());

        //Adding the default role claim.
        ssoProvider.setClaims(new String[]{AppMConstants.claims.CLAIM_ROLES});

        return ssoProvider;
    }

    public static boolean isSelfSubscriptionEnable() throws AppManagementException {
        return readSubscriptionConfigurations("EnableSelfSubscription");
    }

    public static boolean isEnterpriseSubscriptionEnable() throws AppManagementException {
        return readSubscriptionConfigurations("EnableEnterpriseSubscription");
    }

    private static boolean readSubscriptionConfigurations(String key) throws AppManagementException {
        Resource tenantConfResource;
        Registry registryType = null;
        String tenantConfRegistryPath = "/_system/governance" + AppMConstants.APPMGT_APPLICATION_DATA_LOCATION + "/" +
                AppMConstants.TENANT_CONF_FILENAME;
        try {
            registryType = ServiceReferenceHolder.getInstance().
                    getRegistryService().getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
            if (registryType.resourceExists(tenantConfRegistryPath)) {
                tenantConfResource = registryType.get(tenantConfRegistryPath);
                String content = new String((byte[]) tenantConfResource.getContent());
                OMElement element = AXIOMUtil.stringToOM(content);
                Iterator appStoreIterator = element.getChildrenWithLocalName("Subscriptions");
                if (appStoreIterator.hasNext()) {
                    OMElement storeElem = (OMElement) appStoreIterator.next();
                    OMElement subscriptionElem = storeElem.getFirstChildWithName(new QName(key));
                    String subscriptionValue = subscriptionElem.getText();
                    return Boolean.parseBoolean(subscriptionValue);
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while retrieving EnableSelfSubscription configuration from registry path: "
                    + tenantConfRegistryPath;
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Malformed XML found in the subscription configuration resource : "
                    + tenantConfRegistryPath;
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        } catch (OMException e) {
            String msg = "Malformed XML found in the subscription configuration resource : "
                    + tenantConfRegistryPath;
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        }
        return false;
    }
}
