var resources = function (page, meta) {
    return {
        js: ['mobileapp/bootstrap-fileupload.min.js', 'mobileapp/jquery.fileupload.js',
             'mobileapp/edit.mobileapp.js', '/logic/asset.tag.edit.js', 'bootstrap-select.min.js',
             'options.text.js'],
        css: ['mobileapp/bootstrap-fileupload.min.css', 'mobileapp/main.css',
              'bootstrap-select.min.css']
    };
};

var selectCategory = function (data) {
    var arr = [];
    var currentCategory = data.artifact.attributes['overview_category'];
    var categories = selectCategories(data.data.fields);

    for (var i in categories) {
        arr.push({
            cat: categories[i],
            sel: (currentCategory == categories[i])
        });
    }
    data.categorySelect = arr;
    return data;
};

var selectCategories = function (fields) {
    for (var i in fields) {
        if (fields[i].name == "overview_category") {
            return fields[i].valueList;
        }
    }
};

var screenshots = function (data) {
    var screens = data.artifact.attributes.images_screenshots;
    data.artifact.attributes.images_screenshots = screens.split(",");
    return data;
};
