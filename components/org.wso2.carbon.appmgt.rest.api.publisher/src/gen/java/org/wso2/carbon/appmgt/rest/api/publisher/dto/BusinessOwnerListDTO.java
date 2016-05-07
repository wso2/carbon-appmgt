package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.BusinessOwnerDTO;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class BusinessOwnerListDTO  {
  
  
  
  private List<BusinessOwnerDTO> tierList = new ArrayList<BusinessOwnerDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tierList")
  public List<BusinessOwnerDTO> getTierList() {
    return tierList;
  }
  public void setTierList(List<BusinessOwnerDTO> tierList) {
    this.tierList = tierList;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class BusinessOwnerListDTO {\n");
    
    sb.append("  tierList: ").append(tierList).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
