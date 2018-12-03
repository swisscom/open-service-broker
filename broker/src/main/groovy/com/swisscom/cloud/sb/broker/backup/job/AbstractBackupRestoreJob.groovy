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

import com.swisscom.cloud.sb.broker.async.job.AbstractJob
import com.swisscom.cloud.sb.broker.backup.BackupPersistenceService
import com.swisscom.cloud.sb.broker.backup.BackupRestoreProvider
import com.swisscom.cloud.sb.broker.backup.BackupRestoreProviderLookup
import com.swisscom.cloud.sb.broker.model.Backup
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@CompileStatic
@Transactional
@Slf4j
abstract class AbstractBackupRestoreJob<T> extends AbstractJob {
    @Autowired
    protected BackupPersistenceService backupPersistenceService

    @Autowired
    protected BackupRestoreProviderLookup backupRestoreProviderLookup

    @Override
    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String id = getJobId(jobExecutionContext)
        MDC.put("serviceInstanceGuid", id)
        log.info("Executing job with class:${this.class.getSimpleName()} with id:${id}")
        T targetEntity = null
        try {
            targetEntity = getTargetEntity(id)
            if (targetEntity == null) {
                throw new RuntimeException("Could not load object of type:${T.class.simpleName} with ID:${id}")
            }
            Backup.Status status = handleJob(targetEntity)
            if (status == Backup.Status.SUCCESS) {
                handleSucess(targetEntity, id)
            } else if (status == Backup.Status.FAILED) {
                handleFailure(targetEntity, id)
            } else if (status == Backup.Status.IN_PROGRESS) {
                if (isExecutedForLastTime(jobExecutionContext)) {
                    log.warn("Giving up on job with id:${id}")
                    handleFailure(targetEntity, id)
                }
            }
        } catch (Exception e) {
            log.error("Job execution with id:${id} failed", e)
            handleFailure(targetEntity, id)
        }
    }

    private void handleFailure(T t, String id) {
        log.warn("Job with id:${id} failed")
        markFailure(t)
        dequeue(id)
    }

    abstract protected void markFailure(T t)

    private void handleSucess(T t, String id) {
        log.warn("Successfully finished job with id:${id}")
        markSuccess(t)
        dequeue(id)
    }

    abstract protected void markSuccess(T t)

    abstract protected Backup.Status handleJob(T t)

    abstract protected <T> T getTargetEntity(String id)

    protected BackupRestoreProvider findBackupProvider(Backup backup) {
        backupRestoreProviderLookup.findBackupProvider(backup.plan)
    }
}
