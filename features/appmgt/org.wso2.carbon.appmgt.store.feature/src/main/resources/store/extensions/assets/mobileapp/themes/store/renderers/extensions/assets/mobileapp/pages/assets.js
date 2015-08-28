var render = function (theme, data, meta, require) {


    data.header.config = data.config;


    var searchQuery =  data.search.query;
    if(typeof(searchQuery) != typeof({})){
        searchQuery = {overview_name : searchQuery, searchTerm: 'overview_name', search : searchQuery};
    }else{
        for (var key in searchQuery) {
            if (searchQuery.hasOwnProperty(key)) {
                if(key.indexOf("overview_") !== -1){
                    searchQuery.searchTerm = key;
                    searchQuery.search = searchQuery[key];
                }
            }
        }
    }

    data.header.categories = data.navigation.assets[data.type].categories;
    data.header.selectedPlatform = data.selectedPlatform;
    data.header.selectedCategory = data.selectedCategory;
    data.header.searchQuery = searchQuery;
    data.header.assetsPage = true;
		
    var assets = require('/helpers/assets.js');
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
                context: require('/helpers/sort-assets.js').format(data.sorting, data.paging, data.navigation, data.type, data.selectedCategory, data.selectedPlatform)
            },
            {
                partial: 'assets',
                context: assets.currentPage(data.assets,data.sso,data.user, data.paging,data.config, data.myAssets.pageIndices, data.myAssets.leftNav, data.myAssets.rightNav)

            }/*,
            {
                partial: 'pagination',
                context: require('/helpers/pagination.js').format(data.paging)
            } */
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