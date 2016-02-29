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

package org.wso2.carbon.appmgt.impl.idp;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceStub;
import org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException;
import org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

public class CarbonIdpManager implements WebAppIdPManager {

	private String serverUrl;
	private IdentityProviderMgtServiceStub idpStub;
	private IdentityApplicationManagementServiceStub appMgtStub;
	private boolean canAddIdPs;

	private static Log log = LogFactory.getLog(CarbonIdpManager.class);

	@Override
	public List<TrustedIdP> getIdPList(String serviceProvider) throws AppManagementException {
		List<TrustedIdP> idps = new ArrayList<TrustedIdP>();
		if (canAddIdP()) {
			// return complete list of IdPs since we can add the IdP if non
			// existing one is selected by the subscriber
			try {
				org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider[] idpDTOs = idpStub.getAllIdPs();
				if (idpDTOs != null && idpDTOs.length > 0) {
					for (org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider dto : idpDTOs) {
						TrustedIdP idp = new TrustedIdP();
						idp.setName(dto.getIdentityProviderName());
						idps.add(idp);
					}

				}
			} catch (RemoteException e) {
				log.error(e);
				throw new AppManagementException(e.getMessage());
			} catch (IdentityProviderMgtServiceIdentityProviderManagementExceptionException e) {
                log.error(e);
				throw new AppManagementException(e.getMessage());
            }
        }
        else {
			// only returns the set of IdPs which are already added to the SP as
			// trusted.
			try {
				AuthenticationStep[] steps = appMgtStub.getApplication(serviceProvider).getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();
				if (steps != null && steps.length > 0) {
					for (AuthenticationStep step : steps) {
						if (step.getFederatedIdentityProviders() != null && step.getFederatedIdentityProviders().length > 0) {
							for (IdentityProvider idp : step.getFederatedIdentityProviders()) {
								TrustedIdP tIdp = new TrustedIdP();
								tIdp.setName(idp.getIdentityProviderName());
								idps.add(tIdp);
							}
						}
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				throw new AppManagementException(e.getMessage());
			} catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
				e.printStackTrace();
				throw new AppManagementException(e.getMessage());
			}
		}
		return idps;
	}

	@Override
	public boolean canAddIdP() {
		return canAddIdPs;
	}

	@Override
	public void addIdPToSP(TrustedIdP idp, String sp) {
	}

	@Override
	public void init(AppManagerConfiguration config) throws AppManagementException {
		canAddIdPs =
		             config.getFirstProperty(AppMConstants.SSO_CONFIGURATION_ENABLE_IDP_MERGING)
		                   .equalsIgnoreCase("true");
		serverUrl = config.getFirstProperty(AppMConstants.SSO_CONFIGURATION_IDP_SERVICE_URL);

		try {
			idpStub =
			          new IdentityProviderMgtServiceStub(null, serverUrl +
			                                                   "IdentityProviderMgtService");
			appMgtStub =
			             new IdentityApplicationManagementServiceStub(null, serverUrl +
			                                                        "IdentityApplicationManagementService");
			String user =
			              config.getFirstProperty(AppMConstants.SSO_CONFIGURATION_IDP_SERVICE_USER_NAME);
			String pwd = config.getFirstProperty(AppMConstants.SSO_CONFIGURATION_IDP_SERVICE_PWD);

			CarbonUtils.setBasicAccessSecurityHeaders(user, pwd, idpStub._getServiceClient());
			CarbonUtils.setBasicAccessSecurityHeaders(user, pwd, appMgtStub._getServiceClient());
		} catch (AxisFault e) {
			log.error(e);
			throw new AppManagementException(e.getMessage());
		}
	}

}
