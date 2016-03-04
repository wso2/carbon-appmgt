var render = function (theme, data, meta, require) {
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
                context: data.searchQuery
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
                context: {favouriteApps: data.favouriteApps}
            }
        ]
    });
};

function createSortOptions(data) {
    var url = "/assets/favouriteapps?type=" + data.assetType + "&sort=";
    var sortOptions = {};
    var sortByAlphabet = {url: url + "az", title: "Sort by Alphabetical Order", class: "fw fw-sort"};
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
            active: true, partial: 'my-favorites', url: context + "/assets/favouriteapps"
        }
    ];

    leftNavigationData.push({
                                active: false, partial: 'my-apps', url: context + "/extensions/assets/" + data.assetType
            + "/myapps"
                            });

    if (!data.navigation.showAllAppsLink) {
        leftNavigationData.push({
                                    active: false, partial: 'all-apps', url: context + "/assets/"
                + data.assetType
                                });
    }

    return leftNavigationData;
}