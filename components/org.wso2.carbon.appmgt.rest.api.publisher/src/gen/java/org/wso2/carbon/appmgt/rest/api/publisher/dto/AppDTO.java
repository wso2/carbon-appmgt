package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.UriTemplateDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppAppmetaDTO;
import java.math.BigDecimal;
import java.util.*;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.PolicyGroupsDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class AppDTO  {
  
  
  
  private String id = null;
  
  @NotNull
  private String name = null;
  
  
  private String type = null;
  
  
  private AppAppmetaDTO appmeta = null;
  
  
  private List<UriTemplateDTO> uriTemplates = new ArrayList<UriTemplateDTO>();
  
  
  private List<PolicyGroupsDTO> policyGroups = new ArrayList<PolicyGroupsDTO>();
  
  
  private String marketType = null;
  
  
  private String recentChanges = null;
  
  
  private String icon = null;
  
  
  private String isSite = null;
  
  
  private String description = null;
  
  
  private String context = null;
  
  @NotNull
  private String version = null;
  
  
  private String logoutURL = null;
  
  
  private String businessOwnerName = null;
  
  
  private List<String> visibleTenants = new ArrayList<String>();
  
  
  private String trackingCode = null;
  
  
  private String skipGateway = null;
  
  
  private String allowAnonymousAccess = null;
  
  
  private String acsUrl = null;
  
  
  private String provider = null;
  
  
  private String appDefinition = null;
  
  
  private Boolean isDefaultVersion = null;
  
  
  private String transport = null;
  
  
  private List<String> tags = new ArrayList<String>();
  
  
  private String thumbnailUrl = null;
  
  
  private List<String> visibleRoles = new ArrayList<String>();
  
  
  private String path = null;
  
  
  private String resourceId = null;
  
  
  private String lifecycle = null;
  
  
  private String lifecycleState = null;
  
  
  private BigDecimal rating = null;
  
  
  private String appUrL = null;
  
  
  private String bundleversion = null;
  
  
  private String category = null;
  
  @NotNull
  private String displayName = null;
  
  
  private List<String> screenshots = new ArrayList<String>();
  
  
  private String banner = null;
  
  
  private String createdtime = null;
  
  
  private String platform = null;
  
  
  private String appType = null;
  
  
  private String mediaType = null;
  
  
  private String lifecycleAvailableActions = null;
  
  
  private String createdTime = null;

  
  /**
   * UUID of the app registry artifact
   **/
  @ApiModelProperty(value = "UUID of the app registry artifact")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   * App type (either Webapp/Mobile app)
   **/
  @ApiModelProperty(value = "App type (either Webapp/Mobile app)")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("appmeta")
  public AppAppmetaDTO getAppmeta() {
    return appmeta;
  }
  public void setAppmeta(AppAppmetaDTO appmeta) {
    this.appmeta = appmeta;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("uriTemplates")
  public List<UriTemplateDTO> getUriTemplates() {
    return uriTemplates;
  }
  public void setUriTemplates(List<UriTemplateDTO> uriTemplates) {
    this.uriTemplates = uriTemplates;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("policyGroups")
  public List<PolicyGroupsDTO> getPolicyGroups() {
    return policyGroups;
  }
  public void setPolicyGroups(List<PolicyGroupsDTO> policyGroups) {
    this.policyGroups = policyGroups;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("marketType")
  public String getMarketType() {
    return marketType;
  }
  public void setMarketType(String marketType) {
    this.marketType = marketType;
  }

  
  /**
   * recent changes
   **/
  @ApiModelProperty(value = "recent changes")
  @JsonProperty("recentChanges")
  public String getRecentChanges() {
    return recentChanges;
  }
  public void setRecentChanges(String recentChanges) {
    this.recentChanges = recentChanges;
  }

  
  /**
   * the image icon of the application
   **/
  @ApiModelProperty(value = "the image icon of the application")
  @JsonProperty("icon")
  public String getIcon() {
    return icon;
  }
  public void setIcon(String icon) {
    this.icon = icon;
  }

  
  /**
   * Either a webapp or site
   **/
  @ApiModelProperty(value = "Either a webapp or site")
  @JsonProperty("isSite")
  public String getIsSite() {
    return isSite;
  }
  public void setIsSite(String isSite) {
    this.isSite = isSite;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("logoutURL")
  public String getLogoutURL() {
    return logoutURL;
  }
  public void setLogoutURL(String logoutURL) {
    this.logoutURL = logoutURL;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("businessOwnerName")
  public String getBusinessOwnerName() {
    return businessOwnerName;
  }
  public void setBusinessOwnerName(String businessOwnerName) {
    this.businessOwnerName = businessOwnerName;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("visibleTenants")
  public List<String> getVisibleTenants() {
    return visibleTenants;
  }
  public void setVisibleTenants(List<String> visibleTenants) {
    this.visibleTenants = visibleTenants;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("trackingCode")
  public String getTrackingCode() {
    return trackingCode;
  }
  public void setTrackingCode(String trackingCode) {
    this.trackingCode = trackingCode;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("skipGateway")
  public String getSkipGateway() {
    return skipGateway;
  }
  public void setSkipGateway(String skipGateway) {
    this.skipGateway = skipGateway;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("allowAnonymousAccess")
  public String getAllowAnonymousAccess() {
    return allowAnonymousAccess;
  }
  public void setAllowAnonymousAccess(String allowAnonymousAccess) {
    this.allowAnonymousAccess = allowAnonymousAccess;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("acsUrl")
  public String getAcsUrl() {
    return acsUrl;
  }
  public void setAcsUrl(String acsUrl) {
    this.acsUrl = acsUrl;
  }

  
  /**
   * If the provider value is not given user invoking the api will be used as the provider.
   **/
  @ApiModelProperty(value = "If the provider value is not given user invoking the api will be used as the provider.")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  
  /**
   * Swagger definition of the App which contains details about URI templates and scopes
   **/
  @ApiModelProperty(value = "Swagger definition of the App which contains details about URI templates and scopes")
  @JsonProperty("appDefinition")
  public String getAppDefinition() {
    return appDefinition;
  }
  public void setAppDefinition(String appDefinition) {
    this.appDefinition = appDefinition;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("isDefaultVersion")
  public Boolean getIsDefaultVersion() {
    return isDefaultVersion;
  }
  public void setIsDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
  }

  
  /**
   * Supported transports for the App (http and/or https).
   **/
  @ApiModelProperty(value = "Supported transports for the App (http and/or https).")
  @JsonProperty("transport")
  public String getTransport() {
    return transport;
  }
  public void setTransport(String transport) {
    this.transport = transport;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tags")
  public List<String> getTags() {
    return tags;
  }
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("thumbnailUrl")
  public String getThumbnailUrl() {
    return thumbnailUrl;
  }
  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("visibleRoles")
  public List<String> getVisibleRoles() {
    return visibleRoles;
  }
  public void setVisibleRoles(List<String> visibleRoles) {
    this.visibleRoles = visibleRoles;
  }

  
  /**
   * path of asset
   **/
  @ApiModelProperty(value = "path of asset")
  @JsonProperty("path")
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }

  
  /**
   * Resource Id path of the asset
   **/
  @ApiModelProperty(value = "Resource Id path of the asset")
  @JsonProperty("resourceId")
  public String getResourceId() {
    return resourceId;
  }
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  
  /**
   * lifecycle type of the asset
   **/
  @ApiModelProperty(value = "lifecycle type of the asset")
  @JsonProperty("lifecycle")
  public String getLifecycle() {
    return lifecycle;
  }
  public void setLifecycle(String lifecycle) {
    this.lifecycle = lifecycle;
  }

  
  /**
   * lifecycle state of the asset
   **/
  @ApiModelProperty(value = "lifecycle state of the asset")
  @JsonProperty("lifecycleState")
  public String getLifecycleState() {
    return lifecycleState;
  }
  public void setLifecycleState(String lifecycleState) {
    this.lifecycleState = lifecycleState;
  }

  
  /**
   * User rating for the app
   **/
  @ApiModelProperty(value = "User rating for the app")
  @JsonProperty("rating")
  public BigDecimal getRating() {
    return rating;
  }
  public void setRating(BigDecimal rating) {
    this.rating = rating;
  }

  
  /**
   * URL of the asset
   **/
  @ApiModelProperty(value = "URL of the asset")
  @JsonProperty("appUrL")
  public String getAppUrL() {
    return appUrL;
  }
  public void setAppUrL(String appUrL) {
    this.appUrL = appUrL;
  }

  
  /**
   * Bundleversion of the asset
   **/
  @ApiModelProperty(value = "Bundleversion of the asset")
  @JsonProperty("bundleversion")
  public String getBundleversion() {
    return bundleversion;
  }
  public void setBundleversion(String bundleversion) {
    this.bundleversion = bundleversion;
  }

  
  /**
   * category of the asset
   **/
  @ApiModelProperty(value = "category of the asset")
  @JsonProperty("category")
  public String getCategory() {
    return category;
  }
  public void setCategory(String category) {
    this.category = category;
  }

  
  /**
   * displayName of the asset
   **/
  @ApiModelProperty(required = true, value = "displayName of the asset")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  
  /**
   * Screenshots attached to the application
   **/
  @ApiModelProperty(value = "Screenshots attached to the application")
  @JsonProperty("screenshots")
  public List<String> getScreenshots() {
    return screenshots;
  }
  public void setScreenshots(List<String> screenshots) {
    this.screenshots = screenshots;
  }

  
  /**
   * /publisher/api/mobileapp/getfile/uWnObGDXigTO7pl.jpg
   **/
  @ApiModelProperty(value = "/publisher/api/mobileapp/getfile/uWnObGDXigTO7pl.jpg")
  @JsonProperty("banner")
  public String getBanner() {
    return banner;
  }
  public void setBanner(String banner) {
    this.banner = banner;
  }

  
  /**
   * createdtime of the asset
   **/
  @ApiModelProperty(value = "createdtime of the asset")
  @JsonProperty("createdtime")
  public String getCreatedtime() {
    return createdtime;
  }
  public void setCreatedtime(String createdtime) {
    this.createdtime = createdtime;
  }

  
  /**
   * platform of the asset
   **/
  @ApiModelProperty(value = "platform of the asset")
  @JsonProperty("platform")
  public String getPlatform() {
    return platform;
  }
  public void setPlatform(String platform) {
    this.platform = platform;
  }

  
  /**
   * mobile app type (eg: webapp/ios/android)
   **/
  @ApiModelProperty(value = "mobile app type (eg: webapp/ios/android)")
  @JsonProperty("appType")
  public String getAppType() {
    return appType;
  }
  public void setAppType(String appType) {
    this.appType = appType;
  }

  
  /**
   * media yype
   **/
  @ApiModelProperty(value = "media yype")
  @JsonProperty("mediaType")
  public String getMediaType() {
    return mediaType;
  }
  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  
  /**
   * platform of the asset
   **/
  @ApiModelProperty(value = "platform of the asset")
  @JsonProperty("lifecycleAvailableActions")
  public String getLifecycleAvailableActions() {
    return lifecycleAvailableActions;
  }
  public void setLifecycleAvailableActions(String lifecycleAvailableActions) {
    this.lifecycleAvailableActions = lifecycleAvailableActions;
  }

  
  /**
   * asset created timestamp
   **/
  @ApiModelProperty(value = "asset created timestamp")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppDTO {\n");
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("  appmeta: ").append(appmeta).append("\n");
    sb.append("  uriTemplates: ").append(uriTemplates).append("\n");
    sb.append("  policyGroups: ").append(policyGroups).append("\n");
    sb.append("  marketType: ").append(marketType).append("\n");
    sb.append("  recentChanges: ").append(recentChanges).append("\n");
    sb.append("  icon: ").append(icon).append("\n");
    sb.append("  isSite: ").append(isSite).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  context: ").append(context).append("\n");
    sb.append("  version: ").append(version).append("\n");
    sb.append("  logoutURL: ").append(logoutURL).append("\n");
    sb.append("  businessOwnerName: ").append(businessOwnerName).append("\n");
    sb.append("  visibleTenants: ").append(visibleTenants).append("\n");
    sb.append("  trackingCode: ").append(trackingCode).append("\n");
    sb.append("  skipGateway: ").append(skipGateway).append("\n");
    sb.append("  allowAnonymousAccess: ").append(allowAnonymousAccess).append("\n");
    sb.append("  acsUrl: ").append(acsUrl).append("\n");
    sb.append("  provider: ").append(provider).append("\n");
    sb.append("  appDefinition: ").append(appDefinition).append("\n");
    sb.append("  isDefaultVersion: ").append(isDefaultVersion).append("\n");
    sb.append("  transport: ").append(transport).append("\n");
    sb.append("  tags: ").append(tags).append("\n");
    sb.append("  thumbnailUrl: ").append(thumbnailUrl).append("\n");
    sb.append("  visibleRoles: ").append(visibleRoles).append("\n");
    sb.append("  path: ").append(path).append("\n");
    sb.append("  resourceId: ").append(resourceId).append("\n");
    sb.append("  lifecycle: ").append(lifecycle).append("\n");
    sb.append("  lifecycleState: ").append(lifecycleState).append("\n");
    sb.append("  rating: ").append(rating).append("\n");
    sb.append("  appUrL: ").append(appUrL).append("\n");
    sb.append("  bundleversion: ").append(bundleversion).append("\n");
    sb.append("  category: ").append(category).append("\n");
    sb.append("  displayName: ").append(displayName).append("\n");
    sb.append("  screenshots: ").append(screenshots).append("\n");
    sb.append("  banner: ").append(banner).append("\n");
    sb.append("  createdtime: ").append(createdtime).append("\n");
    sb.append("  platform: ").append(platform).append("\n");
    sb.append("  appType: ").append(appType).append("\n");
    sb.append("  mediaType: ").append(mediaType).append("\n");
    sb.append("  lifecycleAvailableActions: ").append(lifecycleAvailableActions).append("\n");
    sb.append("  createdTime: ").append(createdTime).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
