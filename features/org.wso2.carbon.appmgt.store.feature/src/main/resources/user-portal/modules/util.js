var getRXTAttributes = function (tenantId, type) {

    var log=new Log('util');
   // var config = require('/config/publisher.json');
    var store=require('/modules/store.js').storeManagers(request,session, tenantId);

    //var modelManager = application.get(config.app.MODEL_MANAGER);
    var modelManager=store.modelManager;
    var model = modelManager.getModel(type);
    return model.export('formo');
};

var getCategories = function (tenantId, type) {
    //var config = require('/config/publisher.json');
    var store=require('/modules/store.js').storeManagers(request,session, tenantId);
    //var modelManager = application.get(config.app.MODEL_MANAGER);
    var modelManager=store.modelManager;
    var model = modelManager.getModel(type);
    var fieldArr = model.export('form')['fields'];

    for (var i in fieldArr) {
        if (fieldArr.hasOwnProperty(i)) {
            if (fieldArr[i].name == 'overview_category')
                return fieldArr[i].valueList;
        }
    }
};

var resolveUserName = function(user) {
    var userName = null;
    if (user != null) {
        userName = user.username;
        if (userName != null) {
            if (userName.indexOf("@") > -1) {
                userName = userName.substring(0, userName.lastIndexOf("@"));
            }
            if (userName.indexOf("/") > -1) {
                userName = userName.substring(userName.lastIndexOf("/") + 1);
            }
        }
    }
    return userName;
};

