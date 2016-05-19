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
import org.wso2.carbon.appmgt.usage.publisher.internal.APPManagerConfigurationServiceComponent;
import org.wso2.carbon.appmgt.usage.publisher.internal.DataPublisherAlreadyExistsException;
import org.wso2.carbon.appmgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.appmgt.usage.publisher.service.APIMGTConfigReaderService;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class DataPublisherUtil {

    private static final Log log = LogFactory.getLog(DataPublisherUtil.class);

    private static String hostAddress = null;
    public static final String HOST_NAME = "HostName";
    private static final String UNKNOWN_HOST = "UNKNOWN_HOST";
    private static boolean isEnabledMetering=false;

    public static String getHostAddress() {

        if (hostAddress != null) {
            return hostAddress;
        }
        hostAddress =   ServerConfiguration.getInstance().getFirstProperty(HOST_NAME);
        if(null == hostAddress){
        	if (getLocalAddress() != null) {
        		hostAddress = getLocalAddress().getHostName();
        	}
            if (hostAddress == null) {
                hostAddress = UNKNOWN_HOST;
            }
            return hostAddress;
        }else {
            return hostAddress;
        }
    }

    private static InetAddress getLocalAddress(){
        Enumeration<NetworkInterface> ifaces = null;
        try {
            ifaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            log.error("Failed to get host address", e);
        }
        if (ifaces != null) {
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isEnabledMetering() {
        return isEnabledMetering;
    }

    public static void setEnabledMetering(boolean enabledMetering) {
        isEnabledMetering = enabledMetering;
    }

    static DataPublisher getDataPublisher() {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        //Get DataPublisher which has been registered for the tenant.
        DataPublisher dataPublisher = UsageComponent.getDataPublisher(tenantDomain);

        APIMGTConfigReaderService apimgtConfigReaderService = APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService();

        //If a DataPublisher had not been registered for the tenant.
        if (dataPublisher == null && apimgtConfigReaderService.getBamServerURL() != null) {
            String serverUser = apimgtConfigReaderService.getBamServerUser();
            String serverPassword = apimgtConfigReaderService.getBamServerPassword();
            String serverURL = apimgtConfigReaderService.getBamServerURL();

            try {
                //Create new DataPublisher for the tenant.
                dataPublisher = new DataPublisher(null, serverURL, null, serverUser, serverPassword);

                //Add created DataPublisher.
                UsageComponent.addDataPublisher(tenantDomain, dataPublisher);
            } catch (DataPublisherAlreadyExistsException e) {
                log.warn("Attempting to register a data publisher for the tenant " + tenantDomain +
                        " when one already exists. Returning existing data publisher");
                return UsageComponent.getDataPublisher(tenantDomain);
            } catch (DataEndpointConfigurationException | DataEndpointException | DataEndpointAuthenticationException |
                    DataEndpointAgentConfigurationException | TransportException e) {
                log.error("Error while creating data publisher", e);
            }
        }

        return dataPublisher;
    }
}
