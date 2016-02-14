var resource = (function () {

    var log = new Log('add-favourite-api');
    var SubscriptionService;
    var subsApi;
    SubscriptionService = require('/extensions/assets/webapp/services/subscription.js').serviceModule;
    subsApi = new SubscriptionService.SubscriptionService();
    subsApi.init(jagg, session);

    var AuthService = require('/extensions/assets/webapp/services/authentication.js').serviceModule;
    var authenticator = new AuthService.Authenticator();
    authenticator.init(jagg, session);

    var removeFavouritePage = function (context) {
        var user = authenticator.getLoggedInUser();
        if (user == null) {
            context.response.status = 401;
            return;
        }

        var parameters = context.request.getAllParameters();
        var storeTenantDomain = parameters.storeTenantDomain;

        var es = require('store'),
            tenant = es.server.tenant(context.request, session);
        var userName = user.username;
        var tenantIdOfUser = tenant.tenantId;
        var result = subsApi.removeFavouritePage(userName,tenantIdOfUser,storeTenantDomain);
        return result;
    };

    return{
        post: removeFavouritePage
    }
})();