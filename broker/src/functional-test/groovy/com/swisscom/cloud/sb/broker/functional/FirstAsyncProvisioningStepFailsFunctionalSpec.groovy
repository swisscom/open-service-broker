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


import com.swisscom.cloud.sb.broker.util.test.DummyFailingServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState

import static com.swisscom.cloud.sb.broker.services.ServiceProviderLookup.findInternalName

class FirstAsyncProvisioningStepFailsFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('AsyncDummyFailing',
                                                      findInternalName(DummyFailingServiceProvider))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "Service Instance is created when async provision request returned HttpStatus.ACCEPTED even first async step fails"() {
        when:
        serviceLifeCycler.createServiceInstanceAndAssert(DummyFailingServiceProvider.RETRY_INTERVAL_IN_SECONDS * 20,
                                                         true,
                                                         true,
                                                         ['delay': String.valueOf(DummyFailingServiceProvider.RETRY_INTERVAL_IN_SECONDS)])
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.FAILED
    }

    def "Failed Service Instance can be deleted"() {
        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true,
                                                         DummyFailingServiceProvider.RETRY_INTERVAL_IN_SECONDS * 20)
        then:
        noExceptionThrown()
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
    }
}