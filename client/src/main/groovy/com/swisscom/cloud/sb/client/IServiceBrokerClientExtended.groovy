package com.swisscom.cloud.sb.client

import com.swisscom.cloud.sb.model.backup.BackupDto
import com.swisscom.cloud.sb.model.backup.RestoreDto
import com.swisscom.cloud.sb.model.endpoint.Endpoint
import com.swisscom.cloud.sb.model.health.ServiceHealth
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import groovy.transform.CompileStatic
import org.springframework.http.ResponseEntity

@CompileStatic
interface IServiceBrokerClientExtended extends IServiceBrokerClient {
    ResponseEntity<Endpoint> getEndpoint(String serviceInstanceId)

    ResponseEntity<ServiceUsage> getUsage(String serviceInstanceId)

    ResponseEntity<Set<ServiceUsage>> getExtendedUsage(String serviceInstanceId)

    ResponseEntity<ServiceHealth> getHealth(String serviceInstanceId)

    ResponseEntity<Void> createOrUpdateServiceDefinition(String definition)

    ResponseEntity<Void> deleteServiceDefinition(String definition)

    ResponseEntity<BackupDto> createBackup(String serviceInstanceId)

    ResponseEntity<String> deleteBackup(String serviceInstanceId, String backupId)

    ResponseEntity<BackupDto> getBackup(String serviceInstanceId, String backupId)

    ResponseEntity<List<BackupDto>> listBackups(String serviceInstanceId)

    ResponseEntity<RestoreDto> restoreBackup(String serviceInstanceId, String backupId)

    ResponseEntity<RestoreDto> getRestoreStatus(String serviceInstanceId, String backupId, String restore_id)

    ResponseEntity<String> getApi(String serviceInstanceId)

    ResponseEntity<String> lockUser(String serviceInstanceId)

    ResponseEntity<String> unlockUser(String serviceInstanceId)

}
