var resource = (function () {
    var SubscriptionService;
    var subsApi;
    var log = new Log('add-favourite-app');

    SubscriptionService = require('/extensions/assets/webapp/services/subscription.js').serviceModule;
    subsApi = new SubscriptionService.SubscriptionService();
    subsApi.init(jagg, session);

    var AuthService = require('/extensions/assets/webapp/services/authentication.js').serviceModule;
    var authenticator = new AuthService.Authenticator();
    authenticator.init(jagg, session);

    var addToFavouriteApps = function (context) {
        if(authenticator.getLoggedInUser() == null){
            context.response.status = 401;
            return;
        }

        var parameters = context.request.getAllParameters();
        var data = {};
        data['apiName'] = parameters.apiName;
        data['apiVersion'] = parameters.apiVersion;
        data['apiProvider'] = parameters.apiProvider;

        var result = subsApi.addToFavouriteApps(data);
        return result;
    };


    return{
        post: addToFavouriteApps
    }

})();