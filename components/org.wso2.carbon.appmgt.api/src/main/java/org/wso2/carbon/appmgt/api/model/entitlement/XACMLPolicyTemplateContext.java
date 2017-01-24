/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appmgt.api.model.entitlement;

/**
 * Encapsulate data for XACML policy generation.
 */
public class XACMLPolicyTemplateContext {

    private static final String POLICY_ID_PREFIX = "wso2appm";
	
	private int appId;
	private String appUuid;
	private int policyGroupId;
	private int ruleId;
	private String ruleContent;
	private String policyId;

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public String getAppUuid() {
		return appUuid;
	}

	public void setAppUuid(String appUuid) {
		this.appUuid = appUuid;
	}

	public int getPolicyGroupId() {
		return policyGroupId;
	}

	public void setPolicyGroupId(int policyGroupId) {
		this.policyGroupId = policyGroupId;
	}

	public int getRuleId() {
		return ruleId;
	}

	public void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}

	public String getRuleContent() {
		return ruleContent;
	}

	public void setRuleContent(String ruleContent) {
		this.ruleContent = ruleContent;
	}

	public String getPolicyId() {
		
		if(policyId == null){
			policyId = generatePolicyId();
		}

		return policyId;
	}

	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}
	
	public String generatePolicyId(){
		return String.format("%s:%s:%d", POLICY_ID_PREFIX, appUuid, ruleId);
	}
	
}
