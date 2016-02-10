var resource = (function () {
    var SubscriptionService;
    var subsApi;
    var log = new Log('set-favourite-page');

    SubscriptionService = require('/extensions/assets/webapp/services/subscription.js').serviceModule;
    subsApi = new SubscriptionService.SubscriptionService();
    subsApi.init(jagg, session);

    var AuthService = require('/extensions/assets/webapp/services/authentication.js').serviceModule;
    var authenticator = new AuthService.Authenticator();
    authenticator.init(jagg, session);

    var setFavouritePage = function (context) {
        if (authenticator.getLoggedInUser() == null) {
            context.response.status = 401;
            return;
        }

        var result = subsApi.setFavouritePage();
        return result;
    };

    return{
        post: setFavouritePage
    }
})();