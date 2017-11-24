/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.rest.api.publisher.utils.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppAppmetaDTO;
import org.wso2.carbon.appmgt.rest.api.publisher.dto.AppDTO;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is dedicated to validate AppDTO against the given application type
 */
public class AppDTOValidator {
    private static final Log log = LogFactory.getLog(AppDTOValidator.class);

    public static void validateAppDTO(String appType, AppDTO appDTO) {

        if (AppMConstants.MOBILE_ASSET_TYPE.equals(appType)) {

            validateMobileApp(appDTO);
        } else if (AppMConstants.WEBAPP_ASSET_TYPE.equals(appType)) {
            validateWebapp(appDTO);
        }else{
            RestApiUtil.handleBadRequest("Invalid application type provided", log);
        }
    }

    private static void validateWebapp(AppDTO appDTO){


    }

    private static void validateMobileApp(AppDTO appDTO) {
        AppAppmetaDTO appAppmetaDTO = appDTO.getAppmeta();

        //Validate Mandatory fields

        validateMandatoryField("platform", appDTO.getPlatform());

        validateMandatoryField("markettype", appDTO.getMarketType());

        if (validateMandatoryField("appmeta", appAppmetaDTO)) {
            if (AppMConstants.MOBILE_APPS_PLATFORM_ANDROID.equals(appDTO.getPlatform()) ||
                    AppMConstants.MOBILE_APPS_PLATFORM_IOS.equals(appDTO.getPlatform())) {

                if ("enterprise".equals(appDTO.getMarketType())) {
                    validateMandatoryField("path", appAppmetaDTO.getPath());
                    validateMandatoryField("package", appAppmetaDTO.getPackage());
                    validateMandatoryField("version", appAppmetaDTO.getVersion());
                } else if ("public".equals(appDTO.getMarketType())) {
                    validateMandatoryField("package", appAppmetaDTO.getPackage());
                    validateMandatoryField("version", appAppmetaDTO.getVersion());
                } else {
                    RestApiUtil.handleBadRequest("Unsupported market type '" + appDTO.getMarketType() +
                            "' is provided for platform : " + appDTO.getPlatform(), log);
                }
            } else if (AppMConstants.MOBILE_APPS_PLATFORM_WEBAPP.equals(appDTO.getPlatform())) {
                if ("webapp".equals(appDTO.getMarketType())) {
                    validateMandatoryField("weburl", appAppmetaDTO.getWeburl());
                    validateMandatoryField("version", appAppmetaDTO.getVersion());
                } else {
                    RestApiUtil.handleBadRequest("Unsupported market type '" + appDTO.getMarketType() +
                            "' is provided for platform : " + appDTO.getPlatform(), log);
                }
            } else {
                RestApiUtil.handleBadRequest("Unsupported platform '" + appDTO.getPlatform() + "' is provided.", log);
            }
        }
        validateMandatoryField("description", appDTO.getDescription());
        validateMandatoryField("category", appDTO.getCategory());
        validateMandatoryField("banner", appDTO.getBanner());
        validateMandatoryField("iconFile", appDTO.getIcon());
        List<String> screenShots = appDTO.getScreenshots();
        validateMandatoryField("screenshots", screenShots);
        if (screenShots.size() > 3) {
            RestApiUtil.handleBadRequest("Attached screenshots count exceeds the maximum number of allowed screenshots",
                    log);
        }
        while (screenShots.size() < 3) {
            screenShots.add("");
        }

        if (appDTO.getTags() != null) {
            Set<String> apiTags = new HashSet<>(appDTO.getTags());
        }
        List<String> visibleRoleList = new ArrayList<String>();
        visibleRoleList = appDTO.getVisibleRoles();
        if (visibleRoleList != null) {
            String[] visibleRoles = new String[visibleRoleList.size()];
            visibleRoles = visibleRoleList.toArray(visibleRoles);
        }

    }

    private static boolean validateMandatoryField(String fieldName, Object fieldValue) {

        if (fieldValue == null) {
            RestApiUtil.handleBadRequest("Mandatory field  '" + fieldName + "' is not provided.", log);
        }
        return true;
    }

}

