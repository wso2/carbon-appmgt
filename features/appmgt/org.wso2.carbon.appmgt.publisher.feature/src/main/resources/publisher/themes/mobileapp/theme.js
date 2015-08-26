/**
 * Returns Caramel engine for this theme.
 */
var engine = caramel.engine('handlebars', (function () {
    return {
        partials: function (Handlebars) {
            var customHelpersModule = require('/themes/mobileapp/js/lib/handlebars/custom.handlebars.helpers.js');
            customHelpersModule.registerHelpers(Handlebars);

            var theme = caramel.theme();
            var partials = function (file) {
                (function register(prefix, file) {
                    var i, length, name, files;
                    if (file.isDirectory()) {
                        files = file.listFiles();
                        length = files.length;
                        for (i = 0; i < length; i++) {
                            file = files[i];
                            register(prefix ? prefix + '.' + file.getName() : file.getName(), file);
                        }
                    } else {
                        name = file.getName();
                        if (name.substring(name.length - 4) !== '.hbs') {
                            return;
                        }
                        file.open('r');
                        Handlebars.registerPartial(prefix.substring(0, prefix.length - 4),
                                                   file.readAll());
                        file.close();
                    }
                })('', file);
            };
            partials(new File(theme.resolve('partials')));
        }
    };
}()));