/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Parent class for apps.
 */
public class App {

    private String type;
    private String uuid;
    private String displayName;
    private String name;
    private String version;
    private float rating;
    private String[] appVisibility;
    private String banner;
    private String lifeCycleName;
    private APIStatus lifeCycleStatus;
    private List<CustomProperty> customProperties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String[] getAppVisibility() {
        return appVisibility;
    }

    public void setAppVisibility(String[] appVisibility) {
        this.appVisibility = appVisibility;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUUID(String uuid){
        this.uuid =  uuid;
    }

    public String getUUID(){
        return  uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public void setLifeCycleName(String lifeCycleName) {
        this.lifeCycleName = lifeCycleName;
    }

    public String getLifeCycleName() {
        return lifeCycleName;
    }

    public void setLifeCycleStatus(APIStatus lifeCycleStatus) {
        this.lifeCycleStatus = lifeCycleStatus;
    }

    public APIStatus getLifeCycleStatus() {
        return lifeCycleStatus;
    }

    public List<CustomProperty> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(List<CustomProperty> customProperties) {
        this.customProperties = customProperties;
    }

    public void addCustomProperty(CustomProperty customProperty){

        if(customProperties == null){
            customProperties = new ArrayList<>();
        }

        customProperties.add(customProperty);
    }

    public void addCustomProperty(String name, String value){
        CustomProperty customProperty = new CustomProperty(name, value);
        addCustomProperty(customProperty);
    }
}
