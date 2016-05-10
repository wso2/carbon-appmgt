package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class LifeCycleDTO  {
  
  
  
  private String state = null;
  
  
  private List<String> actions = new ArrayList<String>();
  
  
  private Boolean async = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("state")
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("actions")
  public List<String> getActions() {
    return actions;
  }
  public void setActions(List<String> actions) {
    this.actions = actions;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("async")
  public Boolean getAsync() {
    return async;
  }
  public void setAsync(Boolean async) {
    this.async = async;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifeCycleDTO {\n");
    
    sb.append("  state: ").append(state).append("\n");
    sb.append("  actions: ").append(actions).append("\n");
    sb.append("  async: ").append(async).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
