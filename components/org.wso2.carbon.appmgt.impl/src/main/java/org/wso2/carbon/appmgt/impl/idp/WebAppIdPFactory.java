/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl.idp;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;

public class WebAppIdPFactory {
	private static AppManagerConfiguration config;
	private static WebAppIdPFactory instance;
	private static WebAppIdPManager idpManager;

	private static Log log = LogFactory.getLog(WebAppIdPFactory.class);

	private WebAppIdPFactory() {
		config =
		         ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
		                               .getAPIManagerConfiguration();
	}

	public static WebAppIdPFactory getInstance() {
		if (instance == null) {
			instance = new WebAppIdPFactory();
		}
		return instance;
	}

	public WebAppIdPManager getIdpManager() throws AppManagementException {
		if (idpManager == null) {
			String className =
			                   config.getFirstProperty(AppMConstants.SSO_CONFIGURATION_IDENTITY_PROVIDER_MANAGER);
			try {
				idpManager =
				             (WebAppIdPManager) Class.forName(className)
				                                     .getConstructor(AppManagerConfiguration.class)
				                                     .newInstance(config);
			} catch (InstantiationException e) {
				log.error(e);
				throw new AppManagementException(e.getMessage());
			} catch (IllegalAccessException e) {
				log.error(e);
				throw new AppManagementException(e.getMessage());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				log.error(e);
				throw new AppManagementException(e.getMessage());
			} catch (NoSuchMethodException e) {
				log.error(e);
				throw new AppManagementException(e.getMessage());
			} catch (SecurityException e) {
				log.error(e);
				throw new AppManagementException(e.getMessage());
			} catch (ClassNotFoundException e) {
				log.error(e);
				throw new AppManagementException(e.getMessage());
			}
		}
		return idpManager;
	}
}
