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
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.webapp.mgt.stub.WebappAdminStub;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappsWrapper;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Locale;

/**
 * Web Application admin client on the AS. Used only to query available web applications and get
 * associated metadata.
 *
 */
public class AppServerWebappAdminClient {

    private WebappAdminStub webappAdminStub;
    private AuthenticationAdminStub authenticationAdminStub;
    private String serviceURL;
    private String backendServerURL;
    private String userName;
    private String sessionCookie;
    private ConfigurationContext configCtx;

    public AppServerWebappAdminClient(String backendServerURL,
            ConfigurationContext configCtx, Locale locale) throws AppManagementException {
        this.configCtx = configCtx;
        this.backendServerURL = backendServerURL;
        serviceURL = backendServerURL + "WebappAdmin";
    }

    /**
     * Logs in to the webapp admin client
     * @param userName
     * @param password
     * @throws AppManagementException
     */
    public void login(String userName, String password) throws AppManagementException {
        this.userName = userName;
        try {
            authenticationAdminStub = new AuthenticationAdminStub(configCtx,
                    backendServerURL + "AuthenticationAdmin");
            webappAdminStub = new WebappAdminStub(configCtx, serviceURL);
            ServiceClient client = webappAdminStub._getServiceClient();
            Options options = client.getOptions();

            String host = new URL(serviceURL).getHost();
            //Try to use SAML autnentication. If fails use basic authentication.
            if (authenticationAdminStub.login(userName, password, host)) {
                ServiceContext serviceContext = authenticationAdminStub.
                        _getServiceClient().getLastOperationContext().getServiceContext();
                sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
                options.setManageSession(true);
                options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                        sessionCookie);
            } else {
                HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
                auth.setUsername(userName);
                auth.setPassword(password);
                options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE,
                        auth);
            }
        } catch (AxisFault axisFault) {
            final String msg = String
                    .format("Could not login to Admin Service. Server [%s],Reason: %s",
                            backendServerURL, axisFault.getMessage());
            throw new AppManagementException(msg, axisFault);
        } catch (RemoteException e) {
            final String msg = String
                    .format("Could not login to Admin Service. Server [%s],Reason: %s",
                            backendServerURL, e.getMessage());
            throw new AppManagementException(msg, e);
        } catch (LoginAuthenticationExceptionException e) {
            final String msg = String
                    .format("Error in authentication on Admin Service. Server [%s],Reason: %s",
                            backendServerURL, e.getMessage());
            throw new AppManagementException(msg, e);
        } catch (MalformedURLException e) {
            final String msg = String
                    .format("Could not login to Admin Service. Server [%s],Reason: %s",
                            backendServerURL, e.getMessage());
            throw new AppManagementException(msg, e);
        }
    }

    /**
     * Logs out of the previously logged in client.
     *
     * @throws AppManagementException if not already logged in or any error occurs while logging out
     */
    public void logout() throws AppManagementException {
        if (authenticationAdminStub == null) {
            throw new AppManagementException("Logout called on previously not logged in client");
        }
        try {
            authenticationAdminStub.logout();
        } catch (RemoteException e) {
            final String msg = String
                    .format("Could not logout from Admin Service. Server [%s],Reason: %s",
                            backendServerURL, e.getMessage());
            throw new AppManagementException(msg, e);
        } catch (LogoutAuthenticationExceptionException e) {
            final String msg = String
                    .format("Could not logout from Admin Service. Server [%s],Reason: %s",
                            backendServerURL, e.getMessage());
            throw new AppManagementException(msg, e);
        }
    }

    public WebappsWrapper getPagedWebappsSummary(String webappSearchString, String webappState,
            String webappType, int pageNumber) throws AppManagementException {
        try {
            WebappsWrapper result = webappAdminStub
                    .getPagedWebappsSummary(webappSearchString, webappState, webappType,
                            pageNumber);
            return result;
        } catch (RemoteException e) {
            final String msg = String.format("Failed to get available applications from "
                            + "Application server. Server [%s], User Name [%s] Reason: %s",
                    serviceURL, userName, e.getMessage());
            throw new AppManagementException(msg, e);
        }
    }
}
