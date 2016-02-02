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
package org.wso2.carbon.appmgt.hostobjects;

import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.appmgt.api.AppConsumerExtension;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.api.model.SubscribedAppExtension;
import org.wso2.carbon.appmgt.impl.AppConsumerExtensionImpl;
import org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class AppStoreExtensionHostObject extends ScriptableObject {

    private AppConsumerExtension appConsumerExtension;

    private static final String hostObjectName = "AppStoreExtension";

    public static Scriptable jsConstructor(Context cx, Object[] args, Function Obj,
                                           boolean inNewExpr)
            throws ScriptException, AppManagementException {
        return new AppStoreExtensionHostObject();
    }

    public AppStoreExtensionHostObject() throws AppManagementException {
        appConsumerExtension = new AppConsumerExtensionImpl();
    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public AppConsumerExtension getAppConsumerExtension() {
        return appConsumerExtension;
    }

    public void setAppConsumerExtension(AppConsumerExtension appConsumerExtension) {
        this.appConsumerExtension = appConsumerExtension;
    }

    private static AppConsumerExtension getAppConsumer(Scriptable thisObj) {
        return ((AppStoreExtensionHostObject) thisObj).getAppConsumerExtension();
    }

    public static NativeArray jsFunction_getSubscriptionsByUser(Context cx,
                                                                Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, AppManagementException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        NativeArray myn = new NativeArray(0);
        if (args != null && isStringArray(args)) {
            String userName = (String) args[0];
            boolean isTenantFlowStarted = false;
            try {
                String tenantDomain = MultitenantUtils.getTenantDomain(AppManagerUtil.replaceEmailDomainBack(userName));
                if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }
                AppConsumerExtension appConsumer = getAppConsumer(thisObj);
                List<SubscribedAppExtension> subscribedAppsList = appConsumer.getSubscribedApps(userName);

                int i = 0;
                for (SubscribedAppExtension subscribedApp : subscribedAppsList) {
                    NativeObject row = new NativeObject();
                    row.put("subscriptionId", row, subscribedApp.getSubscriptionID());
                    row.put("appId", row, subscribedApp.getSubscribedApp().getApiId().getApplicationId());
                    row.put("appName", row, subscribedApp.getSubscribedApp().getApiId().getApiName());
                    row.put("appVersion", row, subscribedApp.getSubscribedApp().getApiId().getVersion());
                    row.put("appProvider", row, AppManagerUtil.replaceEmailDomainBack(
                            subscribedApp.getSubscribedApp().getApiId().getProviderName()));
                    row.put("subscriptionTime", row, dateFormat.format(subscribedApp.getSubscriptionTime()));
                    row.put("evaluationPeriod", row, subscribedApp.getEvaluationPeriod());
                    row.put("expiredOn", row, dateFormat.format(subscribedApp.getExpireOn()));
                    row.put("isPaid", row, subscribedApp.isPaid());
                    myn.put(i, myn, row);
                    i++;
                }
            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }
        return myn;
    }

    public static boolean isStringArray(Object[] args) {
        int argsCount = args.length;
        for (int i = 0; i < argsCount; i++) {
            if (!(args[i] instanceof String)) {
                return false;
            }
        }
        return true;
    }
}
