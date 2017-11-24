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
package org.wso2.carbon.appmgt.api.dto;

/**
 * Store user name and no of hits per an app.
 */
public class UserHitsPerAppDTO {
    private String context;
    private String userName;
    private int userHitsCount;

    /**
     * Get Context.
     * @return context String.
     */
    public String getContext() {
        return context;
    }

    /**
     * Set Context.
     * @param context String.
     */
    public void setContext(String context) {
        this.context = context;
    }
    /**
     * Get User Name.
     * @return userName String.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set User Name.
     * @param userName String.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Get User Hits count per app.
     * @return no of user hits int.
     */
    public int getUserHitsCount() {
        return userHitsCount;
    }

    /**
     * Set User Hits count per app.
     * @param userHitsCount int.
     */
    public void setUserHitsCount(int userHitsCount) {
        this.userHitsCount = userHitsCount;
    }

}
