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
 Description: The service is used to authenticate users using the carbon administration module.
 Filename: authentication.js
 */

var serviceModule = (function () {
    var log = new Log('Authentication service');

    /*
     A generic authenticator interface.
     */
    function Authenticator() {
        this.instance = null;
        this.context = null;
    }

    Authenticator.prototype.init = function (context, session) {
        this.instance = context.module('user');
        this.context = context;
    };

    Authenticator.prototype.login = function (options) {
        if (options.action == "SSOLogin") {
            var userData = {};
            var result = this.instance.loginWithSAMLToken(options.username);
            if (result.error) {
                throw result.message;
            } else {
                userData['username'] = options.username;
                this.context.setUser(userData);
            }
        } else {
            var result = this.instance.login(options.username, options.password, options.tenant);
            if (result.error) {
                throw result.message;
            } else {
                var userData = {};
                userData['username'] = options.username;
                userData['isSuperTenant'] = result.isSuperTenant;
                userData['cookie'] = result.cookie;
                this.context.setUser(userData);
            }
        }
    };

    Authenticator.prototype.logout = function (options) {
        this.context.setUser(null);
    };

    Authenticator.prototype.getLoggedInUser = function (options) {
        return this.context.getUser();
    };

    return {
        Authenticator: Authenticator
    };
})();
