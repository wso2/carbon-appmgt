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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/

package org.wso2.carbon.appmgt.usage.client.pojo;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.appmgt.usage.client.APIUsageStatisticsClientConstants;

import javax.xml.namespace.QName;

public class AppFirstAccess {
    private String year;
    private String month;
    private String day;

    public AppFirstAccess(OMElement row) {
        year = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.YEAR)).getText();
        month = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.MONTH)).getText();
        day = row.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.DAY)).getText();
    }

    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public String getDay() {
        return day;
    }
}
