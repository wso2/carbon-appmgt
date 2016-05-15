package org.wso2.carbon.appmgt.impl;

import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.App;

import java.io.FileInputStream;
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
     * @param contentId static content id
     * @param fileName content filename
     * @param contentLength content length
     * @param contentType content type
     * @param inputStream file input stream
     * @return
     */
    public String persistStaticContents(String contentId, String fileName, int contentLength, String contentType,
                                        InputStream inputStream) throws AppManagementException;

    /**
     * Retrieve the given static content from storage
     * @param contentId static content uuid
     * @return content stream
     */
    public InputStream getStaticContent(String contentId) throws AppManagementException;


}
