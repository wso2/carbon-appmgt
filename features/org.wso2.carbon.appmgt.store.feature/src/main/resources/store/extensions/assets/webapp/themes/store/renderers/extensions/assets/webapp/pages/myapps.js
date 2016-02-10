var render = function (theme, data, meta, require) {
    var log = new Log();
    var assets = require('/helpers/myapps.js');
    var bodyPartial = "myapps";
    var bodyContext = assets.currentPage(data.assets, data.sso, data.user, data.config,
                                         data.pagination.leftNav, data.pagination.rightNav,
                                         data.pagination.urlQuery, data.user);

    if (request.getHeader("User-Agent").indexOf("Mobile") != -1) {  //mobile devices
        bodyPartial = "assets-for-mobiles";
        bodyContext =
        assets.currentPage(data.assets, data.sso, data.user, data.config,
                           data.pagination.leftNav, data.pagination.rightNav, data.pagination.urlQuery);
    }

    var hasApps = false;
    if (data.assets.length > 0) {
        hasApps = true;
    }

    var searchQuery = data.search.query;
    if (typeof(searchQuery) != typeof({})) {
        searchQuery = {overview_name: searchQuery, searchTerm: 'overview_name', search: searchQuery};
    } else {
        for (var key in searchQuery) {
            if (searchQuery.hasOwnProperty(key)) {
                if (key.indexOf("overview_") !== -1 && key.indexOf("overview_treatAsASite") == -1) {
                    searchQuery.searchTerm = key;
                    searchQuery.search = searchQuery[key];
                }
            }
        }
    }
    data.header.searchQuery = searchQuery;

    var page = '1-column';
    if (!data.config.isSelfSubscriptionEnabled && !data.config.isEnterpriseSubscriptionEnabled) {
        //need to display tags and recent assets in my apps page
        page = '2-column-right'
    }

    theme(page, {
        title: data.title,
        header: [
            {
                partial: 'header',
                context: data.header
            }
        ],
        body: [
            {
                partial: 'sort-assets',
                context: require('/helpers/sort-assets.js').format(data.sorting, data.header, hasApps)
            },
            {
                partial: bodyPartial,
                context: bodyContext
            }

        ],
        right: [
            {
                partial: 'recent-assets',
                context: require('/helpers/asset.js').formatRatings(data.recentAssets)
            },
            {
                partial: 'tags',
                context: data.tags
            }
        ]
    });
};