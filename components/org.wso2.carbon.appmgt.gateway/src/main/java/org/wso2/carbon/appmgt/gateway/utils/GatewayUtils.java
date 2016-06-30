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

package org.wso2.carbon.appmgt.gateway.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.http.HttpStatus;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.appmgt.api.model.URITemplate;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.utils.UrlPatternMatcher;

import java.net.MalformedURLException;
import java.net.URL;

public class GatewayUtils {

    public static String getIDPUrl() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
                getFirstProperty(AppMConstants.SSO_CONFIGURATION_IDENTITY_PROVIDER_URL);
    }

    public static String getAppRootURL(MessageContext messageContext) throws MalformedURLException {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String servicePrefix = axis2MessageContext.getProperty("SERVICE_PREFIX").toString();

        String webAppContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String webAppVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

        URL serverRootURL = new URL(servicePrefix);
        URL appRootURL = new URL(serverRootURL, String.format("%s/%s/", webAppContext, webAppVersion));
        return appRootURL.toString();


    }

    public static boolean isAnonymousAccessAllowed(WebApp webApp, String httpVerb, String relativeResourceURL) {

        if(webApp.getAllowAnonymous()){
            return true;
        }

        URITemplate mostSpecificTemplate = null;

        for(URITemplate  uriTemplate : webApp.getUriTemplates()){

            if(UrlPatternMatcher.match(String.format("%s:%s", uriTemplate.getHTTPVerb(), uriTemplate.getUriTemplate()),
                                        String.format("%s:/%s", httpVerb, relativeResourceURL))){

                if(mostSpecificTemplate == null){
                    mostSpecificTemplate = uriTemplate;
                }else if(mostSpecificTemplate.getUriTemplate().split("/").length < uriTemplate.getUriTemplate().split("/").length){
                    mostSpecificTemplate = uriTemplate;
                }
            }
        }

        if(mostSpecificTemplate != null){
            return mostSpecificTemplate.getPolicyGroup().isAllowAnonymous();
        }

        return false;
    }

    public static void logAndThrowException(Log log, String errorMessage, Exception e) {

        if(e == null){
            log.error(errorMessage);
            throw new SynapseException(errorMessage);
        }else {
            log.error(errorMessage, e);
            throw new SynapseException(errorMessage, e);
        }
    }

    public static void send401(MessageContext messageContext, String reason) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace("http://wso2.org/appm", "appm");
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(reason);

        payload.addChild(errorMessage);

        OMElement firstChild = messageContext.getEnvelope().getBody().getFirstElement();
        if (firstChild != null) {
            firstChild.insertSiblingAfter(payload);
            firstChild.detach();
        } else {
            messageContext.getEnvelope().getBody().addChild(payload);
        }

        axis2MessageContext.setProperty(NhttpConstants.HTTP_SC, HttpStatus.SC_UNAUTHORIZED);
        messageContext.setResponse(true);
        messageContext.setProperty("RESPONSE", "true");
        messageContext.setTo(null);
        axis2MessageContext.removeProperty("NO_ENTITY_BODY");

        axis2MessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/xml");
        axis2MessageContext.removeProperty(Constants.Configuration.CONTENT_TYPE);

        Axis2Sender.sendBack(messageContext);
    }
}
