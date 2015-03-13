package org.wso2.carbon.appmgt.mobile.wso2mdm;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
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
            e.printStackTrace();
        }

        PostMethod postMethod = new PostMethod(serverUrl);
        postMethod.setRequestEntity(requestEntity);
        try {
            int statusCode = httpClient.executeMethod(postMethod);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public JSONArray getDevices(String serverURL, int tenantId, String type, String[] params, String platform, String platformVersion) {

        JSONArray jsonArray = null;
        jsonArray = (JSONArray) new JSONValue().parse("[ { \"id\": \"12\", \"name\": \"My Device 1\", \"platform\": \"android\", \"platform_version\": \"4.0\", \"image\": \"https://localhost:9443/store/extensions/assets/mobileapp/resources/models/none.png\", \"model\": \"Nexus\", \"type\": \"tab\" }, { \"id\": \"11\", \"name\": \"My iPhone\", \"platform\": \"android\", \"platform_version\": \"4.0\", \"image\": \"https://localhost:9443/store/extensions/assets/mobileapp/resources/models/none.png\", \"model\": \"iPhone\", \"type\": \"phone\" } ]");

        return jsonArray;
    }


}
