package org.wso2.carbon.appmgt.mdm.wso2mdm.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appmgt.mdm.wso2mdm.WSO2MDMOperations;
import org.wso2.carbon.appmgt.mobile.interfaces.MDMOperations;

/**
 * @scr.component name="org.wso2.carbon.appmgt.mdm.wso2mdm.WSO2MDMComponent" immediate="true"
 */

public class MDMComponent {

    private static final Log log = LogFactory.getLog(MDMComponent.class);

    private ServiceRegistration mdmServiceRegistration;

    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        mdmServiceRegistration = bundleContext.registerService(MDMOperations.class.getName(), new WSO2MDMOperations(), null);
        log.debug("WSO2 MDM Service Registration activated");
    }

    protected void deactivate(ComponentContext context) {
        if (mdmServiceRegistration != null) {
            mdmServiceRegistration.unregister();
            mdmServiceRegistration = null;
        }
        log.debug("WSO2 MDM Service Registration deactivated");
    }
}


