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
import org.apache.commons.ssl.Base64;
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
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.*;


@Produces({ "application/json"})
@Consumes({ "application/json"})
public class MobileAppService {

        private static final Log log = LogFactory.getLog(MobileAppService.class);

        private static int SUPER_USER_TENANT_ID = -1234;

        @GET
        @Path("list/tenant/{tenantDomain}")
        public AppListResponse getApplicationList(@Context final HttpServletResponse servletResponse,
                                                  @Context HttpHeaders headers, @PathParam("tenantDomain")
                            String tenantDomain, @QueryParam("limit") int limit, @QueryParam("offset") int offset,
                                                  @QueryParam("platform") String platform){

            boolean noLimit = false;

            int pageIndex = 0;
            int index = 0;
            int found = 0;

            if(tenantDomain == null ) tenantDomain = "carbon.super";
            if(limit == 0) noLimit = true;

            log.debug("getApplicationList: Tenant domain is " + tenantDomain);

            AppListResponse response= new AppListResponse();

            try {

                Registry registry = doAuthorizeAndGetRegistry(tenantDomain, headers);
                int tenantId = ((UserRegistry)registry).getTenantId();

                GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
                GenericArtifactManager artifactManager = new GenericArtifactManager((UserRegistry)registry, "mobileapp");

                Map map = new HashMap();
                if(platform != null){
                    map.put("overview_platform", Arrays.asList(platform));
                }
                map.put("lcState", Arrays.asList("Published"));
                GenericArtifact[] artifacts = artifactManager.findGenericArtifacts(map);

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

                    response.getApps().add(MobileAppDataLoader.load(new MobileApp(), artifact, tenantId , false));
                }

                AppListQuery appListQuery = new AppListQuery();
                appListQuery.setLimit(limit);
                appListQuery.setFound(found);
                appListQuery.setOffset(offset);
                appListQuery.setTotal(artifacts.length);
                response.setQuery(appListQuery);

            } catch (GovernanceException e) {
                String errorMessage = "GovernanceException occurred";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
            } catch (UnauthorizedUserException e) {
                String errorMessage = "User is not authorized to access the API";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
                servletResponse.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
            } catch (UserStoreException e) {
                String errorMessage = "UserStoreException occurred";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
            } catch (RegistryException e) {
                String errorMessage = "RegistryException occurred";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
            }catch (Exception e) {
                String errorMessage = "Exception occurred while getting the app list";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
                servletResponse.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
            }finally{
                PrivilegedCarbonContext.endTenantFlow();
                return response;
            }

        }



        @POST
        @Consumes("application/x-www-form-urlencoded")
        @Path("subscribe/tenant/{tenantDomain}/{type}/{typeId}")
        public MobileApp subscribeResource(@Context final HttpServletResponse servletResponse, @PathParam("type")
        String type,  @PathParam("typeId") String typeId, @PathParam("tenantDomain") String tenantDomain, @Context HttpHeaders headers,
            @FormParam("appId") String appId){

            MobileApp mobileApp = null;
            try {

                Registry registry = doAuthorizeAndGetRegistry(tenantDomain, headers);
                int tenantId = ((UserRegistry)registry).getTenantId();

                GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
                GenericArtifactManager artifactManager = new GenericArtifactManager((UserRegistry)registry, "mobileapp");
                GenericArtifact artifact  = artifactManager.getGenericArtifact(appId);
                mobileApp = MobileAppDataLoader.load(new MobileApp(), artifact, tenantId, true);

                if(mobileApp != null){

                    if("role".equals(type)){
                        UserStoreManager userStoreManager = ((UserRegistry) registry).getUserRealm().getUserStoreManager();
                        String[] users = userStoreManager.getUserListOfRole(typeId);
                        for(String userId : users){
                            subscribeApp(registry, userId, appId);
                            showAppVisibilityToUser(artifact.getPath(),userId,"ALLOW");
                        }
                    }else if("user".equals(type)){
                        subscribeApp(registry, typeId, appId);
                        showAppVisibilityToUser(artifact.getPath(),typeId,"ALLOW");
                    }

                }

            } catch (GovernanceException e) {
                String errorMessage = "GovernanceException occurred";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
            } catch (UnauthorizedUserException e) {
                String errorMessage = "User is not authorized to access the API";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
                servletResponse.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
            } catch (UserStoreException e) {
                String errorMessage = "UserStoreException occurred";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
            } catch (RegistryException e) {
                String errorMessage = "RegistryException occurred";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
            }catch (Exception e) {
                String errorMessage = String.format("Exception occurred while subscribe %s %s to app %", type, typeId, appId );
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
                servletResponse.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
            }finally{
                PrivilegedCarbonContext.endTenantFlow();
                return mobileApp;
            }

        }


        @POST
        @Consumes("application/x-www-form-urlencoded")
        @Path("unsubscribe/tenant/{tenantDomain}/{type}/{typeId}")
        public MobileApp unsubscribeResource(@Context final HttpServletResponse servletResponse,  @PathParam("type")
        String type,  @PathParam("typeId") String typeId, @PathParam("tenantDomain") String tenantDomain, @Context HttpHeaders headers,
                                       @FormParam("appId") String appId){
            MobileApp mobileApp = null;
            try {

                Registry registry = doAuthorizeAndGetRegistry(tenantDomain, headers);
                int tenantId = ((UserRegistry)registry).getTenantId();

                GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
                GenericArtifactManager artifactManager = new GenericArtifactManager((UserRegistry)registry, "mobileapp");
                GenericArtifact artifact  = artifactManager.getGenericArtifact(appId);
                mobileApp = MobileAppDataLoader.load(new MobileApp(), artifact, tenantId, false);

                if(mobileApp != null){
                    if("role".equals(type)){
                        UserStoreManager userStoreManager = ((UserRegistry) registry).getUserRealm().getUserStoreManager();
                        String[] users = userStoreManager.getUserListOfRole(typeId);
                        for(String userId : users){
                            unsubscribeApp(registry, userId, appId);
                            showAppVisibilityToUser(artifact.getPath(),userId,"DENY");
                        }
                    }else if("user".equals(type)){
                        unsubscribeApp(registry, typeId, appId);
                        showAppVisibilityToUser(artifact.getPath(),typeId,"DENY");
                    }
                }

            } catch (GovernanceException e) {
                String errorMessage = "GovernanceException occurred";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
            } catch (UnauthorizedUserException e) {
                String errorMessage = "User is not authorized to access the API";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
                servletResponse.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
            } catch (UserStoreException e) {
                String errorMessage = "UserStoreException occurred";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
            } catch (RegistryException e) {
                String errorMessage = "RegistryException occurred";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
            }catch (Exception e) {
                String errorMessage = String.format("Exception occurred while unsubscribe %s %s to app %", type, typeId, appId );
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
                servletResponse.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
            }finally{
                PrivilegedCarbonContext.endTenantFlow();
                return mobileApp;
            }

        }

        @GET
        @Consumes("application/x-www-form-urlencoded")
        @Path("subscriptions/tenant/{tenantDomain}/{type}/{typeId}")
        public List<MobileApp> getSubscribedApps(@Context final HttpServletResponse servletResponse,  @PathParam("type")
            String type,  @PathParam("typeId") String typeId, @PathParam("tenantDomain") String tenantDomain, @Context HttpHeaders headers){

            List<MobileApp> mobileApps = new ArrayList<MobileApp>();


            try {
                
                Registry registry = doAuthorizeAndGetRegistry(tenantDomain, headers);
                
                int tenantId = ((UserRegistry)registry).getTenantId();

                GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
                GenericArtifactManager artifactManager = new GenericArtifactManager((UserRegistry)registry, "mobileapp");

                    if("role".equals(type)){

                        UserStoreManager userStoreManager = ((UserRegistry) registry).getUserRealm().getUserStoreManager();
                        String[] users = userStoreManager.getUserListOfRole(typeId);
                        for(String userId : users){
                            String path = "users/" + userId + "/subscriptions/mobileapp/";
                            String[] subscriptions = (String[])registry.get(path).getContent();
                            for(String subscription : subscriptions){
                                String appId = subscription.substring(subscription.lastIndexOf('/') + 1);
                                if(!"".equals(appId)){
                                    try {
                                        GenericArtifact artifact  = artifactManager.getGenericArtifact(appId);
                                        if(artifact != null){
                                            mobileApps.add(MobileAppDataLoader.load(new MobileApp(), artifact, tenantId, true));
                                        }
                                    }catch (GovernanceException e){
                                        log.debug("Invalid artifact : " + appId);
                                    }
                                }
                            }
                        }
                    
                    }else if("user".equals(type)){
                        String path = "users/" + typeId + "/subscriptions/mobileapp/";
                        String[] subscriptions = (String[])registry.get(path).getContent();
                        for(String subscription : subscriptions){
                            String appId = subscription.substring(subscription.lastIndexOf('/') + 1);
                            if(!"".equals(appId)){
                                try {
                                    GenericArtifact artifact  = artifactManager.getGenericArtifact(appId);
                                    if(artifact != null){
                                        mobileApps.add(MobileAppDataLoader.load(new MobileApp(), artifact, tenantId, true));
                                    }
                                }catch (GovernanceException e){
                                    log.debug("Invalid artifact : " + appId);
                                }
                            }
                        }
                    }


            } catch (GovernanceException e) {
                String errorMessage = "GovernanceException occurred";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
            } catch (UnauthorizedUserException e) {
                String errorMessage = "User is not authorized to access the API";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
                servletResponse.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
            } catch (UserStoreException e) {
                String errorMessage = "UserStoreException occurred";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
            } catch (RegistryException e) {
                String errorMessage = "RegistryException occurred";
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
            }catch (Exception e) {
                String errorMessage = String.format("Exception occurred while getting subscribe applist from %s %s", type, typeId);
                if(log.isDebugEnabled()){
                    log.error(errorMessage, e);
                }else{
                    log.error(errorMessage);
                }
                servletResponse.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
            }finally{
                PrivilegedCarbonContext.endTenantFlow();

                //remove duplicate and return
                Set<MobileApp> hs = new HashSet<MobileApp>();
                hs.addAll(mobileApps);
                mobileApps.clear();
                mobileApps.addAll(hs);
                return mobileApps;
            }

        }


        private void subscribeApp(Registry registry, String userId, String appId) throws org.wso2.carbon.registry.api.RegistryException {
            String path = "users/" + userId + "/subscriptions/mobileapp/" + appId;
            Resource resource = null;
            try {
                resource = registry.get(path);
            } catch (org.wso2.carbon.registry.api.RegistryException e) {
                log.error("RegistryException occurred");
                log.debug("Error: " + e);
            }
            if(resource == null){
                resource = registry.newResource();
                resource.setContent("");
                registry.put(path, resource);
            }
        }


        private void unsubscribeApp(Registry registry, String userId, String appId) throws org.wso2.carbon.registry.api.RegistryException {
            String path = "users/" + userId + "/subscriptions/mobileapp/" + appId;
            registry.delete(path);
        }


        private Registry doAuthorizeAndGetRegistry(String tenantDomain, HttpHeaders headers) throws UnauthorizedUserException, UserStoreException {
            List<String> authorization = headers.getRequestHeader("Authorization");
            if(authorization != null && authorization.size() != 0){
                String basicHeader = authorization.get(0);
                String base64Credentials = basicHeader.substring("Basic".length()).trim();
                String credentialsString = new String(Base64.decodeBase64(base64Credentials.getBytes()));
                final String[] credentials = credentialsString.split(":",2);
                if(credentials.length < 2){
                    throw new UnauthorizedUserException();
                }

                RealmService realmService = (RealmService)PrivilegedCarbonContext
                        .getThreadLocalCarbonContext().getOSGiService(RealmService.class);
                RegistryService registryService  = (RegistryService) PrivilegedCarbonContext
                        .getThreadLocalCarbonContext().getOSGiService(RegistryService.class);
                UserStoreManager userStoreManager = (UserStoreManager) realmService
                        .getTenantUserRealm(SUPER_USER_TENANT_ID).getUserStoreManager();


                String[] userList = userStoreManager.getRoleListOfUser(credentials[0]);
                String authorizedRole = ServicesApiConfigurations.getInstance().getAuthorizedRole();
                if(!Arrays.asList(userList).contains(authorizedRole)){
                    throw new UnauthorizedUserException();
                }

                boolean isAuthenticated = userStoreManager
                        .authenticate( MultitenantUtils.getTenantAwareUsername(credentials[0]), credentials[1]);

                if(!isAuthenticated){
                    throw new UnauthorizedUserException();
                }
            }else{
                throw new UnauthorizedUserException();
            }

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setUsername(PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .getUserRealm().getRealmConfiguration().getAdminUserName());



            return CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_GOVERNANCE);
        }



    private boolean showAppVisibilityToUser(String appPath, String username, String opType){


        String userRole = "Internal/private_" + username;

        try {
            if("ALLOW".equalsIgnoreCase(opType)) {
                org.wso2.carbon.user.api.UserRealm realm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
                realm.getAuthorizationManager().authorizeRole(userRole, appPath, ActionConstants.GET);
                return true;
            }else if("DENY".equalsIgnoreCase(opType)){
                org.wso2.carbon.user.api.UserRealm realm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
                realm.getAuthorizationManager().denyRole(userRole, appPath, ActionConstants.GET);
                return true;
            }
            return false;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while updating visibility of mobile app at " + appPath, e);
            return false;
        }
    }


}
