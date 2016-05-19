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
import org.wso2.carbon.appmgt.usage.publisher.service.APIMGTConfigReaderService;
import org.wso2.carbon.databridge.agent.DataPublisher;

public class APIMgtUsageDataBridgeDataPublisher implements APIMgtUsageDataPublisher{

    private static final Log log   = LogFactory.getLog(APIMgtUsageDataBridgeDataPublisher.class);

    private DataPublisher dataPublisher;

    public void init(){
        try {
            if(log.isDebugEnabled()){
                log.debug("Initializing APIMgtUsageDataBridgeDataPublisher");
            }
            this.dataPublisher = DataPublisherUtil.getDataPublisher();
        }catch (Exception e){
            log.error("Error initializing APIMgtUsageDataBridgeDataPublisher", e);
        }
    }

    public void publishEvent(RequestPublisherDTO requestPublisherDTO) {
        DataBridgeRequestPublisherDTO dataBridgeRequestPublisherDTO = new DataBridgeRequestPublisherDTO(requestPublisherDTO);
        APIMGTConfigReaderService apimgtConfigReaderService = APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService();

        String streamId = apimgtConfigReaderService.getApiManagerRequestStreamName() + ":"
                + apimgtConfigReaderService.getApiManagerRequestStreamVersion();

        //Publish Request Data
        dataPublisher.publish(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                (Object[]) dataBridgeRequestPublisherDTO.createPayload());

    }

    public void publishEvent(ResponsePublisherDTO responsePublisherDTO) {
        DataBridgeResponsePublisherDTO dataBridgeResponsePublisherDTO = new DataBridgeResponsePublisherDTO(responsePublisherDTO);
        APIMGTConfigReaderService apimgtConfigReaderService = APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService();

        String streamId = apimgtConfigReaderService.getApiManagerResponseStreamName() + ":"
                + apimgtConfigReaderService.getApiManagerResponseStreamName();

        //Publish Response Data
        dataPublisher.publish(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                (Object[]) dataBridgeResponsePublisherDTO.createPayload());
    }

    public void publishEvent(FaultPublisherDTO faultPublisherDTO) {
        DataBridgeFaultPublisherDTO dataBridgeFaultPublisherDTO = new DataBridgeFaultPublisherDTO(faultPublisherDTO);
        APIMGTConfigReaderService apimgtConfigReaderService = APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService();

        String streamId = apimgtConfigReaderService.getApiManagerFaultStreamName() + ":"
                + apimgtConfigReaderService.getApiManagerFaultStreamVersion();

        //Publish Fault Data
        dataPublisher.publish(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                (Object[]) dataBridgeFaultPublisherDTO.createPayload());

    }

	@Override
	public void publishEvent(CacheStatPublisherDTO cacheDataPublisherDTO) {
		DataBridgeCacheStatPublisherDTO dataBridgeCacheStatPublisherDTO = new DataBridgeCacheStatPublisherDTO(cacheDataPublisherDTO);
        APIMGTConfigReaderService apimgtConfigReaderService = APPManagerConfigurationServiceComponent.getApiMgtConfigReaderService();

        String streamId = apimgtConfigReaderService.getApiManagerCacheStatStreamName() + ":"
                + apimgtConfigReaderService.getApiManagerCacheStatStreamVersion();
        // Publish Cache Data
        dataPublisher.publish(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                (Object[]) dataBridgeCacheStatPublisherDTO.createPayload());
	}

}
