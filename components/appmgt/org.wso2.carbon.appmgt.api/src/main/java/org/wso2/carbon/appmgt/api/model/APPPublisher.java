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
package org.wso2.carbon.appmgt.api.model;

import org.wso2.carbon.appmgt.api.AppManagementException;

/**
 * One or more implementations of this interface can be used to publish APIs to APIStores .
 */
public interface APPPublisher {
    /**
     * The method to publish WebApp to external Store
     * @param webApp      WebApp
     * @param store    Store
     * @return   published/not
     */
    public boolean publishToStore(WebApp webApp,APPStore store) throws AppManagementException;


    /**
     * The method to publish WebApp to external Store
     * @param webApp      APIIdentifier
     * @param store    Store
     * @return   deleted/not
     */
    public boolean deleteFromStore(WebApp webApp,APPStore store) throws
                                                                       AppManagementException;

}