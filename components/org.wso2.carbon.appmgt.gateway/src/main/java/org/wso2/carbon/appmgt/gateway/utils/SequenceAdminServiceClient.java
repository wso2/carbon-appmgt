/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.appmgt.gateway.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.utils.AbstractAPIGatewayAdminClient;
import org.wso2.carbon.sequences.stub.types.SequenceAdminServiceStub;
import org.wso2.carbon.sequences.stub.types.SequenceEditorException;

import java.rmi.RemoteException;

/**
 * SequenceAdmin service client to deploy the custom sequences to multiple gateway environments.
 * 
 */

public class SequenceAdminServiceClient extends AbstractAPIGatewayAdminClient {
	static final String backendURLl = "local:///services/";
	private SequenceAdminServiceStub sequenceAdminStub;

	public SequenceAdminServiceClient() throws AppManagementException {
		try {
			sequenceAdminStub = new SequenceAdminServiceStub(null, backendURLl +"SequenceAdminService");
		} catch (AxisFault e) {
			throw new AppManagementException("Error while calling to sequence admin service client.", e);
		}
	}

	/**
	 * Deploy the sequence to the gateway for tenant users.
	 * @param sequence  - the sequence element , which to be deployed in synapse.
	 * @param tenantDomain String.
	 * @throws AppManagementException on errors.
     */
	public void addSequenceForTenant(OMElement sequence, String tenantDomain) throws AppManagementException {
		String errorMsg = "Error while adding the sequence for super tenant user.";
		try {
			sequenceAdminStub.addSequenceForTenant(sequence, tenantDomain);
		} catch (SequenceEditorException e) {
			throw new AppManagementException(errorMsg, e);
		} catch (RemoteException e) {
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Deploy the sequence to the gateway for super tenant users.
	 * @param sequence  - The sequence element , which to be deployed in synapse.
	 * @throws AppManagementException on errors.
	 */
	public void addSequence(OMElement sequence) throws AppManagementException {
		String errorMsg = "Error while adding the sequence for tenant user.";
		try {
			sequenceAdminStub.addSequence(sequence);
		} catch (SequenceEditorException e) {
			throw new AppManagementException(errorMsg, e);
		} catch (RemoteException e) {
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Undeploy the sequence from gateway for tenant users.
	 * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration.
	 * @param tenantDomain String.
	 * @throws AppManagementException on errors.
	 */
	public void deleteSequenceForTenant(String sequenceName, String tenantDomain) throws AppManagementException {
		String errorMsg = "Error while deleting the sequence for super tenant user.";
		try {
			sequenceAdminStub.deleteSequenceForTenant(sequenceName, tenantDomain);
		} catch (SequenceEditorException e) {
			throw new AppManagementException(errorMsg, e);
		} catch (RemoteException e) {
			throw new AppManagementException(errorMsg, e);
		}
	}


	/**
	 * Undeploy the sequence from gateway for super tenant users.
	 * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration.
	 * @throws AppManagementException on errors.
	 */
	public void deleteSequence(String sequenceName) throws AppManagementException {
		String errorMsg = "Error while deleting the sequence for tenant user.";
		try {
			sequenceAdminStub.deleteSequence(sequenceName);
		} catch (SequenceEditorException e) {
			throw new AppManagementException(errorMsg, e);
		} catch (RemoteException e) {
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Get the sequence from gateway for tenant users.
	 * @param sequenceName String.
	 * @param tenantDomain String.
	 * @throws AppManagementException on errors.
	 */
	public OMElement getSequenceForTenant(String sequenceName, String tenantDomain) throws AppManagementException {
		String errorMsg = "Error while retrieving the sequence for tenant user.";
		try {
			return (OMElement) sequenceAdminStub.getSequenceForTenant(sequenceName, tenantDomain);
		} catch (SequenceEditorException e) {
			throw new AppManagementException(errorMsg, e);
		} catch (RemoteException e) {
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Get the sequence from gateway for super tenant users.
	 * @param sequenceName String.
	 * @throws AppManagementException on errors.
	 */
	public OMElement getSequence(String sequenceName) throws AppManagementException {
		String errorMsg = "Error while retrieving the sequence for super tenant user.";
		try {
			return  (OMElement) sequenceAdminStub.getSequence(sequenceName);
		} catch (SequenceEditorException e) {
			throw new AppManagementException(errorMsg, e);
		} catch (RemoteException e) {
			throw new AppManagementException(errorMsg, e);
		}
	}

	/**
	 * Check whether there is already a sequence in gateway for tenant users.
	 * @param sequenceName String.
	 * @param tenantDomain String.
	 * @return true or false.
	 * @throws AppManagementException on errors.
     */
	public boolean isExistingSequenceForTenant(String sequenceName, String tenantDomain) throws AppManagementException {
		try{
			return sequenceAdminStub.isExistingSequenceForTenant(sequenceName, tenantDomain);
		}catch (RemoteException e){
			throw new AppManagementException("Error while checking for existence of sequence : " + sequenceName + "in" +
					                                 " tenant " + tenantDomain, e);
		}
	}

	/**
	 * Check whether there is already a sequence in gateway for super tenant users.
	 * @param sequenceName String.
	 * @return true or false.
	 * @throws AppManagementException on errors.
	 */
	public boolean isExistingSequence(String sequenceName) throws AppManagementException{
        try{
	        return sequenceAdminStub.isExistingSequence(sequenceName);
        }catch (RemoteException e){
            throw new AppManagementException("Error while checking for existence of sequence : " + sequenceName , e);
        }
    }
}