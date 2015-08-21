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
package org.wso2.carbon.appmgt.usage.publisher.dto;

import org.wso2.carbon.appmgt.usage.publisher.APIMgtUsagePublisherConstants;
import org.wso2.carbon.appmgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.appmgt.usage.publisher.internal.APPManagerConfigurationServiceComponent;

public class DataBridgeResponsePublisherDTO extends ResponsePublisherDTO {

    public DataBridgeResponsePublisherDTO(ResponsePublisherDTO responsePublisherDTO){
        //setConsumerKey(responsePublisherDTO.getConsumerKey());
        setContext(responsePublisherDTO.getContext());
        setApi_version(responsePublisherDTO.getApi_version());
        setApi(responsePublisherDTO.getApi());
        setResource(responsePublisherDTO.getResource());
        setMethod(responsePublisherDTO.getMethod());
        setVersion(responsePublisherDTO.getVersion());
        setResponseTime(responsePublisherDTO.getResponseTime());
        setServiceTime(responsePublisherDTO.getServiceTime());
        setUsername(responsePublisherDTO.getUsername());
        setTenantDomain(responsePublisherDTO.getTenantDomain());
        setHostName(DataPublisherUtil.getHostAddress());
        setApiPublisher(responsePublisherDTO.getApiPublisher());
        setApplicationName(responsePublisherDTO.getApplicationName());
        setApplicationId(responsePublisherDTO.getApplicationId());
        setTrackingCode(responsePublisherDTO.getTrackingCode());
        //setLoggedInUSer(responsePublisherDTO.getLoggedInUSer());
        setReferer(responsePublisherDTO.getReferer());
        setResponseTime(responsePublisherDTO.getResponseTime());
    }

    public static String getStreamDefinition() {

		String streamDefinition = "{" + "  'name':'"
				+ APPManagerConfigurationServiceComponent
						.getApiMgtConfigReaderService()
						.getApiManagerResponseStreamName()
				+ "',"
				+ "  'version':'"
				+ APPManagerConfigurationServiceComponent
						.getApiMgtConfigReaderService()
						.getApiManagerResponseStreamVersion() + "',"
				+ "  'nickName': 'WebApp Manager Reponse Data',"
				+ "  'description': 'Response Data'," + "  'metaData':["
				+ "          {'name':'clientType','type':'STRING'}" + "  ],"
				+ "  'payloadData':["
				+ "          {'name':'context','type':'STRING'},"
				+ "          {'name':'api_version','type':'STRING'},"
				+ "          {'name':'api','type':'STRING'},"
				+ "          {'name':'resource','type':'STRING'},"
				+ "          {'name':'method','type':'STRING'},"
				+ "          {'name':'version','type':'STRING'},"
				+ "          {'name':'response','type':'INT'},"
				+ "          {'name':'responseTime','type':'LONG'},"
				+ "          {'name':'serviceTime','type':'LONG'},"
				+ "          {'name':'userId','type':'STRING'},"
				+ "          {'name':'tenantDomain','type':'STRING'},"
				+ "          {'name':'hostName','type':'STRING'},"
				+ "          {'name':'apiPublisher','type':'STRING'},"
				+ "          {'name':'applicationName','type':'STRING'},"
				+ "          {'name':'applicationId','type':'STRING'},"
				+ "          {'name':'trackingCode','type':'STRING'},"
				+ "          {'name':'referer','type':'STRING'},"
				+ "          {'name':'responseTime','type':'LONG'}" + "  ]" +

				"}";

		return streamDefinition;
    }

    public Object createPayload(){
        return new Object[]{getContext(),getApi_version(),getApi(),getResource(),getMethod(),
                getVersion(),getResponse(),getResponseTime(),getServiceTime(),getUsername(),getTenantDomain(),getHostName(),
                getApiPublisher(), getApplicationName(), getApplicationId(),getTrackingCode(),getReferer()};
    }

}
