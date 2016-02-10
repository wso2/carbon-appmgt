var render = function (theme, data, meta, require) {
    theme('1-column', {
        title: 'FavouriteApps',
        header: [
            {
                partial:'header',
                context:data.header
            }
        ] ,
        body:[
            {
                partial:'favourite-apps',
                context:{favouriteApps:data.favouriteApps}
            }
        ]
    });

};