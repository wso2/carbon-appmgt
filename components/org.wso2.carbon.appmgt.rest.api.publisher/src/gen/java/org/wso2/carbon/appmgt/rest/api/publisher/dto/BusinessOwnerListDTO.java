package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.BusinessOwnerDTO;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class BusinessOwnerListDTO  {
  
  
  
  private List<BusinessOwnerDTO> businessOwnerList = new ArrayList<BusinessOwnerDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("businessOwnerList")
  public List<BusinessOwnerDTO> getBusinessOwnerList() {
    return businessOwnerList;
  }
  public void setBusinessOwnerList(List<BusinessOwnerDTO> businessOwnerList) {
    this.businessOwnerList = businessOwnerList;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class BusinessOwnerListDTO {\n");
    
    sb.append("  businessOwnerList: ").append(businessOwnerList).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
