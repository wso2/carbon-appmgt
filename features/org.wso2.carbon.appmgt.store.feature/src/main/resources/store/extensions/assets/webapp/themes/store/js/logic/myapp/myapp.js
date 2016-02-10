$(function () {

    var API_ADD_TO_FAVOURITE = caramel.context + '/resources/webapp/v1/add-favourite-app/app';
    var API_REMOVE_FROM_FAVOURITE = caramel.context + '/resources/webapp/v1/remove-favourite-app/app';
    /**
     * This jquery function will be triggered when user
     * click on the icon- add to favourite app
     */
    $('.add-to-fav').on('click', function (e) {
        //Obtain the required information
        var appData = {};
        var appId = $(this).attr('id').replace("add-", "");
        appData['apiName'] = $(this).data("name");
        appData['apiVersion'] = $(this).data("version");
        appData['apiProvider'] = $(this).data("provider");
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
        appData['apiName'] = $(this).data("name");
        appData['apiVersion'] = $(this).data("version");
        appData['apiProvider'] = $(this).data("provider");
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
                           var message = 'You have successfully added  <b>' + data.apiName +
                                         '</b> to your favourite apps';
                           notify(message);
                       } else {
                           $(waitIconId).hide();
                           $(addIconId).show();
                           var message = 'Error occured in while adding  web app: ' + data.apiName +
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
                           var message = 'Error occured in while adding  web app: ' + data.apiName +
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
                           var message = 'You have successfully removed  <b>' + data.apiName
                               + '</b> from your favourite apps';
                           notify(message);
                           $('#btnRemoveFromFav').hide();
                           $('#btnAddToFav').show();
                       } else {
                           $(waitIconId).hide();
                           $(rmvIconId).show();
                           var message = 'Error occured  when remove  web app: ' + data.apiName
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
                           var message = 'Error occured  when remove  web app: ' + data.apiName
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
                 'modal': true,
                 'onClose': function () {
                     location.reload();
                 }
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
});