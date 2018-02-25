/*
 *
 *   Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.appmgt.mobile.mdm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.mobile.utils.HostResolver;
import org.wso2.carbon.appmgt.mobile.utils.MobileConfigurations;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

import java.io.File;

/**
 * This class will loads data to app instance based on the artifact
 */
public class AppDataLoader {

    private static final Log log = LogFactory.getLog(AppDataLoader.class);
    private static final String IOS_DEFAULT_TRANSPORT_PROTOCOL = "https";

    /**
     * Load the data to the empty app instance based on the artifact
     *
     * @param app      Instance of the empty app
     * @param artifact Artifact of the mobile app
     * @param action   Action of the operation
     * @param tenantId Tenant Id
     * @return App which is filled by artifact
     */
    public static App load(App app, GenericArtifact artifact, String action, int tenantId) {

        try {
            app.setId(artifact.getId());
            app.setName(artifact.getAttribute("overview_name"));
            app.setPlatform(artifact.getAttribute("overview_platform"));
            app.setVersion(artifact.getAttribute("overview_version"));
            app.setType(artifact.getAttribute("overview_type"));
            app.setIconImage(HostResolver.getHostWithHTTP() + artifact.getAttribute("images_thumbnail"));

            if ("enterprise".equals(artifact.getAttribute("overview_type"))) {
                app.setType(artifact.getAttribute("overview_type"));
                if ("install".equals(action) || "update".equals(action)) {
                    if ("android".equals(artifact.getAttribute("overview_platform"))) {
                        app.setLocation(HostResolver.getHost(MobileConfigurations.getInstance().getMDMConfigs()
                                                                     .get(MobileConfigurations.APP_DOWNLOAD_URL_HOST)) +
                                                MobileConfigurations.APP_GET_URL
                                                + artifact.getAttribute("overview_url"));
                    } else if ("ios".equals(artifact.getAttribute("overview_platform"))) {
                        String fileName = new File(artifact.getAttribute("overview_url")).getName();
                        app.setLocation(HostResolver.getHost(IOS_DEFAULT_TRANSPORT_PROTOCOL) + "/" +
                                                MobileConfigurations.getInstance().getInstance()
                                                        .getMDMConfigs().get(MobileConfigurations.IOS_PLIST_PATH) +
                                                "/" + tenantId + "/" + fileName);
                    }
                }

            } else if ("public".equals(artifact.getAttribute("overview_type"))) {
                app.setType(artifact.getAttribute("overview_type"));
            } else if ("webapp".equals(artifact.getAttribute("overview_type"))) {
                app.setType(artifact.getAttribute("overview_type"));
                app.setLocation(artifact.getAttribute("overview_url"));
                app.setIdentifier(artifact.getAttribute("overview_url"));
            }


            if ("android".equals(artifact.getAttribute("overview_platform"))) {
                app.setPackageName(artifact.getAttribute("overview_packagename"));
                app.setIdentifier(artifact.getAttribute("overview_packagename"));
                app.setAppIdentifier(artifact.getAttribute("overview_packagename"));
            } else if ("ios".equals(artifact.getAttribute("overview_platform"))) {
                app.setPackageName(artifact.getAttribute("overview_packagename"));
                app.setAppIdentifier(artifact.getAttribute("overview_appid"));
                app.setIdentifier(artifact.getAttribute("overview_appid"));
            }
        } catch (GovernanceException e) {
            log.error("Error occurred while retrieving information from governance registry", e);
        }finally {
            return app;
        }

    }
}
