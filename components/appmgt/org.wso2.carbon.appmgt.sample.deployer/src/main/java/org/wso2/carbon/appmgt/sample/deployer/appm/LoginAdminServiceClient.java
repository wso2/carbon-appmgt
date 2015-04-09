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
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;

import java.rmi.RemoteException;

/**
 *
 * This class is use as a client for AuthenticationAdminStub
 *
 * */
public class LoginAdminServiceClient {

    private final String serviceName = "AuthenticationAdmin";
    private AuthenticationAdminStub authenticationAdminStub;
    private String endPoint;

    /**
     * Creates a new LoginAdminServiceClient object and initialising the AuthenticationAdminStub
     *
     * @param backEndUrl
     *            https server url
     *
     * @throws AxisFault
     *            Throws this when AuthenticationAdminStub failed to initialise
     */
    public LoginAdminServiceClient(String backEndUrl) throws AxisFault {
        this.endPoint = backEndUrl + "/services/" + serviceName;
        authenticationAdminStub = new AuthenticationAdminStub(endPoint);
    }

    /**
     * This method is use to get authentication for accses admin services
     *
     * @return
     *         return a session
     *
     * @throws RemoteException
     *         Throws this when failed connect with the AuthenticationAdminService
     *
     * @throws LoginAuthenticationExceptionException
     *         Throws this when failed to authenticate with given username and password
     * */
    public String authenticate(String userName, String password) throws RemoteException,
            LoginAuthenticationExceptionException {
        String sessionCookie = null;
        if (authenticationAdminStub.login(userName, password, "localhost")) {
            ServiceContext serviceContext = authenticationAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        }
        return sessionCookie;
    }
}
