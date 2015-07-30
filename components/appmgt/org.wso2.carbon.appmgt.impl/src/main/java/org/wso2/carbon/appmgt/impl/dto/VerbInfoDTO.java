package org.wso2.carbon.appmgt.impl.dto;

import org.wso2.carbon.appmgt.impl.AppMConstants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VerbInfoDTO implements Serializable {

	private String httpVerb;

	private String authType;

	private String throttling;

	private String requestKey;

	private boolean skipThrottling;


	public String getThrottling() {
		return throttling;
	}

	public void setThrottling(String throttling) {
		this.throttling = throttling;
	}

	public String getRequestKey() {
		return requestKey;
	}

	public void setRequestKey(String requestKey) {
		this.requestKey = requestKey;
	}

	public String getHttpVerb() {
		return httpVerb;
	}

	public void setHttpVerb(String httpVerb) {
		this.httpVerb = httpVerb;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public boolean requiresAuthentication() {
		return !AppMConstants.AUTH_TYPE_NONE.equalsIgnoreCase(authType);
	}

	public boolean isSkipThrottling() {
		return skipThrottling;
	}

	public void setSkipThrottling(boolean skipThrottling) {
		this.skipThrottling = skipThrottling;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		VerbInfoDTO that = (VerbInfoDTO) o;

		if (httpVerb != null ? !httpVerb.equals(that.getHttpVerb()) : that.getHttpVerb() != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return httpVerb != null ? httpVerb.hashCode() : 0;
	}

    private Map<String, Boolean> allowAnonymousUrlMap = new HashMap<String, Boolean>();

    /**
     * add values to allowAnonymousUrlMap
     *
     * @param key
     * @param value
     */
    public void addAllowAnonymousUrl(String key, Boolean value) {
        allowAnonymousUrlMap.put(key, value);
    }

    /**
     * get value from allowAnonymousUrlMap for the given key
     *
     * @param key
     * @return boolean result
     */
    public boolean getAllowAnonymousUrl(String key) {
        return allowAnonymousUrlMap.get(key);
    }

    /**
     * check if the allowAnonymousUrlMap is empty/null
     *
     * @return boolean result
     */
    public boolean isEmptyAllowAnonymousUrlMap() {
        return ((allowAnonymousUrlMap == null) || (allowAnonymousUrlMap.isEmpty()));
    }

    /**
     * Get all allowAnonymousUrlMap key/value list
     *
     * @return keySet list
     */
    public Set<String> getAllowAnonymousUrlList() {
        return allowAnonymousUrlMap.keySet();
    }


}
