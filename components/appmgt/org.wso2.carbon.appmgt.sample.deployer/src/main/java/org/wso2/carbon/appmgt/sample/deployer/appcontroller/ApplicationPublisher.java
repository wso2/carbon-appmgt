package org.wso2.carbon.appmgt.sample.deployer.appcontroller;

import org.wso2.carbon.appmgt.sample.deployer.configuration.Configuration;
import org.wso2.carbon.appmgt.sample.deployer.http.HttpHandler;

import java.io.IOException;

/**
 * This class is use to publish applications.
 */
public class ApplicationPublisher {

    private HttpHandler httpHandler;
    private String httpsBackEndUrl;


    public  ApplicationPublisher(){
        httpHandler = new HttpHandler();
        httpsBackEndUrl = Configuration.getHttpsUrl();
    }

    /**
     * This method is use for publish web application or mobile application
     *
     * @param applicationType
     *           type of the application whether mobile application or web application
     * @param UUID
     *           UUID of the application
     *
     * @throws java.io.IOException
     *           Throws this when failed to create web application
     *
     */
    public void publishApplication(String applicationType, String UUID,String adminPublisherSession
                                    ) throws IOException {
        httpHandler.doPut(httpsBackEndUrl + "/publisher/api/lifecycle/Submit%20for%20Review/" + applicationType + "/"
                + UUID
                , adminPublisherSession);
        httpHandler.doPut(httpsBackEndUrl + "/publisher/api/lifecycle/Approve/" + applicationType + "/" + UUID
                , adminPublisherSession);
        httpHandler.doPut(httpsBackEndUrl + "/publisher/api/lifecycle/Publish/" + applicationType + "/" + UUID
                , adminPublisherSession);
    }

}
