package org.wso2.carbon.appmgt.sample.deployer.appcontroller;

import org.wso2.carbon.appmgt.sample.deployer.bean.AppCreateRequest;
import org.wso2.carbon.appmgt.sample.deployer.configuration.Configuration;
import org.wso2.carbon.appmgt.sample.deployer.http.HttpHandler;

import java.io.IOException;

/**
 *  this class is use to subscribe applications
 */
public class ApplicationSubscriber {

    private HttpHandler httpHandler;
    private String httpsBackEndUrl;

    ApplicationSubscriber(){
        httpHandler =  new HttpHandler();
        httpsBackEndUrl = Configuration.getHttpsUrl();

    }

    /**
     * This method is use for suscribe web application
     *
     * @param appCreateRequest
     *           Bean of the web application
     *
     * @param storeSession
     *           Bean of the web application
     *
     * @throws java.io.IOException
     *           Throws this when failed to suscribe web application
     */
     public void subscribeApplication(AppCreateRequest appCreateRequest,String storeSession,String currentUserName)
             throws IOException {
        httpHandler.doPostHttps(httpsBackEndUrl + "/store/resources/webapp/v1/subscription/app",
                "apiName=" + appCreateRequest.getOverview_name() + "" +
                        "&apiVersion=" + appCreateRequest.getOverview_version() + "&apiTier=" +
                        appCreateRequest.getOverview_tier()
                        + "&subscriptionType=INDIVIDUAL&apiProvider=" + currentUserName + "&appName=DefaultApplication"
                , storeSession, "application/x-www-form-urlencoded; charset=UTF-8");
    }
}
