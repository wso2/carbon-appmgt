package org.wso2.carbon.appmgt.impl;

import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.App;
import org.wso2.carbon.appmgt.api.model.FileContent;

import java.io.InputStream;

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
