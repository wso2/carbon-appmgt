package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;



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
