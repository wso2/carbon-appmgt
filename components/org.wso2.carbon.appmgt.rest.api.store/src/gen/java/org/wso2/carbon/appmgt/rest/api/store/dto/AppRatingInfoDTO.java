package org.wso2.carbon.appmgt.rest.api.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;



@ApiModel(description = "")
public class AppRatingInfoDTO  {
  
  
  
  private BigDecimal rating = null;
  
  
  private String review = null;
  
  
  private Integer id = null;

  
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

  
  /**
   * Comment/review about the app
   **/
  @ApiModelProperty(value = "Comment/review about the app")
  @JsonProperty("review")
  public String getReview() {
    return review;
  }
  public void setReview(String review) {
    this.review = review;
  }

  
  /**
   * Review Id
   **/
  @ApiModelProperty(value = "Review Id")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppRatingInfoDTO {\n");
    
    sb.append("  rating: ").append(rating).append("\n");
    sb.append("  review: ").append(review).append("\n");
    sb.append("  id: ").append(id).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
