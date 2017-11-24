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

package org.wso2.carbon.appmgt.mdm.wso2emm;


import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.appmgt.mobile.beans.ApplicationOperationAction;
import org.wso2.carbon.appmgt.mobile.beans.ApplicationOperationDevice;
import org.wso2.carbon.appmgt.mobile.interfaces.ApplicationOperations;
import org.wso2.carbon.appmgt.mobile.mdm.App;
import org.wso2.carbon.appmgt.mobile.mdm.Device;
import org.wso2.carbon.appmgt.mobile.mdm.Property;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class ApplicationOperationsImpl implements ApplicationOperations {

    private static final Log log = LogFactory.getLog(ApplicationOperationsImpl.class);

    /**
     *
     * @param applicationOperationAction holds the information needs to perform an action on mdm
     */

    public String performAction(ApplicationOperationAction applicationOperationAction) {

        HashMap<String, String> configProperties = applicationOperationAction.getConfigParams();

        String serverURL = configProperties.get(Constants.PROPERTY_SERVER_URL);
        String authUser = configProperties.get(Constants.PROPERTY_AUTH_USER);
        String authPass = configProperties.get(Constants.PROPERTY_AUTH_PASS);

        String[] params = applicationOperationAction.getParams();
        JSONArray resources = new JSONArray();
        for(String param : params){
            resources.add(param);
        }

        String action = applicationOperationAction.getAction();
        String type = applicationOperationAction.getType();
        int tenantId = applicationOperationAction.getTenantId();
        JSONObject requestObj = new JSONObject();
        requestObj.put("action", action);
        requestObj.put("to", type);
        requestObj.put("resources", resources);
        requestObj.put("tenantId", tenantId);

        JSONObject requestApp = new JSONObject();

        App app = applicationOperationAction.getApp();
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

        requestObj.put("app", requestApp);

        String requestURL = serverURL + String.format(Constants.API_OPERATION, tenantId);
        HttpClient httpClient = AppManagerUtil.getHttpClient(requestURL);
        StringEntity requestEntity = null;

        if(log.isDebugEnabled()) log.debug("Request Payload for MDM: " + requestObj.toJSONString());

        try {
            requestEntity = new StringEntity(requestObj.toJSONString(), "UTF-8");
            requestEntity.setContentType(Constants.RestConstants.APPLICATION_JSON);
        } catch (UnsupportedCharsetException e) {
            String errorMessage = "JSON encoding not supported";
            if(log.isDebugEnabled()){
                log.error(errorMessage, e);
            }else{
                log.error(errorMessage);
            }
        }

        HttpPost postMethod = new HttpPost(requestURL);
        postMethod.setEntity(requestEntity);
        postMethod.setHeader(Constants.RestConstants.AUTHORIZATION, Constants.RestConstants.BASIC +
                new String(Base64.encodeBase64((authUser + ":" + authPass).getBytes())));

        try {
            if(log.isDebugEnabled()) log.debug("Sending POST request to perform operation on MDM. Request path:  "  + requestURL);
            HttpResponse response = httpClient.execute(postMethod);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                if(log.isDebugEnabled()) log.debug(action + " operation on WSO2 EMM performed successfully");
            }

        } catch (IOException e) {
            String errorMessage = "Cannot connect to WSO2 EMM to perform operation";
            if(log.isDebugEnabled()){
                log.error(errorMessage, e);
            }else{
                log.error(errorMessage);
            }
        }

        return null;

    }

    /**
     *
     * @param applicationOperationDevice holds the information needs to retrieve device list
     * @return List of devices
     */

    public List<Device> getDevices(ApplicationOperationDevice applicationOperationDevice) {

        HashMap<String, String> configProperties = applicationOperationDevice.getConfigParams();

        String serverURL = configProperties.get(Constants.PROPERTY_SERVER_URL);
        String authUser = configProperties.get(Constants.PROPERTY_AUTH_USER);
        String authPass = configProperties.get(Constants.PROPERTY_AUTH_PASS);

        List<Device> devices = new ArrayList<>();

        HttpClient httpClient = AppManagerUtil.getHttpClient(serverURL);
        int tenantId = applicationOperationDevice.getTenantId();
        String[] params = applicationOperationDevice.getParams();
        HttpGet getMethod = new HttpGet(
                serverURL + String.format(Constants.API_DEVICE_LIST, tenantId, params[0]));
        getMethod.setHeader(Constants.RestConstants.AUTHORIZATION, Constants.RestConstants.BASIC + new String(
                Base64.encodeBase64((authUser + ":" + authPass).getBytes())));
        try {
            HttpResponse response = httpClient.execute(getMethod);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                String responseString = "";
                if (entity != null) {
                    responseString = EntityUtils.toString(entity, "UTF-8");
                    EntityUtils.consume(entity);
                }

                JSONArray devicesArray = (JSONArray) new JSONValue().parse(responseString);
                if (log.isDebugEnabled())
                    log.debug("Devices Received" + devicesArray.toJSONString());
                Iterator<JSONObject> iterator = devicesArray.iterator();
                while (iterator.hasNext()) {
                    JSONObject deviceObj = iterator.next();
                    Device device = new Device();
                    device.setId(deviceObj.get("id").toString());
                    JSONObject properties = (JSONObject) new JSONValue()
                            .parse(deviceObj.get("properties").toString());
                    device.setName(properties.get("device").toString());
                    device.setModel(properties.get("model").toString());
                    if ("1".equals(deviceObj.get("platform_id").toString())) {
                        device.setPlatform("android");
                    } else if ("2".equals(deviceObj.get("platform_id").toString())) {
                        device.setPlatform("ios");
                    } else if ("3".equals(deviceObj.get("platform_id").toString())) {
                        device.setPlatform("ios");
                    } else if ("4".equals(deviceObj.get("platform_id").toString())) {
                        device.setPlatform("ios");
                    }
                    device.setImage(String.format(configProperties.get("ImageURL"),
                            properties.get("model").toString()));
                    device.setType("mobileDevice");
                    device.setPlatformVersion("0");
                    devices.add(device);

                }
            }
        } catch (IOException e) {
            String errorMessage = "Error while getting the device list from WSO2 EMM";
            if (log.isDebugEnabled()) {
                log.error(errorMessage, e);
            } else {
                log.error(errorMessage);
            }
        }
        return devices;
    }

}
