package org.wso2.carbon.appmgt.rest.api.store.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class FavouritePageDTO  {
  
  
  
  private Boolean isDefaultPage = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("isDefaultPage")
  public Boolean getIsDefaultPage() {
    return isDefaultPage;
  }
  public void setIsDefaultPage(Boolean isDefaultPage) {
    this.isDefaultPage = isDefaultPage;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class FavouritePageDTO {\n");
    
    sb.append("  isDefaultPage: ").append(isDefaultPage).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
