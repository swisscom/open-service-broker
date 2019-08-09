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


import com.swisscom.cloud.sb.broker.services.ServiceProviderService
import com.swisscom.cloud.sb.broker.util.test.DummyServiceHealthServiceProvider
import com.swisscom.cloud.sb.model.health.ServiceHealthStatus

class ServiceHealthProviderFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist(
                'DummyServiceProvider',
                ServiceProviderService.findInternalName(DummyServiceHealthServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "Can get Health informations for service instance"() {
        given:

        serviceLifeCycler.createServiceInstanceAndAssert(
                0,
                false,
                false,
                [] as Map
        )

        when:
        def response = serviceBrokerClient.getHealth(serviceLifeCycler.serviceInstanceId)

        then:
        response.statusCode.'2xxSuccessful'
        response.body.status == ServiceHealthStatus.OK
    }
}
