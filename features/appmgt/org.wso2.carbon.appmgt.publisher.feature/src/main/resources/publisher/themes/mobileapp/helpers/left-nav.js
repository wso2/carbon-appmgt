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

var getTypeObj, breadcrumbItems;
var deploymentManagement = require('/modules/deployment/deployment.manager.js').deploymentManagementModule();
var deploymentManager = deploymentManagement.cached();

var server = require('store').server;
var permissions = require('/modules/permissions.js').permissions;
var config = require('/config/publisher.json');

var log = new Log();

breadcrumbItems = deploymentManager.getAssetData()

var generateLeftNavJson = function (data, listPartial) {

	var leftNavItems = {leftNavLinks: []};

	if (listPartial != 'view-mobileapp') {
		return leftNavItems;
	}

	if (data.artifact == null) {
		return leftNavItems;
	}

	var user = server.current(session);
	var um = server.userManager(user.tenantId);
	var createActionAuthorized = permissions.isAuthorized(user.username, config.permissions.webapp_create, um);
	if (!createActionAuthorized) {
		return leftNavItems;
	}

	var editEnabled = permissions.isEditPermitted(user.username, data.artifact.path, um);
	if (user.hasRoles(["admin"])) {
		editEnabled = true;
	}
	if (data.artifact.lifecycleState == "Published") {
		editEnabled = false;
	}

	if (editEnabled) {
		leftNavItems = {
			leftNavLinks: [
				{
					name: "Edit",
					iconClass: "icon-edit",
					additionalClasses: (listPartial == "edit-asset" ) ? "active" : null,
					url: "/publisher/asset/operations/edit/" + data.shortName + "/" + data.artifact.id,
					isEditable: editEnabled
				}
			]
		};
	} else {
		leftNavItems = {
			leftNavLinks: [
				{
					name: "Edit",
					iconClass: "icon-edit",
					additionalClasses: (listPartial == "edit-asset" ) ? "active" : false,
					url: "#",
					title: "Edit action not permitted.",
					isEditable: editEnabled
				}
			]
		};
	}
	return leftNavItems;

};

getTypeObj = function (type) {
	for (item in breadcrumbItems) {
		var obj = breadcrumbItems[item]
		if (obj.assetType == type) {
			return obj;
		}
	}
};
