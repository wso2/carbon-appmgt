package org.wso2.carbon.appmgt.impl.dao;

/**
 * This class is dedicated to store SQL queries in DAO
 */
public class SQLConstants {
    public static final String GET_FAVOURITE_APPS =
            "SELECT " +
                    " APP.APP_PROVIDER AS APP_PROVIDER," +
                    " APP.APP_NAME AS APP_NAME," +
                    " APP.APP_VERSION AS APP_VERSION" +
                    " FROM APM_APP APP" +
                    " INNER JOIN APM_FAVOURITE_APPS FAV_APP" +
                    " ON  (APP.APP_ID =FAV_APP.APP_ID" +
                    " AND FAV_APP.USER_ID  = ?" +
                    " AND FAV_APP.TENANT_ID = ? )" +
                    " WHERE APP.TENANT_ID = ?";
    public static final String GET_FAVOURITE_APPS_SORT_BY_CREATED_TIME_DESC =
            GET_FAVOURITE_APPS + " ORDER BY FAV_APP.CREATED_TIME DESC";
    public static final String GET_FAVOURITE_APPS_SORT_BY_APP_NAME_ASC =
            GET_FAVOURITE_APPS + " ORDER BY APP.APP_NAME ASC";
    public static final String SEARCH_FAVOURITE_APPS =
            "SELECT " +
                    " APP.APP_PROVIDER AS APP_PROVIDER," +
                    " APP.APP_NAME AS APP_NAME," +
                    " APP.APP_VERSION AS APP_VERSION" +
                    " FROM APM_APP APP" +
                    " INNER JOIN APM_FAVOURITE_APPS FAV_APP" +
                    " ON  (APP.APP_ID =FAV_APP.APP_ID" +
                    " AND FAV_APP.USER_ID  = ?" +
                    " AND FAV_APP.TENANT_ID = ? )" +
                    " WHERE APP.TENANT_ID = ?";
    public static final String SEARCH_FAVOURITE_APPS_BY_APP_PROVIDER =
            SEARCH_FAVOURITE_APPS + " AND  APP.APP_PROVIDER LIKE ?";
    public static final String SEARCH_FAVOURITE_APPS_BY_APP_NAME =
            SEARCH_FAVOURITE_APPS + " AND  APP.APP_NAME LIKE ?";
    public static final String SEARCH_USER_ACCESSIBLE_APPS =
            "SELECT APP_NAME,APP_PROVIDER,APP_VERSION" +
                    " FROM APM_APP LEFT JOIN APM_SUBSCRIPTION ON APM_APP.APP_ID = APM_SUBSCRIPTION.APP_ID" +
                    " WHERE APM_APP.TREAT_AS_SITE = ? AND APM_APP.TENANT_ID = ?" +
                    " AND (APM_SUBSCRIPTION.APPLICATION_ID =? OR APM_APP.APP_ALLOW_ANONYMOUS= ?)";
    public static final String SEARCH_USER_ACCESSIBLE_APPS_BY_APP_PROVIDER =
            SEARCH_USER_ACCESSIBLE_APPS + " AND  APM_APP.APP_PROVIDER LIKE ?";
    public static final String SEARCH_USER_ACCESSIBLE_APPS_BY_APP_NAME =
            SEARCH_USER_ACCESSIBLE_APPS + " AND  APM_APP.APP_NAME LIKE ?";
    public static final String GET_USER_ACCESSIBlE_APPS =
            "SELECT APP_NAME,APP_PROVIDER,APP_VERSION" +
                    " FROM APM_APP LEFT JOIN APM_SUBSCRIPTION ON APM_APP.APP_ID = APM_SUBSCRIPTION.APP_ID" +
                    " WHERE APM_APP.TREAT_AS_SITE = ? AND APM_APP.TENANT_ID = ?" +
                    " AND (APM_SUBSCRIPTION.APPLICATION_ID =? OR APM_APP.APP_ALLOW_ANONYMOUS= ?)";
    public static final String GET_USER_ACCESSIBlE_APPS_ORDER_BY_SUBSCRIPTION_TIME = GET_USER_ACCESSIBlE_APPS +
            " ORDER BY APM_SUBSCRIPTION.SUBSCRIPTION_TIME DESC";
    public static final String GET_USER_ACCESSIBlE_APPS_ORDER_BY_APP_NAME = GET_USER_ACCESSIBlE_APPS +
            " ORDER BY APM_APP.APP_NAME ASC";


}
