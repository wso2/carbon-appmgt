package org.wso2.carbon.appmgt.mobile.utils;

/**
 * Created by dilan on 3/20/15.
 */
public class User {

    private String username;
    private int tenantId;
    private String tenantDomain;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
}
