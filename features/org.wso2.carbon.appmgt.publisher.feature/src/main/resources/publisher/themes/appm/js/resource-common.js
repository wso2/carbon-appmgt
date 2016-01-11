function updateVisuals() {
    var anonymous = false;

    $(".anonymous_checkbox").each(function (index) {
        if ($(this).is(':checked')) {
            anonymous = true;
        }
    });

    if(anonymous) {
        var control_visibility = $(".controll_visibility");
        control_visibility.prop('disabled', true);
        control_visibility.parent().parent().hide();
    } else {
        var control_visibility = $(".controll_visibility");
        control_visibility.prop('disabled', false);
        control_visibility.parent().parent().show();
    }
}

$( document ).ready(function() {

    /**
     * This will collect treat as a site values and push into hidden input filed.
     */
    $(".treatAsASite_checkbox").click(function () {
        var output = [];
        $(".treatAsASite_checkbox").each(function (index) {
            if ($(this).is(':checked')) {
                output.push("TRUE");
            }
            else {
                output.push("FALSE");
            }
        });
        $('#overview_treatAsASite').val(output);
    });

});
