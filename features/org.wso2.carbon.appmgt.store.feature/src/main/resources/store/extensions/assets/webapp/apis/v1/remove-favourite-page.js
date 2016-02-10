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
        if (authenticator.getLoggedInUser() == null) {
            context.response.status = 401;
            return;
        }

        var result = subsApi.removeFavouritePage();
        return result;
    };
    return{
        post: removeFavouritePage
    }

})();