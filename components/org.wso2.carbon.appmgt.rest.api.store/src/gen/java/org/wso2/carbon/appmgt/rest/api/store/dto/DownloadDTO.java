package org.wso2.carbon.appmgt.rest.api.store.dto;

import org.wso2.carbon.appmgt.rest.api.store.dto.UserListDTO;
import org.wso2.carbon.appmgt.rest.api.store.dto.DeviceListDTO;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class DownloadDTO  {
  
  
  
  private String type = null;
  
  
  private List<UserListDTO> users = new ArrayList<UserListDTO>();
  
  
  private List<DeviceListDTO> devices = new ArrayList<DeviceListDTO>();

  
  /**
   * Download type (either user or device).
   **/
  @ApiModelProperty(value = "Download type (either user or device).")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("users")
  public List<UserListDTO> getUsers() {
    return users;
  }
  public void setUsers(List<UserListDTO> users) {
    this.users = users;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("devices")
  public List<DeviceListDTO> getDevices() {
    return devices;
  }
  public void setDevices(List<DeviceListDTO> devices) {
    this.devices = devices;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class DownloadDTO {\n");
    
    sb.append("  type: ").append(type).append("\n");
    sb.append("  users: ").append(users).append("\n");
    sb.append("  devices: ").append(devices).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
