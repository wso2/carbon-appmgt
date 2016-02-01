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
 * Indicates different statues of the app file uploading process.
 * @enum {number}
 */
var UploadStatus = {
    EMPTY: 0,
    SELECTED: 1,
    UPLOADING: 2,
    FINISHED_SUCCESS: 3,
    FINISHED_FAIL: 4
};
/**
 * Current status of the app file upload process.
 * @type {UploadStatus}
 */
var fileUploadStatus = UploadStatus.EMPTY;
/**
 * Current app file upload data.
 * @type {Object}
 */
var fileUploadData = null;
/**
 * CSS class for validation failed input elements.
 * @const
 * @type {string}
 */
var CSS_CLASS_VALIDATION_ERROR = 'validation-error';
/**
 * Data attribute to store validation error message.
 * @const
 * @type {string}
 */
var DATA_ATTRIBUTE_VALIDATION_MESSAGE = 'validation-message';

/**
 * Shows the modal with given parameters.
 * @deprecated replace this with noty jQuery Notification Plugin
 * @param options {{title: (string|null), body: string, button: (string|null), type: string,
 *     callbacks: (Object.<string, Function>|null)}} parameters for the modal
 */
function showModal(options) {
    if (!options) {
        return;
    }

    var modal = $('#message-modal');
    modal.find('.modal-title').html(options.title || 'Message');
    modal.find('.modal-body').html(options.body);
    $('#message-modal-button').html(options.button || 'Close');
    var modalHeader = modal.find('.modal-header');
    var currentCssClass = modalHeader.attr('data-type');
    modalHeader.removeClass('alert-' + currentCssClass);
    modalHeader.addClass('alert-' + options.type);
    modalHeader.attr('data-type', options.type);

    if (options.callbacks) {
        var callbacks = options.callbacks;
        if (callbacks.show) {
            modal.one('shown.bs.modal', callbacks.show);
        }
        if (callbacks.hide) {
            modal.one('hidden.bs.modal', callbacks.hide);
        }
    }
    modal.modal('show');
}

/**
 * Updates progress of the app file upload process in the web page.
 * @param progress {number} progress of the app file upload process
 */
function updateFileUploadProgress(progress) {
    var progressWrapper = $('#file-upload-progress-wrapper');
    var progressbar = $('#file-upload-progressbar');
    var progressbarWrapper = $('#file-upload-progressbar-wrapper');
    var successMessage = $('#file-upload-success-msg');

    switch (fileUploadStatus) {
        case UploadStatus.UPLOADING:
            progressWrapper.show();
            progressbar.css('width', progress + '%').attr('aria-valuenow', progress);
            if (progress == 100) {
                progressbarWrapper.addClass('progress-striped').addClass('active');
                progressWrapper.find('h4').html("Processing...");
            }
            successMessage.hide();
            break;
        case UploadStatus.FINISHED_SUCCESS:
            progressWrapper.hide();
            progressWrapper.find('h4').html("Uploading...");
            progressbar.css('width', '0%').attr('aria-valuenow', 0);
            progressbarWrapper.removeClass('progress-striped').removeClass('active');
            successMessage.show();
            break;
        case UploadStatus.FINISHED_FAIL:
            progressWrapper.hide();
            progressbar.css('width', '0%').attr('aria-valuenow', 0);
            progressbarWrapper.removeClass('progress-striped').removeClass('active');
            successMessage.hide();
            break;
        default:
            // EMPTY or SELECTED
            progressWrapper.hide();
            successMessage.hide();
    }
}

/**
 * Navigates to the specified wizard step in the web page.
 * @param step {number} wizard step
 */
function gotoWizardStep(step) {
    if (step == 1) {

        if (fileUploadStatus == UploadStatus.UPLOADING) {
            return;
        }
        // update navigation buttons
        $('#wizard-step-1-nav').addClass("active");
        $('#wizard-step-2-nav').removeClass("active");
        // show step 01, hide step 02
        $("#wizard-step-1").show();
        $('#wizard-step-2').hide();

    } else if (step == 2) {

        var validationResult = validateAppFileUpload();
        if (!validationResult.valid) {
            var modalOptions = {
                title: "Validation Error",
                body: validationResult.message,
                type: 'danger'
            };
            showModal(modalOptions);
            return;
        }

        var inputsOfForm1 = $('#wizard-step-1-form').find('select:visible, input:text:visible');
        var data = {partial: true};
        for (var i = 0; i < inputsOfForm1.length; i++) {
            var inputElement = $(inputsOfForm1[i]);
            data[inputElement.attr('name')] = inputElement.val();
        }
        $.ajax({
            url: caramel.context + '/api/validate/mobileapp/form/name/copyapp',
            type: 'GET',
            data: data,
            success: function (data, text) {
                if (fileUploadStatus == UploadStatus.SELECTED) {
                    fileUploadData.submit();
                    fileUploadData = null;
                }
                // transfer values form form 01 to form 02
                var webappUrlInput = $('#txtWebappUrl');
                if (webappUrlInput.length == 1) {
                    $('#appWebappUrl').val(webappUrlInput.val());
                }
                var packageNameInput = $('#txtPackageName');
                if (packageNameInput.length == 1) {
                    $('#appPackageName').val(packageNameInput.val());
                }
                var appIdentifierInput = $('#txtAppIdentifier');
                if (appIdentifierInput.length == 1) {
                    $('#appAppIdentifier').val(appIdentifierInput.val());
                }
                // update navigation buttons
                $('#wizard-step-1-nav').removeClass("active");
                $('#wizard-step-2-nav').addClass("active");
                // hide step 01, show step 02
                $('#wizard-step-1').hide();
                $("#wizard-step-2").show();
            },
            error: function (response, status, error) {
                var responseData = JSON.parse(response.responseText);
                switch (response.status) {
                    case 422: // validation error
                        var invalidMessages = setInvalidMessages(responseData.validationResults);
                        var modalOptions = {
                            title: responseData.status,
                            body: ("<ul><li>" + invalidMessages.join("</li><li>") + "</li></ul>"),
                            type: 'danger'
                        };
                        showModal(modalOptions);
                        break;
                    case 500: // server operation error
                        var modalOptions = {
                            title: responseData.status,
                            body: responseData.message,
                            type: 'danger'
                        };
                        showModal(modalOptions);
                        break;
                    default:
                        // some other error
                        console.log(response);
                }
            }
        });

    }
}

/**
 * Validates the app file upload.
 * @returns {{valid: boolean, message: string}} validation status with a message
 */
function validateAppFileUpload() {
    var platform = $('#txtPlatform').val();
    var storeType = $('#txtStoreType').val();
    // if this is an enterprise android/ios mobile app
    if ((platform == 'android' || platform == 'ios') && (storeType == 'enterprise')) {
        // then user should upload a file
        if ((fileUploadStatus == UploadStatus.EMPTY) ||
            (fileUploadStatus == UploadStatus.FINISHED_FAIL)) {
            return {
                valid: false,
                message: "Please select a file to upload before proceed into next step."
            };
        }
    }
    return {valid: true, message: ""};
}

/**
 * Collects validation error messages from given input elements.
 * @param inputElements {jQuery[]} array of inputs elements to be checked
 * @returns {string[]} validation error messages
 */
function getInvalidMessages(inputElements) {
    var messages = [];
    for (var i = 0; i < inputElements.length; i++) {
        var inputElement = $(inputElements[i]);
        if (inputElement.hasClass(CSS_CLASS_VALIDATION_ERROR)) {
            // user have not yet updated a validation failed input
            messages.push(inputElement.data(DATA_ATTRIBUTE_VALIDATION_MESSAGE));
        }
    }
    messages = (messages.length == 0) ? null : messages;
    return messages;
}

/**
 * Sets the validation errors for relevant input elements.
 * @param validationResults {Object.<string, string>} validation results map where key is the
 *     name of the input element and value is the error message
 * @returns {string[]} validation error messages
 */
function setInvalidMessages(validationResults) {
    var messages = [];
    for (var elementName in validationResults) {
        var relevantInput = $('input[name="' + elementName + '"]');
        relevantInput.addClass(CSS_CLASS_VALIDATION_ERROR);
        var msg = validationResults[elementName];
        relevantInput.data(DATA_ATTRIBUTE_VALIDATION_MESSAGE, msg);
        messages.push(msg);
    }
    messages = (messages.length == 0) ? null : messages;
    return messages;
}

$(document).ready(function () {

    $('[data-dismiss="fileupload"]').on('click', function (e) {
        var relevantFileUploadDiv = $(e.target).closest('.fileupload');
        var fileInputName = relevantFileUploadDiv.find('input:file').attr("name");
        relevantFileUploadDiv.find('input:hidden').find('[name="' + fileInputName + '"]').val('');
    });

    //<editor-fold desc="-- Navigation --">
    $('#wizard-step-1-link').on('click', function (e) {
        if ($('#wizard-step-1-nav').hasClass('active')) {
            return;
        }
        gotoWizardStep(1);
    });

    $('#wizard-step-2-link').on('click', function (e) {
        if ($('#wizard-step-2-nav').hasClass('active')) {
            return;
        }
        gotoWizardStep(2);
    });

    $('#btn-next').on('click', function (e) {
        gotoWizardStep(2);
    });
    //</editor-fold>

    //<editor-fold desc="-- Form 01 --">
    var wizardStep1Form = $("#wizard-step-1-form");
    wizardStep1Form.submit(function (e) {
        gotoWizardStep(2)
        e.stopPropagation();
        e.preventDefault();
    });

    $('#appFileUpload').fileuploadFile({
        dataType: 'json',
        url: caramel.context +"/api/mobileapp/upload",
        multipart: true,
        autoUpload: false,
        singleFileUploads: true,
        maxNumberOfFiles: 1,
        add: function (e, data) {
            fileUploadData = data;
            fileUploadStatus = UploadStatus.SELECTED;
        },
        progress: function (e, data) {
            fileUploadStatus = UploadStatus.UPLOADING;
            updateFileUploadProgress(parseInt(data.loaded / data.total * 100));
        },
        done: function (e, data) {
            var response = data._response.result;
            if (response.ok == false) {
                fileUploadStatus = UploadStatus.FINISHED_FAIL;
                var modalOptions = {
                    title: response.message,
                    body: response.report.name,
                    type: 'danger',
                    callbacks: {
                        hide: function (e) {
                            $('#wizard-step-1-form').trigger('reset');
                            gotoWizardStep(1);
                        }
                    }
                };
                showModal(modalOptions);
            } else {
                fileUploadStatus = UploadStatus.FINISHED_SUCCESS;
                $('#txtVersion').val(response.version).attr('disabled', 'disabled');
                $('#appVersion').val(response.version);
                $('#appPackageName').val(response.package);
                $('#appFile').val(response.path);
            }
            updateFileUploadProgress(null);
        }

    });
    //</editor-fold>

    //<editor-fold desc="-- Form 02 --">
    var wizardStep2Form = $("#wizard-step-2-form");
    wizardStep2Form.submit(function (e) {
        if (fileUploadStatus == UploadStatus.UPLOADING) {
            var modalOptions = {
                title: "Please Wait",
                body: "Selected app file is still uploading. "
                      + "Please wait until the uploading process is completed.",
                type: 'danger'
            };
            showModal(modalOptions);
            e.stopPropagation();
            e.preventDefault();
        }
    });

    // bind a callback function
    wizardStep2Form.ajaxForm({
        dataType: 'json', // expected server response type
        success: function (response, statusText, xhr, $form) {
            window.location.replace(caramel.context +"/assets/mobileapp/");
        },
        error: function (response, statusText, err) {
            var responseData = JSON.parse(response.responseText);
            switch (response.status) {
                case 422: // validation error
                    var invalidMessages = setInvalidMessages(responseData.validationResults);
                    var modalOptions = {
                        title: responseData.status,
                        body: ("<ul><li>" + invalidMessages.join("</li><li>") + "</li></ul>"),
                        type: 'danger'
                    };
                    showModal(modalOptions);
                    break;
                case 500: // server operation error
                    var modalOptions = {
                        title: responseData.status,
                        body: responseData.message,
                        type: 'danger'
                    };
                    showModal(modalOptions);
                    break;
                default:
                    // some other error
                    console.log(response);
            }
        }
    });
    //</editor-fold>

});
