package org.wso2.carbon.appmgt.usage.publisher.dto;

import org.wso2.carbon.appmgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.appmgt.usage.publisher.internal.APPManagerConfigurationServiceComponent;

public class DataBridgeCacheStatPublisherDTO extends CacheStatPublisherDTO {
	
	public DataBridgeCacheStatPublisherDTO (CacheStatPublisherDTO cacheStatPublisherDTO) {
		setContext(cacheStatPublisherDTO.getContext());
        setApi_version(cacheStatPublisherDTO.getApi_version());
        setApi(cacheStatPublisherDTO.getApi());
        setVersion(cacheStatPublisherDTO.getVersion());
        setCachHit(cacheStatPublisherDTO.getCachHit());
        setRequestTime((cacheStatPublisherDTO.getRequestTime()));
        setUsername(cacheStatPublisherDTO.getUsername());
        setTenantDomain(cacheStatPublisherDTO.getTenantDomain());
        setHostName(DataPublisherUtil.getHostAddress());
        setApiPublisher(cacheStatPublisherDTO.getApiPublisher());
        setApplicationName(cacheStatPublisherDTO.getApplicationName());
        setApplicationId(cacheStatPublisherDTO.getApplicationId());
        setTrackingCode(cacheStatPublisherDTO.getTrackingCode());
        setReferer(cacheStatPublisherDTO.getReferer());
        setResponseTime(cacheStatPublisherDTO.getResponseTime());
        setFullRequestPath(cacheStatPublisherDTO.getFullRequestPath());
	}
	
	public static String getStreamDefinition() {

		String streamDefinition = "{" + "  'name':'"
				+ APPManagerConfigurationServiceComponent
						.getApiMgtConfigReaderService()
						.getApiManagerCacheStatStreamName()
				+ "',"
				+ "  'version':'"
				+ APPManagerConfigurationServiceComponent
						.getApiMgtConfigReaderService()
						.getApiManagerCacheStatStreamVersion() + "',"
				+ "  'nickName': 'App Manager SAML Cache Data',"
				+ "  'description': 'SAML Token Cache Hit and Miss Data'," + "  'metaData':["
				+ "          {'name':'clientType','type':'STRING'}" + "  ],"
				+ "  'payloadData':[" +

				"          {'name':'context','type':'STRING'},"
				+ "          {'name':'api_version','type':'STRING'},"
				+ "          {'name':'api','type':'STRING'},"
				+ "          {'name':'fullRequestPath','type':'STRING'},"
				+ "          {'name':'version','type':'STRING'},"
				+ "          {'name':'request','type':'INT'},"
				+ "          {'name':'cacheHit','type':'INT'},"
				+ "          {'name':'requestTime','type':'LONG'},"
				+ "          {'name':'userId','type':'STRING'},"
				+ "          {'name':'tenantDomain','type':'STRING'},"
				+ "          {'name':'hostName','type':'STRING'},"
				+ "          {'name':'apiPublisher','type':'STRING'},"
				+ "          {'name':'applicationName','type':'STRING'},"
				+ "          {'name':'applicationId','type':'STRING'},"
				+ "          {'name':'trackingCode','type':'STRING'},"
				+ "          {'name':'referer','type':'STRING'},"
				+ "          {'name':'responseTime','type':'LONG'}" +

				"  ]" +

				"}";

        return streamDefinition;
    }

    public Object createPayload(){
        return new Object[]{getContext(),getApi_version(),getApi(), getFullRequestPath(), 
                getVersion(),getRequestCount(), getCachHit(), getRequestTime(), getUsername(),
                getTenantDomain(),getHostName(),getApiPublisher(), getApplicationName(), getApplicationId(),getTrackingCode(),getReferer(), getResponseTime()};
    }

}
