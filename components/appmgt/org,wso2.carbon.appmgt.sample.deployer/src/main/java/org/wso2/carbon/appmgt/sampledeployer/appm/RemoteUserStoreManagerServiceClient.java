package org.wso2.carbon.appmgt.sampledeployer.appm;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.appmgt.sampledeployer.configuration.Configuration;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;

import java.rmi.RemoteException;

/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class RemoteUserStoreManagerServiceClient {
    private RemoteUserStoreManagerServiceStub userStoreManagerStub;

    public RemoteUserStoreManagerServiceClient(String cookie, String url) throws AxisFault {
        String serviceURL = url + "/services/RemoteUserStoreManagerService";
        userStoreManagerStub = new RemoteUserStoreManagerServiceStub(serviceURL);
        ServiceClient svcClient = userStoreManagerStub._getServiceClient();
        Options option;
        option = svcClient.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public void updateClaims(String claimURI, String data) throws RemoteException,
            RemoteUserStoreManagerServiceUserStoreExceptionException {
        userStoreManagerStub.setUserClaimValue(Configuration.getUserName(), claimURI, data, "default");
    }
}
