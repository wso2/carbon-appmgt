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
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.AppStore;
import org.wso2.carbon.appmgt.api.model.ExternalAppStorePublisher;
import org.wso2.carbon.appmgt.api.model.WebApp;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.service.ServiceReferenceHolder;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class has the methods to create, publish, delete a webapp
 * to other stores in App Manager.
 */
public class WSO2ExternalAppStorePublisher implements ExternalAppStorePublisher {
    private static final Log log = LogFactory.getLog(WSO2ExternalAppStorePublisher.class);

    @Override
    public void publishToStore(WebApp webApp, AppStore store) throws AppManagementException {
        if (log.isDebugEnabled()) {
            String msg = String.format("Start publishing web app : %s to external store : %s "
                    , webApp.getApiName(), store.getName());
            log.debug(msg);
        }

        validateStore(store);

        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        String storeEndpoint = store.getEndpoint();
        HttpClient httpClient = AppManagerUtil.getHttpClient(storeEndpoint);
        String provider = AppManagerUtil.replaceEmailDomain(store.getUsername());
        //login
        loginToExternalStore(store, httpContext, httpClient);
        //create app
        String assetId = addAppToStore(webApp, storeEndpoint, provider, httpContext, httpClient);
        //add tags
        addTags(webApp, assetId, storeEndpoint, httpContext, httpClient);
        //publish app
        publishAppToStore(assetId, storeEndpoint, httpContext, httpClient);
        //logout
        logoutFromExternalStore(storeEndpoint, httpContext, httpClient);

    }

    private void validateStore(AppStore store) throws AppManagementException {
        if (store.getEndpoint() == null) {
            String msg = "External AppStore endpoint URL is not defined.Cannot proceed with " +
                    "publishing App to the App Store: " + store.getDisplayName();
            throw new AppManagementException(msg);
        }

        if (store.getUsername() == null) {
            String msg = "Username for External App Store is not defined.Cannot proceed with " +
                    "publishing App to the App Store: " + store.getDisplayName();
            throw new AppManagementException(msg);
        }

        if (store.getPassword() == null) {
            String msg = "Password for External App Store is not defined.Cannot proceed with " +
                    "publishing App to the App Store: " + store.getDisplayName();
            throw new AppManagementException(msg);
        }
    }

    @Override
    public void deleteFromStore(WebApp webApp, AppStore store) throws AppManagementException {
        if (log.isDebugEnabled()) {
            String msg = String.format("Deleting web app : %s from external store : %s "
                    , webApp.getApiName(), store.getName());
            log.debug(msg);
        }

        validateStore(store);

        String storeEndpoint = store.getEndpoint();
        HttpClient httpClient = AppManagerUtil.getHttpClient(storeEndpoint);
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        loginToExternalStore(store, httpContext, httpClient);
        deleteFromExternalStore(webApp, store.getUsername(), storeEndpoint, httpContext);
        logoutFromExternalStore(storeEndpoint, httpContext, httpClient);
    }

    /**
     * Get the UUID of the given web app from external store.
     *
     * @param webApp            Web App
     * @param externalPublisher Web App provider
     * @param storeEndpoint     Publisher url of external store
     * @param httpContext
     * @return uuid
     * @throws AppManagementException
     */
    private String getUUID(WebApp webApp, String externalPublisher, String storeEndpoint,
                           HttpContext httpContext) throws AppManagementException {
        String provider = AppManagerUtil.replaceEmailDomain(externalPublisher);
        String appName = webApp.getId().getApiName();
        String appVersion = webApp.getId().getVersion();

        try {
            String urlSuffix = provider + "/" + appName + "/" + appVersion;
            urlSuffix = URIUtil.encodePath(urlSuffix, "UTF-8");
            storeEndpoint = storeEndpoint + AppMConstants.APP_STORE_GET_UUID_URL + urlSuffix;
            HttpGet httpGet = new HttpGet(storeEndpoint);
            HttpClient httpClient = AppManagerUtil.getHttpClient(storeEndpoint);

            //Execute and get the response.
            HttpResponse response = httpClient.execute(httpGet, httpContext);
            HttpEntity entity = response.getEntity();
            //{"error" : false, "uuid" : "bbfa1766-e36a-4676-bb61-fdf2ba1f5327"}
            // or {"error" : false, "uuid" : null, "message" : "Could not find UUID for given webapp"}
            String responseString = EntityUtils.toString(entity, "UTF-8");
            JSONObject responseJson = (JSONObject) JSONValue.parse(responseString);
            boolean isError = true;
            if (responseJson != null) {
                Object error = responseJson.get("error");
                if (error != null)
                    isError = Boolean.parseBoolean(error.toString().trim());
            }

            if (!isError) {
                Object assetId = responseJson.get("uuid");
                if(assetId != null) {
                    return assetId.toString().trim();
                }
                return null;
            } else {
                throw new AppManagementException("Error while getting UUID of APP - " + appName + "" +
                        " from the external  AppStore - for provider " + externalPublisher +
                        ".Reason -" + responseString);
            }
        } catch (IOException e) {
            throw new AppManagementException("Error while getting UUID of APP : " + appName + " " +
                    "from the external  AppStore - " + externalPublisher, e);

        }

    }

    /**
     * Delete the given web app from external app store.
     *
     * @param webApp            Web App
     * @param externalPublisher Web App provider
     * @param storeEndpoint     publisher url of external store
     * @param httpContext
     * @throws AppManagementException
     */
    private void deleteFromExternalStore(WebApp webApp, String externalPublisher, String storeEndpoint,
                                         HttpContext httpContext) throws AppManagementException {

        try {

            String uuid = getUUID(webApp, externalPublisher, storeEndpoint, httpContext);
            if (uuid == null) {
                // No uuid found for given web app in external store,i.e this app would have
                //been manually deleted from external store , So consider as app is success fully deleted
                return;
            }
            storeEndpoint = storeEndpoint + AppMConstants.APP_STORE_DELETE_URL + uuid;

            HttpClient httpclient = AppManagerUtil.getHttpClient(storeEndpoint);
            HttpDelete httpDelete = new HttpDelete(storeEndpoint);

            //Execute and get the response.
            HttpResponse response = httpclient.execute(httpDelete, httpContext);
            HttpEntity entity = response.getEntity();
            String responseString = "";
            if (entity != null) {
                //{"ok" : "true", "message" : "Asset deleted"}
                responseString = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);
            }
            JSONObject responseJson = (JSONObject) JSONValue.parse(responseString);
            boolean status = false;

            if (responseJson != null) {
                Object statusOk = responseJson.get("ok");
                if (statusOk != null)
                    status = Boolean.parseBoolean(statusOk.toString().trim());
            }
            if (!status) {
                throw new AppManagementException("Error while deleting the APP : " + webApp.getApiName() + "" +
                        " from the external  AppStore for provider :" + externalPublisher + ".Reason :" +
                        responseString);
            }
        } catch (IOException e) {
            throw new AppManagementException("Error while deleting the APP : " + webApp.getApiName() + " " +
                    "from the external  AppStore for provider :" + externalPublisher, e);

        }
    }

    /**
     * Authenticate to given external store.
     *
     * @param store       External Store
     * @param httpContext HttpContext
     * @param httpClient
     * @throws AppManagementException
     */
    private void loginToExternalStore(AppStore store, HttpContext httpContext, HttpClient httpClient) throws AppManagementException {
        if (log.isDebugEnabled()) {
            String msg = String.format("Log in to external store : %s ", store.getDisplayName());
            log.debug(msg);
        }
        try {
            String storeEndpoint = store.getEndpoint();
            storeEndpoint = storeEndpoint + AppMConstants.APP_STORE_AUTHENTICATE_URL;
            HttpPost httpPost = new HttpPost(storeEndpoint);
            List<NameValuePair> params = new ArrayList<NameValuePair>(3);

            params.add(new BasicNameValuePair(AppMConstants.APP_ACTION, AppMConstants.APP_LOGIN_ACTION));
            params.add(new BasicNameValuePair(AppMConstants.APP_STORE_LOGIN_USERNAME, store.getUsername()));
            params.add(new BasicNameValuePair(AppMConstants.APP_STORE_LOGIN_PASSWORD, store.getPassword()));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpClient.execute(httpPost, httpContext);
            int statusCode = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();
            if (statusCode != HttpStatus.SC_OK) {
                String responseString = "";
                if (entity != null) {
                    responseString = EntityUtils.toString(entity, "UTF-8");
                }
                throw new AppManagementException(" Log in to external AppStore : " + store.getDisplayName() +
                        " failed due to :" + responseString);
            }

            if (entity != null)
                EntityUtils.consume(entity);
        } catch (IOException e) {
            throw new AppManagementException("Error while login to the external store : " + store.getDisplayName(), e);
        }
    }


    /**
     * Logout form given external store.
     *
     * @param storeEndpoint Publisher url of external store
     * @param httpContext   HttpContext
     * @param httpClient
     * @throws AppManagementException
     */
    private void logoutFromExternalStore(String storeEndpoint, HttpContext httpContext, HttpClient httpClient)
            throws AppManagementException {
        if (log.isDebugEnabled()) {
            String msg = String.format("Logout form external store");
            log.debug(msg);
        }
        try {
            storeEndpoint = storeEndpoint + AppMConstants.APP_STORE_AUTHENTICATE_URL;
            HttpPost httpPost = new HttpPost(storeEndpoint);
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair(AppMConstants.APP_ACTION, AppMConstants.APP_LOGOUT_ACTION));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost, httpContext);
            int statusCode = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();
            if (statusCode != HttpStatus.SC_OK) {
                String responseString = "";
                if (entity != null) {
                    responseString = EntityUtils.toString(entity, "UTF-8");
                    EntityUtils.consume(entity);
                }
                throw new AppManagementException(" Log out from external store failed due to :" + responseString);
            }
            if (entity != null)
                EntityUtils.consume(entity);
        } catch (IOException e) {
            throw new AppManagementException("Error while log out from external store: ", e);
        }
    }

    private static String checkValue(String input) {
        return input != null ? input : "";
    }

    private void changeLifeCycleState(String assetId, String nextState, String storeEndpoint,
                                      HttpContext httpContext, HttpClient httpClient) throws AppManagementException {
        if (log.isDebugEnabled()) {
            String msg = String.format("Change the life cycle status of  web app with asset id :%s  to %s"
                    , assetId, nextState);
            log.debug(msg);
        }

        try {
            storeEndpoint = storeEndpoint + AppMConstants.APP_STORE_STATE_CHANGE + nextState +
                    "/" + AppMConstants.APP_TYPE + "/" + assetId;
            HttpPut httpPut = new HttpPut(storeEndpoint);

            HttpResponse response = httpClient.execute(httpPut, httpContext);
            HttpEntity entity = response.getEntity();
            String responseString = "";
            if (entity != null) {
                //{"status" : "Success"}
                responseString = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);
            }
            JSONObject responseJson = (JSONObject) JSONValue.parse(responseString);
            String status = "";

            if (responseJson != null) {
                Object statusVal = responseJson.get("status");
                status = statusVal.toString();
            }

            if (!"Success".equalsIgnoreCase(status)) {
                throw new AppManagementException("Error while change the life cycle status to :" + nextState);
            }
        } catch (IOException e) {
            throw new AppManagementException("Error while change the life cycle status to :" +
                    nextState, e);
        }

    }

    /**
     * Change status of created web app  to publish state in external store.
     *
     * @param assetId       Web App uuid
     * @param storeEndpoint Publisher url of external store
     * @param httpContext   HttpContext
     * @param httpClient
     * @throws AppManagementException
     */
    private void publishAppToStore(String assetId, String storeEndpoint, HttpContext httpContext, HttpClient httpClient) throws
            AppManagementException {
        if (log.isDebugEnabled()) {
            String msg = String.format("Publish the created web app in external store, asset id :%s", assetId);
            log.debug(msg);
        }
        // change state : created->review
        changeLifeCycleState(assetId, AppMConstants.STATE_SUMIT_FOR_REVIEW, storeEndpoint, httpContext, httpClient);
        //  change state : review -> approve
        changeLifeCycleState(assetId, AppMConstants.STATE_APPROVE, storeEndpoint, httpContext, httpClient);
        //  change state : approve ->publish
        changeLifeCycleState(assetId, AppMConstants.STATE_PUBLISH, storeEndpoint, httpContext, httpClient);
    }

    /**
     * Create web app in the external store.
     *
     * @param webApp            Web App
     * @param storeEndpoint     Publisher url of external store
     * @param externalPublisher Web app provider
     * @param httpContext       HttpContext
     * @param httpClient
     * @return UUID of web app
     * @throws AppManagementException
     */
    private String addAppToStore(WebApp webApp, String storeEndpoint, String externalPublisher,
                                 HttpContext httpContext, HttpClient httpClient)
            throws AppManagementException {
        if (log.isDebugEnabled()) {
            String msg = String.format("Creating web app : %s in external store ,using user %s"
                    , webApp.getApiName(), externalPublisher);
            log.debug(msg);
        }

        try {
            storeEndpoint = storeEndpoint + AppMConstants.APP_STORE_ADD_URL;
            HttpPost httpPost = new HttpPost(storeEndpoint);

            List<NameValuePair> params = getParamsList(webApp, externalPublisher);
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            //Execute and get the response.
            HttpResponse response = httpClient.execute(httpPost, httpContext);
            HttpEntity entity = response.getEntity();
            String responseString = "";
            if (entity != null) {
                //{"ok" : "true", "message" : "Asset created.", "id" : "ed218b2b-18ea-4eb9-be1f-60c9182f1ba4"}
                responseString = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);
            }
            JSONObject responseJson = (JSONObject) JSONValue.parse(responseString);
            boolean status = false;
            if (responseJson != null) {
                Object statusOk = responseJson.get("ok");
                if (statusOk != null) {
                    status = Boolean.parseBoolean(statusOk.toString().trim());
                }
            }

            if (status) { //If APP creation success
                String assetId = responseJson.get("id").toString().trim();
                return assetId;
            } else {
                throw new AppManagementException("Error while adding the web app :" + webApp.getApiName() + " " +
                        "to the external  AppStore for user :" + externalPublisher + ".Reason -" + responseString);
            }
        } catch (IOException e) {
            throw new AppManagementException("Error while adding the web app :" + webApp.getApiName() + " " +
                    "to the external  AppStore for user :" + externalPublisher, e);

        } catch (UserStoreException e) {
            throw new AppManagementException("Error while adding the web app :" + webApp.getApiName() + " " +
                    "to the external  AppStore for user :" + externalPublisher, e);
        }
    }

    /**
     * Add tags to web app created in the external store.
     *
     * @param webApp        Web App in the current store
     * @param assetId       UUID of web app created in the external store
     * @param storeEndpoint Publisher url of external store
     * @param httpContext   HttpContext
     * @param httpClient
     * @throws AppManagementException
     */
    private void addTags(WebApp webApp, String assetId, String storeEndpoint, HttpContext httpContext, HttpClient httpClient)
            throws AppManagementException {

        if (log.isDebugEnabled()) {
            String msg = String.format("Adding tags to web app : %s in external store "
                    , webApp.getApiName());
            log.debug(msg);
        }

        try {
            storeEndpoint = storeEndpoint + AppMConstants.APP_STORE_ADD_TAGS_URL + assetId;
            HttpPut httpPut = new HttpPut(storeEndpoint);

            String tags = getTagsAsJsonString(webApp);
            if (tags != null) { //web app have  tags
                StringEntity input = new StringEntity(tags);
                input.setContentType("application/json");
                httpPut.setEntity(input);
                HttpResponse response = httpClient.execute(httpPut, httpContext);
                HttpEntity entity = response.getEntity();
                String responseString = "";
                if (entity != null) {
                    //{"status" : 200, "ok" : true}
                    responseString = EntityUtils.toString(entity, "UTF-8");
                    EntityUtils.consume(entity);
                }
                JSONObject responseJson = (JSONObject) JSONValue.parse(responseString);
                boolean status = false;
                if (responseJson != null) {
                    Object statusOk = responseJson.get("ok");
                    if (statusOk != null)
                        status = Boolean.parseBoolean(statusOk.toString().trim());
                }

                if (!status) { //If tags successfully added
                    throw new AppManagementException("Error while adding tags to web app :" +
                            webApp.getApiName() + ". Reason :" + responseString);
                }
            }
        } catch (IOException e) {
            throw new AppManagementException("Error while adding  tags to web app :" + webApp.getApiName(), e);
        }

    }

    private String getTagsAsJsonString(WebApp webApp) {
        Set<String> tagsSet = webApp.getTags();
        if (tagsSet.size() > 0) {
            JSONArray tagList = new JSONArray();
            for (String tag : tagsSet) {
                tagList.add(tag);
            }
            JSONObject tags = new JSONObject();
            tags.put("tags", tagList);
            return tags.toString();
        }
        return null;
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
            String msg = "Getting external store redirect url for tenantId " + tenantId;
            log.debug(msg);
        }

        try {
            String redirectURL = "";
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(AppMConstants.EXTERNAL_APP_STORES_LOCATION)) {
                Resource resource = registry.get(AppMConstants.EXTERNAL_APP_STORES_LOCATION);
                String content = new String((byte[]) resource.getContent());
                OMElement element = AXIOMUtil.stringToOM(content);
                OMElement storeURL = element.getFirstChildWithName(
                        new QName(AppMConstants.EXTERNAL_APP_STORES_STORE_URL));
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
            String msg = "Error while retrieving External Stores Configuration from registry : " +
                    AppMConstants.EXTERNAL_APP_STORES_LOCATION;
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Malformed XML found in the External Stores Configuration resource";
            log.error(msg, e);
            throw new AppManagementException(msg, e);
        }

    }

    private List<NameValuePair> getParamsList(WebApp webApp, String externalPublisher) throws
            AppManagementException, UserStoreException {
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
        params.add(new BasicNameValuePair("webapp", "webapp"));
        params.add(new BasicNameValuePair("overview_advertisedAppUuid", checkValue(webApp.getUUID())));
        params.add(new BasicNameValuePair("images_thumbnail", checkValue(webApp.getThumbnailUrl())));
        params.add(new BasicNameValuePair("overview_transports", checkValue(webApp.getTransports())));
        params.add(new BasicNameValuePair("overview_webAppUrl", checkValue(webApp.getUrl())));
        params.add(new BasicNameValuePair("overview_treatAsASite",checkValue(webApp.getTreatAsASite())));
        return params;
    }


}