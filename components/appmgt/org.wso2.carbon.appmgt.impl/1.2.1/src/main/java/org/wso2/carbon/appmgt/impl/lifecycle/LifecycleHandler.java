/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl.lifecycle;

import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.APIStatus;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.APIManagerFactory;

public class LifecycleHandler {

	public static void publishToGateway(String provider, String apiName, String version,
	                                    String status, String previousState) {

		try {
			status = status.toUpperCase();
			if (AppMConstants.PUBLISHED.equals(status)) {

				APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(provider);
				APIIdentifier apiId = new APIIdentifier(provider, apiName, version);
				WebApp api = apiProvider.getAPI(apiId);
				if (api != null) {
					APIStatus oldState = getApiStatus(previousState);
					APIStatus newStatus = getApiStatus(status);
					api.setStatus(oldState);
					apiProvider.changeAPIStatus(api, newStatus, provider, true);
				}
			}

		} catch (AppManagementException e) {
			e.printStackTrace();
		}
	}

	private static APIStatus getApiStatus(String status) {
		APIStatus apiStatus = null;
		for (APIStatus aStatus : APIStatus.values()) {
			if (aStatus.getStatus().equalsIgnoreCase(status)) {
				apiStatus = aStatus;
			}

		}
		return apiStatus;
	}

}
