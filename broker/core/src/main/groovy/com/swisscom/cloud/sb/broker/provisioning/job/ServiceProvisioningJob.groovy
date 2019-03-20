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
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.repository.ProvisionRequestRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceProvisioner
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Slf4j
//When renaming, the existing jobs in Quartz DB should be renamed accordingly!!!
class ServiceProvisioningJob extends AbstractLastOperationJob {
    @Autowired
    private ServiceProviderLookup serviceProviderLookup
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    private ProvisionRequestRepository provisionRequestRepository

    protected LastOperationJobContext enrichContext(LastOperationJobContext jobContext) {
        String serviceInstanceGuid = jobContext.lastOperation.guid
        ProvisionRequest provisionRequest = provisionRequestRepository.findByServiceInstanceGuid(serviceInstanceGuid)
        jobContext.provisionRequest = provisionRequest
        jobContext.plan = provisionRequest.plan
        jobContext.serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        return jobContext
    }

    @Override
    protected AsyncOperationResult handleJob(LastOperationJobContext jobContext) {
        log.info("About to request service provision, ${jobContext.lastOperation.toString()}")
        AsyncOperationResult provisionResult = findServiceProvisioner(jobContext).requestProvision(jobContext)
        jobContext.serviceInstance = provisioningPersistenceService.createServiceInstanceOrUpdateDetails(jobContext.provisionRequest, new ProvisionResponse(details: provisionResult.details, isAsync: true))

        if (provisionResult.status == LastOperation.Status.SUCCESS) {
            provisioningPersistenceService.updateServiceInstanceCompletion(jobContext.serviceInstance, true)
        }
        return provisionResult
    }

    private AsyncServiceProvisioner findServiceProvisioner(LastOperationJobContext context) {
        AsyncServiceProvisioner serviceProvisioner = ((AsyncServiceProvisioner) serviceProviderLookup.findServiceProvider(context.plan))
        return serviceProvisioner
    }
}


