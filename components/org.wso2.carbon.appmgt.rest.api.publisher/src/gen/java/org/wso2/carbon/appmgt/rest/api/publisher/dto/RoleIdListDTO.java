package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.Object;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class RoleIdListDTO  {
  
  
  
  private Object roleIds = null;

  
  /**
   * List of Role Id's
   **/
  @ApiModelProperty(value = "List of Role Id's")
  @JsonProperty("roleIds")
  public Object getRoleIds() {
    return roleIds;
  }
  public void setRoleIds(Object roleIds) {
    this.roleIds = roleIds;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class RoleIdListDTO {\n");
    
    sb.append("  roleIds: ").append(roleIds).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
