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

package com.swisscom.cloud.sb.broker.functional

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.repository.PlanRepository
import com.swisscom.cloud.sb.broker.servicedefinition.ServiceDefinitionInitializer
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import com.swisscom.cloud.sb.broker.util.Resource
import com.swisscom.cloud.sb.client.ServiceBrokerClientExtended
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest
import org.springframework.web.client.RestTemplate

import static com.swisscom.cloud.sb.broker.util.Resource.readTestFileContent

class TestHelperServiceSpec extends BaseFunctionalSpec {

    ServiceBrokerClientExtended serviceBrokerClientExtended

    @Autowired
    CFServiceRepository cfServiceRepository

    @Autowired
    PlanRepository planRepository

    @Autowired
    ServiceDefinitionInitializer serviceDefinitionInitializer

    @Autowired
    TestHelperService testHelperService

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "set all services and plans to active"() {
        given:
        serviceBrokerClientExtended = createServiceBrokerClient()
        ServiceDto serviceDto = new ObjectMapper().readValue(
                readTestFileContent("/service-data/serviceDefinitionWithInstance.json"), ServiceDto)
        def serviceId = "serviceDefinitionWithInstance"
        def planId = "planForServiceDefinitionFromServiceManifestWithInstance"

        and:
        serviceDefinitionInitializer.addOrUpdateServiceDefinitions(serviceDto)
        assert (cfServiceRepository.findByGuid(serviceId))
        assert (planRepository.findByGuid(planId))

        and:
        def serviceInstanceId = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(CreateServiceInstanceRequest.
                builder().
                serviceDefinitionId(serviceId).
                planId(planId).
                serviceInstanceId(serviceInstanceId).
                asyncAccepted(false).
                build())

        and:
        serviceDefinitionInitializer.init()

        and:
        assert (!cfServiceRepository.findByGuid(serviceId).active)
        assert (!planRepository.findByGuid(planId).active)

        when:
        testHelperService.setAllServicesAndPlansToActive()

        then:
        assert (cfServiceRepository.findByGuid(serviceId).active)
        assert (planRepository.findByGuid(planId).active)

        cleanup:
        serviceBrokerClientExtended.deleteServiceInstance(DeleteServiceInstanceRequest.
                builder().
                serviceDefinitionId(serviceId).
                planId(planId).
                serviceInstanceId(serviceInstanceId).
                asyncAccepted(true).
                build())
    }

    def "set selected services and plans to active"() {
        given:
        serviceBrokerClientExtended = createServiceBrokerClient()
        ServiceDto serviceDto = new ObjectMapper().readValue(
                readTestFileContent("/service-data/serviceDefinitionWithInstance.json"), ServiceDto)
        def serviceId = "serviceDefinitionWithInstance"
        def planId = "planForServiceDefinitionFromServiceManifestWithInstance"

        and:
        serviceDefinitionInitializer.addOrUpdateServiceDefinitions(serviceDto)
        assert (cfServiceRepository.findByGuid(serviceId))
        assert (planRepository.findByGuid(planId))

        and:
        def serviceInstanceId = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(CreateServiceInstanceRequest.
                builder().
                serviceDefinitionId(serviceId).
                planId(planId).
                serviceInstanceId(serviceInstanceId).
                asyncAccepted(false).
                build())

        and:
        serviceDefinitionInitializer.init()

        and:
        assert (!cfServiceRepository.findByGuid(serviceId).active)
        assert (!planRepository.findByGuid(planId).active)

        when:
        ArrayList<CFService> cfServices = new ArrayList<>()
        cfServices.add(cfServiceRepository.findByGuid(serviceId))
        ArrayList<Plan> plans = new ArrayList<>()
        plans.add(planRepository.findByGuid(planId))
        testHelperService.setServicesAndPlansToActive(cfServices, plans)

        then:
        assert (cfServiceRepository.findByGuid(serviceId).active)
        assert (planRepository.findByGuid(planId).active)

        cleanup:
        serviceBrokerClientExtended.deleteServiceInstance(DeleteServiceInstanceRequest.
                builder().
                serviceDefinitionId(serviceId).
                planId(planId).
                serviceInstanceId(serviceInstanceId).
                asyncAccepted(true).
                build())
    }

    private ServiceBrokerClientExtended createServiceBrokerClient() {
        return new ServiceBrokerClientExtended(
                new RestTemplate(),
                "http://localhost:8080",
                serviceLifeCycler.cfAdminUser.username,
                serviceLifeCycler.cfAdminUser.password,
                serviceLifeCycler.cfExtUser.username,
                serviceLifeCycler.cfExtUser.password)
    }
}
