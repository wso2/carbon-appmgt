package org.wso2.carbon.appmgt.api.model;

/**
 * Represents the authenticated IDP, after an authentication
 */
public class AuthenticatedIDP {

    private String idpName;
    private String identity;

    public String getIdpName() {
        return idpName;
    }

    public void setIdpName(String idpName) {
        this.idpName = idpName;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }
}
