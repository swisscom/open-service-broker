package com.swisscom.cf.broker.backup

import com.swisscom.cf.servicebroker.model.backup.BackupStatus
import spock.lang.Specification

import static com.swisscom.cf.broker.model.Backup.Operation.CREATE
import static com.swisscom.cf.broker.model.Backup.Operation.DELETE
import static com.swisscom.cf.broker.model.Backup.Status.*

class BackupStatusConverterSpec extends Specification {
    BackupStatusConverter backupStatusConverter = new BackupStatusConverter()

    def "conversion happy paths"() {
        expect:
        result == backupStatusConverter.convert(dbStatus, operation)
        where:
        operation | dbStatus    | result
        CREATE    | INIT        | BackupStatus.CREATE_IN_PROGRESS
        CREATE    | IN_PROGRESS | BackupStatus.CREATE_IN_PROGRESS
        CREATE    | FAILED      | BackupStatus.CREATE_FAILED
        CREATE    | SUCCESS     | BackupStatus.CREATE_SUCCEEDED
        DELETE    | INIT        | BackupStatus.DELETE_IN_PROGRESS
        DELETE    | IN_PROGRESS | BackupStatus.DELETE_IN_PROGRESS
        DELETE    | FAILED      | BackupStatus.DELETE_FAILED
        DELETE    | SUCCESS     | BackupStatus.DELETE_SUCCEEDED
    }
}
