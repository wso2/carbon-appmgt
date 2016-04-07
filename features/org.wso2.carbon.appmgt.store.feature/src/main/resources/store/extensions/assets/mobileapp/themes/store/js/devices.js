$('#media').carousel({
    pause: true,
    interval: false,
});

$.ajax({
    url:  caramel.context +"/apis/enterprise/get-devices",
    dataType: "json"
}).done(function(data) {

    var objects = [];

    var length = 6;
    for(var i = 0; i <= parseInt(data.length / length); i++){
        objects.push([]);
    }

    $.each(data, function(key,value) {
        var index = parseInt(key/ length);
        objects[index].push(value);
    });

    for(var j = 0; j < objects.length; j++){
        var isActive = j === 0 ? "active" : "";
        var item = $("<div>", {class: "item " + isActive });
        var row = $("<div>", {class: "row"});

        for (var k = 0; k < objects[j].length; k++) {
            row.append('<div data-dismiss="modal" data-device-id="' + objects[j][k].id +
                '" data-device-platform="' + objects[j][k].platform + '" class="col-md-2 device-image-block-modal">' +
                '<a class="thumbnail" href="#"><img alt="" src="' + objects[j][k].image +
                '"><div>' + objects[j][k].name + '</div></a>' +
                '</div>');
        }
        item.append(row);
        $("#devicesList").append(item);

    }

});


$(".device-image").each(function(index) {
    var device = getURLParameter("device");
    if(device != "null"){
        var deviceId = $(this).data("deviceId");
        if(deviceId != device){
            $(this).fadeTo("slow", 0.1);
        }else{
            $(this).parent().css("cursor", "default");
            $(this).fadeTo("slow", 1);
        }
    }else{
        $(this).css("opacity", 1);
    }

    var srcImage = $(this).attr("src");
    if (!urlExists(srcImage)) {
        $(this).attr("src",  caramel.context +"/extensions/assets/mobileapp/resources/models/none.png");
    }
});

$(".device-image-modal").each(function(index) {
    var srcImage = $(this).attr("src");

    if (!urlExists(srcImage)) {
        $(this).attr("src",  caramel.context +"/extensions/assets/mobileapp/resources/models/none.png");
    }
});






function urlExists(url){

    var http = new XMLHttpRequest();
    try{
        http.open('HEAD', url, false);
    }catch(e){
        http.open('HEAD', url, true);
    }

    try{
        http.send();
    }catch(e){

    }
    return http.status!=404;
}


$(".device-image-block").click(function(index) {


    var device = getURLParameter("device");
    var deviceId = $(this).data("deviceId");
    var platform = $(this).data("platform");
    if(device != deviceId){
        var uri = window.location.pathname + window.location.search;
        uri = updateQueryStringParameter(uri, 'device', deviceId);
        uri = updateQueryStringParameter(uri, 'platform', platform);
        location.href = uri;
    }


});

$("#devicesList").on( "click", ".device-image-block-modal", function() {
    var deviceId = $(this).data("deviceId");
    var devicePlatform = $(this).data("devicePlatform"); // This will type in device identifier in mdm
    performInstalltion(deviceId, devicePlatform, appToInstall);
});


function performInstalltion(deviceId, devicePlatform, app){
    jQuery.ajax({
        url:  caramel.context +"/apps/devices/" + encodeURIComponent(deviceId) + "/" +
              encodeURIComponent(devicePlatform) + "/install",
        type: "POST",
        dataType: "json",
        data : {"asset": app}
    });

    $( document ).ajaxComplete(function() {
       // asset.process("mobileapp",app, location.href);
        noty({
            text : 'You have been subscribed to the application successfully',
            'layout' : 'center',
            'timeout': 1500,
            'modal': false,
             'onClose': function() {
                 location.reload();
            }
        });


    });

}


function performInstalltionUser(app){
    noty({
        text : 'Are you sure you want to install this app?',
        'layout' : 'center',
        'modal' : true,
        buttons : [{
            addClass : 'btn',
            text : 'Yes',
            onClick : function($noty) {

                $noty.close();

                jQuery.ajax({
                    url:  caramel.context +"/apps/user/install",
                    type: "POST",
                    dataType: "json",
                    data : {"asset": app},

                    success : function(data){

                    }
                });

                $( document ).ajaxComplete(function(event, xhr, settings) {
                    // asset.process("mobileapp",app, location.href);
                    noty({
                        text : 'You have been subscribed to the application successfully',
                        'layout' : 'center',
                        'timeout': 1500,
                        'modal': false,
                        'onClose': function() {
                            try{
                                if(JSON.parse(xhr.responseText).redirect != true){
                                    location.reload();
                                }else{
                                    location.replace(JSON.parse(xhr.responseText).location);
                                }
                            }catch(e){
                                location.reload();
                            }

                        }
                    });
                });

            }
        },
            {
                addClass : 'btn',
                text : 'No',
                onClick : function($noty) {
                    $noty.close();
                }
            }]
    });
}


$( document ).ready(function() {
    var id = getURLParameter("id");

    devicePlatform = getURLParameter("platform");

    //var hasdevices = false;
    if(id != "null"){

        $('#devicesList').modal('show');
    }
});


function updateQueryStringParameter(uri, key, value) {
    var re = new RegExp("([?|&])" + key + "=.*?(&|$)", "i");
    separator = uri.indexOf('?') !== -1 ? "&" : "?";
    if (uri.match(re)) {
        return uri.replace(re, '$1' + key + "=" + value + '$2');
    }
    else {
        return uri + separator + key + "=" + value;
    }
}




function getURLParameter(name) {
    return decodeURI(
        (RegExp(name + '=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]
    );
}
