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

package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.ErrorCodeHelper
import spock.lang.Specification
import spock.lang.Unroll

class ProvisioningServiceSpec extends Specification {
    public static final String serviceInstanceGuid = "serviceInstanceGuid"

    ProvisioningService provisioningService

    void setup() {
        given:
        provisioningService = new ProvisioningService()

        and:
        def provisioningPersistenceService = Mock(ProvisioningPersistenceService)
        provisioningService.provisioningPersistenceService = provisioningPersistenceService
    }

    def "provisioning service works with provisionRequest"() {
        given:
        def serviceProvider = Mock(ServiceProvider)
        serviceProvider.provision(_) >> new ProvisionResponse()
        def serviceProviderLookup = Mock(ServiceProviderLookup)
        serviceProviderLookup.findServiceProvider(_) >> serviceProvider
        provisioningService.serviceProviderLookup = serviceProviderLookup

        and:
        def provisioningPersistenceService = Mock(ProvisioningPersistenceService)
        1 * provisioningPersistenceService.createServiceInstance(_) >> Mock(ServiceInstance)
        1 * provisioningPersistenceService.updateServiceDetails(_, _) >> Mock(ServiceInstance)
        provisioningService.provisioningPersistenceService = provisioningPersistenceService

        and:
        def provisionRequest = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid,
                plan: new Plan(asyncRequired: false,
                        service: new CFService(asyncRequired: false)
                )
        )
        when:
        def result = provisioningService.provision(provisionRequest)
        then:
        result.isAsync == false
    }

    def "provisioning service works with provisiongresponse isAsync true"() {
        given:
        def serviceProvider = Mock(ServiceProvider)
        serviceProvider.provision(_) >> new ProvisionResponse(isAsync: true)
        def serviceProviderLookup = Mock(ServiceProviderLookup)
        serviceProviderLookup.findServiceProvider(_) >> serviceProvider
        provisioningService.serviceProviderLookup = serviceProviderLookup

        and:
        def provisioningPersistenceService = Mock(ProvisioningPersistenceService)
        1 * provisioningPersistenceService.createServiceInstance(_) >> Mock(ServiceInstance)
        1 * provisioningPersistenceService.updateServiceDetails(_, _) >> Mock(ServiceInstance)
        provisioningService.provisioningPersistenceService = provisioningPersistenceService

        and:
        def provisionRequest = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid,
                plan: new Plan(asyncRequired: false,
                        service: new CFService(asyncRequired: false)
                ),
                acceptsIncomplete: true
        )
        when:
        def result = provisioningService.provision(provisionRequest)
        then:
        result.isAsync == true
    }

    @Unroll
    def "provisioning service throws exception with acceptsIncomplete: false and asyncRequired on Plan is #planAsync and Service is #serviceAsync"(planAsync, serviceAsync, expectedException, expectedErrorCode) {
        given:
        def serviceProvider = Mock(ServiceProvider)
        serviceProvider.provision(_) >> new ProvisionResponse()
        def serviceProviderLookup = Mock(ServiceProviderLookup)
        serviceProviderLookup.findServiceProvider(_) >> serviceProvider
        provisioningService.serviceProviderLookup = serviceProviderLookup

        when:
        def provisionRequest = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid,
                plan: new Plan(asyncRequired: planAsync,
                        service: new CFService(asyncRequired: serviceAsync)
                ),
                acceptsIncomplete: false
        )
        provisioningService.provision(provisionRequest)

        then:
        def ex = thrown(expectedException)
        ErrorCodeHelper.assertServiceBrokerException(ex, ErrorCode.ASYNC_REQUIRED)

        where:
        planAsync   | serviceAsync | expectedException      | expectedErrorCode
        true        | false        | ServiceBrokerException | ErrorCode.ASYNC_REQUIRED
        false       | true         | ServiceBrokerException | ErrorCode.ASYNC_REQUIRED
        true        | true         | ServiceBrokerException | ErrorCode.ASYNC_REQUIRED
    }

}
