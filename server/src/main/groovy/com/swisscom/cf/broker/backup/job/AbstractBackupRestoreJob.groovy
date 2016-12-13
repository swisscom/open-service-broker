package com.swisscom.cf.broker.backup.job

import com.swisscom.cf.broker.async.job.AbstractJob
import com.swisscom.cf.broker.backup.BackupPersistenceService
import com.swisscom.cf.broker.backup.BackupRestoreProviderLookup
import com.swisscom.cf.broker.model.Backup
import com.swisscom.cf.broker.services.common.BackupRestoreProvider
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@CompileStatic
@Transactional
@Log4j
abstract class AbstractBackupRestoreJob<T> extends AbstractJob {
    @Autowired
    protected BackupPersistenceService backupPersistenceService

    @Autowired
    protected BackupRestoreProviderLookup backupRestoreProviderLookup

    @Override
    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String id = getJobId(jobExecutionContext)
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
