var resources=function(page,meta){
    return{
        js:['logic/myapp/myapp.js', '/libs/bootstrap-tour-0.10.1/bootstrap-tour.min.js'],
        css: ['/libs/bootstrap-tour-0.10.1/bootstrap-tour.css']
    };
};

var currentPage = function (assetsx, ssox, userx, config, leftNav, rightNav, urlQuery,user,assetType) {
    var result  = {
        'assets': assetsx,
        'sso': ssox,
        'user': userx,
        'assetType': assetType,
        'config': config
    };

    if(user){
        result.user = true;
    }

    if (leftNav) {
        result.leftNav = leftNav;
    }
    if (rightNav) {
        result.rightNav = rightNav;
    }
    if (urlQuery) {
        result.urlQuery = urlQuery;
    }
    return result;
};

