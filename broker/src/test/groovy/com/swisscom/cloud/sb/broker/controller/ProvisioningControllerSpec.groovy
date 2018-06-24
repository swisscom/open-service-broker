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

package com.swisscom.cloud.sb.broker.controller

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import spock.lang.Specification

class ProvisioningControllerSpec extends Specification {

    private ProvisioningController provisioningController

    def setup() {
        provisioningController = new ProvisioningController()
    }

    def 'service creation success can always be deleted'() {
        given:
        def serviceInstanceRepository = Mock(ServiceInstanceRepository)
        serviceInstanceRepository.findByGuid(_) >> new ServiceInstance(completed: true)
        def serviceInstanceRepositoryField = provisioningController.getClass().getSuperclass().getDeclaredField('serviceInstanceRepository')
        serviceInstanceRepositoryField.setAccessible(true)
        serviceInstanceRepositoryField.set(provisioningController, serviceInstanceRepository)
        when:
        provisioningController.createDeprovisionRequest("foo", false)

        then:
        noExceptionThrown()
    }

    def 'service creation failed can always be deleted'() {
        given:
        def serviceInstanceRepository = Mock(ServiceInstanceRepository)
        serviceInstanceRepository.findByGuid(_) >> new ServiceInstance(completed: false)
        def serviceInstanceRepositoryField = provisioningController.getClass().getSuperclass().getDeclaredField('serviceInstanceRepository')
        serviceInstanceRepositoryField.setAccessible(true)
        serviceInstanceRepositoryField.set(provisioningController, serviceInstanceRepository)

        when:
        provisioningController.createDeprovisionRequest("foo", false)

        then:
        noExceptionThrown()
    }

    def 'delete deleted service throws exception'() {
        given:
        def serviceInstanceRepository = Mock(ServiceInstanceRepository)
        serviceInstanceRepository.findByGuid(_) >> new ServiceInstance(deleted: true)
        def serviceInstanceRepositoryField = provisioningController.getClass().getSuperclass().getDeclaredField('serviceInstanceRepository')
        serviceInstanceRepositoryField.setAccessible(true)
        serviceInstanceRepositoryField.set(provisioningController, serviceInstanceRepository)

        when:
        provisioningController.createDeprovisionRequest("foo", false)

        then:
        thrown ServiceBrokerException
    }
}


