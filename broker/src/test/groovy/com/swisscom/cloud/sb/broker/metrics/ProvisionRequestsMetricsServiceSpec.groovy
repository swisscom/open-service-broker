package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import spock.lang.Specification

class ProvisionRequestsMetricsServiceSpec extends Specification {
    private ServiceInstanceRepository serviceInstanceRepository
    private LastOperationRepository lastOperationRepository
    private ProvisionRequestsMetricsService provisionRequestsMetricsService

    def setup() {
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        lastOperationRepository = Mock(LastOperationRepository)
        provisionRequestsMetricsService = new ProvisionRequestsMetricsService(serviceInstanceRepository, lastOperationRepository)
    }

    def "retrieve total nr of provision Requests"() {
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def serviceInstance = new ServiceInstance()
        def lastOperation = new LastOperation()

        and:
        serviceInstance.completed = true
        serviceInstanceList.add(serviceInstance)

        when:
        serviceInstanceRepository.findAll() >> serviceInstanceList
        lastOperationRepository.findByGuid(serviceInstance.guid) >> lastOperation

        then:
        provisionRequestsMetricsService.retrieveTotalMetrics()

        expect:
        provisionRequestsMetricsService.total == 1
        provisionRequestsMetricsService.totalSuccess == 1
        provisionRequestsMetricsService.totalFailure == 0
    }

    def "include deleted successfully provisioned service instances in total nr of provision requests"() {
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def deletedServiceInstance = new ServiceInstance()

        and:
        deletedServiceInstance.completed = true
        deletedServiceInstance.deleted = true
        serviceInstanceList.add(deletedServiceInstance)

        when:
        serviceInstanceRepository.findAll() >> serviceInstanceList

        then:
        provisionRequestsMetricsService.retrieveTotalMetrics()

        expect:
        provisionRequestsMetricsService.total == 1
        provisionRequestsMetricsService.totalSuccess == 1
        provisionRequestsMetricsService.totalFailure == 0

    }

    def "include deleted failed service instances in total nr of provision requests"() {
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def deletedFailedServiceInstance = new ServiceInstance()
        def lastOperation = new LastOperation()

        and:
        deletedFailedServiceInstance.completed = false
        deletedFailedServiceInstance.deleted = true
        serviceInstanceList.add(deletedFailedServiceInstance)

        when:
        serviceInstanceRepository.findAll() >> serviceInstanceList
        lastOperationRepository.findByGuid(deletedFailedServiceInstance.guid) >> lastOperation

        then:
        provisionRequestsMetricsService.retrieveTotalMetrics()

        expect:
        provisionRequestsMetricsService.total == 1
        provisionRequestsMetricsService.totalSuccess == 0
        provisionRequestsMetricsService.totalFailure == 1

    }

    def "retrieve total number of failed provision requests"() {
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def failedServiceInstance = new ServiceInstance()
        def lastOperation = new LastOperation()

        and:
        failedServiceInstance.completed = false
        serviceInstanceList.add(failedServiceInstance)

        when:
        serviceInstanceRepository.findAll() >> serviceInstanceList
        lastOperationRepository.findByGuid(failedServiceInstance.guid) >> lastOperation

        then:
        provisionRequestsMetricsService.retrieveTotalMetrics()

        expect:
        provisionRequestsMetricsService.total == 1
        provisionRequestsMetricsService.totalSuccess == 0
        provisionRequestsMetricsService.totalFailure == 1
    }

    def "retrieve total number of provision requests per service"() {
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def serviceInstance = new ServiceInstance()
        def plan = new Plan()
        def cfService = new CFService()

        and:
        cfService.name = "service"
        serviceInstance.completed = true
        serviceInstance.plan = plan
        serviceInstance.plan.service = cfService
        serviceInstanceList.add(serviceInstance)

        when:
        serviceInstanceRepository.findAll() >> serviceInstanceList

        then:
        provisionRequestsMetricsService.retrieveTotalMetricsPerService()

        expect:
        provisionRequestsMetricsService.totalPerService.size() == 1
        provisionRequestsMetricsService.totalSuccessPerService.size() == 1
        provisionRequestsMetricsService.totalFailurePerService.size() == 0

        and:
        provisionRequestsMetricsService.totalPerService.get(cfService.name) == 1
        provisionRequestsMetricsService.totalSuccessPerService.get(cfService.name) == 1
        provisionRequestsMetricsService.totalFailurePerService.get(cfService.name) == null
    }

    def "include deleted service instances in total nr of provision requests per service"(){
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def deletedServiceInstance = new ServiceInstance()
        def plan = new Plan()
        def cfService = new CFService()

        and:
        cfService.name = "service"
        deletedServiceInstance.completed = true
        deletedServiceInstance.deleted = true
        deletedServiceInstance.plan = plan
        deletedServiceInstance.plan.service = cfService
        serviceInstanceList.add(deletedServiceInstance)

        when:
        serviceInstanceRepository.findAll() >> serviceInstanceList

        then:
        provisionRequestsMetricsService.retrieveTotalMetricsPerService()

        expect:
        provisionRequestsMetricsService.totalPerService.size() == 1
        provisionRequestsMetricsService.totalSuccessPerService.size() == 1
        provisionRequestsMetricsService.totalFailurePerService.size() == 0

        and:
        provisionRequestsMetricsService.totalPerService.get(cfService.name) == 1
        provisionRequestsMetricsService.totalSuccessPerService.get(cfService.name) == 1
        provisionRequestsMetricsService.totalFailurePerService.get(cfService.name) == null
    }

    def "include deleted failed service instances in total nr of provision requests per service"(){
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def deletedFailedServiceInstance = new ServiceInstance()
        def lastOperation = new LastOperation()
        def plan = new Plan()
        def cfService = new CFService()

        and:
        cfService.name = "service"
        deletedFailedServiceInstance.completed = false
        deletedFailedServiceInstance.deleted = true
        deletedFailedServiceInstance.plan = plan
        deletedFailedServiceInstance.plan.service = cfService
        serviceInstanceList.add(deletedFailedServiceInstance)

        when:
        serviceInstanceRepository.findAll() >> serviceInstanceList
        lastOperationRepository.findByGuid(deletedFailedServiceInstance.guid) >> lastOperation


        then:
        provisionRequestsMetricsService.retrieveTotalMetricsPerService()

        expect:
        provisionRequestsMetricsService.totalPerService.size() == 1
        provisionRequestsMetricsService.totalSuccessPerService.size() == 0
        provisionRequestsMetricsService.totalFailurePerService.size() == 1

        and:
        provisionRequestsMetricsService.totalPerService.get(cfService.name) == 1
        provisionRequestsMetricsService.totalSuccessPerService.get(cfService.name) == null
        provisionRequestsMetricsService.totalFailurePerService.get(cfService.name) == 1
    }

    def "retrieve total number of provision requests that failed per service"(){
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def failedServiceInstance = new ServiceInstance()
        def lastOperation = new LastOperation()
        def plan = new Plan()
        def cfService = new CFService()

        and:
        cfService.name = "service"
        failedServiceInstance.completed = false
        failedServiceInstance.plan = plan
        failedServiceInstance.plan.service = cfService
        serviceInstanceList.add(failedServiceInstance)

        when:
        serviceInstanceRepository.findAll() >> serviceInstanceList
        lastOperationRepository.findByGuid(failedServiceInstance.guid) >> lastOperation

        then:
        provisionRequestsMetricsService.retrieveTotalMetricsPerService()

        expect:
        provisionRequestsMetricsService.totalPerService.size() == 1
        provisionRequestsMetricsService.totalSuccessPerService.size() == 0
        provisionRequestsMetricsService.totalFailurePerService.size() == 1

        and:
        provisionRequestsMetricsService.totalPerService.get(cfService.name) == 1
        provisionRequestsMetricsService.totalSuccessPerService.get(cfService.name) == null
        provisionRequestsMetricsService.totalFailurePerService.get(cfService.name) == 1
    }

    def "retrieve total number of provision requests per plan"() {
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def serviceInstance = new ServiceInstance()
        def plan = new Plan()

        and:
        serviceInstance.completed = true
        plan.name = "plan"
        serviceInstance.plan = plan
        serviceInstanceList.add(serviceInstance)

        when:
        serviceInstanceRepository.findAll() >> serviceInstanceList

        then:
        provisionRequestsMetricsService.retrieveTotalMetricsPerPlan()

        expect:
        provisionRequestsMetricsService.totalPerPlan.size() == 1
        provisionRequestsMetricsService.totalSuccessPerPlan.size() == 1
        provisionRequestsMetricsService.totalFailurePerPlan.size() == 0

        and:
        provisionRequestsMetricsService.totalPerPlan.get(plan.name) == 1
        provisionRequestsMetricsService.totalSuccessPerPlan.get(plan.name) == 1
        provisionRequestsMetricsService.totalFailurePerPlan.get(plan.name) == null
    }

    def "include deleted service instances in total nr of provision requests per plan"(){
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def deletedServiceInstance = new ServiceInstance()
        def plan = new Plan()

        and:
        deletedServiceInstance.completed = true
        deletedServiceInstance.deleted = true
        plan.name = "plan"
        deletedServiceInstance.plan = plan
        serviceInstanceList.add(deletedServiceInstance)

        when:
        serviceInstanceRepository.findAll() >> serviceInstanceList

        then:
        provisionRequestsMetricsService.retrieveTotalMetricsPerPlan()

        expect:
        provisionRequestsMetricsService.totalPerPlan.size() == 1
        provisionRequestsMetricsService.totalSuccessPerPlan.size() == 1
        provisionRequestsMetricsService.totalFailurePerPlan.size() == 0

        and:
        provisionRequestsMetricsService.totalPerPlan.get(plan.name) == 1
        provisionRequestsMetricsService.totalSuccessPerPlan.get(plan.name) == 1
        provisionRequestsMetricsService.totalFailurePerPlan.get(plan.name) == null
    }

    def "include deleted failed service instances in total nr of provision requests per plan"(){
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def deletedFailedServiceInstance = new ServiceInstance()
        def lastOperation = new LastOperation()
        def plan = new Plan()

        and:
        deletedFailedServiceInstance.completed = false
        deletedFailedServiceInstance.deleted = true
        plan.name = "plan"
        deletedFailedServiceInstance.plan = plan
        serviceInstanceList.add(deletedFailedServiceInstance)

        when:
        serviceInstanceRepository.findAll() >> serviceInstanceList
        lastOperationRepository.findByGuid(deletedFailedServiceInstance.guid) >> lastOperation

        then:
        provisionRequestsMetricsService.retrieveTotalMetricsPerPlan()

        expect:
        provisionRequestsMetricsService.totalPerPlan.size() == 1
        provisionRequestsMetricsService.totalSuccessPerPlan.size() == 0
        provisionRequestsMetricsService.totalFailurePerPlan.size() == 1

        and:
        provisionRequestsMetricsService.totalPerPlan.get(plan.name) == 1
        provisionRequestsMetricsService.totalSuccessPerPlan.get(plan.name) == null
        provisionRequestsMetricsService.totalFailurePerPlan.get(plan.name) == 1
    }

    def "retrieve total number of provision requests that failed per plan"(){
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def failedServiceInstance = new ServiceInstance()
        def lastOperation = new LastOperation()
        def plan = new Plan()

        and:
        failedServiceInstance.completed = false
        plan.name = "plan"
        failedServiceInstance.plan = plan
        serviceInstanceList.add(failedServiceInstance)

        when:
        serviceInstanceRepository.findAll() >> serviceInstanceList
        lastOperationRepository.findByGuid(failedServiceInstance.guid) >> lastOperation

        then:
        provisionRequestsMetricsService.retrieveTotalMetricsPerPlan()

        expect:
        provisionRequestsMetricsService.totalPerPlan.size() == 1
        provisionRequestsMetricsService.totalSuccessPerPlan.size() == 0
        provisionRequestsMetricsService.totalFailurePerPlan.size() == 1

        and:
        provisionRequestsMetricsService.totalPerPlan.get(plan.name) == 1
        provisionRequestsMetricsService.totalSuccessPerPlan.get(plan.name) == null
        provisionRequestsMetricsService.totalFailurePerPlan.get(plan.name) == 1
    }
}
