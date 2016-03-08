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
 Description: This module is responsible for retrieving data about WebApps and Sites
 Filename: app-information.js
 */
var serviceModule = (function () {

    var log = new Log('App information service');

    /*
     Constants used in constructing the API query
     */
    var QUERY_PARAM_PROVIDER = 'provider';
    var QUERY_PARAM_ASSET = 'name';
    var QUERY_PARAM_VERSION = 'version';

    /*
     Constants used in parsing the uriTemplate
     */
    var TEMPLATE_METHOD_INDEX = 1;
    var TEMPLATE_AUTH_INDEX = 2;
    var TEMPLATE_THROTTLE_INDEX = 3;
    var TEMPLATE_AUTH_DEFAULT = 'Any';
    var TEMPLATE_THROTTLE_DEFAULT = 'Unlimited';

    /*
     Constants used in creating URI template JSON
     */
    var URI_MAP_KEY_TIER = 'tier';
    var URI_MAP_KEY_AUTH = 'authType';
    var URI_MAP_KEY_TIERDESCRIPTION = 'tierDescription';

    /*
     Constants used in constructing the description JSON
     */
    var APP_CONTEXT_KEY = 'context';
    var APP_VERSION_KEY = 'version';
    var APP_URI_TEMPLATES_KEY = 'uriTemplates';
    var APP_UPDATED_KEY = 'updatedDate';
    var APP_SERVERURL_KEY = 'serverURL';
    var APP_DISCOVERYURL_KEY = 'discoveryURL';
    var MSG_UNABLE_TO_GET_API_DATA = 'Unable to get the application data';

    //Constants used in parsing the serverURL property
    var SERVER_URL_DESCRIPTION_INDEX = 0;
    var SERVER_URL_SANDBOX_INDEX = 1;
    var SERVER_URL_PRODUCTION_INDEX = 2;

    /*
     The class wraps the functionality of the api module and returns
     tier and app descriptions.
     */
    function AppInformationService() {
        this.instance = null;
        this.user = null;

    }

    /*
     The method is required to initialize the api module used to make the
     data retrieval calls.
     */
    AppInformationService.prototype.init = function (context, session) {
        this.instance = context.module('site');
    };

    /*
     The method is used to obtain details about a given app.
     @assetProvider: The app provider.
     @assetName: The name of the app.
     @assetVersion: The version of the app.
     @ofUser: This is an optional parameter which specified the user. If a value is not provided then name
     is taken from the session.
     @returns: A JSON object containing information on the app.
     */
    AppInformationService.prototype.getAppDescription = function (assetProvider, assetName, assetVersion, ofUser) {
        var query = getQuery(assetProvider, assetName, assetVersion);
        var user = ofUser || this.user;
        var appDescription = this.instance.getAppDescription(query, user);

        //Check if an exception has occured during the method invocation.
        if (appDescription.error != false) {
            throw appDescription.error;
        }

        var tiersArray = this.getTiers().tiers;
        var tierMap = new TierMap(tiersArray);
        var uriTemplateMap = new UriTemplateMap(appDescription.uriTemplates);

        addTierDescription(uriTemplateMap, tierMap);
        return createDescriptionObject(appDescription, uriTemplateMap.toArray());
    };

    /*
     The method returns an array containing tier information.
     @returns: An array containing JSON objects with information on the different tiers.
     */
    AppInformationService.prototype.getTiers = function () {
        var tiers = this.instance.getTiers();
        return tiers.tiers;
    };

    /*
     The function creates a query object used to invoke the getAppDescription method of the
     site module.
     @return: A JSON object encapsulating the app provider, name and version.
     */
    var getQuery = function (assetProvider, assetName, assetVersion) {
        var query = {};
        query[QUERY_PARAM_PROVIDER] = assetProvider;
        query[QUERY_PARAM_ASSET] = assetName;
        query[QUERY_PARAM_VERSION] = assetVersion;
        return query;
    };

    /*
     The function amalgamates the data returned by the site module into a structure that
     can be consumed by templates.
     @return: A JSON object containing app and URI template details
     */
    var createDescriptionObject = function (appDescription, uriTemplates) {
        var map = {};
        map[APP_CONTEXT_KEY] = appDescription.api.context || MSG_UNABLE_TO_GET_API_DATA;
        map[APP_VERSION_KEY] = appDescription.api.version || MSG_UNABLE_TO_GET_API_DATA;
        map[APP_URI_TEMPLATES_KEY] = uriTemplates;
        map[APP_UPDATED_KEY] = appDescription.api.updatedDate || MSG_UNABLE_TO_GET_API_DATA;
        map[APP_SERVERURL_KEY] = readServerURLs(appDescription.api);
        map[APP_DISCOVERYURL_KEY] = appDescription.discoveryURL || MSG_UNABLE_TO_GET_API_DATA;
        return map;
    };

    /*
     The function parses the serverURL property of the data returned by the site module
     */
    var readServerURLs = function (app) {
        var app = app || {};
        var components = app.serverURL.split(',');

        return {
            description: components[SERVER_URL_DESCRIPTION_INDEX] ?
                components[SERVER_URL_DESCRIPTION_INDEX] :
                MSG_UNABLE_TO_GET_API_DATA,
            productionURL: components[SERVER_URL_SANDBOX_INDEX] ?
            components[SERVER_URL_SANDBOX_INDEX] + app.context + "/" :
                MSG_UNABLE_TO_GET_API_DATA
        };
    };

    /*
     The class is used to organize information on the different app tiers
     for easy access.
     */
    function TierMap(tiersArray) {
        this.map = {};
        initTierMap(tiersArray, this.map);
    }

    /*
     The method returns the description of a given tier.
     @returns: A string containing the tier description. If the tier is not valid then an empty string is provided.
     */
    TierMap.prototype.getTierDescription = function (tierName) {
        if (this.map.hasOwnProperty(tierName)) {
            return this.map[tierName];
        }
        return '';
    };

    /*
     The function goes through a tiers array and creates a map.
     */
    var initTierMap = function (tiersArray, map) {
        var tier;
        for (var tierIndex in tiersArray) {
            tier = tiersArray[tierIndex];
            map[tier.tierName] = tier.tierDescription;
        }
    };

    /*
     The class is used to organize uri templates by a context.
     */
    function UriTemplateMap(uriTemplateObj) {
        this.map = {};
        initUriTemplates(uriTemplateObj, this.map);
    }

    /*
     The method obtains URI template details for a given context and method.
     @context: A single context has mutliple URI templates for each of the 5 method types.
     @method: An HTTP method name for which URI templates need to be retrieved.
     ( GET,POST,PUT,DELETE,OPTIONS)
     @returns: A JSON object containing URI template details for a given HTTP method and context. If
     the method type is not supported an empty object is returned.
     */
    UriTemplateMap.prototype.getMethodDetails = function (context, methodName) {

        //Check if the context exists
        if (!this.map.hasOwnProperty(context)) {
            return {};
        }

        //Check if a method exists
        if (this.map[context].hasOwnProperty(methodName)) {
            return this.map[context][methodName];
        }

        return {};
    };

    /*
     The method allows meta data to be saved regarding a given context and HTTP method type.
     @context: The unique context for which the method property must be set.If the context is not present
     then the property value is not saved.
     @methodName: A HTTP method name ( GET,POST,PUT,DELETE,OPTIONS)
     @prop: A unique property value to identify the value. If the property name does not exist it is added,else
     the existing value for the property is overridden.
     @value: The value of the property to be set.
     */
    UriTemplateMap.prototype.setMethodDetails = function (context, methodName, prop, value) {

        if (!this.map.hasOwnProperty(context)) {
            return;
        }

        if (this.map[context].hasOwnProperty(methodName)) {
            this.map[context][methodName][prop] = value;
        }
    };

    /*
     The method returns all HTTP method types supported for a given context.
     @context: The context for which the HTTP method types must be returned.
     @return: An array containing Strings with the supported HTTP method types.
     */
    UriTemplateMap.prototype.getMethodTypes = function (context) {
        var methods = [];
        for (var methodName in this.map[context]) {
            methods.push(methodName);
        }

        return methods;
    };

    /*
     The method returns an array of all the contexts stored in the mapping.
     @returns: An array containing Strings values of the contexts in the mapping. If no contexts are present
     an empty array is returned.
     */
    UriTemplateMap.prototype.getContextEntries = function () {
        var contextEntries = [];

        for (var contextName in this.map) {
            contextEntries.push(contextName);
        }

        return contextEntries;
    };

    /*
     The method returns the JSON object used to map the contexts.
     */
    UriTemplateMap.prototype.toJSON = function () {
        return this.map;
    };

    /*
     The method converts the uriTemplateMap to an array (An associative array)
     The structure of the object returned is as follows;
     [ {
     CONTEXT_1: [ { context: contextName , methodDetails : methodDetails  ]
     } ]
     */
    UriTemplateMap.prototype.toArray = function () {
        var templates = [];
        var methodDetails;
        for (var contextName in this.map) {
            methodDetails = this.getMethodArray(this.map[contextName]);
            templates.push({
                context: contextName,
                methodDetails: methodDetails
            });
        }

        return templates;
    };

    /*
     The method converts the method details for each context to an array
     The structure of the object returned is;
     [ {methodName: methodName, details: details }]
     */
    UriTemplateMap.prototype.getMethodArray = function (methodDetails) {
        var methodDetailsArray = [];
        for (var methodName in methodDetails) {
            methodDetailsArray.push({
                methodName: methodName,
                details: methodDetails[methodName]
            });
        }

        return methodDetailsArray;
    };

    /*
     The function passes the uriTemplateObject returned by the getAPIDescription method
     of the api module and creates a map.
     @uriTemplateObj: A uriTemplateObject array as obtained from the getAPIDescription method.
     @map: An empty JSON object.
     */
    var initUriTemplates = function (uriTemplateObj, map) {
        var uriTemplate;
        var context;

        for (var templateIndex in uriTemplateObj) {
            uriTemplate = uriTemplateObj[templateIndex];
            context = uriTemplate[0];

            if (!map.hasOwnProperty(context)) {
                map[context] = {};
            }

            //Templates with the same name will overwrite the existing entries
            initUriTemplateEntry(uriTemplate, map[context]);
        }
    };

    /*
     The function  passes each individual array element in a uriTemplateObj.The getAPIDescription
     output consists of an array which may contain information on multiple contexts.
     The structure of this data is;
     Array index 1 : The method types for the context as a comma separated array (csv).
     Array index 2 : The authentication types for each of the methods in method array.
     Array index 3 : The throttling limits for each of the methods in the method array.
     @map: An empty JSON object.
     */
    var initUriTemplateEntry = function (uriTemplateObj, map) {

        var methods = uriTemplateObj[TEMPLATE_METHOD_INDEX] ? uriTemplateObj[TEMPLATE_METHOD_INDEX].split(',') : [];
        var authTypes = uriTemplateObj[TEMPLATE_AUTH_INDEX] ? uriTemplateObj[TEMPLATE_AUTH_INDEX].split(',') : [];
        var throttleLimits = uriTemplateObj[TEMPLATE_THROTTLE_INDEX] ? uriTemplateObj[TEMPLATE_THROTTLE_INDEX].split(',') : [];
        var method;

        for (var methodIndex in methods) {
            method = methods[methodIndex];

            //We assume that the map is empty,if not it is over written.
            map[method] = {};
            map[method][URI_MAP_KEY_AUTH] = authTypes[methodIndex] ? authTypes[methodIndex] : TEMPLATE_AUTH_DEFAULT;
            map[method][URI_MAP_KEY_TIER] = throttleLimits[methodIndex] ? throttleLimits[methodIndex] : TEMPLATE_THROTTLE_DEFAULT;
            map[method][URI_MAP_KEY_TIERDESCRIPTION] = '';
        }
    };

    /*
     The function adds tier description data to the uri template map  by reading the tier data
     in a TierMap object.
     */
    var addTierDescription = function (uriTemplateMap, tierMap) {

        var methods;
        var contextEntries = uriTemplateMap.getContextEntries();
        var context;
        var tierDescription;
        var methodName;
        var methodDetails;

        for (var contextIndex in contextEntries) {

            context = contextEntries[contextIndex];
            methods = uriTemplateMap.getMethodTypes(context);

            for (var methodIndex in methods) {
                methodName = methods[methodIndex];
                methodDetails = uriTemplateMap.getMethodDetails(context, methodName);
                tierDescription = tierMap.getTierDescription(methodDetails.tier);
                uriTemplateMap.setMethodDetails(context, methodName, URI_MAP_KEY_TIERDESCRIPTION, tierDescription);
            }
        }
    }

    return {
        AppInformationService: AppInformationService
    }
})();