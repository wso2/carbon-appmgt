/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;
import org.wso2.carbon.appmgt.usage.publisher.internal.APPManagerConfigurationServiceComponent;
import org.wso2.carbon.appmgt.usage.publisher.service.APIMGTConfigReaderService;
import org.wso2.carbon.databridge.agent.DataPublisher;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * This class is used to capture the UI activity changes events and publish to
 * DAS or directly write to database
 */
public class AppMUIActivitiesDASDataPublisher {
	private DataPublisher dataPublisher;

	// used to check if the DAS is configured or not
	private boolean enableUiActivityDASPublishing = false;

	private static final Log log = LogFactory.getLog(AppMUIActivitiesDASDataPublisher.class);

	public AppMUIActivitiesDASDataPublisher() {
		enableUiActivityDASPublishing = APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().
				isUiActivityDASPublishEnabled();
		if (enableUiActivityDASPublishing) {
			this.dataPublisher = DataPublisherUtil.getDataPublisher();
		}
	}

	/**
	 * 
	 * @param parseJSON
	 *            : object passed from the jaggery file containing the events
	 *            (Expects an object like :
	 *            [{"action":"page-load"},{"item":"textarea"
	 *            },{"timestamp":"123123123"
	 *            },{"appId":"111-3434-343"},{"userId":
	 *            "admin"},{"tenantId":"-1234"}]
	 */
    public void processUiActivityObject(Object[] parseJSON) {
        String action, item, timestamp, appId, userId, appName, appVersion, context;
        Integer tenantId;
		for (int i = 0; i < parseJSON.length; i++) {
			NativeObject obj = (NativeObject) parseJSON[i];
			action = obj.get("action", obj).toString();
			item = obj.get("item", obj).toString();
			timestamp = obj.get("timestamp", obj).toString();
			appId = obj.get("appId", obj).toString();
			userId = obj.get("userId", obj).toString();
			tenantId = Integer.parseInt(obj.get("tenantId", obj).toString());
			appName = obj.get("appName", obj).toString();
			appVersion = obj.get("appVersion", obj).toString();
            context = obj.get("context", obj).toString();
			obj = null;

			// consider only form load event
			if ((AppMConstants.PAGE_LOAD_EVENT).equals(action)) {
				publishUserActivityEvents(action, item, timestamp, appId,
					userId, tenantId, appName, appVersion, context);
			}
		}
	}

	/**
	 * Event publishing method (either to BAM or directly write to DB)
	 * 
	 * @param action
	 *            : action name
	 * @param item
	 *            : controller
	 * @param timestampStr
	 *            : time stamp
	 * @param appId
	 *            : Application Id
	 * @param userId
	 *            : User Id
	 * @param tenantId
	 *            : Tenant Id
	 */
    public void publishUserActivityEvents(String action, String item, String timestampStr, String appId,
                                          String userId, Integer tenantId, String appName,
                                          String appVersion, String context){
         try {
             Long timeStamp = new BigDecimal(timestampStr).longValue();
             // if DAS is configured
			 if (enableUiActivityDASPublishing) {
				 APIMGTConfigReaderService apimgtConfigReaderService = APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService();
				 String streamId = apimgtConfigReaderService.getApiManagerDasUiActivityStreamName() + ":"
						 + apimgtConfigReaderService.getApiManagerDasUiActivityStreamVersion();

				 //Publish UIActivity Data
				 dataPublisher.publish(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
						 (new Object[] {appId, userId, item, action, getCurrentDateTimeAsString(), tenantId, appName,
								 appVersion, context}));
			} else {
                // Write directly to DB
                AppMDAO.saveStoreHits(appId.trim(), userId.trim(), tenantId, appName.trim(),
                                      appVersion, context, timeStamp);
            }
		} catch (AppManagementException e) {
            log.error("Failed to write to table : " + e.getMessage(), e);
        }
    }

	private String getCurrentDateTimeAsString() {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date now = new Date();
		String strDate = sdfDate.format(now);
		return strDate;
	}
}
