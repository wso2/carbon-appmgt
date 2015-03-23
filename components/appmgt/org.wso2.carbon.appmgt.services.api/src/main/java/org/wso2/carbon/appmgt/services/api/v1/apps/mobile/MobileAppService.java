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

package org.wso2.carbon.appmgt.services.api.v1.apps.mobile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.services.api.v1.apps.common.*;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import waffle.util.Base64;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Produces({ "application/json"})
@Consumes({ "application/json"})
public class MobileAppService implements AppService {

        private static final Log log = LogFactory.getLog(MobileAppService.class);

        private static int SUPER_USER_TENANT_ID = -1234;

        @GET
        @Path("list/tenant/{tenantDomain}")
        public AppListResponse getApplicationList(@Context final HttpServletResponse servletResponse, @Context HttpHeaders headers, @PathParam("tenantDomain") String tenantDomain, @QueryParam("limit") int limit, @QueryParam("offset") int offset){

            boolean noLimit = false;

            int pageIndex = 0;
            int index = 0;
            int found = 0;

            if(tenantDomain == null ) tenantDomain = "carbon.super";
            if(limit == 0) noLimit = true;

            log.debug("getApplicationList: Tenant domain is " + tenantDomain);

            AppListResponse response= new AppListResponse();

            try {

                List<String> authorization = headers.getRequestHeader("Authorization");
                if(authorization != null && authorization.size() != 0){
                   String basicHeader = authorization.get(0);
                    String base64Credentials = basicHeader.substring("Basic".length()).trim();
                    String credentialsString = new String(Base64.decode(base64Credentials), Charset.forName("UTF-8"));
                    final String[] credentials = credentialsString.split(":",2);
                    if(credentials.length < 2){
                        throw new UnauthorizedUserException();
                    }

                    RealmService realmService = (RealmService)PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(RealmService.class);
                    RegistryService registryService  = (RegistryService) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(RegistryService.class);
                    UserStoreManager userStoreManager = (UserStoreManager) realmService.getTenantUserRealm(SUPER_USER_TENANT_ID).getUserStoreManager();


                    String[] userList = userStoreManager.getRoleListOfUser(credentials[0]);
                    String authorizedRole = ServicesApiConfigurations.getInstance().getAuthorizedRole();
                    if(!Arrays.asList(userList).contains(authorizedRole)){
                        throw new UnauthorizedUserException();
                    }

                    boolean isAuthenticated = userStoreManager.authenticate( MultitenantUtils.getTenantAwareUsername(credentials[0]), credentials[1]);

                    if(!isAuthenticated){
                        throw new UnauthorizedUserException();
                    }
                }else{
                        throw new UnauthorizedUserException();
                }

                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration().getAdminUserName());


                Registry registry = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_GOVERNANCE);

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

                    response.getApps().add(MobileAppDataLoader.load(new MobileApp(), artifact));
                }

                AppListQuery appListQuery = new AppListQuery();
                appListQuery.setLimit(limit);
                appListQuery.setFound(found);
                appListQuery.setOffset(offset);
                appListQuery.setTotal(artifacts.length);
                response.setQuery(appListQuery);

            } catch (GovernanceException e) {
                log.error("GovernanceException occurred");
                log.debug("Error: " + e);
            } catch (UnauthorizedUserException e) {
                log.error("User is not authorized to access the API");
                log.debug("Error: " + e);
                servletResponse.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
            } catch (UserStoreException e) {
                log.error("UserStoreException occurred");
                log.debug("Error: " + e);
            } catch (RegistryException e) {
                log.error("RegistryException occurred");
                log.debug("Error: " + e);
            }finally{
                PrivilegedCarbonContext.endTenantFlow();
                return response;
            }

        }


}
