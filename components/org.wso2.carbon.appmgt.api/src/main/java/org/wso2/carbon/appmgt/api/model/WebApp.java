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
package org.wso2.carbon.appmgt.api.model;

import java.util.*;

/**
 * Provider's & system's view of WebApp
 */
@SuppressWarnings("unused")
public class WebApp extends App{

    private int databaseId;
    private APIIdentifier id;

    private String description;
    private String url;
    private String sandboxUrl;
    private String wsdlUrl;
    private String wadlUrl;
    private String context;
    private String thumbnailUrl;
    private Set<String> tags = new LinkedHashSet<String>();
    private Set<Documentation> documents = new LinkedHashSet<Documentation>();
    private String httpVerb;
    private Date lastUpdated;
    private Set<Tier> availableTiers = new LinkedHashSet<Tier>();
    private AuthorizationPolicy authorizationPolicy;
    private List<EntitlementPolicyGroup> accessPolicyGroups = new ArrayList<EntitlementPolicyGroup>();
    private Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();

    private List<String> claims = new ArrayList<>();

    private String ssoEnabled;
    private String idpProviderUrl;
    private String saml2SsoIssuer;
    private SSOProvider ssoProviderDetails;

    private String trackingCode;

	private String tokenEndpoint;
    private String apiConsumerKey;
    private String apiConsumerSecret;
    private String apiName;

    //dirty pattern to identify which parts to be updated
    private boolean apiHeaderChanged;
    private boolean apiResourcePatternsChanged;

    private APIStatus status;

    private String technicalOwner;
    private String technicalOwnerEmail;
    private String businessOwner;
    private String businessOwnerEmail;

    private String visibility;
    private String visibleRoles;
    private String visibleTenants;

    private boolean endpointSecured = false;
	private String endpointUTUsername;
    private String endpointUTPassword;

    private String transports;
    private Boolean allowAnonymous;
    private String inSequence;
    private String outSequence;

    private String oldInSequence;
    private String oldOutSequence;

    private boolean advertiseOnly;
    private String advertisedAppUuid;
    private String appOwner;
    private String appTenant;
    private String redirectURL;
    private String logoutURL;

    private String subscriptionAvailability;
    private String subscriptionAvailableTenants;


    private String endpointConfig;

    private String responseCache;
    private int cacheTimeout;
    private String policyPartials;
    private String policyGroups; //Policy Groups Id's list
    private String javaPolicies; //Java policies(handlers) List
    private String treatAsASite;

    private boolean isLatest;

    private boolean skipGateway;

    private String acsURL;

    private boolean isDefaultVersion;
    //TODO: missing - total user count, up time statistics,tier



    //Asset Type : either webapp or mobile app
    private String type;

    private String mediaType;
    private String path;
    public String createdTime;
    private String originVersion;

    private boolean saveServiceProvider;

    public WebApp() {
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public WebApp(APIIdentifier id) {
        this.id = id;
    }

    public String getLogoutURL() {
        if(logoutURL != null && logoutURL.startsWith("/")){
            return logoutURL.substring(1);
        }else {
            return logoutURL;
        }
    }

    public void setLogoutURL(String logoutURL) {
        this.logoutURL = logoutURL;
    }

    public boolean isAdvertiseOnly() {
        return advertiseOnly;
    }

    public void setAdvertiseOnly(boolean advertiseOnly) {
        this.advertiseOnly = advertiseOnly;
    }

    public String getAppOwner() {
        return appOwner;
    }

    public void setAppOwner(String appOwner) {
        this.appOwner = appOwner;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public APIIdentifier getId() {
        return id;
    }

	public String getTransports() {
        return transports;
    }

    public void setTransports(String transports) {
        this.transports = transports;
    }

	public Boolean getAllowAnonymous(){
		return allowAnonymous;
	}

	public void setAllowAnonymous(Boolean allowAnonymous){
		this.allowAnonymous=allowAnonymous;
	}

    public String getTechnicalOwner() {
        return technicalOwner;
    }

    public void setTechnicalOwner(String technicalOwner) {
        this.technicalOwner = technicalOwner;
    }

    public String getTechnicalOwnerEmail() {
        return technicalOwnerEmail;
    }

    public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
        this.technicalOwnerEmail = technicalOwnerEmail;
    }

    public String getBusinessOwner() {
        return businessOwner;
    }

    public void setBusinessOwner(String businessOwner) {
        this.businessOwner = businessOwner;
    }

    public String getBusinessOwnerEmail() {
        return businessOwnerEmail;
    }

    public void setBusinessOwnerEmail(String businessOwnerEmail) {
        this.businessOwnerEmail = businessOwnerEmail;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSandboxUrl() {
        return sandboxUrl;
    }

    public void setSandboxUrl(String sandboxUrl) {
        this.sandboxUrl = sandboxUrl;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void addTags(Set<String> tags) {
        this.tags.addAll(tags);
    }

    public void removeTags(Set<String> tags) {
        this.tags.removeAll(tags);
    }

    public Set<Documentation> getDocuments() {
        return Collections.unmodifiableSet(documents);
    }

    public void addDocuments(Set<Documentation> documents) {
        this.documents.addAll(documents);
    }

    public void removeDocuments(Set<Documentation> documents) {
        this.documents.removeAll(documents);
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public Date getLastUpdated() {
        return new Date(lastUpdated.getTime());
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = new Date(lastUpdated.getTime());
    }

    public Set<Tier> getAvailableTiers() {
        return Collections.unmodifiableSet(availableTiers);
    }

    public void addAvailableTiers(Set<Tier> availableTiers) {
        this.availableTiers.addAll(availableTiers);
    }

    public void removeAvailableTiers(Set<Tier> availableTiers) {
        this.availableTiers.removeAll(availableTiers);
    }

    public Set<URITemplate> getUriTemplates() {
        return uriTemplates;
    }

    public void setUriTemplates(Set<URITemplate> uriTemplates) {
        this.uriTemplates = uriTemplates;
    }

    public APIStatus getStatus() {
        return status;
    }

    public void setStatus(APIStatus status) {
        this.status = status;
    }

    public void setLatest(boolean latest) {
        isLatest = latest;
    }

    /**
     * @return true if the current version of the WebApp is the latest
     */
    public boolean isLatest() {
        return isLatest;
    }

    public AuthorizationPolicy getAuthorizationPolicy() {
        return authorizationPolicy;
    }

    public void setAuthorizationPolicy(AuthorizationPolicy authorizationPolicy) {
        this.authorizationPolicy = authorizationPolicy;
    }

    /**
     * Get the application's policy partial ids
     * @return policy partial ids
     */
    public String getPolicyPartials() {
        return policyPartials;
    }

    /**
     * Set application's policy partial ids
     * @param policyPartials
     */
    public void setPolicyPartials(String policyPartials) {
        this.policyPartials = policyPartials;
    }

    public String getWadlUrl() {
        return wadlUrl;
    }

    public void setWadlUrl(String wadlUrl) {
        this.wadlUrl = wadlUrl;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getVisibleRoles() {
        return visibleRoles;
    }

    public void setVisibleRoles(String visibleRoles) {
        this.visibleRoles = visibleRoles;
    }

    public String getVisibleTenants() {
    	return visibleTenants;
    }

    public void setVisibleTenants(String visibleTenants) {
    	this.visibleTenants = visibleTenants;
    }

    public boolean isApiHeaderChanged() {
        return apiHeaderChanged;
    }

    public void setApiHeaderChanged(boolean apiHeaderChanged) {
        this.apiHeaderChanged = apiHeaderChanged;
    }

    public boolean isApiResourcePatternsChanged() {
        return apiResourcePatternsChanged;
    }

    public void setApiResourcePatternsChanged(boolean apiResourcePatternsChanged) {
        this.apiResourcePatternsChanged = apiResourcePatternsChanged;
    }

    /**
  	 * @return the endpointUTUsername
  	 */
  	public String getEndpointUTUsername() {
  		return endpointUTUsername;
  	}

  	/**
  	 * @param endpointUTUsername the endpointUTUsername to set
  	 */
  	public void setEndpointUTUsername(String endpointUTUsername) {
  		this.endpointUTUsername = endpointUTUsername;
  	}

  	/**
  	 * @return the endpointUTPassword
  	 */
  	public String getEndpointUTPassword() {
  		return endpointUTPassword;
  	}

  	/**
  	 * @param endpointUTPassword the endpointUTPassword to set
  	 */
  	public void setEndpointUTPassword(String endpointUTPassword) {
  		this.endpointUTPassword = endpointUTPassword;
  	}

 	/**
 	 * @return the endpointSecured
 	 */
 	public boolean isEndpointSecured() {
 		return endpointSecured;
 	}

 	/**
 	 * @param endpointSecured the endpointSecured to set
 	 */
 	public void setEndpointSecured(boolean endpointSecured) {
 		this.endpointSecured = endpointSecured;
 	}

    public String getInSequence() {
 		return inSequence;
 	}

    /**
     *
     * @param inSeq  insequence for the WebApp
     */
 	public void setInSequence(String inSeq) {
 		this.inSequence = inSeq;
 	}

 	 public String getOutSequence() {
  		return outSequence;
  	}

     /**
      *
      * @param outSeq outSequence for the WebApp
      */
  	public void setOutSequence(String outSeq) {
  		this.outSequence = outSeq;
  	}

    public String getOldInSequence() {
        return oldInSequence;
    }

    public void setOldInSequence(String oldInSequence) {
        this.oldInSequence = oldInSequence;
    }

    public String getOldOutSequence() {
        return oldOutSequence;
    }

    public void setOldOutSequence(String oldOutSequence) {
        this.oldOutSequence = oldOutSequence;
    }

	public String getSubscriptionAvailability() {
		return subscriptionAvailability;
	}

	public void setSubscriptionAvailability(String subscriptionAvailability) {
		this.subscriptionAvailability = subscriptionAvailability;
	}

	public String getSubscriptionAvailableTenants() {
		return subscriptionAvailableTenants;
	}

	public void setSubscriptionAvailableTenants(String subscriptionAvailableTenants) {
		this.subscriptionAvailableTenants = subscriptionAvailableTenants;
	}

    public String getEndpointConfig() {
        return endpointConfig;
    }

    public void setEndpointConfig(String endpointConfig) {
        this.endpointConfig = endpointConfig;
    }

	public String getResponseCache() {
		return responseCache;
	}

	public void setResponseCache(String responseCache) {
		this.responseCache = responseCache;
	}

	public int getCacheTimeout() {
		return cacheTimeout;
	}

	public void setCacheTimeout(int cacheTimeout) {
		this.cacheTimeout = cacheTimeout;
	}

    public String getIdpProviderUrl() {
        return idpProviderUrl;
    }

    public void setIdpProviderURL(String idpProviderUrl) {
        this.idpProviderUrl = idpProviderUrl;
    }

    public String getSaml2SsoIssuer() {
        return saml2SsoIssuer;
    }

    public void setSaml2SsoIssuer(String saml2SsoIssuer) {
        this.saml2SsoIssuer = saml2SsoIssuer;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

 	public String isSsoEnabled() {
 		return ssoEnabled;
 	}

 	public void setSsoEnabled(String ssoEnabled) {
 		this.ssoEnabled = ssoEnabled;
 	}

	public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getTokenEndpoint() {
        return this.tokenEndpoint;
    }

    public void setApiConsumerKey(String apiConsumerKey) {
        this.apiConsumerKey = apiConsumerKey;
    }

    public String getApiConsumerKey() {
        return this.apiConsumerKey;
    }

    public void setApiConsumerSecret(String apiConsumerSecret) {
        this.apiConsumerSecret = apiConsumerSecret;
    }

    public String getApiConsumerSecret() {
        return this.apiConsumerSecret;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiName() {
        return this.apiName;
    }

    public SSOProvider getSsoProviderDetails() {
        return ssoProviderDetails;
    }

    public void setSsoProviderDetails(SSOProvider ssoProviderDetails) {
        this.ssoProviderDetails = ssoProviderDetails;
    }

    /**
     * Set Policy Groups Id's list
     *
     * @param policyGroups : Policy group Id's mapped to Application
     */
    public void setPolicyGroups(String policyGroups) {
        this.policyGroups = policyGroups;
    }

    /**
     * Get Policy Groups Id's list
     *
     * @return : Policy group Id's mapped to Application
     */
    public String getPolicyGroups() {
        return policyGroups;
    }


    public void setJavaPolicies(String javaPolicies) {
        this.javaPolicies = javaPolicies;
    }

    public String getJavaPolicies() {
        return javaPolicies;
    }

    public boolean getSkipGateway() {
        return skipGateway;
    }

    public void setSkipGateway(boolean skipGateway) {
        this.skipGateway = skipGateway;
    }

    public String getAppTenant() {
        return appTenant;
    }

    public void setAppTenant(String appTenant) {
        this.appTenant = appTenant;
    }

    public String getAcsURL() {
        return acsURL;
    }

    public void setAcsURL(String acsURL) {
        this.acsURL = acsURL;
    }

    public String getTreatAsASite() {
        return treatAsASite;
    }

    public void setTreatAsASite(String treatAsASite) {
        this.treatAsASite = treatAsASite;
    }

    public boolean isDefaultVersion() {
        return isDefaultVersion;
    }

    public void setDefaultVersion(boolean isDefaultVersion) {
        this.isDefaultVersion = isDefaultVersion;
    }

    public String getAdvertisedAppUuid() {
        return advertisedAppUuid;
    }

    public void setAdvertisedAppUuid(String advertisedAppUuid) {
        this.advertisedAppUuid = advertisedAppUuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public void addAccessPolicyGroup(EntitlementPolicyGroup policyGroup){

        if(this.accessPolicyGroups == null){
            this.accessPolicyGroups = new ArrayList<EntitlementPolicyGroup>();
        }

        this.accessPolicyGroups.add(policyGroup);
    }

    public void setAccessPolicyGroups(List<EntitlementPolicyGroup> accessPolicyGroups) {
        this.accessPolicyGroups = accessPolicyGroups;
    }

    public List<EntitlementPolicyGroup> getAccessPolicyGroups(){
        return this.accessPolicyGroups;
    }

    public List<String> getClaims() {
        return claims;
    }

    public void setClaims(List<String> claims) {
        this.claims = claims;
    }

    public void setVersion(String version) {
        this.id.setVersion(version);
    }

    public void setOriginVersion(String originVersion) {
        this.originVersion = originVersion;
    }

    public String getOriginVersion() {
        return originVersion;
    }

    public void addURITemplate(URITemplate uriTemplate) {

        if(uriTemplates == null){
            uriTemplates = new HashSet<URITemplate>();
        }

        uriTemplates.add(uriTemplate);
    }

    public URITemplate getURITemplate(int urlTemplateId) {

        if(uriTemplates != null){

            for(URITemplate template : uriTemplates){
                if(template.getId() == urlTemplateId){
                    return template;
                }
            }

        }

        return null;
    }
    public List<String> getVisibleRoleList(){
        List<String> visibleRoleList = new ArrayList<String>();

        if(visibleRoles != null && visibleRoles != ""){
            String allowedRolesString = visibleRoles;
            if(allowedRolesString != null && !allowedRolesString.trim().isEmpty()){
                visibleRoleList = Arrays.asList(allowedRolesString.split(","));
            }
        }
        return visibleRoleList;
    }

    public void setSaveServiceProvider(boolean saveServiceProvider) {
        this.saveServiceProvider = saveServiceProvider;
    }

    public boolean isSaveServiceProvider() {
        return saveServiceProvider;
    }
}
