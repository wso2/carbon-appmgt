
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

import org.apache.commons.logging.Log;
import org.wso2.carbon.CarbonConstants;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Write logs to the audit Log.
 */
public class AuditLogger {
    private static final Log auditLogger = CarbonConstants.AUDIT_LOG;

    /**
     * Get a auditLogEvent as parameters and write details to the audit Log.
     * @param auditLogEvent
     */
    public static void writeLog(AuditLogEvent auditLogEvent) {
        Date curDate = new Date();
        SimpleDateFormat format;
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        String dateAndTime = format.format(curDate);
        String log = "\"Time\" : \"" + dateAndTime + " \", \"Action\" : \"" + auditLogEvent.getAction() +
                " \",\"TenantID\" : \"" + auditLogEvent.getTenantId() + "\",\"UserName\" : \"" +
                auditLogEvent.getUsername() + " \", \"Subject\" : \"" +
                auditLogEvent.getSubject() + "\", \"SubjectID\" : \"" + auditLogEvent.getSubjectId() + "\"";
        auditLogger.info(log);
    }
}
