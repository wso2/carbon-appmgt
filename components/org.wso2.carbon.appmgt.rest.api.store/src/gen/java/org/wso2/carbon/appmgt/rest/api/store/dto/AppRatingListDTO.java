package org.wso2.carbon.appmgt.rest.api.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;



@ApiModel(description = "")
public class AppRatingListDTO  {
  
  
  
  private Integer count = null;
  
  
  private String next = null;
  
  
  private String previous = null;
  
  
  private BigDecimal overallRating = null;
  
  
  private List<AppRatingInfoDTO> ratingDetails = new ArrayList<AppRatingInfoDTO>();

  
  /**
   * Number of Ratings returned.
   **/
  @ApiModelProperty(value = "Number of Ratings returned.")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  
  /**
   * Link to the next subset of resources qualified.\nEmpty if no more resources are to be returned.
   **/
  @ApiModelProperty(value = "Link to the next subset of resources qualified.\nEmpty if no more resources are to be returned.")
  @JsonProperty("next")
  public String getNext() {
    return next;
  }
  public void setNext(String next) {
    this.next = next;
  }

  
  /**
   * Link to the previous subset of resources qualified.\nEmpty if current subset is the first subset returned.
   **/
  @ApiModelProperty(value = "Link to the previous subset of resources qualified.\nEmpty if current subset is the first subset returned.")
  @JsonProperty("previous")
  public String getPrevious() {
    return previous;
  }
  public void setPrevious(String previous) {
    this.previous = previous;
  }

  
  /**
   * List of Role Id's
   **/
  @ApiModelProperty(value = "List of Role Id's")
  @JsonProperty("overallRating")
  public BigDecimal getOverallRating() {
    return overallRating;
  }
  public void setOverallRating(BigDecimal overallRating) {
    this.overallRating = overallRating;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("ratingDetails")
  public List<AppRatingInfoDTO> getRatingDetails() {
    return ratingDetails;
  }
  public void setRatingDetails(List<AppRatingInfoDTO> ratingDetails) {
    this.ratingDetails = ratingDetails;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppRatingListDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  next: ").append(next).append("\n");
    sb.append("  previous: ").append(previous).append("\n");
    sb.append("  overallRating: ").append(overallRating).append("\n");
    sb.append("  ratingDetails: ").append(ratingDetails).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
