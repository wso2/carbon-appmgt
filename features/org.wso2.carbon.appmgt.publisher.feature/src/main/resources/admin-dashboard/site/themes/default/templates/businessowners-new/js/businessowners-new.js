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
var Showalert = function(msg, type, target) {
    type = type || 'info';
    $('#'+ target)
        .removeClass()
        .addClass(type)
        .addClass('alert')
        .stop()
        .fadeIn()
        .delay(3000)
        .fadeOut()
        .find('#statusErrorSpan').html(msg);
    var section=$('.title-section');
    jQuery('html, body').animate({
                                     scrollTop: section.offset().top
                                 }, 1000);
}


var context = "/" + window.location.pathname.split("/")[1];  // default value is "/admin-dashboard"
var fieldCount = 0;

function completeAfter(cm, pred) {
    var cur = cm.getCursor();
    if (!pred || pred()) setTimeout(function() {
        if (!cm.state.completionActive)
            cm.showHint({completeSingle: false});
    }, 30000);
    return CodeMirror.Pass;
}

function completeIfAfterLt(cm) {
    return completeAfter(cm, function() {
        var cur = cm.getCursor();
        return cm.getRange(CodeMirror.Pos(cur.line, cur.ch - 1), cur) == "<";
    });
}

function completeIfInTag(cm) {
    return completeAfter(cm, function() {
        var tok = cm.getTokenAt(cm.getCursor());
        if (tok.type == "string" && (!/['"]/.test(tok.string.charAt(tok.string.length - 1)) || tok.string.length == 1)) return false;
        var inner = CodeMirror.innerMode(cm.getMode(), tok.state).state;
        return inner.tagName;
    });
}

function isEmail(email) {
    var regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    return regex.test(email);
}

function checkFilled() {
    var inputMail = document.getElementById("businessOwnerEmail");
    var inputName = document.getElementById("businessOwnerName");
    var inputSite = document.getElementById("businessOwnerSite");
    var inputDesc = document.getElementById("businessOwnerDesc");
  //  document.getElementById("newProperties").style.visibility = "hidden";

    if (inputDesc.value != "") {
        inputDesc.style.borderColor = "green";
    }
    if (inputSite.value != "") {
        inputSite.style.borderColor = "green";
    }
    if (inputName.value != "") {
        inputName.style.borderColor = "green";
    }
    if (isEmail(inputMail.value)) {
        inputMail.style.borderColor = "green";
    }
}

checkFilled();
//add new fields
$(document).on("click", "#btn-owner-partial-new", function () {
    var div = $("<div />");
    div.html(GetDynamicTextBox(fieldCount));
    fieldCount++;
    $("#TextBoxContainer").append(div);
});

function GetDynamicTextBox(value) {
    var id_key = "key-".concat(value);
    var id_val = "value-".concat(value);
    if(value == 0){
        return '<h5>Property &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp '
               + '&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp Value</h5><br>'
               + '<input name = "key" type="text" id="'+id_key+'"/>&nbsp &nbsp &nbsp &nbsp' +
               '<input name="value" type="text" id="'+id_val+'"/>'

    }
    return '<input name = "key" type="text" id="'+id_key+'"/>&nbsp &nbsp &nbsp &nbsp' +
           '<input name="value" type="text" id="'+id_val+'"/>'
}
//save event
$(document).on("click", "#btn-owner-save", function () {

    var ownerName = $('#businessOwnerName').val();
    var ownerMail = $('#businessOwnerEmail').val();
    var description = $('#businessOwnerDesc').val();
    var siteLink = $('#businessOwnerSite').val();
    var ownerDetails = {};

    if(fieldCount > 0){
        var i = fieldCount;
        while(i > 0){
            var key_id = "#key-".concat(i-1);
            var val_id = "#value-".concat(i-1);
            var key = $(key_id).val();
            var value = $(val_id).val();
            ownerDetails[key] = value;
            i--;
        }
    }
    var details = JSON.stringify(ownerDetails);
    if(!isEmail(ownerMail)){
        Showalert("Enter a Valid E-mail address","alert-error", "statusError");
        return;
    }
    if(ownerName == "" || ownerName == null){
        Showalert("Owner Name Cannot be empty","alert-error", "statusError");
        return;
    }
    if(description == "" || description == null){
        Showalert("Owner businessOwnerDescription Cannot be empty","alert-error", "statusError");
        return;
    }
    if(siteLink == "" || siteLink == null){
        Showalert("Owner site Cannot be empty","alert-error", "statusError");
        return;
    }

    $.ajax({
               url: context + '/apis/businessowners/save',
               type: 'POST',
               contentType: 'application/x-www-form-urlencoded',
               async: false,
               data: {
                   "businessOwnerName": ownerName,
                   "businessOwnerEmail": ownerMail,
                   "businessOwnerDescription": description,
                   "businessOwnerSite": siteLink,
                   "businessOwnerProperties": details
               },
               success: function (data) {
                   Showalert("Business Owner Saved Successfully","alert-success", "statusError");
               },
               error: function () {
                   Showalert("Error occured while adding business owner","alert-error", "statusError");
               }
           });

location.replace(context + "/tasks?task=businessowners");
   });

