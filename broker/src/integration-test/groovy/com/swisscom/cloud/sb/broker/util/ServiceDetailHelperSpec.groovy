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

package com.swisscom.cloud.sb.broker.util

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceDetailRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification


class ServiceDetailHelperSpec extends BaseSpecification {

    @Autowired
    ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    ServiceDetailRepository serviceDetailRepository

    def 'Can resolve serviceDetail by key and value'() {
        given:
        def key = UUID.randomUUID().toString()
        def value = "_custValue"
        def  detail = ServiceDetail.from(key, value, true)
        def serviceInstance = new ServiceInstance(
                guid: UUID.randomUUID().toString(),
                details: [ detail ]
        )
        serviceDetailRepository.save(detail)
        serviceInstanceRepository.saveAndFlush(serviceInstance)

        when:
        def serviceDetail = serviceDetailRepository.findByKeyAndValue(key, value)

        then:
        noExceptionThrown()
        assert serviceDetail != null
        assert serviceDetail.key == key
        assert serviceDetail.value == value

        cleanup:
        serviceInstanceRepository.delete(serviceInstance)
        serviceDetailRepository.delete(detail)
    }

    def 'Can resolve serviceInstance by key and value'() {
        given:
        def key = UUID.randomUUID().toString()
        def value = "_custValue"
        def uuid = UUID.randomUUID().toString()
        def detail = ServiceDetail.from(key, value, true)
        def serviceInstance = new ServiceInstance(
                guid: uuid,
                details: [ detail ]
        )
        serviceDetailRepository.save(detail)
        serviceInstanceRepository.saveAndFlush(serviceInstance)

        when:
        def serviceDetail = serviceDetailRepository.findByKeyAndValue(key, value)

        then:
        noExceptionThrown()
        assert serviceDetail != null
        assert serviceDetail.key == key
        assert serviceDetail.value == value
        assert serviceDetail.serviceInstance != null
        assert serviceDetail.serviceInstance.guid == uuid

        cleanup:
        serviceInstanceRepository.delete(serviceInstance)
        serviceDetailRepository.delete(detail)
    }
}