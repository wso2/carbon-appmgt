package org.wso2.carbon.appmgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class PolicyPartialInfoDTO  {
  
  
  
  private Integer policyPartialId = null;
  
  
  private String policyPartialName = null;

  
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

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyPartialInfoDTO {\n");
    
    sb.append("  policyPartialId: ").append(policyPartialId).append("\n");
    sb.append("  policyPartialName: ").append(policyPartialName).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
