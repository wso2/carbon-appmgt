/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.hostobjects;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.appmgt.api.APIConsumer;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APIIdentifier;
import org.wso2.carbon.appmgt.api.model.APIKey;
import org.wso2.carbon.appmgt.api.model.APIRating;
import org.wso2.carbon.appmgt.api.model.Application;
import org.wso2.carbon.appmgt.api.model.Comment;
import org.wso2.carbon.appmgt.api.model.Documentation;
import org.wso2.carbon.appmgt.api.model.DocumentationType;
import org.wso2.carbon.appmgt.api.model.SubscribedAPI;
import org.wso2.carbon.appmgt.api.model.Subscriber;
import org.wso2.carbon.appmgt.api.model.Subscription;
import org.wso2.carbon.appmgt.api.model.Tag;
import org.wso2.carbon.appmgt.api.model.Tier;
import org.wso2.carbon.appmgt.api.model.URITemplate;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.hostobjects.internal.HostObjectComponent;
import org.wso2.carbon.appmgt.hostobjects.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.APIManagerFactory;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.UserAwareAPIConsumer;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.impl.dto.Environment;
import org.wso2.carbon.appmgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.appmgt.impl.idp.TrustedIdP;
import org.wso2.carbon.appmgt.impl.idp.WebAppIdPFactory;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.appmgt.impl.workflow.WorkflowException;
import org.wso2.carbon.appmgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.appmgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.appmgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.appmgt.usage.client.APIUsageStatisticsClient;
import org.wso2.carbon.appmgt.usage.client.dto.APIVersionUserUsageDTO;
import org.wso2.carbon.appmgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceStub;
import org.wso2.carbon.identity.user.registration.stub.dto.UserDTO;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;


public class APIStoreHostObject extends ScriptableObject {

    private static final long serialVersionUID = -3169012616750937045L;
    private static final Log log = LogFactory.getLog(APIStoreHostObject.class);
    private static final String hostObjectName = "APIStore";
    private static final String httpPort = "mgt.transport.http.port";
    private static final String httpsPort = "mgt.transport.https.port";
    private static final String hostName = "carbon.local.ip";

    private APIConsumer apiConsumer;

    private String username;

    public String getUsername() {
        return username;
    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    // The zero-argument constructor used for create instances for runtime
    public APIStoreHostObject() throws AppManagementException {
        //apiConsumer = APIManagerFactory.getInstance().getAPIConsumer();
    }

    public APIStoreHostObject(String loggedUser) throws AppManagementException {
    	if (loggedUser != null) {
    		this.username = loggedUser;
    		apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
    	} else {
    		apiConsumer = APIManagerFactory.getInstance().getAPIConsumer();
    	}
    }

    public static void jsFunction_loadRegistryOfTenant(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) {
        if (!isStringArray(args)) {
            return;
        }

        String tenantDomain = args[0].toString();
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                .equals(tenantDomain)) {
            try {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                        getTenantManager().getTenantId(tenantDomain);
                AppManagerUtil.loadTenantRegistry(tenantId);
            } catch (org.wso2.carbon.user.api.UserStoreException | AppManagementException e) {
                log.error(
                        "Could not load tenant registry. Error while getting tenant id from tenant domain "
                                + tenantDomain);
            }
        }

    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function Obj,
                                           boolean inNewExpr)
            throws ScriptException, AppManagementException {

        if (args!=null && args.length != 0) {
            String username = (String) args[0];
            return new APIStoreHostObject(username);
        }
        return new APIStoreHostObject(null);
    }

    private static String getUsernameFromObject(Scriptable obj) {
        return ((APIStoreHostObject) obj).getUsername();
    }

    public APIConsumer getApiConsumer() {
        return apiConsumer;
    }

    private static APIConsumer getAPIConsumer(Scriptable thisObj) {
        return ((APIStoreHostObject) thisObj).getApiConsumer();
    }

    private static void handleException(String msg) throws AppManagementException {
        log.error(msg);
        throw new AppManagementException(msg);
    }

    private static void handleException(String msg, Throwable t) throws AppManagementException {
        log.error(msg, t);
        throw new AppManagementException(msg, t);
    }

    public static String jsFunction_getAuthServerURL(Context cx, Scriptable thisObj,
                                                     Object[] args, Function funObj) throws
                                                                                     AppManagementException {

        AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(AppMConstants.AUTH_MANAGER_URL);
        if (url == null) {
            handleException("WebApp key manager URL unspecified");
        }
        return url;
    }

    /**
     * Get the identity provider URL from app-manager.xml file
     *
     * @param context Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return identity provider URL
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception by org.wso2.carbon.apimgt.api.AppManagementException
     */
    public static String jsFunction_getIdentityProviderUrl(Context context, Scriptable thisObj,
                                                           Object[] args,
                                                           Function funObj) throws AppManagementException {
        AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(AppMConstants.SSO_CONFIGURATION_IDENTITY_PROVIDER_URL);
        if (url == null) {
            handleException("Identity provider URL unspecified");
        }
        return url;
    }
    
    public static String jsFunction_getHTTPsURL(Context cx, Scriptable thisObj,
                                                Object[] args, Function funObj)
            throws AppManagementException {
    	
    	String hostName = null;
    	if (args != null && isStringArray(args)) {
    		hostName = (String)args[0];
    		URI uri;
			try {
				uri = new URI(hostName);
				hostName = uri.getHost();
			} catch (URISyntaxException e) {
				//ignore
			}
    	} 
                
        if (hostName == null) {
        	hostName = CarbonUtils.getServerConfiguration().getFirstProperty("HostName");
        }
        if (hostName == null) {
        	hostName = System.getProperty("carbon.local.ip");
        }
        String backendHttpsPort = HostObjectUtils.getBackendPort("https");
        return "https://" + hostName + ":" + backendHttpsPort;

    }


    public static String jsFunction_getHTTPURL(Context cx, Scriptable thisObj,
                                               Object[] args, Function funObj)
            throws AppManagementException {
        return "http://" + System.getProperty(hostName) + ":" + System.getProperty(httpPort);
    }

    public static NativeObject jsFunction_login(Context cx, Scriptable thisObj,
                                                Object[] args, Function funObj) throws ScriptException,
                                                                                       AppManagementException {
        if (args==null || args.length == 0||!isStringArray(args)) {
            handleException("Invalid input parameters for the login method");
        }

        String username = (String) args[0];
        String password = (String) args[1];

        AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(AppMConstants.AUTH_MANAGER_URL);
        if (url == null) {
            handleException("WebApp key manager URL unspecified");
        }

        NativeObject row = new NativeObject();

        try {
            AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(null, url + "AuthenticationAdmin");
            ServiceClient client = authAdminStub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);

            String host = new URL(url).getHost();
            if (!authAdminStub.login(username, password, host)) {
                handleException("Login failed! Please recheck the username and password and try again.");
            }
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            String tenantDomain = MultitenantUtils.getTenantDomain(username);

            String usernameWithDomain = AppManagerUtil.getLoggedInUserInfo(sessionCookie,url).getUserName();
            usernameWithDomain = AppManagerUtil.setDomainNameToUppercase(usernameWithDomain);

            boolean isSuperTenant = false;
            
            if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            	isSuperTenant = true;
            }else {
                usernameWithDomain = usernameWithDomain + "@"+tenantDomain;
            }

            boolean authorized =
                    AppManagerUtil.checkPermissionQuietly(usernameWithDomain, AppMConstants.Permissions.WEB_APP_SUBSCRIBE);


            if (authorized) {
                row.put("user", row, usernameWithDomain);
                row.put("sessionId", row, sessionCookie);
                row.put("isSuperTenant", row, isSuperTenant);
                row.put("error", row, false);
            } else {
                handleException("Login failed! Insufficient Privileges.");
            }
        } catch (Exception e) {
            row.put("error", row, true);
            row.put("detail", row, e.getMessage());
        }

        return row;
    }

    /**
     * Given a base 64 encoded username:password string,
     * this method checks if said user has enough privileges to advance a workflow.
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws ScriptException
     * @throws WorkflowException
     */
    public static NativeObject jsFunction_validateWFPermission(Context cx, Scriptable thisObj,
                                                Object[] args, Function funObj) throws ScriptException,
                                                                                       AppManagementException {
        if (args==null || args.length == 0||!isStringArray(args)) {
            throw new AppManagementException("Invalid input parameters for authorizing workflow progression.");
        }

        NativeObject row = new NativeObject();

        String reqString = (String) args[0];
        String authType = reqString.split("\\s+")[0];
        String encodedString = reqString.split("\\s+")[1];
        if(!HttpTransportProperties.Authenticator.BASIC.equals(authType)){
            //throw new AppManagementException("Invalid Authorization Header Type");
            row.put("error", row, true);
            row.put("statusCode", row, 401);
            row.put("message", row, "Invalid Authorization Header Type");
            return row;
        }

        byte[] decoded = Base64.decodeBase64(encodedString.getBytes());

        String decodedString = new String(decoded);

        if(decodedString.isEmpty() || !decodedString.contains(":")){
            //throw new AppManagementException("Invalid number of arguments. Please provide a valid username and password.");
            row.put("error", row, true);
            row.put("statusCode", row, 401);
            row.put("message", row, "Invalid Authorization Header Value");
            return row;
        }

        String username = decodedString.split(":")[0];
        String password = decodedString.split(":")[1];

        AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        //String url = config.getFirstProperty(AppMConstants.AUTH_MANAGER_URL);
        //if (url == null) {
        //    throw new AppManagementException("WebApp key manager URL unspecified");
        //}

        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();

            int tenantId=ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(MultitenantUtils.getTenantDomain(username));

            org.wso2.carbon.user.api.UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            Boolean authStatus = userStoreManager.authenticate(username, password);

            if(!authStatus){
                //throw new WorkflowException("Please recheck the username and password and try again.");
                row.put("error", row, true);
                row.put("statusCode", row, 401);
                row.put("message", row, "Authentication Failure. Please recheck username and password");
                return row;
            }

            String tenantDomain = MultitenantUtils.getTenantDomain(username);

            String usernameWithDomain = AppManagerUtil.setDomainNameToUppercase(username);

            if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                usernameWithDomain = usernameWithDomain + "@"+tenantDomain;
            }

            boolean authorized = AppManagerUtil.checkPermissionQuietly(usernameWithDomain, AppMConstants.Permissions.APP_WORKFLOWADMIN);

            if (authorized) {
                row.put("error", row, false);
                row.put("statusCode", row, 200);
                row.put("message", row, "Authorization Successful");
                return row;
            } else {
                //handleException("Login failed! Insufficient Privileges.");
                row.put("error", row, true);
                row.put("statusCode", row, 403);
                row.put("message", row, "Forbidden. User not authorized to perform action");
                return row;
            }
        } catch (Exception e) {
            row.put("error", row, true);
            row.put("statusCode", row, 500);
            row.put("message", row, e.getMessage());
            return row;
        }
    }

    public static NativeArray jsFunction_getTopRatedAPIs(Context cx,
                                                         Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {

        NativeArray myn = new NativeArray(0);
        if (args!=null && isStringArray(args)) {
            String limitArg = args[0].toString();
            int limit = Integer.parseInt(limitArg);
            Set<WebApp> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                apiSet = apiConsumer.getTopRatedAPIs(limit);
            } catch (AppManagementException e) {
                log.error("Error from Registry WebApp while getting Top Rated APIs Information", e);
                return myn;
            } catch (Exception e) {
                log.error("Error while getting Top Rated APIs Information", e);
                return myn;
            }
            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object apiObject = it.next();
                WebApp api = (WebApp) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                row.put("name", row, apiIdentifier.getApiName());
                row.put("provider", row, AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                row.put("version", row, apiIdentifier.getVersion());
                row.put("description", row, api.getDescription());
                row.put("rates", row, api.getRating());
                myn.put(i, myn, row);
                i++;
            }

        }// end of the if
        return myn;
    }

    public static NativeArray jsFunction_getRecentlyAddedAPIs(Context cx,
                                                              Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (args!=null && args.length!=0) {
            String limitArg = args[0].toString();
            int limit = Integer.parseInt(limitArg);
            String requestedTenantDomain=(String)args[1];
            Set<WebApp> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            boolean isTenantFlowStarted = false;
            try {
            	if(requestedTenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(requestedTenantDomain)){
            			isTenantFlowStarted = true;
            			PrivilegedCarbonContext.startTenantFlow();
            			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(requestedTenantDomain, true);
            		}
            	apiSet = apiConsumer.getRecentlyAddedAPIs(limit,requestedTenantDomain);
            } catch (AppManagementException e) {
                log.error("Error from Registry WebApp while getting Recently Added APIs Information", e);
                return apiArray;
            } catch (Exception e) {
                log.error("Error while getting Recently Added APIs Information", e);
                return apiArray;
            } finally {
            	if(isTenantFlowStarted){
            		PrivilegedCarbonContext.endTenantFlow();
            	}
            }

            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject currentApi = new NativeObject();
                Object apiObject = it.next();
                WebApp api = (WebApp) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                currentApi.put("name", currentApi, apiIdentifier.getApiName());
                currentApi.put("provider", currentApi, AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                currentApi.put("version", currentApi,
                        apiIdentifier.getVersion());
                currentApi.put("description", currentApi, api.getDescription());
                currentApi.put("rates", currentApi, api.getRating());
                if (api.getThumbnailUrl() == null) {
                    currentApi.put("thumbnailurl", currentApi, "images/api-default.png");
                } else {
                    currentApi.put("thumbnailurl", currentApi, AppManagerUtil.prependWebContextRoot(api.getThumbnailUrl()));
                }
                currentApi.put("visibility", currentApi, api.getVisibility());
                currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
                apiArray.put(i, apiArray, currentApi);
                i++;
            }

        }// end of the if
        return apiArray;
    }

    public static NativeArray jsFunction_searchAPIbyType(Context cx,
                                                         Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (args!=null&&args.length!=0) {
            String searchValue =(String) args[0];
            String tenantDomain = (String)args[1];
            String searchTerm;
            String searchType;
            Set<WebApp> apiSet = null;
            boolean noSearchTerm = false;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            boolean isTenantFlowStarted = false;
            try {
            	if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            		isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            	}
                if (searchValue.contains(":")) {
                    if (searchValue.split(":").length > 1) {
                        searchType = searchValue.split(":")[0];
                        searchTerm = searchValue.split(":")[1];
                        if ("*".equals(searchTerm) || searchTerm.startsWith("*")) {
                            searchTerm = searchTerm.replaceFirst("\\*", ".*");
                        }
                        apiSet = apiConsumer.searchAPI(searchTerm, searchType,tenantDomain);
                    } else {
                        noSearchTerm = true;
                    }

                } else {
                    if ("*".equals(searchValue) || searchValue.startsWith("*")) {
                        searchValue = searchValue.replaceFirst("\\*", ".*");
                    }
                    apiSet = apiConsumer.searchAPI(searchValue,"Name",tenantDomain);
                }

            } catch (AppManagementException e) {
                log.error("Error while searching APIs by type", e);
                return apiArray;
            } catch (Exception e) {
                log.error("Error while searching APIs by type", e);
                return apiArray;
            } finally {
            	if (isTenantFlowStarted) {
            		PrivilegedCarbonContext.endTenantFlow();
            	}
            }

            if (noSearchTerm) {
                throw new AppManagementException("Search term is missing. Try again with valid search query.");
            }
            if(apiSet!=null){
            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {

                NativeObject currentApi = new NativeObject();
                Object apiObject = it.next();
                WebApp api = (WebApp) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                currentApi.put("name", currentApi, apiIdentifier.getApiName());
                currentApi.put("provider", currentApi,
                               AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                currentApi.put("version", currentApi,
                        apiIdentifier.getVersion());
                currentApi.put("description", currentApi, api.getDescription());
                currentApi.put("rates", currentApi, api.getRating());
                currentApi.put("description", currentApi, api.getDescription());
                currentApi.put("endpoint", currentApi, api.getUrl());
                if (api.getThumbnailUrl() == null) {
                    currentApi.put("thumbnailurl", currentApi, "images/api-default.png");
                } else {
                    currentApi.put("thumbnailurl", currentApi, AppManagerUtil.prependWebContextRoot(api.getThumbnailUrl()));
                }
                currentApi.put("visibility", currentApi, api.getVisibility());
                currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
                currentApi.put("description", currentApi, api.getDescription());

                apiArray.put(i, apiArray, currentApi);
                i++;
            }
            }

        }// end of the if
        return apiArray;
    }

    public static NativeObject jsFunction_searchPaginatedAPIsByType(Context cx,
                                                         Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {
        NativeArray apiArray = new NativeArray(0);
        NativeObject resultObj = new NativeObject();
        Map<String,Object> result = new HashMap<String,Object>();
        if (args!=null&&args.length!=0) {
            String searchValue =(String) args[0];
            String tenantDomain = (String)args[1];
            int start = Integer.parseInt((String)args[2]);
            int end = Integer.parseInt((String)args[3]);
            String searchTerm;
            String searchType;
            Set<WebApp> apiSet = null;
            boolean noSearchTerm = false;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            boolean isTenantFlowStarted = false;
            try {
            	if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            		isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            	}
               else{
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);

                }
                if (searchValue.contains(":")) {
                    if (searchValue.split(":").length > 1) {
                        searchType = searchValue.split(":")[0];
                        searchTerm = searchValue.split(":")[1];
                        if ("*".equals(searchTerm) || searchTerm.startsWith("*")) {
                            searchTerm = searchTerm.replaceFirst("\\*", ".*");
                        }
                        result = apiConsumer.searchPaginatedAPIs(searchTerm, searchType,tenantDomain,start,end);
                    } else {
                        noSearchTerm = true;
                    }

                } else {
                    if ("*".equals(searchValue) || searchValue.startsWith("*")) {
                        searchValue = searchValue.replaceFirst("\\*", ".*");
                    }
                    result = apiConsumer.searchPaginatedAPIs(searchValue,"Name",tenantDomain,start,end);
                }

            } catch (AppManagementException e) {
                log.error("Error while searching APIs by type", e);
                return resultObj;
            } catch (Exception e) {
                log.error("Error while searching APIs by type", e);
                return resultObj;
            } finally {
            	if (isTenantFlowStarted) {
            		PrivilegedCarbonContext.endTenantFlow();
            	}
            }

            if (noSearchTerm) {
                throw new AppManagementException("Search term is missing. Try again with valid search query.");
            }
            if(result!=null){
            apiSet= (Set<WebApp>) result.get("apis");
            if(apiSet!=null){
            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {

                NativeObject currentApi = new NativeObject();
                Object apiObject = it.next();
                WebApp api = (WebApp) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                currentApi.put("name", currentApi, apiIdentifier.getApiName());
                currentApi.put("provider", currentApi,
                               AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                currentApi.put("version", currentApi,
                        apiIdentifier.getVersion());
                currentApi.put("description", currentApi, api.getDescription());
                currentApi.put("rates", currentApi, api.getRating());
                currentApi.put("description", currentApi, api.getDescription());
                currentApi.put("endpoint", currentApi, api.getUrl());
                if (api.getThumbnailUrl() == null) {
                    currentApi.put("thumbnailurl", currentApi, "images/api-default.png");
                } else {
                    currentApi.put("thumbnailurl", currentApi, AppManagerUtil.prependWebContextRoot(api.getThumbnailUrl()));
                }
                currentApi.put("visibility", currentApi, api.getVisibility());
                currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
                currentApi.put("description", currentApi, api.getDescription());

                apiArray.put(i, apiArray, currentApi);
                i++;
            }
                resultObj.put("apis",resultObj,apiArray);
                resultObj.put("totalLength",resultObj,result.get("length"));
            }
            }

        }// end of the if
        return resultObj;
    }

    public static boolean jsFunction_isSelfSignupEnabled(){
        AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        return Boolean.parseBoolean(config.getFirstProperty(AppMConstants.SELF_SIGN_UP_ENABLED));
    }

    public static NativeArray jsFunction_getAPIsWithTag(Context cx,
                                                        Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (args!=null && isStringArray(args)) {
            String tagName = args[0].toString();
            Set<WebApp> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                apiSet = apiConsumer.getAPIsWithTag(tagName);
            } catch (AppManagementException e) {
                log.error("Error from Registry WebApp while getting APIs With Tag Information", e);
                return apiArray;
            } catch (Exception e) {
                log.error("Error while getting APIs With Tag Information", e);
                return apiArray;
            }
            if (apiSet != null) {
                Iterator it = apiSet.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject currentApi = new NativeObject();
                    Object apiObject = it.next();
                    WebApp api = (WebApp) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    currentApi.put("name", currentApi, apiIdentifier.getApiName());
                    currentApi.put("provider", currentApi,
                                   AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    currentApi.put("version", currentApi,
                                   apiIdentifier.getVersion());
                    currentApi.put("description", currentApi, api.getDescription());
                    currentApi.put("rates", currentApi, api.getRating());
                    if (api.getThumbnailUrl() == null) {
                        currentApi.put("thumbnailurl", currentApi,
                                       "images/api-default.png");
                    } else {
                        currentApi.put("thumbnailurl", currentApi,
                                       AppManagerUtil.prependWebContextRoot(api.getThumbnailUrl()));
                    }
                    currentApi.put("visibility", currentApi, api.getVisibility());
                    currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
                    currentApi.put("description", currentApi, api.getDescription());
                    apiArray.put(i, apiArray, currentApi);
                    i++;
                }
            }

        }// end of the if
        return apiArray;
    }

    public static NativeObject jsFunction_getPaginatedAPIsWithTag(Context cx,
                                                        Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {
        NativeArray apiArray = new NativeArray(0);
        NativeObject resultObj = new NativeObject();
        Map<String,Object> resultMap=new HashMap<String, Object>();
        if (args!=null && isStringArray(args)) {
            String tagName = args[0].toString();
            int start = Integer.parseInt(args[1].toString());
            int end = Integer.parseInt(args[2].toString());
            Set<WebApp> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                resultMap = apiConsumer.getPaginatedAPIsWithTag(tagName, start, end);
                apiSet= (Set<WebApp>) resultMap.get("apis");
            } catch (AppManagementException e) {
                log.error("Error from Registry WebApp while getting APIs With Tag Information", e);
                return resultObj;
            } catch (Exception e) {
                log.error("Error while getting APIs With Tag Information", e);
                return resultObj;
            }
            if (apiSet != null) {
                Iterator it = apiSet.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject currentApi = new NativeObject();
                    Object apiObject = it.next();
                    WebApp api = (WebApp) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    currentApi.put("name", currentApi, apiIdentifier.getApiName());
                    currentApi.put("provider", currentApi,
                                   AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    currentApi.put("version", currentApi,
                                   apiIdentifier.getVersion());
                    currentApi.put("description", currentApi, api.getDescription());
                    currentApi.put("rates", currentApi, api.getRating());
                    if (api.getThumbnailUrl() == null) {
                        currentApi.put("thumbnailurl", currentApi,
                                       "images/api-default.png");
                    } else {
                        currentApi.put("thumbnailurl", currentApi,
                                       AppManagerUtil.prependWebContextRoot(api.getThumbnailUrl()));
                    }
                    currentApi.put("visibility", currentApi, api.getVisibility());
                    currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
                    currentApi.put("description", currentApi, api.getDescription());
                    apiArray.put(i, apiArray, currentApi);
                    i++;
                }
                resultObj.put("apis",resultObj,apiArray);
                resultObj.put("totalLength",resultObj,resultMap.get("length"));
            }

        }// end of the if
        return resultObj;
    }

    public static NativeArray jsFunction_getSubscribedAPIs(Context cx,
                                                           Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (args!=null && isStringArray(args)) {
            String limitArg = args[0].toString();
            int limit = Integer.parseInt(limitArg);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                Set<WebApp> apiSet = apiConsumer.getTopRatedAPIs(limit);
                if (apiSet != null) {
                    Iterator it = apiSet.iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        NativeObject currentApi = new NativeObject();
                        Object apiObject = it.next();
                        WebApp api = (WebApp) apiObject;
                        APIIdentifier apiIdentifier = api.getId();
                        currentApi.put("name", currentApi, apiIdentifier.getApiName());
                        currentApi.put("provider", currentApi,
                                       apiIdentifier.getProviderName());
                        currentApi.put("version", currentApi,
                                       apiIdentifier.getVersion());
                        currentApi.put("description", currentApi, api.getDescription());
                        currentApi.put("rates", currentApi, api.getRating());
                        apiArray.put(i, apiArray, currentApi);
                        i++;
                    }
                }
            } catch (AppManagementException e) {
                log.error("Error while getting WebApp list", e);
                return apiArray;
            }
        }// end of the if
        return apiArray;
    }

    public static NativeArray jsFunction_getAllTags(Context cx,
                                                    Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {
        NativeArray tagArray = new NativeArray(0);
        Map<String, String> attributeMap = new HashMap<>();
        Set<Tag> tags;
        String tenantDomain = null;
        String assetType =  null;
        if (args!=null && isStringArray(args)) {
            //There will be separate two calls for this method with one argument and with three arguments.
            if (args.length == 1) {
                tenantDomain = args[0].toString();
            } else if (args.length >= 3) {
                assetType = args[1].toString();
                attributeMap.put(AppMConstants.APP_OVERVIEW_TREAT_AS_A_SITE, args[2].toString());
            }
        }
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            tags = apiConsumer.getAllTags(tenantDomain, assetType, attributeMap);
        } catch (AppManagementException e) {
            log.error("Error from registry while getting WebApp tags.", e);
            return tagArray;
        } catch (Exception e) {
            log.error("Error while getting WebApp tags", e);
            return tagArray;
        }
        if(tags!=null){
        Iterator tagsI = tags.iterator();
        int i = 0;
        while (tagsI.hasNext()) {

            NativeObject currentTag = new NativeObject();
            Object tagObject = tagsI.next();
            Tag tag = (Tag) tagObject;

            currentTag.put("name", currentTag, tag.getName());
            currentTag.put("count", currentTag, tag.getNoOfOccurrences());

            tagArray.put(i, tagArray, currentTag);
            i++;
        }
        }

        return tagArray;
    }

    public static NativeArray jsFunction_getAllPublishedAPIs(Context cx,
                                                             Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {
        Set<WebApp> apiSet;
        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain;
            if (args != null) {
                tenantDomain = (String) args[0];
            } else {
                tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,true);
            }
            apiSet = apiConsumer.getAllPublishedAPIs(tenantDomain);

        } catch (AppManagementException e) {
            log.error("Error from Registry WebApp while getting WebApp Information", e);
            return myn;
        } catch (Exception e) {
            log.error("Error while getting WebApp Information", e);
            return myn;
        }
        finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        if (apiSet != null) {
            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object apiObject = it.next();
                WebApp api = (WebApp) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                row.put("name", row, apiIdentifier.getApiName());
                row.put("provider", row, AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                row.put("version", row, apiIdentifier.getVersion());
                row.put("context", row, api.getContext());
                row.put("status", row, "Deployed"); // api.getStatus().toString()
                if (api.getThumbnailUrl() == null) {
                    row.put("thumbnailurl", row, "images/api-default.png");
                } else {
                    row.put("thumbnailurl", row, AppManagerUtil.prependWebContextRoot(api.getThumbnailUrl()));
                }
                row.put("visibility", row, api.getVisibility());
                row.put("visibleRoles", row, api.getVisibleRoles());
                row.put("description", row, api.getDescription());
                String apiOwner = api.getApiOwner();
                if (apiOwner == null) {
                	apiOwner = AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName());
                }
                row.put("apiOwner", row, apiOwner);
                row.put("isAdvertiseOnly", row, api.isAdvertiseOnly());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeObject jsFunction_getAllPaginatedPublishedAPIs(Context cx,
                                                             Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {
        Set<WebApp> apiSet;
        Map<String,Object> resultMap;
        NativeArray myn = new NativeArray(0);
        NativeObject result = new NativeObject();

        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            String tenantDomain;
            if (args[0] != null) {
                tenantDomain = (String) args[0];
            } else {
                tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
            	PrivilegedCarbonContext.startTenantFlow();
            	PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            } else{
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);

            }
            int start=Integer.parseInt((String)args[1]);
            int end=Integer.parseInt((String)args[2]);
            resultMap = apiConsumer.getAllPaginatedPublishedAPIs(tenantDomain,start,end);

        } catch (AppManagementException e) {
            log.error("Error from Registry WebApp while getting WebApp Information", e);
            return result;
        } catch (Exception e) {
            log.error("Error while getting WebApp Information", e);
            return result;
        }
        finally {
        		PrivilegedCarbonContext.endTenantFlow();


        }
        if (resultMap != null) {
            apiSet= (Set<WebApp>) resultMap.get("apis");
        if (apiSet != null) {
            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object apiObject = it.next();
                WebApp api = (WebApp) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                row.put("name", row, apiIdentifier.getApiName());
                row.put("provider", row, AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                row.put("version", row, apiIdentifier.getVersion());
                row.put("context", row, api.getContext());
                row.put("status", row, "Deployed"); // api.getStatus().toString()
                if (api.getThumbnailUrl() == null) {
                    row.put("thumbnailurl", row, "images/api-default.png");
                } else {
                    row.put("thumbnailurl", row, AppManagerUtil.prependWebContextRoot(api.getThumbnailUrl()));
                }
                row.put("visibility", row, api.getVisibility());
                row.put("visibleRoles", row, api.getVisibleRoles());
                row.put("description", row, api.getDescription());
                String apiOwner = api.getApiOwner();
                if (apiOwner == null) {
                	apiOwner = AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName());
                }
                row.put("apiOwner", row, apiOwner);
                row.put("isAdvertiseOnly", row, api.isAdvertiseOnly());
                myn.put(i, myn, row);
                i++;
            }
            result.put("apis",result,myn);
            result.put("totalLength",result,resultMap.get("totalLength"));

            }
        }
        return result;
    }

    public static NativeArray jsFunction_getAPI(Context cx, Scriptable thisObj,
                                                Object[] args, Function funObj) throws ScriptException,
                                                                                       AppManagementException {

        String providerName;
        String apiName;
        String version;
        String username = null;
        boolean isSubscribed = false;
        NativeArray myn = new NativeArray(0);
        if (args!=null && args.length != 0) {

        providerName = AppManagerUtil.replaceEmailDomain((String) args[0]);
        apiName = (String) args[1];
        version = (String) args[2];
        if (args[3] != null) {
        username = (String) args[3];
        }
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        WebApp api;
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        boolean isTenantFlowStarted = false;
        try {
        	String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerName));
        	if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
        		isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        	}
        	
            api = apiConsumer.getAPI(apiIdentifier);
            if (username != null) {
                //TODO @sumedha : remove hardcoded tenant Id
                isSubscribed = apiConsumer.isSubscribed(apiIdentifier, username);
            }

            if(api!=null){
            NativeObject row = new NativeObject();
            apiIdentifier = api.getId();
            row.put("name", row, apiIdentifier.getApiName());
            row.put("provider", row, AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
            row.put("version", row, apiIdentifier.getVersion());
            row.put("description", row, api.getDescription());
            row.put("rates", row, api.getRating());
            row.put("endpoint", row, api.getUrl());
            row.put("wsdl", row, api.getWsdlUrl());
            row.put("wadl", row, api.getWadlUrl());
            DateFormat dateFormat= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            String dateFormatted= dateFormat.format(api.getLastUpdated());
            row.put("updatedDate", row, dateFormatted);
            row.put("context", row, api.getContext());
            row.put("status", row, api.getStatus().getStatus());

            String user = getUsernameFromObject(thisObj);
            if(user!=null){
            int userRate = apiConsumer.getUserRating(apiIdentifier, user);
            row.put("userRate", row, userRate);
            }
            AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
            List<Environment> environments = config.getApiGatewayEnvironments();
            String envDetails = "";
            for(int i=0; i<environments.size(); i++){
                Environment environment = environments.get(i);
                envDetails += environment.getName() + ",";
                envDetails += filterUrls(environment.getApiGatewayEndpoint(),api.getTransports());
                if(i < environments.size() - 1){
                    envDetails += "|";
                }
            }
            //row.put("serverURL", row, config.getFirstProperty(AppMConstants.API_GATEWAY_API_ENDPOINT));
            row.put("serverURL", row, envDetails);
            NativeArray tierArr=new NativeArray(0);
            Set<Tier> tierSet = api.getAvailableTiers();
            if(tierSet!=null){
            Iterator it = tierSet.iterator();
            int j = 0;

            while (it.hasNext()) {
                    NativeObject tierObj=new NativeObject();
                    Object tierObject = it.next();
                    Tier tier = (Tier) tierObject;
                    tierObj.put("tierName", tierObj, tier.getName());
                    tierObj.put("tierDisplayName", tierObj, tier.getDisplayName());
                    tierObj.put("tierDescription", tierObj,
                            tier.getDescription() != null ? tier.getDescription() : "");
                    if(tier.getTierAttributes()!=null){
                        Map<String,Object> attributes;
                        attributes=tier.getTierAttributes();
                        String attributesList="";
                        for (Map.Entry<String, Object> attribute : attributes.entrySet()){
                            attributesList+=attribute.getKey()+"::"+attribute.getValue()+",";

                        }
                        tierObj.put("tierAttributes",tierObj,attributesList);
                    }
                    tierArr.put(j, tierArr, tierObj);
                    j++;

            }
            }
                row.put("tiers", row, tierArr);
                row.put("subscribed", row, isSubscribed);
                if (api.getThumbnailUrl() == null) {
                    row.put("thumbnailurl", row, "images/api-default.png");
                } else {
                    row.put("thumbnailurl", row, AppManagerUtil.prependWebContextRoot(api.getThumbnailUrl()));
                }
                row.put("bizOwner", row, api.getBusinessOwner());
                row.put("bizOwnerMail", row, api.getBusinessOwnerEmail());
                row.put("techOwner", row, api.getTechnicalOwner());
                row.put("techOwnerMail", row, api.getTechnicalOwnerEmail());
                row.put("visibility", row, api.getVisibility());
                row.put("visibleRoles", row, api.getVisibleRoles());

                Set<URITemplate> uriTemplates = api.getUriTemplates();
                List<NativeArray> uriTemplatesArr = new ArrayList<NativeArray>();
                if (uriTemplates.size() != 0) {
                    NativeArray uriTempArr = new NativeArray(uriTemplates.size());
                    Iterator i = uriTemplates.iterator();

                    while (i.hasNext()) {
                        List<String> utArr = new ArrayList<String>();
                        URITemplate ut = (URITemplate) i.next();
                        utArr.add(ut.getUriTemplate());
                        utArr.add(ut.getMethodsAsString().replaceAll("\\s", ","));
                        utArr.add(ut.getAuthTypeAsString().replaceAll("\\s", ","));
                        utArr.add(ut.getThrottlingTiersAsString().replaceAll("\\s", ","));
                        NativeArray utNArr = new NativeArray(utArr.size());
                        for (int p = 0; p < utArr.size(); p++) {
                            utNArr.put(p, utNArr, utArr.get(p));
                        }
                        uriTemplatesArr.add(utNArr);
                    }

                    for (int c = 0; c < uriTemplatesArr.size(); c++) {
                        uriTempArr.put(c, uriTempArr, uriTemplatesArr.get(c));
                    }

                    myn.put(1, myn, uriTempArr);
                }
                row.put("uriTemplates", row, uriTemplatesArr.toString());
                String apiOwner = api.getApiOwner();
                if (apiOwner == null) {
                	apiOwner = AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName());
                }
                row.put("apiOwner", row, apiOwner);
                row.put("isAdvertiseOnly", row, api.isAdvertiseOnly());
                row.put("redirectURL", row, api.getRedirectURL());
                
                row.put("subscriptionAvailability", row, api.getSubscriptionAvailability());
                row.put("subscriptionAvailableTenants", row, api.getSubscriptionAvailableTenants());
                myn.put(0, myn, row);
            }


        } catch (AppManagementException e) {
            handleException("Error from Registry WebApp while getting get WebApp Information on " + apiName, e);

        } catch (Exception e) {
            handleException(e.getMessage(), e);
        } finally {
        	if (isTenantFlowStarted) {
        		PrivilegedCarbonContext.endTenantFlow();
        	}
        }
        }
        return myn;
    }

    private static String filterUrls(String apiData, String transports) {
        if(apiData != null && transports !=null){
            List<String> urls = new ArrayList<String>();
            List<String> transportList = new ArrayList<String>();
            urls.addAll(Arrays.asList(apiData.split(",")));
            transportList.addAll(Arrays.asList(transports.split(",")));
            urls = filterUrlsByTransport(urls, transportList, "https");
            urls = filterUrlsByTransport(urls, transportList, "http");
            String urlString = urls.toString();
            return urlString.substring(1, urlString.length() - 1);
        }
        return apiData;
    }

    private static List<String> filterUrlsByTransport(List<String> urlsList, List<String> transportList, String transportName) {
        if(!transportList.contains(transportName)){
            ListIterator<String> it = urlsList.listIterator();
            while(it.hasNext()){
                String url = it.next();
                if(url.startsWith(transportName+":")){
                    it.remove();
                }
            }
            return urlsList;
        }
        return urlsList;
    }

    public static boolean jsFunction_isSubscribed(Context cx, Scriptable thisObj,
                                                  Object[] args, Function funObj)
            throws ScriptException,
                   AppManagementException {

        String username = null;
        if (args != null && args.length != 0) {
            String providerName = (String) args[0];
            String apiName = (String) args[1];
            String version = (String) args[2];
            if (args[3] != null) {
                username = (String) args[3];
            }
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            return username != null && apiConsumer.isSubscribed(apiIdentifier, username);
        } else {
            throw new AppManagementException("No input username value.");
        }
    }

    public static NativeArray jsFunction_getAllDocumentation(Context cx,
                                                             Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {
        java.util.List<Documentation> doclist = null;
        String providerName = "";
        String apiName = "";
        String version = "";
        String username = "";
        if (args!=null && args.length!=0 ) {
            providerName = AppManagerUtil.replaceEmailDomain((String)args[0]);
            apiName = (String)args[1];
            version = (String)args[2];
            username = (String)args[3];
        }
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            doclist = apiConsumer.getAllDocumentation(apiIdentifier, username);
        } catch (AppManagementException e) {
            handleException("Error from Registry WebApp while getting All Documentation on " + apiName, e);
        } catch (Exception e) {
            handleException("Error while getting All Documentation " + apiName, e);
        }
        if(doclist!=null){
        Iterator it = doclist.iterator();
        int i = 0;
        while (it.hasNext()) {
            NativeObject row = new NativeObject();
            Object docObject = it.next();
            Documentation documentation = (Documentation) docObject;
            Object objectSourceType = documentation.getSourceType();
            String strSourceType = objectSourceType.toString();
            row.put("name", row, documentation.getName());
            row.put("sourceType", row, strSourceType);
            row.put("summary", row, documentation.getSummary());
            String content;
            if (strSourceType.equals("INLINE")) {
                content = apiConsumer.getDocumentationContent(apiIdentifier, documentation.getName());
                row.put("content", row, content);
            }
            row.put("sourceUrl", row, documentation.getSourceUrl());
            row.put("filePath", row, documentation.getFilePath());
            DocumentationType documentationType = documentation.getType();
            row.put("type", row, documentationType.getType());

            if (documentationType == DocumentationType.OTHER) {
                row.put("otherTypeName", row, documentation.getOtherTypeName());
            }

            myn.put(i, myn, row);
            i++;
        }
        }
        return myn;

    }


    public static NativeArray jsFunction_getComments(Context cx,
                                                     Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {
        Comment[] commentlist = new Comment[0];
        String providerName = "";
        String apiName = "";
        String version = "";
        if (args!=null && args.length!=0 ) {
            providerName = AppManagerUtil.replaceEmailDomain((String)args[0]);
            apiName = (String)args[1];
            version = (String)args[2];
        }
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName,
                version);
        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            commentlist = apiConsumer.getComments(apiIdentifier);
        } catch (AppManagementException e) {
            handleException("Error from registry while getting  comments for " + apiName, e);
        } catch (Exception e) {
            handleException("Error while getting comments for " + apiName, e);
        }

        int i = 0;
        if(commentlist!=null){
        for (Comment n : commentlist) {
            NativeObject row = new NativeObject();
            row.put("userName", row, n.getUser());
            row.put("comment", row, n.getText());
            row.put("createdTime", row, n.getCreatedTime().getTime());
            myn.put(i, myn, row);
            i++;
        }
        }
        return myn;

    }

    public static NativeArray jsFunction_addComments(Context cx,
                                                     Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {
        String providerName = "";
        String apiName = "";
        String version = "";
        String commentStr = "";
        if (args!=null&& args.length!=0 &&isStringArray(args)) {
            providerName = AppManagerUtil.replaceEmailDomain((String)args[0]);
            apiName = (String)args[1];
            version = (String)args[2];
            commentStr = (String)args[3];
        }
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            apiConsumer.addComment(apiIdentifier, commentStr, getUsernameFromObject(thisObj));
        } catch (AppManagementException e) {
            handleException("Error from registry while adding comments for " + apiName, e);
        } catch (Exception e) {
            handleException("Error while adding comments for " + apiName, e);
        }

        int i = 0;
        NativeObject row = new NativeObject();
        row.put("userName", row, providerName);
        row.put("comment", row, commentStr);
        myn.put(i, myn, row);

        return myn;
    }

    /**
     * Returns the subscription for the given criteria based on the subscription type. e.g. Individual, Enterprise
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static NativeObject jsFunction_getSubscription(Context cx,
                                         Scriptable thisObj, Object[] args, Function funObj)throws
                                                                                            AppManagementException {

        APIConsumer apiConsumer = getAPIConsumer(thisObj);

        String providerName = (String)args[0];
        providerName= AppManagerUtil.replaceEmailDomain(providerName);
        String apiName = (String)args[1];
        String version = (String)args[2];
        String applicationName = (String)args[3];
        String subscriptionType = (String)args[4];
        String userId = (String)args[5];

        int applicationId = AppManagerUtil.getApplicationId(applicationName,userId);

        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);

        NativeObject subscriptionToReturn = null;

        try {
            Subscription subscription = apiConsumer.getSubscription(apiIdentifier, applicationId, subscriptionType);

            if(subscription != null){
                subscriptionToReturn = new NativeObject();

                subscriptionToReturn.put("subscriptionId", subscriptionToReturn, subscription.getSubscriptionId());
                subscriptionToReturn.put("webAppId", subscriptionToReturn, subscription.getWebAppId());
                subscriptionToReturn.put("applicationId", subscriptionToReturn, subscription.getApplicationId());
                subscriptionToReturn.put("subscriptionType", subscriptionToReturn, subscription.getSubscriptionType());
                subscriptionToReturn.put("subscriptionStatus",subscriptionToReturn,subscription.getSubscriptionStatus());

                Set<String> trustedIdps = subscription.getTrustedIdps();

                String trustedIdpsJsonString = "[]";
                if(trustedIdps != null) {
                    JSONArray jsonArray = new JSONArray();

                    for (String idp : trustedIdps) {
                        jsonArray.add(idp);
                    }

                    trustedIdpsJsonString = JSONValue.toJSONString(jsonArray);
                }

                subscriptionToReturn.put("trustedIdps", subscriptionToReturn, trustedIdpsJsonString);

            }

            return subscriptionToReturn;

        } catch (AppManagementException e) {
            handleException("Error while getting subscription", e);
            return null;
        }

    }


    public static String jsFunction_addSubscription(Context cx,
                                                     Scriptable thisObj, Object[] args, Function funObj)
            throws AppManagementException {
        if (args==null ||args.length==0) {
            return "";
        }
        
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        
        String providerName = (String)args[0];
        providerName= AppManagerUtil.replaceEmailDomain(providerName);
        String apiName = (String)args[1];
        String version = (String)args[2];
        String tier = "Unlimited";//(String)args[3];
        int applicationId = ((Number) args[4]).intValue();
        String userId = (String)args[5];
        String trustedIdp = null;
        if( args.length > 6){
        	trustedIdp = args[6].toString();
        }

        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        boolean isTenantFlowStarted = false;
        try {
        	String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerName));
        	if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
        		isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        	}
                
	        /* Validation for allowed throttling tiers*/
	        WebApp api = apiConsumer.getAPI(apiIdentifier);
	        Set<Tier> tiers = api.getAvailableTiers();
	        
	        Iterator<Tier> iterator = tiers.iterator();
	        boolean isTierAllowed = false;
	        List<String> allowedTierList = new ArrayList<String>();
	        while(iterator.hasNext()) {
	        	Tier t = iterator.next();
	        	if (t.getName() != null && (t.getName()).equals(tier)) {
	        		isTierAllowed = true;
	        	}
	        	allowedTierList.add(t.getName());
	        }
	        if (!isTierAllowed) {
	    		throw new AppManagementException("Tier " + tier + " is not allowed for WebApp " + apiName + "-" + version + ". Only "
	    				+Arrays.toString(allowedTierList.toArray())+ " Tiers are alllowed.");
	        }
	    	if (apiConsumer.isTierDeneid(tier)) {
	    		throw new AppManagementException("Tier " + tier + " is not allowed for user " + userId);
	    	}
	    	/* Tenant based validation for subscription*/
	    	String userDomain = MultitenantUtils.getTenantDomain(userId);
	    	boolean subscriptionAllowed = false;
	    	if (!userDomain.equals(tenantDomain)) {
	    		String subscriptionAvailability = api.getSubscriptionAvailability();
	    		if (AppMConstants.SUBSCRIPTION_TO_ALL_TENANTS.equals(subscriptionAvailability)) {
	    			subscriptionAllowed = true;
	    		} else if (AppMConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS.equals(subscriptionAvailability)) {
	    			String subscriptionAllowedTenants = api.getSubscriptionAvailableTenants();
	    			String allowedTenants[] = null;
	    			if (subscriptionAllowedTenants != null) {
	    				allowedTenants = subscriptionAllowedTenants.split(",");
	    				if (allowedTenants != null) {
	    					for(String tenant: allowedTenants) {
	    						if (tenant != null && userDomain.equals(tenant.trim())) {
	    							subscriptionAllowed = true;
	    							break;
	    						}
	    					}
	    				}
	    			}
	    		}
	    	} else {
	    		subscriptionAllowed = true;
	    	}
	    	if (!subscriptionAllowed) {
	    		throw new AppManagementException("Subscription is not allowed for " + userDomain);
	    	}
    	   	apiIdentifier.setTier(tier);
            String subsStatus = apiConsumer.addSubscription(apiIdentifier, Subscription.SUBSCRIPTION_TYPE_INDIVIDUAL, userId, applicationId, trustedIdp);
            return subsStatus;
        } catch (AppManagementException e) {
            handleException("Error while adding subscription for user: " + userId + " Reason: " + e.getMessage(), e);
            return null;
        } finally {
        	if (isTenantFlowStarted) {
        		PrivilegedCarbonContext.endTenantFlow();
        	}
        }
    }

    public static boolean jsFunction_addAPISubscription(Context cx,
			Scriptable thisObj, Object[] args, Function funObj) {
        if(!isStringArray(args)) {
            return false;
        }

        String providerName = args[0].toString();
        String apiName = args[1].toString();
        String version = args[2].toString();
        String subscriptionType = args[3].toString();
        String tier = "Unlimited";
        String applicationName= ((String) args[5]);
        String userId = args[6].toString();
        String trustedIdp = null;
        if( args.length > 7){
        	trustedIdp = args[7].toString();
        }

        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        apiIdentifier.setTier(tier);

        APIConsumer apiConsumer = getAPIConsumer(thisObj);
		try {
            int applicationId = AppManagerUtil.getApplicationId(applicationName,userId);
			apiConsumer.addSubscription(apiIdentifier, subscriptionType, userId, applicationId, trustedIdp);
            return true;
		} catch (AppManagementException e) {
			log.error("Error while adding subscription for user: " + userId, e);
            return false;
		}
	}

    /**
     * This method takes care of updating the visibiltiy of an app to given user role.
     * It will be invoked when subscribing / un-subscribing to an app in the store.
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     */
    public static boolean jsFunction_updateAPPVisibility(Context cx,
                                                        Scriptable thisObj, Object[] args, Function funObj) {
        if(!isStringArray(args)) {
            return false;
        }

        String providerName = args[0].toString();
        String apiName = args[1].toString();
        String version = args[2].toString();
        String userName = args[3].toString();
        String optype = args[4].toString();
        String userRole = "Internal/private_"+userName;

        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        String apiPath = "/_system/governance"+AppManagerUtil.getAPIPath(apiIdentifier);
        try {
            if(optype.equalsIgnoreCase("ALLOW")) {
                org.wso2.carbon.user.api.UserRealm realm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
                realm.getAuthorizationManager().authorizeRole(userRole, apiPath, ActionConstants.GET);
                return true;
            }else if(optype.equalsIgnoreCase("DENY")){
                org.wso2.carbon.user.api.UserRealm realm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
                realm.getAuthorizationManager().denyRole(userRole, apiPath, ActionConstants.GET);
                return true;
            }
            return false;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while updating visibility of Web App : " + apiName +" at "+apiPath, e);
            return false;
        }
    }

    public static boolean jsFunction_removeSubscriber(Context cx,
                                                      Scriptable thisObj, Object[] args, Function funObj)
            throws AppManagementException {
        String providerName = "";
        String apiName = "";
        String version = "";
        String application = "";
        String userId = "";
        if (args!=null && args.length!=0 ) {
            providerName = (String)args[0];
            apiName = (String)args[1];
            version = (String)args[2];
            application = (String) args[3];
            userId = (String)args[4];
        }
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        apiIdentifier.setApplicationId(application);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            apiConsumer.removeSubscriber(apiIdentifier, userId);
            return true;
        } catch (AppManagementException e) {
            handleException("Error while removing subscriber: " + userId, e);
            return false;
        }

    }


    public static NativeArray jsFunction_rateAPI(Context cx,
                                                 Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {

        NativeArray myn = new NativeArray(0);
        if (args!=null && args.length!=0 ) {
            String providerName = AppManagerUtil.replaceEmailDomain((String)args[0]);
            String apiName = (String)args[1];
            String version = (String)args[2];
            String rateStr = (String)args[3];
            int rate;
            try {
                rate = Integer.parseInt(rateStr.substring(0, 1));
            } catch (NumberFormatException e) {
                log.error("Rate must to be number " + rateStr, e);
                return myn;
            } catch (Exception e) {
                log.error("Error from while Rating WebApp " + rateStr, e);
                return myn;
            }
            APIIdentifier apiId;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                apiId = new APIIdentifier(providerName, apiName, version);
                String user = getUsernameFromObject(thisObj);
                switch (rate) {
                    //Below case 0[Rate 0] - is to remove ratings from a user
                    case 0: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_ZERO, user);
                        break;
                    }
                    case 1: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_ONE, user);
                        break;
                    }
                    case 2: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_TWO, user);
                        break;
                    }
                    case 3: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_THREE, user);
                        break;
                    }
                    case 4: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_FOUR, user);
                        break;
                    }
                    case 5: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_FIVE, user);
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Can't handle " + rate);
                    }

                }
            } catch (AppManagementException e) {
                log.error("Error while Rating WebApp " + apiName
                        + e);
                return myn;
            } catch (Exception e) {
                log.error("Error while Rating WebApp " + apiName + e);
                return myn;
            }

            NativeObject row = new NativeObject();
            row.put("name", row, apiName);
            row.put("provider", row, AppManagerUtil.replaceEmailDomainBack(providerName));
            row.put("version", row, version);
            row.put("rates", row, rateStr);
            row.put("newRating", row, Float.toString(apiConsumer.getAPI(apiId).getRating()));
            myn.put(0, myn, row);

        }// end of the if
        return myn;
    }

    public static NativeArray jsFunction_removeAPIRating(Context cx,
                                                 Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {

        NativeArray myn = new NativeArray(0);
        if (args != null && args.length != 0) {
            String providerName = AppManagerUtil.replaceEmailDomain((String) args[0]);
            String apiName = (String) args[1];
            String version = (String) args[2];
            float rating = 0;
            APIIdentifier apiId;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            boolean isTenantFlowStarted = false;
            try {
                apiId = new APIIdentifier(providerName, apiName, version);
                String user = getUsernameFromObject(thisObj);
                
                String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(user));
                if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
                		isTenantFlowStarted = true;
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }
                
                apiConsumer.removeAPIRating(apiId, user);
                rating = apiConsumer.getAPI(apiId).getRating();

            } catch (AppManagementException e) {
                throw new AppManagementException("Error while remove User Rating of the WebApp " + apiName
                          + e);

            } catch (Exception e) {
                throw new AppManagementException("Error while remove User Rating of the WebApp  " + apiName + e);

            } finally {
            	if (isTenantFlowStarted) {
            		PrivilegedCarbonContext.endTenantFlow();
            	}
            }
            NativeObject row = new NativeObject();
            row.put("newRating", row, Float.toString(rating));
            myn.put(0, myn, row);
        }// end of the if
        return myn;
    }


    public static NativeArray jsFunction_getSubscriptions(Context cx,
                                                          Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {

        NativeArray myn = new NativeArray(0);
        if (args!=null && args.length!=0 ) {
            String providerName = (String)args[0];
            String apiName = (String)args[1];
            String version = (String)args[2];
            String user = (String)args[3];

            APIIdentifier apiIdentifier = new APIIdentifier(AppManagerUtil.replaceEmailDomain(providerName), apiName, version);
            Subscriber subscriber = new Subscriber(user);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Set<SubscribedAPI> apis = apiConsumer.getSubscribedIdentifiers(subscriber, apiIdentifier);
            int i = 0;
            if(apis!=null){
                for (SubscribedAPI api : apis) {
                    NativeObject row = new NativeObject();
                    row.put("application", row, api.getApplication().getName());
                    row.put("applicationId", row, api.getApplication().getId());
                    row.put("prodKey", row, getKey(api, AppMConstants.API_KEY_TYPE_PRODUCTION));
                    row.put("sandboxKey", row, getKey(api, AppMConstants.API_KEY_TYPE_SANDBOX));
                    myn.put(i++, myn, row);

                }
            }
        }
        return myn;
    }

    public static String jsFunction_getSwaggerDiscoveryUrl(Context cx,
                                                           Scriptable thisObj, Object[] args,
                                                           Function funObj)
            throws AppManagementException {
        String apiName;
        String version;
        String providerName;
        
        if (args != null && args.length != 0 ) {

            apiName = (String) args[0];
            version = (String) args[1];
            providerName = (String) args[2];
            
            String apiDefinitionFilePath = AppManagerUtil.getAPIDefinitionFilePath(apiName, version);
            apiDefinitionFilePath = RegistryConstants.PATH_SEPARATOR + "registry"
            		+ RegistryConstants.PATH_SEPARATOR + "resource"
            		+ RegistryConstants.PATH_SEPARATOR + "_system"
            		+ RegistryConstants.PATH_SEPARATOR + "governance"
            		+ apiDefinitionFilePath;
            
            apiDefinitionFilePath = AppManagerUtil.prependTenantPrefix(apiDefinitionFilePath, providerName);
            
            return AppManagerUtil.prependWebContextRoot(apiDefinitionFilePath);
            
        } else {
            handleException("Invalid input parameters.");
            return null;
        }
    }

    private static APIKey getKey(SubscribedAPI api, String keyType) {
        List<APIKey> apiKeys = api.getKeys();
        return getKeyOfType(apiKeys, keyType);
    }

    private static APIKey getAppKey(Application app, String keyType) {
        List<APIKey> apiKeys = app.getKeys();
        return getKeyOfType(apiKeys, keyType);
    }

    private static APIKey getKeyOfType(List<APIKey> apiKeys, String keyType) {
        for (APIKey key : apiKeys) {
            if (keyType.equals(key.getType())) {
                return key;
            }
        }
        return null;
    }

    public static NativeArray jsFunction_getAllSubscriptions(Context cx,
                                                             Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {

        if (args==null || args.length == 0 || !isStringArray(args)) {
            return null;
        }
        String user = (String) args[0];
        Subscriber subscriber = new Subscriber(user);
        Map<Integer, NativeArray> subscriptionsMap = new HashMap<Integer, NativeArray>();
        NativeArray appsObj = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        boolean isTenantFlowStarted = false;
        try {
	        String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(user));
	        if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
	        		isTenantFlowStarted = true;
	                PrivilegedCarbonContext.startTenantFlow();
	                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
	        }
	        Set<SubscribedAPI> subscribedAPIs = apiConsumer.getSubscribedAPIs(subscriber);
	        AppMDAO appMDAO = new AppMDAO();
	        int i = 0;
	        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
	            NativeArray apisArray = subscriptionsMap.get(subscribedAPI.getApplication().getId());
                    if(subscribedAPI.getSubStatus().equals("UNBLOCKED")){
                        if (apisArray == null) {
                            apisArray = new NativeArray(1);
                            NativeObject appObj = new NativeObject();
                            appObj.put("id", appObj, subscribedAPI.getApplication().getId());
                            appObj.put("name", appObj, subscribedAPI.getApplication().getName());
                            appObj.put("callbackUrl", appObj, subscribedAPI.getApplication().getCallbackUrl());
                            APIKey prodKey = getAppKey(subscribedAPI.getApplication(), AppMConstants.API_KEY_TYPE_PRODUCTION);
                            boolean prodEnableRegenarateOption = true;
                            if (prodKey != null) {
                                appObj.put("prodKey", appObj, prodKey.getAccessToken());
                                appObj.put("prodConsumerKey", appObj, prodKey.getConsumerKey());
                                appObj.put("prodConsumerSecret", appObj, prodKey.getConsumerSecret());
                                if (prodKey.getValidityPeriod() == Long.MAX_VALUE) {
                                    prodEnableRegenarateOption = false;
                                }
                                appObj.put("prodRegenarateOption", appObj, prodEnableRegenarateOption);
                                appObj.put("prodAuthorizedDomains", appObj, prodKey.getAuthorizedDomains());

                                if (isApplicationAccessTokenNeverExpire(prodKey.getValidityPeriod())) {
                                    appObj.put("prodValidityTime", appObj, -1);
                                } else {
                                    appObj.put("prodValidityTime", appObj, prodKey.getValidityPeriod());
                                }
                                //appObj.put("prodValidityRemainingTime", appObj, appMDAO.getApplicationAccessTokenRemainingValidityPeriod(prodKey.getAccessToken()));
                            } else {
                                appObj.put("prodKey", appObj, null);
                                appObj.put("prodConsumerKey", appObj, null);
                                appObj.put("prodConsumerSecret", appObj, null);
                                appObj.put("prodRegenarateOption", appObj, prodEnableRegenarateOption);
                                appObj.put("prodAuthorizedDomains", appObj, null);
                                if (isApplicationAccessTokenNeverExpire(getApplicationAccessTokenValidityPeriodInSeconds())) {
                                    appObj.put("prodValidityTime", appObj, -1);
                                } else {
                                    appObj.put("prodValidityTime", appObj, getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
                                }
                               // appObj.put("prodValidityRemainingTime", appObj, getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
                            }

                            APIKey sandboxKey = getAppKey(subscribedAPI.getApplication(), AppMConstants.API_KEY_TYPE_SANDBOX);
                            boolean sandEnableRegenarateOption = true;
                            if (sandboxKey != null) {
                                appObj.put("sandboxKey", appObj, sandboxKey.getAccessToken());
                                appObj.put("sandboxConsumerKey", appObj, sandboxKey.getConsumerKey());
                                appObj.put("sandboxConsumerSecret", appObj, sandboxKey.getConsumerSecret());
                                if (sandboxKey.getValidityPeriod() == Long.MAX_VALUE) {
                                    sandEnableRegenarateOption = false;
                                }
                                appObj.put("sandboxAuthorizedDomains", appObj, sandboxKey.getAuthorizedDomains());
                                if (isApplicationAccessTokenNeverExpire(sandboxKey.getValidityPeriod())) {
                                    if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
                                            isTenantFlowStarted = true;
                                            PrivilegedCarbonContext.startTenantFlow();
                                            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                                    }    appObj.put("sandValidityTime", appObj, -1);
                                } else {
                                    appObj.put("sandValidityTime", appObj, sandboxKey.getValidityPeriod());
                                }
                               // appObj.put("sandValidityRemainingTime", appObj, appMDAO.getApplicationAccessTokenRemainingValidityPeriod(sandboxKey.getAccessToken()));
                            } else {
                                appObj.put("sandboxKey", appObj, null);
                                appObj.put("sandboxConsumerKey", appObj, null);
                                appObj.put("sandboxConsumerSecret", appObj, null);
                                appObj.put("sandRegenarateOption", appObj, sandEnableRegenarateOption);
                                appObj.put("sandboxAuthorizedDomains", appObj, null);
                                if (isApplicationAccessTokenNeverExpire(getApplicationAccessTokenValidityPeriodInSeconds())) {
                                    appObj.put("sandValidityTime", appObj, -1);
                                } else {
                                    appObj.put("sandValidityTime", appObj, getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
                                }
                              //  appObj.put("sandValidityRemainingTime", appObj, getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
                            }

                            try {
                                addAPIObj(subscribedAPI, apisArray, thisObj);
                                appObj.put("subscriptions", appObj, apisArray);
                                appsObj.put(i++, appsObj, appObj);
                                //keep a subscriptions map in order to efficiently group appObj vice.
                                subscriptionsMap.put(subscribedAPI.getApplication().getId(), apisArray);
                            } catch (AppManagementException e) {
                                //If the exception occurred while retrieving data from registry (i.e due to insufficient permissions)
                                //then log the error and continue without throwing exception to avoid breaking the subscription UI.
                                log.error("Error while obtaining application metadata", e);
                            }
                    } else {
                            try {
                                addAPIObj(subscribedAPI, apisArray, thisObj);
                            } catch (AppManagementException e) {
                                //If the exception occurred while retrieving data from registry (i.e due to insufficient permissions)
                                //then log the error and continue without throwing exception to avoid breaking the subscription UI.
                                log.error("Error while obtaining application metadata", e);
                            }
                        }
                }
	        }
        } finally {
        	if (isTenantFlowStarted) {
        		PrivilegedCarbonContext.endTenantFlow();
        	}
        }
        return appsObj;
    }

    private static void addAPIObj(SubscribedAPI subscribedAPI, NativeArray apisArray,
                                  Scriptable thisObj) throws AppManagementException {
        NativeObject apiObj = new NativeObject();
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        AppMDAO appMDAO = new AppMDAO();
        try {
            WebApp api = apiConsumer.getAPI(subscribedAPI.getApiId());
            apiObj.put("name", apiObj, subscribedAPI.getApiId().getApiName());
            apiObj.put("provider", apiObj, AppManagerUtil.replaceEmailDomainBack(subscribedAPI.getApiId().getProviderName()));
            apiObj.put("version", apiObj, subscribedAPI.getApiId().getVersion());
            apiObj.put("status", apiObj, api.getStatus().toString());
            apiObj.put("tier", apiObj, subscribedAPI.getTier().getDisplayName());
            apiObj.put("subStatus", apiObj, subscribedAPI.getSubStatus());
            apiObj.put("thumburl", apiObj, AppManagerUtil.prependWebContextRoot(api.getThumbnailUrl()));
            apiObj.put("context", apiObj, api.getContext());
            apiObj.put("uuid",apiObj,api.getUUID());
            apiObj.put("thumbnail",apiObj,api.getThumbnailUrl());

            APIKey prodKey = getAppKey(subscribedAPI.getApplication(), AppMConstants.API_KEY_TYPE_PRODUCTION);
            if (prodKey != null) {
                apiObj.put("prodKey", apiObj, prodKey.getAccessToken());
                apiObj.put("prodConsumerKey", apiObj, prodKey.getConsumerKey());
                apiObj.put("prodConsumerSecret", apiObj, prodKey.getConsumerSecret());
                apiObj.put("prodAuthorizedDomains", apiObj, prodKey.getAuthorizedDomains());
                if (isApplicationAccessTokenNeverExpire(prodKey.getValidityPeriod())) {
                    apiObj.put("prodValidityTime", apiObj, -1);
                } else {
                    apiObj.put("prodValidityTime", apiObj, prodKey.getValidityPeriod());
                }
                //apiObj.put("prodValidityRemainingTime", apiObj, appMDAO.getApplicationAccessTokenRemainingValidityPeriod(prodKey.getAccessToken()));
            } else {
                apiObj.put("prodKey", apiObj, null);
                apiObj.put("prodConsumerKey", apiObj, null);
                apiObj.put("prodConsumerSecret", apiObj, null);
                apiObj.put("prodAuthorizedDomains", apiObj, null);
                 if (isApplicationAccessTokenNeverExpire(getApplicationAccessTokenValidityPeriodInSeconds())) {
                     apiObj.put("prodValidityTime", apiObj, -1);
                 } else {
                     apiObj.put("prodValidityTime", apiObj, getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
                 }
               // apiObj.put("prodValidityRemainingTime", apiObj, getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
            }

            APIKey sandboxKey = getAppKey(subscribedAPI.getApplication(), AppMConstants.API_KEY_TYPE_SANDBOX);
            if (sandboxKey != null) {
                apiObj.put("sandboxKey", apiObj, sandboxKey.getAccessToken());
                apiObj.put("sandboxConsumerKey", apiObj, sandboxKey.getConsumerKey());
                apiObj.put("sandboxConsumerSecret", apiObj, sandboxKey.getConsumerSecret());
                apiObj.put("sandAuthorizedDomains", apiObj, sandboxKey.getAuthorizedDomains());
                if (isApplicationAccessTokenNeverExpire(sandboxKey.getValidityPeriod())) {
                    apiObj.put("sandValidityTime", apiObj, -1);
                } else {
                    apiObj.put("sandValidityTime", apiObj, sandboxKey.getValidityPeriod());
                }
                //apiObj.put("sandValidityRemainingTime", apiObj, appMDAO.getApplicationAccessTokenRemainingValidityPeriod(sandboxKey.getAccessToken()));
            } else {
                apiObj.put("sandboxKey", apiObj, null);
                apiObj.put("sandboxConsumerKey", apiObj, null);
                apiObj.put("sandboxConsumerSecret", apiObj, null);
                apiObj.put("sandAuthorizedDomains", apiObj, null);
                if (getApplicationAccessTokenValidityPeriodInSeconds() < 0) {
                    apiObj.put("sandValidityTime", apiObj, -1);
                } else {
                    apiObj.put("sandValidityTime", apiObj, getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
                }
               // apiObj.put("sandValidityRemainingTime", apiObj, getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
            }
            apiObj.put("hasMultipleEndpoints", apiObj, String.valueOf(api.getSandboxUrl() != null));
            apisArray.put(apisArray.getIds().length, apisArray, apiObj);
        } catch (AppManagementException e) {
            handleException("Error while obtaining application metadata", e);
        }
    }

    public static NativeObject jsFunction_getSubscriber(Context cx,
                                                        Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {

        if (args != null && isStringArray(args)) {
            NativeObject user = new NativeObject();
            String userName = args[0].toString();
            Subscriber subscriber = null;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                subscriber = apiConsumer.getSubscriber(userName);
            } catch (AppManagementException e) {
                handleException("Error while getting Subscriber", e);
            } catch (Exception e) {
                handleException("Error while getting Subscriber", e);
            }

            if (subscriber != null) {
                user.put("name", user, subscriber.getName());
                user.put("id", user, subscriber.getId());
                user.put("email", user, subscriber.getEmail());
                user.put("subscribedDate", user, subscriber.getSubscribedDate());
                return user;
            }
        }
        return null;
    }

    public static boolean jsFunction_addSubscriber(Context cx,
                                                   Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException, UserStoreException {

        if (args!=null && isStringArray(args)) {
            Subscriber subscriber = new Subscriber((String) args[0]);
            subscriber.setSubscribedDate(new Date());
            //TODO : need to set the proper email
            subscriber.setEmail("");
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                int tenantId=ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(MultitenantUtils.getTenantDomain((String) args[0]));
                subscriber.setTenantId(tenantId);
                apiConsumer.addSubscriber(subscriber);
            } catch (AppManagementException e) {
                handleException("Error while adding the subscriber"+subscriber.getName(), e);
                return false;
            } catch (Exception e) {
                handleException("Error while adding the subscriber"+subscriber.getName(), e);
                return false;
            }
            return true;
        }
        return false;
    }

    public static boolean jsFunction_sleep(Context cx,
                                           Scriptable thisObj, Object[] args, Function funObj){
        if (isStringArray(args)) {
            String millis = (String) args[0];
            try {
                Thread.sleep( Long.valueOf(millis));
            } catch (InterruptedException e) {
                log.error("Sleep Thread Interrupted");
                return false;
            }
        }
        return true;
    }

    public static NativeArray jsFunction_getSubscriptionsByApplication(Context cx,
                                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {

        NativeArray myn = new NativeArray(0);
        if (args!=null && isStringArray(args)) {
            String applicationName = (String) args[0];
            String username = (String) args[1];
            boolean isTenantFlowStarted = false;
            try {
            	String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(username));
                if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
                		isTenantFlowStarted = true;
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }
	            Subscriber subscriber = new Subscriber(username);
	            APIConsumer apiConsumer = getAPIConsumer(thisObj);
	            Set<SubscribedAPI> subscribedAPIs = apiConsumer.getSubscribedAPIs(subscriber,applicationName);
	
	            int i = 0;
	            for (SubscribedAPI subscribedAPI : subscribedAPIs) {
	                    WebApp api = apiConsumer.getAPI(subscribedAPI.getApiId());
	                    NativeObject row = new NativeObject();
	                    row.put("apiName", row, subscribedAPI.getApiId().getApiName());
	                    row.put("apiVersion", row, subscribedAPI.getApiId().getVersion());
	                    row.put("apiProvider", row, AppManagerUtil.replaceEmailDomainBack(subscribedAPI.getApiId().getProviderName()));
	                    row.put("description", row, api.getDescription());
	                    row.put("subscribedTier", row, subscribedAPI.getTier().getName());
	                    row.put("status", row, api.getStatus().getStatus());
	                    myn.put(i, myn, row);
	                    i++;
	            }
            } finally {
            	if (isTenantFlowStarted) {
            		PrivilegedCarbonContext.endTenantFlow();
            	}
            }
        }
        return myn;
    }

    public static NativeObject jsFunction_resumeWorkflow(Context cx,
                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, WorkflowException {

        NativeObject row = new NativeObject();

        if (args!=null && isStringArray(args)) {

            String workflowReference = (String) args[0];
            String status = (String) args[1];
            String description = null;
            if(args.length > 2){
                description = (String) args[2];
            }

            AppMDAO appMDAO = new AppMDAO();

            try {
                if(workflowReference!=null){
                    WorkflowDTO workflowDTO = appMDAO.retrieveWorkflow(workflowReference);

                    if(workflowDTO == null){
                        log.error("Could not find workflow for reference " + workflowReference);
                        row.put("error", row, true);
                        row.put("statusCode", row, 500);
                        row.put("message", row, "Could not find workflow for reference " + workflowReference);
                        return row;
                    }

                    workflowDTO.setWorkflowDescription(description);
                    workflowDTO.setStatus(WorkflowStatus.valueOf(status));

                    String workflowType = workflowDTO.getWorkflowType();
                    WorkflowExecutor workflowExecutor = WorkflowExecutorFactory.getInstance()
                            .getWorkflowExecutor(workflowType);

                    workflowExecutor.complete(workflowDTO);
                    row.put("error", row, false);
                    row.put("statusCode", row, 200);
                    row.put("message", row, "Invoked workflow completion successfully.");
                }
            } catch (IllegalArgumentException e){
                row.put("error", row, true);
                row.put("statusCode", row, 500);
                row.put("message", row, "Illegal argument provided. Valid values for status are APPROVED and REJECTED.");
            } catch (AppManagementException e) {
                row.put("error", row, true);
                row.put("statusCode", row, 500);
                row.put("message", row, "Error while resuming workflow. " + e.getMessage());
            }
        }
        return row;
    }

    public static NativeArray jsFunction_getInlineContent(Context cx,
                                                          Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {
        String apiName;
        String version;
        String providerName;
        String docName;
        String content = null;
        NativeArray myn = new NativeArray(0);


        if (args!=null && isStringArray(args)) {
            providerName = AppManagerUtil.replaceEmailDomain((String)args[0]);
            apiName = (String)args[1];
            version = (String)args[2];
            docName = (String)args[3];
            APIIdentifier apiId = new APIIdentifier(providerName, apiName,
                    version);
            try {
                APIConsumer apiConsumer = getAPIConsumer(thisObj);
                content = apiConsumer.getDocumentationContent(apiId, docName);
            } catch (Exception e) {
                handleException("Error while getting Inline Document Content ", e);
            }

            if(content == null){
                content = "";
            }

            NativeObject row = new NativeObject();
            row.put("providerName", row, providerName);
            row.put("apiName", row, apiName);
            row.put("apiVersion", row, version);
            row.put("docName", row, docName);
            row.put("content", row, content);
            myn.put(0, myn, row);

        }
        return myn;
    }

    /*
      * here return boolean with checking all objects in array is string
      */
    public static boolean isStringArray(Object[] args) {
        int argsCount = args.length;
        for (int i = 0; i < argsCount; i++) {
            if (!(args[i] instanceof String)) {
                return false;
            }
        }
        return true;

    }

    public static boolean jsFunction_hasSubscribePermission(Context cx, Scriptable thisObj,
                                                            Object[] args,
                                                            Function funObj)
            throws ScriptException {
        APIConsumer consumer = getAPIConsumer(thisObj);
        if (consumer instanceof UserAwareAPIConsumer) {
            try {
                ((UserAwareAPIConsumer) consumer).checkSubscribePermission();
                return true;
            } catch (AppManagementException e) {
                return false;
            }
        }
        return false;
    }

    public static void jsFunction_addUser(Context cx, Scriptable thisObj,
                                          Object[] args,
	                                      Function funObj) throws AppManagementException {

		if (args != null && isStringArray(args)) {
			String username = args[0].toString();
			String password = args[1].toString();

			AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
			boolean workFlowEnabled =
			                          Boolean.parseBoolean(config.getFirstProperty(AppMConstants.SELF_SIGN_UP_ENABLED));
			if (!workFlowEnabled) {
				handleException("Self sign up has been disabled on this server");
			}
			String serverURL = config.getFirstProperty(AppMConstants.AUTH_MANAGER_URL);
			String tenantDomain =
			                      MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(username));

			UserDTO userDTO = new UserDTO();

			if (args.length > 2) {
				String fields = args[2].toString();

				/* fieldValues will contain values up to last field user entered */
				String fieldValues[] = fields.split("\\|");
				UserFieldDTO[] userFields = getOrderedUserFieldDTO();
				for (int i = 0; i < fieldValues.length; i++) {
					if (fieldValues[i] != null) {
						userFields[i].setFieldValue(fieldValues[i]);
					}
				}
				/* assign empty string for rest of the user fields */
				for (int i = fieldValues.length; i < userFields.length; i++) {
					userFields[i].setFieldValue("");
				}
				userDTO.setUserFields(userFields);

			}

			userDTO.setUserName(username);
			userDTO.setPassword(password);

			try {

				UserRegistrationAdminServiceStub stub =
				                                        new UserRegistrationAdminServiceStub(
				                                                                             null,
				                                                                             serverURL +
				                                                                                     "UserRegistrationAdminService");
				ServiceClient client = stub._getServiceClient();
				Options option = client.getOptions();
				option.setManageSession(true);

				stub.addUser(userDTO);

				WorkflowExecutor userSignUpWFExecutor =
				                                        WorkflowExecutorFactory.getInstance()
				                                                               .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_USER_SIGNUP);

				WorkflowDTO signUpWFDto = new WorkflowDTO();
				signUpWFDto.setWorkflowReference(username);
				signUpWFDto.setStatus(WorkflowStatus.CREATED);
				signUpWFDto.setCreatedTime(System.currentTimeMillis());
				signUpWFDto.setTenantDomain(tenantDomain);

				try {
					int tenantId =
					               ServiceReferenceHolder.getInstance().getRealmService()
					                                     .getTenantManager()
					                                     .getTenantId(tenantDomain);
					signUpWFDto.setTenantId(tenantId);
				} catch (org.wso2.carbon.user.api.UserStoreException e) {
					log.error("Error while loading Tenant ID for given tenant domain :" +
					          tenantDomain);
				}

				signUpWFDto.setExternalWorkflowReference(userSignUpWFExecutor.generateUUID());
				signUpWFDto.setWorkflowType(WorkflowConstants.WF_TYPE_AM_USER_SIGNUP);
				signUpWFDto.setCallbackUrl(userSignUpWFExecutor.getCallbackURL());

				try {
					userSignUpWFExecutor.execute(signUpWFDto);
				} catch (WorkflowException e) {
					log.error("Unable to execute User SignUp Workflow", e);
					removeUser(username, config, serverURL);
					handleException("Unable to execute User SignUp Workflow", e);
				}

			} catch (RemoteException e) {
				handleException(e.getMessage(), e);
			} catch (Exception e) {
				handleException("Error while adding the user: " + username, e);
			}
		} else {
			handleException("Invalid input parameters.");
		}
	}

    private static void removeUser(String username, AppManagerConfiguration config, String serverURL) throws RemoteException, UserAdminUserAdminException {
        UserAdminStub userAdminStub = new UserAdminStub(null, serverURL
                + "UserAdmin");
        String adminUsername = config.getFirstProperty(AppMConstants.AUTH_MANAGER_USERNAME);
        String adminPassword = config.getFirstProperty(AppMConstants.AUTH_MANAGER_PASSWORD);

        CarbonUtils.setBasicAccessSecurityHeaders(adminUsername, adminPassword,
                true, userAdminStub._getServiceClient());
        userAdminStub.deleteUser(username);
    }

    public static boolean jsFunction_isUserExists(Context cx, Scriptable thisObj,
                                                  Object[] args, Function funObj)
            throws ScriptException,
                   AppManagementException {
        if (args==null || args.length == 0) {
            handleException("Invalid input parameters to the isUserExists method");
        }

        String username = (String) args[0];
        boolean exists = false;
        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            UserRealm realm = realmService.getBootstrapRealm();
            UserStoreManager manager = realm.getUserStoreManager();
            if (manager.isExistingUser(username)) {
                exists = true;
            }
        } catch (UserStoreException e) {
            handleException("Error while checking user existence for " + username);
        }
        return exists;
    }

    public static boolean jsFunction_removeSubscription(Context cx, Scriptable thisObj,
                                                        Object[] args,
                                                        Function funObj)
            throws AppManagementException {
        if (args==null|| args.length == 0) {
            handleException("Invalid number of input parameters.");
        }
        String username = (String)args[0];
        int applicationId = ((Number) args[1]).intValue();
        NativeObject apiData = (NativeObject) args[2];
        String provider = AppManagerUtil.replaceEmailDomain((String) apiData.get("provider", apiData));
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);

        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            apiConsumer.removeSubscription(apiId, username, applicationId);
            return true;
        } catch (AppManagementException e) {
            handleException("Error while removing the subscription of" + name + "-" + version, e);
            return false;
      }
    }

    public static boolean jsFunction_removeAPISubscription(Context cx, Scriptable thisObj,
                                                        Object[] args,
                                                        Function funObj)
            throws AppManagementException {
        if (args==null|| args.length == 0) {
            handleException("Invalid number of input parameters.");
        }
        String username = (String)args[3];
        String applicationName = (String)args[2];
        NativeObject apiData = (NativeObject) args[0];

        String provider = AppManagerUtil.replaceEmailDomain((String) apiData.get("provider", apiData));
        String name = (String) apiData.get("name", apiData);
        String version = (String) apiData.get("version", apiData);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);

        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            apiConsumer.removeAPISubscription(apiId, username, applicationName);
            return true;
        } catch (AppManagementException e) {
            handleException("Error while removing the subscription of" + name + "-" + version, e);
            return false;
        }
    }


    public static NativeArray jsFunction_getPublishedAPIsByProvider(Context cx, Scriptable thisObj,
                                                                    Object[] args,
                                                                    Function funObj)
            throws AppManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (args!=null && isStringArray(args)) {
            String providerName = AppManagerUtil.replaceEmailDomain(args[0].toString());
            String username = args[1].toString();
            Set<WebApp> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            boolean isTenantFlowStarted = false;
            try {
            	String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(username));
                if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                	  	isTenantFlowStarted = true;
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }
                apiSet = apiConsumer.getPublishedAPIsByProvider(providerName, username, 5);
            } catch (AppManagementException e) {
                handleException("Error while getting published APIs information of the provider - " +
                        providerName, e);
                return null;
            } catch (Exception e) {
                handleException("Error while getting published APIs information of the provider", e);
                return null;
            } finally {
            	if (isTenantFlowStarted) {
            		PrivilegedCarbonContext.endTenantFlow();
            	}
            }
            if(apiSet!=null){
            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject currentApi = new NativeObject();
                Object apiObject = it.next();
                WebApp api = (WebApp) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                currentApi.put("name", currentApi, apiIdentifier.getApiName());
                currentApi.put("provider", currentApi,
                               AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                currentApi.put("version", currentApi,
                        apiIdentifier.getVersion());
                currentApi.put("description", currentApi, api.getDescription());
                //Rating should retrieve from db
                currentApi.put("rates", currentApi, AppMDAO.getAverageRating(api.getId()));
                if (api.getThumbnailUrl() == null) {
                    currentApi.put("thumbnailurl", currentApi, "images/api-default.png");
                } else {
                    currentApi.put("thumbnailurl", currentApi, AppManagerUtil.prependWebContextRoot(api.getThumbnailUrl()));
                }
                currentApi.put("visibility", currentApi, api.getVisibility());
                currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
                apiArray.put(i, apiArray, currentApi);
                i++;
            }
            }
            return apiArray;

        } else {
            handleException("Invalid types of input parameters.");
            return null;
        }
    }

    /**
     * Given a name of a user the function checks whether the subscriber role is present
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     * @throws AxisFault
     */
    public static NativeObject jsFunction_checkIfSubscriberRoleAttached (Context cx, Scriptable thisObj,
                                                           Object[] args,
                                                           Function funObj) throws
                                                                            AppManagementException, AxisFault {
        String userName = (String) args[0];
        Boolean valid;

        NativeObject row = new NativeObject();

        if(userName!=null){
            AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
            String serverURL = config.getFirstProperty(AppMConstants.AUTH_MANAGER_URL);

            UserAdminStub userAdminStub = new UserAdminStub(null, serverURL + "UserAdmin");
            String adminUsername = config.getFirstProperty(AppMConstants.AUTH_MANAGER_USERNAME);
            String adminPassword = config.getFirstProperty(AppMConstants.AUTH_MANAGER_PASSWORD);

            CarbonUtils.setBasicAccessSecurityHeaders(adminUsername, adminPassword,
                    true, userAdminStub._getServiceClient());
            try {
                    valid = AppManagerUtil.checkPermissionQuietly(userName, AppMConstants.Permissions.WEB_APP_SUBSCRIBE);
                    if(valid){
                        row.put("error", row, false);
                        return row;
                    }
            } catch (Exception e) {
                handleException(e.getMessage(), e);
                row.put("error", row, true);
                row.put("message", row, "Error while checking if " + userName + " has subscriber role.");
                return row;
            }
            row.put("error", row, true);
            row.put("message", row, "User does not have subscriber role.");
            return row;
        }else{
            row.put("error", row, true);
            row.put("message", row, "Please provide a valid username");
            return row;
        }
    }



    public static NativeArray jsFunction_getAPIUsageforSubscriber(Context cx, Scriptable thisObj,
                                                                  Object[] args, Function funObj)
            throws AppManagementException {
        List<APIVersionUserUsageDTO> list = null;
        if (args==null || args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }
        String subscriberName = (String) args[0];
        String period = (String) args[1];

        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(((APIStoreHostObject) thisObj).getUsername());
            list = client.getUsageBySubscriber(subscriberName, period);
        } catch (APIMgtUsageQueryServiceClientException e) {
            handleException("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        } catch (Exception e) {
            handleException("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        }

        Iterator it = null;

        if (list != null) {
            it = list.iterator();
        }
        int i = 0;
        if (it != null) {
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object usageObject = it.next();
                APIVersionUserUsageDTO usage = (APIVersionUserUsageDTO) usageObject;
                row.put("api", row, usage.getApiname());
                row.put("version", row, usage.getVersion());
                row.put("count", row, usage.getCount());
                row.put("costPerAPI", row, usage.getCostPerAPI());
                row.put("cost", row, usage.getCost());
                myn.put(i, myn, row);
                i++;

            }
        }
        return myn;
    }


    public static boolean jsFunction_isCommentActivated() throws AppManagementException {

        boolean commentActivated = false;
        AppManagerConfiguration config =
                ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService()
                        .getAPIManagerConfiguration();

        commentActivated = Boolean.valueOf(config.getFirstProperty(AppMConstants.API_STORE_DISPLAY_COMMENTS));

        if (commentActivated) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean jsFunction_isRatingActivated() throws AppManagementException {

        boolean ratingActivated = false;
        AppManagerConfiguration config =
                ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService()
                        .getAPIManagerConfiguration();

        ratingActivated = Boolean.valueOf(config.getFirstProperty(AppMConstants.API_STORE_DISPLAY_RATINGS));

        if (ratingActivated) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return true if billing enabled else false
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static boolean jsFunction_isBillingEnabled()
            throws AppManagementException {
        AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String billingConfig = config.getFirstProperty(AppMConstants.BILLING_AND_USAGE_CONFIGURATION);
        return Boolean.parseBoolean(billingConfig);
    }

    public static NativeArray jsFunction_getTiers(Context cx, Scriptable thisObj,
                                                  Object[] args,
                                                  Function funObj) {
        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        Set<Tier> tiers;
        try {
            //If tenant domain is present in url we will use it to get available tiers
            if(args.length>0 && args[0]!=null){
                tiers = apiConsumer.getTiers((String)args[0]);
            }
            else {
            	tiers = apiConsumer.getTiers();
            }
            int i = 0;
            for (Tier tier : tiers) {
                NativeObject row = new NativeObject();
                row.put("tierName", row, tier.getName());
                row.put("tierDisplayName", row, tier.getDisplayName());
                row.put("tierDescription", row,
                        tier.getDescription() != null ? tier.getDescription() : "");
                myn.put(i, myn, row);
                i++;
            }
        } catch (Exception e) {
            log.error("Error while getting available tiers", e);
        }
        return myn;
    }
    
    public static NativeArray jsFunction_getDeniedTiers(Context cx, Scriptable thisObj,
			Object[] args,
			Function funObj) throws AppManagementException {

    	NativeArray myn = new NativeArray(0);
    	APIConsumer apiConsumer = getAPIConsumer(thisObj);
    	
    	try {
    		Set<String> tiers = apiConsumer.getDeniedTiers();
    		int i = 0;
    		for (String tier : tiers) {
    			NativeObject row = new NativeObject();
    			row.put("tierName", row, tier);
    			myn.put(i, myn, row);
    			i++;
    		}
    	} catch (Exception e) {
    		log.error("Error while getting available tiers", e);
    	}
    	return myn;
	}
    public static NativeArray jsFunction_getUserFields(Context cx,
                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        UserFieldDTO[] userFields = getOrderedUserFieldDTO();
        NativeArray myn = new NativeArray(0);
        int limit = userFields.length;
        for (int i = 0; i < limit; i++) {
            NativeObject row = new NativeObject();
            row.put("fieldName", row, userFields[i].getFieldName());
            row.put("claimUri", row, userFields[i].getClaimUri());
            row.put("required", row, userFields[i].getRequired());
            myn.put(i, myn, row);
        }
        return myn;
    }

    public static boolean jsFunction_hasUserPermissions(Context cx,
                                                        Scriptable thisObj, Object[] args,
                                                        Function funObj)
            throws ScriptException, AppManagementException {
        if (args!=null && isStringArray(args)) {
            String username = args[0].toString();
            return AppManagerUtil.checkPermissionQuietly(username, AppMConstants.Permissions.WEB_APP_SUBSCRIBE);
        } else {
            handleException("Invalid types of input parameters.");
        }
        return false;
    }

    private static UserFieldDTO[] getOrderedUserFieldDTO() {
        UserRegistrationAdminServiceStub stub;
        UserFieldDTO[] userFields = null;
        try {
            AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
            String url = config.getFirstProperty(AppMConstants.AUTH_MANAGER_URL);
            if (url == null) {
                handleException("WebApp key manager URL unspecified");
            }
            stub = new UserRegistrationAdminServiceStub(null, url + "UserRegistrationAdminService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            userFields = stub.readUserFieldsForUserRegistration(IdentityConstants.INFOCARD_DIALECT);
            Arrays.sort(userFields, new HostObjectUtils.RequiredUserFieldComparator());
            Arrays.sort(userFields, new HostObjectUtils.UserFieldComparator());
        } catch (Exception e) {
            log.error("Error while retrieving User registration Fields", e);
        }
        return userFields;
    }

    private static void updateRolesOfUser(String serverURL, String adminUsername,
                                          String adminPassword, String userName, String role) throws Exception {
        String url = serverURL + "UserAdmin";

        UserAdminStub userAdminStub = new UserAdminStub(url);
        CarbonUtils.setBasicAccessSecurityHeaders(adminUsername, adminPassword,
                true, userAdminStub._getServiceClient());
        FlaggedName[] flaggedNames = userAdminStub.getRolesOfUser(userName, "*", -1);
        List<String> roles = new ArrayList<String>();
        if (flaggedNames != null) {
            for (int i = 0; i < flaggedNames.length; i++) {
                if (flaggedNames[i].getSelected()) {
                    roles.add(flaggedNames[i].getItemName());
                }
            }
        }
        roles.add(role);
        userAdminStub.updateRolesOfUser(userName, roles.toArray(new String[roles.size()]));
    }

    private static long getApplicationAccessTokenValidityPeriodInSeconds(){
        return OAuthServerConfiguration.getInstance().getApplicationAccessTokenValidityPeriodInSeconds();
    }

    public static NativeArray jsFunction_getActiveTenantDomains(Context cx, Scriptable thisObj,
                                                                Object[] args, Function funObj)
            throws AppManagementException {

        try {
            Set<String> tenantDomains = AppManagerUtil.getActiveTenantDomains();
            NativeArray domains = null;
            int i = 0;
            if (tenantDomains == null || tenantDomains.size() == 0) {
                return domains;
            } else {
                domains = new NativeArray(tenantDomains.size());
                for (String tenantDomain : tenantDomains) {
                    domains.put(i, domains, tenantDomain);
                    i++;
                }
            }
            return domains;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new AppManagementException("Error while checking the APIStore is running in tenant mode or not.", e);
        }


    }

    private static boolean isApplicationAccessTokenNeverExpire(long validityPeriod) {
        return validityPeriod == Long.MAX_VALUE;
    }

    public static boolean jsFunction_isEnableEmailUsername(Context cx,
                                                   Scriptable thisObj, Object[] args,
                                                   Function funObj) {
    return Boolean.parseBoolean(CarbonUtils.getServerConfiguration().getFirstProperty("EnableEmailUserName"));
    }

	/**
	 * Returns trusted IdPs for the web application. If the configuration was
	 * set to show only the already trusted IdPs those will be returned, if else
	 * all the trusted IdPs trusted by the app manager will be provided
	 * 
	 * @param cx
	 * @param thisObj
	 * @param args
	 * @param funObj
	 * @return
	 * @throws org.wso2.carbon.appmgt.api.AppManagementException
	 */
	public static NativeArray jsFunction_getTrustedIdPs(Context cx, Scriptable thisObj,
	                                                    Object[] args, Function funObj)
	                                                                                   throws
                                                                                       AppManagementException {
		NativeArray idps = null;
		if (args != null && isStringArray(args)) {
			String webAppName = args[0].toString();
			List<TrustedIdP> idpList =
			                           WebAppIdPFactory.getInstance().getIdpManager()
			                                           .getIdPList(webAppName);
			if (idpList != null && !idpList.isEmpty()) {
				idps = new NativeArray(idpList.size());
				int i = 0;
				for (TrustedIdP idp : idpList) {
					idps.put(i, idps, idp);
					i++;
				}
			}
		} else {
			handleException("Invalid types of input parameters.");
		}

		return idps;

	}

    public static NativeArray jsFunction_getApplications(Context cx,
                                                         Scriptable thisObj, Object[] args,
                                                         Function funObj)
            throws ScriptException, AppManagementException {

        NativeArray myn = new NativeArray(0);
        if (args != null && isStringArray(args)) {
            String username = args[0].toString();
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] applications = apiConsumer.getApplications(new Subscriber(username));
            if (applications != null) {
                int i = 0;
                for (Application application : applications) {
                    NativeObject row = new NativeObject();
                    row.put("name", row, application.getName());
                    row.put("tier", row, application.getTier());
                    row.put("id", row, application.getId());
                    row.put("callbackUrl", row, application.getCallbackUrl());
                    row.put("status", row, application.getStatus());
                    row.put("description", row, application.getDescription());
                    myn.put(i++, myn, row);
                }
            }
        }
        return myn;
    }

}
