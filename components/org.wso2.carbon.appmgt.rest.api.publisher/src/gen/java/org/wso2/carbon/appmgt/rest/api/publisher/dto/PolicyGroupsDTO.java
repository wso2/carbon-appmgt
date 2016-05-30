package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class PolicyGroupsDTO  {

  private Integer policyGroupId = null;
  
  private String policyGroupName = null;
  
  
  private String description = null;
  
  
  private String throttlingTier = null;
  
  
  private List<String> userRoles = new ArrayList<String>();
  
  
  private List<String> policyPartialMapping = new ArrayList<String>();
  
  
  private String allowAnonymousAccess = null;


  @ApiModelProperty(value = "Id of the policy group")
  @JsonProperty("policyGroupId")
  public Integer getPolicyGroupId() {
    return policyGroupId;
  }
  public void setPolicyGroupId(Integer policyGroupId) {
    this.policyGroupId = policyGroupId;
  }


  /**
   * Name of the policy group
   **/
  @ApiModelProperty(value = "Name of the policy group")
  @JsonProperty("policyGroupName")
  public String getPolicyGroupName() {
    return policyGroupName;
  }
  public void setPolicyGroupName(String policyGroupName) {
    this.policyGroupName = policyGroupName;
  }

  
  /**
   * Description of the policy group
   **/
  @ApiModelProperty(value = "Description of the policy group")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   * Throttling tier
   **/
  @ApiModelProperty(value = "Throttling tier")
  @JsonProperty("throttlingTier")
  public String getThrottlingTier() {
    return throttlingTier;
  }
  public void setThrottlingTier(String throttlingTier) {
    this.throttlingTier = throttlingTier;
  }

  
  /**
   * The name of the uri template policy group
   **/
  @ApiModelProperty(value = "The name of the uri template policy group")
  @JsonProperty("userRoles")
  public List<String> getUserRoles() {
    return userRoles;
  }
  public void setUserRoles(List<String> userRoles) {
    this.userRoles = userRoles;
  }

  
  /**
   * Entitlement policy partial name list
   **/
  @ApiModelProperty(value = "Entitlement policy partial name list")
  @JsonProperty("policyPartialMapping")
  public List<String> getPolicyPartialMapping() {
    return policyPartialMapping;
  }
  public void setPolicyPartialMapping(List<String> policyPartialMapping) {
    this.policyPartialMapping = policyPartialMapping;
  }

  
  /**
   * Allow anonymous access to mobile
   **/
  @ApiModelProperty(value = "Allow anonymous access to mobile")
  @JsonProperty("allowAnonymousAccess")
  public String getAllowAnonymousAccess() {
    return allowAnonymousAccess;
  }
  public void setAllowAnonymousAccess(String allowAnonymousAccess) {
    this.allowAnonymousAccess = allowAnonymousAccess;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyGroupsDTO {\n");
    
    sb.append("  policyGroupName: ").append(policyGroupName).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  throttlingTier: ").append(throttlingTier).append("\n");
    sb.append("  userRoles: ").append(userRoles).append("\n");
    sb.append("  policyPartialMapping: ").append(policyPartialMapping).append("\n");
    sb.append("  allowAnonymousAccess: ").append(allowAnonymousAccess).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
