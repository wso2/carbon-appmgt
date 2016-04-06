package org.wso2.carbon.appmgt.rest.api.storeadmin.dto;

import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.UserInfoDTO;
import java.util.*;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.RoleInfoDTO;
import org.wso2.carbon.appmgt.rest.api.storeadmin.dto.AppInfoDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class InstallDTO  {
  
  
  
  private String type = null;
  
  
  private List<UserInfoDTO> users = new ArrayList<UserInfoDTO>();
  
  
  private List<RoleInfoDTO> roles = new ArrayList<RoleInfoDTO>();
  
  
  private List<AppInfoDTO> apps = new ArrayList<AppInfoDTO>();

  
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
  public List<UserInfoDTO> getUsers() {
    return users;
  }
  public void setUsers(List<UserInfoDTO> users) {
    this.users = users;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("roles")
  public List<RoleInfoDTO> getRoles() {
    return roles;
  }
  public void setRoles(List<RoleInfoDTO> roles) {
    this.roles = roles;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("apps")
  public List<AppInfoDTO> getApps() {
    return apps;
  }
  public void setApps(List<AppInfoDTO> apps) {
    this.apps = apps;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class InstallDTO {\n");
    
    sb.append("  type: ").append(type).append("\n");
    sb.append("  users: ").append(users).append("\n");
    sb.append("  roles: ").append(roles).append("\n");
    sb.append("  apps: ").append(apps).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
