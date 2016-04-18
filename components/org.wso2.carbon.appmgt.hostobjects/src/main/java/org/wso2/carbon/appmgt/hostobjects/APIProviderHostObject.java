/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.woden.WSDLFactory;
import org.apache.woden.WSDLReader;
import org.jaggeryjs.hostobjects.file.FileHostObject;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.mozilla.javascript.*;
import org.wso2.carbon.appmgt.api.APIProvider;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.appmgt.api.model.*;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicyPartial;
import org.wso2.carbon.appmgt.api.model.entitlement.EntitlementPolicyValidationResult;
import org.wso2.carbon.appmgt.hostobjects.internal.HostObjectComponent;
import org.wso2.carbon.appmgt.hostobjects.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.APIManagerFactory;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.impl.UserAwareAPIProvider;
import org.wso2.carbon.appmgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.appmgt.impl.utils.APIVersionStringComparator;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.usage.client.APIUsageStatisticsClient;
import org.wso2.carbon.appmgt.usage.client.dto.*;
import org.wso2.carbon.appmgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.user.api.UserStoreException;

import javax.net.ssl.SSLHandshakeException;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings("unused")
public class APIProviderHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(APIProviderHostObject.class);

    private String username;

    private APIProvider apiProvider;

    public String getClassName() {
        return "APIProvider";
    }

    // The zero-argument constructor used for create instances for runtime
    public APIProviderHostObject() throws AppManagementException {

    }

    public APIProviderHostObject(String loggedUser) throws AppManagementException {
        username = loggedUser;
        apiProvider = APIManagerFactory.getInstance().getAPIProvider(loggedUser);
    }

    public String getUsername() {
        return username;
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function Obj,
                                           boolean inNewExpr)
            throws AppManagementException {
        if (args != null && args.length != 0) {
            String username = (String) args[0];
            return new APIProviderHostObject(username);
        }
        return new APIProviderHostObject();
    }

    public APIProvider getApiProvider() {
        return apiProvider;
    }

    private static APIProvider getAPIProvider(Scriptable thisObj) {
        return ((APIProviderHostObject) thisObj).getApiProvider();
    }

    private static void handleException(String msg) throws AppManagementException {
        log.error(msg);
        throw new AppManagementException(msg);
    }

    private static void handleException(String msg, Throwable t) throws AppManagementException {
        log.error(msg, t);
        throw new AppManagementException(msg, t);
    }

    public static NativeObject jsFunction_login(Context cx, Scriptable thisObj,
                                                Object[] args, Function funObj)
            throws AppManagementException {

        if (args==null || args.length == 0 || !isStringValues(args)) {
            handleException("Invalid input parameters to the login method");
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

            UserAdminStub userAdminStub = new UserAdminStub(url + "UserAdmin");
            CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                    true, userAdminStub._getServiceClient());
            //If multiple user stores are in use, and if the user hasn't specified the domain to which
            //he needs to login to
            /* Below condition is commented out as per new multiple users-store implementation,users from
            different user-stores not needed to input domain names when tried to login,APIMANAGER-1392*/
            // if (userAdminStub.hasMultipleUserStores() && !username.contains("/")) {
            //      handleException("Domain not specified. Please provide your username as domain/username");
            // }
        } catch (Exception e) {
            log.error("Error occurred while checking for multiple user stores");
        }

        try {
            AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(null, url + "AuthenticationAdmin");
            ServiceClient client = authAdminStub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);

            String host = new URL(url).getHost();
            if (!authAdminStub.login(username, password, host)) {
                handleException("Login failed! Please recheck the username and password and try again..");
            }
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);

            String usernameWithDomain = AppManagerUtil.getLoggedInUserInfo(sessionCookie,url).getUserName();
            usernameWithDomain = AppManagerUtil.setDomainNameToUppercase(usernameWithDomain);
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            boolean isSuperTenant = false;

            if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                isSuperTenant = true;
            }else {
                usernameWithDomain = usernameWithDomain+"@"+tenantDomain;
            }

            boolean   authorized =
                    AppManagerUtil.checkPermissionQuietly(usernameWithDomain, AppMConstants.Permissions.WEB_APP_CREATE) ||
                            AppManagerUtil.checkPermissionQuietly(usernameWithDomain, AppMConstants.Permissions.WEB_APP_PUBLISH);


            if (authorized) {

                row.put("user", row, usernameWithDomain);
                row.put("sessionId", row, sessionCookie);
                row.put("isSuperTenant", row, isSuperTenant);
                row.put("error", row, false);
            } else {
                handleException("Login failed! Insufficient privileges.");
            }
        } catch (Exception e) {
            row.put("error", row, true);
            row.put("detail", row, e.getMessage());
        }

        return row;
    }

    public static String jsFunction_getAuthServerURL(Context cx, Scriptable thisObj,
                                                     Object[] args, Function funObj)
            throws AppManagementException {

        AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(AppMConstants.AUTH_MANAGER_URL);
        if (url == null) {
            handleException("WebApp key manager URL unspecified");
        }
        return url;
    }

    public static String jsFunction_getHTTPsURL(Context cx, Scriptable thisObj,
                                                Object[] args, Function funObj)
            throws AppManagementException {
        String hostName = CarbonUtils.getServerConfiguration().getFirstProperty("HostName");
        String backendHttpsPort = HostObjectUtils.getBackendPort("https");
        if (hostName == null) {
            hostName = System.getProperty("carbon.local.ip");
        }
        return "https://" + hostName + ":" + backendHttpsPort;

    }

    /**
     * Check whether the application with a given name, provider and version already exists
     *
     * @param ctx Rhino context
     * @param thisObj Scriptable object
     * @param args passing arguments
     * @param funObj Function object
     * @return true if the webapp already exists
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     * @throws ScriptException
     */
    public static boolean jsFunction_isWebappExists(Context ctx, Scriptable thisObj,Object[] args, Function funObj)
            throws AppManagementException, ScriptException {

        if (args == null || args.length != 3) {
            handleException("Invalid number of input parameters.");
        }

        if (args[0] == null || args[1] == null || args[2] == null) {
            handleException("Error while checking for existence of web app: NULL value in expected parameters ->"
                    + "[webapp name:" + args[0] + ",provider:" + args[1] + ",version:" + args[0] + "]");

        }
        String name = (String) args[0];
        String provider = (String) args[1];
        String version = (String) args[2];

        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        APIProvider apiProvider = getAPIProvider(thisObj);

        return apiProvider.isAPIAvailable(apiId);
    }


    /**
     * This method is to functionality of add a new WebApp in WebApp-Provider
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return true if the WebApp was added successfully
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception
     * by org.wso2.carbon.apimgt.api.AppManagementException
     */
    public static boolean jsFunction_addAPI(Context cx, Scriptable thisObj,
                                            Object[] args,
                                            Function funObj)
            throws AppManagementException, ScriptException {
        if (args==null||args.length == 0) {
            handleException("Invalid number of input parameters.");
        }

        boolean success;
        NativeObject apiData = (NativeObject) args[0];
        String provider = String.valueOf(apiData.get("provider", apiData));
        if (provider != null) {
            provider = AppManagerUtil.replaceEmailDomain(provider);
        }
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        String description = (String) apiData.get("description", apiData);
        String endpoint = (String) apiData.get("endpoint", apiData);
        String sandboxUrl = (String) apiData.get("sandbox", apiData);
        String visibility = (String) apiData.get("visibility", apiData);
        String visibleRoles = "";


        if (visibility != null && visibility.equals(AppMConstants.API_RESTRICTED_VISIBILITY)) {
            visibleRoles = (String) apiData.get("visibleRoles", apiData);
        }

        String visibleTenants = "";
        if (visibility != null && visibility.equals(AppMConstants.API_CONTROLLED_VISIBILITY)) {
            visibleTenants = (String) apiData.get("visibleTenants", apiData);
        }

        if (sandboxUrl != null && sandboxUrl.trim().length() == 0) {
            sandboxUrl = null;
        }

        if (endpoint != null && endpoint.trim().length() == 0) {
            endpoint = null;
        }

        if(endpoint != null && !endpoint.startsWith("http") && !endpoint.startsWith("https")){
            endpoint = "http://" + endpoint;
        }
        if(sandboxUrl != null && !sandboxUrl.startsWith("http") && !sandboxUrl.startsWith("https")){
            sandboxUrl = "http://" + sandboxUrl;
        }

        String redirectURL = (String) apiData.get("redirectURL", apiData);
        boolean advertiseOnly = Boolean.parseBoolean((String) apiData.get("advertiseOnly", apiData));
        String apiOwner = (String) apiData.get("apiOwner", apiData);

        if (apiOwner == null || apiOwner.equals("")) {
            apiOwner = provider;
        }

        String wsdl = (String) apiData.get("wsdl", apiData);
        String wadl = (String) apiData.get("wadl", apiData);
        String tags = (String) apiData.get("tags", apiData);

        String subscriptionAvailability = (String) apiData.get("subscriptionAvailability", apiData);
        String subscriptionAvailableTenants = "";
        if (subscriptionAvailability != null && subscriptionAvailability.equals(AppMConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS)) {
            subscriptionAvailableTenants = (String) apiData.get("subscriptionTenants", apiData);
        }

        Set<String> tag = new HashSet<String>();

        if (tags != null) {
            if (tags.indexOf(",") >= 0) {
                String[] userTag = tags.split(",");
                tag.addAll(Arrays.asList(userTag).subList(0, tags.split(",").length));
            } else {
                tag.add(tags);
            }
        }

        String transport = getTransports(apiData);

        String tier = (String) apiData.get("tier", apiData);
        FileHostObject fileHostObject = (FileHostObject) apiData.get("imageUrl", apiData);
        String contextVal = (String) apiData.get("context", apiData);
        String context = contextVal.startsWith("/") ? contextVal : ("/" + contextVal);
        String providerDomain=MultitenantUtils.getTenantDomain(String.valueOf(apiData.get("provider", apiData)));
        if(!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain))
        {
            //Create tenant aware context for WebApp
            context= "/t/"+ providerDomain+context;
        }

        NativeArray uriTemplateArr = (NativeArray) apiData.get("uriTemplateArr", apiData);

        String techOwner = (String) apiData.get("techOwner", apiData);
        String techOwnerEmail = (String) apiData.get("techOwnerEmail", apiData);
        String bizOwner = (String) apiData.get("bizOwner", apiData);
        String bizOwnerEmail = (String) apiData.get("bizOwnerEmail", apiData);

        String endpointSecured = (String) apiData.get("endpointSecured", apiData);
        String endpointUTUsername = (String) apiData.get("endpointUTUsername", apiData);
        String endpointUTPassword = (String) apiData.get("endpointUTPassword", apiData);

        String inSequence =  (String) apiData.get("inSequence", apiData);
        String outSequence = (String) apiData.get("outSequence", apiData);

        String responseCache = (String) apiData.get("responseCache", apiData);
        int cacheTimeOut = AppMConstants.API_RESPONSE_CACHE_TIMEOUT;
        if (AppMConstants.API_RESPONSE_CACHE_ENABLED.equalsIgnoreCase(responseCache)) {
            responseCache = AppMConstants.API_RESPONSE_CACHE_ENABLED;
            try {
                cacheTimeOut = Integer.parseInt ((String) apiData.get("cacheTimeout", apiData));
            } catch (NumberFormatException e) {
                //Ignore NumberFormatException, hence the default value is setting
                log.warn("Cache timeout value is not a number. Hence switching to default cache timeout value", e);
            }
        } else {
            responseCache = AppMConstants.API_RESPONSE_CACHE_DISABLED;
        }

        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        APIProvider apiProvider = getAPIProvider(thisObj);

        if (apiProvider.isAPIAvailable(apiId)) {
            handleException("Error occurred while adding the WebApp. A duplicate WebApp already exists with name - " +
                    name + " and version -" + version);
        }

        WebApp api = new WebApp(apiId);
        NativeArray uriMethodArr = (NativeArray) apiData.get("uriMethodArr", apiData);
        NativeArray authTypeArr = (NativeArray) apiData.get("uriAuthMethodArr", apiData);
        NativeArray throttlingTierArr = (NativeArray) apiData.get("throttlingTierArr", apiData);
        if (uriTemplateArr != null && uriMethodArr != null && authTypeArr != null) {
            if (uriTemplateArr.getLength() == uriMethodArr.getLength()) {
                Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
                for (int i = 0; i < uriTemplateArr.getLength(); i++) {
                    String uriMethods = (String) uriMethodArr.get(i, uriMethodArr);
                    String uriMethodsAuthTypes = (String) authTypeArr.get(i, authTypeArr);
                    String[] uriMethodArray = uriMethods.split(",");
                    String[] authTypeArray = uriMethodsAuthTypes.split(",");
                    String uriMethodsThrottlingTiers = (String) throttlingTierArr.get(i, throttlingTierArr);
                    String[] throttlingTierArray = uriMethodsThrottlingTiers.split(",");
                    for (int k = 0; k < uriMethodArray.length; k++) {
                        for (int j = 0; j < authTypeArray.length; j++) {
                            if (j == k) {
                                URITemplate template = new URITemplate();
                                String uriTemp = (String) uriTemplateArr.get(i, uriTemplateArr);
                                String uriTempVal = uriTemp.startsWith("/") ? uriTemp : ("/" + uriTemp);
                                template.setUriTemplate(uriTempVal);
                                String throttlingTier = throttlingTierArray[j];
                                template.setHTTPVerb(uriMethodArray[k]);
                                String authType = authTypeArray[j];
                                if (authType.equals("Application & Application User")) {
                                    authType = AppMConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                                }
                                if (authType.equals("Application User")) {
                                    authType = "Application_User";
                                }
                                template.setThrottlingTier(throttlingTier);
                                template.setAuthType(authType);
                                template.setResourceURI(endpoint);
                                template.setResourceSandboxURI(sandboxUrl);

                                uriTemplates.add(template);
                                break;
                            }

                        }
                    }

                }
                api.setUriTemplates(uriTemplates);
            }
        }

        api.setDescription(StringEscapeUtils.escapeHtml(description));
        api.setWsdlUrl(wsdl);
        api.setWadlUrl(wadl);
        api.setLastUpdated(new Date());
        api.setUrl(endpoint);
        api.setSandboxUrl(sandboxUrl);
        api.addTags(tag);
        api.setTransports(transport);
        api.setAppOwner(apiOwner);
        api.setAdvertiseOnly(advertiseOnly);
        api.setRedirectURL(redirectURL);
        api.setSubscriptionAvailability(subscriptionAvailability);
        api.setSubscriptionAvailableTenants(subscriptionAvailableTenants);
        api.setResponseCache(responseCache);
        api.setCacheTimeout(cacheTimeOut);


        if(!"none".equals(inSequence)){
            api.setInSequence(inSequence);
        }
        if(!"none".equals(outSequence)){
            api.setOutSequence(outSequence);
        }

        Set<Tier> availableTier = new HashSet<Tier>();
        String[] tierNames;
        if (tier != null) {
            tierNames = tier.split(",");
            for (String tierName : tierNames) {
                availableTier.add(new Tier(tierName));
            }
            api.addAvailableTiers(availableTier);
        }
        api.setStatus(APIStatus.CREATED);
        api.setContext(context);
        api.setBusinessOwner(bizOwner);
        api.setBusinessOwnerEmail(bizOwnerEmail);
        api.setTechnicalOwner(techOwner);
        api.setTechnicalOwnerEmail(techOwnerEmail);
        api.setVisibility(visibility);
        api.setVisibleRoles(visibleRoles != null ? visibleRoles.trim() : null);
        api.setVisibleTenants(visibleTenants != null ? visibleTenants.trim() : null);

        // @todo needs to be validated
        api.setEndpointConfig((String) apiData.get("endpoint_config", apiData));

        //set secured endpoint parameters
        if ("secured".equals(endpointSecured)) {
            api.setEndpointSecured(true);
            api.setEndpointUTUsername(endpointUTUsername);
            api.setEndpointUTPassword(endpointUTPassword);
        }

        checkFileSize(fileHostObject);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(provider));
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            apiProvider.addWebApp(api);

            if (fileHostObject != null && fileHostObject.getJavaScriptFile().getLength() != 0) {
                Icon icon = new Icon(fileHostObject.getInputStream(),
                        fileHostObject.getJavaScriptFile().getContentType());
                String thumbPath = AppManagerUtil.getIconPath(apiId);

                String thumbnailUrl = apiProvider.addIcon(thumbPath, icon);
                api.setThumbnailUrl(AppManagerUtil.prependTenantPrefix(thumbnailUrl, provider));

                /*Set permissions to anonymous role for thumbPath*/
                AppManagerUtil.setResourcePermissions(api.getId().getProviderName(), null, null, thumbPath);
                apiProvider.updateAPI(api);
            }

            success = true;

        } catch (Exception e) {
            handleException("Error while adding the WebApp- " + name + "-" + version, e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return success;

    }

    private static String getTransports(NativeObject apiData) {
        String transportStr = String.valueOf(apiData.get("overview_transports", apiData));
        String transport  = transportStr;
        if (transportStr != null) {
            if ((transportStr.indexOf(",") == 0) || (transportStr.indexOf(",") == (transportStr.length()-1))) {
                transport =transportStr.replace(",","");
            }
        }
        return transport;
    }

    /**
     * Generates entitlement policies for applied policy partials for the given app.
     *
     * @param context Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception by org.wso2.carbon.apimgt.api.AppManagementException
     */
    public static void jsFunction_generateEntitlementPolicies(Context context, Scriptable thisObj,
                                                              Object[] args,
                                                              Function funObj) throws
            AppManagementException {
        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }
        if (args[0] == null) {
            handleException("Error while generating entitlement policy. The application identifier is null");
        }

        NativeObject appIdentifierNativeObject = (NativeObject) args[0];
        APIIdentifier apiIdentifier = new APIIdentifier(
                (String) (appIdentifierNativeObject.get("provider", appIdentifierNativeObject)),
                (String) (appIdentifierNativeObject.get("name", appIdentifierNativeObject)),
                (String) (appIdentifierNativeObject.get("version", appIdentifierNativeObject)));
        APIProvider apiProvider = getAPIProvider(thisObj);
        apiProvider.generateEntitlementPolicies(apiIdentifier);
    }

    /**
     * Retrieve policy content of a given policy
     *
     * @param context Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return policy content
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception by org.wso2.carbon.apimgt.api.AppManagementException
     */
    public static String jsFunction_getEntitlementPolicyContent(Context context, Scriptable thisObj,
                                                                Object[] args,
                                                                Function funObj) throws
                                                                                 AppManagementException {
        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }
        if (args[0] == null) {
            handleException("Error while retrieving entitlement policy content. Entitlement policy id is null");
        }

        String policyId = args[0].toString();
        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.getEntitlementPolicy(policyId);
    }

    /**
     * Get webapp ID of a webapp with a given uuid
     *
     * @param context Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return webapp id
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception by org.wso2.carbon.apimgt.api.AppManagementException
     */
    public static int jsFunction_getWebAppId(Context context, Scriptable thisObj,
                                             Object[] args,
                                             Function funObj) throws AppManagementException {

        if (args == null || args.length != 1) {
            handleException("Invalid number of input parameters.");
        }
        if (args[0] == null) {
            handleException("Error while retrieving webapp id. The webapp uuid is null ");
        }

        String uuid = args[0].toString();
        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.getWebAppId(uuid);
    }

    /**
     * Saves the given entitlement policy partial in database
     *
     * @param context Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return entitlement policy partial id
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception by org.wso2.carbon.apimgt.api.AppManagementException
     */
    public static int jsFunction_saveEntitlementPolicyPartial(Context context, Scriptable thisObj,
                                                              Object[] args,
                                                              Function funObj) throws
                                                                               AppManagementException {
        if (args == null || args.length != 4) {
            handleException("Invalid number of input parameters.");
        }
        if (args[0] == null || args[1] == null ) {
            handleException("Error while saving policy partial. Policy partial content is null");
        }

        String policyPartialName = args[0].toString();
        String policyPartial = args[1].toString();
        String isShared = args[2].toString();
        String policyPartialDesc = args[3].toString();
        boolean isSharedPartial = isShared.equalsIgnoreCase("true");
        String currentUser = ((APIProviderHostObject) thisObj).getUsername();

        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.saveEntitlementPolicyPartial(policyPartialName, policyPartial, isSharedPartial, currentUser,policyPartialDesc);
    }

    /**
     * Saves the given entitlement policy partial in database
     *
     * @param context Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return entitlement policy partial id
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception by org.wso2.carbon.apimgt.api.AppManagementException
     */
    public static int jsFunction_saveBusinessOwner(Context context, Scriptable thisObj,
                                                              Object[] args,
                                                              Function funObj) throws
                                                                               AppManagementException {
        if (args == null || args.length != 6) {
            handleException("Invalid number of input parameters.");
        }
        if (args[0] == null || args[1] == null ) {
            handleException("Error while saving business owner. Owner content is null");
        }

        String ownerName = args[0].toString();
        String ownerMail = args[1].toString();
        String description = args[2].toString();
        String sitelink = args[3].toString();
        String keys = args[4].toString();
        String values = args[5].toString();

        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.saveBusinessOwner(ownerName, ownerMail, description, sitelink, keys, values);
    }

    /**
     * Update the given business owner.
     *
     * @param context Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return entitlement policy partial id
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception by org.wso2.carbon.apimgt.api
     * .AppManagementException
     */
    public static int jsFunction_updateBusinessOwner(Context context, Scriptable thisObj,
                                                   Object[] args,
                                                   Function funObj) throws
                                                                    AppManagementException {
        if (args == null || args.length != 7) {
            handleException("Invalid number of input parameters.");
        }
        if (args[0] == null || args[1] == null ) {
            handleException("Error while saving business owner. Owner content is null");
        }

        String ownerId = args[0].toString();
        String ownerName = args[1].toString();
        String ownerMail = args[2].toString();
        String description = args[3].toString();
        String sitelink = args[4].toString();
        String keys = args[5].toString();
        String values = args[6].toString();

        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.updateBusinessOwner(ownerId, ownerName, ownerMail, description, sitelink, keys, values);
    }

    /**
     * Delete business owner.
     *
     * @param context Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return entitlement policy partial id
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception by org.wso2.carbon.apimgt.api.AppManagementException
     */
    public static int jsFunction_deleteBusinessOwner(Context context, Scriptable thisObj,
                                                     Object[] args,
                                                     Function funObj) throws
                                                                      AppManagementException {
        if (args[0] == null) {
            handleException("Error while deleting business owner. Owner content is null");
        }
        String ownerId = args[0].toString();
        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.deleteBusinessOwner(ownerId);
    }
    /**
     * Update a given entitlement policy partial with the given partial name and partial content
     *
     * @param context Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return if success true else false
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static boolean jsFunction_updateEntitlementPolicyPartial(Context context, Scriptable thisObj,
                                                                    Object[] args,
                                                                    Function funObj) throws
                                                                                     AppManagementException {
        if (args == null || args.length != 4) {
            handleException("Invalid number of input parameters.");
        }
        if (args[0] == null || args[1] == null || args[2] == null) {
            handleException("Error in updating policy parital :NULL value in expected parameters ->"
                    + "[policyPartialId:" + args[0] + ",policyPartial:" + args[1] + ",isShared:" + args[0] + "]");
        }
        int policyPartialId = Integer.parseInt(args[0].toString());
        String policyPartial = args[1].toString();
        String isShared = args[2].toString();
        String policyPartialDesc = args[3].toString();
        boolean isSharedPartial = isShared.equalsIgnoreCase("true");
        String currentUser = ((APIProviderHostObject) thisObj).getUsername();

        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.updateEntitlementPolicyPartial(policyPartialId, policyPartial, currentUser, isSharedPartial, policyPartialDesc);
    }

    /**
     * Delete entitlement policy partial
     *
     * @param context Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return true if success else false
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static boolean jsFunction_deleteEntitlementPolicyPartial(Context context, Scriptable thisObj,
                                                                    Object[] args,
                                                                    Function funObj) throws
            AppManagementException {
        if (args == null || args.length != 1) {
            handleException("Invalid number of input parameters.");
        }
        if (args == null) {
            handleException("Error while deleting entitlement policy partial. The policy partial id is null");
        }

        int policyPartialId = Integer.parseInt(args[0].toString());
        String currentUser = ((APIProviderHostObject) thisObj).getUsername();

        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.deleteEntitlementPolicyPartial(policyPartialId, currentUser);
    }

    /**
     * Retrieve the name and the content of a given policy partial using policy id
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return policy partial name and content
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception by org.wso2.carbon.apimgt.api.AppManagementException
     */
    public static NativeArray jsFunction_getEntitlementPolicyPartial(Context cx, Scriptable thisObj,
                                                                     Object[] args,
                                                                     Function funObj) throws
                                                                                      AppManagementException {
        if (args == null || args.length != 1) {
            handleException("Invalid number of input parameters.");
        }
        if (args[0] == null) {
            handleException("Error while retrieving entitlement policy partial. The policy partial id is null ");
        }
        NativeArray myn = new NativeArray(0);
        int policyPartialId = Integer.parseInt(args[0].toString());

        APIProvider apiProvider = getAPIProvider(thisObj);
        EntitlementPolicyPartial entitlementPolicyPartial = apiProvider.getPolicyPartial(policyPartialId);
        String policyPartialName = entitlementPolicyPartial.getPolicyPartialName();
        String policyPartialContent = entitlementPolicyPartial.getPolicyPartialContent();

        myn.put(0, myn, checkValue(policyPartialName));
        myn.put(1, myn, checkValue(policyPartialContent));
        return myn;
    }

    /**
     * Retrieve the shared policy partials
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return shared policy partials
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static NativeArray jsFunction_getSharedPolicyPartialList(Context cx, Scriptable thisObj,
                                                                    Object[] args,
                                                                    Function funObj) throws
                                                                                     AppManagementException {

        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);
        List<EntitlementPolicyPartial> policyPartialList = apiProvider.getSharedPolicyPartialsList();
        int count = 0;
        for (EntitlementPolicyPartial entitlementPolicyPartial : policyPartialList) {
            NativeObject row = new NativeObject();
            row.put("partialId", row, entitlementPolicyPartial.getPolicyPartialId());
            row.put("partialName", row, entitlementPolicyPartial.getPolicyPartialName());
            row.put("partialContent", row, entitlementPolicyPartial.getPolicyPartialContent());
            row.put("ruleEffect", row, entitlementPolicyPartial.getRuleEffect());
            row.put("isShared", row, entitlementPolicyPartial.isShared());
            row.put("author", row, entitlementPolicyPartial.getAuthor());
            row.put("description", row, entitlementPolicyPartial.getDescription());
            count++;
            myn.put(count, myn, row);
        }

        return myn;
    }

    /**
     * Retrieve business owners
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return shared policy partials
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */


    public static NativeArray jsFunction_getBusinessOwnerList(Context cx, Scriptable thisObj,
                                                                    Object[] args,
                                                                    Function funObj) throws
                                                                                     AppManagementException {

        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);
        List<BusinessOwner> BusinessOwnerList = apiProvider.getBusinessOwnerList();
        int count = 0;
        for (BusinessOwner businessOwner : BusinessOwnerList) {
            NativeObject row = new NativeObject();
            row.put("owner_id", row, businessOwner.getOwner_id());
            row.put("owner_name", row, businessOwner.getOwner_name());
            row.put("owner_email", row, businessOwner.getOwner_mail());
            row.put("owner_desc", row, businessOwner.getOwner_desc());
            row.put("owner_site", row, businessOwner.getOwner_site());
            row.put("keys", row, businessOwner.getKeys());
            row.put("values", row, businessOwner.getValues());
            count++;
            myn.put(count, myn, row);
        }

        return myn;
    }

    /**
     * Get application wise policy group list
     * @param context Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return Policy Group Array
     * @throws AppManagementException on error
     */
    public static NativeArray jsFunction_getPolicyGroupListByApplication(Context context, Scriptable thisObj,
                                                                          Object[] args,
                                                                          Function funObj) throws
            AppManagementException {
        NativeArray policyGroupArr = new NativeArray(0);
        int applicationId = Integer.parseInt(args[0].toString());
        APIProvider apiProvider = getAPIProvider(thisObj);
        List<EntitlementPolicyGroup> policyGroupList = apiProvider.getPolicyGroupListByApplication(applicationId);
        int count = 0;
        String policyPartials;
        for (EntitlementPolicyGroup entitlementPolicyGroup : policyGroupList) {
            NativeObject row = new NativeObject();
            row.put("policyGroupId", row, entitlementPolicyGroup.getPolicyGroupId());
            row.put("policyGroupName", row, entitlementPolicyGroup.getPolicyGroupName());
            row.put("throttlingTier", row, entitlementPolicyGroup.getThrottlingTier());
            row.put("userRoles", row, entitlementPolicyGroup.getUserRoles());
            row.put("allowAnonymous", row, entitlementPolicyGroup.isAllowAnonymous());
            policyPartials = entitlementPolicyGroup.getPolicyPartials().toString();
            row.put("policyPartials", row, policyPartials);
            row.put("policyGroupDesc",row,entitlementPolicyGroup.getPolicyDescription());

            count++;
            policyGroupArr.put(count, policyGroupArr, row);
        }

        return policyGroupArr;
    }


    /**
     * Retrieve the apps which use the given policy partial
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return list of app names
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static NativeArray jsFunction_getAssociatedAppsNameList(Context cx, Scriptable thisObj,
                                                                   Object[] args,
                                                                   Function funObj) throws
                                                                                    AppManagementException {

        if (args == null || args.length != 1) {
            handleException("Invalid number of input parameters.");
        }
        if (args[0] == null) {
            handleException("Error while getting associated app names list for the parital :Policy id is NULL");
        }

        NativeArray myn = new NativeArray(0);
        int policyPartialId = Integer.parseInt(args[0].toString());
        APIProvider apiProvider = getAPIProvider(thisObj);
        List<APIIdentifier> apiIdentifiers = apiProvider.getAssociatedApps(policyPartialId);

        int count = 0;
        for (APIIdentifier identifier : apiIdentifiers) {
            NativeObject row = new NativeObject();
            row.put("appName", row, identifier.getApiName());
            count++;
            myn.put(count, myn, row);
        }

        return myn;
    }

    /**
     * Validate the given entitlement policy partial
     * @param context      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return whether the policy partial is valid or not
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception by org.wso2.carbon.apimgt.api.AppManagementException
     */
    public static EntitlementPolicyValidationResult jsFunction_validateEntitlementPolicyPartial(
            Context context, Scriptable thisObj, Object[] args, Function funObj) throws
                                                                                 AppManagementException {

        if (args == null || args.length != 1) {
            handleException("Invalid number of input parameters.");
        }
        if (args[0] == null) {
            handleException("Error while validating policy partial. The policy partial content is null");
        }
        String policyPartial = args[0].toString();

        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.validateEntitlementPolicyPartial(policyPartial);

    }


    /**
     * Updates given entitlement policies using the relevant entitlement service implementation.
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception by org.wso2.carbon.apimgt.api.AppManagementException
     */
    public static void jsFunction_updateEntitlementPolicies(Context cx, Scriptable thisObj, Object[] args,
                                                            Function funObj) throws
                                                                                 AppManagementException {
        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }

        NativeArray policies = (NativeArray) args[0];
        APIProvider apiProvider = getAPIProvider(thisObj);
        apiProvider.updateEntitlementPolicies(policies);
    }


    public static boolean jsFunction_updateAPI(Context cx, Scriptable thisObj,
                                               Object[] args,
                                               Function funObj) throws AppManagementException {

        if (args==null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }

        NativeObject apiData = (NativeObject) args[0];
        String uuid = (String) apiData.get("id");
        apiData = (NativeObject)apiData.get("attributes",apiData) ;
        boolean success;
        String provider = String.valueOf(apiData.get("overview_provider", apiData));
        if (provider != null) {
            provider = AppManagerUtil.replaceEmailDomain(provider);
        }

        String name = (String) apiData.get("overview_name", apiData);
        String version = (String) apiData.get("overview_version", apiData);
        String transports = (String) apiData.get("overview_transports", apiData);
        String description = (String) apiData.get("overview_description", apiData);
        String endpoint = (String) apiData.get("overview_webAppUrl",apiData);
        String logoutURL = (String) apiData.get("overview_logoutUrl",apiData);
        String ownerName = (String) apiData.get("overview_owner", apiData);


        logoutURL = logoutURL.replace(endpoint, "");
        boolean allowAnonymous = Boolean.parseBoolean((String) apiData.get("overview_allowAnonymous", apiData));
        boolean makeAsDefaultVersion = Boolean.parseBoolean((String) apiData.get("overview_makeAsDefaultVersion",
                                                                                 apiData));
        String treatAsSite = (String) apiData.get("overview_treatAsASite", apiData);
        //FileHostObject thumbnail_fileHostObject = (FileHostObject) apiData.get("images_thumbnail", apiData);
        //String icon = (String) apiData.get("images_icon", apiData);
        String visibleRoles = "";

        if (endpoint != null && endpoint.trim().length() == 0) {
            endpoint = null;
        }

        if(endpoint != null && !endpoint.startsWith("http") && !endpoint.startsWith("https")){
            endpoint = "http://" + endpoint;
        }

        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);
        APIIdentifier oldApiId = new APIIdentifier(provider, name, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        boolean isTenantFlowStarted = false;
        String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(provider));
        if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
            isTenantFlowStarted = true;
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        }

        WebApp oldApi = apiProvider.getAPI(oldApiId);

        String transport = getTransports(apiData);

        //String tier = (String) apiData.get("tier", apiData);
        String contextVal = (String) apiData.get("overview_context", apiData);
        String context = contextVal.startsWith("/") ? contextVal : ("/" + contextVal);
        String providerDomain=MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(String.valueOf(apiData.get("overview_provider", apiData))));
        if(!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain) && !context.contains("/t/"+ providerDomain))
        {
            //Create tenant aware context for WebApp
            context= "/t/"+ providerDomain+context;
        }

        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        WebApp api = new WebApp(apiId);

        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();

        int index=0;

        //If uritemplate_policyPartialIds attribute value != null, set them as API's policy partial list
        if(apiData.get("uritemplate_policyPartialIds") != null){
            api.setPolicyPartials(apiData.get("uritemplate_policyPartialIds").toString());
        }
        if(uuid != null){
            api.setUUID(uuid);
        }

        //set the value for policy group list property
        if (apiData.get("uritemplate_policyGroupIds") != null) {
            api.setPolicyGroups(apiData.get("uritemplate_policyGroupIds").toString());
        }

        //set the value for Java Policy List property
        if (apiData.get("uritemplate_javaPolicyIds") != null) {
            api.setJavaPolicies(apiData.get("uritemplate_javaPolicyIds").toString());
        }

        while((apiData.get("uritemplate_urlPattern"+index)) != null){
            URITemplate uriTemplate = new URITemplate();
            String uritemplate_urlPattern = (String)apiData.get("uritemplate_urlPattern" + index, apiData);
            uriTemplate.setUriTemplate(uritemplate_urlPattern);
            String uritemplate_httpVerb = (String)apiData.get("uritemplate_httpVerb" + index, apiData);
            uriTemplate.setHTTPVerb(uritemplate_httpVerb);
            Integer uritemplate_policyGroupId = Integer.parseInt((String) apiData.get("uritemplate_policygroupid" + index, apiData));
            uriTemplate.setPolicyGroupId((int) (uritemplate_policyGroupId));
            uriTemplates.add(uriTemplate);
            index++;
        }

        api.setUriTemplates(uriTemplates);
        api.setBusinessOwner(ownerName);
        api.setTransports(transport);
        api.setDescription(StringEscapeUtils.escapeHtml(description));
        api.setLastUpdated(new Date());
        api.setUrl(endpoint);
        api.setLogoutURL(logoutURL);

        api.setContext(context);
        Set<Tier> availableTier = new HashSet<Tier>();
        api.setStatus(oldApi.getStatus());
        api.setLastUpdated(new Date());
        api.setTransports(transport);
        api.setAllowAnonymous(allowAnonymous);
        api.setDefaultVersion(makeAsDefaultVersion);
        api.setTreatAsASite(treatAsSite);

        try {
            apiProvider.updateAPI(api);
            boolean hasAPIUpdated=false;
            if(!oldApi.equals(api)){
                hasAPIUpdated=true;
            }

            success = true;
        } catch (Exception e) {
            handleException("Error while updating the WebApp- " + name + "-" + version, e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return success;
    }

    public static boolean jsFunction_updateSubscriptionStatus(Context cx, Scriptable thisObj,
                                                              Object[] args,
                                                              Function funObj)
            throws AppManagementException {
        if (args==null ||args.length == 0) {
            handleException("Invalid input parameters.");
        }

        NativeObject apiData = (NativeObject) args[0];
        boolean success = false;
        String provider = (String) apiData.get("provider", apiData);
        String name = (String) apiData.get("name", apiData);
        String version = (String) apiData.get("version", apiData);
        String newStatus = (String) args[1];
        int appId = Integer.parseInt((String) args[2]);

        try {
            APIProvider apiProvider = getAPIProvider(thisObj);
            APIIdentifier apiId = new APIIdentifier(provider, name, version);
            apiProvider.updateSubscription(apiId, newStatus, appId);
            return true;

        } catch (AppManagementException e) {
            handleException("Error while updating subscription status", e);
            return false;
        }

    }

    private static void checkFileSize(FileHostObject fileHostObject)
            throws ScriptException, AppManagementException {
        if (fileHostObject != null) {
            long length = fileHostObject.getJavaScriptFile().getLength();
            if (length / 1024.0 > 1024) {
                handleException("Image file exceeds the maximum limit of 1MB");
            }
        }
    }

    public static boolean jsFunction_updateTierPermissions(Context cx, Scriptable thisObj,
                                                           Object[] args,
                                                           Function funObj)
            throws AppManagementException {
        if (args == null ||args.length == 0) {
            handleException("Invalid input parameters.");
        }

        NativeObject tierData = (NativeObject) args[0];
        boolean success = false;
        String tierName = (String) tierData.get("tierName", tierData);
        String permissiontype = (String) tierData.get("permissiontype", tierData);
        String roles = (String) tierData.get("roles", tierData);

        try {
            APIProvider apiProvider = getAPIProvider(thisObj);
            apiProvider.updateTierPermissions(tierName, permissiontype, roles);
            return true;

        } catch (AppManagementException e) {
            handleException("Error while updating subscription status", e);
            return false;
        }

    }

    public static NativeArray jsFunction_getTierPermissions(Context cx, Scriptable thisObj,
                                                            Object[] args,
                                                            Function funObj) {
        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);
         /* Create an array with everyone role */
        String everyOneRoleName = ServiceReferenceHolder.getInstance().getRealmService().
                getBootstrapRealmConfiguration().getEveryOneRoleName();
        String defaultRoleArray[] = new String[1];
        defaultRoleArray[0] = everyOneRoleName;
        try {
            Set<Tier> tiers = apiProvider.getTiers();
            Set<TierPermissionDTO> tierPermissions = apiProvider.getTierPermissions();
            int i = 0;
            if (tiers != null) {

                for (Tier tier: tiers) {
                    NativeObject row = new NativeObject();
                    boolean found = false;
                    for (TierPermissionDTO permission : tierPermissions) {
                        if (permission.getTierName().equals(tier.getName())) {
                            row.put("tierName", row, permission.getTierName());
                            row.put("tierDisplayName", row, tier.getDisplayName());
                            row.put("permissionType", row,
                                    permission.getPermissionType());
                            String[] roles = permission.getRoles();
                             /*If no roles defined return default role list*/
                            if (roles == null ||  roles.length == 0) {
                                row.put("roles", row, defaultRoleArray);
                            } else {
                                row.put("roles", row,
                                        permission.getRoles());
                            }
                            found = true;
                            break;
                        }
                    }
            		 /* If no permissions has defined for this tier*/
                    if (!found) {
                        row.put("tierName", row, tier.getName());
                        row.put("tierDisplayName", row, tier.getDisplayName());
                        row.put("permissionType", row,
                                AppMConstants.TIER_PERMISSION_ALLOW);
                        row.put("roles", row, defaultRoleArray);
                    }
                    myn.put(i, myn, row);
                    i++;
                }
            }
        } catch (Exception e) {
            log.error("Error while getting available tiers", e);
        }
        return myn;
    }

    /**
     * This method is to functionality of getting an existing WebApp to WebApp-Provider based
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return a native array
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception by org.wso2.carbon.apimgt.api.AppManagementException
     */

    public static NativeArray jsFunction_getAPI(Context cx, Scriptable thisObj,
                                                Object[] args,
                                                Function funObj) throws AppManagementException {
        NativeArray myn = new NativeArray(0);

        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }
        String providerName = args[0].toString();
        String providerNameTenantFlow = args[0].toString();
        providerName= AppManagerUtil.replaceEmailDomain(providerName);
        String apiName = args[1].toString();
        String version = args[2].toString();

        APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerNameTenantFlow));
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            WebApp api = apiProvider.getAPI(apiId);
            if (api != null) {
                Set<URITemplate> uriTemplates = api.getUriTemplates();
                myn.put(0, myn, checkValue(api.getId().getApiName()));
                myn.put(1, myn, checkValue(api.getDescription()));
                myn.put(2, myn, checkValue(api.getUrl()));
                myn.put(3, myn, checkValue(api.getWsdlUrl()));
                myn.put(4, myn, checkValue(api.getId().getVersion()));
                StringBuilder tagsSet = new StringBuilder("");
                for (int k = 0; k < api.getTags().toArray().length; k++) {
                    tagsSet.append(api.getTags().toArray()[k].toString());
                    if (k != api.getTags().toArray().length - 1) {
                        tagsSet.append(",");
                    }
                }
                myn.put(5, myn, checkValue(tagsSet.toString()));
                StringBuilder tiersSet = new StringBuilder("");
                StringBuilder tiersDisplayNamesSet = new StringBuilder("");
                StringBuilder tiersDescSet = new StringBuilder("");
                Set<Tier> tierSet = api.getAvailableTiers();
                Iterator it = tierSet.iterator();
                int j = 0;
                while (it.hasNext()) {
                    Object tierObject = it.next();
                    Tier tier = (Tier) tierObject;
                    tiersSet.append(tier.getName());
                    tiersDisplayNamesSet.append(tier.getDisplayName());
                    tiersDescSet.append(tier.getDescription());
                    if (j != tierSet.size() - 1) {
                        tiersSet.append(",");
                        tiersDisplayNamesSet.append(",");
                        tiersDescSet.append(",");
                    }
                    j++;
                }

                myn.put(6, myn, checkValue(tiersSet.toString()));
                myn.put(7, myn, checkValue(api.getStatus().toString()));
                myn.put(8, myn, getWebContextRoot(api.getThumbnailUrl()));
                myn.put(9, myn, api.getContext());
                myn.put(10, myn, checkValue(Long.valueOf(api.getLastUpdated().getTime()).toString()));
                myn.put(11, myn, getSubscriberCount(apiId, thisObj));

                if (uriTemplates.size() != 0) {
                    NativeArray uriTempArr = new NativeArray(uriTemplates.size());
                    Iterator i = uriTemplates.iterator();
                    List<NativeArray> uriTemplatesArr = new ArrayList<NativeArray>();
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

                    myn.put(12, myn, uriTempArr);
                }

                myn.put(13, myn, checkValue(api.getSandboxUrl()));
                myn.put(14, myn, checkValue(tiersDescSet.toString()));
                myn.put(15, myn, checkValue(api.getBusinessOwner()));
                myn.put(16, myn, checkValue(api.getBusinessOwnerEmail()));
                myn.put(17, myn, checkValue(api.getTechnicalOwner()));
                myn.put(18, myn, checkValue(api.getTechnicalOwnerEmail()));
                myn.put(19, myn, checkValue(api.getWadlUrl()));
                myn.put(20, myn, checkValue(api.getVisibility()));
                myn.put(21, myn, checkValue(api.getVisibleRoles()));
                myn.put(22, myn, checkValue(api.getVisibleTenants()));
                myn.put(23, myn, checkValue(api.getEndpointUTUsername()));
                myn.put(24, myn, checkValue(api.getEndpointUTPassword()));
                myn.put(25, myn, checkValue(Boolean.toString(api.isEndpointSecured())));
                myn.put(26, myn, AppManagerUtil.replaceEmailDomainBack(checkValue(api.getId().getProviderName())));
                myn.put(27, myn, checkTransport("http",api.getTransports()));
                myn.put(28, myn, checkTransport("https",api.getTransports()));
                myn.put(29, myn, checkValue(api.getInSequence()));
                myn.put(30, myn, checkValue(api.getOutSequence()));

                myn.put(31, myn, checkValue(api.getSubscriptionAvailability()));
                myn.put(32, myn, checkValue(api.getSubscriptionAvailableTenants()));

                //@todo need to handle backword compatibility
                myn.put(33, myn, checkValue(api.getEndpointConfig()));

                myn.put(34, myn, checkValue(api.getResponseCache()));
                myn.put(35, myn, checkValue(Integer.toString(api.getCacheTimeout())));
                myn.put(36, myn, checkValue(tiersDisplayNamesSet.toString()));
            } else {
                handleException("Cannot find the requested WebApp- " + apiName +
                        "-" + version);
            }
        } catch (Exception e) {
            handleException("Error occurred while getting WebApp information of the api- " + apiName +
                    "-" + version, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return myn;
    }

    /**
     * This method returns the user subscribed APPs
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return Native array
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static NativeArray jsFunction_getSubscribedAPIsByUsers(Context cx, Scriptable thisObj,
                                                                  Object[] args,
                                                                  Function funObj)
            throws AppManagementException {
        NativeArray myn = new NativeArray(0);
        String providerName = null;
        String fromDate = null;
        String toDate = null;
        APIProvider apiProvider = getAPIProvider(thisObj);
        if (args == null || args.length == 0) {
            handleException("Invalid input parameters.");
        }

        if (args[0] == null || args[1] == null || args[2] == null) {

            handleException("Error while getting subscribed apps by users :NULL value in expected parameters->" +
                    "[providerName:" + args[0] + ",+fromDate:" + args[1] + ",toDate:" + args[2] + "]");
        }
        boolean isTenantFlowStarted = false;
        try {
            providerName = AppManagerUtil.replaceEmailDomain((String) args[0]);
            fromDate = (String) args[1];
            toDate = (String) args[2];
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            if (providerName != null) {


                Map<String, List> subscribedApps = apiProvider.getSubscribedAPPsByUsers(fromDate, toDate);
                int i = 0;

                for (Map.Entry<String, List> entry : subscribedApps.entrySet()) {
                    //                    List<WebAppInfoDTO> webAppList = entry.getValue();
//                    for (WebAppInfoDTO webApp : webAppList) {
//                        NativeObject row = new NativeObject();
//                        row.put("user", row, entry.getKey());
//                        row.put("apiName", row, webApp.getWebAppName() + "(" + webApp.getProviderId() + ")");
//                        myn.put(i, myn, row);
//                        i++;
//                    }

                    List<Subscriber> subscribers = entry.getValue();
                    for (Subscriber subscriber : subscribers) {
                        NativeObject row = new NativeObject();
                        String[] parts = entry.getKey().split("/");
                        row.put("apiName", row, parts[0]);
                        row.put("version", row, parts[1]);
                        row.put("user", row, subscriber.getName());
                        row.put("subscribeDate", row,(subscriber.getSubscribedDate()+""));
                        myn.put(i, myn, row);
                        i++;
                    }
                }

            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return myn;
    }

    /**
     * This method returns the Subscription(Subscriber) count of APPs
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return Native array
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static NativeArray jsFunction_getSubscriberCountByAPIs(Context cx, Scriptable thisObj,
                                                                  Object[] args,
                                                                  Function funObj)
            throws AppManagementException {
        NativeArray myn = new NativeArray(0);
        String providerName = null;
        String fromDate = null;
        String toDate = null;
        APIProvider apiProvider = getAPIProvider(thisObj);
        if (args == null ||  args.length==0) {
            handleException("Invalid input parameters.");
        }

        if (args[0] == null || args[1] == null || args[2] == null) {

            handleException("Error while getting subscriber count by apps :NULL value in expected parameters->" +
                    "[providerName:" + args[0] + ",+fromDate:" + args[1] + ",toDate:" + args[2] + "]");
        }
        boolean isTenantFlowStarted = false;
        try {
            providerName = AppManagerUtil.replaceEmailDomain((String) args[0]);
            fromDate = (String) args[1];
            toDate = (String) args[2];
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerName));
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            if (providerName != null) {
                AppManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
                Boolean selfSubscriptionStatus = Boolean.valueOf(config.getFirstProperty(
                        AppMConstants.ENABLE_SELF_SUBSCRIPTION));
                Boolean enterpriseSubscriptionStatus = Boolean.valueOf(config.getFirstProperty(
                        AppMConstants.ENABLE_ENTERPRISE_SUBSCRIPTION));

                boolean isSubscriptionOn = (selfSubscriptionStatus || enterpriseSubscriptionStatus);

                // Map consists data as <<appProvider,appName>,subscriptionCount>
                Map<String, Long> subscriptions = apiProvider.getSubscriptionCountByAPPs(providerName, fromDate, toDate,
                                                                                         isSubscriptionOn);

                List<APISubscription> subscriptionData = new ArrayList<APISubscription>();

                for (Map.Entry<String, Long> entry : subscriptions.entrySet()) {

                    String[] parts = entry.getKey().split("/");
                    String part1 = parts[0];


                    String part2 = parts[1];


                    String[] uuidpart = part2.split("&");
                    String version = uuidpart[0];
                    String uuid = uuidpart[1];


                    APISubscription sub = new APISubscription();
                    sub.name = part1 + "(v" + version + ")";
                    sub.version = version;
                    sub.count = entry.getValue();
                    sub.uuid=uuid;
                    subscriptionData.add(sub);


                }
                Collections.sort(subscriptionData, new Comparator<APISubscription>() {
                    public int compare(APISubscription o1, APISubscription o2) {
                        // Note that o2 appears before o1
                        // This is because we need to sort in the descending order
                        return (int) (o2.count - o1.count);
                    }
                });

                int i = 0;
                for (APISubscription sub : subscriptionData) {
                    NativeObject row = new NativeObject();
                    row.put("apiName", row, sub.name);
                    row.put("count", row, sub.count);
                    row.put("version", row, sub.version);
                    row.put("uuid", row, sub.uuid);

                    myn.put(i, myn, row);
                    i++;
                }
            }
        }  finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getTiers(Context cx, Scriptable thisObj,
                                                  Object[] args,
                                                  Function funObj) {
        NativeArray myn = new NativeArray(1);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            Set<Tier> tiers = apiProvider.getTiers();
            int i = 0;
            if (tiers != null) {
                for (Tier tier : tiers) {
                    NativeObject row = new NativeObject();
                    row.put("tierName", row, tier.getName());
                    row.put("tierDisplayName", row, tier.getDisplayName());
                    row.put("tierDescription", row,
                            tier.getDescription() != null ? tier.getDescription() : "");
                    row.put("tierSortKey", row, tier.getRequestPerMinute());
                    myn.put(i, myn, row);
                    i++;
                }
            }
        } catch (Exception e) {
            log.error("Error while getting available tiers", e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getSubscriberCountByAPIVersions(Context cx,
                                                                         Scriptable thisObj,
                                                                         Object[] args,
                                                                         Function funObj)
            throws AppManagementException {
        NativeArray myn = new NativeArray(0);
        String providerName = null;
        String apiName = null;
        APIProvider apiProvider = getAPIProvider(thisObj);
        if (args == null || args.length==0) {
            handleException("Invalid input parameters.");
        }
        boolean isTenantFlowStarted = false;
        try {
            providerName = AppManagerUtil.replaceEmailDomain((String) args[0]);
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerName));
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            apiName = (String) args[1];
            if (providerName != null && apiName != null) {
                Map<String, Long> subscriptions = new TreeMap<String, Long>();
                Set<String> versions = apiProvider.getAPIVersions(AppManagerUtil.replaceEmailDomain(providerName), apiName);
                for (String version : versions) {
                    APIIdentifier id = new APIIdentifier(providerName, apiName, version);
                    WebApp api = apiProvider.getAPI(id);
                    if (api.getStatus() == APIStatus.CREATED) {
                        continue;
                    }
                    long count = apiProvider.getAPISubscriptionCountByAPI(api.getId());
                    if (count == 0) {
                        continue;
                    }
                    subscriptions.put(api.getId().getVersion(), count);
                }

                int i = 0;
                for (Map.Entry<String, Long> entry : subscriptions.entrySet()) {
                    NativeObject row = new NativeObject();
                    row.put("apiVersion", row, entry.getKey());
                    row.put("count", row, entry.getValue().longValue());
                    myn.put(i, myn, row);
                    i++;
                }
            }
        } catch (Exception e) {
            log.error("Error while getting subscribers of the " +
                    "provider: " + providerName + " and WebApp: " + apiName, e);
        }finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return myn;
    }

    private static int getSubscriberCount(APIIdentifier apiId, Scriptable thisObj)
            throws AppManagementException {
        APIProvider apiProvider = getAPIProvider(thisObj);
        Set<Subscriber> subs = apiProvider.getSubscribersOfAPI(apiId);
        Set<String> subscriberNames = new HashSet<String>();
        if (subs != null) {
            for (Subscriber sub : subs) {
                subscriberNames.add(sub.getName());
            }
            return subscriberNames.size();
        } else {
            return 0;
        }
    }

    private static String checkTransport(String compare, String transport)
            throws AppManagementException {
        if(transport!=null){
            List<String> transportList = new ArrayList<String>();
            transportList.addAll(Arrays.asList(transport.split(",")));
            if(transportList.contains(compare)){
                return "checked";
            }else{
                return "";
            }

        }else{
            return "";
        }
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

    /**
     * This method is to functionality of getting all the APIs stored
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return a native array
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception by org.wso2.carbon.apimgt.api.AppManagementException
     */
    public static NativeArray jsFunction_getAllAPIs(Context cx, Scriptable thisObj,
                                                    Object[] args,
                                                    Function funObj)
            throws AppManagementException {
        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);
        /*String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerName));
        if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        }*/
        try {
            List<WebApp> apiList = apiProvider.getAllAPIs();
            if (apiList != null) {
                Iterator it = apiList.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject row = new NativeObject();
                    Object apiObject = it.next();
                    WebApp api = (WebApp) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    row.put("name", row, apiIdentifier.getApiName());
                    row.put("version", row, apiIdentifier.getVersion());
                    row.put("provider", row, AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    row.put("status", row, checkValue(api.getStatus().toString()));
                    row.put("thumb", row, getWebContextRoot(api.getThumbnailUrl()));
                    row.put("subs", row, getSubscriberCount(apiIdentifier, thisObj));
                    myn.put(i, myn, row);
                    i++;

                }
            }
        } catch (Exception e) {
            handleException("Error occurred while getting the APIs", e);
        }
        return myn;
    }

    /**
     * This method is to functionality of getting all the APIs stored per provider
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return a native array
     * @throws org.wso2.carbon.appmgt.api.AppManagementException Wrapped exception by org.wso2.carbon.apimgt.api.AppManagementException
     */
    public static NativeArray jsFunction_getAPIsByProvider(Context cx, Scriptable thisObj,
                                                           Object[] args,
                                                           Function funObj)
            throws AppManagementException {
        NativeArray myn = new NativeArray(0);
        if (args==null ||args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        String providerName = (String) args[0];
        if (providerName != null) {
            APIProvider apiProvider = getAPIProvider(thisObj);
            boolean isTenantFlowStarted = false;
            try {
                String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerName));
                if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }
                List<WebApp> apiList = apiProvider.getAPIsByProvider(AppManagerUtil.replaceEmailDomain(providerName));
                if (apiList != null) {
                    Iterator it = apiList.iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        NativeObject row = new NativeObject();
                        Object apiObject = it.next();
                        WebApp api = (WebApp) apiObject;
                        APIIdentifier apiIdentifier = api.getId();
                        row.put("name", row, apiIdentifier.getApiName());
                        row.put("version", row, apiIdentifier.getVersion());
                        row.put("provider", row, AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                        row.put("lastUpdatedDate", row, api.getLastUpdated().toString());
                        myn.put(i, myn, row);
                        i++;
                    }
                }
            } catch (Exception e) {
                handleException("Error occurred while getting APIs for " +
                        "the provider: " + providerName, e);
            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getSubscribedAPIs(Context cx, Scriptable thisObj,
                                                           Object[] args,
                                                           Function funObj)
            throws AppManagementException {
        String userName = null;
        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);

        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }
        try {
            userName = (String) args[0];
            Subscriber subscriber = new Subscriber(userName);
            Set<WebApp> apiSet = apiProvider.getSubscriberAPIs(subscriber);
            if (apiSet != null) {
                Iterator it = apiSet.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject row = new NativeObject();
                    Object apiObject = it.next();
                    WebApp api = (WebApp) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    row.put("apiName", row, apiIdentifier.getApiName());
                    row.put("version", row, apiIdentifier.getVersion());
                    row.put("provider", row, AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    row.put("updatedDate", row, api.getLastUpdated().toString());
                    myn.put(i, myn, row);
                    i++;
                }
            }
        } catch (Exception e) {
            handleException("Error occurred while getting the subscribed APIs information " +
                    "for the subscriber-" + userName, e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getAllAPIUsageByProvider(Context cx, Scriptable thisObj,
                                                                  Object[] args, Function funObj)
            throws AppManagementException {

        NativeArray myn = new NativeArray(0);
        String providerName = null;
        APIProvider apiProvider = getAPIProvider(thisObj);

        if (args == null || !isStringValues(args)) {
            handleException("Invalid input parameters.");
        }
        try {
            providerName = (String) args[0];
            if (providerName != null) {
                UserApplicationAPIUsage[] apiUsages = apiProvider.getAllAPIUsageByProvider(providerName);
                for (int i = 0; i < apiUsages.length; i++) {
                    NativeObject row = new NativeObject();
                    row.put("userName", row, apiUsages[i].getUserId());
                    row.put("application", row, apiUsages[i].getApplicationName());
                    row.put("appId", row, "" + apiUsages[i].getAppId());
                    row.put("token", row, apiUsages[i].getAccessToken());
                    row.put("tokenStatus", row, apiUsages[i].getAccessTokenStatus());
                    row.put("subStatus", row, apiUsages[i].getSubStatus());

                    StringBuilder apiSet = new StringBuilder("");
                    for (int k = 0; k < apiUsages[i].getApiSubscriptions().length; k++) {
                        apiSet.append(apiUsages[i].getApiSubscriptions()[k].getSubStatus());
                        apiSet.append("::");
                        apiSet.append(apiUsages[i].getApiSubscriptions()[k].getApiId().getApiName());
                        apiSet.append("::");
                        apiSet.append(apiUsages[i].getApiSubscriptions()[k].getApiId().getVersion());
                        if (k != apiUsages[i].getApiSubscriptions().length - 1) {
                            apiSet.append(",");
                        }
                    }
                    row.put("apis", row, apiSet.toString());
                    myn.put(i, myn, row);
                }
            }
        } catch (Exception e) {
            handleException("Error occurred while getting subscribers of the provider: " + providerName, e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getAllDocumentation(Context cx, Scriptable thisObj,
                                                             Object[] args, Function funObj)
            throws AppManagementException {
        String apiName = null;
        String version = null;
        String providerName;
        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);
        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }
        boolean isTenantFlowStarted = false;
        try {
            providerName = (String) args[0];
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerName));
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            apiName = (String) args[1];
            version = (String) args[2];
            APIIdentifier apiId = new APIIdentifier(AppManagerUtil.replaceEmailDomain(providerName), apiName, version);

            List<Documentation> docsList = apiProvider.getAllDocumentation(apiId);
            Iterator it = docsList.iterator();
            int i = 0;
            while (it.hasNext()) {

                NativeObject row = new NativeObject();
                Object docsObject = it.next();
                Documentation doc = (Documentation) docsObject;
                Object objectSourceType = doc.getSourceType();
                String strSourceType = objectSourceType.toString();
                row.put("docName", row, doc.getName());
                row.put("docType", row, doc.getType().getType());
                row.put("sourceType", row, strSourceType);
                row.put("docLastUpdated", row, (Long.valueOf(doc.getLastUpdated().getTime()).toString()));
                //row.put("sourceType", row, doc.getSourceType());
                if (Documentation.DocumentSourceType.URL.equals(doc.getSourceType())) {
                    row.put("sourceUrl", row, doc.getSourceUrl());
                }

                if (Documentation.DocumentSourceType.FILE.equals(doc.getSourceType())) {
                    row.put("filePath", row, doc.getFilePath());
                }

                if (doc.getType() == DocumentationType.OTHER) {
                    row.put("otherTypeName", row, doc.getOtherTypeName());
                }

                row.put("summary", row, doc.getSummary());
                myn.put(i, myn, row);
                i++;

            }

        } catch (Exception e) {
            handleException("Error occurred while getting documentation of the api - " +
                    apiName + "-" + version, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getInlineContent(Context cx,
                                                          Scriptable thisObj, Object[] args,
                                                          Function funObj)
            throws AppManagementException {
        String apiName;
        String version;
        String providerName;
        String docName;
        String content;
        NativeArray myn = new NativeArray(0);

        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }
        providerName = (String) args[0];
        apiName = (String) args[1];
        version = (String) args[2];
        docName = (String) args[3];
        APIIdentifier apiId = new APIIdentifier(AppManagerUtil.replaceEmailDomain(providerName), apiName, version);
        APIProvider apiProvider = getAPIProvider(thisObj);

        boolean isTenantFlowStarted = false;

        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerName));
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            content = apiProvider.getDocumentationContent(apiId, docName);
        } catch (Exception e) {
            handleException("Error while getting Inline Document Content ", e);
            return null;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        NativeObject row = new NativeObject();
        row.put("providerName", row, AppManagerUtil.replaceEmailDomainBack(providerName));
        row.put("apiName", row, apiName);
        row.put("apiVersion", row, version);
        row.put("docName", row, docName);
        row.put("content", row, content);
        myn.put(0, myn, row);
        return myn;
    }

    public static void jsFunction_addInlineContent(Context cx,
                                                   Scriptable thisObj, Object[] args,
                                                   Function funObj)
            throws AppManagementException {
        String apiName;
        String version;
        String providerName;
        String docName;
        String docContent;

        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }
        providerName = (String) args[0];
        apiName = (String) args[1];
        version = (String) args[2];
        docName = (String) args[3];
        docContent = (String) args[4];
        if (docContent != null) {
            docContent = docContent.replaceAll("\n", "");
        }
        APIIdentifier apiId = new APIIdentifier(AppManagerUtil.replaceEmailDomain(providerName), apiName,
                version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerName));
        boolean isTenantFlowStarted = false;
        if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            isTenantFlowStarted = true;
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        }
        try {
            if (docName.equals(AppMConstants.API_DEFINITION_DOC_NAME)) {
                apiProvider.addAPIDefinitionContent(apiId, docName, docContent);
            } else {
                apiProvider.addDocumentationContent(apiId, docName, docContent);
            }
        } catch (AppManagementException e) {
            handleException("Error occurred while adding the content of the documentation- " + docName, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    public static boolean jsFunction_addDocumentation(Context cx, Scriptable thisObj,
                                                      Object[] args, Function funObj)
            throws AppManagementException {
        if (args == null || args.length==0) {
            handleException("Invalid number of parameters or their types.");
        }
        boolean success;
        String providerName = (String) args[0];
        String apiName = (String) args[1];
        String version = (String) args[2];
        String docName = (String) args[3];
        String docType = (String) args[4];
        String summary = (String) args[5];
        String sourceType = (String) args[6];
        FileHostObject fileHostObject = null;
        String sourceURL = null;

        APIIdentifier apiId = new APIIdentifier(AppManagerUtil.replaceEmailDomain(providerName), apiName, version);
        Documentation doc = new Documentation(getDocType(docType), docName);
        if (doc.getType() == DocumentationType.OTHER) {
            doc.setOtherTypeName(args[9].toString());
        }

        if (sourceType.equalsIgnoreCase(Documentation.DocumentSourceType.URL.toString())) {
            doc.setSourceType(Documentation.DocumentSourceType.URL);
            sourceURL = args[7].toString();
        } else if (sourceType.equalsIgnoreCase(Documentation.DocumentSourceType.FILE.toString())) {
            doc.setSourceType(Documentation.DocumentSourceType.FILE);
            fileHostObject = (FileHostObject) args[8];
        } else {
            doc.setSourceType(Documentation.DocumentSourceType.INLINE);
        }

        doc.setSummary(summary);
        doc.setSourceUrl(sourceURL);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {

            if (fileHostObject != null) {
                Icon icon = new Icon(fileHostObject.getInputStream(),
                        fileHostObject.getJavaScriptFile().getContentType());
                String filePath = AppManagerUtil.getDocumentationFilePath(apiId, fileHostObject.getName());
                WebApp api = apiProvider.getAPI(apiId);
                String apiPath= AppManagerUtil.getAPIPath(apiId);
                String visibleRolesList = api.getVisibleRoles();
                String[] visibleRoles = new String[0];
                if (visibleRolesList != null) {
                    visibleRoles = visibleRolesList.split(",");
                }
                AppManagerUtil.setResourcePermissions(api.getId().getProviderName(),
                        api.getVisibility(), visibleRoles,apiPath);
                doc.setFilePath(apiProvider.addIcon(filePath, icon));
            }

        } catch (Exception e) {
            handleException("Error while creating an attachment for Document- " + docName + "-" + version, e);
            return false;
        }
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerName));
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            apiProvider.addDocumentation(apiId, doc);
            success = true;
        } catch (AppManagementException e) {
            handleException("Error occurred while adding the document- " + docName, e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return success;
    }

    public static boolean jsFunction_removeDocumentation(Context cx, Scriptable thisObj,
                                                         Object[] args, Function funObj)
            throws AppManagementException {
        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }
        boolean success;
        String providerName = (String) args[0];
        String apiName = (String) args[1];
        String version = (String) args[2];
        String docName = (String) args[3];
        String docType = (String) args[4];

        APIIdentifier apiId = new APIIdentifier(AppManagerUtil.replaceEmailDomain(providerName), apiName, version);

        APIProvider apiProvider = getAPIProvider(thisObj);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerName));
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            apiProvider.removeDocumentation(apiId, docName, docType);
            success = true;
        } catch (AppManagementException e) {
            handleException("Error occurred while removing the document- " + docName +
                    ".", e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return success;
    }

    public static boolean jsFunction_copyWebappDocumentations(Context cx, Scriptable thisObj,
                                                              Object[] args, Function funObj)
            throws AppManagementException {

        boolean success = false;
        if (args == null || args.length == 0) {
            handleException("Invalid number of parameters or their types.");
        }
        NativeObject appIdentifierNativeObject = (NativeObject) args[0];
        String providerName = (String) appIdentifierNativeObject.get("provider", appIdentifierNativeObject);
        String webappName = (String) appIdentifierNativeObject.get("name", appIdentifierNativeObject);
        String oldVersion = (String) appIdentifierNativeObject.get("oldVersion", appIdentifierNativeObject);
        String newVersion = (String) appIdentifierNativeObject.get("version", appIdentifierNativeObject);
        try {

            APIIdentifier apiIdentifier = new APIIdentifier(providerName, webappName, oldVersion);

            WebApp api = new WebApp(apiIdentifier);
            APIProvider apiProvider = getAPIProvider(thisObj);
            boolean isTenantFlowStarted = false;
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            apiProvider.copyWebappDocumentations(api, newVersion);

            success = true;
        } catch (AppManagementException e) {
            handleException("Error occurred while copying web application : " + webappName +" with new version : "
                    +newVersion, e);
        }
        return success;
    }

    public static NativeArray jsFunction_getSubscribersOfAPI(Context cx, Scriptable thisObj,
                                                             Object[] args, Function funObj)
            throws AppManagementException {
        String apiName;
        String version;
        String providerName;
        NativeArray myn = new NativeArray(0);
        if (args == null || !isStringValues(args)) {
            handleException("Invalid number of parameters or their types.");
        }

        providerName = (String) args[0];
        apiName = (String) args[1];
        version = (String) args[2];

        APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
        Set<Subscriber> subscribers;
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            subscribers = apiProvider.getSubscribersOfAPI(apiId);
            Iterator it = subscribers.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object subscriberObject = it.next();
                Subscriber user = (Subscriber) subscriberObject;
                row.put("username", row, user.getName());
                row.put("tenantID", row, user.getTenantId());
                row.put("emailAddress", row, user.getEmail());
                row.put("subscribedDate", row, checkValue(Long.valueOf(user.getSubscribedDate().getTime()).toString()));
                myn.put(i, myn, row);
                i++;
            }

        } catch (AppManagementException e) {
            handleException("Error occurred while getting subscribers of the WebApp- " + apiName +
                    "-" + version, e);
        }
        return myn;
    }

    public static String jsFunction_isContextExist(Context cx, Scriptable thisObj,
                                                   Object[] args, Function funObj)
            throws AppManagementException {
        Boolean contextExist = false;
        if (args != null && isStringValues(args)) {
            String context = (String) args[0];
            String oldContext = (String) args[1];

            if (context.equals(oldContext)) {
                return contextExist.toString();
            }
            APIProvider apiProvider = getAPIProvider(thisObj);
            try {
                contextExist = apiProvider.isContextExist(context);
            } catch (AppManagementException e) {
                handleException("Error from registry while checking the input context is already exist", e);
            }
        } else {
            handleException("Input context value is null");
        }
        return contextExist.toString();
    }

    private static DocumentationType getDocType(String docType) {
        DocumentationType docsType = null;
        for (DocumentationType type : DocumentationType.values()) {
            if (type.getType().equalsIgnoreCase(docType)) {
                docsType = type;
            }
        }
        return docsType;
    }

    private static boolean isStringValues(Object[] args) {
        int i = 0;
        for (Object arg : args) {

            if (!(arg instanceof String)) {
                return false;

            }
            i++;
        }
        return true;
    }

    private static String checkValue(String input) {
        return input != null ? input : "";
    }


    private static APIStatus getApiStatus(String status) {
        APIStatus apiStatus = null;
        for (APIStatus aStatus : APIStatus.values()) {
            if (aStatus.getStatus().equalsIgnoreCase(status)) {
                apiStatus = aStatus;
            }

        }
        return apiStatus;
    }

    public static NativeArray jsFunction_getProviderAPIVersionUsage(Context cx, Scriptable thisObj,
                                                                    Object[] args, Function funObj)
            throws AppManagementException {
        List<APIVersionUsageDTO> list = null;
        if (args == null || args.length==0) {
            handleException("Invalid input parameters.");
        }
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }
        String providerName = (String) args[0];
        String apiName = (String) args[1];
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(((APIProviderHostObject) thisObj).getUsername());
            list = client.getUsageByAPIVersions(providerName, apiName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIVersionUsage", e);
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
                APIVersionUsageDTO usage = (APIVersionUsageDTO) usageObject;
                row.put("version", row, usage.getVersion());
                row.put("count", row, usage.getCount());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIUsage(Context cx, Scriptable thisObj,
                                                             Object[] args, Function funObj)
            throws AppManagementException {

        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            NativeArray myn = new NativeArray(0);
            return myn;
        }

        List<APIUsageDTO> list = null;
        if (args == null ||  args.length==0) {
            handleException("Invalid number of parameters.");
        }
        String providerName = (String) args[0];
        String fromDate = (String) args[1];
        String toDate = (String) args[2];
        try {
            String userName = ((APIProviderHostObject) thisObj).getUsername();
            String tenantDomainName = MultitenantUtils.getTenantDomain(userName);
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(userName);

            list = client.getUsageByAPIs(providerName, fromDate, toDate, 10, tenantDomainName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            handleException("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        }
        NativeArray myn = new NativeArray(0);
        Iterator it = null;
        if (list != null) {
            it = list.iterator();
        }
        int i = 0;
        if (it != null) {
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object usageObject = it.next();
                APIUsageDTO usage = (APIUsageDTO) usageObject;
                row.put("apiName", row, usage.getApiName());
                row.put("count", row, usage.getCount());
                myn.put(i, myn, row);
                i++;

            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIUserUsage(Context cx, Scriptable thisObj,
                                                                 Object[] args, Function funObj)
            throws AppManagementException {
        List<PerUserAPIUsageDTO> list = null;
        if (args == null ||  args.length==0) {
            handleException("Invalid number of parameters.");
        }
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }
        String providerName = (String) args[0];
        String apiName = (String) args[1];
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(((APIProviderHostObject) thisObj).getUsername());
            list = client.getUsageBySubscribers(providerName, apiName, 10);
        } catch (APIMgtUsageQueryServiceClientException e) {
            handleException("Error while invoking APIUsageStatisticsClient for ProviderAPIUserUsage", e);
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
                PerUserAPIUsageDTO usage = (PerUserAPIUsageDTO) usageObject;
                row.put("user", row, usage.getUsername());
                row.put("count", row, usage.getCount());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getAPIUsageByResourcePath(Context cx, Scriptable thisObj,
                                                                   Object[] args, Function funObj)
            throws AppManagementException {
        List<APIResourcePathUsageDTO> list = null;
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }
        if (args == null ||  args.length==0) {
            handleException("Invalid input parameters.");
        }
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }
        String providerName = (String) args[0];
        String fromDate = (String) args[1];
        String toDate = (String) args[2];

        try {
            APIUsageStatisticsClient client =
                    new APIUsageStatisticsClient(((APIProviderHostObject) thisObj).getUsername());
            list = client.getAPIUsageByResourcePath(providerName, fromDate, toDate);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
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
                APIResourcePathUsageDTO usage = (APIResourcePathUsageDTO) usageObject;
                row.put("apiName", row, usage.getApiName());
                row.put("version", row, usage.getVersion());
                row.put("method", row, usage.getMethod());
                row.put("context", row, usage.getContext());
                row.put("count", row, usage.getCount());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }


    public static NativeArray jsFunction_getAPIUsageByPage(Context cx, Scriptable thisObj,
                                                           Object[] args, Function funObj)
            throws AppManagementException {
        List<APIPageUsageDTO> list = null;
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }
        if (args == null ||  args.length==0) {
            handleException("Invalid input parameters.");
        }
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }
        String providerName = (String) args[0];
        String fromDate = (String) args[1];
        String toDate = (String) args[2];

        try {
            String userName = ((APIProviderHostObject) thisObj).getUsername();
            String tenantDomainName = MultitenantUtils.getTenantDomain(userName);
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(userName);
            list = client.getAPIUsageByPage(providerName, fromDate, toDate, tenantDomainName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
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
                APIPageUsageDTO usage = (APIPageUsageDTO) usageObject;
                row.put("apiName", row, usage.getApiName());
                row.put("version", row, usage.getVersion());
                row.put("userid", row, usage.getUserId());
                row.put("referer",row,usage.getReferer());
                row.put("context", row, usage.getContext());
                row.put("count", row, usage.getCount());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getAPIUsageByUser(Context cx, Scriptable thisObj,
                                                           Object[] args, Function funObj)
            throws AppManagementException {
        List<APIUsageByUserDTO> list = null;
        NativeArray myn = new NativeArray(0);
        if(!HostObjectUtils.checkDataPublishingEnabled()){
            return myn;
        }
        if (args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }
        String providerName = (String) args[0];
        String fromDate = (String) args[1];
        String toDate = (String) args[2];

        try {
            String userName = ((APIProviderHostObject) thisObj).getUsername();
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(userName);
            String tenantDomainName = MultitenantUtils.getTenantDomain(userName);

            list = client.getAPIUsageByUser(providerName, fromDate, toDate, tenantDomainName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
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
                APIUsageByUserDTO usage = (APIUsageByUserDTO) usageObject;
                row.put("apiName", row, usage.getApiName());
                row.put("version", row, usage.getVersion());
                row.put("userId", row, usage.getUserID());
                row.put("context",row,usage.getContext());
                row.put("count", row, usage.getCount());
                row.put("userId", row, usage.getUserID());
                row.put("time", row, usage.getRequestDate());


                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    //cache hit

    public static NativeArray jsFunction_getcashHitMiss(Context cx, Scriptable thisObj,
                                                           Object[] args, Function funObj)
            throws AppManagementException {
        List<APPMCacheCountDTO> list = null;
        NativeArray myn = new NativeArray(0);
        if(!HostObjectUtils.checkDataPublishingEnabled()){
            return myn;
        }
        if (args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }
        String providerName = (String) args[0];
        String fromDate = (String) args[1];
        String toDate = (String) args[2];

        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(((APIProviderHostObject) thisObj).getUsername());
            //APIUsageStatisticsClient client = new APIUsageStatisticsClient("admin");
            list = client.getCacheHitCount(providerName,fromDate,toDate);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        } catch (SQLException e) {
            log.error("Error while executing the SQL", e);
        } catch (XMLStreamException e) {
            log.error("Error while reading the xml-stream", e);
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
                APPMCacheCountDTO usage = (APPMCacheCountDTO) usageObject;
                String userName[]=usage.getApiName().split("--");
                String name[]=userName[1].split(":");
                row.put("apiName", row,  name[0]);
                row.put("version", row, usage.getVersion());
                row.put("fullRequestPath", row, usage.getFullRequestPath());
                row.put("cachetHit",row,usage.getCacheHit());
                row.put("totalRequestCount", row, usage.getTotalRequestCount());
                row.put("time", row, usage.getRequestDate());

                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIVersionUserUsage(Context cx,
                                                                        Scriptable thisObj,
                                                                        Object[] args,
                                                                        Function funObj)
            throws AppManagementException {
        List<PerUserAPIUsageDTO> list = null;
        if (args == null ||  args.length==0) {
            handleException("Invalid number of parameters.");
        }
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }
        String providerName = (String) args[0];
        String apiName = (String) args[1];
        String version = (String) args[2];
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(((APIProviderHostObject) thisObj).getUsername());
            //APIUsageStatisticsClient client = new APIUsageStatisticsClient("admin");
            list = client.getUsageBySubscribers(providerName, apiName, version, 10);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUserUsage", e);
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
                PerUserAPIUsageDTO usage = (PerUserAPIUsageDTO) usageObject;
                row.put("user", row, usage.getUsername());
                row.put("count", row, usage.getCount());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIVersionUserLastAccess(Context cx,
                                                                             Scriptable thisObj,
                                                                             Object[] args,
                                                                             Function funObj)
            throws AppManagementException {
        List<APIVersionLastAccessTimeDTO> list = null;
        if (args == null ||  args.length==0) {
            handleException("Invalid number of parameters.");
        }
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }

        String providerName = (String) args[0];
        String fromDate = (String) args[1];
        String toDate = (String) args[2];
        try {
            String userName = ((APIProviderHostObject) thisObj).getUsername();
            String tenantDomainName = MultitenantUtils.getTenantDomain(userName);
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(userName);

            list = client.getLastAccessTimesByAPI(providerName, fromDate, toDate, 10, tenantDomainName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIVersionLastAccess", e);
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
                APIVersionLastAccessTimeDTO usage = (APIVersionLastAccessTimeDTO) usageObject;
                row.put("api_name", row, usage.getApiName());
                row.put("api_version", row, usage.getApiVersion());
                row.put("user", row, usage.getUser());
                Date date = new Date(String.valueOf(usage.getLastAccessTime()));
                row.put("lastAccess", row, Long.valueOf(date.getTime()).toString());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIServiceTime(Context cx, Scriptable thisObj,
                                                                   Object[] args, Function funObj)
            throws AppManagementException {
        List<APIResponseTimeDTO> list = null;
        if (args == null ||  args.length==0) {
            handleException("Invalid number of parameters.");
        }
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }

        String providerName = (String) args[0];
        String fromDate = (String) args[1];
        String toDate = (String) args[2];

        try {
            String userName = ((APIProviderHostObject) thisObj).getUsername();
            String tenantDomainName = MultitenantUtils.getTenantDomain(userName);
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(userName);
            list = client.getResponseTimesByAPIs(providerName, fromDate, toDate, 10, tenantDomainName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIServiceTime", e);
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
                APIResponseTimeDTO usage = (APIResponseTimeDTO) usageObject;
                row.put("apiName", row, usage.getApiName());
                row.put("context", row, usage.getContext());
                row.put("version", row, usage.getVersion());
                row.put("referer", row, usage.getReferer());
                row.put("serviceTime", row, usage.getServiceTime());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_searchAPIs(Context cx, Scriptable thisObj,
                                                    Object[] args,
                                                    Function funObj) throws AppManagementException {
        NativeArray myn = new NativeArray(0);

        if (args == null || args.length==0) {
            handleException("Invalid number of parameters.");
        }
        String providerName = (String) args[0];
        providerName= AppManagerUtil.replaceEmailDomain(providerName);
        String searchValue = (String) args[1];
        String searchTerm;
        String searchType;

        if (searchValue.contains(":")) {
            if (searchValue.split(":").length > 1) {
                searchType = searchValue.split(":")[0];
                searchTerm = searchValue.split(":")[1];
            } else {
                throw new AppManagementException("Search term is missing. Try again with valid search query.");
            }

        } else {
            searchTerm = searchValue;
            searchType = "default";
        }
        try {
            if ("*".equals(searchTerm) || searchTerm.startsWith("*")) {
                searchTerm = searchTerm.replaceFirst("\\*", ".*");
            }
            APIProvider apiProvider = getAPIProvider(thisObj);

            List<WebApp> searchedList = apiProvider.searchAPIs(searchTerm, searchType, providerName);
            Iterator it = searchedList.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object apiObject = it.next();
                WebApp api = (WebApp) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                row.put("name", row, apiIdentifier.getApiName());
                row.put("provider", row, AppManagerUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                row.put("version", row, apiIdentifier.getVersion());
                row.put("status", row, checkValue(api.getStatus().toString()));
                row.put("thumb", row, getWebContextRoot(api.getThumbnailUrl()));
                row.put("subs", row, apiProvider.getSubscribersOfAPI(api.getId()).size());
                if (providerName != null) {
                    row.put("lastUpdatedDate", row, checkValue(api.getLastUpdated().toString()));
                }
                myn.put(i, myn, row);
                i++;


            }
        } catch (Exception e) {
            handleException("Error occurred while getting the searched WebApp- " + searchValue, e);
        }
        return myn;
    }


    public static boolean jsFunction_hasCreatePermission(Context cx, Scriptable thisObj,
                                                         Object[] args,
                                                         Function funObj) {
        APIProvider provider = getAPIProvider(thisObj);
        if (provider instanceof UserAwareAPIProvider) {
            try {
                ((UserAwareAPIProvider) provider).checkCreatePermission();
                return true;
            } catch (AppManagementException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean jsFunction_hasManageTierPermission(Context cx, Scriptable thisObj,
                                                             Object[] args,
                                                             Function funObj) {
        APIProvider provider = getAPIProvider(thisObj);
        if (provider instanceof UserAwareAPIProvider) {
            try {
                ((UserAwareAPIProvider) provider).checkManageTiersPermission();
                return true;
            } catch (AppManagementException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean jsFunction_hasUserPermissions(Context cx, Scriptable thisObj,
                                                        Object[] args,
                                                        Function funObj)
            throws AppManagementException {
        if (args == null || !isStringValues(args)) {
            handleException("Invalid input parameters.");
        }
        String username = (String) args[0];
        return AppManagerUtil.checkPermissionQuietly(username, AppMConstants.Permissions.WEB_APP_CREATE) ||
                AppManagerUtil.checkPermissionQuietly(username, AppMConstants.Permissions.WEB_APP_PUBLISH);
    }

    public static boolean jsFunction_hasPublishPermission(Context cx, Scriptable thisObj,
                                                          Object[] args,
                                                          Function funObj) {
        APIProvider provider = getAPIProvider(thisObj);
        if (provider instanceof UserAwareAPIProvider) {
            try {
                ((UserAwareAPIProvider) provider).checkPublishPermission();
                return true;
            } catch (AppManagementException e) {
                return false;
            }
        }
        return false;
    }

    public static void jsFunction_loadRegistryOfTenant(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) {
        String tenantDomain = args[0].toString();
        if (tenantDomain != null
                && !org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
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

    public static NativeArray jsFunction_getLifeCycleEvents(Context cx, Scriptable thisObj,
                                                            Object[] args,
                                                            Function funObj)
            throws AppManagementException {
        NativeArray lifeCycles = new NativeArray(0);
        if (args == null) {
            handleException("Invalid input parameters.");
        }
        NativeObject apiData = (NativeObject) args[0];
        String provider = (String) apiData.get("provider", apiData);
        String name = (String) apiData.get("name", apiData);
        String version = (String) apiData.get("version", apiData);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            List<LifeCycleEvent> lifeCycleEvents = apiProvider.getLifeCycleEvents(apiId);
            int i = 0;
            if (lifeCycleEvents != null) {
                for (LifeCycleEvent lcEvent : lifeCycleEvents) {
                    NativeObject event = new NativeObject();
                    event.put("username", event, AppManagerUtil.replaceEmailDomainBack(checkValue(lcEvent.getUserId())));
                    event.put("newStatus", event, lcEvent.getNewStatus() != null ? lcEvent.getNewStatus().toString() : "");
                    event.put("oldStatus", event, lcEvent.getOldStatus() != null ? lcEvent.getOldStatus().toString() : "");

                    event.put("date", event, checkValue(Long.valueOf(lcEvent.getDate().getTime()).toString()));
                    lifeCycles.put(i, lifeCycles, event);
                    i++;
                }
            }
        } catch (AppManagementException e) {
            log.error("Error from registry while checking the input context is already exist", e);
        }
        return lifeCycles;
    }

    private static class APISubscription {
        private String name;
        private long count;
        private String version;
        private String uuid;
    }

    /**
     * Remove a given application
     *
     * @param context Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return true if success else false
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static boolean jsFunction_deleteApp(Context context, Scriptable thisObj,
                                               Object[] args,
                                               Function funObj) throws AppManagementException {
        if (args == null || args.length != 3) {
            handleException("Invalid number of input parameters.");
        }
        if (args[0] == null || args[2] == null) {
            handleException("Error while deleting application. The required parameters are null.");
        }
        boolean isAppDeleted = false;

        NativeJavaObject appIdentifierNativeJavaObject = (NativeJavaObject) args[0];
        APIIdentifier apiIdentifier = (APIIdentifier) appIdentifierNativeJavaObject.unwrap();
        String username = (String) args[1];
        username = AppManagerUtil.replaceEmailDomain(username);
        NativeJavaObject ssoProviderNativeJavaObject = (NativeJavaObject) args[2];
        SSOProvider ssoProvider = (SSOProvider) ssoProviderNativeJavaObject.unwrap();

        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(username));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            APIProvider appProvider = getAPIProvider(thisObj);
            isAppDeleted = appProvider.deleteApp(apiIdentifier, ssoProvider);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return isAppDeleted;
    }

    public static boolean jsFunction_updateDocumentation(Context cx, Scriptable thisObj,
                                                         Object[] args, Function funObj)
            throws AppManagementException {
        if (args == null || args.length==0) {
            handleException("Invalid number of parameters or their types.");
        }
        boolean success;
        String providerName = (String) args[0];
        providerName= AppManagerUtil.replaceEmailDomain(providerName);
        String apiName = (String) args[1];
        String version = (String) args[2];
        String docName = (String) args[3];
        String docType = (String) args[4];
        String summary = (String) args[5];
        String sourceType = (String) args[6];
        String sourceURL = null;
        FileHostObject fileHostObject = null;

        APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
        Documentation doc = new Documentation(getDocType(docType), docName);

        if (doc.getType() == DocumentationType.OTHER) {
            doc.setOtherTypeName(args[9].toString());
        }

        if (sourceType.equalsIgnoreCase(Documentation.DocumentSourceType.URL.toString())) {
            doc.setSourceType(Documentation.DocumentSourceType.URL);
            sourceURL = args[7].toString();
        } else if (sourceType.equalsIgnoreCase(Documentation.DocumentSourceType.FILE.toString())) {
            doc.setSourceType(Documentation.DocumentSourceType.FILE);
            fileHostObject = (FileHostObject) args[8];
        } else {
            doc.setSourceType(Documentation.DocumentSourceType.INLINE);
        }
        doc.setSummary(summary);
        doc.setSourceUrl(sourceURL);
        APIProvider apiProvider = getAPIProvider(thisObj);
        Documentation oldDoc = apiProvider.getDocumentation(apiId, doc.getType(), doc.getName());

        try {

            if (fileHostObject != null && fileHostObject.getJavaScriptFile().getLength() != 0) {
                Icon icon = new Icon(fileHostObject.getInputStream(),
                        fileHostObject.getJavaScriptFile().getContentType());
                String filePath = AppManagerUtil.getDocumentationFilePath(apiId, fileHostObject.getName());
                doc.setFilePath(apiProvider.addIcon(filePath, icon));
            } else if (oldDoc.getFilePath() != null) {
                doc.setFilePath(oldDoc.getFilePath());
            }

        } catch (Exception e) {
            handleException("Error while creating an attachment for Document- " + docName + "-" + version, e);
            return false;
        }
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(providerName));
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            apiProvider.updateDocumentation(apiId, doc);
            success = true;
        } catch (AppManagementException e) {
            handleException("Error occurred while adding the document- " + docName, e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return success;
    }

    public static boolean jsFunction_isAPIOlderVersionExist(Context cx, Scriptable thisObj,
                                                            Object[] args, Function funObj)
            throws AppManagementException {
        boolean apiOlderVersionExist = false;
        if (args==null ||args.length == 0) {
            handleException("Invalid number of input parameters.");
        }

        NativeObject apiData = (NativeObject) args[0];
        String provider = (String) apiData.get("provider", apiData);
        provider= AppManagerUtil.replaceEmailDomain(provider);
        String name = (String) apiData.get("name", apiData);
        String currentVersion = (String) apiData.get("version", apiData);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(provider));
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            APIProvider apiProvider = getAPIProvider(thisObj);
            Set<String> versions = apiProvider.getAPIVersions(provider, name);
            APIVersionStringComparator comparator = new APIVersionStringComparator();
            for (String version : versions) {
                if (comparator.compare(version, currentVersion) < 0) {
                    apiOlderVersionExist = true;
                    break;
                }
            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return apiOlderVersionExist;
    }

    public static String jsFunction_isURLValid(Context cx, Scriptable thisObj,
                                               Object[] args, Function funObj)
            throws AppManagementException {
        String response = "";
        if (args == null || !isStringValues(args)) {
            handleException("Invalid input parameters.");
        }
        String urlVal = (String) args[1];
        String type = (String) args[0];
        if (urlVal != null && !urlVal.equals("")) {
            try {
                if (type != null && type.equals("wsdl")) {
                    validateWsdl(urlVal);
                } else {
                    URL url = new URL(urlVal);
                    URLConnection conn = url.openConnection();
                    conn.connect();
                }
                response = "success";
            } catch (MalformedURLException e) {
                response = "malformed";
            } catch (UnknownHostException e) {
                response = "unknown";
            } catch (ConnectException e) {
                response = "Cannot establish connection to the provided address";
            } catch (SSLHandshakeException e) {
                response = "ssl_error";
            } catch (Exception e) {
                response = e.getMessage();
            }
        }
        return response;

    }

    private boolean resourceMethodMatches(String[] resourceMethod1,
                                          String[] resourceMethod2) {
        for (String m1 : resourceMethod1) {
            for (String m2 : resourceMethod2) {
                if (m1.equals(m2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void validateWsdl(String url) throws AppManagementException {
        try {
            URL wsdl = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(wsdl.openStream()));
            String inputLine;
            boolean isWsdl2 = false;
            boolean isWsdl10 = false;
            StringBuilder urlContent = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                String wsdl2NameSpace = "http://www.w3.org/ns/wsdl";
                String wsdl10NameSpace = "http://schemas.xmlsoap.org/wsdl/";
                urlContent.append(inputLine);
                isWsdl2 = urlContent.indexOf(wsdl2NameSpace) > 0;
                isWsdl10 = urlContent.indexOf(wsdl10NameSpace) > 0;
            }
            in.close();
            if (isWsdl10) {
                javax.wsdl.xml.WSDLReader wsdlReader11 = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
                wsdlReader11.readWSDL(url);
            } else if (isWsdl2) {
                WSDLReader wsdlReader20 = WSDLFactory.newInstance().newWSDLReader();
                wsdlReader20.readWSDL(url);
            } else {
                handleException("URL is not in format of wsdl1/wsdl2");
            }
        } catch (Exception e) {
            handleException("Error occurred while validating the Wsdl", e);
        }
    }

    private static String getWebContextRoot(String postfixUrl) {
        String webContext = CarbonUtils.getServerConfiguration().getFirstProperty("WebContextRoot");
        if (postfixUrl != null && webContext != null && !webContext.equals("/")) {
            postfixUrl = webContext + postfixUrl;
        }
        return postfixUrl;
    }


    public static NativeArray jsFunction_searchAccessTokens(Context cx, Scriptable thisObj,
                                                            Object[] args,
                                                            Function funObj) throws AppManagementException {
        NativeObject tokenInfo;
        NativeArray tokenInfoArr = new NativeArray(0);
        if (args == null || !isStringValues(args)) {
            handleException("Invalid input parameters.");
        }
        String searchValue = (String) args[0];
        String searchTerm;
        String searchType;
        APIProvider apiProvider = getAPIProvider(thisObj);
        Map<Integer, APIKey> tokenData = null;
        String loggedInUser = ((APIProviderHostObject) thisObj).getUsername();

        if (searchValue.contains(":")) {
            searchTerm = searchValue.split(":")[1];
            searchType = searchValue.split(":")[0];
            if ("*".equals(searchTerm) || searchTerm.startsWith("*")) {
                searchTerm = searchTerm.replaceFirst("\\*", ".*");
            }
            tokenData = apiProvider.searchAccessToken(searchType, searchTerm, loggedInUser);
        } else {
            //Check whether old access token is already available
            if (apiProvider.isApplicationTokenExists(searchValue)) {
                APIKey tokenDetails = apiProvider.getAccessTokenData(searchValue);
                if (tokenDetails.getAccessToken() == null) {
                    throw new AppManagementException("The requested access token is already revoked or No access token available as per requested.");
                }
                tokenData = new HashMap<Integer, APIKey>();
                tokenData.put(0, tokenDetails);
            } else {
                if ("*".equals(searchValue) || searchValue.startsWith("*")) {
                    searchValue = searchValue.replaceFirst("\\*", ".*");
                }
                tokenData = apiProvider.searchAccessToken(null, searchValue, loggedInUser);
            }
        }
        if (tokenData != null && tokenData.size() != 0) {
            for (int i = 0; i < tokenData.size(); i++) {
                tokenInfo = new NativeObject();
                tokenInfo.put("token", tokenInfo, tokenData.get(i).getAccessToken());
                tokenInfo.put("user", tokenInfo, tokenData.get(i).getAuthUser());
                tokenInfo.put("scope", tokenInfo, tokenData.get(i).getTokenScope());
                tokenInfo.put("createTime", tokenInfo, tokenData.get(i).getCreatedDate());
                if (tokenData.get(i).getValidityPeriod() == Long.MAX_VALUE) {
                    tokenInfo.put("validTime", tokenInfo, "Won't Expire");
                } else {
                    tokenInfo.put("validTime", tokenInfo, tokenData.get(i).getValidityPeriod());
                }
                tokenInfo.put("consumerKey", tokenInfo, tokenData.get(i).getConsumerKey());
                tokenInfoArr.put(i, tokenInfoArr, tokenInfo);
            }
        } else {
            throw new AppManagementException("The requested access token is already revoked or No access token available as per requested.");
        }

        return tokenInfoArr;
    }

    public static NativeArray jsFunction_getAPIResponseFaultCount(Context cx, Scriptable thisObj,
                                                                  Object[] args, Function funObj)
            throws AppManagementException {
        List<APIResponseFaultCountDTO> list = null;
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }
        if (args == null || args.length==0) {
            handleException("Invalid number of parameters.");
        }
        String providerName = (String) args[0];
        String fromDate = (String) args[1];
        String toDate = (String) args[2];
        try {
            APIUsageStatisticsClient client =
                    new APIUsageStatisticsClient(((APIProviderHostObject) thisObj).getUsername());
            list = client.getAPIResponseFaultCount(providerName,fromDate,toDate);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        }

        Iterator it = null;
        if (list != null) {
            it = list.iterator();
        }
        int i = 0;
        if (it != null) {
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object faultObject = it.next();
                APIResponseFaultCountDTO fault = (APIResponseFaultCountDTO) faultObject;
                row.put("apiName", row, fault.getApiName());
                row.put("version", row, fault.getVersion());
                row.put("context", row, fault.getContext());
                row.put("count", row, fault.getCount());
                row.put("faultPercentage", row, fault.getFaultPercentage());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getAPIFaultyAnalyzeByTime(Context cx, Scriptable thisObj,
                                                                   Object[] args, Function funObj)
            throws AppManagementException {
        List<APIResponseFaultCountDTO> list = null;
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }
        if (args == null || args.length==0) {
            handleException("Invalid number of parameters.");
        }
        String providerName = (String) args[0];
        try {
            APIUsageStatisticsClient client =
                    new APIUsageStatisticsClient(((APIProviderHostObject) thisObj).getUsername());
            list = client.getAPIFaultyAnalyzeByTime(providerName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        }

        Iterator it = null;
        if (list != null) {
            it = list.iterator();
        }
        int i = 0;
        if (it != null) {
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object faultObject = it.next();
                APIResponseFaultCountDTO fault = (APIResponseFaultCountDTO) faultObject;
                long faultTime = Long.parseLong(fault.getRequestTime());
                row.put("apiName", row, fault.getApiName());
                row.put("version", row, fault.getVersion());
                row.put("context", row, fault.getContext());
                row.put("requestTime", row, faultTime);
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getFirstAccessTime(Context cx, Scriptable thisObj,
                                                            Object[] args, Function funObj)
            throws AppManagementException {

        if(!HostObjectUtils.checkDataPublishingEnabled()){
            NativeArray myn = new NativeArray(0);
            return myn;
        }

        List<String> list = null;
        if (args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        String providerName = (String) args[0];
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(((APIProviderHostObject) thisObj).getUsername());
            //APIUsageStatisticsClient client = new APIUsageStatisticsClient("admin");
            list = client.getFirstAccessTime(providerName,1);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        }
        NativeArray myn = new NativeArray(0);
        NativeObject row = new NativeObject();
        row.put("year",row,list.get(0).toString());
        row.put("month",row,list.get(1).toString());
        row.put("day",row,list.get(2).toString());
        myn.put(0,myn,row);
        return myn;
    }

    public static boolean jsFunction_validateRoles(Context cx,
                                                   Scriptable thisObj, Object[] args,
                                                   Function funObj) {
        if (args == null || args.length==0) {
            return false;
        }

        boolean valid=false;
        String inputRolesSet = (String)args[0];
        String username=  (String) args[1];
        String[] inputRoles=null;
        if (inputRolesSet != null) {
            inputRoles = inputRolesSet.split(",");
        }

        try {
            String[] roles= AppManagerUtil.getRoleNames(username);

            if (roles != null && inputRoles != null) {
                for (String inputRole : inputRoles) {
                    for (String role : roles) {
                        valid= (inputRole.equals(role));
                        if(valid){ //If we found a match for the input role,then no need to process the for loop further
                            break;
                        }
                    }
                    //If the input role doesn't match with any of the role existing in the system
                    if(!valid){
                        return valid;
                    }

                }
                return valid;
            }
        }catch (Exception e) {
            log.error("Error while validating the input roles.",e);
        }

        return valid;
    }

    public static NativeArray jsFunction_getExternalAPIStores(Context cx,
                                                              Scriptable thisObj, Object[] args,
                                                              Function funObj)
            throws AppManagementException {
        Set<AppStore> apistoresList = AppManagerUtil.getExternalAPIStores();
        NativeArray myn = new NativeArray(0);
        if (apistoresList == null) {
            return null;
        } else {
            Iterator it = apistoresList.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object apistoreObject = it.next();
                AppStore apiStore = (AppStore) apistoreObject;
                row.put("displayName", row, apiStore.getDisplayName());
                row.put("name", row, apiStore.getName());
                row.put("endpoint", row, apiStore.getEndpoint());

                myn.put(i, myn, row);
                i++;

            }
            return myn;
        }

    }

    /**
     * Retrieves custom sequences from registry
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static NativeArray jsFunction_getCustomOutSequences(Context cx, Scriptable thisObj,
                                                               Object[] args, Function funObj)
            throws AppManagementException {
        APIProvider apiProvider = getAPIProvider(thisObj);
        List<String> sequenceList = apiProvider.getCustomOutSequences();

        NativeArray myn = new NativeArray(0);
        if (sequenceList == null) {
            return null;
        } else {
            for (int i = 0; i < sequenceList.size(); i++) {
                myn.put(i, myn, sequenceList.get(i));
            }
            return myn;
        }

    }

    /**
     * Retrieves custom sequences from registry
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static NativeArray jsFunction_getCustomInSequences(Context cx, Scriptable thisObj,
                                                              Object[] args, Function funObj)
            throws AppManagementException {
        APIProvider apiProvider = getAPIProvider(thisObj);
        List<String> sequenceList = apiProvider.getCustomInSequences();

        NativeArray myn = new NativeArray(0);
        if (sequenceList == null) {
            return null;
        } else {
            for (int i = 0; i < sequenceList.size(); i++) {
                myn.put(i, myn, sequenceList.get(i));
            }
            return myn;
        }

    }

    /**
     * Retrieves TRACKING_CODE sequences from APM_APP Table
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     */
    public static String jsFunction_getTrackingID(Context cx, Scriptable thisObj,
                                                  Object[] args, Function funObj) throws AppManagementException {
        String uuid = (String) args[0];
        APIProvider apiProvider =  getAPIProvider(thisObj);
        return apiProvider.getTrackingID(uuid);
    }

    /**
     * This method returns the endpoint for the webapps
     *
     * @param ctx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return Native array
     * @throws org.wso2.carbon.appmgt.api.AppManagementException
     *
     */
    public static NativeArray jsFunction_getAppsForTenantDomain(Context ctx, Scriptable thisObj,
                                                           Object[] args,
                                                           Function funObj)
            throws AppManagementException {
        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);

        String tenantDomain = null;
        try {
            tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            List<WebApp> apiList = apiProvider.getAppsWithEndpoint(tenantDomain);
            if (apiList != null) {
                Iterator it = apiList.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject row = new NativeObject();
                    Object appObject = it.next();
                    WebApp app = (WebApp) appObject;
                    APIIdentifier apiIdentifier = app.getId();
                    row.put("name", row, apiIdentifier.getApiName());

                    // This WebApp is for read the registry values.
                    WebApp tempApp = apiProvider.getAPI(apiIdentifier);
                    row.put("version", row, apiIdentifier.getVersion());
                    row.put("endpoint", row, tempApp.getUrl());
                    myn.put(i, myn, row);
                    i++;
                }
            }
        } catch (AppManagementException e) {
            handleException("Error occurred while getting the application endpoints for the tenant domain "
                    + tenantDomain, e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getAppsByPopularity(Context ctx, Scriptable hostObj, Object[] args,
                                                             Function funObj) throws AppManagementException {
        List<AppHitsStatsDTO> appStatsList = null;
        NativeArray popularApps = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(hostObj);
        if (!AppManagerUtil.isUIActivityBAMPublishEnabled()) {
            return popularApps;
        }
        if (args.length != 3) {
            handleException("Invalid number of parameters!");
        }
        String providerName = (String) args[0];
        String fromDate = (String) args[1];
        String toDate = (String) args[2];

        boolean isTenantFlowStarted = false;

        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(
                    AppManagerUtil.replaceEmailDomainBack(providerName));
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(
                    tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                        tenantDomain, true);
            }
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
            APIUsageStatisticsClient client =
                    new APIUsageStatisticsClient(((APIProviderHostObject) hostObj).getUsername());
            appStatsList = client.getAppHitsOverTime(fromDate, toDate, tenantId);
        } catch (APIMgtUsageQueryServiceClientException e) {
            handleException("Error occurred while invoking APPUsageStatisticsClient " +
                        "for ProviderAPPUsage", e);
        }

        if (appStatsList != null) {
            for (int i = 0; i < appStatsList.size(); i++) {
                NativeObject row = new NativeObject();
                Object usageObject = appStatsList.get(i);
                AppHitsStatsDTO usage = (AppHitsStatsDTO) usageObject;
                row.put("AppName", row, usage.getAppName());
                row.put("Context", row, usage.getContext());
                row.put("TotalHits", row, usage.getTotalHitCount());
                List<UserHitsPerAppDTO> userHits = usage.getUserHitsList();
                if (userHits != null) {
                    NativeArray userHitsArray = new NativeArray(userHits.size());
                    for (int j = 0; j < userHits.size(); j++) {
                        NativeObject userHitRow = new NativeObject();
                        Object userHitsObject = userHits.get(j);
                        UserHitsPerAppDTO userHitsPerAppDTO = (UserHitsPerAppDTO) userHitsObject;
                        userHitRow.put("UserName", userHitRow, userHitsPerAppDTO.getUserName());
                        userHitRow.put("Hits", userHitRow, userHitsPerAppDTO.getUserHitsCount());
                        userHitsArray.put(j, userHitsArray, userHitRow);
                    }
                    row.put("UserHits", row, userHitsArray);
                }
                popularApps.put(i, popularApps, row);
            }
        }
        return popularApps;
    }

    /**
     * This methods update(add/remove) the external app stores for given web app
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @throws AppManagementException
     */
    public static void jsFunction_updateExternalAppStores(Context cx, Scriptable thisObj, Object[] args,
                                                          Function funObj)
            throws AppManagementException {
        if (args == null || args.length != 4) {
            handleException("Invalid number of parameters to the updateExternalAPPStores method,Expected number of" +
                    "parameters " + 4);
        }

        if (!(args[3] instanceof NativeArray)) {
            handleException("Invalid input parameter, 4th parameter  should be a instance of NativeArray");
        }
        String provider = (String) args[0];
        if (provider != null) {
            provider = AppManagerUtil.replaceEmailDomain(provider);
        }
        String name = (String) args[1];
        String version = (String) args[2];
        //Getting selected external App stores from UI and publish app to them.
        NativeArray externalAppStores = (NativeArray) args[3];

        if (log.isDebugEnabled()) {
            String msg = String.format("Update external stores  for web app ->" +
                    " app provider : %s, app name :%s, app version : %s", provider, name, version);
            log.debug(msg);
        }

        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            APIProvider appProvider = getAPIProvider(thisObj);
            APIIdentifier identifier = new APIIdentifier(provider, name, version);
            WebApp webApp = appProvider.getAPI(identifier);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantManager().getTenantId(tenantDomain);
            //Check if no external AppStore selected from UI
            if (externalAppStores != null) {
                Set<AppStore> inputStores = new HashSet<AppStore>();
                for (Object store : externalAppStores) {
                    inputStores.add(AppManagerUtil.getExternalAppStore((String) store, tenantId));
                }
                appProvider.updateAppsInExternalAppStores(webApp, inputStores);
            }
        } catch (UserStoreException e) {
            handleException("Error while updating external app stores", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * This method returns the published and unpublished external stores.
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws AppManagementException
     */
    public static NativeArray jsFunction_getExternalAppStoresList(Context cx, Scriptable thisObj, Object[] args,
                                                                  Function funObj)
            throws AppManagementException {

        if (args == null || args.length != 3) {
            handleException("Invalid number of parameters to the getExternalAPPStoresList method,Expected number of" +
                    "parameters : " + 3);
        }

        if (!isStringValues(args)) {
            handleException("Input parameters are not type of String");
        }

        String provider = (String) args[0];
        String appName = (String) args[1];
        String appVersion = (String) args[2];

        if (log.isDebugEnabled()) {
            String msg = String.format("Getting external store details for web app ->" +
                    " app provider : %s, app name :%s, app version : %s", provider, appName, appVersion);
            log.debug(msg);
        }

        APIProvider appProvider = getAPIProvider(thisObj);
        APIIdentifier identifier = new APIIdentifier(provider, appName, appVersion);
        Set<AppStore> storesSet = appProvider.getExternalAppStores(identifier);
        NativeArray appStoresArray = new NativeArray(0);

        if (storesSet != null && storesSet.size() != 0) {
            int i = 0;
            for (AppStore store : storesSet) {
                NativeObject storeObject = new NativeObject();
                storeObject.put("name", storeObject, store.getName());
                storeObject.put("displayName", storeObject, store.getDisplayName());
                storeObject.put("published", storeObject, store.isPublished());
                appStoresArray.put(i, appStoresArray, storeObject);
                i++;
            }
        }
        return appStoresArray;
    }

    /**
     * Get default version of a WebApp by Application Name and Provider and filtered by lifecycle state (isPublished).
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return Default version of the WebApp relevant to given Name and Provider
     * @throws AppManagementException
     */
    public static String jsFunction_getDefaultVersion(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws AppManagementException {
        NativeArray myn = new NativeArray(0);
        if (args == null || args.length != 3) {
            handleException("Invalid input parameters. Expecting Name, Provider and Publish State.");
        }
        String appName = (String) args[0];
        String provider = (String) args[1];
        AppDefaultVersion appStatus = null;

        try {
            appStatus = AppDefaultVersion.valueOf((String) args[2]);
        } catch (IllegalArgumentException e) {
            handleException(String.format("There is no value with name '%s' in Enum %s", (String) args[2],
                                          AppDefaultVersion.class.getName()
            ));
        }

        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.getDefaultVersion(appName, provider, appStatus);
    }

    /**
     * Check if the given WebApp is the default version.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return true if given app is the default version
     * @throws AppManagementException
     */
    public static boolean jsFunction_isDefaultVersion(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws AppManagementException {
        NativeArray myn = new NativeArray(0);
        if (args == null || args.length != 1) {
            handleException("Invalid input parameters. Expecting APIIdentifier.");
        }
        NativeJavaObject appIdentifierNativeJavaObject = (NativeJavaObject) args[0];
        APIIdentifier apiIdentifier = (APIIdentifier) appIdentifierNativeJavaObject.unwrap();
        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.isDefaultVersion(apiIdentifier);
    }

    /**
     * Check if the WebApp has more versions or not.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws AppManagementException
     */
    public static boolean jsFunction_hasMoreVersions(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws AppManagementException {
        NativeArray myn = new NativeArray(0);
        if (args == null || args.length != 1) {
            handleException("Invalid input parameters. Expecting APIIdentifier.");
        }
        NativeJavaObject appIdentifierNativeJavaObject = (NativeJavaObject) args[0];
        APIIdentifier apiIdentifier = (APIIdentifier) appIdentifierNativeJavaObject.unwrap();
        APIProvider apiProvider = getAPIProvider(thisObj);
        return apiProvider.hasMoreVersions(apiIdentifier);
    }

    /**
     * Get WebApp details by UUID.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return Asset basic details
     * @throws AppManagementException
     */
    public static NativeObject jsFunction_getAppDetailsFromUUID(Context cx, Scriptable thisObj, Object[] args,
                                                               Function funObj)
            throws AppManagementException {
        NativeObject webAppNativeObj = new NativeObject();
        if (args == null || args.length != 1) {
            handleException("Invalid input parameters. Expecting UUID.");
        }
        String uuid = (String) args[0];
        APIProvider apiProvider = getAPIProvider(thisObj);
        WebApp api = apiProvider.getAppDetailsFromUUID(uuid);

        if (api != null) {
            webAppNativeObj.put("name", webAppNativeObj, api.getId().getApiName());
            webAppNativeObj.put("provider", webAppNativeObj, api.getId().getProviderName());
            webAppNativeObj.put("version", webAppNativeObj, api.getId().getVersion());
            webAppNativeObj.put("context", webAppNativeObj, api.getContext());
        } else {
            handleException("Cannot find the requested WebApp related to UUID - " + uuid);
        }
        return webAppNativeObj;
    }

    /**
     * Returns the current subscription configuration defined in app-manager.xml.
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return Subscription Configuration
     * @throws AppManagementException
     */
    public static NativeObject jsFunction_getSubscriptionConfiguration(Context cx, Scriptable thisObj, Object[] args,
                                                                       Function funObj) throws AppManagementException {
        Map<String, Boolean> subscriptionConfigurationData = HostObjectUtils.getSubscriptionConfiguration();
        NativeObject subscriptionConfiguration = new NativeObject();
        for (Map.Entry<String, Boolean> entry : subscriptionConfigurationData.entrySet()) {
            subscriptionConfiguration.put(entry.getKey(), subscriptionConfiguration, entry.getValue().booleanValue());
        }
        return subscriptionConfiguration;
    }

    public static NativeObject jsFunction_getDefaultThumbnail(Context cx, Scriptable thisObj, Object[] args,
                                                              Function funObj) throws AppManagementException {
        if (args == null || args.length != 1) {
            throw new AppManagementException("Invalid number of arguments. Arguments length should be one.");
        }
        if (!(args[0] instanceof String)) {
            throw new AppManagementException("Invalid argument type. App name should be a String.");
        }
        String appName = (String) args[0];

        Map<String, String> defaultThumbnailData;
        try {
            defaultThumbnailData = HostObjectUtils.getDefaultThumbnail(appName);
        } catch (IllegalArgumentException e) {
            throw new AppManagementException("App name cannot be null or empty string.", e);
        }

        NativeObject defaultThumbnail = new NativeObject();
        for (Map.Entry<String, String> entry : defaultThumbnailData.entrySet()) {
            defaultThumbnail.put(entry.getKey(), defaultThumbnail, entry.getValue());
        }
        return defaultThumbnail;
    }

	/**
     * Returns the enabled asset type list in app-manager.xml
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws AppManagementException
     */
    public static NativeArray jsFunction_getEnabledAssetTypeList(Context cx, Scriptable thisObj,
                                                                 Object[] args, Function funObj)
            throws AppManagementException {
        NativeArray availableAssetTypes = new NativeArray(0);
        List<String> typeList = HostObjectComponent.getEnabledAssetTypeList();
        for (int i = 0; i < typeList.size(); i++) {
            availableAssetTypes.put(i, availableAssetTypes, typeList.get(i));
        }
        return availableAssetTypes;
    }

    /**
     * Returns asset type enabled or not in app-manager.xml
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws AppManagementException
     */
    public static boolean jsFunction_isAssetTypeEnabled(Context cx, Scriptable thisObj,
                                                        Object[] args, Function funObj)
            throws AppManagementException {
        if (args == null || args.length != 1) {
            throw new AppManagementException(
                    "Invalid number of arguments. Arguments length should be one.");
        }
        if (!(args[0] instanceof String)) {
            throw new AppManagementException("Invalid argument type. App name should be a String.");
        }
        String assetType = (String) args[0];
        List<String> typeList = HostObjectComponent.getEnabledAssetTypeList();

        for (String type : typeList) {
            if (assetType.equals(type)) {
                return true;
            }
        }

        return false;
    }

}





