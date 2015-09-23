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

package org.wso2.carbon.appmgt.gateway.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.AppManagerConfigurationService;
import org.wso2.carbon.appmgt.impl.token.TokenGenerator;

import java.util.HashSet;
import java.util.Set;

/**
 * Initializes the SAML2Authentication handler, with relevant services.
 *
 * @scr.component name="org.wso2.appmgt.impl.services.gateway.saml.authentication.component" immediate="true"
 * @scr.reference name="app.manager.jwt.token,generator"
 * interface="org.wso2.carbon.appmgt.impl.token.TokenGenerator" cardinality="1..1"
 * policy="dynamic" bind="setTokenGenerator" unbind="unsetTokenGenerator"
 * @scr.reference name="app.manager.config.service"
 * interface="org.wso2.carbon.appmgt.impl.AppManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAppManagerConfigurationService" unbind="unsetAppManagerConfigurationService"
 * @see org.wso2.carbon.appmgt.gateway.handlers.security.saml2.SAML2AuthenticationHandler
 */
public class SAML2AuthenticationComponent {

    private static final Log log = LogFactory.getLog(SAML2AuthenticationComponent.class);

    private String tokenGeneratorImplClazz;
    private TokenGenerator tokenGenerator;
    private Set<TokenGenerator> tokenGenerators = new HashSet<>();
    private AppManagerConfiguration configuration = null;

    protected void activate(ComponentContext componentContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Gateway token generator register component activated");
        }
        if (configuration == null) {
            throw new AppManagementException(
                    "The AppManagerConfiguration is not yet set. Delaying the component activation");
        }
        //Find the token generator which is proffered in the configuration
        tokenGeneratorImplClazz = configuration.getFirstProperty(AppMConstants.TOKEN_GENERATOR_IMPL);
        for (TokenGenerator tempTokenGenerator : tokenGenerators) {
            if (tempTokenGenerator.getClass().getName().equals(tokenGeneratorImplClazz)) {
                tokenGenerator = tempTokenGenerator;
            }
        }
        if (tokenGenerator != null) {
            ServiceReferenceHolder.getInstance().setTokenGenerator(tokenGenerator);
        }
    }

    public TokenGenerator getTokenGenerator() {
        return tokenGenerator;
    }

    public void setTokenGenerator(TokenGenerator tokenGenerator) {
        tokenGenerators.add(tokenGenerator);
    }

    public void unsetTokenGenerator(TokenGenerator tokenGenerator) {
        tokenGenerators.remove(tokenGenerator);
    }

    protected void setAppManagerConfigurationService(AppManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("Gateway manager configuration service bound to the WebApp host objects");
        }
        configuration = amcService.getAPIManagerConfiguration();
    }

    protected void unsetAppManagerConfigurationService(AppManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("Gateway manager configuration service unbound from the WebApp host objects");
        }
        configuration = null;
    }
}
