/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.appmgt.usage.publisher;

import java.util.HashMap;
import java.util.Map; 

public final class APIMgtUsagePublisherConstants {
 
	public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String _OAUTH_HEADERS_SPLITTER = ",";
    public static final String _OAUTH_CONSUMER_KEY = "Bearer";
    public static final String HEADER_SEGMENT_DELIMITER = " ";
    public static final String  AXIS2_MC_HTTP_METHOD = "HTTP_METHOD";

    public static final String API_USAGE_TRACKING = "Analytics.";
    public static final String API_USAGE_ENABLED = API_USAGE_TRACKING + "Enabled";
    public static final String API_USAGE_THRIFT_PORT = API_USAGE_TRACKING + "ThriftPort";
    public static final String API_USAGE_DAS_SERVER_URL = API_USAGE_TRACKING + "DASServerURL";
    public static final String API_USAGE_DAS_SERVER_USER = API_USAGE_TRACKING + "DASUsername";
    public static final String API_USAGE_DAS_SERVER_PASSWORD = API_USAGE_TRACKING + "DASPassword";
    public static final String API_USAGE_PUBLISHER_CLASS = API_USAGE_TRACKING + "PublisherClass";

    public static final String CONSUMER_KEY = "api.ut.consumerKey";
    public static final String USER_ID = "api.ut.userId";
    public static final String CONTEXT = "api.ut.context";
    public static final String APP_VERSION = "api.ut.api_version";
    public static final String API = "api.ut.api";
    public static final String VERSION = "api.ut.version";
    public static final String REQUEST_TIME = "api.ut.requestTime";
    public static final String RESOURCE = "api.ut.resource";
    public static final String HTTP_METHOD = "api.ut.HTTP_METHOD";
    public static final String HOST_NAME = "api.ut.hostName";
    public static final String API_PUBLISHER = "api.ut.apiPublisher";
    public static final String APPLICATION_NAME = "api.ut.application.name";
    public static final String APPLICATION_ID = "api.ut.application.id";
    public static final String ANONYMOUS_USER = "anonymous.user";
    public static final String API_AUTH_CONTEXT = "__API_AUTH_CONTEXT";

	public static final String API_MANAGER_STREAM_NAME_DEFINITION = API_USAGE_TRACKING + "DASEventStreams.";
	public static final String API_MANAGER_REQUEST_STREAM_NAME = API_MANAGER_STREAM_NAME_DEFINITION
			+ "RequestStreamName";
	public static final String API_MANAGER_REQUEST_STREAM_VERSION = API_MANAGER_STREAM_NAME_DEFINITION
			+ "RequestStreamVersion";
	public static final String API_MANAGER_RESPONSE_STREAM_NAME = API_MANAGER_STREAM_NAME_DEFINITION
			+ "ResponseStreamName";
	public static final String API_MANAGER_RESPONSE_STREAM_VERSION = API_MANAGER_STREAM_NAME_DEFINITION
			+ "ResponseStreamVersion";
	public static final String API_MANAGER_FAULT_STREAM_NAME = API_MANAGER_STREAM_NAME_DEFINITION
			+ "FaultStreamName";
	public static final String API_MANAGER_FAULT_STREAM_VERSION = API_MANAGER_STREAM_NAME_DEFINITION
			+ "FaultStreamVersion";
	public static final String API_MANAGER_CACHE_STAT_STREAM_NAME = API_MANAGER_STREAM_NAME_DEFINITION
			+ "CacheStatStreamName";
	public static final String API_MANAGER_CACHE_STAT_VERSION = API_MANAGER_STREAM_NAME_DEFINITION
			+ "CacheStatStreamVersion";

	// To add UI Activity DAS publisher
	public static final String API_DAS_UI_ACTIVITY_STREAM = API_MANAGER_STREAM_NAME_DEFINITION
			+ "UIActivityStreamName";
	public static final String API_DAS_UI_ACTIVITY_STREAM_VERSION = API_MANAGER_STREAM_NAME_DEFINITION
			+ "UIActivityStreamVersion";

    public static final String API_GOOGLE_ANALYTICS_TRACKING = API_USAGE_TRACKING + "GoogleAnalyticsTracking.";
    public static final String API_GOOGLE_ANALYTICS_TRACKING_ENABLED = API_GOOGLE_ANALYTICS_TRACKING + "Enabled";
    public static final String API_GOOGLE_ANALYTICS_TRACKING_ID = API_GOOGLE_ANALYTICS_TRACKING + "TrackingID";

    public static final String TRACKING_CODE = "trackingCode";
    //public static final String LOGGEDUSER = "loggedUser";
    public static final String REFERER = "referer";
    public static final String RESPONSE_TIME = "responseTimeforPage";
    public static final String SERVICE_TIME_OF_PAGE = "serviceTimeOfPage";

    public static final String TRANSPORT_URL = "api.ut.transportURL";

    /** To add new ANALYTICS engines, create a static String and populate the ENGINE_TO_CLASS_MAP below **/
    public static final String ANALYTIC_ENGINE_DAS = "ANALYTIC_ENGINE_DAS";
    public static final String ANALYTIC_ENGINE_GOOGLE = "ANALYTIC_ENGINE_GOOGLE";
    public final static Map<String, String> ENGINE_TO_CLASS_MAP = new HashMap<String, String>() {{
        put(ANALYTIC_ENGINE_DAS, APIMgtUsageHandler.class.getName());
        put(ANALYTIC_ENGINE_GOOGLE, APPMgtGoogleAnalayticsHandler.class.getName());
    }};
  	
}

