/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.mobile.mdm;

import org.wso2.carbon.appmgt.mobile.interfaces.MDMOperations;
import org.wso2.carbon.appmgt.mobile.utils.User;

import java.util.HashMap;
import java.util.List;

/**
 * Class implements MDM operations in OSGI Service to WSO2 MDM
 */
public class MDMOperationsImpl implements MDMOperations {


    @Override
    public void performAction(User currentUser, String action, App app, int tenantId, String type, String[] params,
            HashMap<String, String> configParams) {

    }

    @Override
    public List<Device> getDevices(User currentUser, int tenantId, String type, String[] params, String platform,
            String platformVersion, boolean isSampleDevicesEnabled, HashMap<String, String> configParams) {
        return null;
    }
}
