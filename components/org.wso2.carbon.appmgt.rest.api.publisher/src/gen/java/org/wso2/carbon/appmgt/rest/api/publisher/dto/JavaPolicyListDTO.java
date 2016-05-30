package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.JavaPolicyDTO;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class JavaPolicyListDTO  {
  
  
  
  private List<JavaPolicyDTO> policyList = new ArrayList<JavaPolicyDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("policyList")
  public List<JavaPolicyDTO> getPolicyList() {
    return policyList;
  }
  public void setPolicyList(List<JavaPolicyDTO> policyList) {
    this.policyList = policyList;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class JavaPolicyListDTO {\n");
    
    sb.append("  policyList: ").append(policyList).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
