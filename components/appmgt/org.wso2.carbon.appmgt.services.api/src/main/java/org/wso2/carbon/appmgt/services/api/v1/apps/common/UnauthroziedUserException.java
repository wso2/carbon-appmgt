package org.wso2.carbon.appmgt.services.api.v1.apps.common;

/**
 * Created by dilan on 3/23/15.
 */
class UnauthorizedUserException extends Exception  {

    @Override
    public String toString() {
        return "Unauthorized User";
    }
}
