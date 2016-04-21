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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;

/**
 * This class contains REST API Publisher related utility operations
 */
public class RestApiPublisherUtils {

    private static final Log log = LogFactory.getLog(RestApiPublisherUtils.class);

    public static String generateBinaryUUID() {

        String possibleCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String uuid = "";
        for (int i = 0; i < 15; i++) {
            uuid += possibleCharacters.charAt((int) Math.floor(Math.random() * possibleCharacters.length()));
        }
        return uuid;
    }

    /**
     * Upload files into storage location
     * @param fileInputStream
     * @param fileDetail
     * @return file API path
     * @throws AppManagementException
     */
    public static String uploadFileContent(InputStream fileInputStream, Attachment fileDetail, String storageLocation)
            throws AppManagementException {

        File binaryFile = new File(storageLocation);
        ContentDisposition contentDisposition = fileDetail.getContentDisposition();
        String fileExtension = FilenameUtils.getExtension(contentDisposition.getParameter("filename"));
        if(fileExtension == null){
            RestApiUtil.handleBadRequest("Please provide a valid file to upload", log);
        }
        String filename = RestApiPublisherUtils.generateBinaryUUID() + "." + fileExtension;
        RestApiUtil.transferFile(fileInputStream, filename, binaryFile.getAbsolutePath());
        return filename;

    }
}
