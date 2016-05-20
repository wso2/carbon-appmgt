package org.wso2.carbon.appmgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class LifeCycleHistoryDTO  {
  
  
  
  private Integer order = null;
  
  
  private String state = null;
  
  
  private String targetState = null;
  
  
  private String timestamp = null;
  
  
  private String user = null;

  
  /**
   * Sequence order Id.
   **/
  @ApiModelProperty(value = "Sequence order Id.")
  @JsonProperty("order")
  public Integer getOrder() {
    return order;
  }
  public void setOrder(Integer order) {
    this.order = order;
  }

  
  /**
   * Current Lifecycle state.
   **/
  @ApiModelProperty(value = "Current Lifecycle state.")
  @JsonProperty("state")
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }

  
  /**
   * Next/target Lifecycle state.
   **/
  @ApiModelProperty(value = "Next/target Lifecycle state.")
  @JsonProperty("targetState")
  public String getTargetState() {
    return targetState;
  }
  public void setTargetState(String targetState) {
    this.targetState = targetState;
  }

  
  /**
   * Executed time.
   **/
  @ApiModelProperty(value = "Executed time.")
  @JsonProperty("timestamp")
  public String getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  
  /**
   * Executed user.
   **/
  @ApiModelProperty(value = "Executed user.")
  @JsonProperty("user")
  public String getUser() {
    return user;
  }
  public void setUser(String user) {
    this.user = user;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifeCycleHistoryDTO {\n");
    
    sb.append("  order: ").append(order).append("\n");
    sb.append("  state: ").append(state).append("\n");
    sb.append("  targetState: ").append(targetState).append("\n");
    sb.append("  timestamp: ").append(timestamp).append("\n");
    sb.append("  user: ").append(user).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
