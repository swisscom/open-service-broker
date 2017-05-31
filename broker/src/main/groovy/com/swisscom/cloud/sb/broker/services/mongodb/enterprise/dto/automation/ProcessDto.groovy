package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation

class ProcessDto implements Serializable {
    String name
    String processType
    String version
    String hostname
    String cluster
    LogRotateDto logRotate
    int authSchemaVersion
    String featureCompatibilityVersion
    ProcessArgumentsV26Dto args2_6
}
