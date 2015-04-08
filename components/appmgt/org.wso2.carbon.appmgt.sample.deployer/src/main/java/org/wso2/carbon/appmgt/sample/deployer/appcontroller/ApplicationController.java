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
package org.wso2.carbon.appmgt.sample.deployer.appcontroller;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.sample.deployer.appm.WSRegistryServiceClient;
import org.wso2.carbon.appmgt.sample.deployer.bean.AppCreateRequest;
import org.wso2.carbon.appmgt.sample.deployer.bean.MobileApplicationBean;
import org.wso2.carbon.appmgt.sample.deployer.configuration.Configuration;
import org.wso2.carbon.appmgt.sample.deployer.appm.ManageUser;
import org.wso2.carbon.appmgt.sample.deployer.http.HttpHandler;
import org.wso2.carbon.appmgt.sample.deployer.javascriptwrite.InvokeStatistcsJavascriptBuilder;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import java.io.IOException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
*
* This class is use to create,publish and subscribe web applications.
* This also create and publish mobile applications.
*
*/
public class ApplicationController {

    final static Logger log = Logger.getLogger(ApplicationController.class.getName());
    private static String appmHomePath = CarbonUtils.getCarbonHome();
    private String storeSession;
    private HttpHandler httpHandler;
    private String httpsBackEndUrl;
    private String httpBackEndUrl;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private ConcurrentHashMap<String, String> trackingCodes;
    private InvokeStatistcsJavascriptBuilder invokeStatistcsJavascriptBuilder;
    private String ipAddress = "localhost";
    private String currentUserName;
    private ManageUser manageUser;
    private Random random;
    private String adminPublisherSession;

    /**
     * Creates a new ApplicationController object and initialising objects,attributes
     *
     * @param currentUserName
     *            - Current logged username
     *
     * @throws AppManagementException
     *             - Throws this when failed to initialise the ip address
     *             - Throws this when failed to initialise the WSRegistryServiceClient
     */
    public ApplicationController(String currentUserName) throws AppManagementException {
        this.currentUserName = currentUserName;
        try {
            this.ipAddress = NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            log.error("Error while initialising IP address",e);
            throw  new AppManagementException("Error while initialising IP address",e);
        }
        log.info("IP Address is : " + ipAddress);
        httpsBackEndUrl = Configuration.getHttpsUrl();
        httpBackEndUrl = Configuration.getHttpUrl();
        httpHandler = new HttpHandler();
        trackingCodes = new ConcurrentHashMap<String, String>();
        random = new Random();

        try {
            wsRegistryServiceClient = new WSRegistryServiceClient(httpsBackEndUrl);
        } catch (RegistryException e) {
            log.error("Error while creating a WSRegistryServiceClient",e);
            throw  new AppManagementException("Error while creating WSRegistryServiceClient",e);
        } catch (AxisFault axisFault) {
            log.error("Error while creating a WSRegistryServiceClient", axisFault);
            throw  new AppManagementException("Error while creating WSRegistryServiceClient",axisFault);
        }
    }

    /**
     * This method is use for create,publish and subscribe two sample web application
     *
     * @param publisherSession
     *            - Current logged publisher session
     *
     * @throws AppManagementException
     *             - Throws this when failed to add an user
     *             - Throws this when store session is failed while requesting
     *             - Throws this when policy id is failed while requesting
     *             - Throws this when failed to create,publish or subscribe web application
     */
    public void manageWebApplication(String publisherSession) throws AppManagementException{
        if(currentUserName.equals("admin")){
            adminPublisherSession = publisherSession;
        }else{
            try {
                adminPublisherSession =  httpHandler.doPostHttps(httpsBackEndUrl + "/publisher/api/authenticate",
                        "username=" + Configuration.getUserName() + "&password=" + Configuration.getPassword() +
                                "&action=login", ""
                        , "application/x-www-form-urlencoded");
            } catch (IOException e) {
                log.error("Error while requesting publisher session", e);
                throw  new AppManagementException("Error while requesting publisher session",e);
            }
        }

        try {
            manageUser = new ManageUser();
            manageUser.addUser("subscriber_"+currentUserName);
        } catch (UserAdminUserAdminException e) {
            log.error("Error while registering a User",e);
        } catch (RemoteException e) {
            log.error("Error while registering a User",e);
        } catch (LoginAuthenticationExceptionException e) {
            log.error("Error while login", e);
            throw  new AppManagementException("Error while login",e);
        }
        try {
            storeSession = httpHandler.doPostHttp(httpBackEndUrl + "/store/apis/user/login",
                    "{\"username\":\"subscriber_"+currentUserName+"\"" +
                            ",\"password\":\"subscriber\"}", "header", "application/json");
        } catch (IOException e) {
            log.error("Error while requesting a store session",e);
            throw  new AppManagementException("Error while requesting a store session",e);
        }
        log.info("Store session id is : " + storeSession);
        String policyIDResponce = null;
        try {
            policyIDResponce = httpHandler.doPostHttps(httpsBackEndUrl + "/publisher/api/entitlement/policy/partial" +
                            "/policyGroup/save", "anonymousAccessToUrlPattern=false&policyGroupName" +
                            "=test&throttlingTier=Unlimited&objPartialMappings=[]&policyGroupDesc=null&userRoles=",
                    publisherSession, "application/x-www-form-urlencoded; charset=UTF-8").split(":")[3];
        } catch (IOException e) {
            log.error("Error while requesting a policy id", e);
            throw  new AppManagementException("Error while requesting a policy id",e);
        }
        String policyId = policyIDResponce.substring(1, (policyIDResponce.length() - 2)).trim();
        //initialise common properties for web applications
        AppCreateRequest appCreateRequest = new AppCreateRequest();
        appCreateRequest.setUritemplate_policyGroupIds("[" + policyId + "]");
        appCreateRequest.setUritemplate_policyGroupId4(policyId);
        appCreateRequest.setUritemplate_policyGroupId3(policyId);
        appCreateRequest.setUritemplate_policyGroupId2(policyId);
        appCreateRequest.setUritemplate_policyGroupId1(policyId);
        appCreateRequest.setUritemplate_policyGroupId0(policyId);
        appCreateRequest.setOverview_provider(currentUserName);
        appCreateRequest.setClaimPropertyName0("http://wso2.org/claims/streetaddress,http://wso2.org/ffid" +
                ",http://wso2.org/claims/telephone");
        appCreateRequest.setClaimPropertyCounter("3");
        //publishing plan your trip web application
        log.info("publishing PlanYourTrip");
        appCreateRequest.setOverview_name("PlanYourTrip_"+currentUserName);
        appCreateRequest.setOverview_displayName("Plan Your Trip");
        appCreateRequest.setOverview_context(generateWebAppContext("/planYourTrip"));
        appCreateRequest.setOverview_version("1.0.0");
        appCreateRequest.setOverview_trackingCode(appCreateRequest.generateTrackingID());
        appCreateRequest.setOverview_transports("http");
        appCreateRequest.setOverview_webAppUrl(httpBackEndUrl + "/plan-your-trip-1.0/");
        String UUID = null;
        try {
            UUID = createWebApplication(appCreateRequest, publisherSession);
        } catch (IOException e) {
            log.error("Error while creating a web application Plan Your Trip", e);
            throw  new AppManagementException("Error while creating a web application Plan Your Trip", e);
        } catch (RegistryException e) {
            log.error("Error while creating a web application Plan Your Trip", e);
            throw  new AppManagementException("Error while creating a web application Plan Your Trip", e);
        } catch (InterruptedException e) {
            log.error("Error while creating a web application Plan Your Trip", e);
            throw  new AppManagementException("Error while creating a web application Plan Your Trip", e);
        }
        try {
            publishApplication("webapp", UUID);
        } catch (IOException e) {
            log.error("Error while publishing a web application Plan Your Trip", e);
            throw  new AppManagementException("Error while publishing a web application Plan Your Trip", e);
        }
        log.info(appCreateRequest.getOverview_name() + " published and UUID is " + UUID);
        try {
            subscribeApplication(appCreateRequest);
        } catch (IOException e) {
            log.error("Error while subscribing a web application Plan Your Trip", e);
            throw  new AppManagementException("Error while subscribing a web application Plan Your Trip", e);
        }
        log.info(appCreateRequest.getOverview_name() + "application subscribed by user ");
        //publishing travel booking application
        log.info("publishing TravelBooking");
        appCreateRequest.setOverview_name("TravelBooking_"+currentUserName);
        appCreateRequest.setOverview_displayName("TravelBooking");
        appCreateRequest.setOverview_context(generateWebAppContext("/travelBooking"));
        appCreateRequest.setOverview_version("1.0.0");
        appCreateRequest.setOverview_transports("http");
        appCreateRequest.setOverview_trackingCode(appCreateRequest.generateTrackingID());
        appCreateRequest.setClaimPropertyName0("http://wso2.org/claims/givenname,http://wso2.org/claims/lastname" +
                ",http://wso2.org/claims/emailaddress,http://wso2.org/claims/streetaddress" +
                ",http://wso2.org/claims/zipcode,http://wso2.org/claims/country" +
                ",http://wso2.org/claims/card_number,http://wso2.org/claims/card_holder" +
                ",http://wso2.org/claims/expiration_date");
        appCreateRequest.setClaimPropertyCounter("9");
        appCreateRequest.setOverview_webAppUrl(httpBackEndUrl + "/travel-booking-1.0/");
        try {
            UUID = createWebApplication(appCreateRequest, publisherSession);
        } catch (IOException e) {
            log.error("Error while creating a web application TravelBooking", e);
            throw  new AppManagementException("Error while creating a web application TravelBooking", e);
        } catch (RegistryException e) {
            log.error("Error while creating a web application TravelBooking", e);
            throw  new AppManagementException("Error while creating a web application TravelBooking", e);
        } catch (InterruptedException e) {
            log.error("Error while creating a web application TravelBooking", e);
            throw  new AppManagementException("Error while creating a web application TravelBooking", e);
        }
        try {
            publishApplication("webapp", UUID);
        } catch (IOException e) {
            log.error("Error while publishing a web application TravelBooking", e);
            throw  new AppManagementException("Error while publishing a web application TravelBooking", e);
        }
        log.info(appCreateRequest.getOverview_name() + " published and UUID is " + UUID);
        try {
            subscribeApplication(appCreateRequest);
        } catch (IOException e) {
            log.error("Error while subscribing a web application TravelBooking", e);
            throw  new AppManagementException("Error while subscribing a web application TravelBooking", e);
        }
        log.info(appCreateRequest.getOverview_name() + "application subscribed by user ");
    }

    /**
     * This method is use for create and publish sample mobile application
     *
     * @param publisherSession
     *            - Current logged publisher session
     *
     * @throws AppManagementException
     *             - Throws this when apk file is failed while uploading
     *             - Throws this when failed to create or publish web application
     */
    public void manageMobilebApplication(String publisherSession) throws AppManagementException {
        log.info("publishing CleanCalc mobile application");
        MobileApplicationBean mobileApplicationBean = new MobileApplicationBean();
        mobileApplicationBean.setApkFile(appmHomePath + "/repository/resources/mobileapps/CleanCalc" +
                "/Resources/CleanCalc.apk");
        String appMeta = null;
        try {
            appMeta = httpHandler.doPostMultiData(httpsBackEndUrl + "/publisher/api/mobileapp/upload",
                    true, mobileApplicationBean, publisherSession);
        } catch (IOException e) {
            log.error("Error while uploading an APK file of CLeanCalc Mobile Application", e);
            throw  new AppManagementException("Error while uploading an APK file of CLeanCalc Mobile Application", e);
        }
        mobileApplicationBean.setVersion("1.0.0");
        mobileApplicationBean.setProvider("1WSO2Mobile");
        mobileApplicationBean.setMarkettype("Enterprise");
        mobileApplicationBean.setPlatform("android");
        mobileApplicationBean.setName("CleanCalc");
        mobileApplicationBean.setDescription("this is a clean calucultor");
        mobileApplicationBean.setBannerFilePath(appmHomePath + "/repository/resources/mobileapps/" +
                "CleanCalc/Resources/banner.png");
        mobileApplicationBean.setIconFile(appmHomePath + "/repository/resources/mobileapps/CleanCalc" +
                "/Resources/icon.png");
        mobileApplicationBean.setScreenShot1File(appmHomePath + "/repository/resources/mobileapps/CleanCalc" +
                "/Resources/screen1.png");
        mobileApplicationBean.setScreenShot2File(appmHomePath + "/repository/resources/mobileapps/CleanCalc" +
                "/Resources/screen2.png");
        mobileApplicationBean.setScreenShot3File(appmHomePath + "/repository/resources/mobileapps/CleanCalc" +
                "/Resources/screen3.png");
        mobileApplicationBean.setMobileapp("mobileapp");
        mobileApplicationBean.setAppmeta(appMeta);
        String UUID = null;
        try {
            UUID = createMobielAppliaction(mobileApplicationBean, publisherSession);
        } catch (IOException e) {
            log.error("Error while creating CLeanCalc Mobile Application", e);
            throw  new AppManagementException("Error while creating CLeanCalc Mobile Application", e);
        } catch (InterruptedException e) {
            log.error("Error while creating CLeanCalc Mobile Application", e);
            throw  new AppManagementException("Error while creating CLeanCalc Mobile Application", e);
        }
        try {
            publishApplication("mobileapp", UUID);
        } catch (IOException e) {
            log.error("Error while publishing CLeanCalc Mobile Application", e);
            throw  new AppManagementException("Error while publishing CLeanCalc Mobile Application", e);
        }
        //wso2Con mobile application
        log.info("publishing WSO2Con mobile application");
        mobileApplicationBean.setAppmeta("{\"package\":\"com.wso2.wso2con.mobile\",\"version\":\"1.0.0\"}");
        mobileApplicationBean.setVersion("1.0.0");
        mobileApplicationBean.setProvider("1WSO2Mobile");
        mobileApplicationBean.setMarkettype("Market");
        mobileApplicationBean.setPlatform("android");
        mobileApplicationBean.setName("Wso2Con");
        mobileApplicationBean.setDescription("WSO2Con mobile app provides the agenda and meeting tool for " +
                "WSO2Con. Get the app to follow the agenda, find out about the sessions and speakers, provide " +
                "feedback on the sessions, and get more information on the venue and sponsors. Join us at " +
                "WSO2Con, where we will place emerging technology, best practices, and WSO2 product features " +
                "in the perspective of accelerating development and building a connected business. We hope you " +
                "enjoy WSO2Con!");
        mobileApplicationBean.setBannerFilePath(appmHomePath + "/repository/resources/mobileapps/WSO2Con" +
                "/Resources/banner.png");
        mobileApplicationBean.setIconFile(appmHomePath + "/repository/resources/mobileapps/WSO2Con" +
                "/Resources/icon.png");
        mobileApplicationBean.setScreenShot1File(appmHomePath + "/repository/resources/mobileapps/WSO2Con" +
                "/Resources/screen1.png");
        mobileApplicationBean.setScreenShot2File(appmHomePath + "/repository/resources/mobileapps" +
                "/WSO2Con/Resources/screen2.png");
        mobileApplicationBean.setMobileapp("mobileapp");
        try {
            UUID = createMobielAppliaction(mobileApplicationBean, publisherSession);
        } catch (IOException e) {
            log.error("Error while creating WSO2Con Mobile Application", e);
            throw  new AppManagementException("Error while creating WSO2Con Mobile Application", e);
        } catch (InterruptedException e) {
            log.error("Error while creating WSO2Con Mobile Application", e);
            throw  new AppManagementException("Error while creating WSO2Con Mobile Application", e);
        }
        try {
            publishApplication("mobileapp", UUID);
        } catch (IOException e) {
            log.error("Error while publishing WSO2Con Mobile Application", e);
            throw  new AppManagementException("Error while publishing WSO2Con Mobile Application", e);
        }
        //MyTrack mobile application
        log.info("publishing MyTrack mobile application");
        mobileApplicationBean.setAppmeta("{\"package\":\"com.google.android.maps.mytracks\",\"version\":\"1.0" +
                ".0\"}");
        mobileApplicationBean.setVersion("1.0.0");
        mobileApplicationBean.setProvider("1WSO2Mobile");
        mobileApplicationBean.setMarkettype("Market");
        mobileApplicationBean.setPlatform("android");
        mobileApplicationBean.setName("MyTracks");
        mobileApplicationBean.setDescription("My Tracks records your path, speed, distance, and elevation while " +
                "you walk, run, bike, or do anything else outdoors. While recording, you can view your data live" +
                ", annotate your path, and hear periodic voice announcements of your progress.\n" +
                "With My Tracks, you can sync and share your tracks via Google Drive. For sharing, you can share" +
                " tracks with friends, see the tracks your friends have shared with you, or make tracks public " +
                "and share their URLs via Google+, Facebook, Twitter, etc. In addition to Google Drive, you can " +
                "also export your tracks to Google My Maps, Google Spreadsheets, or external storage.\n" +
                "My Tracks also supports Android Wear. For watches with GPS, My Tracks can perform GPS recording" +
                " of tracks without a phone and sync tracks to the phone. For watches without GPS, you can see " +
                "your current distance and time, and control the recording of your tracks from your wrist.");
        mobileApplicationBean.setBannerFilePath(appmHomePath + "/repository/resources/" +
                "mobileapps/MyTracks/Resources/banner.png");
        mobileApplicationBean.setIconFile(appmHomePath + "/repository/resources/mobileapps" +
                "/MyTracks/Resources/icon.png");
        mobileApplicationBean.setScreenShot1File(appmHomePath + "/repository/resources/mobileapps/MyTracks" +
                "/Resources/screen1.png");
        mobileApplicationBean.setScreenShot2File(appmHomePath + "/repository/resources/mobileapps/MyTracks" +
                "/Resources/screen2.png");
        mobileApplicationBean.setMobileapp("mobileapp");
        try {
            UUID = createMobielAppliaction(mobileApplicationBean, publisherSession);
        } catch (IOException e) {
            log.error("Error while creating MyTrack Mobile Application", e);
            throw  new AppManagementException("Error while creating MyTrack Mobile Application", e);
        } catch (InterruptedException e) {
            log.error("Error while creating MyTrack Mobile Application", e);
            throw  new AppManagementException("Error while creating MyTrack Mobile Application", e);
        }
        try {
            publishApplication("mobileapp", UUID);
        } catch (IOException e) {
            log.error("Error while publishing MyTrack Mobile Application", e);
            throw  new AppManagementException("Error while publishing MyTrack Mobile Application", e);
        }

    }

    /**
     * This method is use for create web application
     *
     * @param appCreateRequest
     *            - bean object of the web application
     * @param publisherSession
     *            - Current logged publisher session
     *
     * @throws IOException
     *             - Throws this when failed to create web application
     *
     * @throws RegistryException
     *             - Throws this when UUID failed while requesting
     *
     * @throws AppManagementException
     *             - Throws this when gateway port failed while retrieving
     *
     * @throws java.lang.InterruptedException
     *             - Throws this when thread failed
     */
    private String createWebApplication(AppCreateRequest appCreateRequest, String publisherSession)
            throws IOException, RegistryException, AppManagementException, InterruptedException {
        String payload = appCreateRequest.generateRequestParameters();
        httpHandler.doPostHttps(httpsBackEndUrl + "/publisher/asset/webapp", payload
                , publisherSession, "application/x-www-form-urlencoded");
        String claims_ary = "[\"http://wso2.org/claims/givenname\"]";

        if (appCreateRequest.getClaimPropertyName0().contains(",")) {
            claims_ary = "[";
            String[] claims = appCreateRequest.getClaimPropertyName0().split(",");
            for (int i = 0; i < claims.length; i++) {
                claims_ary += "\"" + claims[i] + "\"";
                if (claims.length - 1 != i) {
                    claims_ary += ",";
                }
            }
            claims_ary += "]";
        }
        String jsonPayload = "{\"provider\":\"wso2is-5.0.0\",\"logout_url\":\"\",\"claims\":" + claims_ary + "" +
                ",\"app_name\":\"" + appCreateRequest.getOverview_name() + "\",\"app_verison\":\""
                + appCreateRequest.getOverview_version() + "\",\"app_transport\":\"http\",\"app_context\":\""
                + appCreateRequest.getOverview_context() + "\",\"app_provider\":\"" + currentUserName
                + "\",\"app_allowAnonymous\":\"f" +
                "alse\"}";
        httpHandler.doPostHttps(httpsBackEndUrl + "/publisher/api/sso/addConfig", jsonPayload, publisherSession
                , "application/json; charset=UTF-8");
        String appPath = "/_system/governance/appmgt/applicationdata/provider/"
                + currentUserName + "/" +
                appCreateRequest.getOverview_name() + "/1.0.0/webapp";
        String UUID = wsRegistryServiceClient.getUUID(appPath);
        String trackingIDResponse = httpHandler.doGet(httpsBackEndUrl + "/publisher/api/asset/webapp/trackingid/" + UUID
                , "", publisherSession, "").split(":")[1].trim();
        String trackingID = trackingIDResponse.substring(1, (trackingIDResponse.length() - 2));

        trackingCodes.put(appCreateRequest.getOverview_context(), trackingID);
        invokeStatistcsJavascriptBuilder = new InvokeStatistcsJavascriptBuilder
                (trackingID, ipAddress,Configuration.getGatewayPort());
        if (appCreateRequest.getOverview_name().equals("PlanYourTrip_"+currentUserName)) {
            invokeStatistcsJavascriptBuilder.buildInvokeStaticsJavascriptFile(appmHomePath +
                    "/repository/deployment/server/webapps/plan-your-trip-1.0");
        } else if (appCreateRequest.getOverview_name().equals("TravelBooking_"+currentUserName)) {
            invokeStatistcsJavascriptBuilder.buildInvokeStaticsJavascriptFile(appmHomePath +
                    "/repository/deployment/server/webapps/travel-booking-1.0/js");
        }
        log.info(appCreateRequest.getOverview_name() + " created and UUID is : " + UUID);
        return UUID;
    }

    /**
     * This method is use for publish web application or mobile application
     *
     * @param applicationType
     *            - type of the application whether mobile application or web application
     * @param UUID
     *            - UUID of the application
     *
     * @throws IOException
     *             - Throws this when failed to create web application
     *
     * @throws RegistryException
     *             - Throws this when UUID failed while requesting
     */
    private void publishApplication(String applicationType, String UUID) throws IOException {
        httpHandler.doPut(httpsBackEndUrl + "/publisher/api/lifecycle/Submit%20for%20Review/" + applicationType + "/"
                + UUID
                , adminPublisherSession);
        httpHandler.doPut(httpsBackEndUrl + "/publisher/api/lifecycle/Approve/" + applicationType + "/" + UUID
                , adminPublisherSession);
        httpHandler.doPut(httpsBackEndUrl + "/publisher/api/lifecycle/Publish/" + applicationType + "/" + UUID
                , adminPublisherSession);
    }

    /**
     * This method is use for suscribe web application
     *
     * @param appCreateRequest
     *            - Bean of the web application
     *
     * @throws IOException
     *             - Throws this when failed to suscribe web application
     */
    private void subscribeApplication(AppCreateRequest appCreateRequest) throws IOException {
        httpHandler.doPostHttps(httpsBackEndUrl + "/store/resources/webapp/v1/subscription/app",
                "apiName=" + appCreateRequest.getOverview_name() + "" +
                        "&apiVersion=" + appCreateRequest.getOverview_version() + "&apiTier=" +
                        appCreateRequest.getOverview_tier()
                        + "&subscriptionType=INDIVIDUAL&apiProvider=" + currentUserName + "&appName=DefaultApplication"
                , storeSession, "application/x-www-form-urlencoded; charset=UTF-8");
    }

    /**
     * This method is use for suscribe web application
     *
     * @param mobileApplicationBean
     *            - Bean of the mobile application
     *
     * @param publisherSession
     *            - Current logged publisher session
     *
     * @throws IOException
     *             - Throws this when failed to suscribe web application
     */
    private String createMobielAppliaction(MobileApplicationBean mobileApplicationBean, String publisherSession
            ) throws IOException, InterruptedException {
        return httpHandler.doPostMultiData(httpsBackEndUrl + "/publisher/api/asset/mobileapp",false,
                mobileApplicationBean, publisherSession);
    }

    /**
     * This method is use for accses a web application according to user given hit count
     *
     * @param webContext
     *            - Context of the web application
     *
     * @param trackingCode
     *            - Tracking code of the web application
     *
     * @param hitCount
     *            - Hit count for web application
     *
     * @param ipAddress
     *            - IP address of the user mashine
     *
     * @throws AppManagementException
     *             - Throws this when failed to accses web application
     *             - Throws this when thread failed to sleep
     */
    public void accsesWebPages(String webContext, String trackingCode, int hitCount, String ipAddress) throws AppManagementException {
        String loginHtmlPage = null;
        StringBuilder webAppUrlBuilder = new StringBuilder();
        webAppUrlBuilder.append("http://");
        webAppUrlBuilder.append(ipAddress);
        webAppUrlBuilder.append(":");
        webAppUrlBuilder.append(Configuration.getGatewayPort());
        webAppUrlBuilder.append(webContext);
        webAppUrlBuilder.append("/1.0.0/");
        String webAppurl = webAppUrlBuilder.toString();
        String responceHtml = null;
        try {
            loginHtmlPage = httpHandler.getHtml(webAppurl);
            Document html = Jsoup.parse(loginHtmlPage);
            Element sessionDataKeyElement = html.select("input[name=sessionDataKey]").first();
            String sessionDataKey = sessionDataKeyElement.val();
            responceHtml = httpHandler.doPostHttps(httpsBackEndUrl + "/commonauth"
                    , "username=subscriber&password=subscriber&sessionDataKey=" + sessionDataKey
                    , "none"
                    , "application/x-www-form-urlencoded; charset=UTF-8");
            Document postHtml = Jsoup.parse(responceHtml);
            Element postHTMLResponse = postHtml.select("input[name=SAMLResponse]").first();
            String samlResponse = postHTMLResponse.val();
            String appmSamlSsoTokenId = httpHandler.doPostHttp(webAppurl,
                    "SAMLResponse=" + URLEncoder.encode(samlResponse, "UTF-8"), "appmSamlSsoTokenId",
                    "application/x-www-form-urlencoded; charset=UTF-8");
            for (int i = 0; i < hitCount; i++) {
                if (webContext.equals("/notifi")) {
                    if (i == hitCount / 5) {
                        webAppurl += "member/";
                    } else if (i == hitCount / 2) {
                        webAppurl = appendPageToUrl("admin", webAppurl, false);
                    }
                } else if (webContext.equals("/travelBooking")) {
                    if (i == hitCount / 5) {
                        webAppurl = appendPageToUrl("booking-step1.jsp", webAppurl, true);
                    } else if (i == hitCount / 2) {
                        webAppurl = appendPageToUrl("booking-step2.jsp", webAppurl, false);
                    }
                }
                httpHandler.doGet("http://" + ipAddress + ":"+Configuration.getGatewayPort()+"/statistics/",
                        trackingCode, appmSamlSsoTokenId, webAppurl);
                log.info("Web Page : " + webAppurl + " Hit count : " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Error while accessing a web page", e);
                    throw  new AppManagementException("Error while accessing a web page", e);
                }
            }
        } catch (IOException e) {
            log.error("Error while accessing a web page", e);
            throw  new AppManagementException("Error while accessing a web page", e);
        }

    }

    /**
     * This method is use to build a url
     *
     * @param pageName
     *            - Page name of the web application
     *
     * @param webAppUrl
     *            - Current url of web application
     *
     * @param isAppendLastOne
     *            -
     *
     */
    private String appendPageToUrl(String pageName, String webAppUrl, boolean isAppendLastOne) {
        String elements[] = webAppUrl.split("/");
        StringBuilder newUrl = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            if (!elements[i].equals("")) {
                if (i == 0) {
                    newUrl.append(elements[i] + "//");
                } else if ((i == (elements.length - 1)) && isAppendLastOne) {
                    newUrl.append(elements[i] + "/");
                } else if (i != (elements.length - 1)) {
                    newUrl.append(elements[i] + "/");
                }
            }
        }
        newUrl.append(pageName + "/");
        return newUrl.toString();
    }

    /**
     * This method is use to check the availabilty of given context and if it is available generate new context
     *
     * @param context
     *            - Page name of the web application
     *
     * @return generated or given context
     *
     */
    private String generateWebAppContext(String context){
        if(!AppMDAO.isContextExist(context)){
            return context;
        }else {
            context += (random.nextInt(10 - 0 + 1));
            return generateWebAppContext(context);
        }


    }

}
