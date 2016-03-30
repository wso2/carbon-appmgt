$(function () {

    $("#owner_input").on('click',function() {
        var names = "";
        names = $("#owner_list").val();
        if (names.length > 5) {
            names = names.substring(2, names.length - 2);
        }
        var ownerNames = names.split("/");
        $("#owner_input").autocomplete({
                                           source: ownerNames
                                       });
    });

});