/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.appmgt.mdm.restconnector.utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.mdm.restconnector.AuthHandler;
import org.wso2.carbon.appmgt.mdm.restconnector.Constants;
import org.wso2.carbon.appmgt.mdm.restconnector.HTTPConnectionException;
import org.wso2.carbon.appmgt.mdm.restconnector.beans.RemoteServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RestUtils {

	private static final Log log = LogFactory.getLog(RestUtils.class);

	/**
	 * If not exists generate new access key or return existing one.
	 *
	 * @param remoteServer bean that holds information about remote server
	 * @param generateNewKey whether generate new access key or not
	 * @return generated access key
	 */
	public static String getAPIToken(RemoteServer remoteServer, boolean generateNewKey) {

		if (!generateNewKey) {
			if (!(AuthHandler.authKey == null || "null".equals(AuthHandler.authKey))) {
				return AuthHandler.authKey;
			}
		}

		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(remoteServer.getTokenApiURL());

		List<NameValuePair> nameValuePairs = new ArrayList<>();
		nameValuePairs.add(new NameValuePair(Constants.RestConstants.GRANT_TYPE,
		                                     Constants.RestConstants.PASSWORD));
		nameValuePairs.add(new NameValuePair(Constants.RestConstants.USERNAME,
		                                     remoteServer.getAuthUser()));
		nameValuePairs.add(new NameValuePair(Constants.RestConstants.PASSWORD,
		                                     remoteServer.getAuthPass()));
		postMethod.setQueryString(nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
		postMethod.addRequestHeader(Constants.RestConstants.AUTHORIZATION,
		                            Constants.RestConstants.BASIC + new String(
				                            Base64.encodeBase64((remoteServer.getClientKey() + Constants.RestConstants.COLON +
				                                                 remoteServer.getClientSecret())
						                                                .getBytes())));
		postMethod.addRequestHeader(Constants.RestConstants.CONTENT_TYPE,
		                            Constants.RestConstants.APPLICATION_FORM_URL_ENCODED);
		try {
			if (log.isDebugEnabled()) {
				log.debug("Sending POST request to API Token endpoint. Request path:  " +
				          remoteServer.getTokenApiURL());
			}

			int statusCode = httpClient.executeMethod(postMethod);

			if (log.isDebugEnabled()) {
				log.debug("Status code " + statusCode +
				          " received while accessing the API Token endpoint.");
			}

		} catch (IOException e) {
			String errorMessage = "Cannot connect to Token API Endpoint.";
			log.error(errorMessage, e);
			return null;
		}

		String response;
		try {
			response = postMethod.getResponseBodyAsString();
		} catch (IOException e) {
			String errorMessage = "Cannot get response body for auth.";
			log.error(errorMessage, e);
			return null;
		}

		JSONObject token = (JSONObject) new JSONValue().parse(response);

		AuthHandler.authKey = String.valueOf(token.get(Constants.RestConstants.ACCESS_TOKEN));
		return AuthHandler.authKey;
	}

	/**
	 * Execute HTTP method and return whether operation success or not.
	 *
	 * @param remoteServer Bean that holds information about remote server
	 * @param httpClient HTTP client object
	 * @param httpMethod HTTP method which should be executed
	 * @return true if HTTP method successfully executed false if not
	 */
	public static boolean executeMethod(RemoteServer remoteServer, HttpClient httpClient,
	                              HttpMethodBase httpMethod) {
		String authKey = getAPIToken(remoteServer, false);
		if (log.isDebugEnabled()) {
			log.debug("Access token received : " + authKey);
		}

		try {
			int statusCode = 401;
			int tries = 0;
			while (statusCode != 200) {

				if (log.isDebugEnabled()) {
					log.debug("Trying to call API : trying for " + (tries + 1) + " time(s).");
				}

				httpMethod.setRequestHeader(Constants.RestConstants.AUTHORIZATION,
				                            Constants.RestConstants.BEARER + authKey);
				if (log.isDebugEnabled()) {
					log.debug("Sending " + httpMethod.getName() + " request to " +
					          httpMethod.getURI());
				}

				statusCode = httpClient.executeMethod(httpMethod);
				if (log.isDebugEnabled()) {
					log.debug("Status code received : " + statusCode);
				}

				if (++tries >= 3) {
					log.info(
							"API Call failed for the 3rd time: No or Unauthorized Access Aborting.");
					return false;
				}
				if (statusCode == 401) {
					authKey = getAPIToken(remoteServer, true);
					if (log.isDebugEnabled()) {
						log.debug(
								"Access token getting again, Access token received :  " + authKey +
								" in  try " + tries +".");
					}
				}
			}
			return true;
		} catch (IOException e) {
			String errorMessage = "No OK response received form the API.";
			log.error(errorMessage, e);
			return false;
		}
	}


	/**
	 * Execute HTTP method and return whether operation success or not.
	 *
	 * @param remoteServer Bean that holds information about remote server
	 * @param httpClient HTTP client object
	 * @param httpMethod HTTP method which should be executed
	 * @return true if HTTP method successfully executed false if not
	 */
	public static String execute(RemoteServer remoteServer, HttpClient httpClient,
										HttpMethodBase httpMethod) throws HTTPConnectionException {
		String authKey = getAPIToken(remoteServer, false);
		if (log.isDebugEnabled()) {
			log.debug("Access token received : " + authKey);
		}

		try {
			int statusCode = 401;
			int tries = 0;
			while (statusCode != 200) {

				if (log.isDebugEnabled()) {
					log.debug("Trying to call API : trying for " + (tries + 1) + " time(s).");
				}

				httpMethod.setRequestHeader(Constants.RestConstants.AUTHORIZATION,
						Constants.RestConstants.BEARER + authKey);
				if (log.isDebugEnabled()) {
					log.debug("Sending " + httpMethod.getName() + " request to " +
							httpMethod.getURI());
				}

				statusCode = httpClient.executeMethod(httpMethod);
				if (log.isDebugEnabled()) {
					log.debug("Status code received : " + statusCode);
				}

				if (++tries >= 3) {
					log.info("API Call failed for the 3rd time: No or Unauthorized Access Aborting.");
					throw new HTTPConnectionException("API Call failed for the 3rd time: No or Unauthorized Access Aborting.");
				}
				if (statusCode == 401) {
					authKey = getAPIToken(remoteServer, true);
					if (log.isDebugEnabled()) {
						log.debug("Access token getting again, Access token received :  " + authKey +
										" in try " + tries +".");
					}
				}
			}
			return httpMethod.getResponseBodyAsString();
		} catch (IOException e) {
			String errorMessage = "No OK response received form the API.";
			log.error(errorMessage, e);
			throw new HTTPConnectionException(errorMessage, e);
		}
	}

}
