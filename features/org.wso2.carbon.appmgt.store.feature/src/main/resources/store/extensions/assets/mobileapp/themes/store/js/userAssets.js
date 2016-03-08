appToUninstall = null;
appToInstall = null;


$(function(){


	var visibleToDevices = function(){
		var ua = navigator.userAgent;
		var checker = {
			iphone: ua.match(/(iPhone|iPod|iPad)/),
			blackberry: ua.match(/BlackBerry/),
			android: ua.match(/Android/)
		};

		if (checker.android){
			$('.type-ios').hide();
		}

		if (checker.iphone){
			$('.type-android').hide();
		}

	};
	visibleToDevices();
	


$('button[data-toggle=tooltip]').tooltip();

$(document).on('click', '#myasset-container .asset-remove-btn', function() {
	
	var aid = $(this).attr('data-aid');
	var type = $(this).attr('type');
	
	
noty({
		text : 'Are you sure you want remove this app?',
		buttons : [{
			addClass : 'btn btn-cancel',
			text : 'Cancel',
			'layout' : 'center',
			onClick : function($noty) {
				$noty.close();

			}
			
			
		}, {
			
			addClass : 'btn btn-orange',
			text : 'Ok',
			onClick : function($noty) {
				
				caramel.get('/apis/remove', {
			    aid: aid,
			    type: type
		            }, function (data) {
				location.reload();
		            });
				
				
			}
			
		}]
	});		
	
	
	
	
	
	

//console.log("removing : "+$(this).attr('data-aid')+" type :"+$(this).attr('type'));
});





$(".device-image-modal").each(function(index) {	
	var srcImage = $(this).attr("src");	
	if (!urlExists(srcImage)) {
        $(this).attr("src", caramel.context + "/extensions/assets/mobileapp/resources/models/none.png");
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




$(document).on('click', '.app-assets .asset-unsubscribe-btn', function() {
	appToUninstall = $(this).data("aid");
    appName = $(this).data("name");
    noty({
        text : 'Are you sure you want to uninstall ' + appName + ' from all of your devices?',
        'layout' : 'center',
        'modal' : true,
        buttons : [{
            addClass : 'btn',
            text : 'Yes',
            onClick : function($noty) {

                $noty.close();

                $.ajax({
                    type: "POST",
                    url: caramel.context + "/apps/devices/uninstall",
                    data: { asset: appToUninstall }
                })
                    .done(function( msg ) {
                        noty({
                            text : appName + ' is uninstalled and unsubscribed',
                            'layout' : 'center',
                            'modal' : true,
                            timeout: 1000,
                            'onClose': function() {
                                location.reload();
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
});

$(document).on('click', '.app-assets .asset-reinstall-btn', function() {

	appToInstall = $(this).data("aid");

	   devicePlatform = $(this).data("platform").toLowerCase();
		
		$('#devicesList').modal('show');
		
});

	$(document).on('click', '.app-assets .asset-reinstall-btn-direct', function() {
		appToInstall = $(this).data("aid");
		performInstalltionUser(appToInstall);
	});





$('#devicesList').on('hidden', function () {
    location.reload(); 
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
        $(this).attr("src", caramel.context + "/extensions/assets/mobileapp/resources/models/none.png");
	}
});

$(".device-image-block-modal").click(function(index) {	
	
	var deviceId = $(this).data("deviceId");
	jQuery.ajax({
        url: caramel.context + "/apps/devices/" + deviceId + "/install",
      type: "POST",
      dataType: "json",	
      data : {"asset": appToInstall}
	});	
		
});








$('.embed-snippet').hide();

$(document).on('click', '#myasset-container .btn-embed', function() {
    $(this).closest('.store-my-item').find('.embed-snippet').toggle(50);
    return false;
});

$('.popover-content').on("click",function(event){
	$('.arrow').css({"display":"none"});
});

$(".popover-content").on("mouseleave",function(){
	$('.arrow').css({"display":"block"});
});

	$("#asset-in-gadget").carouFredSel({
		items:4,
		infinite: false,
		auto : false,
		circular: false,		
		pagination  : "#own-asset-slideshow-pag-gadget"

	});
	
	
	$("#asset-in-site").carouFredSel({
		items:4,
		infinite: false,
		auto : false,
		circular: false,		
		pagination  : "#own-asset-slideshow-pag-site"

	});

    $("#asset-in-ebook").carouFredSel({
		items:4,
		infinite: false,
		auto : false,
		circular: false,
		pagination  : "#own-asset-slideshow-pag-ebook"

	});
	

});