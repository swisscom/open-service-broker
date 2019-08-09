/*
 * Copyright (c) 2019 Swisscom (Switzerland) Ltd.
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
import com.swisscom.cloud.sb.broker.repository.BackupRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.joda.time.LocalDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@CompileStatic
@Slf4j
@Transactional
class BackupCleanup {
    public static final int BACKUP_RETENTION_IN_DAYS = 14

    @Autowired
    BackupRepository backupRepository

    Boolean isDeleted(Backup backup) {
        return backup.operation == Backup.Operation.DELETE && backup.status == Backup.Status.SUCCESS
    }

    Boolean isNotCreated(Backup backup) {
        return backup.operation == Backup.Operation.CREATE && backup.status == Backup.Status.FAILED
    }

    void run() {
        def retentionDate = new LocalDateTime().minusDays(BACKUP_RETENTION_IN_DAYS).toDate()
        def allNoneExistingBackups = backupRepository.findAll()
                .findAll { b -> isDeleted(b) || isNotCreated(b) }
                .findAll { b -> b.dateRequested <= retentionDate }

        backupRepository.deleteAll(allNoneExistingBackups)
    }
}
