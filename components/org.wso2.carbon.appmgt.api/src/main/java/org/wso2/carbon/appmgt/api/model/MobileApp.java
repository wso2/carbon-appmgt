/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.List;

/**
 * This class is used to store and process mobile application details
 */
public class MobileApp {

    private String appName;
    private String appId;
    private String appVersion;
    private String appProvider;
    private String category;
    private String platform;
    private String marketType;
    private String appType;
    private String banner;
    private String description;
    private String displayName;
    private String screenshot1;
    private String screenshot2;
    private String screenshot3;
    private String icon;
    private String bundleVersion;
    private String packageName;
    private String[] availableLifecycleActions;
    private String recentChanges;
    private String thumbnail;
    private APIStatus lifecycleStatus;
    private List<String> screenShots;
    private String appUrl;
    private List<String> visibility;

    public List<String> getVisibility() {
        return visibility;
    }

    public void setVisibility(List<String> visibility) {
        this.visibility = visibility;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public APIStatus getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(APIStatus lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppProvider() {
        return appProvider;
    }

    public void setAppProvider(String appProvider) {
        this.appProvider = appProvider;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getMarketType() {
        return marketType;
    }

    public void setMarketType(String marketType) {
        this.marketType = marketType;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getScreenshot1() {
        return screenshot1;
    }

    public void setScreenshot1(String screenshot1) {
        this.screenshot1 = screenshot1;
    }

    public String getScreenshot2() {
        return screenshot2;
    }

    public void setScreenshot2(String screenshot2) {
        this.screenshot2 = screenshot2;
    }

    public String getScreenshot3() {
        return screenshot3;
    }

    public void setScreenshot3(String screenshot3) {
        this.screenshot3 = screenshot3;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBundleVersion() {
        return bundleVersion;
    }

    public void setBundleVersion(String bundleVersion) {
        this.bundleVersion = bundleVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String[] getAvailableLifecycleActions() {
        return availableLifecycleActions;
    }

    public void setAvailableLifecycleActions(String[] availableLifecycleActions) {
        this.availableLifecycleActions = availableLifecycleActions;
    }

    public String getRecentChanges() {
        return recentChanges;
    }

    public void setRecentChanges(String recentChanges) {
        this.recentChanges = recentChanges;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<String> getScreenShots() {
        return screenShots;
    }

    public void setScreenShots(List<String> screenShots) {
        this.screenShots = screenShots;
    }
}
