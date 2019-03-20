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

package com.swisscom.cloud.sb.broker.backup.job

import com.swisscom.cloud.sb.broker.model.Backup
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
abstract class AbstractBackupJob extends AbstractBackupRestoreJob<Backup> {

    @Override
    protected void markFailure(Backup backup) {
        backup.status = Backup.Status.FAILED
        backupPersistenceService.saveBackup(backup)
    }

    @Override
    protected void markSuccess(Backup backup) {
        backup.status = Backup.Status.SUCCESS
        backupPersistenceService.saveBackup(backup)
    }

    @Override
    protected Backup getTargetEntity(String id) {
        return backupPersistenceService.findBackupByGuid(id)
    }

    @Override
    protected String getServiceInstanceGuid(String id) {
        try {
            return getTargetEntity(id).serviceInstanceGuid
        } catch (NullPointerException e) {
            log.error("Could not get backup for backup guid " + id)
            return null
        }
    }
}
