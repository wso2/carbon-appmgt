package org.wso2.carbon.appmgt.impl.lifecycle;

import org.wso2.carbon.appmgt.api.APIManagementException;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.APIStatus;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.APIConstants;
import org.wso2.carbon.appmgt.impl.APIManagerFactory;

public class LifecycleHandler {

	public static void publishToGateway(String provider, String apiName, String version,
	                                    String status) {

		try {
			String currentUser = "admin";
			APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(currentUser);
			APIIdentifier apiId = new APIIdentifier(provider, apiName, version);
			WebApp api = apiProvider.getAPI(apiId);

			if (api != null) {
				// APIStatus oldStatus = api.getStatus();
				APIStatus newStatus = getApiStatus(APIConstants.PUBLISHED);
				APIStatus currentStatus = getApiStatus(APIConstants.CREATED);

				api.setStatus(currentStatus);

				if (newStatus.getStatus().equals(APIConstants.PUBLISHED)) {
					apiProvider.changeAPIStatus(api, newStatus, currentUser, true);
				}
			}

		} catch (APIManagementException e) {
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
