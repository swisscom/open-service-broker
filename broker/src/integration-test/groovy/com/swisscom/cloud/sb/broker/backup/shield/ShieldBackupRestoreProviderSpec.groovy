package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.BaseTransactionalSpecification
import com.swisscom.cloud.sb.broker.backup.shield.dto.JobStatus
import com.swisscom.cloud.sb.broker.model.Backup
import com.swisscom.cloud.sb.broker.model.Restore
import com.swisscom.cloud.sb.broker.model.ServiceInstance

class ShieldBackupRestoreProviderSpec extends BaseTransactionalSpecification {
    class DummyShieldBackupRestoreProvider implements ShieldBackupRestoreProvider {
        DummyShieldBackupRestoreProvider() {
            target = new DummyTarget()
        }

        class DummyTarget implements ShieldTarget {
            @Override
            String pluginName() { "doesntmatter" }

            @Override
            String endpointJson() { "{}" }
        }

        DummyTarget target

        @Override
        ShieldTarget targetForBackup(Backup backup) { target }
    }

    DummyShieldBackupRestoreProvider shieldBackupRestoreProvider
    ShieldClient_2_3_5 shieldClient

    def setup() {
        shieldClient = Mock(ShieldClient_2_3_5)
        shieldBackupRestoreProvider = new DummyShieldBackupRestoreProvider()
        shieldBackupRestoreProvider.shieldClient = shieldClient
    }

    def "create backup"() {
        given:
        String taskId = 'task-uuid'
        Backup backup = new Backup(serviceInstanceGuid: 'service-guid', operation: Backup.Operation.CREATE)
        shieldClient.registerAndRunJob(backup.serviceInstanceGuid, shieldBackupRestoreProvider.target) >> taskId
        when:
        def externalId = shieldBackupRestoreProvider.createBackup(backup)
        then:
        externalId == taskId
    }

    def "delete backup"() {
        given:
        Backup backup = new Backup(serviceInstanceGuid: 'service-guid', operation: Backup.Operation.DELETE, externalId: 'externalId')
        1 * shieldClient.deleteBackup(backup.externalId)
        when:
        shieldBackupRestoreProvider.deleteBackup(backup)
        then:
        noExceptionThrown()
    }

    def "getting backup status of a deletion job will never fail"() {
        given:
        Backup backup = new Backup(serviceInstanceGuid: 'service-guid', operation: Backup.Operation.DELETE, externalId: "doesntmatter")
        shieldClient.getJobStatus(backup.externalId) >> JobStatus.FAILED
        when:
        Backup.Status status = shieldBackupRestoreProvider.getBackupStatus(backup)
        then:
        status == Backup.Status.SUCCESS
    }

    def "happy path: get backup status"() {
        given:
        String taskId = 'task-uuid'
        Backup backup = new Backup(serviceInstanceGuid: 'service-guid', operation: Backup.Operation.CREATE, externalId: taskId)
        shieldClient.getJobStatus(backup.externalId) >> JobStatus.FINISHED
        when:
        Backup.Status status = shieldBackupRestoreProvider.getBackupStatus(backup)
        then:
        status == Backup.Status.SUCCESS
    }

    def "get backup status when 404 occurs on backup creation (error)"() {
        given:
        String taskId = 'task-uuid'
        Backup backup = new Backup(serviceInstanceGuid: 'service-guid', operation: Backup.Operation.CREATE, externalId: taskId)
        shieldClient.getJobStatus(backup.externalId) >> {throw new ShieldResourceNotFoundException("Doesntmatter")}
        when:
        Backup.Status status = shieldBackupRestoreProvider.getBackupStatus(backup)
        then:
        def ex = thrown(ShieldResourceNotFoundException)
        ex.message == 'Doesntmatter'
    }

    def "get backup status when 404 occurs on backup deletion (is ok)"() {
        given:
        String taskId = 'task-uuid'
        Backup backup = new Backup(serviceInstanceGuid: 'service-guid', operation: Backup.Operation.DELETE, externalId: taskId)
        shieldClient.getJobStatus(backup.externalId) >> {throw new ShieldResourceNotFoundException("Doesntmatter")}
        when:
        Backup.Status status = shieldBackupRestoreProvider.getBackupStatus(backup)
        then:
        status == Backup.Status.SUCCESS
    }

    def "restore backup"() {
        given:
        String taskId = 'task-uuid'
        Backup backup = new Backup(serviceInstanceGuid: 'service-guid', operation: Backup.Operation.CREATE, externalId: 'not-important')
        Restore restore = new Restore(backup: backup)
        shieldClient.restore(restore.backup.externalId) >> taskId
        when:
        def externalId = shieldBackupRestoreProvider.restoreBackup(restore)
        then:
        externalId == taskId
    }

    def "get restore status"() {
        given:
        String taskId = 'task-uuid'
        Restore restore = new Restore(externalId: taskId)
        shieldClient.getJobStatus(restore.externalId) >> JobStatus.FINISHED
        when:
        Backup.Status status = shieldBackupRestoreProvider.getRestoreStatus(restore)
        then:
        status == Backup.Status.SUCCESS
    }

    def "notify service instance deletion"() {
        given:
        ServiceInstance instance = new ServiceInstance(guid: 'service-id')
        1 * shieldClient.deleteJobsAndBackups('service-id')
        when:
        shieldBackupRestoreProvider.notifyServiceInstanceDeletion(instance)
        then:
        noExceptionThrown()
    }

    def "convert backup status"() {
        expect:
        DummyShieldBackupRestoreProvider.convertBackupStatus(jobStatus) == backupStatus
        where:
        jobStatus          | backupStatus
        JobStatus.RUNNING  | Backup.Status.IN_PROGRESS
        JobStatus.FAILED   | Backup.Status.FAILED
        JobStatus.FINISHED | Backup.Status.SUCCESS
    }
}
