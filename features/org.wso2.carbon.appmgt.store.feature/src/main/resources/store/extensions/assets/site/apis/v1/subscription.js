var resource = (function () {

    var log = new Log('subscription-api');

    var SubscriptionService;
    var subsApi;

    SubscriptionService = require('/extensions/assets/site/services/subscription.js').serviceModule;
    subsApi = new SubscriptionService.SubscriptionService();
    subsApi.init(jagg, session);

    var AuthService = require('/extensions/assets/site/services/authentication.js').serviceModule;
    var authenticator = new AuthService.Authenticator();
    authenticator.init(jagg, session);

    /*
     Subscribes the given application to an app with the provided details
     */
    var addSubscription = function (context) {

        if (authenticator.getLoggedInUser() == null) {
            response.sendRedirect(caramel.tenantedUrl('/login'));
            return;
        }

        var parameters = context.request.getAllParameters();
        var subscription = {};
        subscription['apiName'] = parameters.apiName;
        subscription['apiVersion'] = parameters.apiVersion;
        subscription['apiTier'] = parameters.apiTier;
        subscription['apiProvider'] = parameters.apiProvider;
        subscription['appName'] = parameters.appName;
        subscription['subscriptionType'] = parameters.subscriptionType;
        subscription['enterprises'] = '';

        if (subscription['subscriptionType'] == 'ENTERPRISE') {
            subscription['enterprises'] = parameters.enterprises;
        }

        subscription['user'] = authenticator.getLoggedInUser().username;
        var result = subsApi.addSubscription(subscription);

        if (result) {
            subscription['op_type'] = 'ALLOW';
            result = subsApi.updateVisibility(subscription);
        }
        return result;
    };

    /*
     Returns all of the webapps to which the provided app is subscribed to
     */
    var getSubscription = function (context) {

        var uriMatcher = new URIMatcher(request.getRequestURI());
        var URI = '/{context}/resources/{asset}/{version}/{resource}/{username}';
        var isMatch = uriMatcher.match(URI);

        if (isMatch) {
            var userName = uriMatcher.elements().username;
            var applications = subsApi.getAppsWithSubs({user: userName});
            return (applications && applications.length) ? applications[0].subscriptions : null;
        } else {
            return null;
        }
    };

    return {
        post: addSubscription,
        get: getSubscription
    }

})();