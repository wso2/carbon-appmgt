var name,
    log = new Log(),
    that = this,
    hps = require('/themes/store/helpers/assets.js');

/**
 * This is to inherit all variables in the default helper
 */
for (name in hps) {
    if (hps.hasOwnProperty(name)) {
        that[name] = hps[name];
    }
}

var resources=function(page,meta){
    return{
        js:['devices.js','assets.js']
    };
};

var cp = that.currentPage;

var currentPage = function (assetsx,ssox,userx, paging,config) {
    var c = cp(assetsx,ssox,userx);
    c.config = config;
    c.leftNav = paging.leftNav;
    c.rightNav = paging.rightNav;
    c.urlQuery = paging.urlQuery;
    return c;
};
