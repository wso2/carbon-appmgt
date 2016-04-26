package org.wso2.carbon.appmgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class PolicyPartialDTO  {
  
  
  
  private Integer policyPartialId = null;
  
  
  private String policyPartialName = null;
  
  @NotNull
  private String policyPartial = null;
  
  
  private Boolean isSharedPartial = null;
  
  
  private String policyPartialDesc = null;

  
  /**
   * Id of the policy
   **/
  @ApiModelProperty(value = "Id of the policy")
  @JsonProperty("policyPartialId")
  public Integer getPolicyPartialId() {
    return policyPartialId;
  }
  public void setPolicyPartialId(Integer policyPartialId) {
    this.policyPartialId = policyPartialId;
  }

  
  /**
   * name of the policy
   **/
  @ApiModelProperty(value = "name of the policy")
  @JsonProperty("policyPartialName")
  public String getPolicyPartialName() {
    return policyPartialName;
  }
  public void setPolicyPartialName(String policyPartialName) {
    this.policyPartialName = policyPartialName;
  }

  
  /**
   * policy content
   **/
  @ApiModelProperty(required = true, value = "policy content")
  @JsonProperty("policyPartial")
  public String getPolicyPartial() {
    return policyPartial;
  }
  public void setPolicyPartial(String policyPartial) {
    this.policyPartial = policyPartial;
  }

  
  /**
   * whether shared or not
   **/
  @ApiModelProperty(value = "whether shared or not")
  @JsonProperty("isSharedPartial")
  public Boolean getIsSharedPartial() {
    return isSharedPartial;
  }
  public void setIsSharedPartial(Boolean isSharedPartial) {
    this.isSharedPartial = isSharedPartial;
  }

  
  /**
   * description about the policy
   **/
  @ApiModelProperty(value = "description about the policy")
  @JsonProperty("policyPartialDesc")
  public String getPolicyPartialDesc() {
    return policyPartialDesc;
  }
  public void setPolicyPartialDesc(String policyPartialDesc) {
    this.policyPartialDesc = policyPartialDesc;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyPartialDTO {\n");
    
    sb.append("  policyPartialId: ").append(policyPartialId).append("\n");
    sb.append("  policyPartialName: ").append(policyPartialName).append("\n");
    sb.append("  policyPartial: ").append(policyPartial).append("\n");
    sb.append("  isSharedPartial: ").append(isSharedPartial).append("\n");
    sb.append("  policyPartialDesc: ").append(policyPartialDesc).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
