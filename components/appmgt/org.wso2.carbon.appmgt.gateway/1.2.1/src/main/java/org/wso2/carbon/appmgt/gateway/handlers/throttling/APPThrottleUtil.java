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

package org.wso2.carbon.appmgt.gateway.handlers.throttling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.appmgt.gateway.handlers.Utils;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.appmgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.appmgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.appmgt.impl.AppMConstants;

public class APPThrottleUtil {
	private static final Log log = LogFactory.getLog(APPThrottleUtil.class);

	private static final String SEMICOLON = ";";
	private static final String EQUAL = "=";

	private APPThrottleUtil() {
	}

	public static AuthenticationContext getAuthenticationContext(MessageContext synCtx,
	                                                             APPResourceManager resourceManager)
	                                                                                                throws APISecurityException {
		String authCookie = Utils.getAuthenticationCookie(synCtx);
		AuthenticationContext authContext = null;
		boolean initWebAppData = false;
		if (authCookie != null) {
			authContext =
			              (AuthenticationContext) resourceManager.getResourceCache()
			                                                     .get(authCookie);
			if (authContext == null) {
				initWebAppData = true;
			}
		} else {
			// Executes this section if the user is anonymous

			// ClientIP used as a unique key when the user is anonymous
			String clientIP =
					(String) ((Axis2MessageContext) synCtx).getAxis2MessageContext()
							.getProperty(AppMConstants.REMOTE_ADDR);
			authContext = getAnonymousContext(clientIP, authCookie);
			if (log.isDebugEnabled()) {
				log.debug("Anonymous Authentication Context created. Client IP -" + clientIP);
			}
		}

		if (initWebAppData) {
			if (synCtx.getProperty(APISecurityConstants.SUBJECT) != null) {
				String user = String.valueOf(synCtx.getProperty(APISecurityConstants.SUBJECT));
				String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
				String apiVersion =
				                    (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

				authContext =
				              resourceManager.getAuthenticationContext(apiContext, apiVersion,
				                                                       user, authCookie);
			} else {
				log.warn("loggedin user is not set, cannot access the authentication context");
			}
		}
		return authContext;
	}

	/**
	 * Create Anonymous Authenticated tier
	 * @param clientIP - client IP to uniquely identify user
	 * @param accessToken
	 * @return Authentication Context - Anonymous user
	 */
	private static AuthenticationContext getAnonymousContext(String clientIP, String accessToken) {
		AuthenticationContext authContext = new AuthenticationContext();
		authContext.setAccessToken(accessToken);
		authContext.setApplicationId(clientIP);
		authContext.setApplicationName(null);
		authContext.setApplicationTier(AppMConstants.UNAUTHENTICATED_TIER);
		authContext.setTier(AppMConstants.UNAUTHENTICATED_TIER);
		authContext.setConsumerKey(null);
		authContext.setAuthenticated(true);
		return authContext;
	}
}
