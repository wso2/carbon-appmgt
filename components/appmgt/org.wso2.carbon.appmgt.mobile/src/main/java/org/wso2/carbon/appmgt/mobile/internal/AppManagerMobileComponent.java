package org.wso2.carbon.appmgt.mobile.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appmgt.mobile.mdm.MDMOperations;
import org.wso2.carbon.appmgt.mobile.mdm.MDMServiceReferenceHolder;

/**
 * @scr.component name="org.wso2.carbon.appmgt.mobile" immediate="true"
 * @scr.reference name="mdm.service"
 * interface="org.wso2.carbon.appmgt.mobile.mdm.MDMOperations" cardinality="1..1"
 * policy="dynamic" bind="setMDMService" unbind="unsetMDMService"
 */
public class AppManagerMobileComponent {

    private static final Log log = LogFactory.getLog(AppManagerMobileComponent.class);

    protected void activate(ComponentContext context) {
        log.debug("App Manger mobile component activated");
    }

    protected void deactivate(ComponentContext context) {
        log.debug("App Manger mobile components deactivated");
    }

    protected void setMDMService(MDMOperations operations) {
        MDMServiceReferenceHolder.getInstance().setMDMOperation(operations);
        log.debug("App Manger MDM service is bound");
    }

    protected void unsetMDMService(MDMOperations operations) {
        log.debug("App Manger MDM service is unbound");
    }

}
