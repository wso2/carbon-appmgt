var resources = function (page, meta) {
    return {
        template: 'top-assets.hbs',
        js: ['asset-core.js', 'top-assets.js', 'jquery.event.mousestop.js', 'jquery.carouFredSel-6.2.1-packed.js' ],
        css: ['assets.css', 'top-assets.css', 'mobileapp-custom.css']
    };
};

var currentPage = function (items,sso,user,config) {
    var noAssets = true;

    for (var i = 0; i < items.assets.length; i++){
        if(items.assets[0].assets.length > 0 || items.assets[1].assets.length > 0){
            noAssets = false;
        }
    }
    var out  = {
        'assets':items.assets,
        'noAssets':noAssets,
        'popularAssets': items.popularAssets,
        'sso': sso,
        'user': user,
        'config':config
    };
    return out;
};
