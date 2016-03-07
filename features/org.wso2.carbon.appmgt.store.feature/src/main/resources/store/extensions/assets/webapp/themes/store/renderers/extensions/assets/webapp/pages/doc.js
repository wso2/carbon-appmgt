
var render = function (theme, data, meta, require) {

    var log = new Log();

    theme('2-column-left', {
        title: data.title,
        header: [
            {
                partial: 'header',
                context: data.header
            }
        ],
        leftColumn: [
            {
                partial: 'left-column',
                context: {
                    navigation: createLeftNavLinks(data),
                    tags: data.tags,
                    recentApps: require('/helpers/asset.js').formatRatings(data.recentAssets)
                }
            }
        ],
        search: [
            {
                partial: 'search',
                context: {searchQuery:data.search.query,searchUrl:data.search.searchUrl}
            }
        ],
        pageHeader: [
            {
                partial: 'page-header',
                context: {
                    title: "Document",
                    sorting: null
                }
            }
        ],
        pageContent: [
            {
                partial:'doc',
                context:data
            }
        ]
    });

};

function createLeftNavLinks(data) {
    var context = caramel.configs().context;
    var leftNavigationData = [

    ];

    if (data.navigation.showAllAppsLink) {
        leftNavigationData.push({
                                    active: true, partial: 'all-apps', url: context + "/assets/webapp"
                                });
        leftNavigationData.push({
                                    active: false, partial: 'my-apps', url: context + "/extensions/assets/webapp/myapps"
                                });
    } else {
        leftNavigationData.push({
                                    active: true, partial: 'my-apps', url: context + "/extensions/assets/webapp/myapps"
                                });
    }

    if (data.user) {
        leftNavigationData.push({
                                    active: false, partial: 'my-favorites', url: context
                + "/assets/favouriteapps?type=webapp"
                                });
    }
    return leftNavigationData;
}