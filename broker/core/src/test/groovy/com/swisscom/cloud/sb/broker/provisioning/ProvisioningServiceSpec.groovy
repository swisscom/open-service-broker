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
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.ParentServiceProvider
import com.swisscom.cloud.sb.broker.util.test.ErrorCodeHelper
import spock.lang.Specification
import spock.lang.Unroll

class ProvisioningServiceSpec extends Specification {
    public static final String serviceInstanceGuid = "serviceInstanceGuid"

    ProvisioningService provisioningService
    LastOperationRepository lastOperationRepository

    void setup() {
        given:
        provisioningService = new ProvisioningService()

        and:
        def provisioningPersistenceService = Mock(ProvisioningPersistenceService)
        provisioningService.provisioningPersistenceService = provisioningPersistenceService

        and:
        lastOperationRepository = Mock(LastOperationRepository)
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
        planAsync | serviceAsync | expectedException      | expectedErrorCode
        true      | false        | ServiceBrokerException | ErrorCode.ASYNC_REQUIRED
        false     | true         | ServiceBrokerException | ErrorCode.ASYNC_REQUIRED
        true      | true         | ServiceBrokerException | ErrorCode.ASYNC_REQUIRED
    }

    def "checkParent() fails with parent_reference to non-existing service instance"() {
        given:
        def serviceProvider = Mock(ServiceProvider)
        def serviceProviderLookup = Mock(ServiceProviderLookup)
        serviceProviderLookup.findServiceProvider(_) >> serviceProvider
        provisioningService.serviceProviderLookup = serviceProviderLookup

        and:
        def provisioningPersistenceService = Mock(ProvisioningPersistenceService)
        1 * provisioningPersistenceService.findParentServiceInstance(_) >> null
        provisioningService.provisioningPersistenceService = provisioningPersistenceService

        and:
        def provisionRequest = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid,
                plan: new Plan(asyncRequired: false, service: new CFService(asyncRequired: false)
                ), parameters: '{"parent_reference": "test"}', acceptsIncomplete: true
        )
        when:
        provisioningService.checkParent(provisionRequest)
        then:
        thrown(ServiceBrokerException)
    }

    def "checkParent() fails with parent_reference to non-parent service instance"() {
        given:
        def serviceProvider = Mock(ServiceProvider)
        def serviceProviderLookup = Mock(ServiceProviderLookup)
        serviceProviderLookup.findServiceProvider(_) >> serviceProvider
        provisioningService.serviceProviderLookup = serviceProviderLookup

        and:
        def nonParentServiceInstance = Mock(ServiceInstance)
        def plan = Mock(Plan)
        plan.getGuid() >> "test"
        nonParentServiceInstance.plan >> plan

        def provisioningPersistenceService = Mock(ProvisioningPersistenceService)
        2 * provisioningPersistenceService.findParentServiceInstance(_) >> nonParentServiceInstance
        provisioningService.provisioningPersistenceService = provisioningPersistenceService

        and:
        def provisionRequest = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid,
                plan: new Plan(asyncRequired: false, service: new CFService(asyncRequired: false)
                ), parameters: '{"parent_reference": "test"}', acceptsIncomplete: true
        )
        when:
        provisioningService.provision(provisionRequest)
        then:
        thrown(ServiceBrokerException)
    }

    def "checkParent() fails with parent_reference to full parent service instance"() {
        given:
        def serviceProvider = Mock(ServiceProvider).withTraits(ParentServiceProvider)
        serviceProvider.lastOperationRepository = lastOperationRepository
        def childServiceInstanceGuid = UUID.randomUUID().toString()
        lastOperationRepository.findByGuid(childServiceInstanceGuid) >> new LastOperation(status: LastOperation.Status.IN_PROGRESS)
        def serviceProviderLookup = Mock(ServiceProviderLookup)
        serviceProviderLookup.findServiceProvider(_) >> serviceProvider
        provisioningService.serviceProviderLookup = serviceProviderLookup

        and:
        def parentServiceInstance = Mock(ServiceInstance)
        def plan = Mock(Plan)
        parentServiceInstance.getChilds() >> new HashSet<ServiceInstance>([new ServiceInstance(guid: childServiceInstanceGuid)])
        plan.getGuid() >> "test"
        plan.getParameters() >> new HashSet<Parameter>([new Parameter(name: "max_children", value: 1)])
        parentServiceInstance.plan >> plan

        def provisioningPersistenceService = Mock(ProvisioningPersistenceService)
        2 * provisioningPersistenceService.findParentServiceInstance(_) >> parentServiceInstance
        provisioningService.provisioningPersistenceService = provisioningPersistenceService

        and:
        def provisionRequest = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid,
                plan: new Plan(asyncRequired: false, service: new CFService(asyncRequired: false)
                ), parameters: '{"parent_reference": "test"}', acceptsIncomplete: true
        )
        when:
        provisioningService.checkParent(provisionRequest)
        then:
        thrown(ServiceBrokerException)
    }

    def "checkParent() succeeds with parent_reference to a parent service instance"() {
        given:
        def serviceProvider = Mock(ServiceProvider).withTraits(ParentServiceProvider)
        serviceProvider.lastOperationRepository = lastOperationRepository
        def childServiceInstanceGuid = UUID.randomUUID().toString()
        lastOperationRepository.findByGuid(childServiceInstanceGuid) >> new LastOperation(status: LastOperation.Status.IN_PROGRESS)
        def serviceProviderLookup = Mock(ServiceProviderLookup)
        serviceProviderLookup.findServiceProvider(_) >> serviceProvider
        provisioningService.serviceProviderLookup = serviceProviderLookup

        and:
        def parentServiceInstance = Mock(ServiceInstance)
        def plan = Mock(Plan)
        parentServiceInstance.getChilds() >> new HashSet<ServiceInstance>([new ServiceInstance(guid: childServiceInstanceGuid)])
        plan.getGuid() >> "test"
        plan.getParameters() >> new HashSet<Parameter>([new Parameter(name: "max_children", value: 2)])
        parentServiceInstance.plan >> plan

        def provisioningPersistenceService = Mock(ProvisioningPersistenceService)
        2 * provisioningPersistenceService.findParentServiceInstance(_) >> parentServiceInstance
        provisioningService.provisioningPersistenceService = provisioningPersistenceService

        and:
        def provisionRequest = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid,
                plan: new Plan(asyncRequired: false, service: new CFService(asyncRequired: false)
                ), parameters: '{"parent_reference": "test"}', acceptsIncomplete: true
        )
        when:
        provisioningService.checkParent(provisionRequest)
        then:
        noExceptionThrown()
    }

    def "checkActiveChildren fails with active children"() {
        given:
        def serviceProvider = Mock(ServiceProvider).withTraits(ParentServiceProvider)
        serviceProvider.lastOperationRepository = lastOperationRepository
        def childServiceInstanceGuid = UUID.randomUUID().toString()
        lastOperationRepository.findByGuid(childServiceInstanceGuid) >> new LastOperation(status: LastOperation.Status.IN_PROGRESS)
        def serviceProviderLookup = Mock(ServiceProviderLookup)
        serviceProviderLookup.findServiceProvider(_) >> serviceProvider
        provisioningService.serviceProviderLookup = serviceProviderLookup

        and:
        def serviceInstance = Mock(ServiceInstance)
        def plan = Mock(Plan)
        def service = Mock(CFService)
        service.getAsyncRequired() >> false
        plan.service >> service
        serviceInstance.plan >> plan
        serviceInstance.childs >> new HashSet<ServiceInstance>([new ServiceInstance(deleted: false, guid: childServiceInstanceGuid)])
        def deprovisionRequest = new DeprovisionRequest(serviceInstanceGuid: serviceInstanceGuid, acceptsIncomplete: true, serviceInstance: serviceInstance)
        when:
        provisioningService.checkActiveChildren(deprovisionRequest)
        then:
        thrown(ServiceBrokerException)
    }

    def "checkActiveChildren doesn't fail with failed children"() {
        given:
        def serviceProvider = Mock(ServiceProvider).withTraits(ParentServiceProvider)
        serviceProvider.lastOperationRepository = lastOperationRepository
        def childServiceInstanceGuid = UUID.randomUUID().toString()
        lastOperationRepository.findByGuid(childServiceInstanceGuid) >> new LastOperation(status: LastOperation.Status.FAILED)
        def serviceProviderLookup = Mock(ServiceProviderLookup)
        serviceProviderLookup.findServiceProvider(_) >> serviceProvider
        provisioningService.serviceProviderLookup = serviceProviderLookup

        and:
        def serviceInstance = Mock(ServiceInstance)
        def plan = Mock(Plan)
        def service = Mock(CFService)
        service.getAsyncRequired() >> false
        plan.service >> service
        serviceInstance.plan >> plan
        serviceInstance.childs >> new HashSet<ServiceInstance>([new ServiceInstance(deleted: false, guid: childServiceInstanceGuid)])
        def deprovisionRequest = new DeprovisionRequest(serviceInstanceGuid: serviceInstanceGuid, acceptsIncomplete: true, serviceInstance: serviceInstance)
        when:
        provisioningService.checkActiveChildren(deprovisionRequest)
        then:
        noExceptionThrown()
    }

    def "checkActiveChildren succeeds with deleted children"() {
        given:
        def serviceProvider = Mock(ServiceProvider).withTraits(ParentServiceProvider)
        def serviceProviderLookup = Mock(ServiceProviderLookup)
        serviceProviderLookup.findServiceProvider(_) >> serviceProvider
        provisioningService.serviceProviderLookup = serviceProviderLookup

        and:
        def serviceInstance = Mock(ServiceInstance)
        def plan = Mock(Plan)
        def service = Mock(CFService)
        service.getAsyncRequired() >> false
        plan.service >> service
        serviceInstance.plan >> plan
        serviceInstance.childs >> new HashSet<ServiceInstance>([new ServiceInstance(deleted: true)])
        def deprovisionRequest = new DeprovisionRequest(serviceInstanceGuid: serviceInstanceGuid, acceptsIncomplete: true, serviceInstance: serviceInstance)
        when:
        provisioningService.checkActiveChildren(deprovisionRequest)
        then:
        noExceptionThrown()
    }
}
