package org.wso2.carbon.appmgt.rest.api.storeadmin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;



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
