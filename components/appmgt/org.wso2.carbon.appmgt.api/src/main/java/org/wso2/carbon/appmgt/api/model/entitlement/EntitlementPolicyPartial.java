/*
 *  ​Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.​
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

package org.wso2.carbon.appmgt.api.model.entitlement;

/**
 * Represents entitlement policy partial which is defined as conditions in
 * entitlement policy
 */

public class EntitlementPolicyPartial {

	private int policyPartialId;
	private String policyPartialName;
	private String policyPartialContent;
	private String ruleEffect;
	private boolean isShared;
	private String author;
	private String description;

	/**
	 * Get author of the entitlement policy partial
	 * 
	 * @return author name
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Set author of the entitlement policy partial
	 * 
	 * @param author
	 *            author name
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * Check whether the entitlement policy is shared
	 * 
	 * @return true if shared
	 */
	public boolean isShared() {
		return isShared;
	}

	/**
	 * Set whether the entitlement policy is shared
	 * 
	 * @param isShared
	 *            true if shared
	 */
	public void setShared(boolean isShared) {
		this.isShared = isShared;
	}

	/**
	 * Get entitlement policy partial id
	 * 
	 * @return partial id
	 */
	public int getPolicyPartialId() {
		return policyPartialId;
	}

	/**
	 * Set entitlement policy partial id
	 * 
	 * @param policyPartialId
	 *            policy partial id
	 */
	public void setPolicyPartialId(int policyPartialId) {
		this.policyPartialId = policyPartialId;
	}

	/**
	 * Get entitlement policy partial name
	 * 
	 * @return policy partial name
	 */
	public String getPolicyPartialName() {
		return policyPartialName;
	}

	/**
	 * Set entitlement policy partial name
	 * 
	 * @param policyPartialName
	 *            policy partial name
	 */
	public void setPolicyPartialName(String policyPartialName) {
		this.policyPartialName = policyPartialName;
	}

	/**
	 * Get entitlement policy partial content
	 * 
	 * @return policy partial content
	 */
	public String getPolicyPartialContent() {
		return policyPartialContent;
	}

	/**
	 * Set entitlement policy partial content
	 * 
	 * @param policyPartialContent
	 *            policy partial content
	 */
	public void setPolicyPartialContent(String policyPartialContent) {
		this.policyPartialContent = policyPartialContent;
	}

	public String getRuleEffect() {
		return ruleEffect;
	}

	public void setRuleEffect(String ruleEffect) {
		this.ruleEffect = ruleEffect;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
