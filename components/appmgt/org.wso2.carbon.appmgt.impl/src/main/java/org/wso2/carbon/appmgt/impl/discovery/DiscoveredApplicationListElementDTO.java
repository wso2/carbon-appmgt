/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl.discovery;

import java.util.Map;

/**
 * Data Transfer Object which sends discovered application list (web or mobile).
 * The list element is a absolute minimal information needed for listing purposes, but wull not
 * have full information needed to create an application.
 * This is mainly used in order to reduce data being transferred.
 * This will contain all the necessary data to create an application/proxy withing AppM.
 */
public class DiscoveredApplicationListElementDTO {
    private String applicationId;

    /* Server Type e.g. WSO2 AS, Tomcat, MDM */
    private String serverType;

    /* Application Type {webapp, mobileapp}*/
    private String applicationType;
    private String applicationName;
    private String displayName;
    private String version;
    private String proxyContext;
    private String remoteContext;
    private String remoteVersion;
    private String remoteHost;
    private String status;
    private String applicationUrl;
    private String applicationPreviewUrl;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private Map<String, Integer> portMap;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProxyContext() {
        return proxyContext;
    }

    public void setProxyContext(String proxyContext) {
        this.proxyContext = proxyContext;
    }

    public String getRemoteContext() {
        return remoteContext;
    }

    public void setRemoteContext(String remoteContext) {
        this.remoteContext = remoteContext;
    }

    public String getRemoteVersion() {
        return remoteVersion;
    }

    public void setRemoteVersion(String remoteVersion) {
        this.remoteVersion = remoteVersion;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public Map<String, Integer> getPortMap() {
        return portMap;
    }

    public void setPortMap(Map<String, Integer> portMap) {
        this.portMap = portMap;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public String getApplicationPreviewUrl() {
        return applicationPreviewUrl;
    }

    public void setApplicationPreviewUrl(String applicationPreviewUrl) {
        this.applicationPreviewUrl = applicationPreviewUrl;
    }
}
