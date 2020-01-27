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

package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest(properties = "spring.autoconfigure.exclude=com.swisscom.cloud.sb.broker.util.httpserver.WebSecurityConfig")
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "com.swisscom.cloud.sb.broker.util.httpserver.*"))
class ProvisioningPersistenceServiceTest extends Specification {
    String serviceInstanceGuid
    final String KEY = "TESTKEY"
    final String ORIGINAL_VALUE = "ORIGINALVALUE"
    final String NEW_VALUE = "NEWVALUE"

    @Autowired
    ProvisioningPersistenceService sut

    def setup() {
        serviceInstanceGuid = UUID.randomUUID().toString()
    }

    def "Returned details value is updated when servicedetail is marked as uniqueKey"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid)
        ProvisionResponse provisionResponse = new ProvisionResponse(details: [new ServiceDetail(key: KEY,
                                                                                                value: ORIGINAL_VALUE,
                                                                                                uniqueKey: true)])
        ServiceInstance createdServiceInstance = sut.createServiceInstance(provisionRequest, provisionResponse)
        println(serviceInstanceGuid)

        when:
        sut.updateServiceDetails([new ServiceDetail(key: KEY, value: NEW_VALUE, uniqueKey: true)],
                                 createdServiceInstance)
        ServiceDetailsHelper serviceDetailsHelperUpdated = ServiceDetailsHelper.from(sut.getServiceInstance(
                serviceInstanceGuid))

        then:
        serviceDetailsHelperUpdated.getValue(KEY) == NEW_VALUE
        cleanup:
        sut.deleteServiceInstance(sut.getServiceInstance(serviceInstanceGuid))
    }

    def "Returned details value is not updated when servicedetail is not marked as uniqueKey"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid)
        ProvisionResponse provisionResponse = new ProvisionResponse(details: [new ServiceDetail(key: KEY,
                                                                                                value: ORIGINAL_VALUE,
                                                                                                uniqueKey: false)])
        ServiceInstance createdServiceInstance = sut.createServiceInstance(provisionRequest, provisionResponse)
        println(serviceInstanceGuid)

        when:
        sut.updateServiceDetails([new ServiceDetail(key: KEY, value: NEW_VALUE, uniqueKey: false)],
                                 createdServiceInstance)
        ServiceDetailsHelper serviceDetailsHelperUpdated = ServiceDetailsHelper.from(sut.getServiceInstance(
                serviceInstanceGuid))

        then:
        serviceDetailsHelperUpdated.getValue(KEY) == ORIGINAL_VALUE
        cleanup:
        sut.deleteServiceInstance(sut.getServiceInstance(serviceInstanceGuid))
    }
}
