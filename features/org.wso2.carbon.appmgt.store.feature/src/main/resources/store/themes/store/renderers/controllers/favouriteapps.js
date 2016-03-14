var render = function (theme, data, meta, require) {
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
                    tags: data.tags
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
                    title: "Favourite Apps",
                    sorting: createSortOptions(data)
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
    var url = "/assets/favouriteapps?type=" + data.assetType + "&sort=";
    var sortByAlphabet = {url: url + "az", title: "Sort by Alphabetical Order", class: "fw fw-list-sort"};
    var sortByRecent = {url: url + "recent", title: "Sort by Recent", class: "fw fw-calendar"};
    var options = [];
    options.push(sortByAlphabet);
    options.push(sortByRecent);
    sortOptions["options"] = options;
    return sortOptions;
}

function createLeftNavLinks(data) {
    var context = caramel.configs().context;
    var leftNavigationData = [
        {
            active: true, partial: 'my-favorites', url: "/assets/favouriteapps?type=" + data.assetType
        }
    ];

    leftNavigationData.push({
                                active: false, partial: 'my-apps', url: "/extensions/assets/" + data.assetType
            + "/myapps"
                            });

    if (data.navigation.showAllAppsLink) {
        leftNavigationData.push({
                                    active: false, partial: 'all-apps', url: "/assets/" + data.assetType
                                });
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
            URLs.searchUrl = '/assets/favouriteapps?type=webapp';
        } else {
            URLs.tagUrl = '/extensions/assets/site/myapps';
            URLs.searchUrl = '/assets/favouriteapps?type=site';
        }
    } else {
        if (data.assetType == "webapp") {
            URLs.tagUrl = '/assets/webapp';
            URLs.searchUrl = '/assets/favouriteapps?type=webapp';
        } else {
            URLs.tagUrl = '/assets/site';
            URLs.searchUrl = '/assets/favouriteapps?type=site';
        }
    }
    return URLs;
}