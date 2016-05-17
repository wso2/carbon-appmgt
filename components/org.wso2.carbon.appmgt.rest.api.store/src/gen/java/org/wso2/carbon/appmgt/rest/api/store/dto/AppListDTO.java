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

package org.wso2.carbon.appmgt.rest.api.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;
import java.util.List;


@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@ApiModel(description = "")
public class AppListDTO {

  private Integer count = null;
  
  
  private String next = null;
  
  
  private String previous = null;

  private List<AppDTO> appList = new ArrayList<AppDTO>();
  private List<AppInfoDTO> appInfoList = new ArrayList<AppInfoDTO>();


  /**
   * Number of App returned.
   **/
  @ApiModelProperty(value = "Number of App returned.")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }


  /**
   * Link to the next subset of resources qualified. \nEmpty if no more resources are to be returned.
   **/
  @ApiModelProperty(value = "Link to the next subset of resources qualified. \nEmpty if no more resources are to be returned.")
  @JsonProperty("next")
  public String getNext() {
    return next;
  }
  public void setNext(String next) {
    this.next = next;
  }


  /**
   * Link to the previous subset of resources qualified. \nEmpty if current subset is the first subset returned.
   **/
  @ApiModelProperty(value = "Link to the previous subset of resources qualified. \nEmpty if current subset is the first subset returned.")
  @JsonProperty("previous")
  public String getPrevious() {
    return previous;
  }
  public void setPrevious(String previous) {
    this.previous = previous;
  }


  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("appInfoList")
  public List<AppInfoDTO> getAppInfoList() {
    return appInfoList;
  }
  public void setAppInfoList(List<AppInfoDTO> appInfoList) {
    this.appInfoList = appInfoList;
  }

  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("appList")
  public List<AppDTO> getAppList() {
    return appList;
  }
  public void setAppList(List<AppDTO> appList) {
    this.appList = appList;
  }



  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppListDTO {\n");

    sb.append("  count: ").append(count).append("\n");
    sb.append("  next: ").append(next).append("\n");
    sb.append("  previous: ").append(previous).append("\n");
    sb.append("  appList: ").append(appList).append("\n");
    sb.append("  appInfoList: ").append(appInfoList).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
