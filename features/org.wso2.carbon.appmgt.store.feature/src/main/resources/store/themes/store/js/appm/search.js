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
        if(isEmpty(searchTerm)) {
            return;
        }

        if (!checkIllegalChars(searchTerm)) {
            var searchSelect = $('#searchSelect').val();
            searchTerm = searchSelect + ":" + "\"" + searchTerm + "\"";
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
        } else {
            alert("Search value  contains one or more illegal characters : [~!@#;%^*()+={}|\\<>\"\',]");
        }

    }

    function checkIllegalChars(value) {
        // registry doesn't allow following illegal charecters
        var special_regex = /[~!@#;%^*()+={}|\\<>"',]/;
        return special_regex.test(value);
    }

    function isEmpty(value) {
        if (value && value.trim().length > 0) {
            return false
        }
        return true;
    }

});