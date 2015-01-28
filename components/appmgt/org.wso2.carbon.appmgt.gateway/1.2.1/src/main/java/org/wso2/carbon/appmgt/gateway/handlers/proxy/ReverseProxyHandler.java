package org.wso2.carbon.appmgt.gateway.handlers.proxy;

import java.util.TreeMap;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.HTTPEndpoint;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;

public class ReverseProxyHandler extends AbstractHandler {

	private static final String URL_SEPERATOR = "/";
	private static final String EMPTY_STRING = "";
	private static final String SET_COOKIE_PATH = "Path=";
	private static final int SET_COOKIE_PATH_LENGTH = SET_COOKIE_PATH.length();
	private static final String SEMICOLON = ";";

	public boolean handleRequest(MessageContext arg0) {
		return true;
	}

	public boolean handleResponse(MessageContext messageContext) {

		org.apache.axis2.context.MessageContext axis2MC =
		                                                  ((Axis2MessageContext) messageContext).getAxis2MessageContext();
		TreeMap headers =
		                  (TreeMap) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
		String webContextWithVersion =
		                               String.valueOf(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)) +
		                                       URL_SEPERATOR +
		                                       String.valueOf(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));
		int status = Integer.parseInt(String.valueOf(axis2MC.getProperty(NhttpConstants.HTTP_SC)));
		if (status == 302 || status == 301) {
			// Set the location to the gateway URL
			HTTPEndpoint endpoint =
			                        (HTTPEndpoint) messageContext.getProperty(SynapseConstants.LAST_ENDPOINT);
			String endpointUrl = endpoint.getDefinition().getAddress();

			String gatewayContext =
			                        String.valueOf(messageContext.getProperty(RESTConstants.REST_URL_PREFIX)) +
			                                webContextWithVersion +
			                                (endpointUrl.endsWith(URL_SEPERATOR) ? URL_SEPERATOR
			                                                                    : EMPTY_STRING);

			String location = String.valueOf((headers).get(HTTPConstants.HEADER_LOCATION));
			location = location.replace(endpointUrl, gatewayContext);

			headers.put(HTTPConstants.HEADER_LOCATION, location);

		}

		Object cookie = headers.get(HTTPConstants.HEADER_SET_COOKIE);
		if (cookie != null) {
			String cookieString = String.valueOf(cookie);
			int start = cookieString.indexOf(SET_COOKIE_PATH) + SET_COOKIE_PATH_LENGTH;
			int end = cookieString.indexOf(SEMICOLON, start + 1);
			end = end == -1 ? cookieString.length() : end;
			String remoteContext = cookieString.substring(start, end);
			cookieString = cookieString.replace(remoteContext, webContextWithVersion);

			headers.put(HTTPConstants.HEADER_SET_COOKIE, cookieString);
		}

		// final Pipe pipe = (Pipe)
		// axis2MC.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
		// if (pipe != null &&
		// !Boolean.TRUE.equals(messageContext.getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED)))
		// {
		// InputStream in = pipe.getInputStream();
		//
		// // try {
		// // RelayUtils.builldMessage(axis2MC, false, in);
		// // } catch (AxisFault e) {
		// // e.printStackTrace();
		// // } catch (IOException e) {
		// // e.printStackTrace();
		// // }
		//
		// }

		return true;
	}
}
