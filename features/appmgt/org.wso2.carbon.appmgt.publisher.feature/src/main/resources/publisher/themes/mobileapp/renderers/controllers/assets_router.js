/*
 Description: Renders the assets.jag view
 Filename:assets.js
 Created Date: 29/7/2013
 */

var permissions = require('/modules/permissions.js').permissions;
var config = require('/config/publisher.json');
var server = require('store').server;
var lcModule = require('/modules/comment.js');
var user = server.current(session);
var um = server.userManager(user.tenantId);
var publisher = require('/modules/publisher.js').publisher(request, session);
var rxtManager = publisher.rxtManager;

var render = function (theme, data, meta, require) {

    var publishActionAuthorized = permissions.isAuthorized(user.username,
                                                           config.permissions.mobileapp_publish,
                                                           um);
    var createMobileAppAuthorized = permissions.isAuthorized(user.username,
                                                             config.permissions.mobileapp_create,
                                                             um);
    // indicates whether this user has publisher permissions or not
    data.isPublisher = publishActionAuthorized;

    if (data.artifacts) {
        var mobileNotifications = [];
        var artifactManager = rxtManager.getArtifactManager('mobileapp');
        var artifacts = data.artifacts;
        for (var i = 0; i < artifacts.length; i++) {
            var artifact = artifacts[i];
            // indicates whether this user can download or not this artifact
            artifact.isDownloadable = publishActionAuthorized;
            // handle asset based notification
            if (artifact.lifecycleState == "Rejected") {
                // collect notifications
                var notifyObject = null;
                var lcComments = lcModule.getlatestLCComment(artifactManager, artifact.path);
                for (var key in lcComments) {
                    if (lcComments.hasOwnProperty(key)) {
                        notifyObject = {
                            'url': '/publisher/asset/operations/edit/mobileapp/' + artifact.id,
                            'notification': lcComments[key],
                            'appname': artifact.attributes.overview_displayName
                        }
                    }
                }
                mobileNotifications.push(notifyObject);
            }
        }

        // push notification to the user session
        session.put('mobileNotifications', mobileNotifications);
        session.put('mobileNotificationCount', mobileNotifications.length);
    }

    //Determine what view to show
    var listPartial = 'list-assets';
    switch (data.op) {
        case 'list':
            listPartial = 'list-assets';
            data = require('/helpers/view-asset.js').format(data);
            break;
        case 'statistics':
            listPartial = 'statistics';
            break;
        default:
            break;
    }

    theme('single-col-fluid', {
        title: data.title,
        header: [
            {
                partial: 'header',
                context: data
            }
        ],
        ribbon: [
            {
                partial: 'ribbon',
                context: {
                    active: listPartial,
                    createMobileAppPerm: createMobileAppAuthorized,
                    mobileNotifications: session.get('mobileNotifications'),
                    mobileNotificationCount: session.get('mobileNotificationCount')
                }
            }
        ],
        leftnav: [
            {
                partial: 'left-nav',
                context: require('/helpers/left-nav.js').generateLeftNavJson(data, listPartial)
            }
        ],
        listassets: [
            {
                partial: listPartial,
                context: data
            }
        ]
    });
};
