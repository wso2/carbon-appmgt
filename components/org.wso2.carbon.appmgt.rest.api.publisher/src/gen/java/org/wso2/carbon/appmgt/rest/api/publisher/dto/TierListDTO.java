package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;



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
