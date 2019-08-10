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

package com.swisscom.cloud.sb.broker.provisioning.job

import com.swisscom.cloud.sb.broker.async.job.AbstractLastOperationJob
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.repository.UpdateRequestRepository
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceUpdater
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.updating.UpdateResponse
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Slf4j
public class ServiceUpdateJob extends AbstractLastOperationJob {
    @Autowired
    private ServiceProviderLookup serviceProviderLookup
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    private UpdateRequestRepository updateRequestRepository

    protected LastOperationJobContext enrichContext(LastOperationJobContext jobContext) {
        log.info("About to update service instance, ${jobContext.lastOperation.toString()}")
        def serviceInstanceGuid = jobContext.lastOperation.guid
        jobContext.serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        jobContext.plan = jobContext.serviceInstance.plan
        jobContext.updateRequest = updateRequestRepository.findByServiceInstanceGuid(serviceInstanceGuid)
                .sort({it -> it.dateCreated})
                .reverse()
                .first()
        return jobContext
    }

    @Override
    protected AsyncOperationResult handleJob(LastOperationJobContext context) {
        log.info("About to update service instance, ${context.lastOperation.toString()}")
        def updateResult = ((AsyncServiceUpdater)serviceProviderLookup.findServiceProvider(context.serviceInstance.plan)).requestUpdate(context)

        context.serviceInstance = provisioningPersistenceService.updateServiceDetails(context.updateRequest, new UpdateResponse(details: updateResult.details, isAsync: true))

        if (updateResult.status == LastOperation.Status.SUCCESS) {
            provisioningPersistenceService.updateServiceInstanceCompletion(context.serviceInstance, true)
        }
        return updateResult
    }
}
