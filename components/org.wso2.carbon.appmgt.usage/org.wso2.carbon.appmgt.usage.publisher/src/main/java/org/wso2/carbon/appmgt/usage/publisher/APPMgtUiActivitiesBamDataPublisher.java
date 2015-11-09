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
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.appmgt.usage.publisher.internal.APPManagerConfigurationServiceComponent;
import org.wso2.carbon.appmgt.usage.publisher.service.APIMGTConfigReaderService;
import org.wso2.carbon.appmgt.impl.dao.AppMDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

/*
 * This class is used to capture the UI activity changes events and publish to
 * BAM or directly write to database
 */
public class APPMgtUiActivitiesBamDataPublisher {
	private LoadBalancingDataPublisher loadBalancingDataPublisher;

	// Stream name displayed in BAM
	final String USER_ACTIVITY_STREAM = APPManagerConfigurationServiceComponent
			.getApiMgtConfigReaderService()
			.getApiManagerBamUiActivityStreamName();

	// Stream version displayed in BAM
	final String USER_ACTIVITY_STREAM_VERSION = APPManagerConfigurationServiceComponent
			.getApiMgtConfigReaderService()
			.getApiManagerBamUiActivityStreamVersion();

	// used to check if the BAM is configured or not
	private boolean enableUiActivityBamPublishing = false;

	private static final Log log = LogFactory
			.getLog(APPMgtUiActivitiesBamDataPublisher.class);

	// BAM input stream
	private String userActivityStream;

	// builds the input stream format to be sent to BAM
	private String buildBamString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{ 'name':'")
				.append(USER_ACTIVITY_STREAM)
				.append("','version':'")
				.append(USER_ACTIVITY_STREAM_VERSION)
				.append("','nickName': 'AppManager User Activity',")
				.append(" 'description': 'This stream will store user activity',")
				.append("   'payloadData':[")
				.append("    {'name':'appId', 'type':'string'},")
				.append("    {'name':'userId', 'type':'string'},")
				.append("    {'name':'item','type':'string'},")
				.append("    {'name':'action',  'type':'string' },")
				.append("    {'name':'timestamp', 'type':'long'},")
				.append("    {'name':'tenantId', 'type':'int'},")
                .append("    {'name':'appName', 'type':'string'},")
                .append("    {'name':'appVersion', 'type':'string'}")
				.append("    ]    }");
		return builder.toString();

	}

	public APPMgtUiActivitiesBamDataPublisher() {
		userActivityStream = buildBamString();

		APIMGTConfigReaderService apimgtConfigReaderService = APPManagerConfigurationServiceComponent
				.getApiMgtConfigReaderService();
		enableUiActivityBamPublishing = apimgtConfigReaderService.isUiActivityBamPublishEnabled();

		// check if the BAM is configured
		if (enableUiActivityBamPublishing) {
			String bamServerURL = apimgtConfigReaderService.getBamServerURL();

            if ("".equals(bamServerURL)) {
                log.error("BAM Server URL is not set");
                throw new RuntimeException("BAM Server URL is not set");
            }

			String bamServerUserName = apimgtConfigReaderService
					.getBamServerUser();
			String bamServerPassword = apimgtConfigReaderService
					.getBamServerPassword();

			if (log.isDebugEnabled()) {
				StringBuilder builderBamService = new StringBuilder();
				builderBamService.append("bamServerURL:").append(bamServerURL)
						.append(", bamServerUserName:")
						.append(bamServerUserName)
						.append(", bamServerPassword:")
						.append(bamServerPassword)
						.append(", enableStatPublishing:")
						.append(enableUiActivityBamPublishing);
				log.debug(builderBamService.toString());
			}

			AgentConfiguration agentConfiguration = new AgentConfiguration();
			Agent agent = new Agent(agentConfiguration);

			ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
			ArrayList<String> receiverGroupUrls = DataPublisherUtil
					.getReceiverGroups(bamServerURL);

			for (String aReceiverGroupURL : receiverGroupUrls) {
				ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
				String[] urls = aReceiverGroupURL.split(",");
				for (String aUrl : urls) {
					DataPublisherHolder aNode = new DataPublisherHolder(null,
							aUrl.trim(), bamServerUserName, bamServerPassword);
					dataPublisherHolders.add(aNode);
				}
				ReceiverGroup group = new ReceiverGroup(dataPublisherHolders,
						false);
				allReceiverGroups.add(group);
			}

			loadBalancingDataPublisher = new LoadBalancingDataPublisher(
					allReceiverGroups, agent);
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
        String action, item, timestamp, appId, userId, appName, appVersion;
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

            obj = null;

            // consider only form load event
            if ((AppMConstants.PAGE_LOAD_EVENT).equals(action)) {
                publishUserActivityEvents(action, item, timestamp, appId,
                        userId, tenantId, appName, appVersion);
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
    public void publishUserActivityEvents(String action, String item,
                                          String timestampStr, String appId, String userId, Integer tenantId, String appName, String appVersion) {
         try {
             Long timeStamp = new BigDecimal(timestampStr).longValue();
             // if BAM is configured
			if (enableUiActivityBamPublishing) {
				// publish to BAM
				if (!loadBalancingDataPublisher.isStreamDefinitionAdded(
						USER_ACTIVITY_STREAM, USER_ACTIVITY_STREAM_VERSION)) {
					loadBalancingDataPublisher.addStreamDefinition(
							userActivityStream, USER_ACTIVITY_STREAM,
							USER_ACTIVITY_STREAM_VERSION);
					Event event = new Event();
					event.setTimeStamp(System.currentTimeMillis());
					event.setPayloadData(new Object[] { appId, userId, item,
							action, timeStamp, tenantId,appName,appVersion });
					loadBalancingDataPublisher.publish(USER_ACTIVITY_STREAM,
							USER_ACTIVITY_STREAM_VERSION, event);
				}
			} else {
				// Write directly to DB
               // AppMDAO.saveStoreHits(appId.trim(), userId.trim(), tenantId);
                AppMDAO.saveStoreHits(appId.trim(), userId.trim(), tenantId, appName.trim(), appVersion);
            }
		} catch (AgentException e) {
             // Here the exception is only logged (but not thrown externally) as this method is
             // used only just to store the store hit count, An exception in this method should
             // not effect/block the users other actions
             log.error("Failed to publish build event : " + e.getMessage(), e);
        } catch (AppManagementException e) {
            log.error("Failed to write to table : " + e.getMessage(), e);
        } catch (SQLException e) {
            log.error("SQL exception found : " + e.getMessage(), e);
        }
    }
}
