$(function () {

    $('#searchBy li').click(function (e) {
        $('#searchSelect').val($(this).data('value'));
    });

    $('#searchTxt').keypress(function (e) {
        if (e.keyCode == 13) {  // detect the enter key
            var searchTerm = $(this).val();
            e.stopPropagation();
            e.preventDefault();
            searchAsset(searchTerm);
        }
    });

    $('#searchBtn').click(function (e) {
        var searchTerm = $('#searchTxt').val();
        e.stopPropagation();
        e.preventDefault();
        searchAsset(searchTerm);
    });

    function searchAsset(searchTerm) {
        if (checkNonSpecial(searchTerm)) {
            var searchSelect = $('#searchSelect').val();
            if (searchSelect !== "App") {
                searchTerm = searchSelect + ":" + "\"" + searchTerm + "\"";
            }
            var searchUrl = $('#searchUrl').val();
            if (searchUrl.indexOf('type=webapp') > -1) {
                location.href =
                location.protocol + '//' + location.host + searchUrl + '&query=' + searchTerm;
            } else if (searchUrl.indexOf('type=site') > -1) {
                location.href =
                location.protocol + '//' + location.host + searchUrl + '&query=' + searchTerm;
            } else {
                location.href =
                location.protocol + '//' + location.host + searchUrl + '?query=' + searchTerm;
            }
        }

    }

    function checkNonSpecial(value) {
        var non_special_regex = /^[A-Za-z][A-Za-z0-9\s-]*$/;
        return non_special_regex.test(value);
    }

});