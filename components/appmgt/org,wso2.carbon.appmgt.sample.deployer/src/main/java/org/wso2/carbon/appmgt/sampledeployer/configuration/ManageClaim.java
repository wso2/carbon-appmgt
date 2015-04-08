package org.wso2.carbon.appmgt.sampledeployer.configuration;

import org.wso2.carbon.appmgt.sampledeployer.appm.ClaimManagementServiceClient;
import org.wso2.carbon.appmgt.sampledeployer.appm.LoginAdminServiceClient;
import org.wso2.carbon.appmgt.sampledeployer.appm.RemoteUserStoreManagerServiceClient;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;

/**
 * Created by ushan on 3/27/15.
 */
public class ManageClaim {

    private ClaimManagementServiceClient claimManagementServiceClient;
    private LoginAdminServiceClient loginAdminServiceClient;
    private static String  session;
    private static RemoteUserStoreManagerServiceClient remoteUserStoreManagerServiceClient;
    private static String appmPath = CarbonUtils.getCarbonHome();
    private static String backendUrl= Configuration.getHttpsUrl();
    static {
        System.setProperty("javax.net.ssl.trustStore",appmPath+"/repository/resources/security/wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    public ManageClaim()throws RemoteException, LoginAuthenticationExceptionException {
        loginAdminServiceClient = new LoginAdminServiceClient(backendUrl);
        session = loginAdminServiceClient.authenticate(Configuration.getUserName()
                , Configuration.getPassword());
        claimManagementServiceClient =  new ClaimManagementServiceClient(session,backendUrl);
        remoteUserStoreManagerServiceClient =  new RemoteUserStoreManagerServiceClient(session,backendUrl);
    }

    public void addClaimMapping() throws RemoteException, ClaimManagementServiceException {
        claimManagementServiceClient.addClaim("FrequentFlyerID", "http://wso2.org/ffid", true);
        claimManagementServiceClient.addClaim("zipcode", "http://wso2.org/claims/zipcode", true);
        claimManagementServiceClient.addClaim("Credit card number", "http://wso2.org/claims/card_number", true);
        claimManagementServiceClient.addClaim("Credit cArd Holder Name", "http://wso2.org/claims/card_holder"
                , true);
        claimManagementServiceClient.addClaim("Credit card expiration date", "http://wso2.org/claims/expiration_date"
                , true);
    }

    public String setClaimValues() throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        remoteUserStoreManagerServiceClient.updateClaims("http://wso2.org/ffid", "12345151");
        remoteUserStoreManagerServiceClient.updateClaims("http://wso2.org/claims/streetaddress", "21/5");
        remoteUserStoreManagerServiceClient.updateClaims("http://wso2.org/claims/zipcode", "GL");
        remoteUserStoreManagerServiceClient.updateClaims("http://wso2.org/claims/card_number"
                , "001012676878");
        remoteUserStoreManagerServiceClient.updateClaims("http://wso2.org/claims/card_holder", "Admin");
        remoteUserStoreManagerServiceClient.updateClaims("http://wso2.org/claims/telephone", "091222222");
        remoteUserStoreManagerServiceClient.updateClaims("http://wso2.org/claims/givenname", "Sachith");
        remoteUserStoreManagerServiceClient.updateClaims("http://wso2.org/claims/lastname", "Ushan");
        remoteUserStoreManagerServiceClient.updateClaims("http://wso2.org/claims/emailaddress", "wso2@wso2.com");
        remoteUserStoreManagerServiceClient.updateClaims("http://wso2.org/claims/country", "SriLanka");
        remoteUserStoreManagerServiceClient.updateClaims("http://wso2.org/claims/expiration_date"
                , "31/12/2015");
        return session;
    }
}
