$(function () {
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
        if (!checkIllegalChars(searchTerm)) {
            var searchSelect = $("#searchPane input[type='radio'][name='optionsRadios']:checked").val();
            searchTerm = searchSelect + ":" + "\"" + searchTerm + "\"";
            var searchUrl = $('#searchUrl').val();

            location.href =
            location.protocol + '//' + location.host + searchUrl + '?query=' + searchTerm;
        } else {
            alert("Search value  contains one or more illegal characters : [~!@#;%^*()+={}|\\<>\"\',]");
        }
    }

    function checkIllegalChars(value) {
        // registry doesn't allow following illegal charecters
        var special_regex = /[~!@#;%^*()+={}|\\<>"',]/;
        return special_regex.test(value);
    }


    $('html').click(function () {
        if ($('#search-dropdown-cont').is(':visible')) {
            $('#search-dropdown-cont').hide();
            var icon = $('#search-dropdown-arrow').find('i'), cls = icon.attr('class');
            icon.removeClass().addClass(cls == 'icon-sort-down' ? 'icon-sort-up' : 'icon-sort-down');
        }

    });

    $('#category-select').on('change', function () {
        var optionVal = $("#searchPane input[type='radio'][name='optionsRadios']:checked").val();
        if (optionVal == "Category") {
            var selected = $(this).find("option:selected").val();
            $('#searchTxt').val(selected);
        }
    });

    $('#platform-select').on('change', function () {
        var optionVal = $("#searchPane input[type='radio'][name='optionsRadios']:checked").val();
        if (optionVal == "Platform") {
            var selected = $(this).find("option:selected").val();
            $('#searchTxt').val(selected);
        }
    });

    $("#searchPane input[type='radio'][name='optionsRadios']").on('change', function () {
        var optionVal = $(this).val();
        if (optionVal == "Category") {
            $('#searchTxt').val($('#category-select').val());
        } else if (optionVal == "Platform") {
            $('#searchTxt').val($('#platform-select').val());
        } else {
            $('#searchTxt').val("");
        }
    });

});