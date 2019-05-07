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
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class BindingForFailedProvisioningFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('AsyncDummyServiceManagerBased', ServiceProviderLookup.findInternalName(DummyServiceProvider))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance and bind immediately"() {
        given:
        serviceLifeCycler.createServiceInstanceAndAssert(0, true, true, ['success': true, 'delay': String.valueOf(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 2)])
        assert serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.IN_PROGRESS

        when:
        serviceLifeCycler.requestBindService('someKindaServiceBindingGuid', [] as Map, null, serviceLifeCycler.cfService.guid, serviceLifeCycler.plan.guid)

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.PRECONDITION_FAILED
    }

    def "provision async service instance which fails and try to bind"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)

        and:
        // add +30s because of the startup delay of the quartz scheduler
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS + 30, true, true, ['success': false])
        assert serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.FAILED

        when:
        serviceLifeCycler.requestBindService('someKindaServiceBindingGuid', [] as Map, null, serviceLifeCycler.cfService.guid, serviceLifeCycler.plan.guid)

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.PRECONDITION_FAILED
    }

    def "deprovision async service instance"() {
        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, 35)
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
    }
}