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
package org.wso2.carbon.appmgt.core.authenticate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.appmgt.core.internal.ServiceReferenceHolder;


public class APITokenValidator {

    private static final Log log = LogFactory.getLog(APITokenValidator.class);

    public APIKeyValidationInfoDTO validateKey(String context, String version, String accessToken, String requiredAuthenticationLevel,
                                               String clientDomain) throws AppManagementException {
        AppMDAO appMDAO = new AppMDAO();
        return appMDAO.validateKey(context, version, accessToken, requiredAuthenticationLevel);
    }

    public static String getAPIManagerClientDomainHeader() {
        AppManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        return config.getFirstProperty(AppMConstants.API_GATEWAY_CLIENT_DOMAIN_HEADER);
    }
}