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

package org.wso2.carbon.appmgt.mobile.wso2mdm;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.appmgt.mobile.mdm.App;
import org.wso2.carbon.appmgt.mobile.mdm.MDMOperations;
import org.wso2.carbon.appmgt.mobile.mdm.Property;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class WSO2MDMOperations implements MDMOperations {

    private static final Log log = LogFactory.getLog(WSO2MDMOperations.class);

    /**
     * @param serverUrl server URL of the MDM
     * @param action action of the operation. Eg. install, uninstall, update
     * @param app application object
     * @param tenantId tenantId
     * @param type type of the resource. Eg: role, user, device
     * @param params ids of the resources which belong to type
     */
    @Override
    public void performAction(String serverUrl, String action, App app, int tenantId, String type, String[] params) {

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
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

        }

        requestObj.put("app", requestApp);


        HttpClient httpClient = new HttpClient();

        StringRequestEntity requestEntity = null;
        try {
            requestEntity = new StringRequestEntity( requestObj.toJSONString(),"application/json","UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e);
        }

        PostMethod postMethod = new PostMethod(serverUrl);
        postMethod.setRequestEntity(requestEntity);
        try {
            int statusCode = httpClient.executeMethod(postMethod);
            if (statusCode == HttpStatus.SC_OK) {
                log.debug("Operation performed successfully");
            }
        } catch (IOException e) {
            log.info("Could not connect to WSO2 MDM to perform operation");
            log.error(e);
        }

    }

    /**
     *
     * @param serverURL server URL of the MDM
     * @param tenantId tenantId
     * @param type type of the resource. Eg: role, user, device
     * @param params ids of the resources which belong to type
     * @param platform platform of the devices
     * @param platformVersion platform version of the devices
     * @param isSampleDevicesEnabled if MDM is not connected, enable this to display sample devices.
     * @return
     */
    @Override
    public JSONArray getDevices(String serverURL, int tenantId, String type, String[] params, String platform, String platformVersion, boolean isSampleDevicesEnabled) {

        JSONArray jsonArray = null;
        if(isSampleDevicesEnabled){
            jsonArray = (JSONArray) new JSONValue().parse(Sample.SAMPLE_DEVICES_JSON);
            return jsonArray;
        }else{
            HttpClient httpClient = new HttpClient();
            GetMethod getMethod = new GetMethod(serverURL);

            if("user".equals(type)){
                getMethod.setQueryString(new NameValuePair[]{
                        new NameValuePair("tenantId", String.valueOf(tenantId)),
                        new NameValuePair("username", params[0])
                });
            }

            try {
                int statusCode = httpClient.executeMethod(getMethod);
                if (statusCode == HttpStatus.SC_OK) {
                    jsonArray = (JSONArray) new JSONValue().parse(new String(getMethod.getResponseBody()));
                }
            } catch (IOException e) {
               log.info("Could not connect to WSO2 MDM to get device information");
               log.error(e);
            }
        }

        if(jsonArray == null){
            jsonArray = (JSONArray) new JSONValue().parse("[]");
        }

        return jsonArray;
    }


}
