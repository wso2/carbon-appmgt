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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.impl.dto.Environment;
import org.wso2.carbon.appmgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.appmgt.impl.template.APITemplateException;
import org.wso2.carbon.rest.api.stub.RestApiAdminAPIException;
import org.wso2.carbon.rest.api.stub.RestApiAdminStub;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.rmi.RemoteException;

public class RESTAPIAdminClient extends AbstractAPIGatewayAdminClient {

    private static final Log log = LogFactory.getLog(RESTAPIAdminClient.class);

	private RestApiAdminStub restApiAdminStub;
	private String qualifiedName;
	private String qualifiedNonVersionedWebAppName;
	private Environment environment;

	public RESTAPIAdminClient(APIIdentifier appIdentifier, Environment environment) throws AxisFault {
		this.qualifiedName = appIdentifier.getProviderName() + "--" + appIdentifier.getApiName() + ":v" +
				appIdentifier.getVersion();
		this.qualifiedNonVersionedWebAppName = appIdentifier.getProviderName() + "--" + appIdentifier.getApiName();
		restApiAdminStub = new RestApiAdminStub(null, environment.getServerURL() + "RestApiAdmin");
		setup(restApiAdminStub, environment);
		this.environment = environment;
	}

	/**
	 * Adds versioned web app configuration to the gateway
	 *
	 * @param builder gateway configuration builder
	 * @param tenantDomain tenant domain of the web app
	 * @throws AxisFault if an error occurred when adding
	 */
	public void addVersionedWebApp(APITemplateBuilder builder, String tenantDomain) throws AxisFault {
		try {
			String appConfig = builder.getConfigStringForVersionedWebAppTemplate(environment);
			if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				restApiAdminStub.addApiForTenant(appConfig, tenantDomain);
			} else {
				restApiAdminStub.addApiFromString(appConfig);
			}
        } catch (APITemplateException e) {
            String msg = "Cannot build configuration string for the versioned web app";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (RemoteException e) {
            String msg = "An error occurred when calling the RestApiAdmin service to add the versioned web app";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (RestApiAdminAPIException e) {
            String msg = "An error occurred when adding the versioned web app via RestApiAdmin service";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
	}

	/**
	 * Returns versioned web app configuration from the gateway
	 *
	 * @param tenantDomain tenant domain of the web app
	 * @return web app gateway endpoint data
	 * @throws AxisFault if an error occurred when retrieving
	 */
	public APIData getVersionedWebApp(String tenantDomain) throws AxisFault {
		try {
			APIData appData;
			if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				appData = restApiAdminStub.getApiForTenant(qualifiedName, tenantDomain);
			} else {
				appData = restApiAdminStub.getApiByName(qualifiedName);
			}
			return appData;
        } catch (RemoteException e) {
            String msg = "An error occurred when calling the RestApiAdmin service to retrieve the versioned web app";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (RestApiAdminAPIException e) {
            String msg = "An error occurred when retrieving the versioned web app via RestApiAdmin service";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
	}

	/**
	 * Updates versioned web app configuration in the gateway
	 *
	 * @param builder gateway configuration builder
     * @param tenantDomain tenant domain of the web app
     * @throws AxisFault if an error occurred when updating
	 */
	public void updateVersionedWebApp(APITemplateBuilder builder, String tenantDomain) throws AxisFault {
		try {
			String appConfig = builder.getConfigStringForVersionedWebAppTemplate(environment);
			if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				restApiAdminStub.updateApiForTenant(qualifiedName, appConfig, tenantDomain);
			} else {
				restApiAdminStub.updateApiFromString(qualifiedName, appConfig);
			}
        } catch (APITemplateException e) {
            String msg = "Cannot build configuration string for the updating versioned web app";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (RemoteException e) {
            String msg = "An error occurred when calling the RestApiAdmin service to update the versioned web app";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (RestApiAdminAPIException e) {
            String msg = "An error occurred when updating the versioned web app via RestApiAdmin service";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
	}

	/**
	 * Deletes versioned web app configuration from the gateway
	 *
	 * @param tenantDomain tenant domain of the web app
	 * @throws AxisFault if an error occurred when deleting
	 */
	public void deleteVersionedWebApp(String tenantDomain) throws AxisFault {
		try {
			if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				restApiAdminStub.deleteApiForTenant(qualifiedName, tenantDomain);
			} else {
				restApiAdminStub.deleteApi(qualifiedName);
			}
        } catch (RemoteException e) {
            String msg = "An error occurred when calling the RestApiAdmin service to delete the versioned web app";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (RestApiAdminAPIException e) {
            String msg = "An error occurred when deleting the versioned web app via RestApiAdmin service";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
	}

	/**
	 * Adds non-versioned web app configuration to the gateway
	 *
	 * @param builder gateway configuration builder
     * @param tenantDomain tenant domain of the web app
     * @throws AxisFault if an error occurred when adding
	 */
	public void addNonVersionedWebApp(APITemplateBuilder builder, String tenantDomain) throws AxisFault {
		try {
			String appConfig = builder.getConfigStringForNonVersionedWebAppTemplate();
			if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				restApiAdminStub.addApiForTenant(appConfig, tenantDomain);
			} else {
				restApiAdminStub.addApiFromString(appConfig);
			}
        } catch (APITemplateException e) {
            String msg = "Cannot build configuration string for the non-versioned web app";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (RemoteException e) {
            String msg = "An error occurred when calling the RestApiAdmin service to add the non-versioned web app";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (RestApiAdminAPIException e) {
            String msg = "An error occurred when adding the non-versioned web app via RestApiAdmin service";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
	}

	/**
	 * Updates non-versioned web app configuration in the gateway
	 *
	 * @param builder gateway configuration builder
     * @param tenantDomain tenant domain of the web app
     * @throws AxisFault if an error occurred when updating
	 */
	public void updateNonVersionedWebApp(APITemplateBuilder builder, String tenantDomain) throws AxisFault {
		try {
			String appConfig = builder.getConfigStringForNonVersionedWebAppTemplate();
			if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				restApiAdminStub.updateApiForTenant(qualifiedNonVersionedWebAppName, appConfig,
													tenantDomain);
			} else {
				restApiAdminStub.updateApiFromString(qualifiedNonVersionedWebAppName, appConfig);
			}
        } catch (APITemplateException e) {
            String msg = "Cannot build configuration string for the updating non-versioned web app";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (RemoteException e) {
            String msg = "An error occurred when calling the RestApiAdmin service to update the non-versioned web app";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (RestApiAdminAPIException e) {
            String msg = "An error occurred when updating the non-versioned web app via RestApiAdmin service";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
	}

	/**
	 * Deletes non-versioned web app configuration form the gateway
	 *
	 * @param tenantDomain tenant domain of the web app
     * @throws AxisFault if an error occurred when deleting
	 */
	public void deleteNonVersionedWebApp(String tenantDomain) throws AxisFault {
		try {
			if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				restApiAdminStub.deleteApiForTenant(qualifiedNonVersionedWebAppName, tenantDomain);
			} else {
				restApiAdminStub.deleteApi(qualifiedNonVersionedWebAppName);
			}
        } catch (RemoteException e) {
            String msg = "An error occurred when calling the RestApiAdmin service to delete the non-versioned web app";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (RestApiAdminAPIException e) {
            String msg = "An error occurred when deleting the non-versioned web app via RestApiAdmin service";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
	}

	/**
	 * Returns the non-versioned web app configuration from the gateway
	 *
	 * @param tenantDomain tenant domain of the web app
     * @return web app gateway endpoint data
     * @throws AxisFault if an error occurred when retrieving
	 */
	public APIData getNonVersionedWebAppData(String tenantDomain) throws AxisFault {
		try {
			APIData appData;
			if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				appData = restApiAdminStub.getApiForTenant(qualifiedNonVersionedWebAppName, tenantDomain);
			} else {
				appData = restApiAdminStub.getApiByName(qualifiedNonVersionedWebAppName);
			}
			return appData;
        } catch (RemoteException e) {
            String msg = "An error occurred when calling the RestApiAdmin service to retrieve the non-versioned web app";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch (RestApiAdminAPIException e) {
            String msg = "An error occurred when retrieving the non-versioned web app via RestApiAdmin service";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
	}
}
