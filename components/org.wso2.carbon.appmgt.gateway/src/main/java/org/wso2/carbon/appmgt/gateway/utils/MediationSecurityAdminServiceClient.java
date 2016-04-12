/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appmgt.gateway.utils;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.utils.AbstractAPIGatewayAdminClient;
import org.wso2.carbon.mediation.security.stub.MediationSecurityAdminServiceStub;

import java.rmi.RemoteException;

/**
 * Mediation Security Admin Service Client to encode the passwords and store
 * into the registry .
 */

public class MediationSecurityAdminServiceClient extends AbstractAPIGatewayAdminClient {

	static final String backendUrl = "local:///services/";
	private MediationSecurityAdminServiceStub mediationSecurityAdminServiceStub;

	public MediationSecurityAdminServiceClient() throws AppManagementException {
		try {
			mediationSecurityAdminServiceStub = new MediationSecurityAdminServiceStub(null, backendUrl + "MediationSecurityAdminService");

		} catch (AxisFault ex) {
			throw new AppManagementException("Error while adding new sequence", ex);
		}
	}

	/**
	 * Encrypt the plain text password.
	 * @param plainTextPass String.
	 * @return encrypted password in String.
	 * @throws AppManagementException on errors.
     */
	public String doEncryption(String plainTextPass) throws AppManagementException {
		String encodedValue = null;
		try {
			encodedValue =	 mediationSecurityAdminServiceStub.doEncrypt(plainTextPass);
		} catch(RemoteException e) {
			String msg = "Failed to encrypt the secured endpoint password, " + e.getMessage();
			throw new AppManagementException(msg, e);
		}
		return encodedValue;
	}
}
