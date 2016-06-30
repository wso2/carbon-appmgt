package org.wso2.carbon.appmgt.impl;

import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.*;

import java.util.List;
import java.util.Map;

/**
 * The interface for app repository implementations.
 */
public interface AppRepository {


    /**
     *
     * Persists the given app in the repository.
     *
     * @param app
     * @return
     */
    String saveApp(App app) throws AppManagementException;

    /**
     *
     * Creates and persists a new version using the attributes in the given app.
     *
     * @param app
     * @return
     */
    String createNewVersion(App app) throws AppManagementException;

    /**
     * Updates the given app.
     *
     * @param app
     * @throws AppManagementException
     */
    void updateApp(App app) throws AppManagementException;

    /**
     *
     * Returns the app for the given uuid.
     *
     * @param type
     * @param uuid
     * @return
     * @throws AppManagementException
     */
    App getApp(String type, String uuid) throws AppManagementException;

    /**
     *
     * Returns the web app which has the give context and the version.
     *
     * @param context
     * @param version
     * @param tenantId
     * @return
     * @throws AppManagementException
     */
    WebApp getWebAppByContextAndVersion(String context, String version, int tenantId) throws AppManagementException;

    /**
     * Searches and returns the apps for the given search terms.
     *
     * @param appType
     * @param searchTerms
     * @return
     * @throws AppManagementException
     */
    public List<App> searchApps(String appType, Map<String, String> searchTerms) throws AppManagementException;

    /**
     * Save static content into storage
     * @param fileContent details of file content
     * @return
     * @throws AppManagementException
     */
    public void persistStaticContents(FileContent fileContent)  throws AppManagementException;

    /**
     * Retrieve the given static content from storage
     * @param contentId static content uuid
     * @return content stream
     */
    public FileContent getStaticContent(String contentId) throws AppManagementException;

    /**
     * Add user subscription for a webapp/site
     * @param subscriberName Subscriber username
     * @param webApp WebApp object
     * @param applicationName Application Name
     * @return subscription id
     * @throws AppManagementException
     */
    public int addSubscription(String subscriberName, WebApp webApp, String applicationName) throws AppManagementException;

    /**
     * Persist one-time download link reference in database
     * @param oneTimeDownloadLink OneTimeDownloadLink content
     * @throws AppManagementException
     */
    public void persistOneTimeDownloadLink(OneTimeDownloadLink oneTimeDownloadLink) throws AppManagementException;

    /**
     * Retrieve one-time download link details from database
     * @param UUID UUID of the one-time download link
     * @return
     * @throws AppManagementException
     */
    public OneTimeDownloadLink getOneTimeDownloadLinkDetails(String UUID) throws AppManagementException;

    /**
     * Update one-time download link details in database
     * @param oneTimeDownloadLink OneTimeDownloadLink content
     * @throws AppManagementException
     */
    public void updateOneTimeDownloadLinkStatus(OneTimeDownloadLink oneTimeDownloadLink) throws AppManagementException;

    Subscription getEnterpriseSubscription(String webAppContext, String webAppVersion) throws AppManagementException;
}
