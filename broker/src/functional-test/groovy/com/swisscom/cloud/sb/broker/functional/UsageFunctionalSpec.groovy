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

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider

class UsageFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('DummySynchronousService', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "it should get usage data for an existing service instance"() {
        given:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        when:
        def response = serviceBrokerClient.getUsage(serviceLifeCycler.serviceInstanceId)

        then:
        response.statusCode.'2xxSuccessful'
        response.body.value.length() > 0
    }


    def "it should get usage data for a deleted service instance"() {
        given:
        serviceLifeCycler.deleteServiceInstanceAndAssert(false)

        when:
        def response = serviceBrokerClient.getUsage(serviceLifeCycler.serviceInstanceId)

        then:
        response.statusCode.'2xxSuccessful'
        response.body.value.length() > 0
    }
}