/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.appmgt.mdm.restconnector.beans;



public class RemoteServer {

	private String tokenApiURL;
	private String clientKey ;
	private String clientSecret;
	private String authUser ;
	private String authPass ;

	public String getTokenApiURL() {
		return tokenApiURL;
	}

	public void setTokenApiURL(String tokenApiURL) {
		this.tokenApiURL = tokenApiURL;
	}

	public String getClientKey() {
		return clientKey;
	}

	public void setClientKey(String clientKey) {
		this.clientKey = clientKey;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getAuthUser() {
		return authUser;
	}

	public void setAuthUser(String authUser) {
		this.authUser = authUser;
	}

	public String getAuthPass() {
		return authPass;
	}

	public void setAuthPass(String authPass) {
		this.authPass = authPass;
	}

	public boolean isEmpty() {
		return tokenApiURL == null && clientKey == null && clientSecret == null &&
		       authUser == null && authPass == null;
	}

}
