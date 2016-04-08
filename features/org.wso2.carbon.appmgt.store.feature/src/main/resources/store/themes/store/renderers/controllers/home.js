var render = function (theme, data, meta, require) {
    data.tags.tagUrl = "/assets/";

    data.header.active = 'store';
    var enabledTypeList = data.config.enabledTypeList;
    //if only mobile app is enabled hide favourite link from navigation
    if( enabledTypeList.length == 1 &&  enabledTypeList[0] == "mobileapp") {
        data.header.hideFavouriteMenu = true;
    }

    var subscriptionOn = true;
    if (!data.config.isSelfSubscriptionEnabled && !data.config.isEnterpriseSubscriptionEnabled) {
        subscriptionOn = false;
    }

    theme('2-column-left', {
        title: data.title,
        header: [
            {
                partial: 'header',
                context: data.header
            }
        ],
        leftColumn: [
            {
                partial: 'left-column',
                context: {
                    navigation: createLeftNavLinks(data),
                    tags: data.tags,
                    assetType: data.assetType
                }
            }
        ],
        search: [
            {
                partial: 'search',
                context: {}
            }
        ],
        pageHeader: [
            {
                partial: 'page-header',
                context: {
                    title: "Recent Apps"
                }
            }
        ],
        pageContent: [
            {
                partial: 'page-content-home',
                context: getBodyContext(data, subscriptionOn)
            }
        ]
    });
};


function createLeftNavLinks(data) {
    var enabledTypeList = data.config.enabledTypeList;
    var leftNavigationData = [];
    var subscriptionOn = true;
    if (!data.config.isSelfSubscriptionEnabled && !data.config.isEnterpriseSubscriptionEnabled) {
        subscriptionOn = false;
    }
    for (var i = 0; i < enabledTypeList.length; i++) {

        if (subscriptionOn) {
            leftNavigationData.push({
                                        active: false, partial: enabledTypeList[i], url: "/assets/" +
                                                                                         enabledTypeList[i]
                                    });
        } else {
            if (enabledTypeList[i] == 'mobileapp') {
                leftNavigationData.push({
                                            active: false, partial: enabledTypeList[i], url: "/assets/" +
                                                                                             enabledTypeList[i]
                                        });
            } else {
                leftNavigationData.push({
                                            active: false, partial: enabledTypeList[i], url: "/extensions/assets/" +
                                                                                             enabledTypeList[i]
                        + "/apps"
                                        });
            }

        }

    }
    return leftNavigationData;
}

function getBodyContext(data, subscriptionOnStatus) {
    var assetTypes = data.topAssets.assets;
    var user = data.header.user;
    var enabledTypeList = data.config.enabledTypeList;
    for (var i = 0; i < assetTypes.length; i++) {
        assetTypes[i].user = user;
        var type = assetTypes[i].singular.toLowerCase().replace(/ /g,'');
        if (type == "mobileapp") {
            assetTypes[i].seeMoreUrl = "/assets/mobileapp/"
        } else {
            if (subscriptionOnStatus) {
                assetTypes[i].seeMoreUrl = "/assets/" + type + "/"
            } else {
                assetTypes[i].seeMoreUrl = "/extensions/assets/" + type + "/apps/"
            }
        }
    }
    return {assetTypes: assetTypes, subscriptionOn: subscriptionOnStatus};
}
