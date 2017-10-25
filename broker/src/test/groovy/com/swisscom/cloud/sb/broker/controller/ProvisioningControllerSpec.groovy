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


