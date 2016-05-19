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

import org.wso2.carbon.appmgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.appmgt.usage.publisher.internal.APPManagerConfigurationServiceComponent;

public class DataBridgeRequestPublisherDTO extends RequestPublisherDTO {

    public DataBridgeRequestPublisherDTO (RequestPublisherDTO requestPublisherDTO){
       // setConsumerKey(requestPublisherDTO.getConsumerKey());
        setContext(requestPublisherDTO.getContext());
        setApi_version(requestPublisherDTO.getApi_version());
        setApi(requestPublisherDTO.getApi());
        setResource(requestPublisherDTO.getResource());
        setMethod(requestPublisherDTO.getMethod());
        setVersion(requestPublisherDTO.getVersion());
        setRequestTime(requestPublisherDTO.getRequestTime());
        setUsername(requestPublisherDTO.getUsername());
        setTenantDomain(requestPublisherDTO.getTenantDomain());
        setHostName(DataPublisherUtil.getHostAddress());
        setApiPublisher(requestPublisherDTO.getApiPublisher());
        setApplicationName(requestPublisherDTO.getApplicationName());
        setApplicationId(requestPublisherDTO.getApplicationId());
        setTrackingCode(requestPublisherDTO.getTrackingCode());
        //setLoggedInUSer(requestPublisherDTO.getLoggedInUSer());
        setReferer(requestPublisherDTO.getReferer());
        setServiceTimeOfPage(requestPublisherDTO.getServiceTimeOfPage());
    }

    public static String getStreamDefinition() {
		String streamDefinition = "{" + "  'name':'"
				+ APPManagerConfigurationServiceComponent
						.getApiMgtConfigReaderService()
						.getApiManagerRequestStreamName()
				+ "',"
				+ "  'version':'"
				+ APPManagerConfigurationServiceComponent
						.getApiMgtConfigReaderService()
						.getApiManagerRequestStreamVersion() + "',"
				+ "  'nickName': 'WebApp Manager Request Data',"
				+ "  'description': 'Request Data'," + "  'metaData':["
				+ "          {'name':'clientType','type':'STRING'}" + "  ],"
				+ "  'payloadData':["
				+ "          {'name':'context','type':'STRING'},"
				+ "          {'name':'api_version','type':'STRING'},"
				+ "          {'name':'api','type':'STRING'},"
				+ "          {'name':'resource','type':'STRING'},"
				+ "          {'name':'method','type':'STRING'},"
				+ "          {'name':'version','type':'STRING'},"
				+ "          {'name':'request','type':'INT'},"
				+ "          {'name':'requestTime','type':'LONG'},"
				+ "          {'name':'userId','type':'STRING'},"
				+ "          {'name':'tenantDomain','type':'STRING'},"
				+ "          {'name':'hostName','type':'STRING'},"
				+ "          {'name':'apiPublisher','type':'STRING'},"
				+ "          {'name':'applicationName','type':'STRING'},"
				+ "          {'name':'applicationId','type':'STRING'},"
				+ "          {'name':'trackingCode','type':'STRING'},"
				+ "          {'name':'referer','type':'STRING'},"
				+ "          {'name':'serviceTimeOfPage','type':'LONG'}" +

				"  ]" + "}";

		return streamDefinition;
    }

    public Object createPayload(){
        return new Object[]{getContext(),getApi_version(),getApi(),getResource(),getMethod(),
                            getVersion(), getRequestCount(),getRequestTime(),getUsername(),getTenantDomain(),getHostName(),
                            getApiPublisher(), getApplicationName(), getApplicationId(),getTrackingCode(),getReferer(),getServiceTimeOfPage()};
        
    }

}
