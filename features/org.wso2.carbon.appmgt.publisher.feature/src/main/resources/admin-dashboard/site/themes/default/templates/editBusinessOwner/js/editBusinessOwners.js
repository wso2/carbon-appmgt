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

var Showalert = function (msg, type, target) {
    type = type || 'info';
    $('#' + target)
        .removeClass()
        .addClass(type)
        .addClass('alert')
        .stop()
        .fadeIn()
        .delay(3000)
        .fadeOut()
        .find('#statusErrorSpan').html(msg);
    var section = $('.title-section');
    jQuery('html, body').animate({
                                     scrollTop: section.offset().top
                                 }, 1000);
    ;
}
var context = this.jagg.site.context;
var noOfCustomFields = 0;

$(document).ready(function() {
    $('#businessOwnerOther').empty();
    var ownerId = document.getElementById('businessOwnerId').innerHTML;
    $('#businessOwnerName').val();
    $('#businessOwnerEmail').val();
    $('#businessOwnerSite').val();
    $('#businessOwnerDescription').val();
    $('.content-section').show();
    var section = $('.title-section-edit');
    var businessOwnerDetails;
    $.ajax({
               url: context + '/apis/businessowners/' + ownerId,
               type: 'GET',
               contentType: 'application/json',
               dataType: 'json',
               success: function (data) {
                   $('#businessOwnerName').val(data.businessOwnerName);
                   $('#businessOwnerEmail').val(data.businessOwnerEmail);
                   $('#businessOwnerSite').val(data.businessOwnerSite);
                   $('#businessOwnerDescription').val(data.businessOwnerDescription);
                   businessOwnerDetails = data.businessOwnerProperties;
                   var ownerDataObject = JSON.parse(businessOwnerDetails);
                   var noOfFields = Object.keys(ownerDataObject).length;
                   if(noOfFields > 0) {
                       var tableHeader =  '<div class="row-fluid div-custom header-div"><div class="span3">Property</div>'
                                          + '<div class="span3">Value</div><div class="span2">Show in Store</div>'
                                          + '<div class="span2"></div></div> ';
                       $("#editCustomProperties").append(tableHeader);
                       for(var key in ownerDataObject){
                           var ownerProperties = ownerDataObject[key];
                           var ownerProperty = JSON.parse(JSON.stringify(ownerProperties));
                           addCustomProperties(noOfCustomFields, key, ownerProperty["propertyValue"], ownerProperty["isShowingInStore"]);
                           noOfCustomFields ++;
                       }
                   }
               },
               error: function () {
               }
           });
});

$(document).on("click", "#btn-owner-save", function () {
    var businessOwnerName = $('#businessOwnerName').val();
    var businessOwnerEmail = $('#businessOwnerEmail').val();
    var businessOwnerDescription = $('#businessOwnerDescription').val();
    var businessOwnerSiteLink = $('#businessOwnerSite').val();
    var ownerId = document.getElementById('businessOwnerId').innerHTML;

    var ownerProperties = {};
    if (noOfCustomFields > 0) {
        var i = noOfCustomFields;
        while (i > 0) {
            var keyId = "#key-".concat(i - 1);
            var valueId = "#value-".concat(i - 1);
            var showInStoreId = "showInStore-".concat(i - 1);
            var valueElement = document.getElementById(valueId);
                var key = $(keyId).val();
                var value = [];
                value.push($(valueId).val());
                if(document.getElementById(showInStoreId).checked) {
                    value.push(true);
                } else {
                    value.push(false);
                }
                ownerProperties[key] = value;
            i--;
        }
    }
    var properties = JSON.stringify(ownerProperties);
    if(!isEmail(businessOwnerEmail)){
        Showalert("Enter a Valid E-mail address","alert-error", "statusError");
        return;
    }
    if(businessOwnerName == null || businessOwnerName.trim() == ""){
        Showalert("Owner Name cannot be null or empty","alert-error", "statusError");
        return;
    }
    $.ajax({
               url: context + '/apis/businessowners/update',
               type: 'POST',
               contentType: 'application/x-www-form-urlencoded',
               async: false,
               data: {
                   "businessOwnerId": ownerId,
                   "businessOwnerName": businessOwnerName,
                   "businessOwnerEmail": businessOwnerEmail,
                   "businessOwnerDescription": businessOwnerDescription,
                   "businessOwnerSite":businessOwnerSiteLink,
                   "businessOwnerProperties": properties
               },
               success: function (data) {
                   var obj = JSON.parse(data);
                   var isSucceed = obj.success;
                   if (isSucceed) {
                       Showalert("Business Owner updated Successfully","alert-success", "statusError");
                       location.replace(context + "/tasks?task=businessowners");
                   } else {
                       var response = obj.response;
                       var respondMessage = response.message;
                       Showalert(respondMessage,"alert-error", "statusError");
                   }
                   
               },
               error: function () {
                   Showalert("Error occurred while business owner updating.", "alert-error", "statusError");
               }
           });
});


function removeFields(index) {
    var id_key = "#" + "key-".concat(index);
    var id_val = "#" + "value-".concat(index);
    var check_val = "#showInStore-".concat(index);
    var id_btn = "#" + index;
    $(id_key).val("");
    $(id_val).val("");
    $(id_key).hide();
    $(id_val).hide();
    $(check_val).hide();
    $(id_btn).hide();
}


function addCustomProperties(index, key, value, showInStore) {
    var id_key = "key-".concat(index);
    var id_val = "value-".concat(index);
    var check_val = "showInStore-".concat(index);
    var id_btn = "btn-".concat(index);
    var checkBoxStatus;
    if (showInStore) {
        checkBoxStatus = "checked";
    }
    var fieldValue = '<div class="row-fluid"><div class="span3 div-custom">'
                         + '<input name = "key" type="text" id="'+id_key+'" value="' + key + '"/></div>'
                         + '<div class="span3 div-custom"><input name="value" type="text" id="' + id_val
                         +'" value="' + value +'"/></div><div class="span2 div-custom"><input type="checkbox" '
                         + 'name="showInStore" id="'+check_val+'"' + checkBoxStatus +'/></div><div class="span2">'
                         + '<button id="' + index + '" class="btn  btn-info" onClick = "removeFields(this.id)">Remove'
                         + '</button></div></div>';
    $("#editCustomProperties").append(fieldValue);

}

$(document).on("click", "#btn-owner-partial-new", function () {
    $("#editCustomProperties").append(GetDynamicTextBox(noOfCustomFields));
    noOfCustomFields++;
});

$(document).on("click", "#btn-owner-cancel", function () {
    location.replace(context + "/tasks?task=businessowners");
});

function GetDynamicTextBox(value) {
    var propertyKey = "key-".concat(value);
    var PropertyValue = "value-".concat(value);
    var showInStore = "showInStore-".concat(value);
    if(value == 0){
        return '<div class="row-fluid div-custom header-div"><div class="span3">Property</div><div class="span3">Value</div><div'
               + ' class="span2">Show in Store</div></div><div class="row-fluid div-custom"><div class="span3">'
               + '<input name = "key" type="text" id="'+propertyKey+'"/></div>'
               + '<div class="span3"><input name="value" type="text" id="'+PropertyValue+'"/></div>'
               + '<div class="span2"><input type="checkbox" name="showInStore" id="'+showInStore+'"/></div><div class="span1">'
               + '<button id="' + value + '" class="btn  btn-info" onClick = "removeFields(this.id)">Remove'
               + '</button></div></div> ';
    }
    return '<div class="row-fluid div-custom"><div class="span3">'
           + '<input name = "key" type="text" id="'+propertyKey+'"/></div>'
           + '<div class="span3"><input name="value" type="text" id="'+PropertyValue+'"/></div>'
           + '<div class="span2"><input type="checkbox" name="showInStore" id="'+showInStore+'"/></div><div class="span1">'
           + '<button id="' + value + '" class="btn  btn-info" onClick = "removeFields(this.id)">Remove'
           + '</button></div></div>';
}

function isEmail(email) {
    var regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    return regex.test(email);
}
