package org.wso2.carbon.appmgt.rest.api.store.dto;

import java.math.BigDecimal;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class AppRatingInfoDTO  {
  
  
  
  private BigDecimal rating = null;

  
  /**
   * List of Role Id's
   **/
  @ApiModelProperty(value = "List of Role Id's")
  @JsonProperty("rating")
  public BigDecimal getRating() {
    return rating;
  }
  public void setRating(BigDecimal rating) {
    this.rating = rating;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppRatingInfoDTO {\n");
    
    sb.append("  rating: ").append(rating).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
