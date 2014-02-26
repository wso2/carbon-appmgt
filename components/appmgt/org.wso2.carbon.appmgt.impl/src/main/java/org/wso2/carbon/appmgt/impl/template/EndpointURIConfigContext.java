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

		enpoints = new JSONObject();
		enpoints.put("url", api.getSandboxUrl());
		enpoints.put("config", null);

		obj.put("sandbox_endpoints", enpoints);

		// {"production_endpoints":{"url":"http://10.100.0.131:9443/test-web","config":null},"sandbox_endpoints":{"url":"http://10.100.0.131:9443/test-web","config":null},"endpoint_type":"http"}

		// JSONObject type = new JSONObject();
		// type.put("type", "http");
		// context.put("endpoint_type", type);
		//
		// JSONObject endpoint = new JSONObject();
		// endpoint.put("url", api.getSandboxUrl());
		// endpoint.put("config", null);
		//
		// context.put("sandbox_endpoints", endpoint);
		//
		// endpoint = new JSONObject();
		// endpoint.put("url", api.getUrl());
		// endpoint.put("config", null);
		//
		// context.put("production_endpoints", endpoint);
		//
		context.put("endpoint_config", obj);

		return context;

	}

}
