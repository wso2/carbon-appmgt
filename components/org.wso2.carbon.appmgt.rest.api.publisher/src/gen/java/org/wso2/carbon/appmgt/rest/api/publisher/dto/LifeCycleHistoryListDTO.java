package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;



@ApiModel(description = "")
public class LifeCycleHistoryListDTO  {
  
  
  
  private List<LifeCycleHistoryDTO> lifeCycleHistoryList = new ArrayList<LifeCycleHistoryDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("lifeCycleHistoryList")
  public List<LifeCycleHistoryDTO> getLifeCycleHistoryList() {
    return lifeCycleHistoryList;
  }
  public void setLifeCycleHistoryList(List<LifeCycleHistoryDTO> lifeCycleHistoryList) {
    this.lifeCycleHistoryList = lifeCycleHistoryList;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifeCycleHistoryListDTO {\n");
    
    sb.append("  lifeCycleHistoryList: ").append(lifeCycleHistoryList).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
