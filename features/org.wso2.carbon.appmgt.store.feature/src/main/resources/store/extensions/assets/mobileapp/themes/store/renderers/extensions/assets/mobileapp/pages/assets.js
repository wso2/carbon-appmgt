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

    var categories = data.navigation.assets[data.type].categories;
    var selectedPlatform = data.selectedPlatform;
    var selectedCategory = data.selectedCategory;

    var assets = require('/helpers/page-content-all-mobile-apps.js');
    var bodyContext = assets.currentPage(data.assets, data.sso, data.user, data.pagination, data.config);
    bodyContext.searchQuery =searchQuery;
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
                context: {searchQuery: searchQuery, categories: categories, selectedPlatform: selectedPlatform,
                    selectedCategory: selectedCategory,searchUrl:searchUrl}
            }
        ],
        pageHeader: [
            {
                partial: 'page-header',
                context: {
                    title: "Mobile Apps",
                    sorting: createSortOptions(data)
                }
            }
        ],
        pageContent: [
            {
                partial: 'page-content-all-mobile-apps',
                context: bodyContext
            }
        ]
    });


};

function createSortOptions(data) {
    var url = "/assets/mobileapp?sort=";
    var sortOptions = {};
    var sortByPopularity = {url: url + "popular", title: "Sort by Popularity", class: "fw fw-star"};
    var sortByAlphabet = {url: url + "az", title: "Sort by Alphabetical Order", class: "fw fw-list-sort"};
    var sortByRecent = {url: url + "recent", title: "Sort by Recent", class: "fw fw-calendar"};

    var options = [];
    options.push(sortByAlphabet);
    options.push(sortByRecent);// recently created
    options.push(sortByPopularity);
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
    var leftNavigationData = [];

    if(data.user) {
        var data =  {
            active: true, partial: currentAppType, url: "/assets/" + currentAppType,
            myapps: false, myappsUrl: "/extensions/assets/" + currentAppType + "/myapps"
        }
        leftNavigationData.push(data)
    } else {
        var data =  {
            active: true, partial: currentAppType, url: "/assets/" + currentAppType
        }
        leftNavigationData.push(data)
    }

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