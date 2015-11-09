/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.appmgt.usage.client.dto;

import java.util.ArrayList;
import java.util.List;

public class AppHitsStatsDTO {
    private String appName;
    private String version;
    private int totalHitCount;
    private String uuid;
    private List<UserHitsPerAppDTO> userHitsList;

    public AppHitsStatsDTO() {
        userHitsList = new ArrayList<UserHitsPerAppDTO>();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<UserHitsPerAppDTO> getUserHitsList() {
        return userHitsList;
    }

    public void setUserHitsList(List<UserHitsPerAppDTO> userHitsList) {
        this.userHitsList = userHitsList;
    }

    public int getTotalHitCount() {
        return totalHitCount;
    }

    public void setTotalHitCount(int totalHitCount) {
        this.totalHitCount = totalHitCount;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

}
