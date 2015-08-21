package org.wso2.carbon.appmgt.usage.client.dto;

/**
 * Created by binalip91 on 2/28/15.
 */
public class APPMCacheCountDTO {

    private String apiName;
    private String version;
    private long cacheHit;
    private String fullRequestPath;
    private long totalRequestCount;
    private String requestDate;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getCacheHit() {
        return cacheHit;
    }

    public void setCacheHit(long cacheHit) {
        this.cacheHit = cacheHit;
    }

    public String getFullRequestPath() {
        return fullRequestPath;
    }

    public void setFullRequestPath(String fullRequestPath) {
        this.fullRequestPath = fullRequestPath;
    }

    public long getTotalRequestCount() {
        return totalRequestCount;
    }

    public void setTotalRequestCount(long totalRequestCount) {
        this.totalRequestCount = totalRequestCount;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }
}
