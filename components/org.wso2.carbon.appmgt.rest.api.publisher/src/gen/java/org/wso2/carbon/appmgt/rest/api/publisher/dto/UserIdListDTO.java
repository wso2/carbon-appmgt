package org.wso2.carbon.appmgt.rest.api.publisher.dto;

import org.wso2.carbon.appmgt.rest.api.publisher.dto.Object;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class UserIdListDTO  {
  
  
  
  private Object userIds = null;

  
  /**
   * List of User Names
   **/
  @ApiModelProperty(value = "List of User Names")
  @JsonProperty("userIds")
  public Object getUserIds() {
    return userIds;
  }
  public void setUserIds(Object userIds) {
    this.userIds = userIds;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserIdListDTO {\n");
    
    sb.append("  userIds: ").append(userIds).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
