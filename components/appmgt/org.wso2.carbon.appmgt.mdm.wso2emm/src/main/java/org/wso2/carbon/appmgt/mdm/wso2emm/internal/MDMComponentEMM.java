package org.wso2.carbon.appmgt.mdm.wso2emm.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appmgt.mdm.wso2emm.WSO2EMMMDMOperations;
import org.wso2.carbon.appmgt.mobile.interfaces.MDMOperations;

/**
 * @scr.component name="org.wso2.carbon.appmgt.mdm.wso2emm.WSO2EMMMDMComponent" immediate="true"
 */

public class MDMComponentEMM {

    private static final Log log = LogFactory.getLog(MDMComponentEMM.class);

    private ServiceRegistration mdmServiceRegistration;

    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        mdmServiceRegistration = bundleContext.registerService(MDMOperations.class.getName(), new WSO2EMMMDMOperations(), null);
        log.debug("WSO2 EMM Service Registration activated");
    }

    protected void deactivate(ComponentContext context) {
        if (mdmServiceRegistration != null) {
            mdmServiceRegistration.unregister();
            mdmServiceRegistration = null;
        }
        log.debug("WSO2 EMM Service Registration deactivated");
    }
}


