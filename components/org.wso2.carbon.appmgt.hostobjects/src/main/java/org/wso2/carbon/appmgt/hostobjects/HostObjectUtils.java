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
import org.apache.commons.lang.StringUtils;
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
import java.util.HashMap;
import java.util.Map;

public class HostObjectUtils {
    private static final Log log = LogFactory.getLog(APIProviderHostObject.class);
    /**
     * Hex color codes for generating default thumbnails.
     * TODO: load colors from a config file
     */
    private static final String[] DEFAULT_THUMBNAIL_COLORS = new String[]{"1abc9c", "2ecc71", "3498db", "9b59b6",
            "34495e", "16a085", "27ae60", "2980b9", "8e44ad", "2c3e50", "f1c40f", "e67e22", "e74c3c", "95a5a6",
            "f39c12", "d35400", "c0392b", "7f8c8d"};

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

    /**
     * Returns the text (key {@code text}) and color (key {@code color}) of the default thumbnail based on the specified app name.
     * @param appName app name
     * @return default thumbnail data
     * @exception IllegalArgumentException if {@code appName} is {@code null} or empty
     */
    public static Map<String, String> getDefaultThumbnail(String appName) {
        if (appName == null) {
            throw new IllegalArgumentException("Invalid argument. App name cannot be null.");
        }
        if (appName.isEmpty()) {
            throw new IllegalArgumentException("Invalid argument. App name cannot be empty.");
        }

        String defaultThumbnailText;
        if (appName.length() == 1) {
            // only one character in the app name
            defaultThumbnailText = appName;
        } else {
            // there are more than one character in the app name
            String[] wordsInAppName = StringUtils.split(appName);
            int firstCodePoint, secondCodePoint;
            if (wordsInAppName.length == 1) {
                // one word
                firstCodePoint = Character.toTitleCase(wordsInAppName[0].codePointAt(0));
                secondCodePoint = wordsInAppName[0].codePointAt(Character.charCount(firstCodePoint));
            } else {
                // two or more words
                firstCodePoint = Character.toTitleCase(wordsInAppName[0].codePointAt(0));
                secondCodePoint = wordsInAppName[1].codePointAt(0);
            }
            defaultThumbnailText = (new StringBuffer()).append(Character.toChars(firstCodePoint)).append(
                    Character.toChars(secondCodePoint)).toString();
        }
        String defaultThumbnailColor = DEFAULT_THUMBNAIL_COLORS[Math.abs(appName.hashCode()) %
                DEFAULT_THUMBNAIL_COLORS.length];

        Map<String, String> defaultThumbnail = new HashMap<String, String>(2);
        defaultThumbnail.put("text", defaultThumbnailText);
        defaultThumbnail.put("color", defaultThumbnailColor);
        return defaultThumbnail;
    }

    /**
     * Returns the current subscription configuration values of "EnableSelfSubscription" and
     * "EnableEnterpriseSubscription" elements, defined in app-manager.xml.
     *
     * @return Subscription Configuration.
     */
    public static Map<String, Boolean> getSubscriptionConfiguration() {
        AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        Boolean selfSubscriptionStatus = Boolean.valueOf(config.getFirstProperty(
                AppMConstants.ENABLE_SELF_SUBSCRIPTION));
        Boolean enterpriseSubscriptionStatus = Boolean.valueOf(config.getFirstProperty(
                AppMConstants.ENABLE_ENTERPRISE_SUBSCRIPTION));

        Map<String, Boolean> subscriptionCofig = new HashMap<String, Boolean>(2);
        subscriptionCofig.put("EnableSelfSubscription", selfSubscriptionStatus);
        subscriptionCofig.put("EnableEnterpriseSubscription", enterpriseSubscriptionStatus);
        return subscriptionCofig;
    }

    /**
     * Returns binary file storage location configuration
     *
     * @return storage location of images, mobile binaries
     */
    public static String getBinaryStorageConfiguration() {
        AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String binaryStorageLocation = config.getFirstProperty(AppMConstants.BINARY_FILE_STORAGE_ABSOLUTE_LOCATION);
        return binaryStorageLocation;
    }

    /**
     * Get the configuration for create service provider for skip gateway enabled apps
     * @return is create service provider for skip gateway apps enabled or disabled
     */
    public static boolean isServiceProviderCreateEnabledForSkipGatewayApp(){
        AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        boolean isCreateServiceProviderForSkipGatewayApps =
                Boolean.parseBoolean(config.getFirstProperty(AppMConstants.SSO_CONFIGURATION_CREATE_SP_FOR_SKIP_GATEWAY_APPS));
        return isCreateServiceProviderForSkipGatewayApps;
    }


}
