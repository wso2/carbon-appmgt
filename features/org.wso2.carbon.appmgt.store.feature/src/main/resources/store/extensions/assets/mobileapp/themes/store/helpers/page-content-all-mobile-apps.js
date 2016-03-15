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

var currentPage = function (assetsx,ssox,userx, paging,config, pageIndeces ,leftNav, rightNav) {
    var c = cp(assetsx,ssox,userx, paging, pageIndeces,leftNav,rightNav);
    c.config = config;
    var log = new Log();
    c.pageIndeces = pageIndeces;
    if(leftNav) {
        c.leftNav = leftNav;
    }
    if(rightNav) {
        c.rightNav = rightNav;
    }
    return c;
};
