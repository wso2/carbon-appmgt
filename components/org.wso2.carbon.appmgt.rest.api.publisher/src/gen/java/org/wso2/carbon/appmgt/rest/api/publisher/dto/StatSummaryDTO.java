package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class StatSummaryDTO  {
  
  
  
  private List<String> result = new ArrayList<String>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("result")
  public List<String> getResult() {
    return result;
  }
  public void setResult(List<String> result) {
    this.result = result;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class StatSummaryDTO {\n");
    
    sb.append("  result: ").append(result).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
