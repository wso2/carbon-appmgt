var resource = (function () {

    var SubscriptionService;
    var subsApi;

    SubscriptionService = require('/extensions/assets/site/services/subscription.js').serviceModule;
    subsApi = new SubscriptionService.SubscriptionService();
    subsApi.init(jagg, session);

    AuthService = require('/extensions/assets/site/services/authentication.js').serviceModule;
    authenticator = new AuthService.Authenticator();
    authenticator.init(jagg, session);

    var checkSubscriptionWorkflow = function (context) {
        var result = subsApi.checkSubscriptionWorkflow();
        return result;
    };

    return {
        post: checkSubscriptionWorkflow
    }

})();
