package org.wso2.carbon.appmgt.mobile.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appmgt.mobile.mdm.MDMOperations;
import org.wso2.carbon.appmgt.mobile.mdm.MDMServiceReferenceHolder;
import org.wso2.carbon.appmgt.mobile.utils.MobileConfigurations;

/**
 * @scr.component name="org.wso2.carbon.appmgt.mobile" immediate="true"
 * @scr.reference name="mdm.service"
 * interface="org.wso2.carbon.appmgt.mobile.mdm.MDMOperations" cardinality="1..n"
 * policy="dynamic" bind="setMDMService" unbind="unsetMDMService"
 */
public class AppManagerMobileComponent {

    private static final Log log = LogFactory.getLog(AppManagerMobileComponent.class);
    private static final String MDM_OPERATIONS_CLASS = "MDMOperations";

    protected void activate(ComponentContext context) {
        log.debug("App Manger mobile component activated");
    }

    protected void deactivate(ComponentContext context) {
        log.debug("App Manger mobile components deactivated");
    }

    protected void setMDMService(MDMOperations operations) {
        if((MobileConfigurations.getInstance().getActiveMDMBundle() + "." + MDM_OPERATIONS_CLASS).equals(operations.getClass().getName())){
            MDMServiceReferenceHolder.getInstance().setMDMOperation(operations);
            log.info(MobileConfigurations.getInstance().getActiveMDM() + " MDM is bound to App Manager");
        }

    }

    protected void unsetMDMService(MDMOperations operations) {
        log.debug("App Manger MDM service is unbound");
    }

}
