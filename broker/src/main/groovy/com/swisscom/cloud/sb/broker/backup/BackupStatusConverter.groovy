/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.backup

import com.swisscom.cloud.sb.broker.model.Backup
import com.swisscom.cloud.sb.model.backup.BackupStatus
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
