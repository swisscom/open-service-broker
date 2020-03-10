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

package com.swisscom.cloud.sb.broker.controller

import com.swisscom.cloud.sb.broker.context.ServiceContextPersistenceService
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningService
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationStatusService
import com.swisscom.cloud.sb.broker.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.repository.PlanRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.ServiceProviderLookup
import spock.lang.Specification

class ProvisioningControllerSpec extends Specification {

    private ProvisioningService provisioningService
    private LastOperationStatusService lastOperationStatusService
    private ServiceInstanceRepository serviceInstanceRepository
    private ServiceContextPersistenceService serviceContextService
    private CFServiceRepository cfServiceRepository
    private PlanRepository planRepository
    private ServiceProviderLookup serviceProviderLookup

    private ProvisioningController sut

    def setup() {
        provisioningService = Mock(ProvisioningService)
        lastOperationStatusService = Mock(LastOperationStatusService)
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        serviceContextService = Mock(ServiceContextPersistenceService)
        cfServiceRepository = Mock(CFServiceRepository)
        planRepository = Mock(PlanRepository)
        serviceProviderLookup = Mock(ServiceProviderLookup)
        sut = new ProvisioningController(provisioningService,
                                         lastOperationStatusService,
                                         serviceInstanceRepository,
                                         serviceContextService,
                                         cfServiceRepository,
                                         planRepository,
                                         serviceProviderLookup)
    }

    def 'service creation success can always be deleted'() {
        given:
        serviceInstanceRepository.findByGuid(_) >> new ServiceInstance(completed: true)

        when:
        sut.createDeprovisionRequest("foo", false)

        then:
        noExceptionThrown()
    }

    def 'service creation failed can always be deleted'() {
        given:
        serviceInstanceRepository.findByGuid(_) >> new ServiceInstance(completed: false)

        when:
        sut.createDeprovisionRequest("foo", false)

        then:
        noExceptionThrown()
    }

    def 'delete deleted service throws exception'() {
        given:
        serviceInstanceRepository.findByGuid(_) >> new ServiceInstance(deleted: true)

        when:
        sut.createDeprovisionRequest("foo", false)

        then:
        thrown ServiceBrokerException
    }

    def 'fetch service instance throws exception when not supported'() {
        given:
        serviceInstanceRepository.findByGuid(serviceInstanceGuid) >> new ServiceInstance(guid: serviceInstanceGuid,
                                                                                         completed: true,
                                                                                         plan: new Plan(service: new CFService(
                                                                                                 instancesRetrievable: false)))

        when:
        sut.getServiceInstance(serviceInstanceGuid)

        then:
        def ex = thrown(ServiceBrokerException)
        ex.getMessage() == "Fetching Service Instance for this service plan is not supported"

        where:
        serviceInstanceGuid          | _
        UUID.randomUUID().toString() | _
    }
}


