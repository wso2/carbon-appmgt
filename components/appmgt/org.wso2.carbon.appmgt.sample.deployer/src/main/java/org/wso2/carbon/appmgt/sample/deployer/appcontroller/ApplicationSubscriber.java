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
