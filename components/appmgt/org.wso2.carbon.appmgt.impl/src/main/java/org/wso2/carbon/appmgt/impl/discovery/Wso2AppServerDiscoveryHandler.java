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

package org.wso2.carbon.appmgt.impl.discovery;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.impl.APIManagerFactory;
import org.wso2.carbon.appmgt.impl.clients.AppServerWebappAdminClient;
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationDTO;
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationListDTO;
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationListElementDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.VersionedWebappMetadata;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappMetadata;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappsWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Discovery handler implementation which calls the WSO2-AS as backend server and returns the
 * application information
 *
 */
public class Wso2AppServerDiscoveryHandler implements ApplicationDiscoveryHandler {

    private static final Log log = LogFactory.getLog(Wso2AppServerDiscoveryHandler.class);

    private static final String HANDLER_NAME = "WSO2-AS";
    private static final int MAX_GENERATED_CONTEXT_SUFFIX = 1000;
    private static final String CONTEXT_NOT_GENERATED = "<could-not-generate>";
    private static final String STATUS_NEW = "NEW";
    private static final String STATUS_CREATED = "CREATED";
    private static final String JAX_WEBAPP_TYPE = "jaxWebapp";
    private static final String DEFAULT_GENERATED_VERSION = "1.0";
    private static final String DEFAULT_VERSION_STRING = "/default";
    private static final String CONTEXT_DATA_LOGGED_IN_USER = "LOGGED_IN_USER";
    private static final String CONTEXT_DATA_APP_SERVER_URL = "APP_SERVER_URL";

    @Override
    public String getDisplayName() {
        return HANDLER_NAME;
    }

    @Override
    public DiscoveredApplicationListDTO discoverApplications(
            ApplicationDiscoveryContext discoveryContext, DiscoveryCredentials credentials,
            DiscoverySearchCriteria criteria, Locale locale, PrivilegedCarbonContext carbonContext)
            throws AppManagementException {

        if (!(credentials instanceof UserNamePasswordCredentials)) {
            throw new AppManagementException(
                    "Application Listing from WSO2 AS needs " + UserNamePasswordCredentials.class
                            .getName() + " But found " + credentials);
        }

        discoveryContext.putData(CONTEXT_DATA_LOGGED_IN_USER, credentials.getLoggedInUsername());

        ConfigurationContextService configurationContextService = (ConfigurationContextService) carbonContext
                .getOSGiService(ConfigurationContextService.class);
        ConfigurationContext configurationContext = configurationContextService
                .getClientConfigContext();
        UserNamePasswordCredentials userNamePasswordCredentials = (UserNamePasswordCredentials) credentials;
        try {
            AppServerWebappAdminClient webappAdminClient = getAppServerWebappAdminClient(
                    discoveryContext, locale, configurationContext, userNamePasswordCredentials);

            WebappsWrapper webappsWrapper = webappAdminClient
                    .getPagedWebappsSummary(translateApplicationNameSearchString(criteria),
                            translateStatus(criteria), translateStatus(criteria),
                            criteria.getPageNumber());

            discoveryContext.putData(AppServerWebappAdminClient.class.getName(), webappAdminClient);

            List<WebappMetadata> webappMetadataList = flatten(webappsWrapper.getWebapps());
            List<WebappMetadata> filteredMetadataList = filter(webappMetadataList, discoveryContext,
                    credentials, criteria, locale, carbonContext);
            return translateToDto(webappsWrapper, filteredMetadataList,
                    credentials.getLoggedInUsername());
        } catch (AppManagementException ame) {
            String message = String
                    .format("The application server URL, username or password mismatch URL :%s, UserName : %s",
                            userNamePasswordCredentials.getAppServerUrl(),
                            userNamePasswordCredentials.getUserName());
            throw new AppManagementException(message, ame);
        }
    }

    @Override
    public DiscoveredApplicationDTO readApplicationInfo(
            ApplicationDiscoveryContext discoveryContext, APIIdentifier apiIdentifier,
            PrivilegedCarbonContext carbonContext) throws AppManagementException {

        String webappId = apiIdentifier.getApplicationId();
        String loggedInUsername = (String) discoveryContext.getData(CONTEXT_DATA_LOGGED_IN_USER);
        DiscoveredApplicationDTO result = null;

        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(loggedInUsername);

        try {
            AppServerWebappAdminClient webappAdminClient = getAppServerWebappAdminClient(
                    discoveryContext, null, null, null);
            int pageNo = 0;
            int totalPages = 0;

            //Refactor to new session
            while ((pageNo == 0 || pageNo < totalPages) && result == null) {
                WebappsWrapper webappsWrapper = webappAdminClient
                        .getPagedWebappsSummary("", "all", "all", pageNo);
                totalPages = webappsWrapper.getNumberOfPages();
                VersionedWebappMetadata[] versionedWebappMetadataArray = webappsWrapper
                        .getWebapps();

                pageNo++;

                searching:
                for (VersionedWebappMetadata versionedWebappMetadata : versionedWebappMetadataArray) {
                    WebappMetadata[] webappMetadataArray = versionedWebappMetadata
                            .getVersionGroups();
                    for (WebappMetadata webappMetadata : webappMetadataArray) {
                        String currentWebappId = generateWebappId(webappMetadata);
                        String deploymentId = webappMetadata.getWebappFile();
                        if (currentWebappId.equals(webappId)) {
                            result = new DiscoveredApplicationDTO();
                            String version = getVersion(webappMetadata);
                            String context = webappMetadata.getContextPath();
                            result.setDisplayName(webappMetadata.getDisplayName());
                            result.setVersion(version);
                            result.setApplicationType(webappMetadata.getWebappType());
                            result.setRemoteContext(context);
                            result.setProxyContext(generateProxyContext(context, apiProvider));
                            result.setApplicationId(generateWebappId(webappMetadata));
                            result.setStatus(
                                    getStatus(loggedInUsername, result.getApplicationName(),
                                            result.getVersion(), apiProvider));
                            result.setApplicationUrl(
                                    generateAppUrl(webappsWrapper, webappMetadata));

                            break searching; //Found the item. stop search loops
                        }
                    }
                }
            }
        } catch (AppManagementException ame) {
            String message = String
                    .format("Could not get the application information from the underlying service from [%s]. Most probably the underlying session has been expired.",
                            discoveryContext.getData(CONTEXT_DATA_APP_SERVER_URL));
            throw new AppManagementException(message, ame);
        }

        return result;
    }

    /*
    Returns the application server admin client from the context or create one if not exists.
     */
    private AppServerWebappAdminClient getAppServerWebappAdminClient(
            ApplicationDiscoveryContext discoveryContext, Locale locale,
            ConfigurationContext configurationContext,
            UserNamePasswordCredentials userNamePasswordCredentials) throws AppManagementException {

        AppServerWebappAdminClient webappAdminClient = (AppServerWebappAdminClient) discoveryContext
                .getData(AppServerWebappAdminClient.class.getName());
        if (webappAdminClient == null) {
            if (userNamePasswordCredentials == null) {
                throw new AppManagementException(
                        "Can not create the AppServerWebappAdminClient as UserNamePasswordCredentials is null. Try log-off and log back in.");
            }
            webappAdminClient = new AppServerWebappAdminClient(
                    userNamePasswordCredentials.getUserName(),
                    userNamePasswordCredentials.getPassword(),
                    userNamePasswordCredentials.getAppServerUrl(), configurationContext, locale);
        }
        return webappAdminClient;
    }


    /**
     * Translates the WebappsWrapper to its flat list form
     *
     * The structure of the response object is:
     * {WebappsWrapper : {localWebapps(type of VersionedWebappMetadata) : [type of WebappMetadata]}}
     *
     * @param webappsWrapper
     * @return
     */
    private DiscoveredApplicationListDTO translateToDto(WebappsWrapper webappsWrapper,
            List<WebappMetadata> webappMetadataList, String loggedInUsername)
            throws AppManagementException {
        String providerName = loggedInUsername.replace("@", "-AT-");
        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(loggedInUsername);
        DiscoveredApplicationListDTO result = new DiscoveredApplicationListDTO();
        List<DiscoveredApplicationListElementDTO> appList = new ArrayList<DiscoveredApplicationListElementDTO>();
        result.setApplicationList(appList);
        result.setPageCount(webappsWrapper.getNumberOfPages());

        for (WebappMetadata webappMetadata : webappMetadataList) {
            DiscoveredApplicationListElementDTO listElementDTO = new DiscoveredApplicationListElementDTO();
            String version = getVersion(webappMetadata);
            String context = webappMetadata.getContextPath();
            listElementDTO.setDisplayName(webappMetadata.getDisplayName());
            listElementDTO.setVersion(version);
            listElementDTO.setApplicationType(webappMetadata.getWebappType());
            listElementDTO.setRemoteContext(context);
            listElementDTO.setProxyContext(generateProxyContext(context, apiProvider));
            String appId = generateWebappId(webappMetadata);
            listElementDTO.setApplicationId(appId);
            listElementDTO.setStatus(getStatus(providerName, appId, version, apiProvider));
            listElementDTO.setApplicationUrl(generateAppUrl(webappsWrapper, webappMetadata));
            listElementDTO.setApplicationPreviewUrl(
                    generateAppPreviewUrl(webappsWrapper, webappMetadata));

            appList.add(listElementDTO);
        }

        result.setTotalNumberOfResults(appList.size());
        return result;
    }

    /**
     * Flatten and return a list of DiscoveredApplicationListElementDTO related to nested  VersionedWebappMetadata
     *
     * @param versionedWebappMetadataArray
     * @return
     */
    private List<WebappMetadata> flatten(VersionedWebappMetadata[] versionedWebappMetadataArray) {
        List<WebappMetadata> result = new ArrayList<WebappMetadata>();

        for (VersionedWebappMetadata versionedWebappMetadata : versionedWebappMetadataArray) {
            WebappMetadata[] webappMetadataArray = versionedWebappMetadata.getVersionGroups();
            for (WebappMetadata webappMetadata : webappMetadataArray) {
                result.add(webappMetadata);
            }
        }

        return result;
    }

    /**
     * Returns the filtered list of list elements
     *
     * @param applicationListElementList
     * @param discoveryContext
     * @param credentials
     * @param criteria
     * @param locale
     * @param carbonContext
     * @return
     */
    private List<WebappMetadata> filter(List<WebappMetadata> applicationListElementList,
            ApplicationDiscoveryContext discoveryContext, DiscoveryCredentials credentials,
            DiscoverySearchCriteria criteria, Locale locale,
            PrivilegedCarbonContext carbonContext) {

        if (APP_STATUS.ANY == parseAppStatus(criteria.getStatus())) {
            return applicationListElementList;
        }

        List<WebappMetadata> result = new ArrayList<WebappMetadata>();

        return result;
    }

    private String getVersion(WebappMetadata webappMetadata) {
        String version = webappMetadata.getAppVersion();
        if (DEFAULT_VERSION_STRING.equals(version)) {
            return DEFAULT_GENERATED_VERSION;
        }
        if (version.startsWith("/")) {
            version = version.substring(1, version.length());
        }
        return version;
    }

    /*
    Creates the Application URL to reach the backend application
     */
    private String generateAppUrl(WebappsWrapper webappsWrapper, WebappMetadata webappMetadata) {
        String protocol = "http";
        String host = webappsWrapper.getHostName();
        int port = webappsWrapper.getHttpPort();
        String context = webappMetadata.getContextPath();
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append("://").append(host).append(":").append(port).append(context);
        return sb.toString();
    }

    /*
    Creates the Application URL to reach the backend application
     */
    private String generateAppPreviewUrl(WebappsWrapper webappsWrapper,
            WebappMetadata webappMetadata) {
        String protocol = "http";
        String host = webappsWrapper.getHostName();
        int port = webappsWrapper.getHttpPort();
        String context = webappMetadata.getContextPath();
        if (JAX_WEBAPP_TYPE.equals(webappMetadata.getWebappType())) {
            context = context.concat(webappMetadata.getServiceListPath());
        }
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append("://").append(host).append(":").append(port).append(context);
        return sb.toString();
    }

    private String translateStatus(DiscoverySearchCriteria criteria) {
        return "all";
    }

    private String translateApplicationNameSearchString(DiscoverySearchCriteria criteria) {
        return criteria.getApplicationName();
    }

    private String generateWebappId(WebappMetadata webappMetadata) {
        return webappMetadata.getWebappFile().replaceAll("[^\\p{Alnum}]", "_");
    }

    /**
     * Generates a proxy context. use the original context path if it is free. Generate new context
     * path appending an integer if the context is already used.
     *  e.g. Original context is /xyz then it generates /xyz_0 if /xyz is used.
     *  This is bit expensive call as it may iterates through all artifacts in worst case.
     * @param context
     * @param apiProvider
     * @return
     * @throws AppManagementException
     */
    protected String generateProxyContext(String context, APIProvider apiProvider)
            throws AppManagementException {
        String result = context;
        int i = 0;
        while (apiProvider.isContextExist(result)) {
            //Generate a next available context
            result = context + "_" + i;
            i++;

            if (i > MAX_GENERATED_CONTEXT_SUFFIX) {
                log.error("Could not generate context. All context from " +
                        context + "_0 to " + context + "_" + i
                        + " are already taken. Aborting the creation");
                return CONTEXT_NOT_GENERATED;
            }
        }
        return result;
    }

    private String getStatus(String providerName, String appName, String version,
            APIProvider apiProvider) throws AppManagementException {
        if (isCreated(providerName, appName, version, apiProvider)) {
            return STATUS_CREATED;
        }
        return STATUS_NEW;
    }

    /**
     * Returns true if the given application exists
     * @param providerName
     * @param appName
     * @param version
     * @param apiProvider
     * @return
     * @throws AppManagementException
     */
    protected boolean isCreated(String providerName, String appName, String version,
            APIProvider apiProvider) throws AppManagementException {
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, appName, version);
        return apiProvider.isAPIAvailable(apiIdentifier);
    }

    private DiscoveredApplicationListDTO discoverApplicationsWithLocalPaging(
            ApplicationDiscoveryContext discoveryContext, DiscoveryCredentials credentials,
            DiscoverySearchCriteria criteria, Locale locale, PrivilegedCarbonContext carbonContext,
            AppServerWebappAdminClient webappAdminClient) throws AppManagementException {

        String searchString = translateApplicationNameSearchString(criteria);
        int pageNumber = 0;

        WebappsWrapper webappsWrapper = webappAdminClient
                .getPagedWebappsSummary(searchString, translateStatus(criteria),
                        translateStatus(criteria), criteria.getPageNumber());

        return null;
    }

    protected APP_STATUS parseAppStatus(String statusString) {
        APP_STATUS result = APP_STATUS.ANY;
        if (statusString == null || statusString.isEmpty()) {
            result = APP_STATUS.ANY;
        } else if ("CREATED".equalsIgnoreCase(statusString)) {
            result = APP_STATUS.CREATED;
        } else if ("NEW".equalsIgnoreCase(statusString)) {
            result = APP_STATUS.NEW;
        }

        return result;
    }
}
