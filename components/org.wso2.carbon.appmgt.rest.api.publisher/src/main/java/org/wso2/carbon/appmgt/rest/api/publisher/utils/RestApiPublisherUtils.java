/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.rest.api.publisher.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.Documentation;
import org.wso2.carbon.appmgt.api.model.FileContent;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

/**
 * This class contains REST API Publisher related utility operations
 */
public class RestApiPublisherUtils {

    private static final Log log = LogFactory.getLog(RestApiPublisherUtils.class);
    final static String possibleCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateBinaryUUID() {

        String uuid = "";
        for (int i = 0; i < 15; i++) {
            uuid += possibleCharacters.charAt((int) Math.floor(Math.random() * possibleCharacters.length()));
        }
        return uuid;
    }

    public static String uploadFileIntoStorage(FileContent fileContent) throws AppManagementException {
        AppManagerConfiguration appManagerConfiguration = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String directoryLocation =
                appManagerConfiguration.getFirstProperty(AppMConstants.BINARY_FILE_STORAGE_ABSOLUTE_LOCATION);
        File binaryFile = new File(directoryLocation);
        //Generate UUID for the uploading file
        RestApiUtil.transferFile(fileContent.getContent(), fileContent.getFileName(), binaryFile.getAbsolutePath());
        return directoryLocation + File.separator + fileContent.getFileName();
    }

    public static String getCreatedTimeEpoch() {

        int prefix = AppMConstants.ASSET_CREATED_DATE_LENGTH;
        long createdTimeStamp = new Date().getTime();
        String time = String.valueOf(createdTimeStamp);

        if (time.length() != prefix) {
            for (int i = 0; i < prefix - time.length(); i++) {
                time =  "0"+time;
            }
        }
        return time;
    }

    /**
     * Attaches a file to the specified document
     *
     * @param webApp WebApp object
     * @param documentation Documentation object
     * @param inputStream input Stream containing the file
     * @param fileDetails file details object as cxf Attachment
     * @throws AppManagementException if unable to add the file
     */
    public static void attachFileToDocument(WebApp webApp, Documentation documentation, InputStream inputStream,
                                            Attachment fileDetails) throws AppManagementException {

        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String documentId = documentation.getId();
        String randomFolderName = RandomStringUtils.randomAlphanumeric(10);
        String tmpFolder = System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator
                + RestApiConstants.DOC_UPLOAD_TMPDIR + File.separator + randomFolderName;
        File docFile = new File(tmpFolder);

        boolean folderCreated = docFile.mkdirs();
        if (!folderCreated) {
            RestApiUtil.handleInternalServerError("Failed to add content to the document " + documentId, log);
        }

        InputStream docInputStream = null;
        try {
            ContentDisposition contentDisposition = fileDetails.getContentDisposition();
            String filename = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
            if (StringUtils.isBlank(filename)) {
                filename = RestApiConstants.DOC_NAME_DEFAULT + randomFolderName;
                log.warn(
                        "Couldn't find the name of the uploaded file for the document " + documentId + ". Using name '"
                                + filename + "'");
            }

            RestApiUtil.transferFile(inputStream, filename, docFile.getAbsolutePath());
            docInputStream = new FileInputStream(docFile.getAbsolutePath() + File.separator + filename);
            String mediaType = fileDetails.getHeader(RestApiConstants.HEADER_CONTENT_TYPE);
            mediaType = mediaType == null ? RestApiConstants.APPLICATION_OCTET_STREAM : mediaType;
            apiProvider.addFileToDocumentation(webApp, documentation, filename, docInputStream, mediaType);
            apiProvider.updateDocumentation(webApp.getId(), documentation);
            docFile.deleteOnExit();
        } catch (FileNotFoundException e) {
            RestApiUtil.handleInternalServerError("Unable to read the file from path ", e, log);
        } finally {
            IOUtils.closeQuietly(docInputStream);
        }
    }

}
