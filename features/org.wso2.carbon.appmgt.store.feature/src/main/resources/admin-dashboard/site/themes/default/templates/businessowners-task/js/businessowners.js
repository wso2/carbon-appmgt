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

$(document).ready(function() {
    $('#ownersTable').DataTable({
          "processing": true,
          "serverSide": true,
          "paging": true,
          "ordering": true,
          "order": [[1, "asc"]],
          "ajax": context + '/apis/businessowners/filtered/datatable',
          "columnDefs": [
              {
                  "targets": [0],
                  "visible": false
              },
             {
                "render": function (data, type, row) {
                    return '<a data-target="" ' +
                           'data-toggle="modal" data-owner-id="' + row[0] + '" class="owner-edit-button">' +
                           '<i class="icon-edit"></i></a> &nbsp<a'
                           +' class="owner-delete-button" data-owner-id="' + row[0] + '"><i class="icon-trash"></i></a>';
                },
                "targets": -1
          }]
    });
});

//save event
$(document).on("click", "#btn-owner-save", function () {


    var businessOwnerName = $('#businessOwnerName').val();
    var businessOwnerEmail = $('#businessOwnerEmail').val();
    var businessOwnerDescription = $('#businessOwnerDescription').val();
    var businessOwnerSiteLink = $('#businessOwnerSite').val();
    var ownerProperties = {};

    if (extraFieldCount > 0) {
        var i = extraFieldCount;
        while (i > 1) {
            var key_id = "#key-".concat(i - 1);
            var val_id = "#value-".concat(i - 1);
            var showInStoreId = "showInStore-".concat(i - 1);
            var key = $(key_id).val();
            var value = [];
            value.push($(val_id).val());
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
                   "businessOwnerProperties": properties
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
                                                  + '</td> <td><a data-target="" ' +
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
function GetDynamicTextBox(hasHeader, index, key, value, showInStore) {
    var id_key = "key-".concat(index);
    var id_val = "value-".concat(index);
    var check_val = "showInStore-".concat(index);
    var id_btn = "btn-".concat(index);
    var checkBoxStatus;
    if (showInStore) {
        checkBoxStatus = "checked";
    }
    if (!hasHeader) {
        var tableHeader =  '<div class="row-fluid div-custom header-div"><div class="span3">Property</div>'
                           + '<div class="span3">Value</div><div class="span2">Show in Store</div>'
                           + '<div class="span2"></div></div> ';
        $("#editCustomProperties").append(tableHeader);
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

function removeFields(index) {
    var id_key = "#" + "key-".concat(index);
    var id_val = "#" + "value-".concat(index);
    var check_val = "showInStore-".concat(index);
    var id_btn = "#" + index;
    $(id_key).val("");
    $(id_val).val("");
    $(id_key).hide();
    $(id_val).hide();
    $(check_val).hide();
    $(id_btn).hide();
}

$(document).on("click", "#btn-owner-add-field", function () {
    var div = $("<div />");
    div.html(GetDynamicTextBox(extraFieldCount, "", "", ""));
    $("#businessOwnerOther").append(div);
    extraFieldCount++;
});

//edit event
$(document).on("click", ".owner-edit-button", function () {
    $('#businessOwnerOther').empty();
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
    $.ajax({
               url: context + '/apis/businessowners/' + editedOwnerId,
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
                   var noOfCustomProperties = Object.keys(ownerDataObject).length;
                   if(noOfCustomProperties > 0) {
                       var i = 1;
                       var tableHeader =  '<div class="row-fluid div-custom header-div"><div class="span3">Property</div>'
                                          + '<div class="span3">Value</div><div class="span2">Show in Store</div>'
                                          + '<div class="span2"></div></div> ';
                       $("#editCustomProperties").append(tableHeader);
                       var hasHeader = true;
                       for(var key in ownerDataObject){
                           var ownerProperties = ownerDataObject[key];
                           var ownerProperty = JSON.parse(JSON.stringify(ownerProperties));
                           GetDynamicTextBox(hasHeader, i, key, ownerProperty["propertyValue"], ownerProperty["isShowingInStore"]);
                           i ++;
                           extraFieldCount = i;
                       }
                   }
               },
               error: function () {
               }
           });
});



$(document).on("click", ".owner-delete-button", function () {
    var ownerId = $(this).data("ownerId");
    var ownerName = $(this).closest('tr').find("td:first").text();
    var isConfirmed = confirm("Are you sure you want to delete the business owner " + ownerName + "?");
    if (isConfirmed) {
        $.ajax({
                   url: context + '/apis/businessowners/' + ownerId,
                   type: 'DELETE',
                   contentType: 'application/json',
                   dataType: 'json',
                   success: function (response) {
                       if(response) {
                           updateOwners();
                           Showalert("Business Owner : " + ownerName + " Deleted Successfully ", "alert-success", "statusSuccess");
                           location.reload();
                       } else {
                           Showalert('Business Owner : ' + ownerName + ' is assigned to one or more apps. Please remove'
                                     + ' it from them before deleting.', "alert-error", "statusError");
                       }
                   },
                   error: function (response) {
                       Showalert('Error occured while deleting the business owner : ' + ownerName, "alert-error", "statusError");
                   }
               });
    }

});

$(document).on('click', '#btn-owner-cancel', function(){
    $('.content-section').delay(300).hide(0);
});
$(document).on("click", "#btn-owner-add-new", function () {
    location.replace(context + "/tasks?task=businessowners-new")
});
