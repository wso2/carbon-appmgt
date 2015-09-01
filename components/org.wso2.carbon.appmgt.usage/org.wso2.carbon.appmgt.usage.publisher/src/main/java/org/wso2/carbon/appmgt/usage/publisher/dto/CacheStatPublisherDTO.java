package org.wso2.carbon.appmgt.usage.publisher.dto;

public class CacheStatPublisherDTO extends PublisherDTO {
	
	private int requestCount = 1;
	
	private int cachHit;
	
	private String fullRequestPath;	
	
	private long requestTime;
	
	public int getRequestCount(){
        return requestCount;
    }

    public int getCachHit() {
		return cachHit;
	}

	public void setCachHit(int cachHit) {
		this.cachHit = cachHit;
	}

	public long getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(long requestTime) {
		this.requestTime = requestTime;
	}
	
	public String getFullRequestPath() {
		return fullRequestPath;
	}

	public void setFullRequestPath(String fullRequestTime) {
		this.fullRequestPath = fullRequestTime;
	}


}
