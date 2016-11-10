
var render = function (theme, data, meta, require) {

    var log = new Log();

    theme('2-column-left', {
        title: data.title,
        header: [
            {
                partial: 'header',
                context: data.header
            }
        ],
        pageHeader: [
            {
                partial: 'page-header',
                context: {
                    title: "Document",
                    sorting: null
                }
            }
        ],
        pageContent: [
            {
                partial:'doc',
                context:data
            }
        ]
    });

}