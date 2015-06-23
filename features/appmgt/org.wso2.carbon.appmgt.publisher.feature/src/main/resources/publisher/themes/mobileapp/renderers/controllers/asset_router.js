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
var permissions=require('/modules/permissions.js').permissions;
var config = require('/config/publisher.json');

var render=function(theme,data,meta,require){
    //var _url = "/publisher/asset/"  + data.meta.shortName + "/" + data.info.id + "/edit"

    var user=server.current(session);
    var um=server.userManager(user.tenantId);
    var createMobileAppAuthorized = permissions.isAuthorized(user.username, config.permissions.mobileapp_create, um);
    var updateMobileAppAuthorized = permissions.isAuthorized(user.username, config.permissions.mobileapp_update, um);

    if(!updateMobileAppAuthorized){
        response.sendError(400);
        return;
    }

	var listPartial='view-asset';
    var heading = "";
    var mobileNotifications = session.get('mobileNotifications');
    var mobileNotificationCount = session.get('mobileNotificationCount');
	//Determine what view to show
	switch(data.op){
	case 'create':
		listPartial='add-asset';
		if(data.data.meta.shortName=='mobileapp'){
			//log.info('Special rendering case for mobileapp-using add-mobilepp.hbs');
			listPartial='add-mobileapp';
		}
        heading = "Create New Mobile App";
		break;
	case 'view':
        listPartial='view-mobileapp';
        data = require('/helpers/edit-asset.js').screenshots(data);
        if(data.artifact.attributes.overview_platform == 'android' && data.artifact.attributes.overview_type == 'public') {
            data.artifact.attributes.overview_identifier = data.artifact.attributes.overview_packagename;
        }else if(data.artifact.attributes.overview_platform == 'ios' && data.artifact.attributes.overview_type == 'public'){
            data.artifact.attributes.overview_identifier = data.artifact.attributes.overview_appid;
        }
         var createdDate = new Date();
        createdDate.setTime(data.artifact.attributes.overview_createdtime);
        data.artifact.attributes['overview_createdtime'] = createdDate.toUTCString();
        heading = data.artifact.attributes.overview_displayName;
        break;
    case 'edit':
        listPartial='edit-asset';
        if(data.data.meta.shortName=='mobileapp'){
			//log.info('Special rendering case for mobileapp-using edit-mobilepp.hbs');
			listPartial='edit-mobileapp';
		}
        data = require('/helpers/edit-asset.js').selectCategory(data);
        data = require('/helpers/edit-asset.js').screenshots(data);

        data = require('/helpers/splitter.js').splitData(data);
        if(data.artifact.lifecycleState == "Published"){
            response.sendError(400);
            return;
        }
        heading = data.newViewData.name.value;
        break;
    case 'lifecycle':
        listPartial='lifecycle-asset';
        heading = "Lifecycle";
        break;
    case 'versions':
        listPartial='versions-asset';
        heading = "Versions";
        break;
	default:
		break;
	}

    var breadCrumbData = require('/helpers/breadcrumb.js').generateBreadcrumbJson(data);
    breadCrumbData.activeRibbonElement = listPartial;
    breadCrumbData.createMobileAppPerm = createMobileAppAuthorized;

	theme('single-col-fluid', {
        title: data.title,
     	header: [
            {
                partial: 'header',
                context: data
            }
        ],
        ribbon: [
            {
                partial: 'ribbon',
		        context:{
                    active:listPartial,
                    isNotReviwer:data.isNotReviwer,
                    createMobileAppPerm:createMobileAppAuthorized,
                    mobileNotifications : mobileNotifications,
                    mobileNotificationCount: mobileNotificationCount
                }
            }
        ],
        leftnav: [
            {
                partial: 'left-nav',
                context: require('/helpers/left-nav.js').generateLeftNavJson(data, listPartial)
            }
        ],
        listassets: [
            {
                partial:listPartial,
		        context: data
            }
        ],
        heading: [
            {
                partial:'heading',
                context: {title:heading,menuItems:require('/helpers/left-nav.js').generateLeftNavJson(data, listPartial)}
            }
        ]
    });
};
