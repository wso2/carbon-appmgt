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

/*
 Description: This module is used to handle the subscriptions of the user. The supported actions include:
 1. Listing the subscription details of each application owned by the user.
 2. Listing the subscriptions of each application.
 3. Allowing an application to subscribe to an app.
 4. Removing the subscription of an application.
 */
var serviceModule = (function () {

    var log = new Log('Subscriptions service');

    function Subscriber() {
        this.instance = null;
    }

    Subscriber.prototype.init = function (context, session) {
        this.instance = context.module('subscription');
    };

    /*
     Allows an application to subscribe to an application.
     @options.apiName : The name of the API
     @options.apiVersion: The version of the API
     @options.apiTier : The tier of the API
     @options.apiProvider : The provider of the API
     @options.appName: The name of the application
     @options.user:
     */
    Subscriber.prototype.addSubscription = function (options) {

        var appData = {};
        appData['name'] = options.apiName;
        appData['version'] = options.apiVersion;
        appData['provider'] = options.apiProvider;
        var result = this.instance.addAPISubscription(appData, options.subscriptionType, options.apiTier, options.appName,
            options.user, options.enterprises);
        return result;
    };

    Subscriber.prototype.updateVisibility = function (options) {

        var appData = {};
        appData['name'] = options.apiName;
        appData['version'] = options.apiVersion;
        appData['provider'] = options.apiProvider;
        appData['user'] = options.user;
        appData['op_type'] = options.op_type;
        log.info("User : " + options.user);
        var result = this.instance.updateAPPVisibility(appData);
        return result;
    };

    Subscriber.prototype.removeSubscription = function (options) {

        var appData = {};
        appData['name'] = options.apiName;
        appData['version'] = options.apiVersion;
        appData['provider'] = options.apiProvider;
        var result = this.instance.removeAPISubscription(appData, options.apiTier, options.appName, options.user);

        return result;
    };

    Subscriber.prototype.checkSubscriptionWorkflow = function () {

        var result = this.instance.checkSubscriptionWorkflow();
        return result;
    };

    /*
     The function returns all applications that have subscriptions
     options.user: The name of the user whose apps must be returned
     */
    Subscriber.prototype.getAppsWithSubs = function (options) {
        var result = this.instance.getAllSubscriptions(options.user);
        return (result) ? result.applications : [];
    };


    Subscriber.prototype.getSubsForApp = function (options) {
        var result = this.instance.getAPISubscriptionsForApplication(options.user, "DefaultApplication");
        return (result) ? result.subscriptions : [];
    };


    Subscriber.prototype.getSubscription = function (appInfo, appName, subscriptionType, user) {

        var result = this.instance.getAPISubscription(appInfo, appName, subscriptionType, user);
        return result;
    }

    /*
     Returns all available enterprises for the app.
     */
    Subscriber.prototype.getEnterprisesForApplication = function (options) {
        var result = this.instance.getEnterprisesForApplication(options.appName, options.ssoProviderName,
            options.ssoProviderVersion);
        return result;
    }

    return {
        SubscriptionService: Subscriber
    }
})();