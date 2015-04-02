/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.sample.deployer.bean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public abstract class AbstractRequest {

    private static final String ACTION_PARAMETER_VALUE = "action";
    public String action;
    private Map<String, String> parameterMap = new HashMap<String, String>();

    public String generateRequestParameters() {
        parameterMap.clear();
        init();
        String requestParams = "";
        Iterator<String> irt = parameterMap.keySet().iterator();
        while (irt.hasNext()) {
            String key = irt.next();
            if (key.equals("claimPropertyName0") && parameterMap.get(key).contains(",")) {
                String[] claims = parameterMap.get(key).split(",");

                for (int i = 0; i < claims.length; i++) {
                    requestParams += "&claimPropertyName" + i + "=" + claims[i].trim();
                }

                //parameterMap.remove("claimPropertyCounter");
            } else {
                if (!requestParams.equals("")) {
                    requestParams += "&";
                }
                requestParams += key + "=" + parameterMap.get(key);
            }
        }
        return requestParams;
    }

    public String generateTrackingID() {
        Random rand = new Random();
        StringBuilder randomNumber = new StringBuilder();
        randomNumber.append("AM_");
        for (int i = 0; i < 18; i++) {
            randomNumber.append(rand.nextInt((10 - 1) + 1) + 1);
        }
        return randomNumber.toString();
    }

    public void addParameter(String key, String value) {
        parameterMap.put(key, value);
    }

    public abstract void setAction();

    public abstract void init();

    public void setAction(String actionName) {
        this.action = actionName;
    }
}
