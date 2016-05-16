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

package org.wso2.carbon.appmgt.impl;

import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APIStatus;
import org.wso2.carbon.appmgt.api.model.App;
import org.wso2.carbon.appmgt.api.model.MobileApp;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Factory class to create mobile apps.
 */
public class MobileAppFactory extends AppFactory {

    @Override
    public App createApp(GenericArtifact artifact, Registry registry) throws AppManagementException {

        try {

            MobileApp mobileApp = new MobileApp();

            mobileApp.setType(AppMConstants.MOBILE_ASSET_TYPE);

            mobileApp.setUUID(artifact.getId());
            mobileApp.setAppName(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_NAME));
            mobileApp.setAppProvider(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_PROVIDER));
            mobileApp.setVersion(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_VERSION));
            mobileApp.setDescription(artifact.getAttribute(AppMConstants.API_OVERVIEW_DESCRIPTION));

            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            Set<String> tags = new HashSet<String>();
            org.wso2.carbon.registry.core.Tag[] tag = registry.getTags(artifactPath);
            for (org.wso2.carbon.registry.core.Tag tag1 : tag) {
                tags.add(tag1.getTagName());
            }
            mobileApp.addTags(tags);

            //Set Lifecycle status
            if (artifact.getLifecycleState() != null && artifact.getLifecycleState() != "") {
                if (artifact.getLifecycleState().toUpperCase().equalsIgnoreCase(APIStatus.INREVIEW.getStatus())) {
                    mobileApp.setLifeCycleStatus(APIStatus.INREVIEW);
                } else {
                    mobileApp.setLifeCycleStatus(APIStatus.valueOf(artifact.getLifecycleState().toUpperCase()));
                }
            }

            mobileApp.setCategory(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_CATEGORY));
            mobileApp.setPlatform(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_PLATFORM));
            mobileApp.setAppType(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_TYPE));
            mobileApp.setBanner(artifact.getAttribute(AppMConstants.MOBILE_APP_IMAGES_BANNER));
            if (artifact.getAttribute(AppMConstants.MOBILE_APP_IMAGES_SCREENSHOTS) != null) {
                List<String> screenShots = new ArrayList<>(Arrays.asList(artifact.getAttribute(
                        AppMConstants.MOBILE_APP_IMAGES_SCREENSHOTS).split(",")));
                mobileApp.setScreenShots(screenShots);
            }
            mobileApp.setBundleVersion(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_BUNDLE_VERSION));
            mobileApp.setPackageName(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_PACKAGE_NAME));
            mobileApp.setThumbnail(artifact.getAttribute(AppMConstants.MOBILE_APP_IMAGES_THUMBNAIL));
            mobileApp.setAppUrl(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_URL));

            mobileApp.setRecentChanges(artifact.getAttribute(AppMConstants.MOBILE_APP_OVERVIEW_RECENT_CHANGES));
            mobileApp.setCreatedTime(artifact.getAttribute(AppMConstants.API_OVERVIEW_CREATED_TIME));

            if (artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBILITY) != null) {
                mobileApp.setAppVisibility(artifact.getAttribute(AppMConstants.API_OVERVIEW_VISIBILITY).split(","));
            }

            return mobileApp;

        } catch (GovernanceException e) {
            String errorMessage = "Error while creating the mobile app object from the registry artifact.";
            throw new AppManagementException(errorMessage, e);
        } catch (RegistryException e) {
            String errorMessage = "Error while creating the mobile app object from the registry artifact.";
            throw new AppManagementException(errorMessage, e);
        }
    }

}
