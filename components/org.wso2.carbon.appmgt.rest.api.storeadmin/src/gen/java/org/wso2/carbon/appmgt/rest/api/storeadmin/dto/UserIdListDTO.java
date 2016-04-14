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
