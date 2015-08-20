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

package org.wso2.carbon.appmgt.impl.token;

import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.dto.APIKeyValidationInfoDTO;

import java.util.Map;

/**
 * This interface can be used to Generate a Token with the invoking user's details.
 */
public interface TokenGenerator {

    /**
     * @deprecated
     * This method is no longer need for APPM since this method is related to APIM keymanager implementations.
     * Since AppMDAO class referring this method, this method is declared in here. When we removing keymanager
     * implementations we need to refactor this method and its implementation as well.
     */
    @Deprecated
    public String generateToken(APIKeyValidationInfoDTO keyValidationInfoDTO, String apiContext,
                                String version, boolean includeEndUserName) throws AppManagementException;

    public String generateToken(Map<String, Object> saml2Assertions, String apiContext, String version) throws
            AppManagementException;
}
