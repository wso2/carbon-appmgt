package org.wso2.carbon.appmgt.impl.template;

import org.apache.velocity.VelocityContext;
import org.json.simple.JSONObject;
import org.wso2.carbon.appmgt.api.model.WebApp;

public class EndpointURIConfigContext extends ConfigContextDecorator {

	private WebApp api;

	public EndpointURIConfigContext(ConfigContext context, WebApp api) {
		super(context);
		this.api = api;
	}

	@Override
	public VelocityContext getContext() {
		VelocityContext context = super.getContext();

		JSONObject obj = new JSONObject();
		obj.put("endpoint_type", "http");

		JSONObject enpoints = new JSONObject();
		enpoints.put("url", api.getUrl());
		enpoints.put("config", null);

		obj.put("production_endpoints", enpoints);
		context.put("endpoint_config", obj);

		return context;

	}

}
