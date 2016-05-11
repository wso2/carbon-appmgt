var resources = function (page, meta) {
    return {
        js: ['lib/fileupload/bootstrap-fileupload.min.js',
             'lib/fileupload/jquery.fileupload.js',
             'logic/edit.mobileapp.js',
             'bootstrap-select.min.js',
             'options.text.js'],
        css: ['lib/fileupload/bootstrap-fileupload.min.css',
              'mobileapp/main.css',
              'bootstrap-select.min.css']
    };
};

var selectCategory = function (data) {
    var selectedCategories = [];
    var currentCategory = data.artifact.attributes['overview_category'];
    var categories = selectCategories(data.data.fields);

    for (var i in categories) {
        selectedCategories.push({
            cat: categories[i],
            sel: (currentCategory == categories[i])
        });
    }
    data.categorySelect = selectedCategories;
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
