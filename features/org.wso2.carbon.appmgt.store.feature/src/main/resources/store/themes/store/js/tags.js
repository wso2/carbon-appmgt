$(function() {

	History.Adapter.bind(window, 'statechange', function() {
		var state = History.getState();
		if (state.data.id === 'tags') {
			renderAssets(state.data.context);
		}
	});

	$('.store-tags').on('click', 'a', function(e) {
        var log = new Log();
		e.preventDefault();
		var url = $(this).attr('href'),
			tagName = $(this).text();
			
		currentPage = 1;
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
						id : 'tags',
						context : data
					}, document.title, url);
				}
				$('.search-bar h2').find('.page').text(' / Tag: "' + tagName + '"');

			},
            error: function (xhr, status, error) {
                if (log.isDebugEnabeld()) {
                    log.debug("Error occurred while retrieving data for the url: " + url
                    + ", when searching by the tag name: " + tagName);
                }
                theme.loaded($('.store-left'), '<p>Error while retrieving data.</p>');
            }
		});
		theme.loading($('.store-left'));
	});
});
