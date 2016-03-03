var name;
var log = new Log();
var that = this;
var hps = require('/themes/store/helpers/assets.js');

for (name in hps) {
    if (hps.hasOwnProperty(name)) {
        that[name] = hps[name];
    }
}

var fn = that.resources;


var resources = function (page, meta) {
    return {
        js: ['logic/myapp/myapp.js']
    };
};

var cp = that.currentPage;

var currentPage = function (assetsx, ssox, userx, config, leftNav, rightNav, urlQuery,user) {
    var c = cp(assetsx, ssox, userx);
    c.config = config;
    if(user){
        c.user = true;
    }

    if (leftNav) {
        c.leftNav = leftNav;
    }
    if (rightNav) {
        c.rightNav = rightNav;
    }
    if (urlQuery) {
        c.urlQuery = urlQuery;
    }
    return c;
};

