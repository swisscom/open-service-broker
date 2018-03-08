package com.swisscom.cloud.sb.broker.updating

import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.model.repository.UpdateRequestRepository
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import spock.lang.Specification

class UpdatingPersistenceServiceSpec extends Specification {
    private UpdateRequestRepository updateRequestRepository
    private ServiceInstanceRepository serviceInstanceRepository
    private ProvisioningPersistenceService provisioningPersistenceService

    def setup() {
        updateRequestRepository = Mock(UpdateRequestRepository)
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        provisioningPersistenceService = Mock(ProvisioningPersistenceService)
    }

    def "Merge with empty Parameters does not change values"() {
        setup:
        def sut = new UpdatingPersistenceService(updateRequestRepository, serviceInstanceRepository, provisioningPersistenceService)

        def oldParameters = '{"keyA":"Value-A","keyB":"Value-B"}'
        def updateParameters = ''

        when:
        def result = sut.mergeServiceInstanceParameter(oldParameters, updateParameters)

        then:
        result == oldParameters
    }

    def "Merge with empty object Parameters does not change values"() {
        setup:
        def sut = new UpdatingPersistenceService(updateRequestRepository, serviceInstanceRepository, provisioningPersistenceService)

        def oldParameters = '{"keyA":"Value-A","keyB":"Value-B"}'
        def updateParameters = '{}'

        when:
        def result = sut.mergeServiceInstanceParameter(oldParameters, updateParameters)

        then:
        result == oldParameters
    }

    def "Merge adds value"() {
        setup:
        def sut = new UpdatingPersistenceService(updateRequestRepository, serviceInstanceRepository, provisioningPersistenceService)

        def oldParameters = '{"keyA":"Value-A","keyB":"Value-B"}'
        def updateParameters = '{"keyA":"Value-A","keyB":"Value-B","keyC":"Value-C"}'

        when:
        def result = sut.mergeServiceInstanceParameter(oldParameters, updateParameters)

        then:
        def resultMap = sut.toMap(result)
        resultMap.find({ key, value -> key == "keyC" }).value == "Value-C"
    }

    def "Merge does not remove value"() {
        setup:
        def sut = new UpdatingPersistenceService(updateRequestRepository, serviceInstanceRepository, provisioningPersistenceService)

        def oldParameters = '{"keyA":"Value-A","keyB":"Value-B"}'
        def updateParameters = '{"keyB":"Value-B"}'

        when:
        def result = sut.mergeServiceInstanceParameter(oldParameters, updateParameters)

        then:
        def resultMap = sut.toMap(result)
        resultMap.find({ key, value -> key == "keyA" }).value == "Value-A"
    }

    def "Merge updates value"() {
        setup:
        def sut = new UpdatingPersistenceService(updateRequestRepository, serviceInstanceRepository, provisioningPersistenceService)

        def oldParameters = '{"keyA":"Value-A","keyB":"Value-B"}'
        def updateParameters = '{"keyA":"Value-NEWA","keyB":"Value-NEWB"}'

        when:
        def result = sut.mergeServiceInstanceParameter(oldParameters, updateParameters)

        then:
        def resultMap = sut.toMap(result)
        resultMap.find({ key, value -> key == "keyA" }).value == "Value-NEWA"
        resultMap.find({ key, value -> key == "keyB" }).value == "Value-NEWB"
    }
}
