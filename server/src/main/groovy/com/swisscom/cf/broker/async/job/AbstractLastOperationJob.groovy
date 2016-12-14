package com.swisscom.cf.broker.async.job

import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContextService
import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cf.broker.provisioning.async.AsyncOperationResult
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@CompileStatic
@Transactional
@Log4j
public abstract class AbstractLastOperationJob extends AbstractJob {
    @Autowired
    protected LastOperationJobContextService lastOperationContextService

    @Autowired
    protected ProvisioningPersistenceService provisioningPersistenceService

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String id = getJobId(jobExecutionContext)
        log.info("Executing job with id:${id}")
        LastOperationJobContext lastOperationContext = null
        try {
            lastOperationContext = enrichContext(lastOperationContextService.loadContext(id))
            def jobStatus = handleJob(lastOperationContext)
            if (jobStatus.status == LastOperation.Status.SUCCESS) {
                log.warn("Successfully finished job with id:${id}")
                lastOperationContext.notifySuccess(jobStatus.description)
                dequeue(lastOperationContext, id)
            } else if (jobStatus.status == LastOperation.Status.FAILED) {
                log.warn("Job with id:${id} failed")
                dequeueFailed(lastOperationContext, id, jobStatus.description)
            } else if (jobStatus.status == LastOperation.Status.IN_PROGRESS) {
                if (isExecutedForLastTime(jobExecutionContext)) {
                    log.warn("Giving up on job with id:${id}")
                    dequeueFailed(lastOperationContext, id)
                } else {
                    if (jobStatus instanceof AsyncOperationResult) {
                        lastOperationContext.notifyProgress(jobStatus.description, ((AsyncOperationResult) jobStatus).internalStatus)
                    } else {
                        lastOperationContext.notifyProgress(jobStatus.description)
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Job execution with id:${id} failed", e)
            dequeueFailed(lastOperationContext, id)
        }
    }

    protected LastOperationJobContext enrichContext(LastOperationJobContext context) { return context }

    protected abstract JobResult handleJob(LastOperationJobContext context)

    private void dequeueFailed(LastOperationJobContext lastOperationContext, String id, String description = null) {
        lastOperationContext.notifyFailure(description)
        dequeue(lastOperationContext, id)
    }

    private void dequeue(LastOperationJobContext lastOperationContext, String id) {
        cleanUpRequestData(lastOperationContext)
        dequeue(id)
    }

    def cleanUpRequestData(LastOperationJobContext lastOperationContext) {
        if (lastOperationContext.lastOperation.operation == LastOperation.Operation.PROVISION) {
            provisioningPersistenceService.removeProvisionRequestIfExists(lastOperationContext.lastOperation.guid)
        } else if (lastOperationContext.lastOperation.operation == LastOperation.Operation.DEPROVISION) {
            provisioningPersistenceService.removeDeprovisionRequestIfExists(lastOperationContext.lastOperation.guid)
        }
    }
}
