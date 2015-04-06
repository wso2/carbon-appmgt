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


public class AppDataLoader {

    private static final Log log = LogFactory.getLog(AppDataLoader.class);

    public static App load(App app, GenericArtifact artifact, String action, int tenantId){

        try {
            app.setId(artifact.getId());
            app.setName(artifact.getAttribute("overview_name"));
            app.setPlatform(artifact.getAttribute("overview_platform"));
            app.setVersion(artifact.getAttribute("overview_version"));
            app.setType(artifact.getAttribute("overview_type"));
            app.setIconImage(HostResolver.getHostWithHTTP() + artifact.getAttribute("images_thumbnail"));

            if("Enterprise".equals(artifact.getAttribute("overview_type"))){
                app.setType("enterprise");


                if("install".equals(action)){
                    if("android".equals(artifact.getAttribute("overview_platform"))){
                        app.setLocation(HostResolver.getHost(MobileConfigurations.getInstance().getAppDownloadHost()) + artifact.getAttribute("overview_url"));
                    }else  if("ios".equals(artifact.getAttribute("overview_platform"))){
                        String fileName = new File(artifact.getAttribute("overview_url")).getName();
                        app.setLocation(HostResolver.getHost(MobileConfigurations.getInstance().getAppDownloadHost()) + "/" + MobileConfigurations.getInstance().getIosPlistPath() + "/" + tenantId + "/"  + fileName);
                    }
                }

            }else if ("Market".equals(artifact.getAttribute("overview_type"))){
                app.setType("public");
            }else if ("Web App".equals(artifact.getAttribute("overview_type"))){
                app.setType("webapp");
                app.setLocation(artifact.getAttribute("overview_url"));
                app.setIdentifier(artifact.getAttribute("overview_url"));
            }


            if("android".equals(artifact.getAttribute("overview_platform"))){
                app.setPackageName(artifact.getAttribute("overview_packagename"));
                app.setIdentifier(artifact.getAttribute("overview_packagename"));
            }else  if("ios".equals(artifact.getAttribute("overview_platform"))){
                app.setPackageName(artifact.getAttribute("overview_packagename"));
                app.setAppIdentifier(artifact.getAttribute("overview_appid"));
                app.setIdentifier(artifact.getAttribute("overview_appid"));
            }
        } catch (GovernanceException e) {
            log.error("Error occurred while retrieving information from governance registry");
            log.debug("Error: " + e);
        }finally {
            return app;
        }

    }
}
