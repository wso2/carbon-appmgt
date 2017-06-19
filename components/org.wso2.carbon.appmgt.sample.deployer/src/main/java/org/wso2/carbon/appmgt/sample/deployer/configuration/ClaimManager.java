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

package org.wso2.carbon.appmgt.sample.deployer.configuration;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.sample.deployer.appm.ClaimManagementServiceClient;
import org.wso2.carbon.appmgt.sample.deployer.appm.LoginAdminServiceClient;
import org.wso2.carbon.appmgt.sample.deployer.appm.RemoteUserStoreManagerServiceClient;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceClaimManagementException;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is use to manage claims
 *
 * */
public class ClaimManager {

    final static Logger log = Logger.getLogger(ClaimManager.class.getName());
    private String session;
    private RemoteUserStoreManagerServiceClient remoteUserStoreManagerServiceClient;
    private static String appmPath = CarbonUtils.getCarbonHome();
    private static String backendUrl = Configuration.getHttpsUrl();
    private ClaimManagementServiceClient claimManagementServiceClient;
    private LoginAdminServiceClient loginAdminServiceClient;

    static {
        System.setProperty("javax.net.ssl.trustStore", appmPath + "/repository/resources/security/wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    /**
     * Creates a new ClaimManager object and initialising the ClaimManagementServiceStub
     *
     *
     * @throws AppManagementException
     *             Throws this when LoginAdminServiceClient failed initialise
     *             Throws this when authentication failed
     *             Throws this when ClaimManagementServiceClient failed initialise
     *             Throws this when RemoteUserStoreManagerServiceClient failed initialise
     */
    public ClaimManager() throws AppManagementException {
        try {
            loginAdminServiceClient = new LoginAdminServiceClient(backendUrl);
        } catch (AxisFault axisFault) {
            log.error("Error while creating an AuthenticationAdminStub", axisFault);
            throw  new AppManagementException("Error while creating AuthenticationAdminStub", axisFault);
        }

        try {
            session = loginAdminServiceClient.authenticate(Configuration.getUserName()
                    , Configuration.getPassword());
        } catch (RemoteException e) {
            log.error("Error while requesting a session ",e);
            throw  new AppManagementException("Error while requesting a session ",e);
        } catch (LoginAuthenticationExceptionException e) {
            log.error("Error while authenticating", e);
            throw  new AppManagementException("Error while authenticating", e);
        }

        try {
            claimManagementServiceClient = new ClaimManagementServiceClient(session, backendUrl);
        } catch (AxisFault axisFault) {
            log.error("Error while creating ClaimManagementServiceStub", axisFault);
            throw  new AppManagementException("Error while creating ClaimManagementServiceStub", axisFault);
        }
        try {
            remoteUserStoreManagerServiceClient = new RemoteUserStoreManagerServiceClient(session, backendUrl);
        } catch (AxisFault axisFault) {
            log.error("Error while creating RemoteUserStoreManagerServiceStub", axisFault);
            throw  new AppManagementException("Error while creating RemoteUserStoreManagerServiceStub", axisFault);
        }
    }
    /**
     * This method is use to add claim mapping
     *
     * @param claims
     *         map that include claim name,value and uri
     *
     * */
    public void addClaimMapping(ConcurrentHashMap<String,String[]> claims) throws AppManagementException {
        try {
            Iterator claimEntries = claims.entrySet().iterator();
            while (claimEntries.hasNext()) {
                Map.Entry thisEntry = (Map.Entry) claimEntries.next();
                Object key = thisEntry.getKey();
                String[] value = (String[]) thisEntry.getValue();
                if (value[2].equalsIgnoreCase("true")) {
                    claimManagementServiceClient.addClaim(key.toString()
                            , value[0], false);
                }
            }
        } catch (RemoteException e) {
            if(!e.getMessage().equals("Duplicate claim exist in the system. Please pick a different Claim Uri")){
                String errorMessage = "Error while adding a ClaimMapping";
                log.error(errorMessage, e);
                throw  new AppManagementException(errorMessage,e);
            }
        } catch (ClaimManagementServiceClaimManagementException e) {
            String errorMessage = "Error while adding a ClaimMapping";
            log.error(errorMessage, e);
            throw  new AppManagementException(errorMessage,e);
        }
    }

    /**
     * This method is use to update claim values
     *
     * @param userName
     *           currently logged user
     *
     * @param claims
     *         map that include claim name,value and uri
     *
     * @throws AppManagementException
     *           Throws this when failed to update claim value
     *
     * */
    public void setClaimValues(ConcurrentHashMap<String,String[]> claims,String userName) throws AppManagementException {
        try {
            Iterator claimEntries = claims.entrySet().iterator();
            while (claimEntries.hasNext()) {
                Map.Entry thisEntry = (Map.Entry) claimEntries.next();
                Object key = thisEntry.getKey();
                String[] value = (String[]) thisEntry.getValue();
                remoteUserStoreManagerServiceClient.updateClaims(userName,value[0]
                        , value[1]);
            }
        } catch (RemoteException e) {
            String errorMessage = "Error while updating a claim vaue";
            log.error(errorMessage, e);
            throw  new AppManagementException(errorMessage, e);
        } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
            String errorMessage = "Error while updating a claim vaue";
            log.error(errorMessage, e);
            throw  new AppManagementException(errorMessage, e);
        }
    }
}
