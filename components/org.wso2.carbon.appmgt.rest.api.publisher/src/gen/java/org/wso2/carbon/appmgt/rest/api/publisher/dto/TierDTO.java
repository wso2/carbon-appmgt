package org.wso2.carbon.appmgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class TierDTO  {
  
  
  
  private String tierName = null;
  
  
  private String tierDisplayName = null;
  
  
  private String tierDescription = null;
  
  
  private Integer tierSortKey = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tierName")
  public String getTierName() {
    return tierName;
  }
  public void setTierName(String tierName) {
    this.tierName = tierName;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tierDisplayName")
  public String getTierDisplayName() {
    return tierDisplayName;
  }
  public void setTierDisplayName(String tierDisplayName) {
    this.tierDisplayName = tierDisplayName;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tierDescription")
  public String getTierDescription() {
    return tierDescription;
  }
  public void setTierDescription(String tierDescription) {
    this.tierDescription = tierDescription;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tierSortKey")
  public Integer getTierSortKey() {
    return tierSortKey;
  }
  public void setTierSortKey(Integer tierSortKey) {
    this.tierSortKey = tierSortKey;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class TierDTO {\n");
    
    sb.append("  tierName: ").append(tierName).append("\n");
    sb.append("  tierDisplayName: ").append(tierDisplayName).append("\n");
    sb.append("  tierDescription: ").append(tierDescription).append("\n");
    sb.append("  tierSortKey: ").append(tierSortKey).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
