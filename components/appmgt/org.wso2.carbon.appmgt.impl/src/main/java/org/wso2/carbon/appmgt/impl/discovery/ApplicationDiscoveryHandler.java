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

import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationDTO;
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationListDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.Locale;

/**
 * Definition of application discovery handler
 *
 */
public interface ApplicationDiscoveryHandler {

    public enum APP_STATUS {ANY, CREATED, NEW}

    String getDisplayName();

    /**
     * Discover applications given the credentials and the search criteria
     *
     * @param credentials
     * @param criteria
     * @return the bean holding the list of paged list of applications
     */
    DiscoveredApplicationListDTO discoverApplications(ApplicationDiscoveryContext discoveryContext,
            DiscoveryCredentials credentials, DiscoverySearchCriteria criteria, Locale locale,
            PrivilegedCarbonContext carbonContext) throws AppManagementException;

    /**
     * Reads the application information necessary to create a new proxy application
     *
     * @param apiIdentifier
     * @param carbonContext
     * @return
     * @throws AppManagementException
     */
    DiscoveredApplicationDTO readApplicationInfo(ApplicationDiscoveryContext discoveryContext,
            APIIdentifier apiIdentifier, PrivilegedCarbonContext carbonContext)
            throws AppManagementException;
}
