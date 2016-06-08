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

package org.wso2.carbon.appmgt.usage.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.appmgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.usage.publisher.dto.CacheStatPublisherDTO;
import org.wso2.carbon.appmgt.usage.publisher.internal.APPManagerConfigurationServiceComponent;
import org.wso2.carbon.appmgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Caching;
import java.util.Map;

public class APPMgtUsageHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(APPMgtUsageHandler.class);
    private volatile APIMgtUsageDataPublisher publisher;
    private String publisherClass =
            APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getPublisherClass();
    private boolean enabled = APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().isEnabled();

    public boolean handleRequest(MessageContext mc) {

        try {

            if (enabled) {
                long currentTime = System.currentTimeMillis();
                org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) mc).
                        getAxis2MessageContext();

                Map<String, String> headers = (Map<String, String>) axis2MC.getProperty(
                        org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

                String transportURL = (String) axis2MC.getProperty("TransportInURL");
                String referer = headers.get("Referer");

                mc.setProperty(APIMgtUsagePublisherConstants.REQUEST_TIME, currentTime);
                mc.setProperty(APIMgtUsagePublisherConstants.REFERER, referer);
                mc.setProperty(APIMgtUsagePublisherConstants.TRANSPORT_URL, transportURL);

            }
        } catch (Throwable e) {
            log.error("Error:  " + e.getMessage(), e);
        }
        return true;
    }

    /**
     * This method publishes cache hit/miss events to BAM
     * * @param messageContext
     */
    private void publishCacheEvent(MessageContext messageContext) {

        String saml2CookieValue = null;
        String username = null;

        saml2CookieValue =
                String.valueOf(messageContext.getProperty(AppMConstants.APPM_SAML2_COOKIE));

        if (saml2CookieValue != null) {
            String fullRequestPath =
                    String.valueOf(messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH));
            AuthenticationContext authContext = (AuthenticationContext) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.API_AUTH_CONTEXT);

            if (publisher == null) {
                synchronized (this) {
                    if (publisher == null) {
                        try {
                            log.debug("Instantiating Data Publisher");
                            publisher =
                                    (APIMgtUsageDataPublisher) Class.forName(publisherClass)
                                            .newInstance();
                            publisher.init();
                        } catch (ClassNotFoundException e) {
                            log.error("Class not found " + publisherClass);
                        } catch (InstantiationException e) {
                            log.error("Error instantiating " + publisherClass);
                        } catch (IllegalAccessException e) {
                            log.error("Illegal access to " + publisherClass);
                        }
                    }
                }
            }

            int cacheHit = 1;

            //read the cache hit value set by SAML2AuthenticationHandler
            if (messageContext.getProperty(AppMConstants.APPM_SAML2_CACHE_HIT) != null) {
                cacheHit =
                        Integer.parseInt(String.valueOf(messageContext.getProperty(
                                AppMConstants.APPM_SAML2_CACHE_HIT)));
            }

            if (Caching.getCacheManager(AppMConstants.API_MANAGER_CACHE_MANAGER)
                    .getCache(AppMConstants.KEY_CACHE_NAME) != null) {
                username =
                        (String) Caching.getCacheManager(AppMConstants.API_MANAGER_CACHE_MANAGER)
                                .getCache(AppMConstants.KEY_CACHE_NAME)
                                .get(saml2CookieValue);
            } else {
                username = authContext.getUsername();
            }

            long requestTime =
                    ((Long) messageContext.getProperty(APIMgtUsagePublisherConstants.REQUEST_TIME)).longValue();

            CacheStatPublisherDTO cacheStatPublisherDTO = new CacheStatPublisherDTO();

            cacheStatPublisherDTO.setContext((String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT));
            cacheStatPublisherDTO.setApi_version((String) messageContext.getProperty(
                    RESTConstants.SYNAPSE_REST_API_VERSION));
            cacheStatPublisherDTO.setApi((String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API));
            cacheStatPublisherDTO.setVersion((String) messageContext.getProperty(
                    RESTConstants.SYNAPSE_REST_API_VERSION));
            cacheStatPublisherDTO.setCachHit(cacheHit);
            cacheStatPublisherDTO.setRequestTime(requestTime);
            if (username != null) {
                cacheStatPublisherDTO.setUsername(username);
                cacheStatPublisherDTO.setTenantDomain(MultitenantUtils.getTenantDomain(
                        cacheStatPublisherDTO.getUsername()));
            }
            cacheStatPublisherDTO.setHostName((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.HOST_NAME));
            cacheStatPublisherDTO.setApiPublisher(authContext.getApiPublisher());
            cacheStatPublisherDTO.setApplicationName(authContext.getApplicationName());
            cacheStatPublisherDTO.setApplicationId(authContext.getApplicationId());
            cacheStatPublisherDTO.setTrackingCode((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.TRACKING_CODE));
            cacheStatPublisherDTO.setReferer((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.REFERER));
            cacheStatPublisherDTO.setResponseTime(System.currentTimeMillis() - requestTime);
            cacheStatPublisherDTO.setFullRequestPath(fullRequestPath);
            publisher.publishEvent(cacheStatPublisherDTO);
        }
    }

    public boolean handleResponse(MessageContext mc) {
        try {
            if (enabled) {
                Boolean isLogoutReqeust = (Boolean) mc.getProperty("isLogoutRequest");
                if (isLogoutReqeust != null && isLogoutReqeust.booleanValue()) {
                    return true;
                }
                Long currentTime = System.currentTimeMillis();
                Long serviceTime = currentTime - (Long) mc.getProperty(APIMgtUsagePublisherConstants.REQUEST_TIME);
                String transportURL = (String) mc.getProperty(APIMgtUsagePublisherConstants.TRANSPORT_URL);
                UsageComponent.addResponseTime(transportURL, serviceTime);
            }
        } catch (Throwable e) {
            log.error("Error " + e.getMessage(), e);
        }
        return true; // Should never stop the message flow
    }
}
