/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppConsumerExtension;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.SubscribedAppExtension;
import org.wso2.carbon.appmgt.impl.dao.AppMSubscriptionExtensionDAO;

import java.util.List;


public class AppConsumerExtensionImpl implements AppConsumerExtension {

    private static final Log log = LogFactory.getLog(AppConsumerExtensionImpl.class);

    public List<SubscribedAppExtension> getSubscribedApps(String user) throws
                                                                       AppManagementException {
        AppMSubscriptionExtensionDAO appMSubscriptionExtensionDAO = new AppMSubscriptionExtensionDAO();
        List<SubscribedAppExtension> subscribedAppsList = appMSubscriptionExtensionDAO.getSubscribedApps(user);
        return subscribedAppsList;
    }
}
