package org.wso2.carbon.appmgt.rest.api.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;



@ApiModel(description = "")
public class AdminInstallDTO  {
  
  
  
  private String type = null;
  
  
  private Object typeIds = null;
  
  
  private String appId = null;

  
  /**
   * Download type (either user or roles).
   **/
  @ApiModelProperty(value = "Download type (either user or roles).")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  
  /**
   * List of Id's against type (role/user)
   **/
  @ApiModelProperty(value = "List of Id's against type (role/user)")
  @JsonProperty("typeIds")
  public Object getTypeIds() {
    return typeIds;
  }
  public void setTypeIds(Object typeIds) {
    this.typeIds = typeIds;
  }

  
  /**
   * App Id
   **/
  @ApiModelProperty(value = "App Id")
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
    sb.append("class AdminInstallDTO {\n");
    
    sb.append("  type: ").append(type).append("\n");
    sb.append("  typeIds: ").append(typeIds).append("\n");
    sb.append("  appId: ").append(appId).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
