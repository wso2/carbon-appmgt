var render = function(theme, data, meta, require) {
	
	
	var images = data.asset.attributes.images_screenshots.split(",");
	data.asset.attributes.images_screenshots = images;

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

    var categories = data.navigation.assets[data.type].categories;

	data.header.config = data.config;
    var searchUrl = "/assets/mobileapp";

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
                    tags: null,
                    recentApps: data.recentAssets,
                    assetType: data.assetType
                }
            }
        ],
        search: [
            {
                partial: 'search',
                context: {searchQuery: searchQuery, categories: categories, searchUrl: searchUrl}
            }
        ],
        pageHeader: [
            {
                partial: 'page-header',
                context: {
                    title: "Mobile App",
                    sorting: null
                }
            }
        ],
        pageContent: [{
                          partial: 'page-content-app-details',
                          context: require('/helpers/asset.js').format({
                                                                           user: data.user,
                                                                           sso: data.sso,
                                                                           asset: data.asset,
                                                                           type: data.type,
                                                                           inDashboard: data.inDashboard,
                                                                           isEnterpriseInstallEnabled: data.isEnterpriseInstallEnabled,
                                                                           isDeviceSubscriptionEnabled: data.isDeviceSubscriptionEnabled,
                                                                           isDirectDownloadEnabled: data.isDirectDownloadEnabled,
                                                                           embedURL: data.embedURL,
                                                                           isSocial: data.isSocial,
                                                                           devices: data.devices,
                                                                           config: data.config
                                                                       })
                      }]
    });

};


function createLeftNavLinks(data) {
    var context = caramel.configs().context;
    var leftNavigationData = [
        {
            active: true, partial: 'all-apps', url: "/assets/mobileapp"
        }
    ];

    if (data.user) {
        leftNavigationData.push({
                                    active: false, partial: 'my-apps',
                                    url: "/extensions/assets/mobileapp/subscriptions"
                                });
    }

    return leftNavigationData;
}