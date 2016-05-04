package org.wso2.carbon.appmgt.rest.api.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;



@ApiModel(description = "")
public class EventsDTO  {
  
  
  @NotNull
  private List<Object> events = new ArrayList<Object>();

  
  /**
   * User hit count per App related User Stats details stream
   **/
  @ApiModelProperty(required = true, value = "User hit count per App related User Stats details stream")
  @JsonProperty("events")
  public List<Object> getEvents() {
    return events;
  }
  public void setEvents(List<Object> events) {
    this.events = events;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class EventsDTO {\n");
    
    sb.append("  events: ").append(events).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
