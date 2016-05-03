package org.wso2.carbon.appmgt.rest.api.store.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;



@ApiModel(description = "")
public class EventsDTO  {
  
  
  
  private Object events = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("events")
  public Object getEvents() {
    return events;
  }
  public void setEvents(Object events) {
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
