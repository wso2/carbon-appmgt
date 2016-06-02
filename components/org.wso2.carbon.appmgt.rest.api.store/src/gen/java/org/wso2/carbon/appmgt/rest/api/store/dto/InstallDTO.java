package org.wso2.carbon.appmgt.rest.api.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;



@ApiModel(description = "")
public class InstallDTO  {
  
  
  
  private String type = null;
  
  
  private List<String> deviceIds = new ArrayList<String>();
  
  
  private String appId = null;

  
  /**
   * Download type (either user or devices).
   **/
  @ApiModelProperty(value = "Download type (either user or devices).")
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
  public List<String> getDeviceIds() {
    return deviceIds;
  }
  public void setDeviceIds(List<String> deviceIds) {
    this.deviceIds = deviceIds;
  }

  
  /**
   * Installing mobile app ID
   **/
  @ApiModelProperty(value = "Installing mobile app ID")
  @JsonProperty("appId")
  public String getAppId() {
    return appId;
  }
  public void setAppId(String appId) {
    this.appId = appId;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class InstallDTO {\n");
    
    sb.append("  type: ").append(type).append("\n");
    sb.append("  deviceIds: ").append(deviceIds).append("\n");
    sb.append("  appId: ").append(appId).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
