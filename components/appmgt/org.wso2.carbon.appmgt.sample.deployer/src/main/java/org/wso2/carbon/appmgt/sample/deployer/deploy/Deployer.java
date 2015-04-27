package org.wso2.carbon.appmgt.sample.deployer.deploy;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.sample.deployer.appcontroller.BackEndApplicationCreator;
import org.wso2.carbon.appmgt.sample.deployer.appcontroller.ProxyApplicationCreator;
import org.wso2.carbon.appmgt.sample.deployer.appm.UserAdminServiceClient;
import org.wso2.carbon.appmgt.sample.deployer.bean.WebAppDetail;
import org.wso2.carbon.appmgt.sample.deployer.configuration.ClaimManager;
import org.wso2.carbon.appmgt.sample.deployer.configuration.Configuration;
import org.wso2.carbon.appmgt.sample.deployer.http.HttpHandler;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

/**
 * This class is use to handle all the deploying processes for web application such as add subscribed user
 * ,add claims,set claim values,create web application,publish web application and subscribed web application
 *
 */
public class Deployer {
    final static Logger log = Logger.getLogger(Deployer.class.getName());

    public boolean deploySample(NativeObject request) throws AppManagementException {

        ProxyApplicationCreator proxyApplicationCreator = new ProxyApplicationCreator();
        WebAppDetail webAppDetail =  new WebAppDetail();
        ClaimManager claimManager = new ClaimManager();
        BackEndApplicationCreator backEndApplicationCreator = new BackEndApplicationCreator();
        HttpHandler httpHandler = new HttpHandler();

        String httpBackEndUrl = Configuration.getHttpsUrl();
        String storeSession;
        String username =  (String) NativeObject.getProperty(request, "username");
        username = username.replace("\"", "").trim();
        boolean response = false;
        log.debug("Username is : " + username);
        String creatorSession = (String) NativeObject.getProperty(request,"creatorSession");
        log.debug("Creator Session is : " + creatorSession);
        String webAppsJasonString = (String) NativeObject.getProperty(request,"webapps");
        JSONParser  parser =  new JSONParser();
        JSONObject webAppsJasonObject = null;
        try {
            webAppsJasonObject =(JSONObject) parser.parse(webAppsJasonString);
        } catch (ParseException e) {
            log.error("Error while parsing JSON object", e);
            throw  new AppManagementException("Error while parsing JSON object",e);
        }
        UserAdminServiceClient userAdminServiceClient;
        try {
            userAdminServiceClient = new UserAdminServiceClient();
            userAdminServiceClient.addUser("subscriber_" + username);
        } catch (UserAdminUserAdminException e) {

        } catch (RemoteException e) {
            log.error("Error while registering a User subscriber_" + username,e);
            throw  new AppManagementException("Error while registering a User subscriber_" + username,e);
        } catch (LoginAuthenticationExceptionException e) {
            log.error("Error while login in to UserAdminStub", e);
            throw  new AppManagementException("Error while login in to UserAdminStub",e);
        }

        try {
            storeSession = httpHandler.doPostHttp(httpBackEndUrl + "/store/apis/user/login",
                    "{\"username\":\"subscriber_"+username+"\"" +
                            ",\"password\":\"subscriber\"}", "header", "application/json");
            webAppDetail.setStoreSession(storeSession);
        } catch (IOException e) {
            log.error("Error while requesting a store session",e);
            throw  new AppManagementException("Error while requesting a store session",e);
        }

        for (Object webappName : webAppsJasonObject.keySet()){
            JSONObject webAppJsonObject = (JSONObject) webAppsJasonObject.get(webappName);
            webAppDetail.setWebAppName(webappName.toString() + "_" + username);
            webAppDetail.setVersion(webAppJsonObject.get("version").toString());
            if(!AppMDAO.isWebAppAvailable(webAppDetail.getWebAppName(),webAppDetail.getVersion())) {
                webAppDetail.setContext(webAppJsonObject.get("context").toString());
                webAppDetail.setCreatorSession(creatorSession);
                webAppDetail.setUserName(username);
                webAppDetail.setWarFileName(webAppJsonObject.get("warfilename").toString());
                webAppDetail.setDisplayName(webAppJsonObject.get("displayname").toString());
                JSONObject claims = (JSONObject) webAppJsonObject.get("claims");
                ConcurrentHashMap<String, String[]> claimsMap = new ConcurrentHashMap<String, String[]>();
                for (Object claimKey : claims.keySet()) {
                    JSONObject claimJobj = (JSONObject) claims.get(claimKey);
                    claimsMap.put(claimKey.toString(),
                            new String[]{claimJobj.get("url").toString(), claimJobj.get("value").toString()
                                    , claimJobj.get("isNew").toString()});
                }
                webAppDetail.setClaims(claimsMap);
                backEndApplicationCreator.copyFileUsingFileStreams(webAppDetail.getWarFileName());
                claimManager.addClaimMapping(claimsMap);
                claimManager.setClaimValues(claimsMap, "subscriber_" + username);
                proxyApplicationCreator.manageWebApplication(webAppDetail);
                response = true;
            }
        }
        return response;
    }
}
