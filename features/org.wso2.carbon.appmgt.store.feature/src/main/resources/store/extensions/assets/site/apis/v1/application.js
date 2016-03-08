var resource = (function () {

    var log = new Log('application-api');

    var addApplication = function (context) {
        if (log.isDebugEnabeld()) {
            log.debug('Adding an application');
        }
        var parameters = context.request.getAllParameters();
        var AppService = require('/extensions/assets/site/services/app.js').serviceModule;

        appApi = new AppService.AppService();
        appApi.init(jagg, session);
        AuthService = require('/extensions/assets/site/services/authentication.js').serviceModule;
        authenticator = new AuthService.Authenticator();
        authenticator.init(jagg, session);

        var result = appApi.addApplication({
            username: authenticator.getLoggedInUser().username,
            application: parameters.appName,
            tier: parameters.appTier,
            callbackUrl: parameters.appCallbackUrl,
            description: parameters.appDescription
        });

        return result;
    };

    var deleteApplication = function (context) {
        if (log.isDebugEnabeld()) {
            log.debug('Application delete');
        }
        var AppService = require('/extensions/assets/site/services/app.js').serviceModule;

        appApi = new AppService.AppService();
        appApi.init(jagg, session);
        AuthService = require('/extensions/assets/site/services/authentication.js').serviceModule;
        authenticator = new AuthService.Authenticator();
        authenticator.init(jagg, session);

        var uriMatcher = new URIMatcher(context.request.getRequestURI());
        var URI = '/{context}/resources/{asset}/{version}/application/{appName}';
        var isMatch = uriMatcher.match(URI);

        if (isMatch) {
            var appName = uriMatcher.elements().appName;
            appApi.deleteApplication({
                appName: appName,
                username: authenticator.getLoggedInUser().username
            });

            return {isRemoved: true};
        }

        return {isRemoved: false};
    };

    var updateApplication = function (context) {
        if (log.isDebugEnabeld()) {
            log.info('Entered update application');
        }
        var parameters = request.getContent();
        var AppService = require('/extensions/assets/site/services/app.js').serviceModule;

        appApi = new AppService.AppService();
        appApi.init(jagg, session);
        AuthService = require('/extensions/assets/site/services/authentication.js').serviceModule;
        authenticator = new AuthService.Authenticator();
        authenticator.init(jagg, session);

        var result = appApi.updateApplication({
            newAppName: parameters.newAppName,
            oldAppName: parameters.appName,
            username: authenticator.getLoggedInUser().username,
            tier: parameters.tier,
            callbackUrl: parameters.newCallbackUrl,
            description: parameters.newDescription
        });

        return result;
    };

    return {
        post: addApplication,
        delete: deleteApplication,
        put: updateApplication
    }

})();
