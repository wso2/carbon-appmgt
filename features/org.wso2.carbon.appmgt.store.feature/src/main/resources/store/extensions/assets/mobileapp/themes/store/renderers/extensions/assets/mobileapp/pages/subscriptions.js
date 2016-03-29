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
    var searchUrl = "/extensions/assets/mobileapp/subscriptions";
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


        for( i = 0; i < data.userAssets.mobileapp.length; i++){

            var platform = data.userAssets.mobileapp[i].attributes.overview_platform;
            switch(userOS){
                case "android":
                    if(platform === "android" || platform === "webapp"){
                        assets.push(data.userAssets.mobileapp[i]);
                    }
                    break;
                case "ios":
                    if(platform === "ios" || platform === "webapp"){
                        assets.push(data.userAssets.mobileapp[i]);
                    }
                    break;
                default:
                    assets.push(data.userAssets.mobileapp[i]);
            }
        }

        data.userAssets.mobileapp = assets;




        for(i = 0; i < data.userAssets.mobileapp.length; i++){
            //print(data.userAssets.mobileapp[i].lifecycleState);
            if(data.userAssets.mobileapp[i].lifecycleState == 'Unpublished'){
                delete data.userAssets.mobileapp.splice (i, 1);;
            }
        }
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
                        title: "My Mobile Apps",
                        sorting: null
                    }
                }
            ],
            pageContent: [
                {
                    partial: 'page-content-userAssets',
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
    var leftNavigationData = [];
    var isAllAppsActive = true;

    if (data.user) {
        leftNavigationData.push({
                                    active: true, partial: 'my-apps', url: "/extensions/assets/mobileapp/subscriptions"
                                });
        isAllAppsActive = false;
    }

    leftNavigationData.push({
                                active: isAllAppsActive, partial: 'all-apps', url: "/assets/mobileapp"
                            });

    return leftNavigationData;
}