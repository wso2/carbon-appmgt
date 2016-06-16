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
	Description: Renders the asset.jag view
	Filename:asset.js
	Created Date: 29/7/2013
*/

var server = require('store').server;
var permissions = require('/modules/permissions.js').permissions;
var config = require('/config/publisher.json');

var render = function(theme, data, meta, require) {

    var user = server.current(session);
    var um = server.userManager(user.tenantId);
    var createMobileAppAuthorized = permissions.isAuthorized(user.username, config.permissions.mobileapp_create, um);
    var updateMobileAppAuthorized = permissions.isAuthorized(user.username, config.permissions.mobileapp_update, um);
    var apiProvider = jagg.module('manager').getAPIProviderObj();
    var typeList = apiProvider.getEnabledAssetTypeList();
    data.typeList = typeList;
    var listPartial = 'view-asset';
    var heading = "";
    var mobileNotifications = session.get('mobileNotifications');
    var mobileNotificationCount = session.get('mobileNotificationCount');

    //Determine what view to show
    switch (data.op) {
        case 'create':
            listPartial = 'add-asset';
            if (data.data.meta.shortName == 'mobileapp') {
                listPartial = 'add-mobileapp';
            }
            heading = "Create New Mobile App";
            break;
        case 'view':
            listPartial = 'view-mobileapp';
            data = require('/helpers/edit-asset.js').screenshots(data);
            var createdDate = new Date();
            createdDate.setTime(data.artifact.attributes.overview_createdtime);
            data.artifact.attributes['overview_createdtime'] = createdDate.toUTCString();
            heading = data.artifact.attributes.overview_displayName;
            break;
        case 'edit':
            var editEnabled = permissions.isEditPermitted(user.username, data.artifact.path, um);
            if (data.artifact.lifecycleState == "Published") {
                editEnabled = false;
            }
            if (user.hasRoles(["admin"]) || updateMobileAppAuthorized) {
                editEnabled = true;
            }
            if (!editEnabled) {
                response.sendError(404);
            }
            listPartial = 'edit-mobileapp';
            data = require('/helpers/edit-asset.js').selectCategory(data);
            data = require('/helpers/edit-asset.js').screenshots(data);

            data = require('/helpers/splitter.js').splitData(data);
            if (data.artifact.lifecycleState == "Published") {
                response.sendError(400);
                return;
            }
            heading = data.newViewData.name.value;
            break;
        case 'copyapp':
            listPartial = 'add-mobileapp-version';
            var editHelper = require('/helpers/edit-asset.js');
            data = editHelper.selectCategory(data);
            data = editHelper.screenshots(data);

            data = require('/helpers/splitter.js').splitData(data);
            heading = data.artifact.attributes.overview_displayName;
            break;
        case 'lifecycle':
            listPartial = 'lifecycle-asset';
            heading = "Lifecycle";
            break;
        case 'versions':
            listPartial = 'versions-asset';
            heading = "Versions";
            break;
        default:
            break;
    }

    var breadCrumbData = require('/helpers/breadcrumb.js').generateBreadcrumbJson(data);
    breadCrumbData.activeRibbonElement = listPartial;
    breadCrumbData.createMobileAppPerm = createMobileAppAuthorized;

    //setting categories
    for (var i = 0; i < data.rxtTemplate.contentBlock.tables.length; i++) {
        if (data.rxtTemplate.contentBlock.tables[i].name === 'overview') {
            for (var j = 0; j < data.rxtTemplate.contentBlock.tables[i].fieldsArray.length; j++) {
                if (data.rxtTemplate.contentBlock.tables[i].fieldsArray[j].name.name == "category") {
                    data.categories = (data.rxtTemplate.contentBlock.tables[i].fieldsArray[j].values);
                }
            }
        }
    }

    theme('single-col-fluid', {
        title: data.title,
        header: [{
            partial: 'header',
            context: data
        }],
        ribbon: [{
            partial: 'ribbon',
            context: {
                active: listPartial,
                isNotReviwer: data.isNotReviwer,
                createMobileAppPerm: createMobileAppAuthorized,
                mobileNotifications: mobileNotifications,
                mobileNotificationCount: mobileNotificationCount
            }
        }],
        leftnav: [{
            partial: 'left-nav',
            context: require('/helpers/left-nav.js').generateLeftNavJson(data, listPartial)
        }],
        listassets: [{
            partial: listPartial,
            context: data
        }],
        heading: [{
            partial: 'heading',
            context: {
                title: heading,
                menuItems: require('/helpers/left-nav.js').generateLeftNavJson(data, listPartial)
            }
        }]
    });
};