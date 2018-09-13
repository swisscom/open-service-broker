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

import com.swisscom.cloud.sb.broker.cfapi.dto.UpdateDto
import com.swisscom.cloud.sb.broker.context.ServiceContextPersistenceService
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.ServiceContext
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.updating.UpdateResponse
import com.swisscom.cloud.sb.broker.updating.UpdatingService
import spock.lang.Specification

class UpdatingControllerSpec extends Specification {
    private ServiceInstanceRepository serviceInstanceRepository
    private PlanRepository planRepository
    private UpdatingService updatingService
    private ServiceContextPersistenceService serviceContextService

    def setup() {
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        planRepository = Mock(PlanRepository)
        updatingService = Mock(UpdatingService)
        serviceContextService = Mock()
    }

    def "Throws Exception when ServiceInstance does not exist"() {
        def sut = new UpdatingController( serviceInstanceRepository, planRepository, updatingService)
        def notExisitingGuid = "DoesNotExist";
        def updateRequestDto = new UpdateDto()
        def acceptIncomplete = true;

        when:
            sut.update(notExisitingGuid, acceptIncomplete, updateRequestDto)

        then:
            def exception = thrown(Exception)
            exception.message == ErrorCode.SERVICE_INSTANCE_NOT_FOUND.description

    }

    def "Doesn't throw Exception when ServiceInstance exists"() {
        def sut = new UpdatingController( serviceInstanceRepository, planRepository, updatingService)
        sut.serviceContextService = serviceContextService
        def existingGuid = "DoesExist";
        def updateRequestDto = new UpdateDto()
        def acceptIncomplete = true;

        serviceContextService.findOrCreate(updateRequestDto.context) >> new ServiceContext()
        serviceInstanceRepository.findByGuid(existingGuid) >> new ServiceInstance()
        updatingService.update(*_) >> new UpdateResponse()

        when:
            sut.update(existingGuid, acceptIncomplete, updateRequestDto)

        then:
            noExceptionThrown()
    }
}
