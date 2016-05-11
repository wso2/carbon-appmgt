var render = function (theme, data, meta, require) {
    data.tags.tagUrl = getTagAndSearchUrl(data).tagUrl;
    var searchUrl = getTagAndSearchUrl(data).searchUrl;
    data.header.active = 'favourite';

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
                    assetType: data.assetType,
                    hideTag: true
                }
            }
        ],
        pageHeader: [
            {
                partial: 'page-header',
                context: {
                    title: "Favourites",
                    sorting: createSortOptions(data),
                    myFav: true,
                    isHomePage: data.isHomePage
                }
            }
        ],
        pageContent: [
            {
                partial: 'page-content-favouriteapps',
                context: {favouriteApps: data.favouriteApps, searchQuery: data.search.query}
            }
        ]
    });
};

function createSortOptions(data) {
    var sortOptions = {};
    if (data.favouriteApps && data.favouriteApps.length == 0) {
        return sortOptions;
    }
    var url = "/assets/favourite?type=" + data.assetType + "&sort=";
    var sortByAlphabet = {url: url + "az", title: "Sort by Alphabetical Order", class: "fw fw-list-sort"};
    var sortByRecent = {url: url + "recent", title: "Sort by Recent", class: "fw fw-calendar"};
    var options = [];
    options.push(sortByAlphabet);
    options.push(sortByRecent);
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
    for (var i = 0; i < enabledTypeList.length; i++) {

        if (subscriptionOn) {
            leftNavigationData.push({
                                        active: false, partial: enabledTypeList[i], url: "/assets/" +
                                                                                         enabledTypeList[i]
                                    });
        } else {
            if (enabledTypeList[i] == 'mobileapp') {
                leftNavigationData.push({
                                            active: false, partial: enabledTypeList[i], url: "/assets/" +
                                                                                             enabledTypeList[i]
                                        });
            } else {
                leftNavigationData.push({
                                            active: false, partial: enabledTypeList[i], url: "/extensions/assets/" +
                                                                                             enabledTypeList[i]
                        + "/apps"
                                        });
            }

        }

    }
    return leftNavigationData;
}

function getTagAndSearchUrl(data) {
    var URLs = {}
    var isSelfSubscriptionEnabled = data.config.isSelfSubscriptionEnabled;
    var isEnterpriseSubscriptionEnabled = data.config.isEnterpriseSubscriptionEnabled;
    if (!isSelfSubscriptionEnabled && !isEnterpriseSubscriptionEnabled) {
        if (data.assetType == "webapp") {
            URLs.tagUrl = '/extensions/assets/webapp/myapps';
            URLs.searchUrl = '/assets/favourite?type=webapp';
        } else {
            URLs.tagUrl = '/extensions/assets/site/myapps';
            URLs.searchUrl = '/assets/favourite?type=site';
        }
    } else {
        if (data.assetType == "webapp") {
            URLs.tagUrl = '/assets/webapp';
            URLs.searchUrl = '/assets/favourite?type=webapp';
        } else {
            URLs.tagUrl = '/assets/site';
            URLs.searchUrl = '/assets/favourite?type=site';
        }
    }
    return URLs;
}