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


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
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
import org.wso2.carbon.appmgt.mobile.mdm.Sample;
import org.wso2.carbon.appmgt.mobile.utils.User;

import javax.ws.rs.core.Response;
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

    public void performAction(User currentUser, String action, App app, int tenantId, String type, String[] params, HashMap<String, String> configProperties) {



        String serverURL = configProperties.get(Constants.PROPERTY_SERVER_URL);
        String authUser = configProperties.get(Constants.PROPERTY_AUTH_USER);
        String authPass = configProperties.get(Constants.PROPERTY_AUTH_PASS);

        JSONArray resources = new JSONArray();
        for(String param : params){
            resources.add(param);
        }

        JSONObject requestObj = new JSONObject();
        requestObj.put("action", action);
        requestObj.put("to", type);
        requestObj.put("resources", resources);
        requestObj.put("tenantId", tenantId);

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
                    log.error("Illegal Action", e);
                } catch (InvocationTargetException e) {
                    log.error("Target invocation failed", e);
                }
            }

        }

        requestObj.put("app", requestApp);

        HttpClient httpClient = new HttpClient();
        StringRequestEntity requestEntity = null;

        log.debug("Request Payload for MDM: " + requestObj.toJSONString());

        try {
            requestEntity = new StringRequestEntity( requestObj.toJSONString(),"application/json","UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("JSON encoding not supported", e);
        }

        String requestURL = serverURL + String.format(Constants.API_OPERATION, tenantId);

        PostMethod postMethod = new PostMethod(requestURL);
        postMethod.setRequestEntity(requestEntity);
        postMethod.setRequestHeader("Authorization", "Basic " + new String(Base64.encodeBase64((authUser + ":" + authPass).getBytes())));

        try {
            log.debug("Sending POST request to perform operation on MDM. Request path:  "  + requestURL);
            int statusCode = httpClient.executeMethod(postMethod);
            if (statusCode == HttpStatus.SC_OK) {
                log.debug(action + " operation on WSO2 EMM performed successfully");
            }

        } catch (IOException e) {
            log.error("Cannot connect to WSO2 EMM to perform operation", e);
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

    public List<Device> getDevices(User currentUser, int tenantId, String type, String[] params, String platform, String platformVersion, boolean isSampleDevicesEnabled, HashMap<String, String> configProperties) {

        String serverURL = configProperties.get(Constants.PROPERTY_SERVER_URL);
        String authUser = configProperties.get(Constants.PROPERTY_AUTH_USER);
        String authPass = configProperties.get(Constants.PROPERTY_AUTH_PASS);

        List<Device> devices = new ArrayList<Device>();


        if(isSampleDevicesEnabled){
            return Sample.getSampleDevices();
        }else {



            HttpClient httpClient = new HttpClient();
            GetMethod getMethod = new GetMethod(serverURL + String.format(Constants.API_DEVICE_LIST, tenantId, params[0]));
            getMethod.setRequestHeader("Authorization", "Basic " + new String(Base64.encodeBase64((authUser + ":" + authPass).getBytes())));
            try {
                int statusCode = httpClient.executeMethod(getMethod);
                if(statusCode == Response.Status.OK.getStatusCode()){
                   String response = getMethod.getResponseBodyAsString();
                   JSONArray devicesArray = (JSONArray) new JSONValue().parse(response);
                   log.debug("Devices Received" + devicesArray.toJSONString());
                   Iterator<JSONObject> iterator = devicesArray.iterator();
                   while (iterator.hasNext()){
                       JSONObject deviceObj = iterator.next();
                       Device device = new Device();
                       device.setId(deviceObj.get("id").toString());
                       JSONObject properties = (JSONObject) new JSONValue().parse(deviceObj.get("properties").toString());
                       device.setName(properties.get("device").toString());
                       device.setModel(properties.get("model").toString());
                       if("1".equals(deviceObj.get("platform_id").toString())){
                           device.setPlatform("android");
                       }else if("2".equals(deviceObj.get("platform_id").toString())){
                           device.setPlatform("ios");
                       }
                       device.setImage(String.format(configProperties.get("ImageURL"), properties.get("model").toString()));
                       device.setType("mobileDevice");
                       device.setPlatformVersion("0");
                       devices.add(device);

                   }
                }
            } catch (IOException e) {
                log.error("Error while getting the device list from WSO2 EMM", e);
            }

            return devices;
        }
    }



}
