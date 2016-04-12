$(function () {
    var API_REMOVE_FROM_FAVOURITE = caramel.context + '/apis/favourite/remove-favourite-app';
    var API_SET_AS_HOME_PAGE = caramel.context + '/apis/favourite/set-favourite-page';
    var API_REMOVE_FROM_HOME_PAGE = caramel.context + '/apis/favourite/remove-favourite-page';

    var storeTenantDomain = $('#store-tenant-domain').val();
    var loggedInUserName = $('#user-name').val();
    var loggedInUserTenantId = $('#user-tenant-id').val();

    var jsonObj = {
        "isActive": "1",
        "path": caramel.context + "/apis/eventpublish/",
        "appData": {
            "appId": 'init',
            "userId": 'init',
            "tenantId": -1234,
            "appName": 'init',
            "appVersion": '1',
            "context": '/init'
        },
        "appControls": {"0": "a"},
        "publishedPeriod": "1200000",
        "pageName": "My_Favourite"
    };
    //Setup event publishing time.
    if(loggedInUserName) {
        initializeUserActivity("init", jsonObj);
    }

    $("a[data-stats='usage']").on('click', function (event) {
        if(loggedInUserName) {
            jsonObj.appData.appId = $(this).data("id");
            jsonObj.appData.appName = $(this).data("name");
            jsonObj.appData.appVersion = $(this).data("version");
            jsonObj.appData.context = $(this).data("context");
            jsonObj.appData.userId = loggedInUserName;
            jsonObj.appData.tenantId = loggedInUserTenantId;
            //only tag -"page-load"  is currently filtered in backend
            //so even though this is a click event add the tag as 'page-load'
            initializeUserActivity("page-load", jsonObj);
        }
    });
    /**
     * This jquery function will be triggered when user
     * click on the icon- remove from favourite app
     */
    $('.remove-from-fav').on('click', function (event) {
        var appData = {};
        appData['name'] = $(this).data("name");
        appData['version'] = $(this).data("version");
        appData['provider'] = $(this).data("provider");
        appData['storeTenantDomain'] = storeTenantDomain;

        removeFromFavourite(appData);
        disabledEventPropagation(event);
    });

    /**
     This jquery function will be triggered when user
     * click on the icon - Set this page as home page
     */
    $('#set-home').on('click', function (event) {
        var data = {};
        data['storeTenantDomain'] = storeTenantDomain;
        setAsHomePage(data);
    });

    /**
     This jquery function will be triggered when user
     * click on the icon -remove this page frin home page
     */
    $('#rmv-home').on('click', function (event) {
        var data = {};
        data['storeTenantDomain'] = storeTenantDomain;
        removeFromHomePage(data);
    });

    function disabledEventPropagation(event) {
        if (event.stopPropagation) {
            event.stopPropagation();
        }
        else if (window.event) {// ie
            window.event.cancelBubble = true;
        }
    }

    var removeFromFavourite = function (data) {
        $.ajax({
                   url: API_REMOVE_FROM_FAVOURITE,
                   type: 'POST',
                   data: data,
                   success: function (response, textStatus, xhr) {
                       if (response.error == false) {
                           var message = 'You have successfully removed  <b>' + data.name
                               + '</b> from your favourite apps';
                           //To remove the app from favourite page , need to reload the page
                           notifyAndReload(message);
                       } else {
                           var message = 'Error occured  when remove  web app: ' + data.name
                               + ' from my favourite web apps';
                           notify(message)
                       }
                   },
                   error: function (response) {
                       if (response.status == 401) {
                           var message = 'Your session has time out.Please login again';
                           notify(message);
                       } else {
                           var message = 'Error occured  when remove  web app: ' + data.name
                               + ' from my favourite web apps';
                           notify(message);
                       }
                   }
               });
    };

    var removeFromHomePage = function (data) {
        $.ajax({
                   url: API_REMOVE_FROM_HOME_PAGE,
                   type: 'POST',
                   data: data,
                   success: function (response, textStatus, xhr) {
                       if (response.error == false) {
                           var message = 'You have successfully removed   <b> MyFavourites Page'
                               + '</b> from home page';
                           notify(message);
                           $('#set-home').show();
                           $('#rmv-home').hide();

                       } else {
                           var message = 'Error occured  when removing "MyFavourites" Page' +
                                         ' from Home page';
                           notify(message);
                       }
                   },
                   error: function (response) {
                       if (response.status == 401) {
                           var message = 'Your session has time out.Please login again';
                           notify(message);
                       } else {
                           var message = 'Error occured  when removing "MyFavourites" Page' +
                                         ' from Home page';
                           notify(message);
                       }
                   }
               });
    };

    var setAsHomePage = function (data) {
        $.ajax({
                   url: API_SET_AS_HOME_PAGE,
                   type: 'POST',
                   data: data,
                   success: function (response, textStatus, xhr) {
                       if (response.error == false) {
                           var message = 'You have successfully set   <b> MyFavourites Page'
                               + '</b> as home page';
                           notify(message);
                           $('#rmv-home').show();
                           $('#set-home').hide();

                       } else {
                           var message = 'Error occured  when setting "MyFavourites" Page' +
                                         ' as Home page';
                           notify(message);
                       }
                   },
                   error: function (response) {
                       if (response.status == 401) {
                           var message = 'Your session has time out.Please login again';
                           notify(message);
                       } else {
                           var message = 'Error occured  when setting "MyFavourites" Page' +
                                         ' as Home page';
                           notify(message);
                       }

                   }
               });
    };


    var notify = function (message) {
        noty({
                 text: message,
                 'layout': 'center',
                 'timeout': 1500,
                 'modal': true
             });
    };

    var notifyAndReload = function (message) {
        noty({
                 text: message,
                 'layout': 'center',
                 'timeout': 1500,
                 'modal': true,
                 'onClose': function () {
                     location.reload();
                 }
             });
    };

    // Instance the tour
    var favouritesTour = new Tour({
        storage: false,
        steps: [
            {
                element: "#my-apps-link",
                title: "Select apps as Favourite",
                placement: "right",
                content: "You can mark or unmark your favourite apps in My Apps page"
            },
            {
                element: "#set-home",
                title: "Set the default page",
                placement: "left",
                content: "My Favourites page can be set/reset as your default page with the configuration option here."
            },
            {
                element: "#all-apps-link",
                title: "Checkout new applications",
                placement: "right",
                content: "You can go to App Store from this link, search and subscribe to new applications"
            },
            {
                element: "#my-favorites-link",
                title: "Add to your favourites",
                placement: "right",
                content: "The subscribed application can be marked as your Favourites and make it your default home page."
            }
        ]
    });

    var favouritesSearchTour = new Tour({

        storage: false,
        steps: [
            {
                element: ".input-group",
                title: "Change the Search term",
                placement: "bottom",
                content: "Change the search term here"
            },
            {
                element: ".input-group-btn",
                title: "Change the query type",
                placement: "bottom",
                content: "You can change what you want to search by selecting one of \"App Name\" or \"the App Provider\""
            }
        ]
    });

    $('#no-apps-default-jumbotron').on('click', function(e){
        favouritesTour.init(true);
        favouritesTour.start(false);
    });

    $('#no-apps-search-jumbotron').on('click', function(e){
        favouritesSearchTour.init(true);
        favouritesSearchTour.start(false);
    });
});