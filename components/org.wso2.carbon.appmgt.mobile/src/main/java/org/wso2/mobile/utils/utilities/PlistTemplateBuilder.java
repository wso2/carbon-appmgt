/*
 *
 *  * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * you may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.mobile.utils.utilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.wso2.carbon.appmgt.api.model.MobileApp;
import org.wso2.carbon.appmgt.api.model.PlistTemplateContext;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicy;
import org.wso2.carbon.appmgt.api.model.entitlement.XACMLPolicyTemplateContext;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

import java.io.File;
import java.io.StringWriter;

/**
 * This class is responsible for building XACML policies using templates and partials.
 */
public class PlistTemplateBuilder {

    private static final Log log = LogFactory.getLog(PlistTemplateBuilder.class);
    private static final String TEMPLATE_NAME = "plist_template";
    private static final String DOWNLOAD_URL = "downloadURL";
    private static final String PACKAGE_NAME = "packageName";
    private static final String BUNDLE_VERSION = "bundleVersion";
    private static final String APP_NAME = "appName";


    private VelocityEngine velocityEngine;

    public PlistTemplateBuilder(){
        velocityEngine = new VelocityEngine();
        try {
            velocityEngine.init();
        } catch (Exception e) { // VelocityEngine throws a generic exception hence it needs to be caught
            log.error("Cannot initialize Velocity Engine.", e);
        }
    }

    public String generatePlistConfig(PlistTemplateContext plistTemplateContext){

        try {
            Template template = velocityEngine.getTemplate(getTemplatePath());
            StringWriter writer = new StringWriter();
            template.merge(getVelocityContext(plistTemplateContext), writer);
            return writer.toString();

        } catch (Exception e) {
            // VelocityEngine throws a generic exception hence it needs to be caught
            log.error("Error while generating Plist configuration for PlistTemplateContext", e);
            return null;
        }
    }

    private String getTemplatePath() {
        return "repository" + File.separator + "resources" + File.separator + "plist-templates" +
                File.separator + PlistTemplateBuilder.TEMPLATE_NAME + ".xml";
    }

    private VelocityContext getVelocityContext(PlistTemplateContext plistTemplateContext){

        VelocityContext velocityContext = new VelocityContext();

        velocityContext.put(DOWNLOAD_URL, plistTemplateContext.getOneTimeDownloadUrl());
        velocityContext.put(PACKAGE_NAME, plistTemplateContext.getPackageName());
        velocityContext.put(BUNDLE_VERSION, plistTemplateContext.getBundleVersion());
        velocityContext.put(APP_NAME, plistTemplateContext.getAppName());

        return velocityContext;
    }


}
