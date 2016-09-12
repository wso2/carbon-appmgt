package org.wso2.carbon.appmgt.rest.api.util.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

public class CommonValidator {
    private static final Log log = LogFactory.getLog(CommonValidator.class);

    public static boolean isValidAppType(String appType) {
        if (AppMConstants.MOBILE_ASSET_TYPE.equalsIgnoreCase(appType) ||
                AppMConstants.WEBAPP_ASSET_TYPE.equalsIgnoreCase(appType) ||
                AppMConstants.SITE_ASSET_TYPE.equalsIgnoreCase(appType)) {
            return true;
        } else {
            throw RestApiUtil.buildBadRequestException("Invalid Asset Type : " + appType);
        }
    }
}
