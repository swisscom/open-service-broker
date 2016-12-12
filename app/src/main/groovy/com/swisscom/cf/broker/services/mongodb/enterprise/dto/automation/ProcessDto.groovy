package com.swisscom.cf.broker.services.mongodb.enterprise.dto.automation

class ProcessDto implements Serializable {
    String name
    String processType
    String version
    String hostname
    String cluster
    LogRotateDto logRotate
    int authSchemaVersion
    ProcessArgumentsV26Dto args2_6
}
