package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;



@ApiModel(description = "")
public class StatSummaryDTO  {
  
  
  
  private List<Object> result = new ArrayList<Object>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("result")
  public List<Object> getResult() {
    return result;
  }
  public void setResult(List<Object> result) {
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
