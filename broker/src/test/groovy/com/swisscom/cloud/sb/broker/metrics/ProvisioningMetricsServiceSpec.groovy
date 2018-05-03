package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import spock.lang.Specification

class ProvisioningMetricsServiceSpec extends Specification {
    private ProvisioningMetricsService provisioningMetricsService
    private ServiceInstanceRepository serviceInstanceRepository

    def setup() {
        provisioningMetricsService = Mock(ProvisioningMetricsService)
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
    }

    def "retrieve metrics for total nr of provision requests"() {
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def serviceInstance = new ServiceInstance()
        serviceInstanceList.add(serviceInstance)

        given:
        serviceInstanceRepository.findAll() >> serviceInstanceList

        expect:
        null
    }

}
