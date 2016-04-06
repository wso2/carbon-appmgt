/*
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

$(".btn-action").click(function (e) {
	$(this).hide(); //to avoid user from click again before the operation proceeds
	var app = $(this).data("app");
    var provider = $(this).data("provider");
    var name = $(this).data("name");
    var version = $(this).data("version");
	var action = $(this).data("action");
    var isDefault = Boolean($(this).data("isdefault"));

    var status = isPublishedToExternalStore(action, provider, name, version);

    if(status) {
        var msg = "This app is published to one or more external stores .\n" +
            "Please remove this app from external stores before " +action;
        var head = action +"Asset";
        showMessageModel(msg, head, "webapp");
        $(parent).children().attr('disabled', false);
        return false;
    }

    if (action == "Reject") {
        showCommentModel("Reason for Rejection", action, app, "webapp");
    } else {
        jQuery.ajax({
                        url: caramel.context + '/api/lifecycle/' + action + '/webapp/' + app,
                        type: 'PUT',
                        success: function (data, text) {
                            var msg = data.messages[0];
                            for (var i = 1; i < data.messages.length; i++) {
                                msg = msg + "</br>" + data.messages[i];
                            }
                            //showMessageModel(msg, data.status, 'webapp');
                            location.reload();
                        },
                        error: function (request, status, error) {
                            var data = jQuery.parseJSON(request.responseText);
                            var msg = data.messages[0];
                            for (var i = 1; i < data.messages.length; i++) {
                                msg = msg + "</br>" + data.messages[i];
                            }
                            showMessageModel(msg, data.status, 'webapp');
                        }
                    });
    }

	// Stop even propagation since it would trigger the click event listeners for the table rows.
	e.stopPropagation();
});


$(".btn-reject-proceed").click(function () {
	var comment = $("#commentText").val();
	if (comment.trim() == "") {
		alert("Please provide a comment.");
		return false;
	}

	var app = $("#webappName").val();
	var action = $("#action").val();
	jQuery.ajax({
		url: caramel.context + '/api/lifecycle/' + action + '/webapp/' + app,
		type: "PUT",
		data: JSON.stringify({comment: comment}),
		success: function (msg) {
			location.reload();
		}
	});

});


$(".btn-deploySample").click(function (e) {
	jQuery.ajax({
		url: caramel.context + '/api/asset/webapp/deploySample',
		type: "PUT",
		dataType: "json",
		async: false,
		success: function (msg) {
			if (msg.isError == "true") {
				$(document).ajaxComplete(function () {
					location.reload();
				});
			}
		}
	});

});

$(".tab-button").click(function () {

	var status = $(this).data("status");

	$(".app-row").each(function (index) {
		$(this).css("display", "none");
		var appRowStatus = $(this).data("status");
		if (status == "All") {
			$(this).css("display", "table-row");
		} else if (status == appRowStatus) {
			$(this).css("display", "table-row");
		}
	});


});

$(".btn-view-app").click(function (e) {


	// alert($(this).data("id"));
	//alert($(this).data("category") );
	if ($(this).data("category") === "webapp") {
		var url = $(this).data("url");
	} else {
		if ($(this).data("url") == 'undefined') {
			if ($(this).data("category") === "android") {
				var url = "https://play.google.com/store/apps/details?id=" + $(this).data("package");
			} else if ($(this).data("category") === "ios") {
				{
					var url = "https://itunes.apple.com/en/app/id" + $(this).data("appid");
				}

			}

		} else {
			var url = window.location.protocol + "//" + window.location.host + $(this).data("url");
		}
	}
	$("#appModalAppURL").attr("href", url);
	$("#appModalAppURL").html(url);
	updateQRCode(url);

	$("#appModal").modal('show');
	e.stopPropagation();


});

var showMessageModel = function (msg, head, type) {
	$('#messageModal2 #commentText').html('');
	$('#messageModal2').html($('#confirmation-data1').html());
	$('#messageModal2 h3.modal-title').html((head));
	$('#messageModal2 #myModalLabel').html((head));
	$('#messageModal2 div.modal-body').html('\n\n' + (msg) + '</b>');
	$('#messageModal2 a.btn-other').html('OK');
	$('#messageModal2').modal();
	$("#messageModal2").on('hidden.bs.modal', function () {
		window.location = caramel.context + '/assets/' + type + '/';
	});

};

var showCommentModel = function (head, action, app, type) {
	$('#messageModal3').html($('#confirmation-data1').html());
	$('#messageModal3 h4.modal-title').html((head));
	$('#messageModal3 #webappName').val(app);
	$('#messageModal3 #action').val(action);
	$('#messageModal3').modal();
	$("#messageModal3").on('hidden.bs.modal', function () {
		window.location = caramel.context + '/assets/' + type + '/';
	});
};

function updateQRCode(text) {

	var element = document.getElementById("qrcode");
	var bodyElement = document.body;
	if (element.lastChild)
		element.replaceChild(showQRCode(text), element.lastChild);
	else
		element.appendChild(showQRCode(text));

}

function isPublishedToExternalStore(action, provider, name, version) {
    var publishedInExternalStores = false;
    if (action == "Unpublish" || action == "Deprecate") {

        $.ajax({
            async: false,
            url: caramel.context + '/api/asset/get/external/stores/webapp/' + provider + '/' + name + '/' + version,
            type: 'GET',
            processData: true,
            success: function (response) {
                if (!response.error) {
                    var appStores = response.appStores;

                    if (appStores != null && appStores != undefined) {
                        for (var i = 0; i < appStores.length; i++) {
                            if (appStores[i].published) {
                                publishedInExternalStores = true;
                                break;
                            }
                        }
                    }
                }
                return publishedInExternalStores;

            },
            error: function (response) {

            }
        });

    }
    return publishedInExternalStores;
}