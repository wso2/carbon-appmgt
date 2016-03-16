var resource = (function () {

    var SubscriptionService;
    var subsApi;

    var log = new Log('unsubscription-api');
    var audutLog = require('/modules/auditLog/logger.js');

    SubscriptionService = require('/extensions/assets/webapp/services/subscription.js').serviceModule;
    subsApi = new SubscriptionService.SubscriptionService();

	subsApi.init(jagg, session);

    AuthService = require('/extensions/assets/webapp/services/authentication.js').serviceModule;
    authenticator = new AuthService.Authenticator();
    authenticator.init(jagg, session);

    

     
    var removeSubscription = function (context) {

        var parameters = context.request.getAllParameters();
        var subscription = {};
        var tenantId = session.get("tenantId");
        var userName = session.get("LOGGED_IN_USER");
        subscription['apiName'] = parameters.apiName;
        subscription['apiVersion'] = parameters.apiVersion;
        subscription['apiTier'] = parameters.apiTier;
        subscription['apiProvider'] = parameters.apiProvider;
        subscription['appName'] = parameters.appName;
        subscription['user'] = authenticator.getLoggedInUser().username;

        log.info('Trying to add a subscription');

        var result = subsApi.removeSubscription(subscription);
        if(result){
            subscription['op_type'] = 'DENY';
            result = subsApi.updateVisibility(subscription);
            audutLog.writeLog(tenantId, userName, "UserUnSubscribed", "Webapp", "{" +
                                                                                "providerName='"
                                                                                + subscription['apiProvider'] + '\'' +
                                                                                ", apiName='" + subscription['apiName']
                                                                                + '\'' +
                                                                                ", version='"
                                                                                + subscription['apiVersion'] + '\'' +
                                                                                '}', "", "");
        }

        return result;
    };
    
  

  

    return{
        post: removeSubscription
       
        
      
    }

})();