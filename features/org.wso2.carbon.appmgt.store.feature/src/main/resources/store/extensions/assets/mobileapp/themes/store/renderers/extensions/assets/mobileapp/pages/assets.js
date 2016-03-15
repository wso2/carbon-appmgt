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
    var bodyContext = assets.currentPage(data.assets, data.sso, data.user, data.paging, data.config,
                                         data.myAssets.pageIndices, data.myAssets.leftNav, data.myAssets.rightNav);

    var searchUrl = "/assets/mobileapp";

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
                context: {searchQuery: searchQuery, categories: categories, selectedPlatform: selectedPlatform,
                    selectedCategory: selectedCategory,searchUrl:searchUrl}
            }
        ],
        pageHeader: [
            {
                partial: 'page-header',
                context: {
                    title: "All Mobile Apps",
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
    var sortByAlphabet = {url: url + "az", title: "Sort by Alphabetical Order", class: "fw fw-list-sort"};
    var sortByRecent = {url: url + "recent", title: "Sort by Recent", class: "fw fw-calendar"};

    var options = [];
    options.push(sortByAlphabet);
    options.push(sortByRecent);// recently created
    sortOptions["options"] = options;
    return sortOptions;
}


function createLeftNavLinks(data) {
    var leftNavigationData = [
        {
            active: true, partial: 'all-apps', url: "/assets/mobileapp"
        }
    ];


    if (data.user) {
        leftNavigationData.push({
                                    active: false, partial: 'my-apps', url: "/extensions/assets/mobileapp/subscriptions"
                                });
    }
    return leftNavigationData;
}