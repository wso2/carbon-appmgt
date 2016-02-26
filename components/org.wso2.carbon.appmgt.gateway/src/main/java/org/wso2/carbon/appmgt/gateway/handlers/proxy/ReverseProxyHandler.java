/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.appmgt.gateway.handlers.proxy;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.HTTPEndpoint;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReverseProxyHandler extends AbstractHandler {

	private static final String URL_SEPERATOR = "/";
	private static final String EMPTY_STRING = "";
	private static final long MAX_AGE_UNSPECIFIED = -1;

	public boolean handleRequest(MessageContext messageContext) {
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

		// If the user enter gateway endpoint without "/" end of the request, relative path resource will no be loaded.
		// In this logic it identified such cases and append the "/" to the end of the request and rewrite the response
		// as a request to the gateway.
		String appVersion = String.valueOf(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));
		String requestContextPath = String.valueOf(messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH));
		if (requestContextPath.endsWith(appVersion)) {
			String endpointUrl = String.valueOf(messageContext.getProperty(RESTConstants.REST_URL_PREFIX)) + requestContextPath;
			String gatewayContext = endpointUrl +
							(endpointUrl.endsWith(URL_SEPERATOR) ? EMPTY_STRING : URL_SEPERATOR);
			headers.put(HTTPConstants.HEADER_LOCATION, gatewayContext);
			axis2MC.setProperty(NhttpConstants.HTTP_SC, 302);
		}

		fixCookiePaths(axis2MC, headers, webContextWithVersion);

		return true;
	}

	/**
	 * Fixes the cookie paths if there exists any in the response so that it matches only to this context.
	 * Otherwise cookies destined to other applications will also arrive onto this application. Because most cookies are
	 * set to the root context or the context of the the original web application.
	 *
	 * The headers.get(x) from axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)
	 * only returns single header entry even the transport contains multiple headers with the same header name. However
	 * the other headers are put into axis2MC.getProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS).
	 *
	 * This method re-writes the Cookie Path value so that it always starts with "ProxyContext/Version" on all the
	 * "Set-Cookie" headers.
	 *
	 * @param axis2MC
	 * @param headers
	 * @param webContextWithVersion
	 */
	private void fixCookiePaths(org.apache.axis2.context.MessageContext axis2MC, TreeMap headers,
			String webContextWithVersion) {

		Object cookieFromHeader = headers.get(HTTPConstants.HEADER_SET_COOKIE);
		if (cookieFromHeader == null) {
			//No cookie in main header, which means no cookies available in excess headers. No need to continue.
			return;
		}

		//Fix the path in the cookie in the header (primary map).
		List<HttpCookie> httpCookies = HttpCookie.parse(String.valueOf(cookieFromHeader));
		for (HttpCookie httpCookie : httpCookies) {
			String newPath = replaceCookieContextPath(webContextWithVersion, httpCookie);
			httpCookie.setPath(newPath);
			//Put the last cookie as the header cookie. This works as there is only one header cookie in the list.
			headers.put(HTTPConstants.HEADER_SET_COOKIE, toHeaderString(httpCookie));
		}

		//Now Fix the paths in Excess Cookies.
		Map<String, Object> excessHeaders = (Map<String, Object>) axis2MC
				.getProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS);
		List<String> excessCookies = (List<String>) excessHeaders.get(HTTPConstants.HEADER_SET_COOKIE);
		if (excessCookies != null) {
			List<String> fixedCookies = new ArrayList<>();
			for (String cookie : excessCookies) {
				List<HttpCookie> httpExcessCookies = HttpCookie.parse(cookie);
				for (HttpCookie httpCookie : httpExcessCookies) {
					String newPath = replaceCookieContextPath(webContextWithVersion, httpCookie);
					httpCookie.setPath(newPath);
					fixedCookies.add(toHeaderString(httpCookie));
				}
			}

			if (! fixedCookies.isEmpty()) {
				excessHeaders.remove(HTTPConstants.HEADER_SET_COOKIE);
				for (String fixedCookie : fixedCookies) {
					excessHeaders.put(HTTPConstants.HEADER_SET_COOKIE, fixedCookie);
				}
			}
		}

	}

	private String replaceCookieContextPath(String webContextWithVersion, HttpCookie httpCookie) {
		String oldPath = httpCookie.getPath();
		if (oldPath == null) {
			return webContextWithVersion;
		}

		int firstSlashIndex = oldPath.indexOf("/");
		boolean pathEndsWithSlash = oldPath.endsWith("/");
		if (firstSlashIndex >= 0) {
			int lastPosition = pathEndsWithSlash ? oldPath.length() - 1 : oldPath.length();
			oldPath = oldPath.substring(firstSlashIndex, lastPosition);
		}
		return webContextWithVersion + oldPath;
	}

	/*
	 *  Converts the HttpCookie into the header string
     */
	private String toHeaderString(HttpCookie cookie) {
		StringBuilder sb = new StringBuilder();

		sb.append(cookie.getName()).append("=").append(cookie.getValue());
		if (cookie.getPath() != null)
			sb.append("; Path=").append(cookie.getPath());
		if (cookie.getDomain() != null)
			sb.append("; Domain=").append(cookie.getDomain());

		if (cookie.getMaxAge() != MAX_AGE_UNSPECIFIED) {
			sb.append("; Max-Age=").append(cookie.getMaxAge());
		}
		if (cookie.getSecure())
			sb.append("; Secure");
		if (cookie.isHttpOnly())
			sb.append("; HttpOnly");

		return sb.toString();
	}
}
