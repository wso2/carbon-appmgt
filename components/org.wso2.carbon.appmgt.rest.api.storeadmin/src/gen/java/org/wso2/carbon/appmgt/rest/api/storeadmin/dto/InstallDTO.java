/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.rest.api.storeadmin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;



@ApiModel(description = "")
public class InstallDTO  {
  
  
  
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
    sb.append("class InstallDTO {\n");
    
    sb.append("  type: ").append(type).append("\n");
    sb.append("  typeIds: ").append(typeIds).append("\n");
    sb.append("  appId: ").append(appId).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
