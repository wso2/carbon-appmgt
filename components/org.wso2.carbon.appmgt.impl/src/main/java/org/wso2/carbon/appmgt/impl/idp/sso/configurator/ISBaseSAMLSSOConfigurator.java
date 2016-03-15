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

package org.wso2.carbon.appmgt.impl.idp.sso.configurator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.model.SSOProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;

/**
 * Base class used for IS SAML configuration
 */
public abstract class ISBaseSAMLSSOConfigurator {

    private static Log log = LogFactory.getLog(ISBaseSAMLSSOConfigurator.class);

    protected static String SERVER_URL = "providerURL";
    protected static String PASSWORD = "password";
    protected static String USERNAME = "username";

    /**
     * Translates the SSOProvider to SAMLSSOServiceProviderDTO.
     *
     * @param provider the Single Sig On Provider.
     * @return translated SAMLSSOServiceProviderDTO
     */
    protected SAMLSSOServiceProviderDTO generateDTO(SSOProvider provider) {
        SAMLSSOServiceProviderDTO dto = new SAMLSSOServiceProviderDTO();

        dto.setIssuer(provider.getIssuerName());
        dto.setAssertionConsumerUrls(new String[]{provider.getAssertionConsumerURL()});
        dto.setDefaultAssertionConsumerUrl(provider.getAssertionConsumerURL());
        dto.setCertAlias(null);

        dto.setNameIDFormat(provider.getNameIdFormat());
        if (dto.getNameIDFormat() != null) {
            dto.setNameIDFormat(dto.getNameIDFormat().replace(":", "/"));
        }

        dto.setDoSingleLogout(true);
        dto.setRequestedClaims(provider.getClaims());
        dto.setEnableAttributesByDefault(true);

        dto.setEnableAttributeProfile(true);

        dto.setIdPInitSSOEnabled(true);

        return dto;
    }
}
