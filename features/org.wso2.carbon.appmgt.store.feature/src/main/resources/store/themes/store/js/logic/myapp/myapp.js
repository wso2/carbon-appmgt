$( document ).ready(function() {

    var API_ADD_TO_FAVOURITE = caramel.context + '/apis/favourite/add-favourite-app';
    var API_REMOVE_FROM_FAVOURITE = caramel.context + '/apis/favourite/remove-favourite-app';
    var API_UNSUBSCRIPTION_URL = caramel.context + '/resources/webapp/v1/unsubscription/app';

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
        "pageName": "My_App"
    };
    //Setup event publishing time.
    if(loggedInUserName) {
        initializeUserActivity("init", jsonObj);
    }

    $('.accessUrl').on('click', function (event) {
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
     * click on the icon- add to favourite app
     */
    $('.add-to-fav').on('click', function (e) {
        //Obtain the required information
        var appData = {};
        var appId = $(this).attr('id').replace("add-", "");
        appData['name'] = $(this).data("name");
        appData['version'] = $(this).data("version");
        appData['provider'] = $(this).data("provider");
        appData['storeTenantDomain'] = storeTenantDomain;

        $(this).hide();
        var waitIconId = '#wait-' + appId;
        $(waitIconId).show();

        addToFavourite(appData, appId);
        disabledEventPropagation(e);
    });

    /**
     * This jquery function will be triggered when user
     * click on the icon- remove from favourite app
     */
    $('.remove-from-fav').on('click', function (e) {
        var appData = {};
        var appId = $(this).attr('id').replace("rmv-", "");
        appData['name'] = $(this).data("name");
        appData['version'] = $(this).data("version");
        appData['provider'] = $(this).data("provider");
        appData['storeTenantDomain'] = storeTenantDomain;

        $(this).hide();
        var waitIconId = '#wait-' + appId;
        $(waitIconId).show();

        removeFromFavourite(appData, appId);
        disabledEventPropagation(e);
    });

    var addToFavourite = function (data, appId) {
        var waitIconId = '#wait-' + appId;
        var rmvIconId = '#rmv-' + appId;
        var addIconId = '#add-' + appId;
        $.ajax({
                   url: API_ADD_TO_FAVOURITE,
                   dataType: 'JSON',
                   type: 'POST',
                   data: data,
                   success: function (response, textStatus, xhr) {
                       if (response.error == false) {
                           $(waitIconId).hide();
                           $(rmvIconId).show();
                           var message = 'You have successfully added  <b>' + data.name +
                                         '</b> to your favourite apps';
                           notify(message);
                       } else {
                           $(waitIconId).hide();
                           $(addIconId).show();
                           var message = 'Error occured in while adding  web app: ' + data.name +
                                         ' to my favourite web apps';
                           notify(message);

                       }
                   },
                   error: function (response) {
                       $(waitIconId).hide();
                       $(addIconId).show();
                       if (response.status == 401) {
                           var message = 'Your session has time out.Please login again';
                           notify(message);
                       } else {
                           var message = 'Error occured in while adding  web app: ' + data.name +
                                         ' to my favourite web apps';
                           notify(message);
                       }
                   }
               });
    };

    var removeFromFavourite = function (data, appId) {
        var waitIconId = '#wait-' + appId;
        var rmvIconId = '#rmv-' + appId;
        var addIconId = '#add-' + appId;

        $.ajax({
                   url: API_REMOVE_FROM_FAVOURITE,
                   type: 'POST',
                   data: data,
                   success: function (response, textStatus, xhr) {
                       if (response.error == false) {
                           $(waitIconId).hide();
                           $(addIconId).show();
                           var message = 'You have successfully removed  <b>' + data.name
                               + '</b> from your favourite apps';
                           notify(message);
                           $('#btnRemoveFromFav').hide();
                           $('#btnAddToFav').show();
                       } else {
                           $(waitIconId).hide();
                           $(rmvIconId).show();
                           var message = 'Error occured  when remove  web app: ' + data.name
                               + ' from my favourite web apps';
                           notify(message);
                       }
                   },
                   error: function (response) {
                       $(waitIconId).hide();
                       $(rmvIconId).show();
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

    var notify = function (message) {
        noty({
                 text: message,
                 'layout': 'center',
                 'timeout': 1500,
                 'modal': true
             });
    };

    function disabledEventPropagation(event) {
        if (event.stopPropagation) {
            event.stopPropagation();
        }
        else if (window.event) {// ie
            window.event.cancelBubble = true;
        }
    }

    $('.btnUnsubscribe').on('click', function (e) {
        var subscription = {};
        subscription['apiName'] = $(this).data("name");
        subscription['apiVersion'] = $(this).data("version");
        subscription['apiProvider'] = $(this).data("provider");
        subscription['apiTier'] = "Unlimited";
        subscription['appName'] = "DefaultApplication";

        unsubscribeToApp(subscription);
    });


    var unsubscribeToApp = function (subscription) {
        $.ajax({
                   url: API_UNSUBSCRIPTION_URL,
                   type: 'POST',
                   data: subscription,
                   success: function (response) {
                       if (response.error == false) {
                           noty({
                                    text: 'You have successfully unsubscribed from the <b>' + subscription.apiName
                                          + '</b>',
                                    'layout': 'center',
                                    'timeout': 1500,
                                    'modal': true,
                                    'onClose': function () {
                                        location.reload();
                                    }
                                });
                       } else {
                           alert('Error occured in unsubscribe to web app: ' + subscription.apiName);
                       }
                   },
                   error: function (response) {
                       alert('Error occured in unsubscribe');
                   }
               });
    };

    // My Apps the tour
    var myAppsTour = new Tour({
        storage: false,
        steps: [
            {
                element: "#all-apps-link",
                title: "Checkout new applications",
                placement: "right",
                content: "You can go to App Store from this link, search and subscribe to new applications"
            },
            {
                element: "#my-apps-link",
                title: "Select apps as Favourite",
                placement: "right",
                content: "You can mark or unmark your favourite apps in My Apps page"
            },
            {
                element: "#my-favorites-link",
                title: "Add to your favourites",
                placement: "right",
                content: "The subscribed application can be marked as your Favourites and make it your default home page."
            }
        ]
    });

    var myAppsSearchTour = new Tour({

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
        myAppsTour.init(true);
        myAppsTour.start(false);
    });

    $('#no-apps-search-jumbotron').on('click', function(e){
        myAppsSearchTour.init(true);
        myAppsSearchTour.start(false);
    });
});
