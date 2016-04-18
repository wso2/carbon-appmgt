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


var apiProvider = jagg.module('manager').getAPIProviderObj();
var log = new Log();
var apiUtil = new Packages.org.wso2.carbon.appmgt.impl.utils.AppManagerUtil;



function deleteOwner(ownerId){
    var partialId = deleteBusinessOwner(ownerId);
    var response = {"id": partialId};
    return response;
}
function deleteBusinessOwner(ownerId){
    return apiProvider.deleteBusinessOwner(ownerId);
}
function saveOwner(ownerName, ownerMail, description, sitelink, keys, values) {
    var partialId = saveBusinessOwner(ownerName, ownerMail, description, sitelink, keys, values);
    var response = {"id": partialId};
    return response;
}
function saveBusinessOwner(ownerName, ownerMail, description, sitelink, keys, values) {
    return apiProvider.saveBusinessOwner(ownerName, ownerMail, description, sitelink, keys, values);
}

function updateOwner(ownerID, ownerName, ownerMail, description, sitelink, keys, values) {
    var partialId = updateBusinessOwner(ownerID,ownerName, ownerMail, description, sitelink, keys, values);
    var response = {"id": partialId};
    return response;
}
function updateBusinessOwner(ownerID, ownerName, ownerMail, description, sitelink, keys, values) {
    return apiProvider.updateBusinessOwner(ownerID, ownerName, ownerMail, description, sitelink, keys, values);
}

var getOwnerList = function () {
    return apiProvider.getBusinessOwnerList();
};