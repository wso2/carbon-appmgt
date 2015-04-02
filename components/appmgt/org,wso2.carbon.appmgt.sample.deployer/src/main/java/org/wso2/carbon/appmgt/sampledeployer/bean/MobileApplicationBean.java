package org.wso2.carbon.appmgt.sampledeployer.bean;

/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class MobileApplicationBean {

    private String appmeta;
    private String version;
    private String provider;
    private String markettype;
    private String platform;
    private String name;
    private String description;
    private String recentChanges;
    private String bannerFilePath;
    private String sso_ssoProvider = "wso2is-5.0.0";
    private String iconFile;
    private String screenShot1File;
    private String screenShot2File;
    private String screenShot3File;
    private String mobileapp;

    private String apkFile;

    public MobileApplicationBean(){

    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getMarkettype() {
        return markettype;
    }

    public void setMarkettype(String markettype) {
        this.markettype = markettype;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {return description;}

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBannerFilePath() {
        return bannerFilePath;
    }

    public void setBannerFilePath(String bannerFilePath) {
        this.bannerFilePath = bannerFilePath;
    }

    public String getIconFile() {
        return iconFile;
    }

    public void setIconFile(String iconFile) {
        this.iconFile = iconFile;
    }

    public String getScreenShot1File() {
        return screenShot1File;
    }

    public void setScreenShot1File(String screenShot1File) {
        this.screenShot1File = screenShot1File;
    }

    public String getScreenShot2File() {
        return screenShot2File;
    }

    public void setScreenShot2File(String screenShot2File) {
        this.screenShot2File = screenShot2File;
    }

    public String getScreenShot3File() {
        return screenShot3File;
    }

    public void setScreenShot3File(String screenShot3File) {
        this.screenShot3File = screenShot3File;
    }

    public String getMobileapp() {
        return mobileapp;
    }

    public void setMobileapp(String mobileapp) {
        this.mobileapp = mobileapp;
    }

    public String getAppmeta() {
        return appmeta;
    }

    public void setAppmeta(String appmeta) {
        this.appmeta = appmeta;
    }

    public String getApkFile() {
        return apkFile;
    }

    public void setApkFile(String apkFile) {
        this.apkFile = apkFile;
    }


    public String getSso_ssoProvider() {
        return sso_ssoProvider;
    }

    public void setSso_ssoProvider(String sso_ssoProvider) {
        this.sso_ssoProvider = sso_ssoProvider;
    }
}
