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

package com.swisscom.cloud.sb.broker.services

import com.swisscom.cloud.sb.broker.async.AsyncProvisioningService
import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointLookup
import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointProvider
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceDeprovisioner
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceProvisioner
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceUpdater
import com.swisscom.cloud.sb.broker.provisioning.job.*
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.common.Utils
import com.swisscom.cloud.sb.broker.updating.UpdateResponse
import com.swisscom.cloud.sb.model.endpoint.Endpoint
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
@Slf4j
abstract class AsyncServiceProvider<T extends AsyncServiceConfig>
        implements ServiceProvider, AsyncServiceProvisioner, AsyncServiceDeprovisioner, AsyncServiceUpdater, EndpointProvider {

    @Autowired
    protected AsyncProvisioningService asyncProvisioningService
    @Autowired
    protected ProvisioningPersistenceService provisioningPersistenceService
    @Autowired
    protected T serviceConfig
    @Autowired
    protected EndpointLookup endpointLookup

    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        Utils.verifyAsychronousCapableClient(request)

        asyncProvisioningService.scheduleProvision(
                new ProvisioningJobConfig(ServiceProvisioningJob.class, request,
                        serviceConfig.retryIntervalInSeconds,
                        serviceConfig.maxRetryDurationInMinutes))
        return new ProvisionResponse(isAsync: true)
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        asyncProvisioningService.scheduleDeprovision(new DeprovisioningJobConfig(ServiceDeprovisioningJob.class, request,
                serviceConfig.retryIntervalInSeconds, serviceConfig.maxRetryDurationInMinutes))
        return new DeprovisionResponse(isAsync: true)
    }

    @Override
    UpdateResponse update(UpdateRequest request) {
        asyncProvisioningService.scheduleUpdate(new UpdateJobConfig(ServiceUpdateJob.class, request, request.serviceInstanceGuid,
                serviceConfig.retryIntervalInSeconds, serviceConfig.maxRetryDurationInMinutes))
        return new UpdateResponse(isAsync: true)
    }

    @Override
    AsyncOperationResult requestUpdate(LastOperationJobContext context) {
        ErrorCode.SERVICE_UPDATE_NOT_ALLOWED.throwNew()
        return null
    }

    @Override
    Collection<Endpoint> findEndpoints(ServiceInstance serviceInstance) {
        // return endpointLookup.findEndpoints(serviceInstance, serviceConfig)
    }

}
