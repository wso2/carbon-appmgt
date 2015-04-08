/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.impl.discovery;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * List response for the JAX-RS web service
 *
 */
public class DiscoveredApplicationListDTO {

    private int pageCount;
    private int currentPage;
    private String authCookie;
    private int totalNumberOfResults;

    @XmlElement
    private List<DiscoveredApplicationListElementDTO> applicationList;

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public String getAuthCookie() {
        return authCookie;
    }

    public void setAuthCookie(String authCookie) {
        this.authCookie = authCookie;
    }

    public int getTotalNumberOfResults() {
        return totalNumberOfResults;
    }

    public void setTotalNumberOfResults(int totalNumberOfResults) {
        this.totalNumberOfResults = totalNumberOfResults;
    }

    public List<DiscoveredApplicationListElementDTO> getApplicationList() {
        return applicationList;
    }

    public void setApplicationList(List<DiscoveredApplicationListElementDTO> applicationList) {
        this.applicationList = applicationList;
    }
}
