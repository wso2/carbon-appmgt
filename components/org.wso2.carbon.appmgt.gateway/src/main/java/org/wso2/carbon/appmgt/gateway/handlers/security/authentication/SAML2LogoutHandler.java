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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.opensaml.saml2.core.*;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.gateway.handlers.security.Session;
import org.wso2.carbon.appmgt.gateway.handlers.security.SessionStore;
import org.wso2.carbon.appmgt.gateway.handlers.security.saml2.IDPMessage;
import org.wso2.carbon.appmgt.gateway.handlers.security.saml2.SAMLException;
import org.wso2.carbon.appmgt.gateway.handlers.security.saml2.SAMLUtils;
import org.wso2.carbon.appmgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.DefaultAppRepository;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * Handles Gateway Authentication with SAML2
 */
public class SAML2LogoutHandler extends AbstractHandler implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(SAML2LogoutHandler.class);

    private static final String SESSION_ATTRIBUTE_SAML_SESSION_INDEX = "samlSessionIndex";

    // A Synapse handler is instantiated per Synapse API.
    // So the web app for the relevant Synapse API can be fetched and stored as an instance variable.
    private WebApp webApp;
    private AppManagerConfiguration configuration;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        configuration = org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        // If the request comes to the logout URL then execute the logout logic.

        Session session = GatewayUtils.getSession(messageContext);

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String webAppContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String webAppVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String fullResourceURL = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        String baseURL = String.format("%s/%s/", webAppContext, webAppVersion);
        String relativeResourceURL = StringUtils.substringAfter(fullResourceURL, baseURL);

        // Fetch the web app for the requested context and version.
        try {
            if(webApp == null){
                webApp = new DefaultAppRepository(null).getWebAppByContextAndVersion(webAppContext, webAppVersion, -1234);
            }
        } catch (AppManagementException e) {
            String errorMessage = String.format("Can't fetch the web for '%s' from the repository.", fullResourceURL);
            GatewayUtils.logAndThrowException(log, errorMessage, e);
        }

        if(GatewayUtils.isLogoutURL(webApp, relativeResourceURL)){

            if(log.isDebugEnabled()){
                GatewayUtils.logWithRequestInfo(log, messageContext, "User has requested a logout.");
            }

            // Build the message.
            try {
                RelayUtils.buildMessage(axis2MessageContext);
            } catch (IOException e) {
                String errorMessage = String.format("Can't build the incoming request message for '%s'.", fullResourceURL);
                GatewayUtils.logAndThrowException(log, errorMessage, e);
            } catch (XMLStreamException e) {
                String errorMessage = String.format("Can't build the incoming request message for '%s'.", fullResourceURL);
                GatewayUtils.logAndThrowException(log, errorMessage, e);
            }

            try {
                IDPMessage idpMessage = SAMLUtils.processIDPMessage(messageContext);

                if(idpMessage.isSLORequest()){

                    OMFactory fac = OMAbstractFactory.getOMFactory();
                    OMNamespace ns = fac.createOMNamespace("http://wso2.org/appm", "appm");
                    OMElement payload = fac.createOMElement("SLOResponse", ns);

                    OMElement errorMessage = fac.createOMElement("message", ns);
                    errorMessage.setText("SLORequest has been successfully processed by WSO2 App Manager");

                    payload.addChild(errorMessage);

                    GatewayUtils.send200(messageContext, payload);
                    return false;

                }else{
                    SessionStore.getInstance().removeSession(session.getUuid());
                    redirectToIDPWithLogoutRequest(messageContext, session);
                }

            } catch (SAMLException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private void redirectToIDPWithLogoutRequest(MessageContext messageContext, Session session) {
        LogoutRequest logoutRequest = SAMLUtils.buildLogoutRequest(webApp.getSaml2SsoIssuer(), session);
        GatewayUtils.logWithRequestInfo(log, messageContext, "Redirecting to the IDP for logging out.");
        GatewayUtils.redirectToIDPWithSAMLRequest(messageContext, logoutRequest);
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

   @Override
    public void destroy() {

    }

}
