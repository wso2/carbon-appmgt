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
package org.wso2.carbon.appmgt.api.model;

import java.util.Date;

public class SubscribedAppExtension {

    private Date subscriptionTime;
    // Evaluation period must be provided as hours.
    private int evaluationPeriod;

    private int subscriptionID;

    private Date expireOn;

    private boolean isPaid;

    private SubscribedAPI subscribedApp;

    public SubscribedAppExtension(APIIdentifier apiIdentifier) {
        subscribedApp = new SubscribedAPI(null, apiIdentifier);
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }

    public int getSubscriptionID() {
        return subscriptionID;
    }

    public void setSubscriptionID(int subscriptionID) {
        this.subscriptionID = subscriptionID;
    }
    public Date getSubscriptionTime() {
        return subscriptionTime;
    }

    public void setSubscriptionTime(Date subscriptionTime) {
        this.subscriptionTime = subscriptionTime;
    }

    public int getEvaluationPeriod() {
        return evaluationPeriod;
    }

    public void setEvaluationPeriod(int evaluationPeriod) {
        this.evaluationPeriod = evaluationPeriod;
    }

    public Date getExpireOn() {
        return expireOn;
    }

    public void setExpireOn(Date expireOn) {
        this.expireOn = expireOn;
    }

    public SubscribedAPI getSubscribedApp() {
        return subscribedApp;
    }

    public void setSubscribedApp(SubscribedAPI subscribedApp) {
        this.subscribedApp = subscribedApp;
    }
}
