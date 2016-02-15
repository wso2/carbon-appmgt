$(function () {
    var API_REMOVE_FROM_FAVOURITE = caramel.context + '/apis/favourite/remove-favourite-app';
    var API_SET_AS_HOME_PAGE = caramel.context + '/apis/favourite/set-favourite-page';
    var API_REMOVE_FROM_HOME_PAGE = caramel.context + '/apis/favourite/remove-favourite-page';

    var storeTenantDomain = $('#store-tenant-domain').val();
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
});