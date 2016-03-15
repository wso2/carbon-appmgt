/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl.idp.sso.is500;

import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.IdentityApplicationManagementAdapter;
import org.wso2.carbon.appmgt.api.IdentityApplicationManagementFactory;

public class IdentityApplicationManagementFactoryImpl implements IdentityApplicationManagementFactory {

    @Override
    public IdentityApplicationManagementAdapter createAdapter(String backendServerURL, String authCookie) throws
            AppManagementException {
        IS500IdentityApplicationManagementAdapter adapter = new IS500IdentityApplicationManagementAdapter();
        adapter.init(backendServerURL);
        adapter.setAuthCookie(authCookie);
        return adapter;
    }
}
