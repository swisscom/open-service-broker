package com.swisscom.cloud.sb.broker.backup

import com.swisscom.cloud.sb.broker.model.Backup
import com.swisscom.cloud.sb.broker.model.Restore
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.RestoreRepository
import com.swisscom.cloud.sb.broker.model.repository.BackupRepository
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@CompileStatic
class BackupPersistenceService {
    @Autowired
    private BackupRepository backupRepository
    @Autowired
    private RestoreRepository restoreRepository

    Backup createBackup(ServiceInstance serviceInstance, String guid) {
        def dateCreation = new Date()
        def backup = new Backup(serviceInstanceGuid: serviceInstance.guid, service: serviceInstance.plan.service,
                plan: serviceInstance.plan, guid: guid, dateRequested: dateCreation,
                dateUpdated: dateCreation, status: Backup.Status.INIT, operation: Backup.Operation.CREATE)
        backupRepository.save(backup)
        return backup
    }

    void deleteBackup(Backup backup) {
        backupRepository.delete(backup)
    }

    Collection<Backup> findAllBackupsForServiceInstance(ServiceInstance serviceInstance) {
        return backupRepository.findByServiceInstanceGuid(serviceInstance.guid)
    }

    Backup updateBackupOperation(Backup backup, Backup.Operation operation) {
        def backup1 = backupRepository.merge(backup)
        backup1.operation = operation
        backup1.status = Backup.Status.INIT
        backup1.dateUpdated = new Date()
        return backupRepository.save(backup1)
    }

    Backup findBackupByGuid(String guid) {
        return backupRepository.findByGuid(guid)
    }

    Restore createRestore(Backup backup, String guid) {
        def dateCreated = new Date()
        def restore = new Restore(guid: guid, dateRequested: dateCreated, dateUpdated: dateCreated, status: Backup.Status.INIT, backup: backup)
        return restoreRepository.save(restore)
    }

    Restore findRestoreByGuid(String guid) {
        return restoreRepository.findByGuid(guid)
    }

    void saveBackup(Backup backup) {
        def backup1 = backupRepository.merge(backup)
        backup1.dateUpdated = new Date()
        backupRepository.save(backup1)
    }

    void saveRestore(Restore restore) {
        def restore1 = restoreRepository.merge(restore)
        restore1.dateUpdated = new Date()
        restoreRepository.save(restore1)
    }
}