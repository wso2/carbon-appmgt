package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.LifeCycleHistoryDTO;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class LifeCycleHistoryListDTO  {
  
  
  
  private List<LifeCycleHistoryDTO> policyList = new ArrayList<LifeCycleHistoryDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("policyList")
  public List<LifeCycleHistoryDTO> getPolicyList() {
    return policyList;
  }
  public void setPolicyList(List<LifeCycleHistoryDTO> policyList) {
    this.policyList = policyList;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifeCycleHistoryListDTO {\n");
    
    sb.append("  policyList: ").append(policyList).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
