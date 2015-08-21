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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.usage.publisher.dto.CacheStatPublisherDTO;
import org.wso2.carbon.appmgt.usage.publisher.dto.DataBridgeCacheStatPublisherDTO;
import org.wso2.carbon.appmgt.usage.publisher.dto.DataBridgeFaultPublisherDTO;
import org.wso2.carbon.appmgt.usage.publisher.dto.DataBridgeRequestPublisherDTO;
import org.wso2.carbon.appmgt.usage.publisher.dto.DataBridgeResponsePublisherDTO;
import org.wso2.carbon.appmgt.usage.publisher.dto.FaultPublisherDTO;
import org.wso2.carbon.appmgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.appmgt.usage.publisher.dto.ResponsePublisherDTO;
import org.wso2.carbon.appmgt.usage.publisher.internal.APPManagerConfigurationServiceComponent;
import org.wso2.carbon.appmgt.usage.publisher.internal.DataPublisherAlreadyExistsException;
import org.wso2.carbon.appmgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.appmgt.usage.publisher.service.APIMGTConfigReaderService;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.commons.exception.*;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class APIMgtUsageDataBridgeDataPublisher implements APIMgtUsageDataPublisher{

    private static final Log log   = LogFactory.getLog(APIMgtUsageDataBridgeDataPublisher.class);

    private LoadBalancingDataPublisher dataPublisher;

    public void init(){
        try {
            if(log.isDebugEnabled()){
                log.debug("Initializing APIMgtUsageDataBridgeDataPublisher");
            }

            this.dataPublisher = getDataPublisher();

            //If Request Stream Definition does not exist.
            if(!dataPublisher.isStreamDefinitionAdded(APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerRequestStreamName(),
            		APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerRequestStreamVersion())){

                //Get Request Stream Definition
                String requestStreamDefinition =  DataBridgeRequestPublisherDTO.getStreamDefinition();

                //Add Request Stream Definition.
                dataPublisher.addStreamDefinition(requestStreamDefinition,
                		APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerRequestStreamName(),
                		APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerRequestStreamVersion());
            }

            //If Response Stream Definition does not exist.
            if(!dataPublisher.isStreamDefinitionAdded(APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerResponseStreamName(),
            		APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerResponseStreamVersion())){

                //Get Response Stream Definition.
                String responseStreamDefinition = DataBridgeResponsePublisherDTO.getStreamDefinition();

                //Add Response Stream Definition.
                dataPublisher.addStreamDefinition(responseStreamDefinition,
                		APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerResponseStreamName(),
                		APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerResponseStreamVersion());

            }

            //If Fault Stream Definition does not exist.
            if(!dataPublisher.isStreamDefinitionAdded(APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerFaultStreamName(),
            		APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerFaultStreamVersion())){

                //Get Fault Stream Definition
                String faultStreamDefinition = DataBridgeFaultPublisherDTO.getStreamDefinition();

                //Add Fault Stream Definition;
                dataPublisher.addStreamDefinition(faultStreamDefinition,
                		APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerFaultStreamName(),
                		APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerFaultStreamVersion());
            }
            
          //If Cache stat Stream Definition does not exist.
            if(!dataPublisher.isStreamDefinitionAdded(APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerCacheStatStreamName(),
            		APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerCacheStatStreamVersion())){

                //Get cache Stream Definition
                String cacheStatStreamDefinition = DataBridgeCacheStatPublisherDTO.getStreamDefinition();

                //Add cache Stream Definition;
                dataPublisher.addStreamDefinition(cacheStatStreamDefinition,
                              APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerCacheStatStreamName(),
                              APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerCacheStatStreamVersion());
            }
        }catch (Exception e){
            log.error("Error initializing APIMgtUsageDataBridgeDataPublisher", e);
        }
    }

    public void publishEvent(RequestPublisherDTO requestPublisherDTO) {
        DataBridgeRequestPublisherDTO dataBridgeRequestPublisherDTO = new DataBridgeRequestPublisherDTO(requestPublisherDTO);
        try {
            //Publish Request Data
			dataPublisher.publish(APPManagerConfigurationServiceComponent
					.getApiMgtConfigReaderService()
					.getApiManagerRequestStreamName(),
					APPManagerConfigurationServiceComponent
							.getApiMgtConfigReaderService()
							.getApiManagerRequestStreamVersion(), System
							.currentTimeMillis(), new Object[] { "external" },
					null, (Object[]) dataBridgeRequestPublisherDTO
							.createPayload());
        } catch(AgentException e){
            log.error("Error while publishing Request event", e);
        }
    }

    public void publishEvent(ResponsePublisherDTO responsePublisherDTO) {
        DataBridgeResponsePublisherDTO dataBridgeResponsePublisherDTO = new DataBridgeResponsePublisherDTO(responsePublisherDTO);
        try {
            //Publish Response Data
            dataPublisher.publish(APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerResponseStreamName(),
            		APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerResponseStreamVersion() ,
                    System.currentTimeMillis(), new Object[]{"external"}, null,
                    (Object[]) dataBridgeResponsePublisherDTO.createPayload());

        } catch (AgentException e) {
            log.error("Error while publishing Response event", e);
        }
    }

    public void publishEvent(FaultPublisherDTO faultPublisherDTO) {
        DataBridgeFaultPublisherDTO dataBridgeFaultPublisherDTO = new DataBridgeFaultPublisherDTO(faultPublisherDTO);
        try {
            //Publish Fault Data
            dataPublisher.publish(APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerFaultStreamName(),
            		APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService().getApiManagerFaultStreamVersion() ,
                    System.currentTimeMillis(), new Object[]{"external"}, null,
                    (Object[]) dataBridgeFaultPublisherDTO.createPayload());

        } catch (AgentException e) {
            log.error("Error while publishing Fault event", e);
        }
    }
    

	@Override
	public void publishEvent(CacheStatPublisherDTO cacheDataPublisherDTO) {
		DataBridgeCacheStatPublisherDTO dataBridgeCacheStatPublisherDTO =
		                                                                  new DataBridgeCacheStatPublisherDTO(
		                                                                                                      cacheDataPublisherDTO);
		try {
			// Publish Fault Data
			dataPublisher.publish(APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService()
			                                                             .getApiManagerCacheStatStreamName(),
			                      APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService()
			                                                             .getApiManagerCacheStatStreamVersion(),
			                      System.currentTimeMillis(), new Object[] { "external" }, null,
			                      (Object[]) dataBridgeCacheStatPublisherDTO.createPayload());

		} catch (AgentException e) {
			log.error("Error while publishing Fault event", e);
		}

	}

    private static LoadBalancingDataPublisher getDataPublisher()
            throws AgentException, MalformedURLException, AuthenticationException,
                   TransportException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        //Get LoadBalancingDataPublisher which has been registered for the tenant.
        LoadBalancingDataPublisher loadBalancingDataPublisher = UsageComponent.getDataPublisher(tenantDomain);

        //If a LoadBalancingDataPublisher had not been registered for the tenant.
        if(loadBalancingDataPublisher == null){
            APIMGTConfigReaderService apimgtConfigReaderService =
                    APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService();

            List<String> receiverGroups = org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil.
                    getReceiverGroups(apimgtConfigReaderService.getBamServerURL());

            String serverUser = apimgtConfigReaderService.getBamServerUser();
            String serverPassword = apimgtConfigReaderService.getBamServerPassword();
            List<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();

            for(String receiverGroupString : receiverGroups){
                String[] serverURLs = receiverGroupString.split(",");
                List<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();

                for(int i=0; i<serverURLs.length; i++){
                    String serverURL = serverURLs[i];
                    DataPublisherHolder dataPublisherHolder =
                            new DataPublisherHolder(null, serverURL, serverUser, serverPassword);
                    dataPublisherHolders.add(dataPublisherHolder);
                }

                ReceiverGroup receiverGroup = new ReceiverGroup((ArrayList)dataPublisherHolders);
                allReceiverGroups.add(receiverGroup);
            }

            //Create new LoadBalancingDataPublisher for the tenant.
            loadBalancingDataPublisher = new LoadBalancingDataPublisher((ArrayList)allReceiverGroups);
            try {
                //Add created LoadBalancingDataPublisher.
                UsageComponent.addDataPublisher(tenantDomain, loadBalancingDataPublisher);
            } catch (DataPublisherAlreadyExistsException e) {
                log.warn("Attempting to register a data publisher for the tenant " + tenantDomain +
                        " when one already exists. Returning existing data publisher");
                return UsageComponent.getDataPublisher(tenantDomain);
            }
        }

        return loadBalancingDataPublisher;
    }

}
