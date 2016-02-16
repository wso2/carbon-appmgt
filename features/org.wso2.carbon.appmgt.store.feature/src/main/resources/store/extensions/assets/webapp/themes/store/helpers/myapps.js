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
    var o = fn(page, meta);
    o.js.push('logic/assets/lazy-load.js');
    o.js.push('logic/assets/assets.js');
    o.js.push('search.js');
    o.js.push('logic/login/login.js');
    o.js.push('logic/myapp/myapp.js');

    o.css.push('cstyles.css');
    return o;
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

