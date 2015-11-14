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
package org.wso2.carbon.appmgt.usage.publisher.service;

import org.apache.axis2.util.JavaUtils;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.AppManagerConfiguration;
import org.wso2.carbon.appmgt.usage.publisher.APIMgtUsagePublisherConstants;
import java.util.HashMap;
import java.util.Map;

public class APIMGTConfigReaderService {

    private String bamServerThriftPort;
    private String bamServerURL;
    private String bamServerUser;
    private String bamServerPassword;
    private boolean enabled;
    private String publisherClass;
    private boolean googleAnalyticsTrackingEnabled;
    private String googleAnalyticsTrackingID;
   	private boolean cacheStatsEnabled;   
    

	private String apiManagerRequestStreamName;
    private String apiManagerRequestStreamVersion;
    private String apiManagerResponseStreamName;
    private String apiManagerResponseStreamVersion;
    private String apiManagerFaultStreamName;
    private String apiManagerFaultStreamVersion;
    private String apiManagerBamUiActivityStreamName;
    private String apiManagerBamUiActivityStreamVersion; 
	private boolean uiActivityBamPublishEnabled;
    private String apiManagerCacheStatStreamName;
   	private String apiManagerCacheStatStreamVersion;
   	
 

    private Map<String, Boolean> enabledAnalyticsEngines = new HashMap<String, Boolean>();

    public APIMGTConfigReaderService(AppManagerConfiguration config) {
        String enabledStr = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_ENABLED);
        enabled = enabledStr != null && JavaUtils.isTrueExplicitly(enabledStr);
        bamServerThriftPort = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_THRIFT_PORT);
        bamServerURL = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_SERVER_URL);
        bamServerUser = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_SERVER_USER);
        bamServerPassword = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_SERVER_PASSWORD);
        publisherClass = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_PUBLISHER_CLASS);
        String googleAnalyticsEnabledStr = config.getFirstProperty(APIMgtUsagePublisherConstants.API_GOOGLE_ANALYTICS_TRACKING_ENABLED);
        googleAnalyticsTrackingEnabled = googleAnalyticsEnabledStr != null && JavaUtils.isTrueExplicitly(googleAnalyticsEnabledStr);
        googleAnalyticsTrackingID = config.getFirstProperty(APIMgtUsagePublisherConstants.API_GOOGLE_ANALYTICS_TRACKING_ID);

		apiManagerRequestStreamName = config
				.getFirstProperty(APIMgtUsagePublisherConstants.API_MANAGER_REQUEST_STREAM_NAME);
		apiManagerRequestStreamVersion = config
				.getFirstProperty(APIMgtUsagePublisherConstants.API_MANAGER_REQUEST_STREAM_VERSION);
		apiManagerResponseStreamName = config
				.getFirstProperty(APIMgtUsagePublisherConstants.API_MANAGER_RESPONSE_STREAM_NAME);
		apiManagerResponseStreamVersion = config
				.getFirstProperty(APIMgtUsagePublisherConstants.API_MANAGER_RESPONSE_STREAM_VERSION);
		apiManagerFaultStreamName = config
				.getFirstProperty(APIMgtUsagePublisherConstants.API_MANAGER_FAULT_STREAM_NAME);
		apiManagerFaultStreamVersion = config
				.getFirstProperty(APIMgtUsagePublisherConstants.API_MANAGER_FAULT_STREAM_VERSION);
		apiManagerCacheStatStreamName = config
				.getFirstProperty(APIMgtUsagePublisherConstants.API_MANAGER_CACHE_STAT_STREAM_NAME);
		apiManagerCacheStatStreamVersion = config
				.getFirstProperty(APIMgtUsagePublisherConstants.API_MANAGER_CACHE_STAT_VERSION);
		apiManagerBamUiActivityStreamName = config
				.getFirstProperty(APIMgtUsagePublisherConstants.API_BAM_UI_ACTIVITY_STREAM);
		apiManagerBamUiActivityStreamVersion = config
				.getFirstProperty(APIMgtUsagePublisherConstants.API_BAM_UI_ACTIVITY_STREAM_VERSION);   
		String uiActivityBAMPublishEnabledStr = config
				.getFirstProperty(AppMConstants.APP_USAGE_BAM_UI_ACTIVITY_ENABLED);
		uiActivityBamPublishEnabled = uiActivityBAMPublishEnabledStr != null
				&& JavaUtils.isTrueExplicitly(uiActivityBAMPublishEnabledStr);
		String cacheStatsEnabledString = config.
				getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_CACHE_STATS_ENABLED);
		cacheStatsEnabled = cacheStatsEnabledString != null && JavaUtils.isTrueExplicitly(cacheStatsEnabledString);
		
		
        if(enabled) {
            enabledAnalyticsEngines.put(APIMgtUsagePublisherConstants.ANALYTIC_ENGINE_BAM, true);
        } else {
            enabledAnalyticsEngines.put(APIMgtUsagePublisherConstants.ANALYTIC_ENGINE_BAM, false);
        }

        if(googleAnalyticsTrackingEnabled) {
            enabledAnalyticsEngines.put(APIMgtUsagePublisherConstants.ANALYTIC_ENGINE_GOOGLE, true);
        } else {
            enabledAnalyticsEngines.put(APIMgtUsagePublisherConstants.ANALYTIC_ENGINE_GOOGLE, false);
        }
    }

    public String getBamServerThriftPort() {
        return bamServerThriftPort;
    }

    public String getBamServerPassword() {
        return bamServerPassword;
    }

    public String getBamServerUser() {
        return bamServerUser;
    }

    public String getBamServerURL() {
        return bamServerURL;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getPublisherClass() {
        return publisherClass;
    }

    public String getGoogleAnalyticsTrackingID() {
 		return googleAnalyticsTrackingID;
 	}

    public boolean isGoogleAnalyticsTrackingEnabled() {
    	return googleAnalyticsTrackingEnabled;
    }

    public Map<String, Boolean> getEnabledAnalyticsEngines() {
        return enabledAnalyticsEngines;
    }

	public String getApiManagerRequestStreamName() {
		return apiManagerRequestStreamName;
	}

	public String getApiManagerRequestStreamVersion() {
		return apiManagerRequestStreamVersion;
	}

	public String getApiManagerResponseStreamName() {
		return apiManagerResponseStreamName;
	}

	public String getApiManagerResponseStreamVersion() {
		return apiManagerResponseStreamVersion;
	}

	public String getApiManagerFaultStreamName() {
		return apiManagerFaultStreamName;
	}

	public String getApiManagerFaultStreamVersion() {
		return apiManagerFaultStreamVersion;
	}

	public String getApiManagerBamUiActivityStreamName() {
		return apiManagerBamUiActivityStreamName;
	}

	public String getApiManagerBamUiActivityStreamVersion() {
		return apiManagerBamUiActivityStreamVersion;
	}

	public boolean isUiActivityBamPublishEnabled() {
		return uiActivityBamPublishEnabled;
	}
	
	public String getApiManagerCacheStatStreamName() {
		return apiManagerCacheStatStreamName;
	}

	public String getApiManagerCacheStatStreamVersion() {
		return apiManagerCacheStatStreamVersion;
	}
	
	public boolean isCacheStatsEnabled() {
		return cacheStatsEnabled;
	}

}
