package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;



@ApiModel(description = "")
public class TierListDTO  {
  
  
  
  private Object roleIds = null;

  
  /**
   * List of Tiers
   **/
  @ApiModelProperty(value = "List of Tiers")
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
    sb.append("class TierListDTO {\n");
    
    sb.append("  roleIds: ").append(roleIds).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
