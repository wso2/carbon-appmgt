/**
 * @license
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

/**
 * Binds on click event handlers for life-cycle action buttons in the specified table rows.
 * @param rows {jQuery[]} rows of the table
 */
function bindEventHandlersForLCActionButtons(rows) {
    var clickEventHandler = function (e) {
        e.stopPropagation();
        var thisButton = $(e.target);
        var uuid = thisButton.data("id");
        var lcAction = thisButton.data("action");

        if (lcAction == "Reject") {
            showCommentModal(function (comment) {
                promoteLifeCycleAction(uuid, lcAction, comment, thisButton);
                location.reload(); //  to update notifications
            });
        } else {
            promoteLifeCycleAction(uuid, lcAction, "", thisButton);
        }
    };
    for (var i = 0; i < rows.length; i++) {
        var lifeCycleActionsCell = $(rows[i]).find('[data-column="lifecycleAvailableActions"]');
        lifeCycleActionsCell.find('span').find('button').on('click', clickEventHandler);
    }
}

/**
 * Promotes the given asset to the next life-cycle status by performing the specified life-cycle
 * action on it.
 * @param assetUUID {string} UUID of the asset to be promoted
 * @param lcAction {string} life-cycle action to be performed
 * @param comment {string} user comment for the life-cycle action
 * @param triggeredButton {jQuery} button pressed by the user to perform the action
 */
function promoteLifeCycleAction(assetUUID, lcAction, comment, triggeredButton) {
    var allButtons = triggeredButton.closest('td').find('button');
    $.ajax({
        url: caramel.context +'/api/lifecycle/' + lcAction + '/mobileapp/' + assetUUID,
        type: 'PUT',
        contentType: 'application/json',
        data: (comment) ? JSON.stringify({comment: comment}) : "",
        dataType: 'json', // the type of data that you're expecting back from the server
        beforeSend: function (xhr) {
            triggeredButton.button('loading');
            allButtons.prop('disabled', true);
        },
        success: function (responseData, status, xhr) {
            updateTable(responseData.artifacts);
            displayMessage('success',
                           ("<ul><li>" + responseData.messages.join("</li><li>") + "</li></ul>"));
        },
        error: function (response, status, error) {
            var responseData = JSON.parse(response.responseText);
            displayMessage('danger',
                           ("<ul><li>" + responseData.messages.join("</li><li>") + "</li></ul>"));
        },
        complete: function (response, status) {
            triggeredButton.button('reset');
            allButtons.prop('disabled', false);
        }
    });
}

/**
 * Updates specified row of the 'apps-listing-table' table with the given data using Caramel engine.
 * @param assets {Object[]} an array of asset objects
 */
function updateTable(assets) {
    var tableBody = $('#apps-listing-table').find('tbody');
    // This function was added to avoid 'Mutable variable is accessible from closure' warning.
    var updateRow = function (row, asset) {
        caramel.render('list-assets-row', asset, function (info, content) {
            row.html(content);
            bindEventHandlersForLCActionButtons([row]);
        });
    };
    for (var i = 0; i < assets.length; i++) {
        var asset = assets[i];
        var row = tableBody.find('tr[data-row="' + asset.id + '"]');
        if (row.length) {
            updateRow(row, asset);
        }
    }
}

/**
 * This callback is displayed as a global member.
 * @callback afterProceed
 * @param {string} comment entered by the user
 */
/**
 * Shows the comment modal.
 * @param callback {afterProceed} callback function to be called after user click on the 'Proceed'
 *     button.
 */
function showCommentModal(callback) {
    var appRejectCommentModal = $('#app-reject-comment-modal');
    var appRejectCommentInput = appRejectCommentModal.find('[name="app-reject-comment-input"]');
    var appRejectProceedButton = appRejectCommentModal.find('[name="app-reject-proceed-btn"]');
    appRejectProceedButton.one('click', function () {
        var comment = appRejectCommentInput.val().trim();
        if (comment.length == 0) {
            alert("Please provide a comment.");
            return false;
        }
        appRejectCommentModal.modal('hide');
        callback(comment);
    });
    appRejectCommentInput.val("");
    appRejectCommentModal.one('shown.bs.modal', function () {
        appRejectCommentInput.focus();
    });
    appRejectCommentModal.modal('show');
}

/**
 * Shows an alert message.
 * @param type {string} message type; see Bootstrap 3 alert types
 * @param text {string} text to be shown
 */
function displayMessage(type, text) {
    var messageBlock = $('#message-block');

    var currentType = messageBlock.data('type');
    if (currentType) {
        messageBlock.removeClass(currentType);
    }
    var newType = 'alert-' + type;
    messageBlock.addClass(newType);
    messageBlock.data('type', newType);

    messageBlock.removeClass('in');
    messageBlock.one('click', function (e) {
        messageBlock.removeClass('in');
        messageBlock.hide();
    });
    messageBlock.find('[name="message-text"]').html(text);
    messageBlock.show();
    messageBlock.addClass('in');
}

$(document).ready(function () {
    // register custom Handlebars helpers
    registerHelpers(Handlebars);

    var rows = $('#apps-listing-table').find('tbody').find('tr');
    // bind click event handler for each row
    rows.on('click', function (e) {
        window.location = caramel.context +"/asset/mobileapp/" + $(e.target).closest('tr').data('row');
    });
    // bind click event handler for app download button
    rows.find('[data-column="download"]').on('click', function (e) {
        var thisButton = $(e.target).closest('button');
        var platform = thisButton.data('platform');
        var storeType = thisButton.data('type');

        var url = "";
        switch (platform) {
            case "android":
                if (storeType == "enterprise") {
                    url = window.location.protocol + "//" + window.location.host
                          + thisButton.data("url");
                } else if (storeType == "public") {
                    url = "https://play.google.com/store/apps/details?id="
                          + thisButton.data("package");
                }
                break;
            case "ios":
                if (storeType == "enterprise") {
                    url = window.location.protocol + "//" + window.location.host
                          + thisButton.data("url");
                } else if (storeType == "public") {
                    url = "https://itunes.apple.com/en/app/id" + thisButton.data("appid");
                }
                break;
            case "webapp":
                url = thisButton.data("url");
                break;
            default :
            // some invalid value, do nothing
        }
        if (url) {
            e.stopPropagation();
            var appDownloadModal = $('#app-download-modal');
            appDownloadModal.find('[name="app-download-url"]').attr('href', url).html(url);
            appDownloadModal.find('#app-download-qrcode').html(showQRCode(url));
            appDownloadModal.modal('show');
        }
    });
    // bind click event handlers for life-cycle action buttons
    bindEventHandlersForLCActionButtons(rows);
    // bind click event handler for app delete button
    rows.find('[name="app-delete-btn"]').on('click', function (e) {
        e.stopPropagation();
        var thisButton = $(e.target);
        var uuid = thisButton.data('id');
        var appName = thisButton.data('name');

        var appDeleteConfirmModal = $('#app-delete-confirm-modal');
        var message = "Are you sure you want to delete '" + appName + "' mobile app?"
                      + "<br /> This action cannot be undo.";
        appDeleteConfirmModal.find('.modal-body').find('p').html(message);
        var appDeleteConfirmButton = appDeleteConfirmModal.find('[name="app-delete-confirm-btn"]');
        var appDeleteCancelButton = appDeleteConfirmModal.find('[name="app-delete-cancel-btn"]');
        appDeleteConfirmButton.one('click', function (e) {
            $.ajax({
                url: caramel.context +'/api/mobile/delete/' + uuid,
                type: "DELETE",
                beforeSend: function (xhr) {
                    appDeleteConfirmModal.modal({backdrop: 'static', keyboard: false});
                    appDeleteConfirmButton.button('loading');
                    appDeleteCancelButton.prop('disabled', true);
                },
                success: function (responseData, status, xhr) {
                    location.reload();
                },
                error: function (response, status, error) {
                    appDeleteConfirmModal.modal('hide');
                    appDeleteConfirmModal.modal({backdrop: true, keyboard: true});
                    appDeleteConfirmButton.button('reset');
                    appDeleteCancelButton.prop('disabled', false);
                }
            });
        });
        appDeleteConfirmModal.modal('show');
    });
});
