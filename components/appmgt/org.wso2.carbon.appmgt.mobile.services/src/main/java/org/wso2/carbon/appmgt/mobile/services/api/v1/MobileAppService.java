/*
 *
 *   Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.appmgt.mobile.services.api.v1;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;

@Produces({ "application/json"})
@Consumes({ "application/json"})
public class MobileAppService {

        private static final Log log = LogFactory.getLog(MobileAppService.class);

        @GET
        public AppListResponse getApplicationList(@QueryParam("tenantId") int tenantId, @QueryParam("limit") int limit, @QueryParam("offset") int offset){

            boolean noLimit = false;

            int pageIndex = 0;
            int index = 0;
            int found = 0;

            if(tenantId == 0) tenantId = -1234;
            if(limit == 0) noLimit = true;

            log.debug("getApplicationList: Tenant id is " + tenantId);

            AppListResponse response= new AppListResponse();

            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration().getAdminUserName());

                CarbonContext cCtx = CarbonContext.getThreadLocalCarbonContext();
                Registry registry = cCtx.getRegistry(RegistryType.USER_GOVERNANCE);

                GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
                GenericArtifactManager artifactManager = new GenericArtifactManager((UserRegistry)registry, "mobileapp");
                GenericArtifact[] artifacts = artifactManager.getAllGenericArtifactsByLifecycleStatus("MobileAppLifeCycle", "Published");

                response.setApps(new ArrayList<App>());

                for(GenericArtifact artifact : artifacts){

                    //  Pagination Logic
                    if(offset > index++){
                        continue;
                    }
                    if(!noLimit) {
                        if(pageIndex == limit){
                            break;
                        }
                    }
                    found = ++pageIndex;

                    response.getApps().add(AppDataLoader.load(new App(), artifact));
                }

                AppListQuery appListQuery = new AppListQuery();
                appListQuery.setStatus("OK");
                appListQuery.setLimit(limit);
                appListQuery.setFound(found);
                appListQuery.setOffset(offset);
                appListQuery.setTotal(artifacts.length);
                response.setQuery(appListQuery);



            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                PrivilegedCarbonContext.endTenantFlow();
                return response;
            }

        }


}
