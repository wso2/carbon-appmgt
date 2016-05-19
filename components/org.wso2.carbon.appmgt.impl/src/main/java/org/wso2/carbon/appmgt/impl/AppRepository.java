package org.wso2.carbon.appmgt.impl;

import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.App;
import org.wso2.carbon.appmgt.api.model.FileContent;
import org.wso2.carbon.governance.api.exception.GovernanceException;

import java.io.InputStream;
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
     * Searches and returns the apps for the given search terms.
     *
     * @param appType
     * @param searchTerms
     * @return
     * @throws AppManagementException
     */
    public List<App> searchApps(String appType, Map<String, String> searchTerms) throws AppManagementException, GovernanceException;

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


}
