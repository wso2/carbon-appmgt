package org.wso2.carbon.appmgt.api.model.entitlement;

/**
 * Created by rushmin on 7/29/14.
 */
public class EntitlementPolicy {

    private String policyId;
    private String policyContent;

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicyContent() {
        return policyContent;
    }

    public void setPolicyContent(String policyContent) {
        this.policyContent = policyContent;
    }

    public boolean isValid() { // TODO : Add proper validation.
        return policyContent != null && !policyContent.isEmpty();
    }
}
