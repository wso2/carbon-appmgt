/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.gateway.handlers.common;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;

import java.util.Map;

public class SynapsePropertiesHandler extends AbstractHandler {

	@SuppressWarnings("unchecked")
	public boolean handleRequest(MessageContext messageContext) {

		org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
				getAxis2MessageContext();
		Map<String, String> headers = (Map<String, String>) axis2MC.getProperty(
				org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
		String ipWithPort = headers.get("HOST");
		String ip = ipWithPort.substring(0, ipWithPort.indexOf(':'));

		String httpPort = System.getProperty("http.nio.port");
		String httpsPort = System.getProperty("https.nio.port");

		messageContext.setProperty("synapse.host.ip", ip);
		messageContext.setProperty("http.nio.port", httpPort);
		messageContext.setProperty("https.nio.port", httpsPort);
		return true;
	}

	public boolean handleResponse(MessageContext messageContext) {
		return true;
	}
}
