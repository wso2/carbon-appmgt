var resource = (function () {

    var log = new Log('unsubscription-api');

    var SubscriptionService;
    var subsApi;

    SubscriptionService = require('/extensions/assets/site/services/subscription.js').serviceModule;
    subsApi = new SubscriptionService.SubscriptionService();
    subsApi.init(jagg, session);

    AuthService = require('/extensions/assets/site/services/authentication.js').serviceModule;
    authenticator = new AuthService.Authenticator();
    authenticator.init(jagg, session);

    var removeSubscription = function (context) {

        var parameters = context.request.getAllParameters();
        var subscription = {};
        subscription['apiName'] = parameters.apiName;
        subscription['apiVersion'] = parameters.apiVersion;
        subscription['apiTier'] = parameters.apiTier;
        subscription['apiProvider'] = parameters.apiProvider;
        subscription['appName'] = parameters.appName;
        subscription['user'] = authenticator.getLoggedInUser().username;

        var result = subsApi.removeSubscription(subscription);
        if (result) {
            subscription['op_type'] = 'DENY';
            result = subsApi.updateVisibility(subscription);
        }

        return result;
    };

    return {
        post: removeSubscription
    }

})();