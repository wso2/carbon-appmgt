/**
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.appmgt.gateway.utils;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.appmgt.impl.utils.AbstractAPIGatewayAdminClient;
import org.wso2.carbon.rest.api.stub.RestApiAdminAPIException;
import org.wso2.carbon.rest.api.stub.RestApiAdminStub;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;
import org.wso2.carbon.appmgt.api.AppManagementException;

import java.rmi.RemoteException;

public class RESTAPIAdminClient extends AbstractAPIGatewayAdminClient {

	private RestApiAdminStub restApiAdminStub;
	private String qualifiedName;
	private String qualifiedNonVersionedWebAppName;

	private static final String backendURLl = "local:///services/";

	public RESTAPIAdminClient(String apiProviderName, String apiName, String version) throws AppManagementException {
		try {
			this.qualifiedName = apiProviderName + "--" + apiName + ":v" + version;
			this.qualifiedNonVersionedWebAppName = apiProviderName + "--" + apiName;
			restApiAdminStub = new RestApiAdminStub(null, backendURLl + "RestApiAdmin");
		} catch (AxisFault ex) {
			throw new AppManagementException("Error occurred while gateway operations.", ex);
		}
	}

	/**
	 * Add versioned webapp configuration to the gateway for tenant users.
	 * @param appConfig String.
	 * @param tenantDomain String.
	 * @throws AppManagementException on errors.
     */
	public void addVersionedWebAppForTenant(String appConfig, String tenantDomain) throws AppManagementException {
		try {
			restApiAdminStub.addApiForTenant(appConfig, tenantDomain);
		} catch (RemoteException e) {
			String errorMsg = "Error while adding new WebApp for tenant : " + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		} catch (RestApiAdminAPIException e) {
			String errorMsg = "Error while adding new WebApp for tenant : " + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Add versioned webapp configuration to the gateway for super tenant users.
	 * @param appConfig String.
	 * @throws AppManagementException on errors.
     */
	public void addVersionedWebApp(String appConfig) throws AppManagementException {
		try {
			restApiAdminStub.addApiFromString(appConfig);
		} catch (RemoteException e) {
			String errorMsg = "Error while adding new WebApp for super tenant user.";
			throw new AppManagementException(errorMsg, e);
		} catch (RestApiAdminAPIException e) {
			String errorMsg = "Error while adding new WebApp for super tenant user.";
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Return versioned webapp configuration from the gateway for tenant users.
	 * @param tenantDomain String.
	 * @return versioned webapp configuration data.
	 * @throws AppManagementException on errors.
     */
	public APIData getVersionedWebAppForTenant(String tenantDomain) throws AppManagementException {
		try {
			APIData apiData = restApiAdminStub.getApiForTenant(qualifiedName, tenantDomain);
			return apiData;
		} catch (RemoteException e) {
			String errorMsg = "Error while obtaining versioned webApp information from gateway for tenant : " + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		} catch (RestApiAdminAPIException e) {
			String errorMsg = "Error while obtaining versioned webApp information from gateway for tenant :." + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Return versioned webapp configuration from the gateway for super tenant users.
	 * @return versioned webapp configuration data.
	 * @throws AppManagementException
     */
	public APIData getVersionedWebApp() throws AppManagementException {
		try {
			APIData apiData = restApiAdminStub.getApiByName(qualifiedName);
			return apiData;
		} catch (RemoteException e) {
			throw new AppManagementException("Error while obtaining versioned webApp information from gateway for super "
			                                 + "tenant user.", e);
		}
	}

	/**
	 * Update versioned webapp configuration in the gateway for tenant users.
	 * @param apiConfig String.
	 * @param tenantDomain String.
	 * @throws AppManagementException on errors.
     */
	public void updateVersionedWebAppForTenant(String apiConfig, String tenantDomain) throws AppManagementException {
		try {
			restApiAdminStub.updateApiForTenant(qualifiedName, apiConfig, tenantDomain);
		} catch (RemoteException e) {
			String errorMsg = "Error while updating WebApp for tenant : " + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		} catch (RestApiAdminAPIException e) {
			String errorMsg = "Error while updating WebApp for tenant : " + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		}
	}
	/**
	 * Update versioned webapp configuration in the gateway for super tenant users.
	 * @param apiConfig String.
	 * @throws AppManagementException on errors.
	 */
	public void updateVersionedWebApp(String apiConfig) throws AppManagementException {
		try {
			restApiAdminStub.updateApiFromString(qualifiedName, apiConfig);
		} catch (RemoteException e) {
			String errorMsg = "Error while updating WebApp for super tenant user.";
			throw new AppManagementException(errorMsg, e);
		} catch (RestApiAdminAPIException e) {
			String errorMsg = "Error while updating WebApp for super tenant user.";
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Delete versioned webapp configuration from the gateway for tenant users.
	 * @param tenantDomain String.
	 * @throws AppManagementException on errors.
     */
	public void deleteVersionedWebAppForTenant(String tenantDomain) throws AppManagementException {
		try {
			restApiAdminStub.deleteApiForTenant(qualifiedName, tenantDomain);
		} catch (RemoteException e) {
			String errorMsg = "Error while deleting WebApp for tenant :." + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		} catch (RestApiAdminAPIException e) {
			String errorMsg = "Error while deleting WebApp for tenant :." + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Delete versioned webapp configuration from the gateway for super tenant users.
	 * @throws AppManagementException on errors.
	 */
	public void deleteVersionedWebApp() throws AppManagementException {
		try {
			restApiAdminStub.deleteApi(qualifiedName);
		} catch (RemoteException e) {
			String errorMsg = "Error while deleting WebApp for super tenant users.";
			throw new AppManagementException(errorMsg, e);
		} catch (RestApiAdminAPIException e) {
			String errorMsg = "Error while deleting WebApp for super tenant users.";
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Add non versioned webapp configuration to the gateway for tenant users.
	 * @param apiConfig String.
	 * @param tenantDomain String.
	 * @throws AppManagementException on errors.
     */
	public void addNonVersionedWebAppForTenant(String apiConfig, String tenantDomain) throws AppManagementException {
		try {
			restApiAdminStub.addApiForTenant(apiConfig, tenantDomain);
		} catch (RemoteException e) {
			String errorMsg = "Error publishing non-versioned web app to the gateway for tenant : " + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		} catch (RestApiAdminAPIException e) {
			String errorMsg = "Error publishing non-versioned web app to the gateway for tenant : " + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Add non versioned webapp configuration to the gateway for tenant users.
	 * @param apiConfig String.
	 * @throws AppManagementException on errors.
	 */
	public void addNonVersionedWebApp(String apiConfig) throws AppManagementException {
		try {
			restApiAdminStub.addApiFromString(apiConfig);
		} catch (RemoteException e) {
			String errorMsg = "Error publishing non-versioned web app to the gateway for super tenant users.";
			throw new AppManagementException(errorMsg, e);
		} catch (RestApiAdminAPIException e) {
			String errorMsg = "Error publishing non-versioned web app to the gateway for super tenant users.";
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Update non versioned webapp configuration in the gateway for tenant users.
	 * @param apiConfig String.
	 * @param tenantDomain String.
	 * @throws AppManagementException on errors.
     */
	public void updateNonVersionedWebAppForTenant(String apiConfig, String tenantDomain) throws AppManagementException {
		try {
			restApiAdminStub.updateApiForTenant(qualifiedNonVersionedWebAppName, apiConfig, tenantDomain);
		} catch (RemoteException e) {
			String errorMsg = "Error while updating non-versioned web app in the gateway for tenant : " + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		} catch (RestApiAdminAPIException e) {
			String errorMsg = "Error while updating non-versioned web app in the gateway for tenant : " + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Update non versioned webapp configuration in the gateway for super tenant users.
	 * @param apiConfig String.
	 * @throws AppManagementException on errors.
	 */
	public void updateNonVersionedWebApp(String apiConfig) throws AppManagementException {
		try {
			restApiAdminStub.updateApiFromString(qualifiedNonVersionedWebAppName, apiConfig);
		} catch (RemoteException e) {
			String errorMsg = "Error while updating non-versioned web app in the gateway for super tenant.";
			throw new AppManagementException(errorMsg, e);
		} catch (RestApiAdminAPIException e) {
			String errorMsg = "Error while updating non-versioned web app in the gateway for super tenant.";
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Delete non versioned webapp configuration from the gateway for tenant users.
	 * @param tenantDomain String.
	 * @throws AppManagementException on errors.
     */
	public void deleteNonVersionedWebAppForTenant(String tenantDomain) throws AppManagementException {
		try {
			restApiAdminStub.deleteApiForTenant(qualifiedNonVersionedWebAppName, tenantDomain);
		} catch (RemoteException e) {
			String errorMsg = "Error while deleting non-versioned web app in the gateway for tenant : " + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		} catch (RestApiAdminAPIException e) {
			String errorMsg = "Error while deleting non-versioned web app in the gateway for tenant : " + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Delete non versioned webapp configuration from the gateway for super tenant users.
	 * @throws AppManagementException on errors.
	 */
	public void deleteNonVersionedWebApp() throws AppManagementException {
		try {
			restApiAdminStub.deleteApi(qualifiedNonVersionedWebAppName);
		} catch (RemoteException e) {
			String errorMsg = "Error while deleting non-versioned web app in the gateway for super tenant users.";
			throw new AppManagementException(errorMsg, e);
		} catch (RestApiAdminAPIException e) {
			String errorMsg = "Error while deleting non-versioned web app in the gateway for super tenant users.";
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Return non versioned webapp configuration from the gateway for tenant users.
	 * @param tenantDomain String.
	 * @return non versioned webapp configuration data.
	 * @throws AppManagementException on errors.
     */
	public APIData getNonVersionedWebAppDataForTenant(String tenantDomain) throws AppManagementException {
		try {
			APIData apiData = restApiAdminStub.getApiForTenant(qualifiedNonVersionedWebAppName, tenantDomain);
			return apiData;
		} catch (RemoteException e) {
			String errorMsg = "Error while obtaining non-versioned web app in the gateway for tenant : " + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		} catch (RestApiAdminAPIException e) {
			String errorMsg = "Error while obtaining non-versioned web app in the gateway for tenant : " + tenantDomain;
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Return non versioned webapp configuration from the gateway for super tenant users.
	 * @return non versioned webapp configuration data.
	 * @throws AppManagementException on errors.
	 */
	public APIData getNonVersionedWebAppData() throws AppManagementException {
		try {
			APIData apiData = restApiAdminStub.getApiByName(qualifiedNonVersionedWebAppName);
			return apiData;
		} catch (RemoteException e) {
			throw new AppManagementException("Error while obtaining non-versioned web app information from gateway " +
					                                 "for super tenant users.", e);
		}
	}
}
