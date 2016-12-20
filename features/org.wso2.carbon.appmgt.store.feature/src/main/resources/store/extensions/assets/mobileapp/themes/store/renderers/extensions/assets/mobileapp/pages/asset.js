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
    data.tags.tagUrl = "/assets/mobileapp";

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
                                                                           isUpdatedApp: data.isUpdatedApp,
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
    var enabledTypeList = data.config.enabledTypeList;
    var leftNavigationData = [];
    var subscriptionOn = true;
    if (!data.config.isSelfSubscriptionEnabled && !data.config.isEnterpriseSubscriptionEnabled) {
        subscriptionOn = false;
    }
    var currentType = "mobileapp";

    if (subscriptionOn) {
        leftNavigationData.push({
                                    active: true, partial: currentType, url: "/assets/" + currentType

                                });

        for (var i = 0; i < enabledTypeList.length; i++) {
            if (enabledTypeList[i] != currentType) {
                leftNavigationData.push({
                                            active: false, partial: enabledTypeList[i], url: "/assets/"
                        + enabledTypeList[i]

                                        });
            }
        }
    } else {
        leftNavigationData.push({
                                    active: true, partial: currentType, url: "/assets/" +
                                                                             currentType
                                });
        for (var i = 0; i < enabledTypeList.length; i++) {
            if (enabledTypeList[i] != currentType) {
                leftNavigationData.push({
                                            active: false, partial: enabledTypeList[i], url: "/extensions/assets/" +
                                                                                             enabledTypeList[i] + "/apps"
                                        });

            }
        }
    }
    return leftNavigationData;
}