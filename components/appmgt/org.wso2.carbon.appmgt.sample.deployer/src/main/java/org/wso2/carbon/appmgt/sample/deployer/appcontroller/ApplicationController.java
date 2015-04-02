package org.wso2.carbon.appmgt.sample.deployer.appcontroller;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.wso2.carbon.appmgt.sample.deployer.appm.WSRegistryService_Client;
import org.wso2.carbon.appmgt.sample.deployer.bean.AppCreateRequest;
import org.wso2.carbon.appmgt.sample.deployer.bean.MobileApplicationBean;
import org.wso2.carbon.appmgt.sample.deployer.configuration.Configuration;
import org.wso2.carbon.appmgt.sample.deployer.configuration.ManageUser;
import org.wso2.carbon.appmgt.sample.deployer.http.HttpHandler;
import org.wso2.carbon.appmgt.sample.deployer.javascriptwrite.InvokeStatistcsJavascriptBuilder;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationController {

    final static Logger log = Logger.getLogger(ApplicationController.class.getName());
    private static String appmHomePath = CarbonUtils.getCarbonHome();
    private String storeSession;
    private HttpHandler httpHandler;
    private String httpsBackEndUrl;
    private String httpBackEndUrl;
    private WSRegistryService_Client wsRegistryService_client;
    private ConcurrentHashMap<String, String> trackingCodes;
    private InvokeStatistcsJavascriptBuilder invokeStatistcsJavascriptBuilder;
    private String ipAddress = "localhost";
    private String currentUserName;
    private ManageUser manageUser;

    public ApplicationController(String currentUserName) throws IOException, RegistryException {
        this.currentUserName = currentUserName;
        this.ipAddress = NetworkUtils.getLocalHostname();
        log.info("IP Address is : " + ipAddress);
        httpsBackEndUrl = Configuration.getHttpsUrl();
        httpBackEndUrl = Configuration.getHttpUrl();
        httpHandler = new HttpHandler();
        trackingCodes = new ConcurrentHashMap<String, String>();
        wsRegistryService_client = new WSRegistryService_Client(httpsBackEndUrl);
    }

    public void manageWebApplication(String publisherSession) throws IOException, RegistryException, LoginAuthenticationExceptionException {
        manageUser = new ManageUser();
        try {
            manageUser.addUser();
        } catch (UserAdminUserAdminException e) {
            log.error(e.getMessage());
        }
        storeSession = httpHandler.doPostHttp(httpBackEndUrl + "/store/apis/user/login",
                "{\"username\":\"subscriber\"" +
                        ",\"password\":\"subscriber\"}", "header", "application/json");
        log.info("Store session id is : " + storeSession);
        String policyIDResponce = httpHandler.doPostHttps(httpsBackEndUrl + "/publisher/api/entitlement/policy/partial" +
                        "/policyGroup/save", "anonymousAccessToUrlPattern=false&policyGroupName" +
                        "=test&throttlingTier=Unlimited&objPartialMappings=[]&policyGroupDesc=null&userRoles=",
                publisherSession, "application/x-www-form-urlencoded; charset=UTF-8").split(":")[3];
        String policyId = policyIDResponce.substring(1, (policyIDResponce.length() - 2)).trim();
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
        //publishing travelWebapp
        log.info("publishing travleWebapp");
        appCreateRequest.setOverview_name("PlanYourTrip");
        appCreateRequest.setOverview_displayName("PlanYourTrip");
        appCreateRequest.setOverview_context("/planYourTrip");
        appCreateRequest.setOverview_version("1.0.0");
        appCreateRequest.setOverview_trackingCode(appCreateRequest.generateTrackingID());
        appCreateRequest.setOverview_transports("http");
        appCreateRequest.setOverview_webAppUrl(httpBackEndUrl + "/plan-your-trip-1.0/");
        String UUID = createWebApplication(appCreateRequest, publisherSession);
        publishApplication("webapp", UUID, publisherSession);
        log.info(appCreateRequest.getOverview_name() + " published and UUID is " + UUID);
        subscribeApplication(appCreateRequest);
        log.info(appCreateRequest.getOverview_name() + "application subscribed by user ");
        //publishing travel booking application
        log.info("publishing TravelBooking");
        appCreateRequest.setOverview_name("TravelBooking");
        appCreateRequest.setOverview_displayName("TravelBooking");
        appCreateRequest.setOverview_context("/travelBooking");
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
        UUID = createWebApplication(appCreateRequest, publisherSession);
        publishApplication("webapp", UUID, publisherSession);
        log.info(appCreateRequest.getOverview_name() + " published and UUID is " + UUID);
        subscribeApplication(appCreateRequest);
        log.info(appCreateRequest.getOverview_name() + "application subscribed by user ");
    }

    public void manageMobilebApplication(String publisherSession) throws IOException, InterruptedException {
        log.info("publishing CleanCalc mobile application");
        MobileApplicationBean mobileApplicationBean = new MobileApplicationBean();
        mobileApplicationBean.setApkFile(appmHomePath + "/repository/resources/mobileapps/CleanCalc" +
                "/Resources/CleanCalc.apk");
        String appMeta = httpHandler.doPostMultiData(httpsBackEndUrl + "/publisher/api/mobileapp/upload",
                "upload", mobileApplicationBean, publisherSession);
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
        String UUID = createMobielAppliaction(mobileApplicationBean, publisherSession);
        publishApplication("mobileapp", UUID, publisherSession);
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
        UUID = createMobielAppliaction(mobileApplicationBean, publisherSession);
        publishApplication("mobileapp", UUID, publisherSession);
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
        UUID = createMobielAppliaction(mobileApplicationBean, publisherSession);
        publishApplication("mobileapp", UUID, publisherSession);

    }

    private String createWebApplication(AppCreateRequest appCreateRequest, String publisherSession)
            throws IOException, RegistryException {
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
        String UUID = wsRegistryService_client.getUUID(appPath);
        String trackingIDResponse = httpHandler.doGet(httpsBackEndUrl + "/publisher/api/asset/webapp/trackingid/" + UUID
                , "", publisherSession, "").split(":")[1].trim();
        String trackingID = trackingIDResponse.substring(1, (trackingIDResponse.length() - 2));

        trackingCodes.put(appCreateRequest.getOverview_context(), trackingID);
        invokeStatistcsJavascriptBuilder = new InvokeStatistcsJavascriptBuilder
                (trackingID, ipAddress);//ip address to be implement
        if (appCreateRequest.getOverview_name().equals("travelWebapp")) {
            invokeStatistcsJavascriptBuilder.buildInvokeStaticsJavascriptFile(appmHomePath +
                    "/repository/deployment/server/webapps/plan-your-trip-1.0");
        } else if (appCreateRequest.getOverview_name().equals("TravelBooking")) {
            invokeStatistcsJavascriptBuilder.buildInvokeStaticsJavascriptFile(appmHomePath +
                    "/repository/deployment/server/webapps/travel-booking-1.0/js");
        }
        log.info(appCreateRequest.getOverview_name() + " created and UUID is : " + UUID);
        return UUID;
    }

    private void publishApplication(String applicationType, String UUID, String publisherSession) throws IOException {
        httpHandler.doPut(httpsBackEndUrl + "/publisher/api/lifecycle/Submit%20for%20Review/" + applicationType + "/"
                + UUID
                , publisherSession);
        httpHandler.doPut(httpsBackEndUrl + "/publisher/api/lifecycle/Approve/" + applicationType + "/" + UUID
                , publisherSession);
        httpHandler.doPut(httpsBackEndUrl + "/publisher/api/lifecycle/Publish/" + applicationType + "/" + UUID
                , publisherSession);
    }

    private void subscribeApplication(AppCreateRequest appCreateRequest) throws IOException {
        httpHandler.doPostHttps(httpsBackEndUrl + "/store/resources/webapp/v1/subscription/app",
                "apiName=" + appCreateRequest.getOverview_name() + "" +
                        "&apiVersion=" + appCreateRequest.getOverview_version() + "&apiTier=" +
                        appCreateRequest.getOverview_tier()
                        + "&subscriptionType=INDIVIDUAL&apiProvider=" + currentUserName + "&appName=DefaultApplication"
                , storeSession, "application/x-www-form-urlencoded; charset=UTF-8");
    }

    private String createMobielAppliaction(MobileApplicationBean mobileApplicationBean, String publisherSession) throws IOException, InterruptedException {
        return httpHandler.doPostMultiData(httpsBackEndUrl + "/publisher/api/asset/mobileapp", "none",
                mobileApplicationBean, publisherSession);
    }

    public void accsesWebPages(String webContext, String trackingCode, int hitCount, String ipAddress) {
        String loginHtmlPage = null;
        String webAppurl = "http://" + ipAddress + ":8280" + webContext + "/1.0.0/";
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
                httpHandler.doGet("http://" + ipAddress + ":8280/statistics/",
                        trackingCode, appmSamlSsoTokenId, webAppurl);
                log.info("Web Page : " + webAppurl + " Hit count : " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

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

}
