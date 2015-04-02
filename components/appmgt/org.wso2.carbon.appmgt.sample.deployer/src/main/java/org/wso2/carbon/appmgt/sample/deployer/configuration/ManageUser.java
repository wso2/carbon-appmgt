package org.wso2.carbon.appmgt.sample.deployer.configuration;


import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.appmgt.sample.deployer.appm.LoginAdminServiceClient;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
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
public class ManageUser {

    private static final String appmHome = CarbonUtils.getCarbonHome();

    private static final String axis2Repo = appmHome + File.separator + "repository" +
            File.separator + "deployment" + File.separator + "client";
    private static final String axis2Conf =
            ServerConfiguration.getInstance().getFirstProperty("Axis2Config.clientAxis2XmlLocation");
    private UserAdminStub userAdminStub;

    public ManageUser() throws RemoteException, LoginAuthenticationExceptionException {
        String backEndUrl = Configuration.getHttpsUrl();
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                axis2Repo, axis2Conf);
        userAdminStub = new UserAdminStub(configContext, backEndUrl + "/services/UserAdmin");
        LoginAdminServiceClient loginAdminServiceClient = new LoginAdminServiceClient(backEndUrl);
        String session = loginAdminServiceClient.authenticate(Configuration.getUserName()
                , Configuration.getPassword());
        Options option;
        ServiceClient serviceClient;
        serviceClient = userAdminStub._getServiceClient();
        option = serviceClient.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, session);
    }

    public void addUser() throws RemoteException, UserAdminUserAdminException {
        ClaimValue ffid = new ClaimValue();
        ffid.setClaimURI("http://wso2.org/ffid");
        ffid.setValue("234455666");
        ClaimValue streetaddress = new ClaimValue();
        streetaddress.setClaimURI("http://wso2.org/claims/streetaddress");
        streetaddress.setValue("234455666");
        ClaimValue zipcode = new ClaimValue();
        zipcode.setClaimURI("http://wso2.org/claims/zipcode");
        zipcode.setValue("GL");
        ClaimValue card_number = new ClaimValue();
        card_number.setClaimURI("http://wso2.org/claims/card_number");
        card_number.setValue("001012676878");
        ClaimValue card_holder = new ClaimValue();
        card_holder.setClaimURI("http://wso2.org/claims/card_holder");
        card_holder.setValue("subscriber");
        ClaimValue telephone = new ClaimValue();
        telephone.setClaimURI("http://wso2.org/claims/telephone");
        telephone.setValue("0918886565");
        ClaimValue givenName = new ClaimValue();
        givenName.setClaimURI("http://wso2.org/claims/givenname");
        givenName.setValue("Subscriber");
        ClaimValue lastName = new ClaimValue();
        lastName.setClaimURI("http://wso2.org/claims/lastname");
        lastName.setValue("Subscriber");
        ClaimValue email = new ClaimValue();
        email.setClaimURI("http://wso2.org/claims/emailaddress");
        email.setValue("wso2@wso2.com");
        ClaimValue country = new ClaimValue();
        country.setClaimURI("http://wso2.org/claims/country");
        country.setValue("SriLanka");
        ClaimValue expire_date = new ClaimValue();
        expire_date.setClaimURI("http://wso2.org/claims/expiration_date");
        expire_date.setValue("31/12/2015");
        ClaimValue claimValues[] = new ClaimValue[]{ffid, streetaddress, zipcode,
                card_number, card_holder, telephone
                , givenName, lastName, email, country, expire_date};
        userAdminStub.addUser("subscriber", "subscriber",
                new String[]{"Internal/subscriber"}, claimValues, "Subscriber");
    }
}
