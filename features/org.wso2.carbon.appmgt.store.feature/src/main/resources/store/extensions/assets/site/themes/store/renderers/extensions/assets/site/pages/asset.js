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
        isSelfSubscriptionEnabled: data.config.isSelfSubscriptionEnabled,
        isEnterpriseSubscriptionEnabled: data.config.isEnterpriseSubscriptionEnabled,
        isEnterpriseSubscriptionAllowed: data.isEnterpriseSubscriptionAllowed,
        metadata:data.metadata,
        tabs:{
            documentation:{
                data:data.documentation,
                assetType:"site"
            }
        }
    };

    data.tags.tagUrl = getTagAndSearchUrl(data).tagUrl;
    var searchUrl = getTagAndSearchUrl(data).searchUrl;

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
                context: {searchQuery:data.search.query,searchUrl:searchUrl}
            }
        ],
        pageHeader: [
            {
                partial: 'page-header',
                context: {
                    title: "Site",
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
                                    active: true, partial: 'all-apps', url: "/assets/site"
                                });
        leftNavigationData.push({
                                    active: false, partial: 'my-apps', url: "/extensions/assets/site/myapps"
                                });
    } else {
        leftNavigationData.push({
                                    active: true, partial: 'my-apps', url: "/extensions/assets/site/myapps"
                                });
    }

    if (data.user) {
        leftNavigationData.push({
                                    active: false, partial: 'my-favorites', url: "/assets/favouriteapps?type=site"
                                });
    }

    return leftNavigationData;
}

function getTagAndSearchUrl(data) {
    var URLs = {}
    var isSelfSubscriptionEnabled = data.config.isSelfSubscriptionEnabled;
    var isEnterpriseSubscriptionEnabled = data.config.isEnterpriseSubscriptionEnabled;
    if (!isSelfSubscriptionEnabled && !isEnterpriseSubscriptionEnabled) {
        URLs.tagUrl = '/extensions/assets/site/myapps';
        URLs.searchUrl = '/extensions/assets/site/myapps';
    } else {
        URLs.tagUrl = '/assets/site';
        URLs.searchUrl = '/assets/site';
    }
    return URLs;
}