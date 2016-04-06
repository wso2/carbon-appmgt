var render = function (theme, data, meta, require) {

    var appUrl; // User is present here, so no need to check for user session.
    if (!data.isSelfSubscriptionEnabled && !data.isEnterpriseSubscriptionAllowed) {
        // Subscription model is inactive, so any user can access this app.
        appUrl = getAppUrl(data);
    } else {
        // Subscription model is active.
        if (data.asset.attributes['overview_allowAnonymous'].toUpperCase() == "TRUE") {
            // This is an anonymous app, so anyone can access it.
            appUrl = getAppUrl(data);
        } else if (data.isSubscribed && !data.subscriptionInfo.individualSubscription.OnHold) {
            // User has subscribed to this app.
            appUrl = getAppUrl(data);
        } else {
            appUrl = null;
        }
    }

    var bodyContext = {
        sso: data.sso,
        user: data.user,
        tenantId: data.tenantId,
        inDashboard: data.inDashboard,
        asset: data.asset,
        type: data.type,
        appUrl: appUrl,
        isFavouriteApp: data.isFavourite,
        isSocial: data.isSocial,
        skipGateway: data.skipGateway,
        apiData: data.apiData,
        isSubscribed: data.isSubscribed,
        subscriptionInfo: data.subscriptionInfo,
        isSubscriptionAvailable: data.isSubscriptionAvailable,
        isSelfSubscriptionEnabled: data.isSelfSubscriptionEnabled,
        isEnterpriseSubscriptionEnabled: data.isEnterpriseSubscriptionEnabled,
        isEnterpriseSubscriptionAllowed: data.isEnterpriseSubscriptionAllowed,
        businessOwner: data.businessOwner,
        ownerName: data.ownerName,
        metadata:data.metadata,
        tabs:{
            documentation:{
                data:data.documentation,
                assetType:"webapp"
            }
        }
    };

    //var assetsByProvider = data.assetsByProvider;
    //assetsByProvider['assets'] =
    // require('/helpers/rating-provider.js').ratingProvider.formatRating(data.assetsByProvider.assets);

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
                    title: "Web App",
                    sorting: null
                }
            }
        ],
        pageContent: [
            {
                partial: 'page-content-app-details',
                context: bodyContext
            }
        ]
    });
};

function getAppUrl(data) {
    return (data.skipGateway) ? data.asset.attributes['overview_webAppUrl'] : data.apiData.serverURL.productionURL;
}

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