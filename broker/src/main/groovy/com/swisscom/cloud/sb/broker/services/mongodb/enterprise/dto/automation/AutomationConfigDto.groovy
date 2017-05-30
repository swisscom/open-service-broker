package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation

import groovy.transform.CompileStatic

@CompileStatic
class AutomationConfigDto implements Serializable {
    List<MonitoringVersionDto> monitoringVersions
    List<BackupVersionDto> backupVersions
    List<ProcessDto> processes
    List<MongoDbVersionDto> mongoDbVersions
    List<ReplicaSetDto> replicaSets
    AuthenticationDto auth
    Map options
    transient int version
}
