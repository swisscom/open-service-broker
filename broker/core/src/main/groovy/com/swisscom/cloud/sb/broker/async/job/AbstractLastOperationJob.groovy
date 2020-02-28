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

package com.swisscom.cloud.sb.broker.async.job

import com.swisscom.cloud.sb.broker.cfapi.converter.ErrorDtoConverter
import com.swisscom.cloud.sb.broker.cfapi.dto.ErrorDto
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.metrics.LastOperationMetricService
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContextService
import com.swisscom.cloud.sb.broker.util.Audit
import groovy.transform.CompileStatic
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

import static java.lang.String.format

@CompileStatic
@Transactional
abstract class AbstractLastOperationJob extends AbstractJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLastOperationJob.class)
    
    @Autowired
    protected LastOperationJobContextService lastOperationContextService
    @Autowired
    protected ProvisioningPersistenceService provisioningPersistenceService
    @Autowired
    protected ErrorDtoConverter errorDtoConverter
    @Autowired
    protected LastOperationMetricService lastOperationMetricsService

    @Override
    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String id = getJobId(jobExecutionContext)
        MDC.put("serviceInstanceGuid", id)
        def failed = false
        def completed = true
        LOGGER.info("Executing job with id:{}", id)
        LastOperationJobContext lastOperationContext = null

        try {
            lastOperationContext = enrichContext(lastOperationContextService.loadContext(id))
            AsyncOperationResult jobStatus = handleJob(lastOperationContext)
            if (jobStatus.status == LastOperation.Status.SUCCESS) {
                LOGGER.warn("Successfully finished job with id:{}", id)
                lastOperationContext.notifySuccess(jobStatus.description)
                dequeue(lastOperationContext, id)
                lastOperationMetricsService.notifySucceeded(lastOperationContext.planGuidOrUndefined)
            } else if (jobStatus.status == LastOperation.Status.FAILED) {
                LOGGER.warn("Job with id:{} failed", id)
                dequeueFailed(lastOperationContext, id, jobStatus.description)
                failed = true
                lastOperationMetricsService.notifyFailedByServiceProvider(lastOperationContext.planGuidOrUndefined)
            } else if (jobStatus.status == LastOperation.Status.IN_PROGRESS) {
                if (isExecutedForLastTime(jobExecutionContext)) {
                    LOGGER.warn("Giving up on job with id:{}", id)
                    dequeueFailed(lastOperationContext, id)
                    lastOperationMetricsService.notifyFailedWithTimeout(lastOperationContext.planGuidOrUndefined)
                    failed = true
                } else {
                    lastOperationContext.notifyProgress(jobStatus.description, jobStatus.internalStatus)
                    completed = false
                }
            }
        } catch (ServiceBrokerException sbe) {
            LOGGER.error(format("Job execution with id:%s failed", id), sbe)
            dequeueFailed(lastOperationContext, id, errorDtoConverter.convert(sbe))
            lastOperationMetricsService.notifyFailedWithException(lastOperationContext.planGuidOrUndefined)
            failed = true
        } catch (Exception e) {
            LOGGER.error(format("Job execution with id:%s failed", id), e)
            dequeueFailed(lastOperationContext, id)
            lastOperationMetricsService.notifyFailedWithException(lastOperationContext.planGuidOrUndefined)
            failed = true
        }
        finally {
            if (failed) {
                Audit.log("Async action failed",
                        [
                                serviceInstanceGuid: lastOperationContext.serviceInstance.guid,
                                action: Audit.AuditAction.Scheduler,
                                failed: failed
                        ])
            } else if (completed) {
                Audit.log("Async action completed",
                        [
                                serviceInstanceGuid: lastOperationContext.serviceInstance.guid,
                                action: Audit.AuditAction.Scheduler,
                                failed: false,
                                completed: true
                        ])
            }
        }
    }

    protected LastOperationJobContext enrichContext(LastOperationJobContext context) { return context }

    protected abstract AsyncOperationResult handleJob(LastOperationJobContext context)

    private void dequeueFailed(LastOperationJobContext lastOperationContext, String id, ErrorDto errorDto) {
        dequeueFailed(lastOperationContext, id, errorDto.description)
    }

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
