package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import io.micrometer.core.instrument.MeterRegistry
import spock.lang.Specification

class LifecycleTimeMetricsSpec extends Specification {

    private ServiceInstanceRepository serviceInstanceRepository
    private CFServiceRepository cfServiceRepository
    private LastOperationRepository lastOperationRepository
    private PlanRepository planRepository
    private MeterRegistry meterRegistry
    private LifecycleTimeMetrics lifecylceTimeMetrics

    private final int TIME_INTERVAL_BETWEEN_CREATION_AND_DELETION1 = 20
    private final int TIME_INTERVAL_BETWEEN_CREATION_AND_DELETION2 = 10

    private final int MILLISECONDS_PER_SECOND = 1000

    def setup() {
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        cfServiceRepository = Mock(CFServiceRepository)
        lastOperationRepository = Mock(LastOperationRepository)
        planRepository = Mock(PlanRepository)
        meterRegistry = Mock(MeterRegistry)
        cfServiceRepository.findAll() >> new ArrayList<CFService>()

        lifecylceTimeMetrics = new LifecycleTimeMetrics(serviceInstanceRepository, cfServiceRepository, lastOperationRepository, planRepository, meterRegistry)
        }

    def "retrieve mean lifecycle time per service"() {
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def serviceInstance = new ServiceInstance()
        def plan = new Plan()
        def service = new CFService()

        and:
        serviceInstance.dateCreated = Date.parse("yyyy-MM-dd hh:mm:ss", "2018-05-16 11:00:00")
        serviceInstance.completed = true
        serviceInstance.deleted = true
        serviceInstance.dateDeleted = Date.parse("yyyy-MM-dd hh:mm:ss", "2018-05-16 11:00:20")

        and:
        serviceInstance.plan = plan
        service.name = "service"
        serviceInstance.plan.service = service

        when:
        serviceInstanceList.add(serviceInstance)

        then:
        def lifecycleTimePerService = lifecylceTimeMetrics.calculateLifecycleTimePerService(serviceInstanceList)

        expect:
        lifecylceTimeMetrics.totalNrOfDeleteInstancesPerService.size() == 1
        lifecylceTimeMetrics.totalNrOfDeleteInstancesPerService.get(service.name) == 1

        and:
        lifecylceTimeMetrics.totalLifecycleTimePerService.size() == 1
        lifecylceTimeMetrics.totalLifecycleTimePerService.get(service.name) == TIME_INTERVAL_BETWEEN_CREATION_AND_DELETION1 * MILLISECONDS_PER_SECOND

        and:
        lifecycleTimePerService.size() == 1
        lifecycleTimePerService.get(service.name) == TIME_INTERVAL_BETWEEN_CREATION_AND_DELETION1 * MILLISECONDS_PER_SECOND
    }

    def "retrieve mean lifecycle time per service with multiple service instances"() {
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def serviceInstance1 = new ServiceInstance()
        def serviceInstance2 = new ServiceInstance()
        def plan = new Plan()
        def service = new CFService()

        and:
        serviceInstance1.dateCreated = Date.parse("yyyy-MM-dd hh:mm:ss", "2018-05-16 11:00:00")
        serviceInstance1.completed = true
        serviceInstance1.deleted = true
        serviceInstance1.dateDeleted = Date.parse("yyyy-MM-dd hh:mm:ss", "2018-05-16 11:00:20")

        and:
        serviceInstance1.plan = plan
        service.name = "service"
        serviceInstance1.plan.service = service
        serviceInstanceList.add(serviceInstance1)

        and:
        serviceInstance2.dateCreated = Date.parse("yyyy-MM-dd hh:mm:ss", "2018-05-16 11:00:00")
        serviceInstance2.completed = true
        serviceInstance2.deleted = true
        serviceInstance2.dateDeleted = Date.parse("yyyy-MM-dd hh:mm:ss", "2018-05-16 11:00:10")

        and:
        serviceInstance2.plan = plan
        serviceInstance2.plan.service = service

        when:
        serviceInstanceList.add(serviceInstance2)

        then:
        def lifecycleTimePerService = lifecylceTimeMetrics.calculateLifecycleTimePerService(serviceInstanceList)

        expect:
        lifecylceTimeMetrics.totalNrOfDeleteInstancesPerService.size() == 1
        lifecylceTimeMetrics.totalNrOfDeleteInstancesPerService.get(service.name) == 2

        and:
        lifecylceTimeMetrics.totalLifecycleTimePerService.size() == 1
        lifecylceTimeMetrics.totalLifecycleTimePerService.get(service.name) == (TIME_INTERVAL_BETWEEN_CREATION_AND_DELETION1 + TIME_INTERVAL_BETWEEN_CREATION_AND_DELETION2) * MILLISECONDS_PER_SECOND

        and:
        lifecycleTimePerService.size() == 1
        lifecycleTimePerService.get(service.name) == ((TIME_INTERVAL_BETWEEN_CREATION_AND_DELETION1 + TIME_INTERVAL_BETWEEN_CREATION_AND_DELETION2)/2) * MILLISECONDS_PER_SECOND
    }
}
