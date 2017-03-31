$(function() {
    var tagsWrapper = $('#store-tags-wrapper');
    tagsWrapper.html("Loading tags ...");
    caramel.ajax({
        url: '/apis/tag/webapp',
        dataType: 'json',
        success: function (data, status, xhr) {
            var context = {
                url: '/assets/' + store.asset.type + '/?page=1&tag=',
                tags: data
            };
            caramel.render('tags', context, function (e, html) {
                tagsWrapper.html(html || "Cannot load tags!");
            });
        },
        error: function (xhr, status, error) {
            tagsWrapper.html("Cannot load tags!");
        }
    });
});
