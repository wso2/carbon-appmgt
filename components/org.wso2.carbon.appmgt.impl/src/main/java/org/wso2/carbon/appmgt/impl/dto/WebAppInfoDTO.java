/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl.dto;

import java.util.Set;

public class WebAppInfoDTO {

    private String providerId;
    private String webAppName;
    private String version;
    private String context;
    private String idpProviderUrl;
    private String saml2SsoIssuer;
    private int appID;
    private Boolean allowAnonymous;

    public int getAppID(){return appID;}
    public void setAppID(int appID) {this.appID=appID;}

    private Set<ResourceInfoDTO> resources;
    private String logoutUrl;

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getWebAppName() {
        return webAppName;
    }

    public void setWebAppName(String webAppName) {
        this.webAppName = webAppName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Set<ResourceInfoDTO> getResources() {
        return resources;
    }

    public void setResources(Set<ResourceInfoDTO> resources) {
        this.resources = resources;
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

    /**
     * Computes WebApp Identifier using Provider Id, WebApp Name & Version
     *
     * @return WebApp Identifier as a String
     */
    public String getAPIIdentifier() {
        if (providerId != null && webAppName != null && version != null) {
            return providerId + "_" + webAppName + "_" + version;
        } else {
            return null;
        }
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public Boolean getAllowAnonymous() {
        return allowAnonymous;
    }

    public void setAllowAnonymous(Boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }
}
