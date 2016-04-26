package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class PolicyGroupDTO  {
  
  
  
  private Integer id = null;
  
  @NotNull
  private String name = null;
  
  
  private String throttlingTier = null;
  
  
  private String userRoles = null;
  
  
  private Boolean isAnonymousAllowed = null;
  
  
  private Object policyPartialMappings = null;
  
  
  private String description = null;

  
  /**
   * policy group id
   **/
  @ApiModelProperty(value = "policy group id")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   * throttling tier name
   **/
  @ApiModelProperty(value = "throttling tier name")
  @JsonProperty("throttlingTier")
  public String getThrottlingTier() {
    return throttlingTier;
  }
  public void setThrottlingTier(String throttlingTier) {
    this.throttlingTier = throttlingTier;
  }

  
  /**
   * visibility restricted user roles
   **/
  @ApiModelProperty(value = "visibility restricted user roles")
  @JsonProperty("userRoles")
  public String getUserRoles() {
    return userRoles;
  }
  public void setUserRoles(String userRoles) {
    this.userRoles = userRoles;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("isAnonymousAllowed")
  public Boolean getIsAnonymousAllowed() {
    return isAnonymousAllowed;
  }
  public void setIsAnonymousAllowed(Boolean isAnonymousAllowed) {
    this.isAnonymousAllowed = isAnonymousAllowed;
  }

  
  /**
   * list of xacml policy partial Id's relevant to policy group.
   **/
  @ApiModelProperty(value = "list of xacml policy partial Id's relevant to policy group.")
  @JsonProperty("policyPartialMappings")
  public Object getPolicyPartialMappings() {
    return policyPartialMappings;
  }
  public void setPolicyPartialMappings(Object policyPartialMappings) {
    this.policyPartialMappings = policyPartialMappings;
  }

  
  /**
   * brief description about policy group
   **/
  @ApiModelProperty(value = "brief description about policy group")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyGroupDTO {\n");
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  throttlingTier: ").append(throttlingTier).append("\n");
    sb.append("  userRoles: ").append(userRoles).append("\n");
    sb.append("  isAnonymousAllowed: ").append(isAnonymousAllowed).append("\n");
    sb.append("  policyPartialMappings: ").append(policyPartialMappings).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
