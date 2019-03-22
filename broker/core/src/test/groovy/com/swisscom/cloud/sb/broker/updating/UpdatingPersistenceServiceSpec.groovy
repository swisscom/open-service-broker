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

package com.swisscom.cloud.sb.broker.updating

import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.model.repository.UpdateRequestRepository
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import spock.lang.Specification

class UpdatingPersistenceServiceSpec extends Specification {
    private UpdateRequestRepository updateRequestRepository
    private ServiceInstanceRepository serviceInstanceRepository
    private ProvisioningPersistenceService provisioningPersistenceService
    private String oldParameters
    UpdatingPersistenceService sut = new UpdatingPersistenceService(updateRequestRepository, serviceInstanceRepository, provisioningPersistenceService)

    def setup() {
        updateRequestRepository = Mock(UpdateRequestRepository)
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        provisioningPersistenceService = Mock(ProvisioningPersistenceService)
        oldParameters = '{"keyA":"Value-A","keyB":"Value-B"}'
        sut = new UpdatingPersistenceService(updateRequestRepository, serviceInstanceRepository, provisioningPersistenceService)
    }

    def "Merge with empty Parameters does not change values"() {
        setup:
        def updateParameters = ''

        when:
        def result = sut.mergeServiceInstanceParameter(oldParameters, updateParameters)

        then:
        result == oldParameters
    }

    def "Merge with empty object Parameters does not change values"() {
        setup:
        def updateParameters = '{}'

        when:
        def result = sut.mergeServiceInstanceParameter(oldParameters, updateParameters)

        then:
        result == oldParameters
    }

    def "Merge adds value"() {
        setup:
        def updateParameters = '{"keyA":"Value-A","keyB":"Value-B","keyC":"Value-C"}'

        when:
        def result = sut.mergeServiceInstanceParameter(oldParameters, updateParameters)

        then:
        def resultMap = sut.toMap(result)
        resultMap.find({ key, value -> key == "keyC" }).value == "Value-C"
    }

    def "Merge does not remove value"() {
        setup:
        def updateParameters = '{"keyB":"Value-B"}'

        when:
        def result = sut.mergeServiceInstanceParameter(oldParameters, updateParameters)

        then:
        def resultMap = sut.toMap(result)
        resultMap.find({ key, value -> key == "keyA" }).value == "Value-A"
    }

    def "Merge updates value"() {
        setup:
        def updateParameters = '{"keyA":"Value-NEWA","keyB":"Value-NEWB"}'

        when:
        def result = sut.mergeServiceInstanceParameter(oldParameters, updateParameters)

        then:
        def resultMap = sut.toMap(result)
        resultMap.find({ key, value -> key == "keyA" }).value == "Value-NEWA"
        resultMap.find({ key, value -> key == "keyB" }).value == "Value-NEWB"
    }
}
