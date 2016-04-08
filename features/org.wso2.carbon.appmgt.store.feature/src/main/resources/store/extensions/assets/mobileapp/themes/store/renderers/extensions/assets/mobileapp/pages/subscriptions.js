/*
 var render = function (theme, data, meta, require) {
 //print(caramel.build(data));

 theme('1-column', {
 title: data.title,
 navigation: [
 {
 partial: 'navigation',
 context: data.navigation
 }
 ],
 body: [
 {
 partial: 'userAssets',
 context: data.userAssets
 }
 ]
 });
 };

 */


var render = function (theme, data, meta, require) {
    var categories = data.navigation.assets[data.type].categories;
    var searchUrl = "/extensions/assets/mobileapp/myapps";
    var searchQuery =  data.search.query

    var storeObj = jagg.module("manager").getAPIStoreObj();

    var enabledTypeList = storeObj.getEnabledAssetTypeList();
    data.tags.tagUrl = "/assets/mobileapp";
    if(data.userAssets){


        useragent = request.getHeader("User-Agent");


        if(useragent.match(/iPad/i) || useragent.match(/iPhone/i)) {
            userOS = 'ios';
        } else if (useragent.match(/Android/i)) {
            userOS = 'android';
        } else {
            userOS = 'unknown';
        }

        var assets = [];


        for(var i = 0; i < data.userAssets.mobileapp.length; i++){
            var app = data.userAssets.mobileapp[i];
            var platform = app.attributes.overview_platform;
            switch(userOS){
                case "android":
                    if(platform === "android" || platform === "webapp"){
                        app.isActive = isActive(app);
                        assets.push(app);
                    }
                    break;
                case "ios":
                    if(platform === "ios" || platform === "webapp"){
                        app.isActive = isActive(app);
                        assets.push(app);
                    }
                    break;
                default:
                    app.isActive = isActive(app);
                    assets.push(app);
            }
        }

        data.userAssets.mobileapp = assets;

    }


    data.header.myApps = true;


    if(storeObj.isAssetTypeEnabled("mobileapp")) {
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
                    context: {
                        searchQuery: searchQuery,
                        categories: categories,
                        searchUrl: searchUrl
                    }
                }
            ],
            pageHeader: [
                {
                    partial: 'page-header',
                    context: {
                        title: "Mobile Apps",
                        sorting: null
                    }
                }
            ],
            pageContent: [
                {
                    partial: 'page-content-myapps',
                    context: {
                        'userAssets': data.userAssets,
                        'URL': data.URL,
                        'devices': data.devices,
                        'selfUnsubscription': data.selfUnsubscription,
                        'isDeviceSubscriptionEnabled': data.isDeviceSubscriptionEnabled,
                        'searchQuery':searchQuery
                    }
                }
            ]
        });

    }
    else {
        response.sendError(404, 'Resource does not exist');
    }
};


function createSortOptions(data) {
    var url = "/extensions/assets/mobileapp/subscriptions?sort=";
    var sortOptions = {};
    var sortByAlphabet = {url: url + "az", title: "Sort by Alphabetical Order", class: "fw fw-list-sort"};
    var sortByRecent = {url: url + "recent", title: "Sort by Recent", class: "fw fw-calendar"};

    var options = [];
    options.push(sortByAlphabet);
    options.push(sortByRecent);// recently created
    sortOptions["options"] = options;
    return sortOptions;
}


function createLeftNavLinks(data) {
    var enabledTypeList = data.config.enabledTypeList;
    var subscriptionOn = true;
    if (!data.config.isSelfSubscriptionEnabled && !data.config.isEnterpriseSubscriptionEnabled) {
        subscriptionOn = false;
    }
    var currentAppType = 'mobileapp';

    var leftNavigationData = [
        {
            active: true, partial: currentAppType, url: "/assets/" + currentAppType,
            myapps: true, myappsUrl: "/extensions/assets/" + currentAppType + "/myapps"
        }
    ];

    for (var i = 0; i < enabledTypeList.length; i++) {
        if (enabledTypeList[i] != currentAppType) {
            var data;
            if (subscriptionOn) {
                data = {
                    active: false, partial: enabledTypeList[i], url: "/assets/" +
                                                                     enabledTypeList[i]
                }
            } else {
                data = {
                    active: false, partial: enabledTypeList[i], url: "/extensions/assets/" +
                                                                     enabledTypeList[i] + "/apps"
                }
            }
            leftNavigationData.push(data);
        }

    }
    return leftNavigationData;
}

/**
 * check whether asset has an active life cycle.
 * return true if published/deprecated else false
 */
function isActive(asset) {
    var active = false;
    var lifeCycleState = asset.lifecycleState.toUpperCase();
    if (lifeCycleState == "PUBLISHED" || lifeCycleState == "DEPRECATED") {
        active = true;
    }
    asset.lifecycleState = lifeCycleState;
    return active
};