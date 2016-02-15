/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appmgt.impl.template;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfigurationService;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.dto.Environment;

import javax.xml.stream.XMLStreamException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Constructs WebApp and resource configurations for the ESB/Synapse using a Apache velocity
 * templates.
 */
public class APITemplateBuilderImpl implements APITemplateBuilder {

	private static final Log log = LogFactory.getLog(APITemplateBuilderImpl.class);

    private static final String RUNTIME_LOG_LOG_SYSTEM_CLASS_NAME = "org.apache.velocity.runtime.log.Log4JLogChute";
    private static final String RUNTIME_LOG_LOG_SYSTEM_LOG4J_LOGGER = "runtime.log.logsystem.log4j.logger";
	private static final String VELOCITY_TEMPLATE_SYNAPSE_CONFIG_NON_VERSIONED_WEBAPP =
			"velocity-template_synapse-config_non-versioned-webapp.xml";
	private static final String VELOCITY_TEMPLATE_SYNAPSE_CONFIG_VERSIONED_WEBAPP =
			"velocity-template_synapse-config_versioned-webapp.xml";
    private static final String SYNAPSE_PARAM_API_CONTEXT = "apiContext";
    private static final String SYNAPSE_PARAM_FORWARD_APP_CONTEXT = "forwardAppContext";
    private static final String SYNAPSE_PARAM_FORWARD_APP_VERSION = "forwardAppVersion";

	private WebApp webapp;
	private String velocityLoggerName;
	private List<HandlerConfig> handlers = new ArrayList<HandlerConfig>();

	public APITemplateBuilderImpl(WebApp webapp) {
		this.webapp = webapp;
		this.velocityLoggerName = getVelocityLoggerName();
	}

	@Override
	public String getConfigStringForVersionedWebAppTemplate(Environment environment) throws APITemplateException {
		// build the context for template and apply the necessary decorators
		ConfigContext configContext = new APIConfigContext(this.webapp);
		configContext = new TransportConfigContext(configContext, webapp);
		configContext = new ResourceConfigContext(configContext, webapp);
		configContext = new EndpointURIConfigContext(configContext, webapp);
		configContext = new SecurityConfigContext(configContext, webapp);
		configContext = new JwtConfigContext(configContext);
		configContext = new ResponseCacheConfigContext(configContext, webapp);
		configContext = new HandlerConfigContex(configContext, handlers);
		configContext = new EnvironmentConfigContext(configContext, environment);
		configContext = new TemplateUtilContext(configContext);

		try {
			configContext.validate();
		} catch (AppManagementException e) {
            String msg = "Cannot validate configuration context for template";
            log.error(msg, e);
			throw new APITemplateException(msg, e);
		}

		VelocityEngine velocityEngine = new VelocityEngine();
		if (this.velocityLoggerName != null) {
            velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, RUNTIME_LOG_LOG_SYSTEM_CLASS_NAME);
            velocityEngine.setProperty(RUNTIME_LOG_LOG_SYSTEM_LOG4J_LOGGER, velocityLoggerName);
        }
		try {
			velocityEngine.init();
		} catch (Exception e) {
            String msg = "Cannot initialize Velocity engine";
            log.error(msg, e);
			throw new APITemplateException(msg, e);
		}
		VelocityContext context = configContext.getContext();

		return processTemplate(velocityEngine, context, getVersionedWebAppTemplatePath());
	}

	@Override
	public String getConfigStringForNonVersionedWebAppTemplate() throws APITemplateException {
		VelocityEngine velocityengine = new VelocityEngine();
		if (this.velocityLoggerName != null) {
            velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, RUNTIME_LOG_LOG_SYSTEM_CLASS_NAME);
            velocityengine.setProperty(RUNTIME_LOG_LOG_SYSTEM_LOG4J_LOGGER, velocityLoggerName);
		}
		try {
			velocityengine.init();
		} catch (Exception e) {
            String msg = "Cannot initialize Velocity engine";
			log.error(msg, e);
			throw new APITemplateException(msg, e);
		}

		ConfigContext configcontext = new APIConfigContext(this.webapp);
		configcontext = new TransportConfigContext(configcontext, webapp);
		configcontext = new ResourceConfigContext(configcontext, webapp);

		VelocityContext context = configcontext.getContext();
		context.put(SYNAPSE_PARAM_API_CONTEXT, this.webapp.getContext());
		String forwardAppContext = this.webapp.getContext();
		if (forwardAppContext != null && forwardAppContext.charAt(0) == '/') {
			forwardAppContext = forwardAppContext.substring(1);
		}
		context.put(SYNAPSE_PARAM_FORWARD_APP_CONTEXT, forwardAppContext);
		context.put(SYNAPSE_PARAM_FORWARD_APP_VERSION, this.webapp.getId().getVersion());

		return processTemplate(velocityengine, context, getNonVersionedWebAppTemplatePath());
	}

	@Override
	public OMElement getConfigXMLForTemplate(Environment environment) throws APITemplateException {
        String configString = getConfigStringForVersionedWebAppTemplate(environment);
		try {
			return AXIOMUtil.stringToOM(configString);
		} catch (XMLStreamException e) {
			String msg = "Error occurred when converting '" + configString + "' to OMElement";
			log.error(msg, e);
			throw new APITemplateException(msg, e);
		}
	}

	public void addHandler(String handlerName, Map<String, String> properties) {
		addHandlerPriority(handlerName, properties, handlers.size());
	}

	public void addHandlerPriority(String handlerName, Map<String, String> properties, int priority) {
		HandlerConfig handler = new HandlerConfig(handlerName, properties);
		handlers.add(priority, handler);
	}

	private String getVersionedWebAppTemplatePath() {
		return "repository" + File.separator + "resources" + File.separator + "api_templates" +
				File.separator + APITemplateBuilderImpl.VELOCITY_TEMPLATE_SYNAPSE_CONFIG_VERSIONED_WEBAPP;
	}

	private String getNonVersionedWebAppTemplatePath() {
		return "repository" + File.separator + "resources" + File.separator + "api_templates" +
				File.separator + APITemplateBuilderImpl.VELOCITY_TEMPLATE_SYNAPSE_CONFIG_NON_VERSIONED_WEBAPP;
	}

	private String getVelocityLoggerName() {
		AppManagerConfigurationService config =
				ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
		String velocityLogPath = config.getAPIManagerConfiguration().getFirstProperty(
				AppMConstants.VELOCITY_LOGGER);
		if (velocityLogPath != null && velocityLogPath.length() > 1) {
			return velocityLogPath;
		} else {
			return null;
		}
	}

	private String processTemplate(VelocityEngine ve, VelocityContext vc, String templatePath)
			throws APITemplateException {
		StringWriter writer = new StringWriter();
		try {
			Template t = ve.getTemplate(templatePath);
			t.merge(vc, writer);
		} catch (ResourceNotFoundException e) {
			String msg = "Cannot find Velocity template " + templatePath;
			log.error(msg, e);
			throw new APITemplateException(msg, e);
		} catch (ParseErrorException e) {
			String msg = "Cannot parse Velocity template " + templatePath;
			log.error(msg, e);
			throw new APITemplateException(msg, e);
		} catch (IOException e) {
			log.error("Cannot write processed Velocity template", e);
			throw new APITemplateException("Cannot write processed Velocity template", e);
		} catch (Exception e) {
			log.error("Cannot process Velocity template", e);
			throw new APITemplateException("Cannot process Velocity template", e);
		}
		return writer.toString();
	}
}
