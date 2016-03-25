var resources = function (page, meta) {
	return {
		js: ['bootstrap-select.min.js'],
		css: ['bootstrap-select.min.css']
	};

};

var selectCategory = function (data) {
	var selectedCategories = [],
		currentCategory = data.artifact.attributes['overview_category'],
		categories = selectCategories(data.data.fields);

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
	var screenShotsCombinedString = data.artifact.attributes.images_screenshots;
	data.artifact.attributes.images_screenshots = screenShotsCombinedString.split(",");
	return data;
};
