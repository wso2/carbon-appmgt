var render = function (theme, data, meta, require) {
    var assets = require('/helpers/page-content-myapps.js');
    var bodyContext = assets.currentPage(data.assets, data.sso, data.user, data.config, data.pagination.leftNav,
                                         data.pagination.rightNav, data.pagination.urlQuery, data.user, data.assetType);

    var searchQuery = data.search.query;
    if (typeof(searchQuery) != typeof({})) {
        searchQuery = {overview_name: searchQuery, searchTerm: 'overview_name', search: searchQuery};
    } else {
        for (var key in searchQuery) {
            if (searchQuery.hasOwnProperty(key)) {
                if (key.indexOf("overview_") !== -1 && key.indexOf("overview_treatAsASite") == -1) {
                    searchQuery.searchTerm = key;
                    searchQuery.search = searchQuery[key];
                }
            }
        }
    }
    bodyContext.searchQuery =searchQuery;

    data.tags.tagUrl = getTagUrl(data);
    var searchUrl = '/extensions/assets/webapp/myapps';

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
                    assetType: data.assetType,
                    hideTag: hideTag(data)
                }
            }
        ],
        search: [
            {
                partial: 'search',
                context: {searchQuery:searchQuery,searchUrl:searchUrl}
            }
        ],
        pageHeader: [
            {
                partial: 'page-header',
                context: {
                    title: "Web Apps",
                    sorting: createSortOptions(data.user, data.config)
                }
            }
        ],
        pageContent: [
            {
                partial: 'page-content-myapps',
                context: bodyContext
            }
        ]
    });
};

function createSortOptions(user, config) {
    var isSelfSubscriptionEnabled = config.isSelfSubscriptionEnabled;
    var isEnterpriseSubscriptionEnabled = config.isEnterpriseSubscriptionEnabled;
    var subscriptionOn = true;
    if (!isSelfSubscriptionEnabled && !isEnterpriseSubscriptionEnabled) {
        subscriptionOn = false;
    }
    var url = "/extensions/assets/webapp/apps?sort=";
    if(subscriptionOn){
        url = "/extensions/assets/webapp/myapps?sort=";
    }
    var sortOptions = {};
    var sortByPopularity = {url: url + "popular", title: "Sort by Popularity", class: "fw fw-star"};
    var sortByAlphabet = {url: url + "az", title: "Sort by Alphabetical Order", class: "fw fw-list-sort"};
    var sortByRecent = {url: url + "recent", title: "Sort by Recent", class: "fw fw-calendar"};
    var sortByUsage = {url: url + "usage", title: "Sort by Usage", class: "fw fw-statistics"};

    var options = [];

    if (!subscriptionOn) {
        options.push(sortByAlphabet);
        options.push(sortByRecent);// recently added
        options.push(sortByPopularity);
        if (user) {
            options.push(sortByUsage);
        }
    } else {
        if (user) {
            options.push(sortByAlphabet);
            options.push(sortByRecent);// recently subscribed
        }
    }

    sortOptions["options"] = options;
    return sortOptions;
}

function createLeftNavLinks(data) {
    var enabledTypeList = data.config.enabledTypeList;
    var leftNavigationData = [];
    var subscriptionOn = true;
    if (!data.config.isSelfSubscriptionEnabled && !data.config.isEnterpriseSubscriptionEnabled) {
        subscriptionOn = false;
    }
    var currentAppType = 'webapp';

    if(subscriptionOn) {
        var data =  { active: true, partial: currentAppType, url: "/assets/"+currentAppType,
            myapps: true, myappsUrl: "/extensions/assets/"+currentAppType+"/myapps" };
        leftNavigationData.push(data)
        for (var i = 0; i < enabledTypeList.length; i++) {
                if (enabledTypeList[i] != currentAppType) {
                        leftNavigationData.push({
                                                    active: false, partial: enabledTypeList[i], url: "/assets/" +
                                                                                                     enabledTypeList[i]
                                                });

                }

        }
    } else {
        var data =  { active: true, partial: currentAppType, url: "/extensions/assets/webapp/apps"};
        leftNavigationData.push(data)
        for (var i = 0; i < enabledTypeList.length; i++) {
                if (enabledTypeList[i] != currentAppType) {
                    if (enabledTypeList[i] == 'mobileapp') {
                        leftNavigationData.push({
                                                    active: false, partial: enabledTypeList[i], url: "/assets/" +
                                                                                                     enabledTypeList[i]
                                                });
                    } else {
                        leftNavigationData.push({
                                                    active: false, partial: enabledTypeList[i], url: "/extensions/assets/" +
                                                                                                     enabledTypeList[i] + "/apps"
                                                });
                    }

                }

        }
    }

    return leftNavigationData;
}

function getTagUrl(data) {
    var tagUrl;
    var isSelfSubscriptionEnabled = data.config.isSelfSubscriptionEnabled;
    var isEnterpriseSubscriptionEnabled = data.config.isEnterpriseSubscriptionEnabled;
    if (!isSelfSubscriptionEnabled && !isEnterpriseSubscriptionEnabled) {
        tagUrl = '/extensions/assets/webapp/apps';
    } else {
        tagUrl = '/assets/webapp';
    }
    return tagUrl;
}

function hideTag(data){
    var isSelfSubscriptionEnabled = data.config.isSelfSubscriptionEnabled;
    var isEnterpriseSubscriptionEnabled = data.config.isEnterpriseSubscriptionEnabled;
    if (!isSelfSubscriptionEnabled && !isEnterpriseSubscriptionEnabled) {
        return false;
    }
    return true;
}