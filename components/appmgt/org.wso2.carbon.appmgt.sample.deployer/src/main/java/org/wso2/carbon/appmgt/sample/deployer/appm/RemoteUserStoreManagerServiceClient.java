/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.sample.deployer.appm;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;

import java.rmi.RemoteException;

/**
 * This class is use as a client for Remote UserStore Manager Service
 */
public class RemoteUserStoreManagerServiceClient {
    private RemoteUserStoreManagerServiceStub userStoreManagerStub;

    /**
     * Creates a new RemoteUserStoreManagerServiceClient object and initialising the RemoteUserStoreManagerServiceStub
     *
     * @param cookie cookie to get authentication for RemoteUserStoreManagerService
     * @param url    https server url
     * @throws AxisFault Throws this when RemoteUserStoreManagerServiceStub failed initialise
     */
    public RemoteUserStoreManagerServiceClient(String cookie, String url) throws AxisFault {
        String serviceURL = url + "/services/RemoteUserStoreManagerService";
        userStoreManagerStub = new RemoteUserStoreManagerServiceStub(serviceURL);
        ServiceClient svcClient = userStoreManagerStub._getServiceClient();
        Options option;
        option = svcClient.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * This method is use to set a value for given claim
     *
     * @param userName  currently logged user
     * @param claimURI  URI of the claim
     * @param climValue value of the claim
     * @throws RemoteException Throws this when failed to update a claim value
     */
    public void updateClaims(String userName, String claimURI, String climValue) throws RemoteException,
            RemoteUserStoreManagerServiceUserStoreExceptionException {
        userStoreManagerStub.setUserClaimValue(userName, claimURI, climValue, "default");
    }
}
