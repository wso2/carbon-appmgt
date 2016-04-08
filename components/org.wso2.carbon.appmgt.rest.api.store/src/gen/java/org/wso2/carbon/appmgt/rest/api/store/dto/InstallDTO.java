package org.wso2.carbon.appmgt.rest.api.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;



@ApiModel(description = "")
public class InstallDTO  {
  
  
  
  private String type = null;
  
  
  private Object deviceIds = null;
  
  
  private Object appIds = null;

  
  /**
   * Download type (either use or devices).
   **/
  @ApiModelProperty(value = "Download type (either use or devices).")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  
  /**
   * List of device Id's
   **/
  @ApiModelProperty(value = "List of device Id's")
  @JsonProperty("deviceIds")
  public Object getDeviceIds() {
    return deviceIds;
  }
  public void setDeviceIds(Object deviceIds) {
    this.deviceIds = deviceIds;
  }

  
  /**
   * List of App Id's
   **/
  @ApiModelProperty(value = "List of App Id's")
  @JsonProperty("appIds")
  public Object getAppIds() {
    return appIds;
  }
  public void setAppIds(Object appIds) {
    this.appIds = appIds;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class InstallDTO {\n");
    
    sb.append("  type: ").append(type).append("\n");
    sb.append("  deviceIds: ").append(deviceIds).append("\n");
    sb.append("  appIds: ").append(appIds).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
