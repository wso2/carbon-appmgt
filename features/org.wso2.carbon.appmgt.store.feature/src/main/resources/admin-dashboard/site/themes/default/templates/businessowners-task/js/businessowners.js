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
var businessOwnersArray = new Array(); // xacml policy details array
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
                                                    owner_id: data[i].owner_id,
                                                    owner_name: data[i].owner_name,
                                                    owner_mail: data[i].owner_email,
                                                    owner_desc: data[i].owner_desc,
                                                    owner_site: data[i].owner_site,
                                                    keys: data[i].keys,
                                                    values: data[i].values
                                                });
                   }
                   updatePolicyPartial();
               },
               error: function () {
               }
           });
    $('#policy-name').select();
});

//save event
$(document).on("click", "#btn-owner-save", function () {

    var ownerName = $('#owner-name').val();
    var ownerMail = $('#owner-email').val();
    var description = $('#owner-desc').val();
    var siteLink = $('#owner-site').val();
    var keys = "";
    var values = "";

    if (extraFieldCount > 0) {
        var i = extraFieldCount;
        while (i > 1) {
            var key_id = "#key-".concat(i - 1);
            var val_id = "#value-".concat(i - 1);
            var key = $(key_id).val();
            var value = $(val_id).val();
            keys = keys.concat('/', key);
            values = values.concat('/', value);
            i--;
        }
    }


    $.ajax({
               url: context + '/apis/businessowners/update',
               type: 'POST',
               contentType: 'application/x-www-form-urlencoded',
               async: false,
               data: {
                   "owner_id": editedOwnerId,
                   "ownerName": ownerName,
                   "ownerMail": ownerMail,
                   "description": description,
                   "sitelink": siteLink,
                   "keys": keys,
                   "values": values
               },
               success: function (data) {

                   Showalert("Business Owner Saved Successfully", "alert-success", "statusError");
                   location.reload();
                   location.reload();
               },
               error: function () {
               }
           });
});

function updatePolicyPartial() {
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

            $('#ownerPartialsTable tbody').append('<tr><td>' + obj.owner_id + '</td> <td>' +
                                                  obj.owner_name + '</td> <td>' + obj.owner_mail + '</td> <td>'
                                                  + obj.owner_site + '</td> <td>' + obj.owner_desc
                                                  + '</td> <td><a data-target="#entitlement-policy-editor" ' +
                                                  'data-toggle="modal" data-policy-id="' + obj.owner_id
                                                  + '" class="policy-edit-button">' +
                                                  '<i class="icon-edit"></i></a> &nbsp;<a  data-policy-name="'
                                                  + obj.owner_name +
                                                  '"  data-policy-id="' + obj.owner_id
                                                  + '" class="policy-delete-button"><i class="icon-trash"></i>' +
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
    $("#owner-others").append(div);
    extraFieldCount++;
});

//edit event
$(document).on("click", ".policy-edit-button", function () {
    var policyId = $(this).data("policyId");
    editedOwnerId = policyId;
    $('#owner_name').val("");
    $('#owner-email').val("");
    $('#owner_site').val("");
    $('#owner_desc').val("");
    $('.content-section').show();
    var section = $('.title-section-edit');
    jQuery('html, body').animate({
                                     scrollTop: section.offset().top
                                 }, 1000);
    // editor.setValue("");

    $.each(businessOwnersArray, function (index, obj) {
        if (obj != null && obj.owner_id == policyId) {
            $('#owner-name').val(obj.owner_name);
            $('#owner-email').val(obj.owner_mail);
            $('#owner-site').val(obj.owner_site);
            $('#owner-desc').val(obj.owner_desc);

            if (obj.keys != null && obj.values != null) {
                var keySet = obj.keys.split("/");
                var valueSet = obj.values.split("/");
                for (var i = 1; i < keySet.length; i++) {
                    var div = $("<div />");
                    div.html(GetDynamicTextBox(i, keySet[i], valueSet[i]));
                    $("#owner-others").append(div);
                }

                extraFieldCount = keySet.length;
            }
        }
    });
});


//delete event


$(document).on("click", ".policy-delete-button", function () {
    var ownerId = $(this).data("policyId");
    $.ajax({
               url: context + '/apis/businessowners/delete/' + ownerId,
               type: 'DELETE',
               contentType: 'application/json',
               dataType: 'json',
               success: function (response) {
                   updatePolicyPartial();
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