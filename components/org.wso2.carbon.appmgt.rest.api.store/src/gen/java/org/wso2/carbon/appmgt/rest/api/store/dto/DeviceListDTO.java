package org.wso2.carbon.appmgt.rest.api.store.dto;

import org.wso2.carbon.appmgt.rest.api.store.dto.DeviceInfoDTO;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class DeviceListDTO  {
  
  
  
  private Integer count = null;
  
  
  private String next = null;
  
  
  private String previous = null;
  
  
  private List<DeviceInfoDTO> deviceList = new ArrayList<DeviceInfoDTO>();

  
  /**
   * Number of Devices returned.
   **/
  @ApiModelProperty(value = "Number of Devices returned.")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  
  /**
   * Link to the next subset of resources qualified. \nEmpty if no more resources are to be returned.
   **/
  @ApiModelProperty(value = "Link to the next subset of resources qualified. \nEmpty if no more resources are to be returned.")
  @JsonProperty("next")
  public String getNext() {
    return next;
  }
  public void setNext(String next) {
    this.next = next;
  }

  
  /**
   * Link to the previous subset of resources qualified. \nEmpty if current subset is the first subset returned.
   **/
  @ApiModelProperty(value = "Link to the previous subset of resources qualified. \nEmpty if current subset is the first subset returned.")
  @JsonProperty("previous")
  public String getPrevious() {
    return previous;
  }
  public void setPrevious(String previous) {
    this.previous = previous;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("deviceList")
  public List<DeviceInfoDTO> getDeviceList() {
    return deviceList;
  }
  public void setDeviceList(List<DeviceInfoDTO> deviceList) {
    this.deviceList = deviceList;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeviceListDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  next: ").append(next).append("\n");
    sb.append("  previous: ").append(previous).append("\n");
    sb.append("  deviceList: ").append(deviceList).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
