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


//new button click event
$(document).on("click", "#btn-policy-new", function () {
    resetControls();
});

function isEmail(email) {
    var regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    return regex.test(email);
}

function checkFilled() {
    var inputMail = document.getElementById("owner-email");
    var inputName = document.getElementById("owner-name");
    var inputSite = document.getElementById("owner-site");
    var inputDesc = document.getElementById("owner-desc");

    if (inputDesc.value != "") {
        inputDesc.style.borderColor = "green";
    }
    else{
        inputDesc.style.borderColor = "red";
    };
    if (inputSite.value != "") {
        inputSite.style.borderColor = "green";
    }
    else{
        inputSite.style.borderColor = "red";
    };
    if (inputName.value != "") {
        inputName.style.borderColor = "green";
    }
    else{
        inputName.style.borderColor = "red";
    };


    if (isEmail(inputMail.value)) {
        inputMail.style.borderColor = "green";
    }
    else{
        inputMail.style.borderColor = "red";
    };
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
        return '<h5>Key &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp Value</h5><br>'
               + '<input name = "key" type="text" id="'+id_key+'"/>&nbsp &nbsp &nbsp &nbsp' +
               '<input name="value" type="text" id="'+id_val+'"/>'

    }
    return '<input name = "key" type="text" id="'+id_key+'"/>&nbsp &nbsp &nbsp &nbsp' +
           '<input name="value" type="text" id="'+id_val+'"/>'
}
//save event
$(document).on("click", "#btn-owner-save", function () {

    var ownerName = $('#owner-name').val();
    var ownerMail = $('#owner-email').val();
    var description = $('#owner-desc').val();
    var siteLink = $('#owner-site').val();
    var keys = "";
    var values = "";

    if(fieldCount > 0){
        var i = fieldCount;
        while(i > 0){
            var key_id = "#key-".concat(i-1);
            var val_id = "#value-".concat(i-1);
            var key = $(key_id).val();
            var value = $(val_id).val();
            keys = keys.concat('/',key);
            values = values.concat('/',value);
            i--;
        }

        console.log(keys);
    }

    if(!isEmail(ownerMail)){
        alert("Enter a Valid E-mail address");
        return;
    }
    if(ownerName == "" || ownerName == null){
        alert("Enter a Valid Owner Name");
        return;
    }
    if(description == "" || description == null){
        alert("Enter a Owner Description");
        return;
    }
    if(siteLink == "" || siteLink == null){
        alert("Enter a Owner valid owner site");
        return;
    }

    $.ajax({
               url: context + '/apis/businessowners/save',
               type: 'POST',
               contentType: 'application/x-www-form-urlencoded',
               async: false,
               data: {
                   "ownerName": ownerName,
                   "ownerMail": ownerMail,
                   "description": description,
                   "sitelink": siteLink,
                   "keys": keys,
                   "values" : values
               },
               success: function (data) {
                   Showalert("Business Owner Saved Successfully","alert-success", "statusError");
                   sleep(10000);
               },
               error: function () {
               }
           });

location.reload();
   });

function sleep(milliseconds) {
    var start = new Date().getTime();
    for (var i = 0; i < 1e7; i++) {
        if ((new Date().getTime() - start) > milliseconds){
            break;
        }
    }
}

//validate the condition
function wait(){}
$(document).on("click", ".policy-delete-button", function () {

    var policyName = $(this).data("policyName");
    var policyId = $(this).data("policyId");
    var policyPartial;
    var arrayIndex;
    var conf;
    $.each(xacmalPolicyPartialArray, function (index, obj) {
        if (obj != null && obj.id == policyId) {
            policyPartial = obj;
            arrayIndex = index;
            return false; // break
        }

    });

    if (policyPartial.isShared) {
        $.ajax({
            async: false,
            url: context + '/apis/xacmlpolicies/associated/apps',
            data: {"policyId": policyId},
            type: 'GET',
            contentType: 'application/json',
            dataType: 'json',
            success: function (response) {
                var apps = "";
                if (response.length != 0) {
                    // construct and show the  the warning message with app names which use this partial before delete
                    for (var i = 0; i < response.length; i++) {
                        var j = i + 1;
                        apps = apps + j + ". " + response[i].appName + "\n";

                    }
                    var msg = "You cannot delete the policy " + policyName + " because it is been used in following apps\n\n" +
                        apps;
                    Showalert(msg);
                    return;

                } else {
                    conf = confirm("Are you sure you want to delete the policy " + policyName + "?");
                }

            },
            error: function (response) {
                if (response.status==500){
                    Showalert('Sorry, your session has expired', "alert-error", "statusError");
                    location.reload();
                }
            }
        });

    }

    if (conf == true) {

        $.ajax({

            url: context + '/apis/xacmlpolicies/delete/' + policyId,
            type: 'DELETE',
            contentType: 'application/json',
            dataType: 'json',
            success: function (response) {

                var success = JSON.parse(response);
                if (success) {
                    delete xacmalPolicyPartialArray[arrayIndex];
                    updatePolicyPartial();


                } else {
                    Showalert("Couldn't delete the partial.This partial is being used by web apps  ", "alert-error", "statusError");
                }

            },
            error: function (response) {
                Showalert('Error occured while fetching entitlement policy content', "alert-error", "statusError");
            }
        });

    }

});
