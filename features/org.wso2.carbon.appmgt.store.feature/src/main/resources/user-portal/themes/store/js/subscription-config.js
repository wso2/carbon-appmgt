var manager = jagg.module("manager");
var subscriptionConfig = manager.getSubscriptionConfiguration();

var site = require("/config/store.json");
var isSelfSubEnabled = subscriptionConfig.EnableSelfSubscription;
var isEnterpriseSubEnabled = subscriptionConfig.EnableEnterpriseSubscription;

var log = new Log("subscription-config.js");

var isSelfSubscriptionEnabled = function () {
    return isSelfSubEnabled;
};

var isEnterpriseSubscriptionEnabled = function () {
    return isEnterpriseSubEnabled;
};

var isMyFavouriteMenu = function () {
    if (!isSelfSubEnabled && !isEnterpriseSubEnabled) {
        return true;
    }
    return false;
};

var getAnonymousApps = function (fn, request, session, tenantId) {
    var result = [];
    var managers = require('/modules/store.js').storeManagers(request, session, tenantId);
    var rxtManager = managers.rxtManager;
    var artifactManager = rxtManager.getArtifactManager(type);
    result = artifactManager.find(fn, null);
    return result;
};



