package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import spock.lang.Specification

class BindingMetricsServiceSpec extends Specification {
    private ServiceInstanceRepository serviceInstanceRepository
    private LastOperationRepository lastOperationRepository
    private ServiceBindingRepository serviceBindingRepository
    private BindingMetricsService bindingMetricsService

    def setup() {
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        lastOperationRepository = Mock(LastOperationRepository)
        serviceBindingRepository = Mock(ServiceBindingRepository)
        bindingMetricsService = new BindingMetricsService(serviceInstanceRepository, lastOperationRepository, serviceBindingRepository)
    }

    def "retrieve total nr of successful service bindings"() {
        setup:
        def serviceBindingList = new ArrayList<ServiceBinding>()
        def serviceBinding = new ServiceBinding()

        and:
        serviceBindingList.add(serviceBinding)

        when:
        serviceBindingRepository.findAll() >> serviceBindingList

        then:
        bindingMetricsService.retrieveMetricsForTotalNrOfBindings()

        expect:
        bindingMetricsService.totalSuccessfulNrOfBindings == 1
    }

    def "retrieve total nr of sucessful service bindings per service"() {
        setup:
        def serviceBindingList = new ArrayList<ServiceBinding>()
        def serviceBinding = new ServiceBinding()
        def serviceInstance = new ServiceInstance()
        def plan = new Plan()
        def service = new CFService()

        and:
        service.name = "service"
        serviceInstance.plan = plan
        serviceInstance.plan.service = service
        serviceBinding.serviceInstance = serviceInstance
        serviceBindingList.add(serviceBinding)

        when:
        serviceBindingRepository.findAll() >> serviceBindingList

        then:
        bindingMetricsService.retrieveTotalSuccessfulBindingsPerService()

        expect:
        bindingMetricsService.totalSuccessfulBindingsPerService.size() == 1
        bindingMetricsService.totalSuccessfulBindingsPerService.get(service.name) == 1
    }

    def "dispatch and record new binding request"() {
        setup:
        def serviceBinding = new ServiceBinding()
        def serviceInstance = new ServiceInstance()
        def plan = new Plan()
        def service = new CFService()

        and:
        service.name = "service"
        serviceInstance.plan = plan
        serviceInstance.plan.service = service
        serviceBinding.serviceInstance = serviceInstance

        when:
        bindingMetricsService.setTotalBindingRequestsPerService(serviceInstance)

        then:
        bindingMetricsService.totalBindingRequestsPerService.size() == 1
        bindingMetricsService.totalBindingRequestsPerService.get(service.name) == 1
    }

    def "dispatch and record successful binding request"() {
        setup:
        def serviceBinding = new ServiceBinding()
        def serviceInstance = new ServiceInstance()
        def plan = new Plan()
        def service = new CFService()

        and:
        service.name = "service"
        serviceInstance.plan = plan
        serviceInstance.plan.service = service
        serviceBinding.serviceInstance = serviceInstance

        when:
        bindingMetricsService.setTotalBindingRequestsPerService(serviceInstance)

        then:
        bindingMetricsService.setSuccessfulBindingRequestsPerService(serviceInstance)

        expect:
        bindingMetricsService.totalBindingRequestsPerService.size() == 1
        bindingMetricsService.totalBindingRequestsPerService.get(service.name) == 1

        and:
        bindingMetricsService.totalSuccessfulBindingRequestsPerService.size() == 1
        bindingMetricsService.totalSuccessfulBindingRequestsPerService.get(service.name) == 1

        and:
        bindingMetricsService.totalFailedBindingRequestsPerService.size() == 1
        bindingMetricsService.totalFailedBindingRequestsPerService.get(service.name) == 0
    }
}
