/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appmgt.impl.entitlement;

import java.io.File;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicy;
import org.wso2.carbon.appmgt.api.model.entitlement.XACMLPolicyTemplateContext;

/**
 * This class is responsible for building XACML policies using templates and partials.
 */
public class XACMLTemplateBuilder {

    private static final Log log = LogFactory.getLog(XACMLTemplateBuilder.class);
    private static final String TEMPLATE_NAME = "xacml_policy_template";
    private static final String MOCK_TEMPLATE_NAME = "mock_xacml_policy_template";

    private VelocityEngine velocityEngine;

    public XACMLTemplateBuilder(){
        velocityEngine = new VelocityEngine();
        try {
            velocityEngine.init();
        } catch (Exception e) { // VelocityEngine throws a generic exception hence it needs to be caught
            log.error("Cannot initialize Velocity Engine.", e);
        }
    }

    /**
     * Generate Entitlement policy with a given XACML policy context
     * @param xacmlPolicyTemplateContext XACML policy context
     * @return Entitlement policy
     */
    public EntitlementPolicy generatePolicy(XACMLPolicyTemplateContext xacmlPolicyTemplateContext){

        EntitlementPolicy entitlementPolicy = null;
        try {
            Template template = velocityEngine.getTemplate(getTemplatePath());

            StringWriter writer = new StringWriter();
            template.merge(getVelocityContext(xacmlPolicyTemplateContext), writer);

            entitlementPolicy = new EntitlementPolicy();

            entitlementPolicy.setPolicyContent(writer.toString());
            entitlementPolicy.setPolicyId(xacmlPolicyTemplateContext.getPolicyId());

            return entitlementPolicy;

        } catch (Exception e) {
            // VelocityEngine throws a generic exception hence it needs to be caught
            log.error("Error while generating XACML policy for XacmlPolicyTemplateContext", e);
            return null;
        }
    }

    /**
     * Generate mock policy with a given policy partial
     * @param partial policy partial
     * @return mock policy
     */
    public String generateMockPolicy(String partial){
        try {
            Template template = velocityEngine.getTemplate(getMockTemplatePath());

            StringWriter writer = new StringWriter();

            VelocityContext context = new VelocityContext();
            context.put("condition", partial);

            template.merge(context, writer);
            return writer.toString();
        } catch (Exception e) {
            // VelocityEngine throws a generic exception hence it needs to be caught
            log.error("Error while generating XACML policy with policy partial : " + partial, e);
            return null;
        }
    }

    private String getTemplatePath() {
        return "repository" + File.separator + "resources" + File.separator + "entitlement-templates" +
                File.separator + XACMLTemplateBuilder.TEMPLATE_NAME + ".xml";
    }


    private String getMockTemplatePath() {
        return "repository" + File.separator + "resources" + File.separator + "entitlement-templates" +
                File.separator + XACMLTemplateBuilder.MOCK_TEMPLATE_NAME + ".xml";
    }

    private VelocityContext getVelocityContext(XACMLPolicyTemplateContext policyTemplateContext){

        VelocityContext velocityContext = new VelocityContext();

        velocityContext.put("appUuid", policyTemplateContext.getAppUuid());
        velocityContext.put("ruleId", policyTemplateContext.getRuleId());
        velocityContext.put("ruleContent", policyTemplateContext.getRuleContent());
        velocityContext.put("policyId", policyTemplateContext.getPolicyId());

        return velocityContext;
    }


}
