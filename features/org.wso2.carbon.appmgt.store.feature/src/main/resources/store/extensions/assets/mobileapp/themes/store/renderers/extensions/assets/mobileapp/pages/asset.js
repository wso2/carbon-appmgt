var render = function(theme, data, meta, require) {
	
	
	var images = data.asset.attributes.images_screenshots.split(",");
	data.asset.attributes.images_screenshots = images;
	
		
	data.header.config = data.config;


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
                    recentApps: require('/helpers/asset.js').formatRatings(data.recentAssets)
                }
            }
        ],
        search: [
            {
                partial: 'search',
                context: {}
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