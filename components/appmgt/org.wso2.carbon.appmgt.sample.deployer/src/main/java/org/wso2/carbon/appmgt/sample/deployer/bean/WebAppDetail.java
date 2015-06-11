package org.wso2.carbon.appmgt.sample.deployer.bean;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is use to store web application details
 */
public class WebAppDetail {

    private String userName;
    private String creatorSession;
    private String storeSession;
    private ConcurrentHashMap<String, String[]> claims;
    private String context;
    private String webAppName;
    private Object[][] webPagesurl;
    private String version;
    private String displayName;
    private String warFileName;
    private String trackingCode;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCreatorSession() {
        return creatorSession;
    }

    public void setCreatorSession(String creatorSession) {
        this.creatorSession = creatorSession;
    }

    public String getStoreSession() {
        return storeSession;
    }

    public void setStoreSession(String storeSession) {
        this.storeSession = storeSession;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getWebAppName() {
        return webAppName;
    }

    public void setWebAppName(String webAppName) {
        this.webAppName = webAppName;
    }

    public Object[][] getWebPagesurl() {
        return webPagesurl;
    }

    public void setWebPagesurl(Object[][] webPagesurl) {
        this.webPagesurl = webPagesurl;
    }

    public ConcurrentHashMap<String, String[]> getClaims() {
        return claims;
    }

    public void setClaims(ConcurrentHashMap<String, String[]> claims) {
        this.claims = claims;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getWarFileName() {
        return warFileName;
    }

    public void setWarFileName(String warFileName) {
        this.warFileName = warFileName;
    }


    public String getTrackingCode() {
        return trackingCode;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }
}
