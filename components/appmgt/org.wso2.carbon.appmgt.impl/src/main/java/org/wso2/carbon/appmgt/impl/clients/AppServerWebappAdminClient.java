/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.impl.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.webapp.mgt.stub.WebappAdminStub;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappsWrapper;

import java.rmi.RemoteException;
import java.util.Locale;

/**
 * Web Application admin client on the AS. Used only to query available web applications and get
 * associated metadata.
 *
 */
public class AppServerWebappAdminClient {

    private WebappAdminStub stub;
    private String serviceURL;
    private String userName;

    public AppServerWebappAdminClient(String userName, String password, String backendServerURL,
            ConfigurationContext configCtx, Locale locale) throws AppManagementException {
        this.userName = userName;
        serviceURL = backendServerURL + "WebappAdmin";

        try {
            stub = new WebappAdminStub(configCtx, serviceURL);
        } catch (AxisFault axisFault) {
            final String msg = String
                    .format("Could not initialize Admin Service Stub. Server [%s],Reason: %s",
                            serviceURL, axisFault.getMessage());
            throw new AppManagementException(msg, axisFault);
        }

        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);

        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
        auth.setUsername(userName);
        auth.setPassword(password);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
    }

    public AppServerWebappAdminClient(String sessionCookie, String backendServerURL,
            ConfigurationContext configCtx, Locale locale) throws AppManagementException {
        serviceURL = backendServerURL + "WebappAdmin";

        try {
            stub = new WebappAdminStub(configCtx, serviceURL);
        } catch (AxisFault axisFault) {
            final String msg = String
                    .format("Could not initialize Admin Service Stub. Server [%s],Reason: %s",
                            serviceURL, axisFault.getMessage());
            throw new AppManagementException(msg, axisFault);
        }

        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);

        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                sessionCookie);

    }

    public WebappsWrapper getPagedWebappsSummary(String webappSearchString, String webappState,
            String webappType, int pageNumber) throws AppManagementException {
        try {
            WebappsWrapper result = stub
                    .getPagedWebappsSummary(webappSearchString, webappState, webappType,
                            pageNumber);
            ServiceContext serviceContext = stub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            System.out.println(sessionCookie);
            return result;
        } catch (RemoteException e) {
            final String msg = String.format("Failed to get available applications from "
                            + "Application server. Server [%s], User Name [%s] Reason: %s",
                    serviceURL, userName, e.getMessage());
            throw new AppManagementException(msg, e);
        }
    }
}
