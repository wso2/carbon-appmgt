/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

$(function () {

    //if user directly access the overview page, check the anonymous condition.
    //if anonymous mode is not allowed and user is not authenticated, prompt the login page
    var loggedUser = $("#hdnUsertId").val();
    var allowAnonymous = $("#allowAnonymous").val();
    var skipGateway = $("#skipGateway").val();
    var subscriptionOff = $("#subscription-off").val();
    //hide html body when loading
    $('#wrap').css('visibility', 'hidden');

    if (loggedUser == "" || loggedUser == null) {
//        if (allowAnonymous.toUpperCase() != "TRUE") {
//            var localIP = $("#assetsLocalIP").val();
//            var port = $("#assetshttpsPort").val()
//            location.href = localIP + ":" + port + "/store/login";
//        }
//        e.preventDefault();
//        e.stopPropagation();
    }

    //show html body after loading
    $('#wrap').css('visibility', 'visible');



    var jsonObj = {
        "isActive": "1",
        "path": caramel.context + "/apis/eventpublish/",
        "appData": {
            "appId": $('#hdnAssetId').val(),
            "userId": $('#hdnUsertId').val(),
            "tenantId": $('#hdnTenantId').val(),
            "appName": $('#hdnOverviewName').val(),
            "appVersion": $("#hdnOverviewVersion").val(),
            "context": $("#hdnOverviewContext").val()
        },
        "appControls": {"0": "a"},
        "publishedPeriod": "1200000",
        "pageName": "Store_Overview"
    };
    initializeUserActivity("page-load", jsonObj);

    $("#gatewayURL").on('click', function (e) {
        //check if subscribed only if skip gateway disabled
        if ((skipGateway == "false")) {
            if ($('#hdnUsertId').val() != "" && (allowAnonymous != "TRUE")) {
                if(subscriptionOff != "true") {
                    var isSubscribed = $('#subscribed').val();
                    if (isSubscribed.toLowerCase() === 'false') {
                        noty({
                                 text : 'You have not subscribed to this app',
                                 'layout' : 'center',
                                 'timeout': 1500,
                                 'modal' : true

                             });
                        e.preventDefault();
                        e.stopPropagation();
                    }
                }

            }
        }
    });
});
