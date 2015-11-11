var render = function (theme, data, meta, require) {
    var log = new Log("Site=========> " );
    var assets = require('/helpers/assets.js');
    var bodyPartial = "assets";
    var bodyContext =  assets.currentPage(data.assets,data.sso,data.user, data.paging,data.config, data.myAssets.pageIndices, data.myAssets.leftNav, data.myAssets.rightNav, data.myAssets.urlQuery);

    if(request.getHeader("User-Agent").indexOf("Mobile") != -1){  //mobile devices
        bodyPartial = "assets-for-mobiles";
        bodyContext =  assets.currentPage(data.assets,data.sso,data.user, data.paging,data.config, data.myAssets.pageIndices, data.myAssets.leftNav, data.myAssets.rightNav);
    }

    var hasApps = false;
    if(data.assets.length > 0){
        hasApps = true;
    }else{
        hasApps = false;
    }

    var searchQuery =  data.search.query;
    if(typeof(searchQuery) != typeof({})){
        searchQuery = {overview_name : searchQuery, searchTerm: 'overview_name', search : searchQuery};
    }else{
        for (var key in searchQuery) {
            if (searchQuery.hasOwnProperty(key)) {
                if(key.indexOf("overview_") !== -1){
                    if(key.indexOf("overview_treatAsASite") == -1) {
                        searchQuery.searchTerm = key;
                        searchQuery.search = searchQuery[key];
                    }
                }
            }
        }
    }

    data.header.searchQuery = searchQuery;


    theme('2-column-right', {
        title: data.title,
        header: [
            {
                partial: 'header',
                context: data.header
            }
        ],
        navigation: [
            {
                partial: 'navigation',
                context: require('/helpers/navigation.js').currentPage(data.navigation, data.type, assets.format(data.search))
            }
        ],

        body: [
            {
                partial: 'sort-assets',
                context: require('/helpers/sort-assets.js').format(data.sorting, data.paging, data.navigation, data.type, data.selectedCategory, data.header, hasApps)
            },
            {
                partial: bodyPartial,
                context: bodyContext
            }
        ],
        right: [
            {
                partial: 'my-assets-link',
                context: data.myAssets
            },
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