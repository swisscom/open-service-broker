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
import com.swisscom.cloud.sb.broker.metrics.LastOperationMetricsService
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContextService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@CompileStatic
@Transactional
@Slf4j
abstract class AbstractLastOperationJob extends AbstractJob {
    @Autowired
    protected LastOperationJobContextService lastOperationContextService
    @Autowired
    protected ProvisioningPersistenceService provisioningPersistenceService
    @Autowired
    protected ErrorDtoConverter errorDtoConverter
    @Autowired
    protected LastOperationMetricsService lastOperationMetricsService

    @Override
    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String id = getJobId(jobExecutionContext)
        log.info("Executing job with id:${id}")
        LastOperationJobContext lastOperationContext = null
        try {
            lastOperationContext = enrichContext(lastOperationContextService.loadContext(id))
            AsyncOperationResult jobStatus = handleJob(lastOperationContext)
            if (jobStatus.status == LastOperation.Status.SUCCESS) {
                log.warn("Successfully finished job with id:${id}")
                lastOperationContext.notifySuccess(jobStatus.description)
                dequeue(lastOperationContext, id)
                lastOperationMetricsService.notifySucceeded(lastOperationContext.plan.guid)
            } else if (jobStatus.status == LastOperation.Status.FAILED) {
                log.warn("Job with id:${id} failed")
                dequeueFailed(lastOperationContext, id, jobStatus.description)
                lastOperationMetricsService.notifyFailedByServiceProvider(lastOperationContext.plan.guid)
            } else if (jobStatus.status == LastOperation.Status.IN_PROGRESS) {
                if (isExecutedForLastTime(jobExecutionContext)) {
                    log.warn("Giving up on job with id:${id}")
                    dequeueFailed(lastOperationContext, id)
                    lastOperationMetricsService.notifyFailedWithTimeout(lastOperationContext.plan.guid)
                } else {
                    lastOperationContext.notifyProgress(jobStatus.description, jobStatus.internalStatus)
                }
            }
        } catch (ServiceBrokerException sbe) {
            log.warn("Job execution with id:${id} failed", sbe)
            dequeueFailed(lastOperationContext, id, errorDtoConverter.convert(sbe))
            lastOperationMetricsService.notifyFailedWithException(lastOperationContext.plan.guid)
        } catch (Exception e) {
            log.warn("Job execution with id:${id} failed", e)
            dequeueFailed(lastOperationContext, id)
            lastOperationMetricsService.notifyFailedWithException(lastOperationContext.plan.guid)
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
