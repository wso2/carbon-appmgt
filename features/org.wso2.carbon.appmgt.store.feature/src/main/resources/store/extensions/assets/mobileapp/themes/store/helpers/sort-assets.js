var format = function (context, paging, navigation, type, selectedCategory, selectedPlatform) {
    return {
        url: context.url,
        categories : navigation.assets[type].categories,
        type: type,
        selectedCategory: selectedCategory,
        selectedPlatform: selectedPlatform
    };
};

var resources = function (page, meta) {
    return {
        js: ['sort-assets.js'],
        css: ['sort-assets.css']
    };
};