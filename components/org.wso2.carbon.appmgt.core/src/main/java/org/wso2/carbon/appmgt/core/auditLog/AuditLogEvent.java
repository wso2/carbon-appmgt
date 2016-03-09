
/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appmgt.core.auditLog;

import java.io.Serializable;

/**
 * Stores details of a log temporary.
 */
public class AuditLogEvent implements Serializable {

    private String action;
    private String username;
    private String tenantId;
    private String subjectId;
    private String subject;
    private Object beforeChange;
    private Object afterChange;

    /**
     * Constructor of the class to create a instant of a bean.

     * @param tenantId
     * @param username
     * @param action
     * @param subject
     * @param subjectId
     * @param beforeChange
     * @param afterChange
     */
    public AuditLogEvent(String tenantId, String username, String action, String subject, String subjectId,
                         Object beforeChange,
                         Object afterChange) {
        this.username = username;
        this.afterChange = afterChange;
        this.action = action;
        this.beforeChange = beforeChange;
        this.subject = subject;
        this.subjectId = subjectId;
        this.tenantId = tenantId;

    }

    /**
     * Default Constructor.
     */
    public AuditLogEvent() {

    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Object getBeforeChange() {
        return beforeChange;
    }

    public void setBeforeChange(Object beforeChange) {
        this.beforeChange = beforeChange;
    }

    public Object getAfterChange() {
        return afterChange;
    }

    public void setAfterChange(Object afterChange) {
        this.afterChange = afterChange;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
