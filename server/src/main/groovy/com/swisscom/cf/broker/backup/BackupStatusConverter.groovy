package com.swisscom.cf.broker.backup

import com.swisscom.cf.broker.model.Backup
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@CompileStatic
@Component
class BackupStatusConverter {
    BackupStatus convert(Backup.Status status, Backup.Operation operation) {
        if (Backup.Operation.CREATE == operation) {
            parseCreateOperationStatus(status)
        } else if (Backup.Operation.DELETE == operation) {
            parseDeleteOperationStatus(status)
        } else {
            throw new RuntimeException("Unknown Backup operation:${operation}")
        }
    }

    private BackupStatus parseCreateOperationStatus(Backup.Status status) {
        switch (status) {
            case Backup.Status.INIT:
            case Backup.Status.IN_PROGRESS:
                return BackupStatus.CREATE_IN_PROGRESS
            case Backup.Status.SUCCESS:
                return BackupStatus.CREATE_SUCCEEDED
            case Backup.Status.FAILED:
                return BackupStatus.CREATE_FAILED
            default:
                throw new RuntimeException("Unknown status:${status}")
        }
    }

    private BackupStatus parseDeleteOperationStatus(Backup.Status status) {
        switch (status) {
            case Backup.Status.INIT:
            case Backup.Status.IN_PROGRESS:
                return BackupStatus.DELETE_IN_PROGRESS
            case Backup.Status.SUCCESS:
                return BackupStatus.DELETE_SUCCEEDED
            case Backup.Status.FAILED:
                return BackupStatus.DELETE_FAILED
            default:
                throw new RuntimeException("Unknown status:${status}")
        }
    }

}
