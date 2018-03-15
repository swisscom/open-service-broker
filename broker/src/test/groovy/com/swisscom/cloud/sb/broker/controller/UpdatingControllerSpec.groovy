package com.swisscom.cloud.sb.broker.controller

import com.swisscom.cloud.sb.broker.cfapi.dto.UpdateDto
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.updating.UpdateResponse
import com.swisscom.cloud.sb.broker.updating.UpdatingService
import spock.lang.Specification

class UpdatingControllerSpec extends Specification {
    private ServiceInstanceRepository serviceInstanceRepository
    private PlanRepository planRepository
    private UpdatingService updatingService

    def setup() {
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        planRepository = Mock(PlanRepository)
        updatingService = Mock(UpdatingService)
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
        def existingGuid = "DoesExist";
        def updateRequestDto = new UpdateDto()
        def acceptIncomplete = true;

        serviceInstanceRepository.findByGuid(existingGuid) >> new ServiceInstance()
        updatingService.update(*_) >> new UpdateResponse()

        when:
            sut.update(existingGuid, acceptIncomplete, updateRequestDto)

        then:
            noExceptionThrown()
    }
}
