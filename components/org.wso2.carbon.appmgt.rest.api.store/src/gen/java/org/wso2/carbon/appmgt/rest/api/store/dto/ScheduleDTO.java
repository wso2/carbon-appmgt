/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScheduleDTO {
  
  private String scheduleTime = null;
  private List<String> deviceIds = new ArrayList<String>();
  private String appId = null;

  /**
   **/
  public ScheduleDTO scheduleTime(String scheduleTime) {
    this.scheduleTime = scheduleTime;
    return this;
  }

  
  @ApiModelProperty(example = "04-30-2016 11:25 am", value = "")
  @JsonProperty("scheduleTime")
  public String getScheduleTime() {
    return scheduleTime;
  }
  public void setScheduleTime(String scheduleTime) {
    this.scheduleTime = scheduleTime;
  }

  /**
   * List of device Id's
   **/
  public ScheduleDTO deviceIds(List<String> deviceIds) {
    this.deviceIds = deviceIds;
    return this;
  }

  
  @ApiModelProperty(value = "List of device Id's")
  @JsonProperty("deviceIds")
  public List<String> getDeviceIds() {
    return deviceIds;
  }
  public void setDeviceIds(List<String> deviceIds) {
    this.deviceIds = deviceIds;
  }

  /**
   * Installing mobile app ID
   **/
  public ScheduleDTO appId(String appId) {
    this.appId = appId;
    return this;
  }

  
  @ApiModelProperty(example = "aff3b9ad-3d7b-4d8e-8bf9-600e86edf88c", value = "Installing mobile app ID")
  @JsonProperty("appId")
  public String getAppId() {
    return appId;
  }
  public void setAppId(String appId) {
    this.appId = appId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScheduleDTO schedule = (ScheduleDTO) o;
    return Objects.equals(scheduleTime, schedule.scheduleTime) &&
        Objects.equals(deviceIds, schedule.deviceIds) &&
        Objects.equals(appId, schedule.appId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scheduleTime, deviceIds, appId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Schedule {\n");
    
    sb.append("    scheduleTime: ").append(toIndentedString(scheduleTime)).append("\n");
    sb.append("    deviceIds: ").append(toIndentedString(deviceIds)).append("\n");
    sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

