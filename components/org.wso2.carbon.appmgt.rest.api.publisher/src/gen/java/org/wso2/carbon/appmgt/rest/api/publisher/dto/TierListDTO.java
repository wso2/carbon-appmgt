package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.TierDTO;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class TierListDTO  {
  
  
  
  private List<TierDTO> tierList = new ArrayList<TierDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tierList")
  public List<TierDTO> getTierList() {
    return tierList;
  }
  public void setTierList(List<TierDTO> tierList) {
    this.tierList = tierList;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class TierListDTO {\n");
    
    sb.append("  tierList: ").append(tierList).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
