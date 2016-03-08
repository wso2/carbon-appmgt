

function writeLog(tenantID, userName, action, subject, subjectID, beforeChange, afterChange)
{
    var auditLog = Packages.org.wso2.carbon.appmgt.core.auditLog.AuditLogEvent;
    var auditLogger = Packages.org.wso2.carbon.appmgt.core.auditLog.AuditLogger;

    var auditLogEvent = new  Packages.org.wso2.carbon.appmgt.core.auditLog.AuditLogEvent(tenantID, userName, action, subject, subjectID, beforeChange, afterChange);
    auditLogger.writeLog(auditLogEvent);
}
