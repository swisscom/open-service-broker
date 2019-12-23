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

import com.swisscom.cloud.sb.broker.services.credential.ServiceBindingPersistenceService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.StringGenerator
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

import static com.swisscom.cloud.sb.broker.services.ServiceProviderLookup.findInternalName

class BindingParametersFunctionalSpec extends BaseFunctionalSpec {

    @Autowired
    protected ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    protected ServiceBindingRepository serviceBindingRepository
    @Autowired
    private ServiceBindingPersistenceService serviceBindingPersistenceService

    def setupSpec() {
        System.setProperty('http.nonProxyHosts', 'localhost|127.0.0.1|uaa.service.cf.internal|credhub.service.consul')
        System.setProperty('javax.net.ssl.keyStore',
                           FileUtils.getFile('src/functional-test/resources/credhub_client.jks').toURI().getPath())
        System.setProperty('javax.net.ssl.keyStorePassword', 'changeit')
        System.setProperty('javax.net.ssl.trustStore',
                           FileUtils.getFile('src/functional-test/resources/credhub_client.jks').toURI().getPath())
        System.setProperty('javax.net.ssl.trustStorePassword', 'changeit')
    }

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummy',
                                                      findInternalName(
                                                              DummySynchronousServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance and bind with parameters"() {
        given:
        def serviceBindingGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        String username = StringGenerator.randomUuid()
        String password = StringGenerator.randomUuid()

        when:
        serviceLifeCycler.bindServiceInstanceAndAssert(serviceBindingGuid, [username: username, password: password])

        then:
        noExceptionThrown()

        def serviceBinding = serviceBindingRepository.findByGuid(serviceBindingGuid)
        serviceBinding != null
        serviceBinding.credentials != null
        serviceBinding.applicationUser.username == cfAdminUser.username

        cleanup:
        serviceBindingRepository.delete(serviceBindingRepository.findByGuid(serviceBindingGuid))
        serviceInstanceRepository.deleteByGuid(serviceLifeCycler.getServiceInstanceId())
    }

    def "provision async service instance and bind with parameters with bindings not retrievable"() {
        given:
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummy',
                                                      findInternalName(
                                                              DummySynchronousServiceProvider.class),
                                                      null,
                                                      null,
                                                      null,
                                                      0,
                                                      false,
                                                      false)

        def serviceInstanceGuid = UUID.randomUUID().toString()
        def serviceBindingGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)

        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false,)

        when:
        serviceLifeCycler.bindServiceInstanceAndAssert(serviceBindingGuid, ['key1': 'value1'])
        serviceBrokerClient.getServiceInstanceBinding(serviceInstanceGuid, serviceBindingGuid)

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.BAD_REQUEST

        cleanup:
        serviceBindingRepository.delete(serviceBindingRepository.findByGuid(serviceBindingGuid))
    }

    def "provision async service instance and bind with parameters with bindings retrievable"() {
        given:
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummyInstancesRetrievable',
                                                      findInternalName(
                                                              DummySynchronousServiceProvider.class),
                                                      null,
                                                      null,
                                                      null,
                                                      0,
                                                      true,
                                                      true)

        def serviceInstanceGuid = UUID.randomUUID().toString()
        def serviceBindingGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false,)

        when:
        serviceLifeCycler.bindServiceInstanceAndAssert(serviceBindingGuid, ['key1': 'value1'])
        def bindingResponse = serviceBrokerClient.getServiceInstanceBinding(serviceInstanceGuid, serviceBindingGuid)

        then:
        noExceptionThrown()
        bindingResponse != null
        bindingResponse.body.credentials != null
        bindingResponse.body.parameters != null

        cleanup:
        serviceBindingRepository.delete(serviceBindingRepository.findByGuid(serviceBindingGuid))
    }

    def "provision async service instance and fetch non existing binding"() {
        given:
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummyInstancesRetrievable',
                                                      findInternalName(
                                                              DummySynchronousServiceProvider.class),
                                                      null,
                                                      null,
                                                      null,
                                                      0,
                                                      true,
                                                      true)

        def serviceInstanceGuid = UUID.randomUUID().toString()
        def serviceBindingGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false,)

        when:
        serviceBrokerClient.getServiceInstanceBinding(serviceInstanceGuid, serviceBindingGuid)

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.NOT_FOUND
    }

    def "provision async service instance and unbind non existing binding"() {
        given:
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummyInstancesRetrievable',
                                                      findInternalName(
                                                              DummySynchronousServiceProvider.class),
                                                      null,
                                                      null,
                                                      null,
                                                      0,
                                                      true,
                                                      true)

        def serviceInstanceGuid = UUID.randomUUID().toString()
        def serviceBindingGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false,)

        when:
        serviceBrokerClient.deleteServiceInstanceBinding(
                DeleteServiceInstanceBindingRequest.builder().
                        serviceDefinitionId(serviceLifeCycler.cfService.guid).
                        planId((serviceLifeCycler.cfService.plans[0] as Plan).guid).
                        serviceInstanceId(serviceInstanceGuid).
                        bindingId(serviceBindingGuid).
                        build())
        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.GONE
    }

    def "deprovision async service instance"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        def serviceBindingGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert()

        when:
        serviceLifeCycler.deleteServiceBindingAndServiceInstanceAndAssert()

        then:
        noExceptionThrown()
    }
}