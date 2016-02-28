var resources = function (page, meta) {
	var log = new Log();
	return {
		js: ['bootstrap-select.min.js'],
		css: ['bootstrap-select.min.css']
	};

};

var selectCategory = function (data) {
	var selected,
		selectedCategories = [],
		currentCategory = data.artifact.attributes['overview_category'],
		categories = selectCategories(data.data.fields);

	for (var i in categories) {
		selected = (currentCategory == categories[i]) ? true : false;
		selectedCategories.push({
			cat: categories[i],
			sel: selected
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
	var screenShots = screenShotsCombinedString.split(",");
	data.artifact.attributes.images_screenshots = screenShots;
	return data;
};
