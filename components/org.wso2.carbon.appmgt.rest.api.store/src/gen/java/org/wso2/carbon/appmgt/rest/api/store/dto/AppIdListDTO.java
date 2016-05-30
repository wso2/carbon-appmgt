package org.wso2.carbon.appmgt.rest.api.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;



@ApiModel(description = "")
public class AppIdListDTO  {
  
  
  
  private Object appIds = null;

  
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
    sb.append("class AppIdListDTO {\n");
    
    sb.append("  appIds: ").append(appIds).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
