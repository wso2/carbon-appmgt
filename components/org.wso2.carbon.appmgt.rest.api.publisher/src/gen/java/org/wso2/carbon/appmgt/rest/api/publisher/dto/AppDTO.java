package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class AppDTO {

    private String id = null;
    private String name = null;
    private String type = null;
    private String isSite = null;
    private String description = null;
    private String context = null;
    private String version = null;
    private String provider = null;
    private String appDefinition = null;
    private Boolean isDefaultVersion = null;
    private List<String> transport = new ArrayList<String>();
    private List<String> tags = new ArrayList<String>();
    private String thumbnailUrl = null;
    private List<String> visibleRoles = new ArrayList<String>();
    private String path = null;
    private String resourceId = null;
    private String lifecycle = null;
    private String lifecycleState = null;
    private String appUrL = null;
    private String bundleversion = null;
    private String packagename = null;
    private String category = null;
    private String displayName = null;
    private String screenshots = null;
    private String banner = null;
    private String createdtime = null;
    private String platform = null;
    private String lifecycleAvailableActions = null;


    /**
     * UUID of the app registry artifact\n
     */
    public AppDTO id(String id) {
        this.id = id;
        return this;
    }


    @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the app registry artifact\n")
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    /**
     **/
    public AppDTO name(String name) {
        this.name = name;
        return this;
    }


    @ApiModelProperty(example = "CalculatorApp", required = true, value = "")
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * App type (either Webapp/Mobile app
     */
    public AppDTO type(String type) {
        this.type = type;
        return this;
    }


    @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "App type (either Webapp/Mobile app")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    /**
     * Either a webapp or site
     */
    public AppDTO isSite(String isSite) {
        this.isSite = isSite;
        return this;
    }


    @ApiModelProperty(example = "for sites - TRUE", value = "Either a webapp or site")
    @JsonProperty("isSite")
    public String getIsSite() {
        return isSite;
    }

    public void setIsSite(String isSite) {
        this.isSite = isSite;
    }


    /**
     **/
    public AppDTO description(String description) {
        this.description = description;
        return this;
    }


    @ApiModelProperty(example = "A calculator App that supports basic operations", value = "")
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    /**
     **/
    public AppDTO context(String context) {
        this.context = context;
        return this;
    }


    @ApiModelProperty(example = "CalculatorApp", required = true, value = "")
    @JsonProperty("context")
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }


    /**
     **/
    public AppDTO version(String version) {
        this.version = version;
        return this;
    }


    @ApiModelProperty(example = "1.0.0", required = true, value = "")
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    /**
     * If the provider value is not given user invoking the api will be used as the provider.\n
     */
    public AppDTO provider(String provider) {
        this.provider = provider;
        return this;
    }


    @ApiModelProperty(example = "admin",
                      value = "If the provider value is not given user invoking the api will be used as the provider" +
                              ".\n")
    @JsonProperty("provider")
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }


    /**
     * Swagger definition of the App which contains details about URI templates and scopes\n
     */
    public AppDTO appDefinition(String appDefinition) {
        this.appDefinition = appDefinition;
        return this;
    }


    @ApiModelProperty(required = true,
                      value = "Swagger definition of the App which contains details about URI templates and scopes\n")
    @JsonProperty("appDefinition")
    public String getAppDefinition() {
        return appDefinition;
    }

    public void setAppDefinition(String appDefinition) {
        this.appDefinition = appDefinition;
    }


    /**
     **/
    public AppDTO isDefaultVersion(Boolean isDefaultVersion) {
        this.isDefaultVersion = isDefaultVersion;
        return this;
    }


    @ApiModelProperty(example = "false", value = "")
    @JsonProperty("isDefaultVersion")
    public Boolean getIsDefaultVersion() {
        return isDefaultVersion;
    }

    public void setIsDefaultVersion(Boolean isDefaultVersion) {
        this.isDefaultVersion = isDefaultVersion;
    }


    /**
     * Supported transports for the App (http and/or https).\n
     */
    public AppDTO transport(List<String> transport) {
        this.transport = transport;
        return this;
    }


    @ApiModelProperty(required = true, value = "Supported transports for the App (http and/or https).\n")
    @JsonProperty("transport")
    public List<String> getTransport() {
        return transport;
    }

    public void setTransport(List<String> transport) {
        this.transport = transport;
    }


    /**
     **/
    public AppDTO tags(List<String> tags) {
        this.tags = tags;
        return this;
    }


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
    public AppDTO thumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }


    @ApiModelProperty(example = "", value = "")
    @JsonProperty("thumbnailUrl")
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }


    /**
     **/
    public AppDTO visibleRoles(List<String> visibleRoles) {
        this.visibleRoles = visibleRoles;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("visibleRoles")
    public List<String> getVisibleRoles() {
        return visibleRoles;
    }

    public void setVisibleRoles(List<String> visibleRoles) {
        this.visibleRoles = visibleRoles;
    }


    /**
     * Registry path of the asset\n
     */
    public AppDTO path(String path) {
        this.path = path;
        return this;
    }


    @ApiModelProperty(example = "/_system/governance/mobileapps/admin/android/test/1.0",
                      value = "Registry path of the asset\n")
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    /**
     * Resource Id path of the asset\n
     */
    public AppDTO resourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }


    @ApiModelProperty(example = "/_system/governance/mobileapps/admin/android/test/1.0",
                      value = "Resource Id path of the asset\n")
    @JsonProperty("resourceId")
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }


    /**
     * lifecycle type of the asset\n
     */
    public AppDTO lifecycle(String lifecycle) {
        this.lifecycle = lifecycle;
        return this;
    }


    @ApiModelProperty(example = "MobileAppLifeCycle", value = "lifecycle type of the asset\n")
    @JsonProperty("lifecycle")
    public String getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(String lifecycle) {
        this.lifecycle = lifecycle;
    }


    /**
     * lifecycle state of the asset\n
     */
    public AppDTO lifecycleState(String lifecycleState) {
        this.lifecycleState = lifecycleState;
        return this;
    }


    @ApiModelProperty(example = "Created", value = "lifecycle state of the asset\n")
    @JsonProperty("lifecycleState")
    public String getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(String lifecycleState) {
        this.lifecycleState = lifecycleState;
    }


    /**
     * URL of the asset\n
     */
    public AppDTO appUrL(String appUrL) {
        this.appUrL = appUrL;
        return this;
    }


    @ApiModelProperty(example = "/publisher/api/mobileapp/getfile/WKzeMgMhOrGvwTy.apk", value = "URL of the asset\n")
    @JsonProperty("appUrL")
    public String getAppUrL() {
        return appUrL;
    }

    public void setAppUrL(String appUrL) {
        this.appUrL = appUrL;
    }


    /**
     * Bundleversion of the asset\n
     */
    public AppDTO bundleversion(String bundleversion) {
        this.bundleversion = bundleversion;
        return this;
    }


    @ApiModelProperty(example = "1.0", value = "Bundleversion of the asset\n")
    @JsonProperty("bundleversion")
    public String getBundleversion() {
        return bundleversion;
    }

    public void setBundleversion(String bundleversion) {
        this.bundleversion = bundleversion;
    }


    /**
     * packagename of the asset\n
     */
    public AppDTO packagename(String packagename) {
        this.packagename = packagename;
        return this;
    }


    @ApiModelProperty(example = "home.jmstudios.calc", value = "packagename of the asset\n")
    @JsonProperty("packagename")
    public String getPackagename() {
        return packagename;
    }

    public void setPackagename(String packagename) {
        this.packagename = packagename;
    }


    /**
     * category of the asset\n
     */
    public AppDTO category(String category) {
        this.category = category;
        return this;
    }


    @ApiModelProperty(example = "Business", value = "category of the asset\n")
    @JsonProperty("category")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    /**
     * displayName of the asset\n
     */
    public AppDTO displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }


    @ApiModelProperty(example = "testapp1", value = "displayName of the asset\n")
    @JsonProperty("displayName")
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    /**
     * packagename of the asset\n
     */
    public AppDTO screenshots(String screenshots) {
        this.screenshots = screenshots;
        return this;
    }


    @ApiModelProperty(
            example = "/publisher/api/mobileapp/getfile/TJXfaEeHsdeYSFS.jpg," +
                    "/publisher/api/mobileapp/getfile/aCJ1MXUXWGGHS3t.JPG," +
                    "/publisher/api/mobileapp/getfile/FcFEWkaLroetXlq.JPG",
            value = "packagename of the asset\n")
    @JsonProperty("screenshots")
    public String getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(String screenshots) {
        this.screenshots = screenshots;
    }


    /**
     * /publisher/api/mobileapp/getfile/uWnObGDXigTO7pl.jpg\n
     */
    public AppDTO banner(String banner) {
        this.banner = banner;
        return this;
    }


    @ApiModelProperty(
            example = "/publisher/api/mobileapp/getfile/TJXfaEeHsdeYSFS.jpg," +
                    "/publisher/api/mobileapp/getfile/aCJ1MXUXWGGHS3t.JPG," +
                    "/publisher/api/mobileapp/getfile/FcFEWkaLroetXlq.JPG",
            value = "/publisher/api/mobileapp/getfile/uWnObGDXigTO7pl.jpg\n")
    @JsonProperty("banner")
    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }


    /**
     * createdtime of the asset\n
     */
    public AppDTO createdtime(String createdtime) {
        this.createdtime = createdtime;
        return this;
    }


    @ApiModelProperty(example = "00000001458300149192", value = "createdtime of the asset\n")
    @JsonProperty("createdtime")
    public String getCreatedtime() {
        return createdtime;
    }

    public void setCreatedtime(String createdtime) {
        this.createdtime = createdtime;
    }


    /**
     * platform of the asset\n
     */
    public AppDTO platform(String platform) {
        this.platform = platform;
        return this;
    }


    @ApiModelProperty(example = "andoid", value = "platform of the asset\n")
    @JsonProperty("platform")
    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }


    /**
     * platform of the asset\n
     */
    public AppDTO lifecycleAvailableActions(String lifecycleAvailableActions) {
        this.lifecycleAvailableActions = lifecycleAvailableActions;
        return this;
    }


    @ApiModelProperty(example = "[Submit for Review]", value = "platform of the asset\n")
    @JsonProperty("lifecycleAvailableActions")
    public String getLifecycleAvailableActions() {
        return lifecycleAvailableActions;
    }

    public void setLifecycleAvailableActions(String lifecycleAvailableActions) {
        this.lifecycleAvailableActions = lifecycleAvailableActions;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppDTO app = (AppDTO) o;
        return Objects.equals(id, app.id) &&
                Objects.equals(name, app.name) &&
                Objects.equals(type, app.type) &&
                Objects.equals(isSite, app.isSite) &&
                Objects.equals(description, app.description) &&
                Objects.equals(context, app.context) &&
                Objects.equals(version, app.version) &&
                Objects.equals(provider, app.provider) &&
                Objects.equals(appDefinition, app.appDefinition) &&
                Objects.equals(isDefaultVersion, app.isDefaultVersion) &&
                Objects.equals(transport, app.transport) &&
                Objects.equals(tags, app.tags) &&
                Objects.equals(thumbnailUrl, app.thumbnailUrl) &&
                Objects.equals(visibleRoles, app.visibleRoles) &&
                Objects.equals(path, app.path) &&
                Objects.equals(resourceId, app.resourceId) &&
                Objects.equals(lifecycle, app.lifecycle) &&
                Objects.equals(lifecycleState, app.lifecycleState) &&
                Objects.equals(appUrL, app.appUrL) &&
                Objects.equals(bundleversion, app.bundleversion) &&
                Objects.equals(packagename, app.packagename) &&
                Objects.equals(category, app.category) &&
                Objects.equals(displayName, app.displayName) &&
                Objects.equals(screenshots, app.screenshots) &&
                Objects.equals(banner, app.banner) &&
                Objects.equals(createdtime, app.createdtime) &&
                Objects.equals(platform, app.platform) &&
                Objects.equals(lifecycleAvailableActions, app.lifecycleAvailableActions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, isSite, description, context, version, provider, appDefinition,
                            isDefaultVersion, transport, tags, thumbnailUrl, visibleRoles, path, resourceId, lifecycle,
                            lifecycleState, appUrL, bundleversion, packagename, category, displayName, screenshots,
                            banner, createdtime, platform, lifecycleAvailableActions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class App {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    isSite: ").append(toIndentedString(isSite)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    context: ").append(toIndentedString(context)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
        sb.append("    appDefinition: ").append(toIndentedString(appDefinition)).append("\n");
        sb.append("    isDefaultVersion: ").append(toIndentedString(isDefaultVersion)).append("\n");
        sb.append("    transport: ").append(toIndentedString(transport)).append("\n");
        sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
        sb.append("    thumbnailUrl: ").append(toIndentedString(thumbnailUrl)).append("\n");
        sb.append("    visibleRoles: ").append(toIndentedString(visibleRoles)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    resourceId: ").append(toIndentedString(resourceId)).append("\n");
        sb.append("    lifecycle: ").append(toIndentedString(lifecycle)).append("\n");
        sb.append("    lifecycleState: ").append(toIndentedString(lifecycleState)).append("\n");
        sb.append("    appUrL: ").append(toIndentedString(appUrL)).append("\n");
        sb.append("    bundleversion: ").append(toIndentedString(bundleversion)).append("\n");
        sb.append("    packagename: ").append(toIndentedString(packagename)).append("\n");
        sb.append("    category: ").append(toIndentedString(category)).append("\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    screenshots: ").append(toIndentedString(screenshots)).append("\n");
        sb.append("    banner: ").append(toIndentedString(banner)).append("\n");
        sb.append("    createdtime: ").append(toIndentedString(createdtime)).append("\n");
        sb.append("    platform: ").append(toIndentedString(platform)).append("\n");
        sb.append("    lifecycleAvailableActions: ").append(toIndentedString(lifecycleAvailableActions)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

