$(function () {

    $("#overview_owner").on('click',function() {
        var names = "";
        names = $("#owner_list").val();
        if (names.length > 5) {
            names = names.substring(2, names.length - 2);
        }
        var ownerNames = names.split("/");
        $("#overview_owner").autocomplete({
                                           source: ownerNames
                                       });
    });
    $("#overview_ownerName").on('click',function() {
        var names = "";
        names = $("#owner_list").val();
        if (names.length > 5) {
            names = names.substring(2, names.length - 2);
        }
        var ownerNames = names.split("/");
        $("#overview_ownerName").autocomplete({
                                              source: ownerNames
                                          });
    });
});