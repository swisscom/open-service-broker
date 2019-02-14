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

import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.servicedefinition.ServiceDefinitionInitializer
import com.swisscom.cloud.sb.broker.util.Resource
import com.swisscom.cloud.sb.client.ServiceBrokerClientExtended
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.web.client.RestTemplate

class ServiceDefinitionFromServiceManifestFunctionalSpec extends BaseFunctionalSpec {

    String REDIS_SERVICE_GUID_FROM_APPLICATION_YAML = "781e8f8c-c753-4a93-95eb-17c1f745b229"
    String REDIS_XXLARGE_PLAN_GUID_FROM_APPLICATION_YAML = "7b71cf85-0e50-4509-af04-eafd3a6ad141"
    String REDIS_XLARGE_PLAN_GUID_FROM_APPLICATION_YAML = "ebe11e59-5261-4939-ac8f-0a35c3850b4e"
    String REDIS_LARGE_PLAN_GUID_FROM_APPLICATION_YAML = "ea4b1b7d-3060-4ac6-836b-e134de0e7d9b"

    ServiceBrokerClientExtended serviceBrokerClientExtended

    @Autowired
    CFServiceRepository cfServiceRepository

    @Autowired
    PlanRepository planRepository

    @Autowired
    ServiceDefinitionInitializer serviceDefinitionInitializer

    def "read service definition from service manifest as active"() {
        when:
        def redisService = cfServiceRepository.findByGuid(REDIS_SERVICE_GUID_FROM_APPLICATION_YAML)
        def largePlan = planRepository.findByGuid(REDIS_LARGE_PLAN_GUID_FROM_APPLICATION_YAML)
        def xlargePlan = planRepository.findByGuid(REDIS_XLARGE_PLAN_GUID_FROM_APPLICATION_YAML)
        def xxlargePlan = planRepository.findByGuid(REDIS_XXLARGE_PLAN_GUID_FROM_APPLICATION_YAML)

        then:
        assert (redisService.active)
        assert (largePlan.active)
        assert (xlargePlan.active)
        assert (xxlargePlan.active)
    }

    def "adding service definition via endpoint as active"() {
        given:
        serviceBrokerClientExtended = createServiceBrokerClient()
        def serviceId = "serviceDefinitionFromServiceManifestUnused"

        when:
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/unusedServiceDefinition.json"))

        then:
        assert (cfServiceRepository.findByGuid(serviceId))
        assert (cfServiceRepository.findByGuid(serviceId).active)

        cleanup:
        serviceBrokerClientExtended.deleteServiceDefinition(serviceId)
    }

    /* redis service definition in resources for integration tests service-data/redisServiceToBeUpdated is identical to service definition
     * in application.yml except for bindable = false. The service definition is created in the DB and then updated using the definition
     * in the application.yml
     */
    def "update service definition by updating it in the service manifest"() {
        given:
        serviceBrokerClientExtended = createServiceBrokerClient()

        and:
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/redisServiceToBeUpdated.json"))
        assert (!cfServiceRepository.findByGuid(REDIS_SERVICE_GUID_FROM_APPLICATION_YAML).bindable)

        when:
        serviceDefinitionInitializer.init()

        then:
        assert (cfServiceRepository.findByGuid(REDIS_SERVICE_GUID_FROM_APPLICATION_YAML).bindable)

        and:
        noExceptionThrown()
    }

    def "delete service definition from db that is not in config and has no service instances"() {
        given:
        serviceBrokerClientExtended = createServiceBrokerClient()
        def serviceId = "serviceDefinitionFromServiceManifestUnused"

        and:
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/unusedServiceDefinition.json"))
        assert (cfServiceRepository.findByGuid(serviceId))

        when:
        serviceDefinitionInitializer.init()

        then:
        assert (!cfServiceRepository.findByGuid(serviceId))
    }

    def "flag service definition that is in DB but not in config and has service instance"() {
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

        when:
        serviceDefinitionInitializer.init()

        then:
        assert (!cfServiceRepository.findByGuid(serviceId).active)
        assert (!planRepository.findByGuid(planId).active)
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
