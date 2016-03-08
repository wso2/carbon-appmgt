//TODO: add delay before opening more details
/*
 var timer;
 var details;
 ;
 */
var opened = false;

$(function() {

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


	var details;

	$(document).on('click', '.assets-container .asset-add-btn', function(event) {
		var parent = $(this).parent().parent().parent();
		asset.process(parent.data('type'), parent.data('id'), location.href);
		event.stopPropagation();
	});

	$(document).on('click', '.asset > .asset-details', function(event) {
		var link = $(this).find('.asset-name > a').attr('href');
		location.href = link;
	});
	
	$(".asset-icon").on('click', function(e) {

           if($(this).data("type") === "mobileapp"){
               return;
           }

		  var loggedUser = $("#loggedinuser").val();
		  if(loggedUser == "" || loggedUser == null){
              var allowAnonymous = $(this).find("input").val();
              if (allowAnonymous.toUpperCase() != "TRUE") {
				  var ssoEnabled = $('#sso').val();
				  if (ssoEnabled == 'true') {
                      caramel.tenantedUrl('/login');
				  } else {
					  var assetId = $('#slideAsset').data('id');
					  $('#modal-login').data('value', assetId);
					  $("#modal-login").modal('show');
				  }
				  e.preventDefault();
				  e.stopPropagation();
			  }
		  }
	    	
	});
	
	$(".recent-asset-icon").on('click', function(e) {
		  var loggedUser = $("#loggedinuser").val();
		  if(loggedUser == "" || loggedUser == null){
			  var allowAnonymous = $(this).find("input").val();
              if (allowAnonymous.toUpperCase() != "TRUE") {
				  var ssoEnabled = $('#sso').val();
				  if (ssoEnabled == 'true') {
                      caramel.tenantedUrl('/login');
				  } else {
					  var assetId = $('#slideAsset').data('id');
					  $('#modal-login').data('value', assetId);
					  $("#modal-login").modal('show');
				  }
				  e.preventDefault();
				  e.stopPropagation();
			  }
		  }
	    	
	});

	$(".recent-asset-details").on('click', function(e) {
		  var loggedUser = $("#loggedinuser").val();
		  if(loggedUser == "" || loggedUser == null){
			  var allowAnonymous = $(this).find("input").val();
              if (allowAnonymous.toUpperCase() != "TRUE") {
				  var ssoEnabled = $('#sso').val();
				  if (ssoEnabled == 'true') {
                      caramel.tenantedUrl('/login');
				  } else {
					  var assetId = $('#slideAsset').data('id');
					  $('#modal-login').data('value', assetId);
					  $("#modal-login").modal('show');
				  }
				  e.preventDefault();
				  e.stopPropagation();
			  }
		  }
	    	
	});
	
	
	$(".asset-details").on('click', function(e){


            if($(this).data("type") === "mobileapp"){
                return;
            }

		  var loggedUser = $("#loggedinuser").val();
		  if(loggedUser == "" || loggedUser == null){
			  var allowAnonymous = $(this).find("input").val();
              if (allowAnonymous.toUpperCase() != "TRUE") {
				  var ssoEnabled = $('#sso').val();
				  if (ssoEnabled == 'true') {
					  location.href = "/store/login";
				  } else {
					  var assetId = $('#slideAsset').data('id');
					  $('#modal-login').data('value', assetId);
					  $("#modal-login").modal('show');
				  }
				  e.preventDefault();
				  e.stopPropagation();
			  }
		  }
	    	
	});
	
	$("#btn-add-gadget").on('click', function(e) {
		  var loggedUser = $("#loggedinuser").val();
		  if(loggedUser == "" || loggedUser == null){
			  var allowAnonymous = $(this).find("input").val();
              if (allowAnonymous.toUpperCase() != "TRUE") {
				  var ssoEnabled = $('#sso').val();
				  if (ssoEnabled == 'true') {
                      caramel.tenantedUrl('/login');
				  } else {
					  var assetId = $('#slideAsset').data('id');
					  $('#modal-login').data('value', assetId);
					  $("#modal-login").modal('show');
				  }
				  e.preventDefault();
				  e.stopPropagation();
			  }
		  }
	    	
	});

	mouseStop();
	applyTopAssetsSlideshow();

	$("#top-asset-slideshow-gadget").carouFredSel({
		items : 4,
		width : "100%",
		infinite : false,
		auto : false,
		circular : false,
		pagination : "#top-asset-slideshow-pag-gadget"

	});

	$("#top-asset-slideshow-site").carouFredSel({
		items : 4,
		width : "100%",
		infinite : false,
		auto : false,
		circular : false,
		pagination : "#top-asset-slideshow-pag-site"

	});

        $("#top-asset-slideshow-ebook").carouFredSel({
		items : 4,
		width : "100%",
		infinite : false,
		auto : false,
		circular : false,
		pagination : "#top-asset-slideshow-pag-ebook"
       });

});

var mouseStop = function() {
	$('.asset').bind('mousestop', 300, function() {
		//console.log("In");
		bookmark = $(this).find('.store-bookmark-icon');
		bookmark.animate({
			top : -200
		}, 200);
		details = $(this).find('.asset-details');
		details.animate({
			top : 0
		}, 200);
		opened = true;
	}).mouseleave(function() {
		//console.log("out");
		bookmark = $(this).find('.store-bookmark-icon');
		bookmark.animate({
			top : -4
		}, 200);
		opened = opened && details.stop(true, true).animate({
			top : 200
		}, 200) ? false : opened;
	});

}

var applyTopAssetsSlideshow = function(){
	var visible,
		size =  $('#asset-slideshow-cont').find('.slide').size();
		
	if(size<=3){
		visible = 1;
		$('#asset-slideshow-cont .html_carousel').css('margin-left', 163);
	} else {
		visible = 3;
	}
	
	$("#asset-slideshow").carouFredSel({
		items : {
			visible : visible
		},
		height : 300,
		scroll : 1,
		auto : true,
		prev : {
			button : "#asset-slideshow-next",
			key : "left"
		},
		next : {
			button : "#asset-slideshow-prev",
			key : "right"
		}

	}).find(".slide").hover(function() {
		$(this).find(".asset-intro-box").slideDown("fast");
	}, function() {
		$(this).find(".asset-intro-box").slideUp("fast");
	});
	
	
}
