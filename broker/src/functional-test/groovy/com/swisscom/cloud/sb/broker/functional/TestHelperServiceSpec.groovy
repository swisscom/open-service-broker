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

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.servicedefinition.ServiceDefinitionInitializer
import com.swisscom.cloud.sb.broker.util.Resource
import com.swisscom.cloud.sb.client.ServiceBrokerClientExtended
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest
import org.springframework.web.client.RestTemplate

class TestHelperServiceSpec extends BaseFunctionalSpec {

    ServiceBrokerClientExtended serviceBrokerClientExtended

    @Autowired
    CFServiceRepository cfServiceRepository

    @Autowired
    PlanRepository planRepository

    @Autowired
    ServiceDefinitionInitializer serviceDefinitionInitializer

    def "set all services and plans to active"() {
        given:
        serviceBrokerClientExtended = createServiceBrokerClient()
        def serviceId = "serviceDefinitionWithInstance"
        def planId = "planForServiceDefinitionFromServiceManifestWithInstance"

        and:
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/serviceDefinitionWithInstance.json"))
        assert (cfServiceRepository.findByGuid(serviceId))
        assert (planRepository.findByGuid(planId))

        and:
        def serviceInstanceId = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(serviceId, planId, null, null, null).withServiceInstanceId(serviceInstanceId).withAsyncAccepted(false))

        and:
        serviceDefinitionInitializer.init()

        and:
        assert (!cfServiceRepository.findByGuid(serviceId).active)
        assert (!planRepository.findByGuid(planId).active)

        when:
        activateAllServicesAndPlans()

        then:
        assert (cfServiceRepository.findByGuid(serviceId).active)
        assert (planRepository.findByGuid(planId).active)
    }

    def "set selected services and plans to active"() {
        given:
        serviceBrokerClientExtended = createServiceBrokerClient()
        def serviceId = "serviceDefinitionWithInstance"
        def planId = "planForServiceDefinitionFromServiceManifestWithInstance"

        and:
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/serviceDefinitionWithInstance.json"))
        assert (cfServiceRepository.findByGuid(serviceId))
        assert (planRepository.findByGuid(planId))

        and:
        def serviceInstanceId = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(serviceId, planId, null, null, null).withServiceInstanceId(serviceInstanceId).withAsyncAccepted(false))

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
        activateServicesAndPlans(cfServices, plans)

        then:
        assert (cfServiceRepository.findByGuid(serviceId).active)
        assert (planRepository.findByGuid(planId).active)
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
