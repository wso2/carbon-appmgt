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

package org.wso2.carbon.appmgt.services.api.v1.apps.discovery.service;

import org.wso2.carbon.appmgt.impl.discovery.DiscoverySearchCriteria;
import org.wso2.carbon.appmgt.impl.discovery.UserNamePasswordCredentials;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Application Discover JAX-RS request
 */
public class DiscoverRequest {

    @XmlElement (name = "credentials")
    private UserNamePasswordCredentials credentials;

    @XmlElement (name = "searchCriteria")
    private DiscoverySearchCriteria searchCriteria;

    public UserNamePasswordCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(UserNamePasswordCredentials credentials) {
        this.credentials = credentials;
    }

    public DiscoverySearchCriteria getSearchCriteria() {
        return searchCriteria;
    }

    public void setSearchCriteria(DiscoverySearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }
}
