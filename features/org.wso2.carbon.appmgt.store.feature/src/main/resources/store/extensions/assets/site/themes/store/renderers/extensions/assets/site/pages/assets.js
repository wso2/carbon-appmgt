var render = function (theme, data, meta, require) {
    var assets = require('/helpers/page-content-all-apps.js');
    var bodyContext = assets.currentPage(data.assets, data.sso, data.user, data.config, data.pagination.leftNav,
                                         data.pagination.rightNav, data.pagination.urlQuery, data.user,data.assetType);

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

    data.header.searchQuery = searchQuery;

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
                context: searchQuery
            }
        ],
        pageHeader: [
            {
                partial: 'page-header',
                context: {
                    title: "All Web Apps",
                    sorting: createSortOptions(data)
                }
            }
        ],
        pageContent: [
            {
                partial: 'page-content-all-apps',
                context: bodyContext
            }
        ]
    });
};

function createSortOptions(data) {
    var url = "/assets/site?sort=";
    var sortOptions = {};
    var sortByPopularity = {url: url + "popular", title: "Sort by Popularity", class: "fw fw-star"};
    var sortByAlphabet = {url: url + "az", title: "Sort by Alphabetical Order", class: "fw fw-sort"};
    var sortByRecent = {url: url + "recent", title: "Sort by Recent", class: "fw fw-calendar"};
    var sortByUsage = {url: url + "usage", title: "Sort by Usage", class: "fw fw-statistics"};

    var options = [];
    options.push(sortByAlphabet);
    options.push(sortByRecent);// recently created
    options.push(sortByPopularity);
    if (data.user) {
        options.push(sortByUsage);
    }
    sortOptions["options"] = options;
    return sortOptions;
}


function createLeftNavLinks(data) {
    var context = caramel.configs().context;
    var leftNavigationData = [
        {
            active: true, partial: 'all-apps', url: context + "/assets/site"
        }
    ];

    leftNavigationData.push({
                                active: false, partial: 'my-apps', url: context + "/extensions/assets/site/myapps"
                            });
    if (data.user) {
        leftNavigationData.push({
                                    active: false, partial: 'my-favorites', url: context
                + "/assets/favouriteapps?type=site"
                                });
    }
    return leftNavigationData;
}