package org.wso2.carbon.appmgt.sample.deployer.appcontroller;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.sample.deployer.configuration.Configuration;
import org.wso2.carbon.appmgt.sample.deployer.http.HttpHandler;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * This class is use to accses web pages in sample web pages
 */
public class WebpageAccessor {

    final static Logger log = Logger.getLogger(WebpageAccessor.class.getName());

    /**
     * This method is use for accses a web application according to user given hit count
     *
     * @param webContext
     *            Context of the web application
     *
     * @param trackingCode
     *            Tracking code of the web application
     *
     * @param hitCount
     *            Hit count for web application
     *
     * @param ipAddress
     *            IP address of the user mashine
     *
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     *            Throws this when failed to accses web application
     *            Throws this when thread failed to sleep
     */
    public static void accsesWebPages(String webContext, String trackingCode, int hitCount, String ipAddress)
            throws AppManagementException {
        HttpHandler httpHandler = new HttpHandler();
        String httpsBackEndUrl =Configuration.getHttpsUrl();
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
     *            Page name of the web application
     *
     * @param webAppUrl
     *            Current url of web application
     *
     * @param isAppendLastOne
     *
     *
     */
    private static String appendPageToUrl(String pageName, String webAppUrl, boolean isAppendLastOne) {
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


