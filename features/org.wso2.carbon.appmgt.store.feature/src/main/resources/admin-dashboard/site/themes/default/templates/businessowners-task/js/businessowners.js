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
var businessOwnersArray = new Array();
var editedOwnerId = 0; //if 1 then edit else save
var context = this.jagg.site.context;
var tags = [];
var editor;
var extraFieldCount = 0;

function completeAfter(cm, pred) {
    var cur = cm.getCursor();
    if (!pred || pred()) {
        setTimeout(function () {
            if (!cm.state.completionActive) {
                cm.showHint({completeSingle: false});
            }
        }, 100);
    }
    return CodeMirror.Pass;
}

function completeIfAfterLt(cm) {
    return completeAfter(cm, function () {
        var cur = cm.getCursor();
        return cm.getRange(CodeMirror.Pos(cur.line, cur.ch - 1), cur) == "<";
    });
}

function completeIfInTag(cm) {
    return completeAfter(cm, function () {
        var tok = cm.getTokenAt(cm.getCursor());
        if (tok.type == "string" && (!/['"]/.test(tok.string.charAt(tok.string.length - 1)) || tok.string.length
                                                                                               == 1)) {
            return false;
        }
        var inner = CodeMirror.innerMode(cm.getMode(), tok.state).state;
        return inner.tagName;
    });
}


$(document).ready(function () {
    // Get shared partials
    $.ajax({
               url: context + '/apis/businessowners/list',
               type: 'GET',
               contentType: 'application/json',
               dataType: 'json',
               success: function (data) {
                   for (var i = 0; i < data.length; i++) {
                       businessOwnersArray.push({
                                                    businessOwnerId: data[i].businessOwnerId,
                                                    businessOwnerName: data[i].businessOwnerName,
                                                    businessOwnerEmail: data[i].businessOwnerEmail,
                                                    businessOwnerDescription: data[i].businessOwnerDescription,
                                                    businessOwnerSite: data[i].businessOwnerSite
                                                });
                   }
                   updateOwners();
               },
               error: function () {
               }
           });
    $('#policy-name').select();
});

//save event
$(document).on("click", "#btn-owner-save", function () {


    var businessOwnerName = $('#businessOwnerName').val();
    var businessOwnerEmail = $('#businessOwnerEmail').val();
    var businessOwnerDescription = $('#businessOwnerDescription').val();
    var businessOwnerSiteLink = $('#businessOwnerSite').val();
    var ownerDetails = {};

    if (extraFieldCount > 0) {
        var i = extraFieldCount;
        while (i > 1) {
            var key_id = "#key-".concat(i - 1);
            var val_id = "#value-".concat(i - 1);
            var key = $(key_id).val();
            var value = $(val_id).val();
            ownerDetails[key] = value;
            i--;
        }
    }

    var details = JSON.stringify(ownerDetails);
    $.ajax({
               url: context + '/apis/businessowners/update',
               type: 'POST',
               contentType: 'application/x-www-form-urlencoded',
               async: false,
               data: {
                   "businessOwnerId": editedOwnerId,
                   "businessOwnerName": businessOwnerName,
                   "businessOwnerEmail": businessOwnerEmail,
                   "businessOwnerDescription": businessOwnerDescription,
                   "businessOwnerSite":businessOwnerSiteLink,
                   "businessOwnerDetails": details
               },
               success: function (data) {
                   Showalert("Business Owner updated Successfully", "alert-success", "statusError");
                   location.reload();
               },
               error: function () {
                   Showalert("Business Owner Saving not Successfull", "alert-error", "statusError");
               }
           });
});

function updateOwners() {
    $('#ownerPartialsTable tbody').html("");
    //show empty msg
    if (businessOwnersArray.length > 0) {
        $('.no-owner').hide();
        $('.owner-list').show();
    } else {
        $('.no-owner').show();
        $('.owner-list').hide();
    }
    $.each(businessOwnersArray, function (index, obj) {
        if (obj != null) {

            $('#ownerPartialsTable tbody').append('<tr><td>' +
                                                  obj.businessOwnerName + '</td> <td>' + obj.businessOwnerEmail + '</td> <td>'
                                                  + obj.businessOwnerSite + '</td> <td>' + obj.businessOwnerDescription
                                                  + '</td> <td><a data-target="#entitlement-policy-editor" ' +
                                                  'data-toggle="modal" data-owner-id="' + obj.businessOwnerId
                                                  + '" class="owner-edit-button">' +
                                                  '<i class="icon-edit"></i></a> &nbsp;<a  data-policy-name="'
                                                  + obj.businessOwnerName +
                                                  '"  data-owner-id="' + obj.businessOwnerId
                                                  + '" class="owner-delete-button"><i class="icon-trash"></i>' +
                                                  '</a></td></tr>');
        }
    });

}
function GetDynamicTextBox(index, key, value) {
    var id_key = "key-".concat(index);
    var id_val = "value-".concat(index);
    var id_btn = "btn-".concat(index);
    return '<input name = "key" type="text" id="' + id_key + '" value="' + key + '"/>&nbsp &nbsp &nbsp &nbsp' +
           '<input name="value" type="text" id="' + id_val + '" value="' + value
           + '"/>&nbsp &nbsp &nbsp &nbsp<button id="' + index
           + '" class="btn  btn-info" onClick = "removeFields(this.id)">Remove</button>'
}

function removeFields(index) {
    var id_key = "#" + "key-".concat(index);
    var id_val = "#" + "value-".concat(index);
    var id_btn = "#" + index;
    $(id_key).val("");
    $(id_val).val("");
    $(id_key).hide();
    $(id_val).hide();
    $(id_btn).hide();
}

$(document).on("click", "#btn-owner-add-field", function () {
    var div = $("<div />");
    div.html(GetDynamicTextBox(extraFieldCount, "", ""));
    $("#businessOwnerOther").append(div);
    extraFieldCount++;
});

//edit event
$(document).on("click", ".owner-edit-button", function () {
    var ownerId = $(this).data("ownerId");
    editedOwnerId = ownerId;
    $('#businessOwnerName').val("");
    $('#businessOwnerEmail').val("");
    $('#businessOwnerSite').val("");
    $('#businessOwnerDescription').val("");
    $('.content-section').show();
    var section = $('.title-section-edit');
    var businessOwnerDetails;
    jQuery('html, body').animate({
                                     scrollTop: section.offset().top
                                 }, 1000);

    $.each(businessOwnersArray, function (index, obj) {
        if (obj != null && obj.businessOwnerId == ownerId) {
            $('#businessOwnerName').val(obj.businessOwnerName);
            $('#businessOwnerEmail').val(obj.businessOwnerEmail);
            $('#businessOwnerSite').val(obj.businessOwnerSite);
            $('#businessOwnerDescription').val(obj.businessOwnerDescription);

            $.ajax({
                       url: context + '/apis/businessowners/details/' + obj.businessOwnerId,
                       type: 'GET',
                       contentType: 'application/json',
                       dataType: 'json',
                       success: function (data) {
                          businessOwnerDetails = data.businessOwnerDeatails;
                           var ownerDataObject = JSON.parse(businessOwnerDetails);
                           var i = 1;
                           for(var key in ownerDataObject){
                               var div = $("<div />");
                                           div.html(GetDynamicTextBox(i, key, ownerDataObject[key]));
                                          $("#businessOwnerOther").append(div);
                               i ++;
                               extraFieldCount = i;
                           }
                       },
                       error: function () {
                       }
                   });

    }
});
});



$(document).on("click", ".owner-delete-button", function () {
    var ownerId = $(this).data("ownerId");
    $.ajax({
               url: context + '/apis/businessowners/delete/' + ownerId,
               type: 'DELETE',
               contentType: 'application/json',
               dataType: 'json',
               success: function (response) {
                   updateOwners();
                   Showalert("Owner Deleted Successfully ", "alert-success", "statusSuccess");
                   location.reload();
               },
               error: function (response) {
                   Showalert('Error occured while deleting the business owner', "alert-error", "statusError");
               }
           });
});

$(document).on('click', '#btn-owner-cancel', function(){
    $('.content-section').delay(300).hide(0);
});
$(document).on("click", "#btn-owner-add-new", function () {
    location.replace(context + "/tasks?task=businessowners-new")
});