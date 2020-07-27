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

import com.swisscom.cloud.sb.broker.binding.ServiceBindingPersistenceService
import com.swisscom.cloud.sb.broker.cfextensions.ServiceInstancePurgeInformation
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationPersistenceService
import com.swisscom.cloud.sb.broker.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.repository.PlanRepository
import com.swisscom.cloud.sb.broker.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.ServiceProviderLookup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification
import spock.lang.Unroll

import static java.time.Duration.ofMillis

@ContextConfiguration
@ActiveProfiles(["default","test","secrets"])
@SpringBootTest(properties = "spring.autoconfigure.exclude=com.swisscom.cloud.sb.broker.util.httpserver.WebSecurityConfig,org.springframework.cloud.servicebroker.autoconfigure.web.ServiceBrokerAutoConfiguration,org.springframework.cloud.servicebroker.autoconfigure.web.servlet.ServiceBrokerWebMvcAutoConfiguration",
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.
        ASPECTJ, pattern = "com.swisscom.cloud.sb.broker.util.httpserver.*"))
class AdminControllerSpec extends Specification {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminControllerSpec.class)

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    private LastOperationRepository lastOperationRepository

    @Autowired
    private ProvisioningPersistenceService provisioningPersistenceService

    @Autowired
    private LastOperationPersistenceService lastOperationPersistenceService

    @Autowired
    private ServiceBindingPersistenceService serviceBindingPersistenceService

    @Autowired
    private ServiceBindingRepository serviceBindingRepository

    @Autowired
    private ServiceProviderLookup serviceProviderLookup

    @Autowired
    private PlanRepository planRepository

    private WebTestClient webTestClient

    @LocalServerPort
    private int port

    private String getBaseUrl() {
        "http://localhost:" + port.toString()
    }

    def setup() {
        LOGGER.info("Testing against Service Broker running at http://localhost:{}", port.toString())
        webTestClient = WebTestClient.bindToServer().
                responseTimeout(ofMillis(60000)).baseUrl(getBaseUrl()).
                defaultHeaders({
                    header ->
                        header.setBasicAuth("cc_ext", "change_me")
                        header.setContentType(MediaType.APPLICATION_JSON)
                        header.setAccept([MediaType.APPLICATION_JSON])
                }).
                build()
    }

    @Unroll
    def "should successfully mark service instance '#serviceInstanceGuid' for purge also remove binding '#bindingGuid'"() {
        given: "the service instance to be cleaned up"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceInstanceGuid,
                                                              plan: planRepository.findByGuid(planGuid))
        serviceInstanceRepository.save(serviceInstance)

        and: "a service binding associated to the service instance to be purged"
        serviceBindingPersistenceService.
                create(serviceInstance, '{"foo": "bar"}', "no parameters", bindingGuid, [], null, "cc_admin")

        when:
        ServiceInstancePurgeInformation result = webTestClient.delete().
                uri("/admin/service_instances/" + serviceInstanceGuid + "/purge").
                exchange().
                expectStatus().isOk().
                expectBody(ServiceInstancePurgeInformation.class).
                returnResult().getResponseBody()

        then: "should return the purged service instance"
        result != null
        result.getPurgedServiceInstanceGuid() == serviceInstanceGuid
        result.getDeletedBindings() == 1
        result.isBackupRestoreProvider() == systemBackupProvider
        if (systemBackupProvider) {
            result.getErrors().size() == 1
            result.getErrors().contains("Failed to deregister from backup system")
        } else {
            result.getErrors().size() == 0
        }

        and: "should have marked the service instance to be cleaned up"
        ServiceInstance markedServiceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        markedServiceInstance.isDeleted()
        markedServiceInstance.getDateDeleted().before(new Date())

        and: "should have created a successful deprovision last operation"
        LastOperation lastOperation = lastOperationRepository.findByGuid(serviceInstanceGuid)
        lastOperation.getOperation() == LastOperation.Operation.DEPROVISION
        lastOperation.getStatus() == LastOperation.Status.SUCCESS

        and: "should have removed the binding"
        ServiceBinding serviceBinding = serviceBindingRepository.findByGuid(bindingGuid)
        serviceBinding == null

        where:
        serviceInstanceGuid << [UUID.randomUUID().toString(), UUID.randomUUID().toString()]
        bindingGuid << [UUID.randomUUID().toString(), UUID.randomUUID().toString()]
        planGuid << ["0ef19631-1212-47cc-9c77-22d78ddaae3a", "47273c6a-ff8b-40d6-9981-2b25663718a1"]
        systemBackupProvider << [false, true]
    }

    @Unroll
    def "should fail to mark a service instance for purge which does not exist for service instance guid: '#serviceInstanceGuid'"() {
        when:
        ServiceInstancePurgeInformation result = webTestClient.delete().
                uri("/admin/service_instances/" + serviceInstanceGuid + "/purge").
                exchange().
                expectStatus().isBadRequest().
                expectBody(ServiceInstancePurgeInformation.class).
                returnResult().getResponseBody()

        then: "should return the purged service instance"
        result != null
        result.getErrors().size() == 1
        result.getErrors().contains(String.format(message, serviceInstanceGuid))

        where:
        serviceInstanceGuid          | message
        UUID.randomUUID().toString() | "Service Instance Guid '%s' does not exist"
        null                         | "Service Instance Guid 'null' does not exist"
        " "                          | "Service Instance Guid cannot be empty"
    }
}
