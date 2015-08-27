/*
 * Copyright WSO2 Inc.
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

	private static final String VELOCITY_TEMPLATE_SYNAPSE_CONFIG_NON_VERSIONED_WEBAPP =
			"velocity-template_synapse-config_non-versioned-webapp.xml";
	private static final String VELOCITY_TEMPLATE_SYNAPSE_CONFIG_VERSIONED_WEBAPP =
			"velocity-template_synapse-config_versioned-webapp.xml";

	private WebApp api;
	private String velocityLoggerName = null;
	private List<HandlerConfig> handlers = new ArrayList<HandlerConfig>();

	public APITemplateBuilderImpl(WebApp api) {
		this.api = api;
		this.velocityLoggerName = getVelocityLoggerName();
	}

	@Override
	public String getConfigStringForVersionedWebAppTemplate(Environment environment)
			throws APITemplateException {

		// build the context for template and apply the necessary decorators
		ConfigContext configcontext = new APIConfigContext(this.api);
		configcontext = new TransportConfigContext(configcontext, api);
		configcontext = new ResourceConfigContext(configcontext, api);
		configcontext = new EndpointURIConfigContext(configcontext, api);
		configcontext = new SecurityConfigContext(configcontext, api);
		configcontext = new JwtConfigContext(configcontext);
		configcontext = new ResponseCacheConfigContext(configcontext, api);
		configcontext = new HandlerConfigContex(configcontext, handlers);
		configcontext = new EnvironmentConfigContext(configcontext, environment);
		configcontext = new TemplateUtilContext(configcontext);

		// @todo: this validation might be better to do when the builder is initialized.
		try {
			configcontext.validate();
		} catch (Exception e) {
			log.error("Cannot validate configuration context for template", e);
			throw new APITemplateException("Cannot validate configuration context for template",
										   e);
		}

		VelocityEngine velocityengine = new VelocityEngine();
		if (this.velocityLoggerName != null) {
			velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
									   "org.apache.velocity.runtime.log.Log4JLogChute");
			velocityengine.setProperty("runtime.log.logsystem.log4j.logger", velocityLoggerName);
		}
		try {
			velocityengine.init();
		} catch (Exception e) {
			log.error("Cannot initialize Velocity engine", e);
			throw new APITemplateException("Cannot initialize Velocity engine", e);
		}
		VelocityContext context = configcontext.getContext();

		return processTemplate(velocityengine, context, getVersionedWebAppTemplatePath());
	}

	@Override
	public String getConfigStringForNonVersionedWebAppTemplate() throws APITemplateException {
		VelocityEngine velocityengine = new VelocityEngine();
		if (this.velocityLoggerName != null) {
			velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
									   "org.apache.velocity.runtime.log.Log4JLogChute");
			velocityengine.setProperty("runtime.log.logsystem.log4j.logger", velocityLoggerName);
		}
		try {
			velocityengine.init();
		} catch (Exception e) {
			log.error("Cannot initialize Velocity engine", e);
			throw new APITemplateException("Cannot initialize Velocity engine", e);
		}

		ConfigContext configcontext = new APIConfigContext(this.api);
		configcontext = new TransportConfigContext(configcontext, api);
		configcontext = new ResourceConfigContext(configcontext, api);

		VelocityContext context = configcontext.getContext();
		context.put("apiContext", this.api.getContext());
		String forwardAppContext = this.api.getContext();
		if (forwardAppContext != null && forwardAppContext.charAt(0) == '/') {
			forwardAppContext = forwardAppContext.substring(1);
		}
		context.put("forwardAppContext", forwardAppContext);
		context.put("forwardAppVersion", this.api.getId().getVersion());

		return processTemplate(velocityengine, context, getNonVersionedWebAppTemplatePath());
	}

	@Override
	public OMElement getConfigXMLForTemplate(Environment environment) throws APITemplateException {
		try {
			return AXIOMUtil.stringToOM(getConfigStringForVersionedWebAppTemplate(environment));
		} catch (XMLStreamException e) {
			String msg = "Error converting string to OMElement - String: " +
					getConfigStringForVersionedWebAppTemplate(environment);
			log.error(msg, e);
			throw new APITemplateException(msg, e);
		}
	}

	public void addHandler(String handlerName, Map<String, String> properties) {
		addHandlerPriority(handlerName, properties, handlers.size());
	}

	public void addHandlerPriority(String handlerName, Map<String, String> properties,
								   int priority) {
		HandlerConfig handler = new HandlerConfig(handlerName, properties);
		handlers.add(priority, handler);
	}

	private String getVersionedWebAppTemplatePath() {
		return "repository" + File.separator + "resources" + File.separator + "api_templates" +
				File.separator +
				APITemplateBuilderImpl.VELOCITY_TEMPLATE_SYNAPSE_CONFIG_VERSIONED_WEBAPP;
	}

	private String getNonVersionedWebAppTemplatePath() {
		return "repository" + File.separator + "resources" + File.separator + "api_templates" +
				File.separator +
				APITemplateBuilderImpl.VELOCITY_TEMPLATE_SYNAPSE_CONFIG_NON_VERSIONED_WEBAPP;
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
