/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appmgt.impl.utils;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.impl.dto.Environment;
import org.wso2.carbon.appmgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.rest.api.stub.RestApiAdminStub;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class RESTAPIAdminClient extends AbstractAPIGatewayAdminClient {

	private RestApiAdminStub restApiAdminStub;
	private String qualifiedName;
	private String qualifiedNonVersionedWebAppName;
	private Environment environment;

	public RESTAPIAdminClient(APIIdentifier apiId, Environment environment) throws AxisFault {
		this.qualifiedName = apiId.getProviderName() + "--" + apiId.getApiName() + ":v" +
				apiId.getVersion();
		this.qualifiedNonVersionedWebAppName = apiId.getProviderName() + "--" + apiId.getApiName();
		restApiAdminStub = new RestApiAdminStub(null, environment.getServerURL() + "RestApiAdmin");
		setup(restApiAdminStub, environment);
		this.environment = environment;
	}

	/**
	 * Adds versioned web app configuration to the gateway
	 *
	 * @param builder
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void addVersionedWebApp(APITemplateBuilder builder, String tenantDomain)
			throws AxisFault {
		try {
			String apiConfig = builder.getConfigStringForVersionedWebAppTemplate(environment);
			if (tenantDomain != null && !("").equals(tenantDomain)
					&& !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
				restApiAdminStub.addApiForTenant(apiConfig, tenantDomain);
			} else {
				restApiAdminStub.addApiFromString(apiConfig);
			}
		} catch (Exception e) {
			throw new AxisFault("Error while adding new WebApp", e);
		}
	}

	/**
	 * Returns versioned web app configuration from the gateway
	 *
	 * @param tenantDomain
	 * @return
	 * @throws AxisFault
	 */
	public APIData getVersionedWebApp(String tenantDomain) throws AxisFault {
		try {
			APIData apiData;
			if (tenantDomain != null && !("").equals(tenantDomain)
					&& !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
				apiData = restApiAdminStub.getApiForTenant(qualifiedName, tenantDomain);
			} else {
				apiData = restApiAdminStub.getApiByName(qualifiedName);
			}
			return apiData;
		} catch (Exception e) {
			throw new AxisFault("Error while obtaining WebApp information from gateway", e);
		}
	}

	/**
	 * Updates versioned web app configuration in the gateway
	 *
	 * @param builder
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void updateVersionedWebApp(APITemplateBuilder builder, String tenantDomain)
			throws AxisFault {
		try {
			String apiConfig = builder.getConfigStringForVersionedWebAppTemplate(environment);
			if (tenantDomain != null && !("").equals(tenantDomain) &&
					!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {

				restApiAdminStub.updateApiForTenant(qualifiedName, apiConfig, tenantDomain);
			} else {
				restApiAdminStub.updateApiFromString(qualifiedName, apiConfig);
			}
		} catch (Exception e) {
			throw new AxisFault("Error while updating WebApp", e);
		}
	}

	/**
	 * Deletes versioned web app configuration from the gateway
	 *
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void deleteVersionedWebApp(String tenantDomain) throws AxisFault {
		try {
			if (tenantDomain != null && !("").equals(tenantDomain) &&
					!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
				restApiAdminStub.deleteApiForTenant(qualifiedName, tenantDomain);
			} else {
				restApiAdminStub.deleteApi(qualifiedName);
			}

		} catch (Exception e) {
			throw new AxisFault("Error while deleting WebApp", e);
		}
	}

	/**
	 * Adds non-versioned web app configuration to the gateway
	 *
	 * @param builder
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void addNonVersionedWebApp(APITemplateBuilder builder, String tenantDomain)
			throws AxisFault {

		try {
			String apiConfig = builder.getConfigStringForNonVersionedWebAppTemplate();
			if (tenantDomain != null && !("").equals(tenantDomain)
					&& !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
				restApiAdminStub.addApiForTenant(apiConfig, tenantDomain);
			} else {
				restApiAdminStub.addApiFromString(apiConfig);
			}
		} catch (Exception e) {
			throw new AxisFault("Error publishing non-versioned web app to the gateway", e);
		}
	}

	/**
	 * Updates non-versioned web app configuration in the gateway
	 *
	 * @param builder
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void updateNonVersionedWebApp(APITemplateBuilder builder, String tenantDomain)
			throws AxisFault {
		try {
			String apiConfig = builder.getConfigStringForNonVersionedWebAppTemplate();
			if (tenantDomain != null && !("").equals(tenantDomain) &&
					!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {

				restApiAdminStub.updateApiForTenant(qualifiedNonVersionedWebAppName, apiConfig,
													tenantDomain);
			} else {
				restApiAdminStub.updateApiFromString(qualifiedNonVersionedWebAppName, apiConfig);
			}
		} catch (Exception e) {
			throw new AxisFault("Error while updating non-versioned web app in the gateway", e);
		}
	}

	/**
	 * Deletes non-versioned web app configuration form the gateway
	 *
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void deleteNonVersionedWebApp(String tenantDomain) throws AxisFault {
		try {
			if (tenantDomain != null && !("").equals(tenantDomain) &&
					!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
				restApiAdminStub.deleteApiForTenant(qualifiedNonVersionedWebAppName, tenantDomain);
			} else {
				restApiAdminStub.deleteApi(qualifiedNonVersionedWebAppName);
			}
		} catch (Exception e) {
			throw new AxisFault("Error while deleting non-versioned web app from the gateway", e);
		}
	}

	/**
	 * Returns the non-versioned web app configuration from the gateway
	 *
	 * @param tenantDomain
	 * @return
	 * @throws AxisFault
	 */
	public APIData getNonVersionedWebAppData(String tenantDomain) throws AxisFault {
		try {
			APIData apiData;
			if (tenantDomain != null && !("").equals(tenantDomain)
					&& !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
				apiData = restApiAdminStub.getApiForTenant(qualifiedNonVersionedWebAppName,
														   tenantDomain);
			} else {
				apiData = restApiAdminStub.getApiByName(qualifiedNonVersionedWebAppName);
			}
			return apiData;
		} catch (Exception e) {
			throw new AxisFault(
					"Error while obtaining non-versioned web app information from gateway", e);
		}
	}
}
