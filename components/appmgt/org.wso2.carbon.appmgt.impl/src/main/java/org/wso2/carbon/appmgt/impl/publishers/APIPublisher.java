/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.appmgt.impl.publishers;

import org.wso2.carbon.appmgt.api.APIManagementException;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.APIStore;

/**
 * One or more implementations of this interface can be used to publish APIs to APIStores .
 */
public interface APIPublisher {
    /**
     * The method to publish WebApp to external Store
     * @param api      WebApp
     * @param store    Store
     * @return   published/not
     */
    public boolean publishToStore(WebApp api,APIStore store) throws APIManagementException;

    /**
     * The method to publish WebApp to external Store
     * @param api      WebApp
     * @param store    Store
     * @return   updated/not
     */
    public boolean updateToStore(WebApp api,APIStore store) throws APIManagementException;

    /**
     * The method to publish WebApp to external Store
     * @param apiId      APIIdentifier
     * @param store    Store
     * @return   deleted/not
     */
    public boolean deleteFromStore(APIIdentifier apiId,APIStore store) throws APIManagementException;
}