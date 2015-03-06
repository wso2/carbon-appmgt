package org.wso2.carbon.appmgt.mobile.services.api.v1;

import org.wso2.carbon.appmgt.mobile.services.api.v1.apps.App;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;

/**
 * @scr.component name="org.wso2.carbon.registry.samples.statistics" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */



@Produces({ "application/json"})
@Consumes({ "application/json"})
public class MobileAppService {


        @GET
        public AppListResponse getApplicationList(@QueryParam("tenantId") int tenantId, @QueryParam("limit") int limit, @QueryParam("offset") int offset){

            AppListResponse response= new AppListResponse();
            response.setApps(new ArrayList<App>());

            App app1 = new App();
            app1.setPackageName("Hello");
            response.getApps().add(app1);

            App app2 = new App();
            app2.setAppIdentifier("Hello3");
            response.getApps().add(app2);

            AppListQuery appListQuery = new AppListQuery();
            appListQuery.setStatus("OK");
            appListQuery.setLimit(limit);
            appListQuery.setOffset(offset);
            response.setQuery(appListQuery);

            return response;
        }

}
