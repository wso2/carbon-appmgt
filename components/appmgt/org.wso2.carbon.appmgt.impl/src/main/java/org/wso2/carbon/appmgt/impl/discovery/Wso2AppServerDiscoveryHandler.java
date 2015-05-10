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
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationDTO;
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationListDTO;
import org.wso2.carbon.appmgt.impl.dto.DiscoveredApplicationListElementDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.VersionedWebappMetadata;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappMetadata;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappsWrapper;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Discovery handler implementation which calls the WSO2-AS as backend server and returns the
 * application information
 *
 */
public class Wso2AppServerDiscoveryHandler implements ApplicationDiscoveryHandler {

    private static final Log log = LogFactory.getLog(Wso2AppServerDiscoveryHandler.class);

    private static final String HANDLER_NAME = "WSO2-AS";
    private static final int MAX_GENERATED_CONTEXT_SUFFIX = 1000;
    private static final int DEFAULT_PAGE_SIZE = 16;
    private static final String CONTEXT_NOT_GENERATED = "<could-not-generate>";
    private static final String STATUS_NEW = "NEW";
    private static final String STATUS_CREATED = "CREATED";
    private static final String JAX_WEBAPP_TYPE = "jaxWebapp";
    private static final String DEFAULT_GENERATED_VERSION = "1.0";
    private static final String DEFAULT_VERSION_STRING = "/default";
    private static final String CONTEXT_DATA_LOGGED_IN_USER = "LOGGED_IN_USER";

    private static final String CONTEXT_DATA_PAGE_MAP = "PageMap";
    private static final String CONTEXT_DATA_SEARCH_APPLICATION_NAME = "SearchApplicationName";
    private static final String CONTEXT_DATA_SEARCH_APPLICATION_STATUS = "SearchApplicationStatus";
    private static final String CONTEXT_DATA_LAST_APPSERVER_PAGE = "LastAppServerPage";
    private static final String CONTEXT_DATA_LAST_APPSERVER_INDEX = "LastAppServerIndex";
    private static final String CONTEXT_DATA_LASTWEBAPPSWRAPER = "LastWebappsWraper";
    private static final String CONTEXT_DATA_IS_MORE_RESULTS_POSSIBLE = "IsMoreResultsPossible";
    private static final String CONTEXT_DATA_APP_SERVER_URL = "APP_SERVER_URL";

    private static final int MAX_USERNAME_CONTRIBUTION_LENGTH = 8;
    private static final int MAX_HOSTNAME_CONTRIBUTION_LENGTH = 15;
    private static final String PROTOCOL_HTTP = "http";
    private static final String TENANT_CONTEXT_PATH_START_WITH_T = "/t/";
    private static final String TENANT_CONTEXT_PATH_REPLACE_WITH_U = "u";

    private Pattern nonAlphaNumericPattern = Pattern.compile("[^\\p{Alnum}]");

    @Override
    public String getDisplayName() {
        return HANDLER_NAME;
    }

    @Override
    public DiscoveredApplicationListDTO discoverApplications(
            ApplicationDiscoveryContext discoveryContext, DiscoveryCredentials credentials,
            DiscoverySearchCriteria criteria, Locale locale, ConfigurationContext configurationContext)
            throws AppManagementException {

        if (!(credentials instanceof UserNamePasswordCredentials)) {
            throw new AppManagementException(
                    "Application Listing from WSO2 AS needs " + UserNamePasswordCredentials.class
                            .getName() + " But found " + credentials);
        }

        discoveryContext.putData(CONTEXT_DATA_LOGGED_IN_USER, credentials.getLoggedInUsername());

        if (isResetPageCache(criteria, discoveryContext)) {
            clearCache(discoveryContext);
        }
        UserNamePasswordCredentials userNamePasswordCredentials = (UserNamePasswordCredentials) credentials;
        try {
            AppServerWebappAdminClient webappAdminClient = getAppServerWebappAdminClient(
                    discoveryContext, locale, configurationContext, userNamePasswordCredentials);

            WebappsWrapper webappsWrapper = webappAdminClient
                    .getPagedWebappsSummary(translateApplicationNameSearchString(criteria),
                            translateStatus(criteria), translateStatus(criteria),
                            criteria.getPageNumber());

            discoveryContext.putData(AppServerWebappAdminClient.class.getName(), webappAdminClient);
            String loggedInUsername = (String) discoveryContext
                    .getData(CONTEXT_DATA_LOGGED_IN_USER);
            String providerName = loggedInUsername.replace("@", "-AT-");
            APIProvider apiProvider = APIManagerFactory.getInstance()
                    .getAPIProvider(loggedInUsername);

            if (criteria.getStatus() != null && criteria.getStatus().length() > 0) {
                return discoverApplicationsWithPaging(discoveryContext, credentials, criteria,
                        locale, webappAdminClient, providerName, loggedInUsername,
                        apiProvider);
            } else {
                List<WebappMetadata> webappMetadataList = flatten(webappsWrapper.getWebapps());
                List<WebappMetadata> filteredMetadataList = filter(webappsWrapper,
                        webappMetadataList, discoveryContext, credentials, criteria, locale,
                        providerName, loggedInUsername, apiProvider);
                return translateToDto(webappsWrapper, filteredMetadataList, providerName,
                        loggedInUsername, apiProvider);
            }
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
            ConfigurationContext configurationContext) throws AppManagementException {

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
                        String providerName = loggedInUsername.replace("@", "-AT-");
                        String currentWebappId = generateWebappId(webappMetadata, webappsWrapper,
                                providerName);
                        if (currentWebappId.equals(webappId)) {
                            result = translateToDiscoveredApplicationDTO(loggedInUsername,
                                    apiProvider, webappsWrapper, webappMetadata, currentWebappId);

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
     * {WebappsWrapper : {localWebapps(type of VersionedWebappMetadata) : [type of WebappMetadata]}}.
     *
     * @param webappsWrapper
     * @return
     */
    private DiscoveredApplicationListDTO translateToDto(WebappsWrapper webappsWrapper,
            List<WebappMetadata> webappMetadataList, String providerName, String loggedInUsername,
            APIProvider apiProvider) throws AppManagementException {

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
            listElementDTO.setProxyContext(generateProxyContext(context, version, apiProvider));
            String appId = generateWebappId(webappMetadata, webappsWrapper, providerName);
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
     * Translate to given webapp metadata to the webapp information DTO.
     *
     * @param loggedInUsername
     * @param apiProvider
     * @param webappsWrapper
     * @param webappMetadata
     * @param currentWebappId
     * @return
     * @throws AppManagementException
     */
    private DiscoveredApplicationDTO translateToDiscoveredApplicationDTO(String loggedInUsername,
            APIProvider apiProvider, WebappsWrapper webappsWrapper, WebappMetadata webappMetadata,
            String currentWebappId) throws AppManagementException {
        DiscoveredApplicationDTO result;
        result = new DiscoveredApplicationDTO();
        String version = getVersion(webappMetadata);
        String context = webappMetadata.getContextPath();
        result.setDisplayName(webappMetadata.getDisplayName());
        result.setVersion(version);
        result.setApplicationType(webappMetadata.getWebappType());
        result.setRemoteContext(context);
        result.setProxyContext(generateProxyContext(context, version, apiProvider));
        result.setApplicationId(currentWebappId);
        result.setStatus(
                getStatus(loggedInUsername, result.getApplicationName(), result.getVersion(),
                        apiProvider));
        result.setApplicationUrl(generateAppUrl(webappsWrapper, webappMetadata));
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
     * @return
     */
    private List<WebappMetadata> filter(WebappsWrapper webappsWrapper,
            List<WebappMetadata> applicationListElementList,
            ApplicationDiscoveryContext discoveryContext, DiscoveryCredentials credentials,
            DiscoverySearchCriteria criteria, Locale locale,
            String providerName, String userName, APIProvider apiProvider)
            throws AppManagementException {

        APP_STATUS status = parseAppStatus(criteria.getStatus());
        if (status == APP_STATUS.ANY) {
            return applicationListElementList;
        }

        List<WebappMetadata> result = new ArrayList<WebappMetadata>();
        int i = 0;
        for (WebappMetadata webappMetadata : applicationListElementList) {
            String webappId = generateWebappId(webappMetadata, webappsWrapper, userName);
            boolean isCreated = isCreated(providerName, webappId, getVersion(webappMetadata),
                    apiProvider);
            if (status == APP_STATUS.NEW && !isCreated) {
                result.add(webappMetadata);
            } else if (status == APP_STATUS.CREATED && isCreated) {
                result.add(webappMetadata);
            }

        }

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
        String protocol = PROTOCOL_HTTP;
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
        String protocol = PROTOCOL_HTTP;
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

    /**
     * Generates the webapp Identifier.
     * this is in the format of
     * <webapp file name><user name><host name>
     * All the non Alphanumeric characters are removed and any immediate character is converted to upper case so that the name looks like a CamelCase.
     * @param webappMetadata
     * @param webappsWrapper
     * @param userName
     * @return
     */
    private String generateWebappId(WebappMetadata webappMetadata, WebappsWrapper webappsWrapper,
            String userName) {
        String fileName = webappMetadata.getWebappFile();
        String madeName = fileName + "By_" + userName
                .substring(0, Math.min(MAX_USERNAME_CONTRIBUTION_LENGTH, userName.length())) + "On_"
                +
                webappsWrapper.getHostName().substring(0, Math.min(MAX_HOSTNAME_CONTRIBUTION_LENGTH,
                        webappsWrapper.getHostName().length()));

        //remove all non "Alphanumeric" characters and capitalize the character next to the removed one
        StringBuilder sb = new StringBuilder();
        String[] splits = nonAlphaNumericPattern.split(madeName);
        for (String s : splits) {
            if (s.length() > 0) {
                sb.append(s.substring(0, 1).toUpperCase()).append(s.substring(1, s.length()));
            }
        }

        return sb.toString();
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
    protected String generateProxyContext(String context, String version, APIProvider apiProvider)
            throws AppManagementException {

        String result = getContextFromTenant(context);
        int i = 0;
        while (isContextExist(result, version, apiProvider)) {
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

    private boolean isContextExist(String context, String version, APIProvider apiProvider)
            throws AppManagementException {

        return AppMDAO.isContextExist(context);
    }

    /**
     * Returns the application context name form the tenant url of the AS
     * e.g. /t/tenant_domain/webapps/Context/version and we need only the "Context"
     * @param context
     * @return
     */
    private String getContextFromTenant(String context) {
        String result = context;
        if (result.startsWith(TENANT_CONTEXT_PATH_START_WITH_T)) {
            String[] splits = result.split("/");
            String s = TENANT_CONTEXT_PATH_REPLACE_WITH_U;
            if (splits.length > 4) {
                s = splits[4];
            } else if (splits.length > 3) {
                s = splits[3];
            } else if (splits.length > 2) {
                s = splits[2];
            }
            result = "/" + nonAlphaNumericPattern.matcher(s).replaceAll("");
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
     * Returns true if the given application exists.
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

    private DiscoveredApplicationListDTO discoverApplicationsWithPaging(
            ApplicationDiscoveryContext discoveryContext, DiscoveryCredentials credentials,
            DiscoverySearchCriteria criteria, Locale locale,
            AppServerWebappAdminClient webappAdminClient, String providerName,
            String loggedInUserName, APIProvider apiProvider) throws AppManagementException {

        String searchString = translateApplicationNameSearchString(criteria);

        Map<Integer, PagingResult> cachedMap = (Map<Integer, PagingResult>) discoveryContext
                .getData(CONTEXT_DATA_PAGE_MAP);
        if (cachedMap == null) {
            cachedMap = new HashMap<Integer, PagingResult>();
            discoveryContext.putData(CONTEXT_DATA_PAGE_MAP, cachedMap);
            setPageCacheData(criteria, discoveryContext);
        }
        boolean isMoreResultsPossible = true;
        if (discoveryContext.getData(CONTEXT_DATA_IS_MORE_RESULTS_POSSIBLE) != null) {
            isMoreResultsPossible = isMoreResultsPossible && (Boolean) discoveryContext
                    .getData(CONTEXT_DATA_IS_MORE_RESULTS_POSSIBLE);
        }

        PagingResult result = cachedMap.get(Integer.valueOf(criteria.getPageNumber()));
        if (result == null) {
            result = getNextPage(discoveryContext, credentials, searchString, criteria, locale,
                    webappAdminClient, providerName, loggedInUserName, apiProvider);

            if (result.metadataList.size() > 0) {
                cachedMap.put(criteria.getPageNumber(), result);
            }
            isMoreResultsPossible = isMoreResultsPossible && result.isMoreResultsPossible;
            discoveryContext.putData(CONTEXT_DATA_IS_MORE_RESULTS_POSSIBLE, isMoreResultsPossible);
        }

        DiscoveredApplicationListDTO discoveredApplicationListDTO = translateToDto(
                result.webappsWrapper, result.metadataList, providerName, loggedInUserName,
                apiProvider);

        discoveredApplicationListDTO.setPageCount(cachedMap.size());
        discoveredApplicationListDTO.setMoreResultsPossible(isMoreResultsPossible);
        discoveredApplicationListDTO.setTotalNumberOfPagesKnown(!isMoreResultsPossible);

        return discoveredApplicationListDTO;
    }

    private void setPageCacheData(DiscoverySearchCriteria criteria,
            ApplicationDiscoveryContext discoveryContext) {
        discoveryContext
                .putData(CONTEXT_DATA_SEARCH_APPLICATION_NAME, criteria.getApplicationName());
        discoveryContext.putData(CONTEXT_DATA_SEARCH_APPLICATION_STATUS, criteria.getStatus());
    }

    private boolean isResetPageCache(DiscoverySearchCriteria criteria,
            ApplicationDiscoveryContext discoveryContext) {
        //TODO: Use URL and user name to reset too
        String cachedName = (String) discoveryContext.getData(CONTEXT_DATA_SEARCH_APPLICATION_NAME);
        String cachedStatus = (String) discoveryContext
                .getData(CONTEXT_DATA_SEARCH_APPLICATION_STATUS);

        return !isEqual(cachedName, criteria.getApplicationName()) || !isEqual(cachedStatus,
                criteria.getStatus());
    }

    private void clearCache(ApplicationDiscoveryContext discoveryContext) {
        discoveryContext.clear(CONTEXT_DATA_SEARCH_APPLICATION_NAME);
        discoveryContext.clear(CONTEXT_DATA_PAGE_MAP);
        discoveryContext.clear(CONTEXT_DATA_SEARCH_APPLICATION_STATUS);
        discoveryContext.clear(CONTEXT_DATA_LAST_APPSERVER_PAGE);
        discoveryContext.clear(CONTEXT_DATA_LAST_APPSERVER_INDEX);
        discoveryContext.clear(CONTEXT_DATA_LASTWEBAPPSWRAPER);
        discoveryContext.clear(CONTEXT_DATA_IS_MORE_RESULTS_POSSIBLE);
    }

    private boolean isEqual(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        }
        return s1.equals(s2);
    }

    /**
     * Reads tha data from the server. Handles the pagination with the service session.
     * Strategy:
     *  1: Read the first page from the AS  and filter
     *  2: If the data retrieved can be filled to current page, then return
     *  3: if data is short of page size, then query next page until end of the page form AS occurs.
     *  4: cache the queried pages so that we do not query the AS anu more.
     *
     * @param discoveryContext  the discovery context: keeps track of sessions
     * @param credentials  the credentials of the discovery WSO2-AS
     * @param searchString the search string (name of the applicaiton)
     * @param criteria search criteria
     * @param locale
     * @param webappAdminClient WebappAdmin client for AS
     * @param providerName  Provider name e.g. user1-AT-tenant.domain
     * @param loggedInUserName Currently logged in user
     * @param apiProvider The API Provider service
     * @return
     * @throws AppManagementException
     */
    private PagingResult getNextPage(ApplicationDiscoveryContext discoveryContext,
            DiscoveryCredentials credentials, String searchString, DiscoverySearchCriteria criteria,
            Locale locale,
            AppServerWebappAdminClient webappAdminClient, String providerName,
            String loggedInUserName, APIProvider apiProvider) throws AppManagementException {

        int pageSize = criteria.getPageSize() > 0 ? criteria.getPageSize() : DEFAULT_PAGE_SIZE;
        WebappsWrapper webappsWrapper = null;
        Integer lastAppServerPage = (Integer) discoveryContext
                .getData(CONTEXT_DATA_LAST_APPSERVER_PAGE);
        Integer lastAppServerIndex = (Integer) discoveryContext
                .getData(CONTEXT_DATA_LAST_APPSERVER_INDEX);
        webappsWrapper = (WebappsWrapper) discoveryContext.getData(CONTEXT_DATA_LASTWEBAPPSWRAPER);
        if (lastAppServerPage == null) {
            lastAppServerPage = 0;
        }
        if (lastAppServerIndex == null) {
            lastAppServerIndex = 0;
        }
        List<WebappMetadata> accumulatedMetadataList = new ArrayList<WebappMetadata>();
        boolean isEndOfPages = false;
        boolean isCurrentPageFull = false;
        boolean isLastResultAdded = false;

        //Loads the data from the rest of the previously queried page
        if (webappsWrapper != null) {
            List<WebappMetadata> webappMetadataList = flatten(webappsWrapper.getWebapps());
            List<WebappMetadata> filteredMetadataList = filter(webappsWrapper, webappMetadataList,
                    discoveryContext, credentials, criteria, locale, providerName,
                    loggedInUserName, apiProvider);
            int recordsToAdd = Math
                    .min(pageSize - accumulatedMetadataList.size(), filteredMetadataList.size());
            if (lastAppServerIndex >= recordsToAdd) {
                lastAppServerPage++;
            } else {
                accumulatedMetadataList
                        .addAll(filteredMetadataList.subList(lastAppServerIndex, recordsToAdd));
            }
        }
        //Now read data from the next page
        do {
            webappsWrapper = webappAdminClient
                    .getPagedWebappsSummary(searchString, translateStatus(criteria),
                            translateStatus(criteria), lastAppServerPage);
            List<WebappMetadata> webappMetadataList = flatten(webappsWrapper.getWebapps());
            List<WebappMetadata> filteredMetadataList = filter(webappsWrapper, webappMetadataList,
                    discoveryContext, credentials, criteria, locale, providerName,
                    loggedInUserName, apiProvider);
            int recordsToAdd = Math
                    .min(pageSize - accumulatedMetadataList.size(), filteredMetadataList.size());
            List subList = filteredMetadataList.subList(0, recordsToAdd);
            accumulatedMetadataList.addAll(subList);
            lastAppServerIndex = subList.size();

            isEndOfPages = webappsWrapper.getNumberOfPages() <= lastAppServerPage + 1;
            isCurrentPageFull = (accumulatedMetadataList.size() >= pageSize) || isEndOfPages;
            lastAppServerPage++;
            isLastResultAdded = filteredMetadataList.size() - recordsToAdd <= 0;
        } while (!(isEndOfPages || isCurrentPageFull));

        discoveryContext.putData(CONTEXT_DATA_LAST_APPSERVER_PAGE, lastAppServerPage);
        discoveryContext.putData(CONTEXT_DATA_LAST_APPSERVER_INDEX, lastAppServerIndex);
        discoveryContext.putData(CONTEXT_DATA_LASTWEBAPPSWRAPER, webappsWrapper);

        PagingResult result = new PagingResult(accumulatedMetadataList, webappsWrapper);

        result.isMoreResultsPossible = !(isEndOfPages && isLastResultAdded);
        return result;
    }

    protected APP_STATUS parseAppStatus(String statusString) {
        APP_STATUS result = APP_STATUS.ANY;
        if (statusString == null || statusString.isEmpty()) {
            result = APP_STATUS.ANY;
        } else if (STATUS_CREATED.equalsIgnoreCase(statusString)) {
            result = APP_STATUS.CREATED;
        } else if (STATUS_NEW.equalsIgnoreCase(statusString)) {
            result = APP_STATUS.NEW;
        }

        return result;
    }

    private class PagingResult {

        List<WebappMetadata> metadataList;
        WebappsWrapper webappsWrapper;
        boolean isMoreResultsPossible = true;

        public PagingResult(List<WebappMetadata> metadataList, WebappsWrapper webappsWrapper) {
            this.metadataList = metadataList;
            this.webappsWrapper = webappsWrapper;
        }
    }
}
