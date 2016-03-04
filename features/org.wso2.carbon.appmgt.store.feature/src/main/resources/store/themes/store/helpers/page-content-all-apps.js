var currentPage = function (assetsx, ssox, userx, config, leftNav, rightNav, urlQuery,user) {
    var result  = {
        'assets': assetsx,
        'sso': ssox,
        'user': userx
    };

    result.config = config;
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
