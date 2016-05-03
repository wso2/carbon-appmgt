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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.MobileApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.CarbonUtils;

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

    public static String uploadFileIntoStorage(InputStream fileInputStream, String filename) throws AppManagementException {
        AppManagerConfiguration appManagerConfiguration = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String directoryLocation = CarbonUtils.getCarbonHome() + File.separator +
                appManagerConfiguration.getFirstProperty(AppMConstants.MOBILE_APPS_FILE_PRECISE_LOCATION);
        File binaryFile = new File(directoryLocation);
        //Generate UUID for the uploading file
        RestApiUtil.transferFile(fileInputStream, filename, binaryFile.getAbsolutePath());
        return directoryLocation + File.separator + filename;
    }

}
