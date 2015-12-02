var assetLinks = function (user) {

    return {
        title: 'Sites',
        links: [
            {
                title: 'My Applications',
                url: 'myapps',
                path: 'myapps.jag'
            },
            {
                title: 'Login',
                url: 'login',
                path: 'login.jag'
            },
            {
                url: 'logout',
                path: 'logout.jag'
            },
            {
                title: 'Subscriptions',
                url: 'subscriptions',
                path: 'subscriptions.jag'
            },
            {
                title: 'Documentations',
                url: 'doc',
                path: 'doc.jag'
            }
        ]
    }
};

var assetManager = function (manager) {
    var add = manager.add;
    var log = new Log('asset');


    //Override the add actions of the API
    manager.add = function (options) {
        add.call(manager, options);
    };

    return manager;
};
