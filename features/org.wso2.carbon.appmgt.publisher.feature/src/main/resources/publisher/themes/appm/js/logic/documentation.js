$(document).ready(function () {

    var docUrlDiv = $('#docUrl');
    docUrlDiv.click(function () {
        docUrlDiv.removeClass('error');
        docUrlDiv.next().hide();
    });

    docUrlDiv.change(function () {
        validInputUrl(docUrlDiv);
    });

    $('input[name=optionsRadios1]:radio:checked').change(function () {
        if (getRadioValue($('input[name=optionsRadios1]:radio:checked')) == "inline") {
            $('#docUrl').removeClass('error');
            $('#docUrl').next().hide();
        }
    });

    var docId = $("#docName");
    docId.change(function () {
        var apiName = $("#docAPIName").val();
        var errorCondition = false;
        if (checkIllegalCharacters(docId.val())) {
            errorCondition = true;
            var message = "contains one or more illegal characters (~!@#;%^*()+={}|\\<>\"',)";
            if (!validInput(docId, message, errorCondition)) {
                return;
            }
        }
        //Check the doc name is duplicated
        errorCondition = isAvailableDoc(apiName + "-" + docId.val());
        validInput(docId, 'Duplicate Document Name.', errorCondition);

    });

    $('#saveDoc').click(function () {
        var sourceType = getRadioValue($('input[name=optionsRadios1]:radio:checked'));
        var docUrlDiv = $("#docUrl");
        var fileDiv = $("#docLocation");
        var apiName = $("#docAPIName").val();
        var errCondition = docUrlDiv.val() == "";
        var isFilePathEmpty = fileDiv.val() == "";
        var isOtherTypeNameEmpty = $('#specifyBox').val() == null || $('#specifyBox').val() == '';
        var docType = getRadioValue($('input[name=optionsRadios]:radio:checked'));

        var errorCondition = false;

        if (docId.val() == '') {
            errorCondition = true;
            if (!validInput(docId, 'This field is required.', errorCondition)) {
                return;
            }
        } else if (checkIllegalCharacters(docId.val())) {
            errorCondition = true;
            var message = "contains one or more illegal characters (~!@#;%^*()+={}|\\<>\"',)";
            if (!validInput(docId, message, errorCondition)) {
                return;
            }
        }
        if ($(this).val() != "Update") {
            errorCondition = isAvailableDoc(apiName + "-" + docId.val());
        }
        if (apiName && !validInput(docId, 'Duplicate Document Name.', errorCondition)) {
            return;
        } else if (sourceType == 'url' && !validInput(docUrlDiv, 'This field is required.', errCondition)) {
            return;
        } else if (sourceType == 'url' && !validInputUrl(docUrlDiv)) {
            return;
        } else if ($(this).val() != "Update" && sourceType == 'file' && !validInput(fileDiv, 'This field is required.',
                                                                                    isFilePathEmpty)) {
            return;
        } else if (docType.toLowerCase() == 'other' && !validInput($('#specifyBox'), 'This field is required.',
                                                                   isOtherTypeNameEmpty)) {
            return;
        }

        if (!isFilePathEmpty) {
            //check whether file name contains illegal characters
            var fileName = fileDiv.val().split('\\').pop();
            errorCondition = checkIllegalCharacters(fileName);
            var message = "contains one or more illegal characters (~!@#;%^*()+={}|\\<>\"',)";
            if (!validInput(fileDiv, message, errorCondition)) {
                return;
            }
        }
        if ($(this).val() == "Update" && $("#docLocation").val() == "") {
            $("#docLocation").removeClass('required');
        }

        //$("#addNewDoc").validate();
        // if ($("#addNewDoc").valid()) {
        var version = $("#docAPIVersion").val();
        var provider = $("#docAPIProvider").val();
        var docName = $("#docName").val();
        var summary = $("#summary").val();

        var docUrl = docUrlDiv.val();
        if (docUrl.indexOf("http") == -1) {
            docUrl = "http://" + docUrl;
        }

        var mode = $('#newDoc .btn-primary').val();
        $('<input>').attr('type', 'hidden')
            .attr('name', 'provider').attr('value', provider).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'action').attr('value', "addDocumentation").prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'apiName').attr('value', apiName).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'version').attr('value', version).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'docName').attr('value', docName).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'docType').attr('value', docType).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'summary').attr('value', summary).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'sourceType').attr('value', sourceType).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'docUrl').attr('value', docUrl).prependTo('#addNewDoc');
        $('<input>').attr('type', 'hidden')
            .attr('name', 'mode').attr('value', mode).prependTo('#addNewDoc');
        if (docType.toLowerCase() == 'other') {
            $('<input>').attr('type', 'hidden')
                .attr('name', 'newType').attr('value', $('#specifyBox').val()).prependTo('#addNewDoc');
        }

        $('#addNewDoc').ajaxSubmit(function (result) {
            if (!result.error) {
                clearDocs();
            } else {
                if (result.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: result.message, type: "error"});
                }
            }
        });
    });

    //handle documentation tab open after delete
    var currentPath = window.location.search.substring(1);
    var currentPathArray = currentPath.split("=");
    if ((currentPathArray[0] == 'docs') && (currentPathArray[1] == 'true')) {
        $('.nav-tabs a[href=#documentation]').tab('show');
    }

    // Document Type selecting radio buttons
    var documentTypeRadioBtns = $('#documentTypeRadioWrapper').find('input:radio');
    documentTypeRadioBtns.on('click', function (event) {
        documentTypeRadioBtns.each(function (index, element) {
            element = $(element);
            var isChecked = element.is(':checked');
            var selectElementId = element.data('onclick-select');
            if (isChecked && selectElementId) {
                $(selectElementId).attr('checked', true);;
                $(selectElementId).click();
            }
            var viewElementId = element.data('onclick-view');
            if (viewElementId) {
                var viewElement = $(viewElementId);
                if (isChecked) {
                    viewElement.show('slow');
                } else {
                    viewElement.hide('slow');
                }
            }
            var enableElementId = element.data('onclick-enable');
            if(isChecked && enableElementId){
                $(enableElementId).prop('disabled', false);
            }
            var disableElementId = element.data('onclick-disable');
            if(isChecked && disableElementId){
                $(disableElementId).prop('disabled', true);
            }
        });
    });

    // Document Source selecting radio buttons
    var documentSourceRadioBtns = $('#documentSourceRadioWrapper').find('input:radio');
    documentSourceRadioBtns.on('click', function (event) {
        documentSourceRadioBtns.each(function (index, element) {
            element = $(element);
            var isChecked = element.is(':checked');
            var viewElementId = element.data('onclick-view');
            if (viewElementId) {
                var viewElement = $(viewElementId);
                if (isChecked) {
                    viewElement.show('slow');
                } else {
                    viewElement.hide('slow');
                }
            }
        });
    });
});

var isAvailableDoc = function (id) {
    id = id.replace(/ /g, '__');
    var docEntry = $("#docTable #" + id).text();
    if (docEntry != "") {
        return true;
    }
};

var getRadioValue = function (radioButton) {
    if (radioButton.length > 0) {
        return radioButton.val();
    } else {
        return 0;
    }
};

var clearDocs = function () {
    if (window.location.href.indexOf("docs=true") > -1) {
        window.location.href = window.location.href;
    } else {
        window.location.href = window.location.href + "?docs=true";
    }
};

var removeDocumentation = function (provider, apiName, version, docName, docType) {
    $('#messageModal').html($('#confirmation-data').html());
    $('#messageModal h3.modal-title').html(('Confirm Delete'));
    $('#messageModal div.modal-body').html(
        '\n\n' + ('Are you sure you want to delete the file') + '<b>"' + docName + '</b>"?');
    $('#messageModal a.btn-primary').html('Yes');
    $('#messageModal a.btn-other').html('No');
    $('#messageModal a.btn-primary').click(function () {
        $.ajax({
                   url: caramel.context + '/api/doc?action=removeDocumentation',
                   type: 'POST',
                   data: {provider: provider, apiName: apiName, version: version, docName: docName, docType: docType},
                   success: function (response) {
                       if (response.error == false) {
                           $('#messageModal').modal('hide');
                           $('#' + apiName + '-' + docName.replace(/ /g, '__')).remove();
                           if ($('#docTable tr').length == 1) {
                               $('#docTable').append($('<tr><td colspan="6">' + ('resultMsgs.noDocs') + '</td></tr>'));
                           }
                           if (window.location.href.indexOf("docs=true") > -1) {
                               window.location.href = window.location.href;
                           } else {
                               window.location.href = window.location.href + "?docs=true";
                           }
                       }
                       else {
                           console.log("error occurred while deleting");
                       }
                   },
                   error: function (response) {
                       console.log("error occurred while deleting");
                   }

               });
    });
    $('#messageModal a.btn-other').click(function () {
        return;
    });
    $('#messageModal').modal();
};

var validUrl = function (url) {
    var invalid = true;
    var regex = /^(https?|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i;
    if (regex.test(url)) {
        invalid = false;
    }
    return invalid;
};

var validInputUrl = function (docUrlDiv) {
    if (docUrlDiv) {
        var docUrlD;
        if (docUrlDiv.val().indexOf("http") == -1) {
            docUrlD = "http://" + docUrlDiv.val();
        } else {
            docUrlD = docUrlDiv.val();
        }
        var erCondition = validUrl(docUrlD);
        return validInput(docUrlDiv, 'Invalid URL', erCondition);
    }
};

var validInput = function (divId, message, condition) {
    if (condition) {
        divId.addClass('error');
        if (!divId.next().hasClass('error')) {
            divId.parent().append('<label class="error">' + message + '</label>');
        } else {
            divId.next().show();
            divId.next().text(message);
        }
        return false;
    } else {
        divId.removeClass('error');
        divId.next().hide();
        return true;
    }
};

var checkIllegalCharacters = function (value) {
    // registry doesn't allow following illegal charecters
    var match = value.match(/[~!@#;%^*()+={}|\\<>"',]/);
    return !!match; // '!!' converts the value to boolean
};
