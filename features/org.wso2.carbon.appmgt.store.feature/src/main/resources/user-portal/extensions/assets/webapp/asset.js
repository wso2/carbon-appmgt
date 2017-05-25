var assetLinks = function (user) {

    return{
        title: 'Applications',
        links: [
            {
                title: 'Custom',
                url: 'custom',
                path: 'custom.jag'
            } ,
            {
                title: 'My Apps',
                url: 'myapps',
                path: 'myapps.jag'
            },
            {
                title: 'All Apps',
                url: 'apps',
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
                title:'Custom',
                url:'custom',
                path:'custom.jag'
            } ,
            {
                title:'Subscriptions',
                url:'subscriptions',
                path:'subscriptions.jag'
            },
            {
                title:'Documentations',
                url:'doc',
                path:'doc.jag'
            },
            {
                title:'Favourite Apps',
                url:'favouriteapps',
                path:'favouriteapps.jag'
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
