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

package org.wso2.carbon.appmgt.mdm.wso2mdm;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.mobile.interfaces.MDMOperations;
import org.wso2.carbon.appmgt.mobile.mdm.App;
import org.wso2.carbon.appmgt.mobile.mdm.Device;
import org.wso2.carbon.appmgt.mobile.mdm.Property;
import org.wso2.carbon.appmgt.mobile.utils.User;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class MDMOperationsImpl implements MDMOperations {

    private static final Log log = LogFactory.getLog(MDMOperationsImpl.class);

    /**
     * @param action action of the operation. Eg. install, uninstall, update
     * @param app application object
     * @param tenantId tenantId
     * @param type type of the resource. Eg: role, user, device
     * @param params ids of the resources which belong to type
     */

    public void performAction(User currentUser, String action, App app, int tenantId, String type, String[] params,
                              HashMap<String, String> configProperties) {



        String tokenApiURL = configProperties.get(Constants.PROPERTY_TOKEN_API_URL);
        String clientKey = configProperties.get(Constants.PROPERTY_CLIENT_KEY);
        String clientSecret = configProperties.get(Constants.PROPERTY_CLIENT_SECRET);



        JSONObject requestObj = new JSONObject();

        if("user".equals(type)){
            JSONArray resources = new JSONArray();
            for(String param : params){
                resources.add(param);
            }
            requestObj.put("userList", resources);
        }else if("role".equals(type)){
            JSONArray resources = new JSONArray();
            for(String param : params){
                resources.add(param);
            }
            requestObj.put("userList", resources);
        }else{
            JSONArray resources = new JSONArray();
            for(String param : params){
                JSONObject obj =  new JSONObject();
                String []paramDevices = param.split("---");
                obj.put("id", paramDevices[0]);
                obj.put("type", paramDevices[1]);
                resources.add(obj);
            }
            requestObj.put("deviceIdentifiers", resources);
        }


        JSONObject requestApp = new JSONObject();

        Method[] methods = app.getClass().getMethods();

        for (Method method : methods){

            if (method.isAnnotationPresent(Property.class)) {
                try {
                    Object value = method.invoke(app);
                    if(value != null){
                        requestApp.put(method.getAnnotation(Property.class).name(), value);
                    }
                } catch (IllegalAccessException e) {
                    String errorMessage = "Illegal Action";
                    if(log.isDebugEnabled()){
                        log.error(errorMessage, e);
                    }else{
                        log.error(errorMessage);
                    }
                } catch (InvocationTargetException e) {
                    String errorMessage = "Target invocation failed";
                    if(log.isDebugEnabled()){
                        log.error(errorMessage, e);
                    }else{
                        log.error(errorMessage);
                    }
                }
            }

        }


        requestApp.put("type",requestApp.get("type").toString().toUpperCase());



        requestObj.put("application", requestApp);

        HttpClient httpClient = new HttpClient();
        StringRequestEntity requestEntity = null;

        if(log.isDebugEnabled()) log.debug("Request Payload for MDM: " + requestObj.toJSONString());

        try {
            requestEntity = new StringRequestEntity( requestObj.toJSONString(),"application/json","UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e);
        }

        String requestURL = configProperties.get(Constants.PROPERTY_SERVER_URL);

        String actionURL = null;
        if("install".equals("install")){
            actionURL = Constants.API_INSTALL_APP;
        }else{
            actionURL = Constants.API_UNINSTALL_APP;
        }

        PostMethod postMethod = new PostMethod(requestURL + actionURL);
        postMethod.setRequestEntity(requestEntity);

        try {
            if(log.isDebugEnabled()) log.debug("Sending POST request to perform operation on MDM. Request path:  "  +
                    requestURL + actionURL);
            int statusCode = httpClient.executeMethod(postMethod);
            if (statusCode == HttpStatus.SC_OK) {
                if(log.isDebugEnabled()) log.debug(action + " operation performed successfully");
            }else{
                if(log.isDebugEnabled()) log.debug(action + " operation unsuccessful. Status code : " + statusCode);
            }

        } catch (IOException e) {
            String errorMessage = "Cannot connect to WSO2 MDM to perform operation";
            if(log.isDebugEnabled()){
                log.error(errorMessage, e);
            }else{
                log.error(errorMessage);
            }
        }

    }

    /**
     *
     * @param tenantId tenantId
     * @param type type of the resource. Eg: role, user, device
     * @param params ids of the resources which belong to type
     * @param platform platform of the devices
     * @param platformVersion platform version of the devices
     * @param isSampleDevicesEnabled if MDM is not connected, enable this to display sample devices.
     * @return
     */

    public List<Device> getDevices(User currentUser, int tenantId, String type, String[] params, String platform,
                                   String platformVersion, boolean isSampleDevicesEnabled,
                                   HashMap<String, String> configProperties) {

        String tokenApiURL = configProperties.get(Constants.PROPERTY_TOKEN_API_URL);
        String clientKey = configProperties.get(Constants.PROPERTY_CLIENT_KEY);
        String clientSecret = configProperties.get(Constants.PROPERTY_CLIENT_SECRET);

        JSONArray jsonArray = null;

        if(isSampleDevicesEnabled){
            return Sample.getSampleDevices();
        }else{

            HttpClient httpClient = new HttpClient();

            String deviceListAPI = String.format(Constants.API_DEVICE_LIST, params[0]);
            String requestURL = configProperties.get(Constants.PROPERTY_SERVER_URL) + deviceListAPI;
            GetMethod getMethod = new GetMethod(requestURL);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new NameValuePair("tenantId", String.valueOf(tenantId)));

            if(platform != null) nameValuePairs.add(new NameValuePair("platform", platform));

            if(platformVersion != null) nameValuePairs.add(new NameValuePair("platformVersion", platform));

            getMethod.setQueryString((NameValuePair[]) nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
            getMethod.setRequestHeader("Accept", "application/json");

            if(executeMethod(tokenApiURL, clientKey, clientSecret, httpClient, getMethod)){
                try {
                    jsonArray = (JSONArray) new JSONValue().parse(new String(getMethod.getResponseBody()));
                    if(jsonArray != null){
                        if(log.isDebugEnabled()) log.debug("Devices received from MDM: " + jsonArray.toJSONString());
                    }
                } catch (IOException e) {
                    String errorMessage = "Invalid response from the devices API";
                    if(log.isDebugEnabled()){
                        log.error(errorMessage, e);
                    }else{
                        log.error(errorMessage);
                    }
                }
            }else{
                log.error("Getting devices from MDM API failed");
            }
        }

        if(jsonArray == null){
            jsonArray = (JSONArray) new JSONValue().parse("[]");
        }

        List<Device> devices = new ArrayList<Device>();

        Iterator<JSONObject> iterator = jsonArray.iterator();
        while (iterator.hasNext()){
            JSONObject deviceObj = iterator.next();


            Device device = new Device();
            device.setId(deviceObj.get("deviceIdentifier").toString()+ "---" + deviceObj.get("type").toString());
            device.setName(deviceObj.get("name").toString());
            device.setModel(deviceObj.get("name").toString());
            device.setType("mobileDevice");
            device.setImage("/store/extensions/assets/mobileapp/resources/models/none.png");
            device.setPlatform(deviceObj.get("type").toString());
            devices.add(device);

        }

        return devices;
    }


    private String getAPIToken(String tokenApiURL, String clientKey, String clientSecret, boolean generateNewKey){

        if(!generateNewKey){
            if(AuthHandler.authKey != null){
                return AuthHandler.authKey;
            }
        }

        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(tokenApiURL);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new NameValuePair("grant_type", "client_credentials"));
        postMethod.setQueryString((NameValuePair[]) nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
        postMethod.addRequestHeader("Authorization" , "Basic " +
                new String(Base64.encodeBase64((clientKey + ":" + clientSecret).getBytes())));
        postMethod.addRequestHeader("Content-Type" , "application/x-www-form-urlencoded");
        try {
            if(log.isDebugEnabled()) log.debug("Sending POST request to API Token endpoint. Request path:  "  + tokenApiURL);
            int statusCode = httpClient.executeMethod(postMethod);
        } catch (IOException e) {
            String errorMessage = "Cannot connect to Token API Endpoint";
            if(log.isDebugEnabled()){
                log.error(errorMessage, e);
            }else{
                log.error(errorMessage);
            }
            return null;
        }

        String response = null;
        try {
            response = postMethod.getResponseBodyAsString();
        } catch (IOException e) {
            String errorMessage = "Cannot get response body for auth";
            if(log.isDebugEnabled()){
                log.error(errorMessage, e);
            }else{
                log.error(errorMessage);
            }
            return null;
        }

        JSONObject token = (JSONObject) new JSONValue().parse(response);

        AuthHandler.authKey = String.valueOf( token.get("access_token"));
        return AuthHandler.authKey;
    }


    private boolean executeMethod(String tokenApiURL, String clientKey, String clientSecret, HttpClient httpClient,
                                  HttpMethodBase httpMethod){
                String authKey = getAPIToken(tokenApiURL, clientKey, clientSecret, false);
                if(log.isDebugEnabled()) log.debug("Access token received : " + authKey);
                //String authKey = "12345";
                try {
                    int statusCode = 401;
                    int tries = 0;
                    while(statusCode != 200){
                        httpMethod.setRequestHeader("Authorization", "Bearer " + authKey);
                        statusCode = httpClient.executeMethod(httpMethod);
                        if(statusCode == 401){
                            if(++tries > 3){
                                log.info("API Call failed for the 3rd time: Unauthorized Access Aborting...");
                                return false;
                            }
                            authKey = getAPIToken(tokenApiURL, clientKey, clientSecret, true);
                        }
                    }
                    return true;
                } catch (IOException e) {
                    String errorMessage = "No OK response received form the API";
                    if(log.isDebugEnabled()){
                        log.error(errorMessage, e);
                    }else{
                        log.error(errorMessage);
                    }
                    return false;
                }
    }

}
