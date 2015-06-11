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

import net.minidev.json.JSONArray;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.sample.deployer.appm.WSRegistryServiceClient;
import org.wso2.carbon.appmgt.sample.deployer.bean.AppCreateRequest;
import org.wso2.carbon.appmgt.sample.deployer.bean.WebAppDetail;
import org.wso2.carbon.appmgt.sample.deployer.configuration.Configuration;
import org.wso2.carbon.appmgt.sample.deployer.http.HttpHandler;
import org.wso2.carbon.appmgt.sample.deployer.javascriptwrite.InvokeStatistcsJavascriptBuilder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is use to create,publish and subscribe web applications.
 * This also create and publish mobile applications.
 */
public class ProxyApplicationCreator {

    final static Logger log = Logger.getLogger(ProxyApplicationCreator.class.getName());
    private static String appmHomePath = CarbonUtils.getCarbonHome();
    private HttpHandler httpHandler;
    private String httpsBackEndUrl;
    private String httpBackEndUrl;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private ConcurrentHashMap<String, String> trackingCodes;
    private InvokeStatistcsJavascriptBuilder invokeStatistcsJavascriptBuilder;
    private String ipAddress = "localhost";
    private Random random;
    private String adminPublisherSession;
    private ApplicationPublisher applicationPublisher;
    private ApplicationSubscriber applicationSubscriber;

    /**
     * Creates a new ProxyApplicationCreator object and initialising objects,attributes
     *
     * @throws AppManagementException Throws this when failed to initialise the ip address
     *                                Throws this when failed to initialise the WSRegistryServiceClient
     */
    public ProxyApplicationCreator() throws AppManagementException {
        this.ipAddress = Configuration.getIPAddress();
        httpsBackEndUrl = Configuration.getHttpsUrl();
        httpBackEndUrl = Configuration.getHttpUrl();
        httpHandler = new HttpHandler();
        trackingCodes = new ConcurrentHashMap<String, String>();
        applicationPublisher = new ApplicationPublisher();
        applicationSubscriber = new ApplicationSubscriber();
        random = new Random();
        String errorMessage = "Error while creating a WSRegistryServiceClient";
        try {
            wsRegistryServiceClient = new WSRegistryServiceClient(httpsBackEndUrl);
        } catch (RegistryException e) {
            log.error(errorMessage, e);
            throw new AppManagementException(errorMessage, e);
        } catch (AxisFault axisFault) {
            log.error(errorMessage, axisFault);
            throw new AppManagementException(errorMessage, axisFault);
        }
    }

    /**
     * This method is use for create,publish and subscribe given web application
     *
     * @param webAppDetail Bean object of the web application
     * @throws AppManagementException Throws this when failed to add an user
     *                                Throws this when store session is failed while requesting
     *                                Throws this when policy id is failed while requesting
     *                                Throws this when failed to create,publish or subscribe web application
     */
    public void createAndPublishWebApplication(WebAppDetail webAppDetail) throws AppManagementException {
        String currentUserName = webAppDetail.getUserName();
        String creatorSession = webAppDetail.getCreatorSession();
        String storeSession = webAppDetail.getStoreSession();
        String java_policyId = AppMDAO.getDisplayOrderSeqNo();
        webAppDetail.setContext(generateWebAppContext(webAppDetail.getContext()));
        if (currentUserName.equals("admin")) {
            adminPublisherSession = webAppDetail.getCreatorSession();
        } else {
            try {
                adminPublisherSession = httpHandler.doPostHttps(httpsBackEndUrl + "/publisher/api/authenticate",
                        "username=" + Configuration.getUserName() + "&password=" + Configuration.getPassword() +
                                "&action=login", ""
                        , "application/x-www-form-urlencoded");
            } catch (IOException e) {
                String errorMessage = "Error while requesting publisher session";
                log.error(errorMessage, e);
                throw new AppManagementException(errorMessage, e);
            }
        }

        String policyIDResponce = null;
        try {
            policyIDResponce = httpHandler.doPostHttps(httpsBackEndUrl + "/publisher/api/entitlement/policy/partial" +
                            "/policyGroup/save", "anonymousAccessToUrlPattern=false&policyGroupName" +
                            "=samples&throttlingTier=Unlimited&objPartialMappings=[]&policyGroupDesc=null&userRoles=",
                    creatorSession, "application/x-www-form-urlencoded; charset=UTF-8").split(":")[3];
        } catch (IOException e) {
            log.error("Error while requesting a policy id", e);
            throw new AppManagementException("Error while requesting a policy id", e);
        }
        String policyId = policyIDResponce.substring(1, (policyIDResponce.length() - 2)).trim();
        AppCreateRequest appCreateRequest = new AppCreateRequest();
        appCreateRequest.setUritemplate_policyGroupIds("[" + policyId + "]");
        appCreateRequest.setUritemplate_policyGroupId4(policyId);
        appCreateRequest.setUritemplate_policyGroupId3(policyId);
        appCreateRequest.setUritemplate_policyGroupId2(policyId);
        appCreateRequest.setUritemplate_policyGroupId1(policyId);
        appCreateRequest.setUritemplate_policyGroupId0(policyId);
        appCreateRequest.setOverview_provider(currentUserName);
        appCreateRequest.setUritemplate_javaPolicyIds("[" + java_policyId + "]");
        appCreateRequest.setClaimPropertyCounter(webAppDetail.getClaims().size() + "");
        appCreateRequest.setOverview_name(webAppDetail.getWebAppName());
        appCreateRequest.setOverview_displayName(webAppDetail.getDisplayName());
        appCreateRequest.setOverview_context(webAppDetail.getContext());
        appCreateRequest.setOverview_version(webAppDetail.getVersion());
        appCreateRequest.setOverview_trackingCode(appCreateRequest.generateTrackingID());
        appCreateRequest.setOverview_transports("http");
        appCreateRequest.setOverview_webAppUrl(httpBackEndUrl + "/" + webAppDetail.getWarFileName() + "/");
        String UUID = null;
        String errorMessage = "Error while creating a web application" + webAppDetail.getDisplayName();
        try {
            UUID = createWebApplication(appCreateRequest.generateRequestParameters(), webAppDetail);
        } catch (IOException e) {
            log.error(errorMessage, e);
            throw new AppManagementException(errorMessage, e);
        } catch (RegistryException e) {
            log.error(errorMessage, e);
            throw new AppManagementException(errorMessage, e);
        } catch (InterruptedException e) {
            log.error(errorMessage, e);
            throw new AppManagementException(errorMessage, e);
        }
        try {
            applicationPublisher.publishApplication("webapp", UUID, adminPublisherSession);
        } catch (IOException e) {
            String publishingErrorMessage = "Error while publishing a web application "
                    + webAppDetail.getDisplayName();
            log.error(publishingErrorMessage, e);
            throw new AppManagementException(publishingErrorMessage, e);
        }
        log.info(appCreateRequest.getOverview_name() + " published and UUID is " + UUID);
        try {
            applicationSubscriber.subscribeApplication(appCreateRequest, storeSession,
                    currentUserName);
        } catch (IOException e) {
            String subscribingErrorMessage = "Error while subscribing a web application " + webAppDetail.getDisplayName();
            log.error(subscribingErrorMessage, e);
            throw new AppManagementException(subscribingErrorMessage, e);
        }
        log.info(appCreateRequest.getOverview_name() + "application subscribed by subsciber_" + currentUserName);
    }

    /**
     * This method is use for create web application
     *
     * @param payload      request payload for web app create api
     * @param webAppDetail bean object of the web application
     * @throws IOException                    Throws this when failed to create web application
     * @throws RegistryException              Throws this when UUID failed while requesting
     * @throws AppManagementException         Throws this when gateway port failed while retrieving
     * @throws java.lang.InterruptedException Throws this when thread failed
     */
    private String createWebApplication(String payload, WebAppDetail webAppDetail)
            throws IOException, RegistryException, AppManagementException, InterruptedException {
        String currentUserName = webAppDetail.getUserName();
        String creatorSession = webAppDetail.getCreatorSession();
        httpHandler.doPostHttps(httpsBackEndUrl + "/publisher/asset/webapp", payload
                , creatorSession, "application/x-www-form-urlencoded");

        JSONArray claimsAry = new JSONArray();
        ConcurrentHashMap<String, String[]> claimMap = webAppDetail.getClaims();
        Iterator entries = claimMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry thisEntry = (Map.Entry) entries.next();
            String claimName = ((String[]) thisEntry.getValue())[0];
            claimsAry.add(claimName);
        }
        JSONObject configJson = new JSONObject();
        configJson.put("provider", "wso2is-5.0.0");//use a constant
        configJson.put("logout_url", "");
        configJson.put("claims", claimsAry);
        configJson.put("app_name", webAppDetail.getWebAppName());
        configJson.put("app_verison", webAppDetail.getVersion());
        configJson.put("app_transport", "http");
        configJson.put("app_context", webAppDetail.getContext());
        configJson.put("app_provider", webAppDetail.getUserName());
        configJson.put("app_allowAnonymous", "false");
        httpHandler.doPostHttps(httpsBackEndUrl + "/publisher/api/sso/addConfig", configJson.toJSONString(),
                creatorSession, "application/json; charset=UTF-8");
        String appPath = "/_system/governance/appmgt/applicationdata/provider/"
                + currentUserName + "/" +
                webAppDetail.getWebAppName() + "/1.0.0/webapp";
        String UUID = wsRegistryServiceClient.getUUID(appPath);
        String trackingIDResponse = httpHandler.doGet(httpsBackEndUrl + "/publisher/api/asset/webapp/trackingid/" + UUID
                , "", creatorSession, "").split(":")[1].trim();
        String trackingID = trackingIDResponse.substring(1, (trackingIDResponse.length() - 2));
        webAppDetail.setTrackingCode(trackingID);
        invokeStatistcsJavascriptBuilder = new InvokeStatistcsJavascriptBuilder
                (trackingID, ipAddress, Configuration.getGatewayPort("http"));
        if (webAppDetail.getWebAppName().equals("PlanYourTrip_" + currentUserName)) {
            invokeStatistcsJavascriptBuilder.buildInvokeStaticsJavascriptFile(appmHomePath +
                    "/repository/deployment/server/webapps/plan-your-trip-1.0");
        } else if (webAppDetail.getWebAppName().equals("TravelBooking_" + currentUserName)) {
            invokeStatistcsJavascriptBuilder.buildInvokeStaticsJavascriptFile(appmHomePath +
                    "/repository/deployment/server/webapps/travel-booking-1.0/js");
        }
        log.info(webAppDetail.getWebAppName() + " created and UUID is : " + UUID);
        return UUID;
    }

    /**
     * This method is use to check the availabilty of given context and if it is available generate new context
     *
     * @param context Page name of the web application
     * @return generated or given context
     */
    private String generateWebAppContext(String context) {
        if (!AppMDAO.isContextExist(context)) {
            return context;
        } else {
            context += (random.nextInt(10 - 0 + 1));
            return generateWebAppContext(context);
        }


    }

}
