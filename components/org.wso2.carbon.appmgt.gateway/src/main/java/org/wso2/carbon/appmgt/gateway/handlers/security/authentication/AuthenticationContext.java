/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   you may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.appmgt.gateway.handlers.security.authentication;

import org.wso2.carbon.appmgt.api.model.AuthenticatedIDP;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents an authenticated user and the context.
 */
public class AuthenticationContext {

    private String tenantDomain;
    private boolean authenticated;
    private List<AuthenticatedIDP> authenticatedIDPs;
    private String subject;
    private List<String> roles;
    private Map<String, Object> attributes;

    public AuthenticationContext() {
        setAuthenticated(false);
        roles = new ArrayList<String>();
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public void setSubject(String subject) {
        if(subject != null){
            setAuthenticated(true);
            this.subject = subject;
        }
    }

    public String getSubject() {
        return subject;
    }

    public void setAuthenticatedIDPs(List<AuthenticatedIDP> authenticatedIDPs) {
        this.authenticatedIDPs = authenticatedIDPs;
    }

    public List<AuthenticatedIDP> getAuthenticatedIDPs() {
        return authenticatedIDPs;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void addRole(String role) {
        roles.add(role);
    }
}
