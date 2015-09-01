/*
 *
 *   Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.appmgt.mobile.utils;

import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;

/**
 * Resolved host information
 */
public class HostResolver {

    private static final Log log = LogFactory.getLog(HostResolver.class);

    public static String getHost(String abbr){

        String host = "";

        if("%http%".equals(abbr)){

            try {
                host += "http://" + NetworkUtils.getLocalHostname() + ":" +
                        CarbonUtils.getTransportPort(ConfigurationContextFactory.createDefaultConfigurationContext(), "http");
            } catch (Exception e) {
               log.error("Error occurred while getting host", e);
               log.debug("Error: " + e);
            }
        }else if("%https%".equals(abbr)){
            try {
                host += "https://" + NetworkUtils.getLocalHostname() + ":" +
                        CarbonUtils.getTransportPort(ConfigurationContextFactory.createDefaultConfigurationContext(), "https");
            } catch (Exception e) {
                log.error("Error occurred while getting host", e);
            }
        }else{
            host = abbr;
        }

        return host;
    }


    public static String getHostWithHTTP(){

        String host = "";

        try {
            host += "http://" + NetworkUtils.getLocalHostname() + ":" +
                    CarbonUtils.getTransportPort(ConfigurationContextFactory.createDefaultConfigurationContext(), "http");
        } catch (Exception e) {
            log.error("Error occurred while getting host", e);
        }

        return host;
    }
}
