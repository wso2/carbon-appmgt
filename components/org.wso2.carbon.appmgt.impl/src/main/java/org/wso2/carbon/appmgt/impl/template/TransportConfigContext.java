package org.wso2.carbon.appmgt.impl.template;

import org.apache.velocity.VelocityContext;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.apache.axis2.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Set the transport config context
 */
public class TransportConfigContext extends ConfigContextDecorator {

    private WebApp api;

    public TransportConfigContext(ConfigContext context, WebApp api) {
        super(context);
        this.api = api;
    }

    @Override
    public void validate() throws APITemplateException, AppManagementException {
        super.validate();
    }

    @Override
    public VelocityContext getContext() {
        VelocityContext context = super.getContext();
        if (api.getTransports().contains(",")) {
            List<String> transports = new ArrayList<String>(Arrays.asList(api.getTransports().split(",")));
            if(transports.contains(Constants.TRANSPORT_HTTP) && transports.contains(Constants.TRANSPORT_HTTPS)){
                context.put("transport","");
            }
        }else{
            context.put("transport",api.getTransports());
        }

        return context;
    }
}
