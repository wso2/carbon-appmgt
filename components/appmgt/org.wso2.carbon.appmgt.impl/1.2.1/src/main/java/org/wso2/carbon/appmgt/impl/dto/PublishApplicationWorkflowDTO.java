/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl.dto;

/**
 * Application DTO class for workflow 
 * 
 */

public class PublishApplicationWorkflowDTO extends WorkflowDTO {

    private String appName;
    private String appVersion;
    private String appProvider;
    private String lcState;
    private String newState;
    private  int tenantID;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getLcState() {
        return lcState;
    }

    public void setLcState(String lcState) {
        this.lcState = lcState;
    }

    public  void setNewState(String newState){
        this.newState = newState;
    }

    public String getNewState(){
        return newState;
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

    public int getTenantId() {
        return tenantID;
    }

    public void setTenantId(int tenantID) {
        this.tenantID = tenantID;
    }
}
