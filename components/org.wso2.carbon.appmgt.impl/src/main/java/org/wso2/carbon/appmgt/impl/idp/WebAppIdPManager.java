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

import java.util.List;

import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;

public interface WebAppIdPManager {

	public void init(AppManagerConfiguration config) throws AppManagementException;
	

	/**
	 * If dynamically adding IdPs is supported then return all the external IdPs
	 * registered in the IdP. If not only returns the set of IdPs already
	 * trusted by the SP
	 * 
	 * @param serviceProvider
	 * @return
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 */
	public List<TrustedIdP> getIdPList(String serviceProvider) throws AppManagementException;

	/**
	 * Check whether the IdP supports dynamically adding external IdPs to the
	 * SP.
	 * 
	 * @return
	 */
	public boolean canAddIdP();

	/**
	 * 
	 * @param idp
	 * @param sp
	 */
	public void addIdPToSP(TrustedIdP idp, String sp);

}
