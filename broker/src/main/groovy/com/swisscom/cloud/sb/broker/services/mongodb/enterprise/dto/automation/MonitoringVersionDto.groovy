package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation

import groovy.transform.CompileStatic

@CompileStatic
class MonitoringVersionDto implements Serializable {
    String hostname
    String logPath
    LogRotateDto logRotate
}
