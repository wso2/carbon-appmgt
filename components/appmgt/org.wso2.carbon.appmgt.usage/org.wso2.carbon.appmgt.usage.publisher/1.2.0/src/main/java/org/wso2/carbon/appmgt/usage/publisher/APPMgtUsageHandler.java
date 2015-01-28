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
import org.wso2.carbon.appmgt.usage.publisher.internal.APPManagerConfigurationServiceComponent;
import org.wso2.carbon.appmgt.usage.publisher.internal.UsageComponent;

import java.util.Map;

public class APPMgtUsageHandler extends AbstractHandler {

    private static final Log log   = LogFactory.getLog(APPMgtUsageHandler.class);

    private volatile APIMgtUsageDataPublisher publisher;

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
