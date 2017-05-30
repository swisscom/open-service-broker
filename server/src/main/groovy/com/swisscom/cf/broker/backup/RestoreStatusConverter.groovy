package com.swisscom.cf.broker.backup

import com.swisscom.cf.broker.model.Backup
import com.swisscom.cloud.sb.model.backup.RestoreStatus
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@CompileStatic
@Component
class RestoreStatusConverter {
    RestoreStatus convert(Backup.Status status) {
        switch (status) {
            case Backup.Status.INIT:
            case Backup.Status.IN_PROGRESS:
                return RestoreStatus.IN_PROGRESS
            case Backup.Status.SUCCESS:
                return RestoreStatus.SUCCEEDED
            case Backup.Status.FAILED:
                return RestoreStatus.FAILED
            default:
                throw new RuntimeException("Unknown status:${status}")
        }
    }
}
