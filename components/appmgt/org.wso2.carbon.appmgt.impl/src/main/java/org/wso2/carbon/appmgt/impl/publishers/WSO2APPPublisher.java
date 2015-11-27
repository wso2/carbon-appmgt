/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appmgt.impl.publishers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.APPPublisher;
import org.wso2.carbon.appmgt.api.model.APPStore;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class has the methods to create, publish, delete a webapp
 * to given external store.
 */
public class WSO2APPPublisher implements APPPublisher {
    private static Log log = LogFactory.getLog(WSO2APPPublisher.class);


    /**
     * The method to publish Webapp to external WSO2 Store
     *
     * @param webApp Web App
     * @param store  Store
     * @return published/not
     */
    @Override
    public boolean publishToStore(WebApp webApp, APPStore store) throws AppManagementException {
        if (log.isDebugEnabled()) {
            String text = String.format("Start publishing web app : %s to external store : %s "
                    , webApp.getApiName(), store.getName());
            log.debug(text);
        }

        boolean published = false;
        if (store.getEndpoint() == null || store.getUsername() == null || store.getPassword() == null) {
            String msg = "External APPStore endpoint URL or credentials are not defined.Cannot proceed with " +
                    "publishing API to the APIStore - " + store.getDisplayName();
            throw new AppManagementException(msg);
        } else {
            CookieStore cookieStore = new BasicCookieStore();
            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            boolean authenticated = loginToExternalStore(store, httpContext);
            if (authenticated) {  //First try to login to store
                String storeEndpoint = store.getEndpoint();
                String provider = AppManagerUtil.replaceEmailDomain(store.getUsername());
                String assetId = addAPPToStore(webApp, storeEndpoint, provider,
                        httpContext);
                //If API creation success,then try publishing the API
                published = publishAppToStore(assetId, storeEndpoint, httpContext);
                logoutFromExternalStore(store, httpContext);
            }
        }
        return published;
    }

    /**
     * Delete web app from given external store
     * @param webApp      APIIdentifier
     * @param store    Store
     * @return
     * @throws AppManagementException
     */
    @Override
    public boolean deleteFromStore(WebApp webApp, APPStore store) throws AppManagementException {
        if (log.isDebugEnabled()) {
            String text = String.format("Deleting web app : %s from external store : %s "
                    , webApp.getApiName(), store.getName());
            log.debug(text);
        }
        boolean deleted = false;
        if (store.getEndpoint() == null || store.getUsername() == null || store.getPassword() == null) {
            String msg = "External APIStore endpoint URL or credentials are not defined.Cannot proceed " +
                    "with publishing API to the APIStore - " + store.getDisplayName();
            throw new AppManagementException(msg);

        } else {
            CookieStore cookieStore = new BasicCookieStore();
            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            boolean authenticated = loginToExternalStore(store, httpContext);
            if (authenticated) {
                deleted = deleteFromWSO2Store(webApp, store.getUsername(), store.getEndpoint(), httpContext);
                logoutFromExternalStore(store, httpContext);
            }
            return deleted;
        }
    }

    /**
     * get the UUID of the given web app from external store
     *
     * @param webApp            webapp
     * @param externalPublisher webapp provider
     * @param storeEndpoint     store publisher endpoint
     * @param httpContext
     * @return uuid
     * @throws AppManagementException
     */
    private String getUUID(WebApp webApp, String externalPublisher, String storeEndpoint,
                           HttpContext httpContext) throws AppManagementException {
        String provider = AppManagerUtil.replaceEmailDomain(externalPublisher);
        String appName = webApp.getId().getApiName();
        String appVersion = webApp.getId().getVersion();
        String urlSuffix = provider + "/" + appName + "/" + appVersion;

        HttpClient httpclient = new DefaultHttpClient();
        storeEndpoint = storeEndpoint + AppMConstants.APISTORE_GET_UUID_URL + urlSuffix;

        HttpGet httpGet = new HttpGet(storeEndpoint);

        try {
            //Execute and get the response.
            HttpResponse response = httpclient.execute(httpGet, httpContext);
            HttpEntity entity = response.getEntity();
            //{"error" : false, "uuid" : "bbfa1766-e36a-4676-bb61-fdf2ba1f5327"}
            String responseString = EntityUtils.toString(entity, "UTF-8");
            JSONObject responseJson = (JSONObject) JSONValue.parse(responseString);
            boolean isError = true;
            if (responseJson != null) {
                isError = Boolean.parseBoolean(responseJson.get("error").toString().trim());
            }

            if (!isError) {  //If API deletion success
                String assetId = responseJson.get("uuid").toString().trim();
                return assetId;

            } else {
                throw new AppManagementException("Error while getting UUID of APP - " + appName + "" +
                        " from the external WSO2 APPStore - for provider " + externalPublisher +
                        ".Reason -" + responseString);
            }
        } catch (UnsupportedEncodingException e) {
            throw new AppManagementException("Error while getting UUID of APP - " + appName + " " +
                    "from the external WSO2 APPStore - for provider " + externalPublisher +
                    "--" + e.getMessage(), e);

        } catch (ClientProtocolException e) {
            throw new AppManagementException("Error while getting UUID of APP - " + appName + " " +
                    "from the external WSO2 APPStore - for provider  " + externalPublisher + "--" +
                    e.getMessage(), e);

        } catch (IOException e) {
            throw new AppManagementException("Error while getting UUID of APP - " + appName + " " +
                    "from the external WSO2 APPStore - " + externalPublisher + "--" + e.getMessage(), e);

        }

    }

    /**
     * delete the given web app from external api store
     *
     * @param webApp            Web App
     * @param externalPublisher Web App provider
     * @param storeEndpoint     publisher url of external store
     * @param httpContext
     * @return
     * @throws AppManagementException
     */
    private boolean deleteFromWSO2Store(WebApp webApp, String externalPublisher, String storeEndpoint,
                                        HttpContext httpContext) throws AppManagementException {

        HttpClient httpclient = new DefaultHttpClient();
        String uuid = getUUID(webApp, externalPublisher, storeEndpoint, httpContext);

        storeEndpoint = storeEndpoint + AppMConstants.APPSTORE_DELETE_URL + uuid;
        HttpDelete httpDelete = new HttpDelete(storeEndpoint);


        try {
            //Execute and get the response.
            HttpResponse response = httpclient.execute(httpDelete, httpContext);
            HttpEntity entity = response.getEntity();
            //{"ok" : "true", "message" : "Asset deleted"}
            String responseString = EntityUtils.toString(entity, "UTF-8");
            JSONObject responseJson = (JSONObject) JSONValue.parse(responseString);
            boolean status = false;

            if (responseJson != null) {
                status = Boolean.parseBoolean(responseJson.get("ok").toString().trim());
            }

            if (status) {  //If API deletion success
                return true;

            } else {
                throw new AppManagementException("Error while deleting the APP with asset id - " + webApp + "" +
                        " from the external WSO2 APPStore - for provider " + externalPublisher + ".Reason -" + responseString);
            }
        } catch (UnsupportedEncodingException e) {
            throw new AppManagementException("Error while deleting the APP with asset id - " + webApp + " " +
                    "from the external WSO2 APIStore - for provider " + externalPublisher + "--" + e.getMessage(), e);

        } catch (ClientProtocolException e) {
            throw new AppManagementException("Error while deleting the APP with asset id - " + webApp + " " +
                    "from the external WSO2 APPStore - for provider" + externalPublisher + "--" + e.getMessage(), e);

        } catch (IOException e) {
            throw new AppManagementException("Error while deleting the APP with asset id - " + webApp + " " +
                    "from the external WSO2 APPStore - for provider" + externalPublisher + "--" + e.getMessage(), e);

        }
    }

    /**
     * Authenticate to external APIStore
     *
     * @param httpContext HTTPContext
     */
    private boolean loginToExternalStore(APPStore store, HttpContext httpContext) throws AppManagementException {
        if (log.isDebugEnabled()) {
            String text = String.format("Log in to external store : %s "
                    , store.getDisplayName());
            log.debug(text);
        }
        try {
            // create a post request to addAPP.
            HttpClient httpclient = new DefaultHttpClient();
            String storeEndpoint = store.getEndpoint();
            storeEndpoint = storeEndpoint + AppMConstants.APISTORE_LOGIN_URL;
            HttpPost httppost = new HttpPost(storeEndpoint);
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(3);

            params.add(new BasicNameValuePair(AppMConstants.API_ACTION, AppMConstants.API_LOGIN_ACTION));
            params.add(new BasicNameValuePair(AppMConstants.APISTORE_LOGIN_USERNAME, store.getUsername()));
            params.add(new BasicNameValuePair(AppMConstants.APISTORE_LOGIN_PASSWORD, store.getPassword()));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost, httpContext);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");

                throw new AppManagementException(" Log in to external APPStore - " + store.getDisplayName() +
                        " failed due to -" + responseString);

            } else {
                return true;
            }

        } catch (IOException e) {
            throw new AppManagementException("Error while accessing the external store : " + store.getDisplayName() + " : " + e.getMessage(), e);

        }
    }

    /**
     * Login out from external APIStore
     *
     * @param httpContext HTTPContext
     */
    private boolean logoutFromExternalStore(APPStore store, HttpContext httpContext) throws AppManagementException {
        if (log.isDebugEnabled()) {
            String text = String.format("Logout form external store : %s"
                    , store.getDisplayName());
            log.debug(text);
        }
        try {
            // create a post request to addAPI.
            HttpClient httpclient = new DefaultHttpClient();
            String storeEndpoint = store.getEndpoint();
            storeEndpoint = storeEndpoint + AppMConstants.APISTORE_LOGIN_URL;
            HttpPost httppost = new HttpPost(storeEndpoint);
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(3);

            params.add(new BasicNameValuePair(AppMConstants.API_ACTION, AppMConstants.API_LOGOUT_ACTION));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost, httpContext);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");

                throw new AppManagementException(" Log out from external APPStore - " + store.getDisplayName() +
                        " failed due to -" + responseString);

            } else {
                return true;
            }

        } catch (Exception e) {
            throw new AppManagementException("Error while login out from : " + store.getDisplayName(), e);

        }
    }

    private static String checkValue(String input) {
        return input != null ? input : "";
    }

    private boolean changeLifeCycleState(String assetId, String nextState, String storeEndpoint,
                                         HttpContext httpContext) throws AppManagementException {
        if (log.isDebugEnabled()) {
            String text = String.format("Change the life cycle status of  web app with asset id :%s  to %s"
                    , assetId, nextState);
            log.debug(text);
        }
        HttpClient httpclient = new DefaultHttpClient();
        storeEndpoint = storeEndpoint + AppMConstants.APPSTORE_STATE_CHANGE;

        try {
            storeEndpoint = storeEndpoint + nextState + "/webapp/" + assetId;
            HttpPut httpPut = new HttpPut(storeEndpoint);
            HttpResponse response = httpclient.execute(httpPut, httpContext);
            HttpEntity entity = response.getEntity();
            //{"status" : "ok"}
            String responseString = EntityUtils.toString(entity, "UTF-8");
            JSONObject responseJson = (JSONObject) JSONValue.parse(responseString);
            String status = "";

            if (responseJson != null) {
                status = responseJson.get("status").toString();
            }

            if ("ok".equalsIgnoreCase(status)) {
                return true;
            } else {
                throw new AppManagementException("Error while change the life cycle status to -" + nextState);
            }

        } catch (UnsupportedEncodingException e) {
            throw new AppManagementException("Error while change the life cycle status to -" +
                    nextState + "--" + e.getMessage(), e);
        } catch (IOException e) {
            throw new AppManagementException("Error while change the life cycle status to -" +
                    nextState + "--" + e.getMessage(), e);
        }

    }

    /**
     * Bring the created web app status to publish state in external store
     *
     * @param assetId       webapp uuid
     * @param storeEndpoint publisher url of external store
     * @param httpContext
     * @return
     * @throws AppManagementException
     */
    private boolean publishAppToStore(String assetId, String storeEndpoint, HttpContext httpContext) throws AppManagementException {
        if (log.isDebugEnabled()) {
            String text = String.format("Publish the created web app in external stores, asset id :%s",assetId);
            log.debug(text);
        }
        // submit for review -> approve ->publish
        boolean status = changeLifeCycleState(assetId, AppMConstants.STATE_SUMIT_FOR_REVIEW, storeEndpoint, httpContext);
        if (status) {
            status = changeLifeCycleState(assetId, AppMConstants.STATE_APPROVE, storeEndpoint, httpContext);
            if (status) {
                status = changeLifeCycleState(assetId, AppMConstants.STATE_PUBLISH, storeEndpoint, httpContext);
            }
        }
        return status;
    }

    /**
     * Create webapp in the external store
     *
     * @param webApp            Web App
     * @param storeEndpoint     publisher url of external store
     * @param externalPublisher webapp provider
     * @param httpContext
     * @return
     * @throws AppManagementException
     */
    private String addAPPToStore(WebApp webApp, String storeEndpoint, String externalPublisher,
                                 HttpContext httpContext)
            throws AppManagementException {
        if (log.isDebugEnabled()) {
            String text = String.format("Creating web app : %s in external store ,using user %s"
                    , webApp.getApiName(), externalPublisher);
            log.debug(text);
        }
        HttpClient httpclient = new DefaultHttpClient();
        storeEndpoint = storeEndpoint + AppMConstants.APPSTORE_ADD_URL;
        HttpPost httppost = new HttpPost(storeEndpoint);

        try {


            List<NameValuePair> params = getParamsList(webApp, externalPublisher);
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            //Execute and get the response.
            HttpResponse response = httpclient.execute(httppost, httpContext);
            HttpEntity entity = response.getEntity();
            //{"ok" : "true", "message" : "Asset created.", "id" : "ed218b2b-18ea-4eb9-be1f-60c9182f1ba4"}
            String responseString = EntityUtils.toString(entity, "UTF-8");
            JSONObject responseJson = (JSONObject) JSONValue.parse(responseString);
            boolean status = false;
            if (responseJson != null) {
                status = Boolean.parseBoolean(responseJson.get("ok").toString().trim());
            }

            if (status) { //If API creation success
                String assetId = responseJson.get("id").toString().trim();
                return assetId;
            } else {
                throw new AppManagementException("Error while adding the web app-" + webApp.getId().getApiName() + " " +
                        "to the external WSO2 APPStore for user :" + externalPublisher + ".Reason -" + responseString);
            }
        } catch (UnsupportedEncodingException e) {
            throw new AppManagementException("Error while adding the web app-" + webApp.getId().getApiName() + " " +
                    "to the external WSO2 APPStore for user :" + externalPublisher + "--" + e.getMessage(), e);

        } catch (ClientProtocolException e) {
            throw new AppManagementException("Error while adding the web app-" + webApp.getId().getApiName() + " " +
                    "to the external WSO2 APPStore for user :" + externalPublisher + "--" + e.getMessage(), e);

        } catch (IOException e) {
            throw new AppManagementException("Error while adding the web app:" + webApp.getId().getApiName() + " " +
                    "to the external WSO2 APPStore for user :" + externalPublisher + "--" + e.getMessage(), e);

        } catch (UserStoreException e) {
            throw new AppManagementException("Error while adding the web app:" + webApp.getId().getApiName() + " " +
                    "to the external WSO2 APPStore for user :" + externalPublisher + "--" + e.getMessage(), e);
        }
    }


    /**
     * Get the redirect url of external store form registry configuration
     *
     * @param tenantId
     * @return
     * @throws AppManagementException
     */
    private String getExternalStoreRedirectURL(int tenantId) throws AppManagementException {
        if (log.isDebugEnabled()) {
            String text = "Getting external store redirect url for tenantId " + tenantId;
            log.debug(text);
        }
        UserRegistry registry;
        String redirectURL = "";

        try {
            registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(AppMConstants.EXTERNAL_APP_STORES_LOCATION)) {
                Resource resource = registry.get(AppMConstants.EXTERNAL_APP_STORES_LOCATION);
                String content = new String((byte[]) resource.getContent());
                OMElement element = AXIOMUtil.stringToOM(content);
                OMElement storeURL = element.getFirstChildWithName(new QName(AppMConstants.EXTERNAL_APP_STORES_STORE_URL));
                if (storeURL != null) {
                    redirectURL = storeURL.getText();
                } else {
                    String msg = "Store URL element is missing in External APPStores configuration";
                    log.error(msg);
                    throw new AppManagementException(msg);
                }
            }
            return redirectURL;
        } catch (RegistryException e) {
            String msg = "Error while retrieving External Stores Configuration from registry";
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Malformed XML found in the External Stores Configuration resource";
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        }

    }

    private List<NameValuePair> getParamsList(WebApp webApp, String externalPublisher) throws AppManagementException, UserStoreException {
        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(
                webApp.getId().getProviderName()));
        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                .getTenantId(tenantDomain);


        params.add(new BasicNameValuePair("overview_provider", checkValue(externalPublisher)));
        params.add(new BasicNameValuePair("overview_name", checkValue(webApp.getId().getApiName())));
        params.add(new BasicNameValuePair("overview_displayName", checkValue(webApp.getDisplayName())));
        params.add(new BasicNameValuePair("overview_appOwner", checkValue(webApp.getAppOwner())));
        params.add(new BasicNameValuePair("overview_appTenant", checkValue(webApp.getAppTenant())));
        params.add(new BasicNameValuePair("overview_advertiseOnly", "true"));
        params.add(new BasicNameValuePair("overview_redirectUrl", checkValue(getExternalStoreRedirectURL(tenantId))));
        params.add(new BasicNameValuePair("overview_context", checkValue(webApp.getContext())));
        params.add(new BasicNameValuePair("overview_version", checkValue(webApp.getId().getVersion())));
        params.add(new BasicNameValuePair("overview_transports", checkValue(webApp.getTransports())));
        params.add(new BasicNameValuePair("overview_webAppUrl", checkValue(webApp.getUrl())));

        String allowAnonymous = String.valueOf(webApp.getAllowAnonymous());
        String skipGateway = String.valueOf(webApp.getSkipGateway());

        params.add(new BasicNameValuePair("overview_allowAnonymous", checkValue(allowAnonymous)));
        params.add(new BasicNameValuePair("overview_skipGateway", checkValue(skipGateway)));
        params.add(new BasicNameValuePair("webapp", "webapp"));

        params.add(new BasicNameValuePair("overview_uuid", checkValue(webApp.getUUID())));
        params.add(new BasicNameValuePair("images_thumbnail", checkValue(webApp.getThumbnailUrl())));

        StringBuilder tagsSet = new StringBuilder("");

        Iterator it = webApp.getTags().iterator();
        int j = 0;
        while (it.hasNext()) {
            Object tagObject = it.next();
            tagsSet.append((String) tagObject);
            if (j != webApp.getTags().size() - 1) {
                tagsSet.append(",");
            }
            j++;
        }
        params.add(new BasicNameValuePair("tags", tagsSet.toString()));

        return params;
    }


}