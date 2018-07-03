package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.*
import io.micrometer.core.instrument.MeterRegistry
import spock.lang.Specification

class BindingMetricsServiceSpec extends Specification {
    private ServiceInstanceRepository serviceInstanceRepository
    private LastOperationRepository lastOperationRepository
    private ServiceBindingRepository serviceBindingRepository
    private BindingMetricsServiceService bindingMetricsService
    private CFServiceRepository cfServiceRepository
    private PlanRepository planRepository
    private MeterRegistry meterRegistry


    def setup() {
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        lastOperationRepository = Mock(LastOperationRepository)
        serviceBindingRepository = Mock(ServiceBindingRepository)
        cfServiceRepository = Mock(CFServiceRepository)
        planRepository = Mock(PlanRepository)
        meterRegistry = Mock(MeterRegistry)
        planRepository.findAll() >> new ArrayList<Plan>()

        cfServiceRepository.findAll() >> new ArrayList<CFService>()
        serviceBindingRepository.findAll() >> new ArrayList<ServiceBinding>()

        bindingMetricsService = new BindingMetricsServiceService(serviceInstanceRepository, cfServiceRepository, lastOperationRepository, serviceBindingRepository, planRepository, meterRegistry)
    }

    def "retrieve total nr of successful service bindings"() {
        setup:
        def serviceBindingList = new ArrayList<ServiceBinding>()
        def serviceBinding = new ServiceBinding()

        when:
        serviceBindingList.add(serviceBinding)

        then:
        def totalNrOfSuccessfulBindings = bindingMetricsService.retrieveMetricsForTotalNrOfSuccessfulBindings(serviceBindingList)

        expect:
        totalNrOfSuccessfulBindings == 1
    }

    def "retrieve total nr of successful service bindings per service"() {
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

        when:
        serviceBindingList.add(serviceBinding)

        then:
        def totalNrOfSuccessfulBindingsPerService = bindingMetricsService.retrieveTotalNrOfSuccessfulBindingsPerService(serviceBindingList, cfServiceRepository)

        expect:
        totalNrOfSuccessfulBindingsPerService.size() == 1
        totalNrOfSuccessfulBindingsPerService.get(service.name) == 1
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
