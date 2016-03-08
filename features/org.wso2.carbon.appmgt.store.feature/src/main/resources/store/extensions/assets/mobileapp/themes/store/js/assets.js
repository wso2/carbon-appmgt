//TODO: add delay before opening more details
/*
 var timer;
 var details;
 ;
 */

var opened = false, currentPage = 1, infiniteScroll = null;

$(function() {



	$('#category-select').on('change', function(){
		var selected = $(this).find("option:selected").val();
		location.href = caramel.tenantedUrl("/assets/mobileapp?query=category:\"" + selected + "\"");
	});

	$('#platform-select').on('change', function(){
		var selected = $(this).find("option:selected").val();
		location.href = caramel.tenantedUrl("/assets/mobileapp?query=platform:\"" + selected + "\"");
	});



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

	$(document).on('click', '#assets-container .asset-add-btn', function(event) {
		
		var device = getURLParameter("device");	
		appToInstall = $(this).data("app");
		devicePlatform = $(this).data("platform").toLowerCase();
		
		var showDevices = false;
		
		$(".device-image-block-modal").each(function(index) {	
			var platform = $(this).data("platform").toLowerCase();
			if(devicePlatform != "webapp"){
				if(devicePlatform != platform){
					$(this).css("display", "none");
				}else{
					showDevices = true;
				}
			}else{
				showDevices = true;
			}
		
		});
		
		if(showDevices == false){
			$('.modal-body').html("<div class='offset2'>Sorry you dont have devices to install this app<div>");
		}
		
		if(!(device > 0)){
			$('#devicesList').modal('show');
		}else{			
			performInstalltion(device, appToInstall);
		}				
	
		event.stopPropagation();
	});

	$(document).on('click', '.asset > .asset-details', function(event) {
		var link = $(this).find('.asset-name > a').attr('href');
		location.href = link;
	});

	//mouseStop();

	History.Adapter.bind(window, 'statechange', function() {
		var state = History.getState();
		if (state.data.id === 'assets') {
			renderAssets(state.data.context);
		}
	});

	var loadAssets = function(url) {
		caramel.data({
			title : null,
			header : ['header'],
			body : ['assets', 'sort-assets']
		}, {
			url : url,
			success : function(data, status, xhr) {
				//TODO: Integrate a new History.js library to fix this
				if ($.browser.msie == true && $.browser.version < 10) {
					renderAssets(data);
				} else {
					History.pushState({
						id : 'assets',
						context : data
					}, document.title, url);
				}

			},
			error : function(xhr, status, error) {
				theme.loaded($('#assets-container').parent(), '<p>Error while retrieving data.</p>');
			}
		});
		theme.loading($('.store-left'));
	};

	var loadAssetsScroll = function(url) {
		//TO-DO 
		/* As tags are not Indexing so far
		*  Assert pagination and is not supporteed and There for infiniteScroll is disable to 'Tag'
		* */
		if(url.indexOf('tag')== -1){
		caramel.data({
			title : null,
			body : ['assets']
		}, {
			url : url,
			success : function(data, status, xhr) {
                infiniteScroll = data.body.assets.context.assets.length >= 12;
                currentPag = 1;
				renderAssetsScroll(data);
				$('.loading-inf-scroll').hide();
			},
			error : function(xhr, status, error) {
                infiniteScroll = false;
			}
		});
		$('.loading-inf-scroll').show();
		}
	};

	$(document).on('click', '#ul-sort-assets li a', function(e) {
		currentPage = 1;
		$('#ul-sort-assets li a').removeClass('selected-type');
		var thiz = $(this);
		thiz.addClass('selected-type');
		loadAssets(thiz.attr('href'));
		mouseStop();
		e.preventDefault();
	});

	$(document).on('click', '.pagination a', function(e) {
		e.preventDefault();
		var url = $(this).attr('href');
		if (url === '#') {
			return;
		}
		loadAssets(url);
	});

	var scroll = function() {

		if(infiniteScroll || (store.asset.paging.size >= 12 && infiniteScroll == null)) {
			if($(window).scrollTop() + $(window).height() >= $(document).height() * .8) {
				var url = caramel.url(store.asset.paging.url + (++currentPage));
				loadAssetsScroll(url);
				$(window).unbind('scroll', scroll);
				setTimeout(function() {
					$(window).bind('scroll', scroll);
				}, 500);
			}
		} else {
			$('.loading-inf-scroll').hide();
		}
	}

	$(window).bind('scroll', scroll);

	$("a[data-toggle='tooltip']").tooltip();
	
	$('#my-assets').hide();
	$('.my-assets-link').click(function(){

		if($(this).find('.pull-right').hasClass('icon-angle-down')){
			$(this).find('.pull-right').removeClass('icon-angle-down').addClass('icon-angle-up');
		}else{
			$(this).find('.pull-right').removeClass('icon-angle-up').addClass('icon-angle-down');
		}
		$('#my-assets').slideToggle("fast");
	});

	//caramel.loaded('js', 'assets');
	//caramel.loaded('js', 'sort-assets');
});
