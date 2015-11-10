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


package org.wso2.carbon.appmgt.hostobjects;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.hostobjects.internal.HostObjectComponent;
import org.wso2.carbon.appmgt.hostobjects.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.usage.publisher.APIMgtUsagePublisherConstants;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Comparator;

public class HostObjectUtils {
    private static final Log log = LogFactory.getLog(APIProviderHostObject.class);
    private static ConfigurationContextService configContextService = null;

     public static void setConfigContextService(ConfigurationContextService configContext) {
        HostObjectUtils.configContextService = configContext;
    }

    public static ConfigurationContext getConfigContext() throws AppManagementException {
        if (configContextService == null) {
            throw new AppManagementException("ConfigurationContextService is null");
        }

        return configContextService.getServerConfigContext();

    }

    /**
     * Get the running transport port
     *
     * @param transport [http/https]
     * @return port
     */
    public static String getBackendPort(String transport) {
        int port;
        String backendPort;
        try {
            port = CarbonUtils.getTransportProxyPort(getConfigContext(), transport);
            if (port == -1) {
                port = CarbonUtils.getTransportPort(getConfigContext(), transport);
            }
            backendPort = Integer.toString(port);
            return backendPort;
        } catch (AppManagementException e) {
            log.error(e.getMessage());
            return null;

        }
    }

    private static void handleException(String msg) throws AppManagementException {
        log.error(msg);
        throw new AppManagementException(msg);
    }

    private static void handleException(String msg, Throwable t) throws AppManagementException {
        log.error(msg, t);
        throw new AppManagementException(msg, t);
    }
    
    public static class RequiredUserFieldComparator implements Comparator<UserFieldDTO> {

        public int compare(UserFieldDTO filed1, UserFieldDTO filed2) {
            if (filed1.getDisplayOrder() == 0) {
                filed1.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (filed2.getDisplayOrder() == 0) {
                filed2.setDisplayOrder(Integer.MAX_VALUE);
            }
            
            if (!filed1.getRequired() && filed2.getRequired()){
            	return 1;
            }
            
            if (filed1.getRequired() && filed2.getRequired()){
            	return 0;
            }
            
            if (filed1.getRequired() && !filed2.getRequired()){
            	return -1;
            }

            return 0;
        }

    }
    public static class UserFieldComparator implements Comparator<UserFieldDTO> {

        public int compare(UserFieldDTO filed1, UserFieldDTO filed2) {
            if (filed1.getDisplayOrder() == 0) {
                filed1.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (filed2.getDisplayOrder() == 0) {
                filed2.setDisplayOrder(Integer.MAX_VALUE);
            }
            
            if (filed1.getDisplayOrder() < filed2.getDisplayOrder()) {
                return -1;
            }
            if (filed1.getDisplayOrder() == filed2.getDisplayOrder()) {
                return 0;
            }
            if (filed1.getDisplayOrder() > filed2.getDisplayOrder()) {
                return 1;
            }
            return 0;
        }

    }

    protected static boolean checkDataPublishingEnabled() {
        AppManagerConfiguration configuration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String enabledStr = configuration.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_ENABLED);
        return enabledStr != null && Boolean.parseBoolean(enabledStr);
    }

    public static boolean isUIActivityBAMPublishEnabled() {
        AppManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String isEnabled = configuration
                .getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_UI_ACTIVITY_ENABLED);
        return isEnabled != null && Boolean.parseBoolean(isEnabled);
    }
}
