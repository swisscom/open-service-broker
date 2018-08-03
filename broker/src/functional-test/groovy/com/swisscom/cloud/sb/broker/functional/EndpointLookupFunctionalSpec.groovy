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

import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.ServiceLifeCycler
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider
import org.springframework.beans.factory.annotation.Autowired

class EndpointLookupFunctionalSpec extends BaseFunctionalSpec {

    @Autowired
    ServiceInstanceRepository serviceInstanceRepository

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummyServiceManagerBased', ServiceProviderLookup.findInternalName(DummyServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "should get a non empty response for endpoints of a service manager based service"() {
        given:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        when:
        def response = serviceBrokerClient.getEndpoint(serviceLifeCycler.serviceInstanceId)
        then:
        response.statusCode.'2xxSuccessful'
        response.body.size() > 0

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(false)
    }

    def "should get an empty response for a *NON* service manager based service"() {
        given:
        ServiceLifeCycler lifeCycler = applicationContext.getBean(ServiceLifeCycler.class)
        lifeCycler.createServiceIfDoesNotExist('SynchronousDummy', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class))
        lifeCycler.createServiceInstanceAndAssert(0, false, false)
        when:
        def response = serviceBrokerClient.getEndpoint(lifeCycler.serviceInstanceId)
        then:
        response.statusCode.'2xxSuccessful'
        response.body.size() == 0

        cleanup:
        lifeCycler.deleteServiceInstanceAndAssert(false)
        serviceInstanceRepository.delete(serviceInstanceRepository.findByGuid(lifeCycler.serviceInstanceId))
    }
}