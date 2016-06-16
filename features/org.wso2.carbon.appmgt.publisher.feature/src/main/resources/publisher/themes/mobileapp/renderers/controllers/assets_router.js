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
var caramel = require('caramel');

var render = function (theme, data, meta, require) {

    var publishActionAuthorized = permissions.isAuthorized(user.username,
                                                           config.permissions.mobileapp_publish,
                                                           um);
    var createMobileAppAuthorized = permissions.isAuthorized(user.username,
                                                             config.permissions.mobileapp_create,
                                                             um);

    var lifecycleColors = {"Create": "btn-green", "Recycle": "btn-blue", "Re-Publish": "btn-blue", "Submit for Review": "btn-blue", "Unpublish": "btn-orange", "Deprecate": "btn-danger", "Retire": "btn-danger", "Publish": "btn-blue", "Approve": "btn-blue", "Reject": "btn-orange"};

    appPublishWFExecutor = org.wso2.carbon.appmgt.impl.workflow.WorkflowExecutorFactory.getInstance().getWorkflowExecutor("AM_APPLICATION_PUBLISH");
    var isAsynchronousFlow = appPublishWFExecutor.isAsynchronus();

    // indicates whether this user has publisher permissions or not
    data.isPublisher = publishActionAuthorized;

    if (data.artifacts) {
        var deleteButtonAvailability = false;
        var mobileNotifications = [];
        var artifactManager = rxtManager.getArtifactManager('mobileapp');
        var artifacts = data.artifacts;
        var pubActions = config.publisherActions;

        for (var i = 0; i < artifacts.length; i++) {
            var artifact = artifacts[i];
            // indicates whether this user can download or not this artifact
            artifact.isDownloadable = publishActionAuthorized;

            var lifecycleAvailableActionsButtons = new Array();
            if (permissions.isLCActionsPermitted(user.username, data.artifacts[i].path, um)) {
                if (data.artifacts[i].lifecycleAvailableActions) {

                    for (var j = 0; j < data.artifacts[i].lifecycleAvailableActions.length; j++) {
                        var name = data.artifacts[i].lifecycleAvailableActions[j];


                        for (var k = 0; k < data.roles.length; k++) {

                            var skipFlag = false;

                            if (pubActions.indexOf(String(name)) > -1) {
                                if (!publishActionAuthorized) {
                                    skipFlag = true;
                                }
                            }

                            // To Send an app to submit for review, created permission is required.
                            if (createMobileAppAuthorized) {
                                if (name == "Submit for Review") {
                                    lifecycleAvailableActionsButtons.push({name: name, style: lifecycleColors[name]});
                                    if (skipFlag) {
                                        break;
                                    }
                                }
                            }

                            if (!skipFlag) {
                                if (name == "Publish") {
                                    lifecycleAvailableActionsButtons.push({name: name, style: lifecycleColors[name]});
                                }
                                if (name == "Reject" && isAsynchronousFlow) {
                                    lifecycleAvailableActionsButtons.push({name: name, style: lifecycleColors[name]});
                                }
                                if (name == "Recycle") {
                                    lifecycleAvailableActionsButtons.push({name: name, style: lifecycleColors[name]});
                                }
                                if (name == "Deprecate") {
                                    lifecycleAvailableActionsButtons.push({name: name, style: lifecycleColors[name]});
                                }
                                if (name == "Re-Publish") {
                                    lifecycleAvailableActionsButtons.push({name: name, style: lifecycleColors[name]});
                                }
                                if (name == "Unpublish") {
                                    lifecycleAvailableActionsButtons.push({name: name, style: lifecycleColors[name]});
                                }
                                if (name == "Depreicate") {
                                    lifecycleAvailableActionsButtons.push({name: name, style: lifecycleColors[name]});
                                }
                                if (name == "Retire") {
                                    lifecycleAvailableActionsButtons.push({name: name, style: lifecycleColors[name]});
                                }
                                if (name == "Approve" && isAsynchronousFlow) {
                                    lifecycleAvailableActionsButtons.push({name: name, style: lifecycleColors[name]});
                                }
                                break;
                            }
                        }
                    }

                }

            }

            data.artifacts[i].lifecycleAvailableActions = lifecycleAvailableActionsButtons;

           //Adding the delete button
            if (permissions.isDeletePermitted(user.username, data.artifacts[i].path, um)) {
                deleteButtonAvailability = true;
            }

            data.artifacts[i].deleteButtonAvailability = deleteButtonAvailability;


            // handle asset based notification
            if (artifact.lifecycleState == "Rejected") {
                // collect notifications
                var notifyObject = null;
                var lcComments = lcModule.getlatestLCComment(artifactManager, artifact.path);
                for (var key in lcComments) {
                    if (lcComments.hasOwnProperty(key)) {
                        notifyObject = {
                            'url': caramel.configs().context + '/asset/operations/edit/mobileapp/' + artifact.id,
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
