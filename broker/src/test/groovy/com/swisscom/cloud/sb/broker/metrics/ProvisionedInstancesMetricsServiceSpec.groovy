package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import spock.lang.Specification

class ProvisionedInstancesMetricsServiceSpec extends Specification {
    private ServiceInstanceRepository serviceInstanceRepository
    private LastOperationRepository lastOperationRepository
    private ProvisionedInstancesMetricsService provisioningMetricsService

    def setup() {
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        lastOperationRepository = Mock(LastOperationRepository)
        provisioningMetricsService = new ProvisionedInstancesMetricsService(serviceInstanceRepository, lastOperationRepository)
    }

    def "retrieve total nr of provisioned instances"() {
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
        provisioningMetricsService.retrieveTotalMetrics()

        expect:
        provisioningMetricsService.total == 1
        provisioningMetricsService.totalSuccess == 1
        provisioningMetricsService.totalFailure == 0
    }

    def "exclude deleted service instances from total nr of provisioned instances"() {
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
        provisioningMetricsService.retrieveTotalMetrics()

        expect:
        provisioningMetricsService.total == 0
        provisioningMetricsService.totalSuccess == 0
        provisioningMetricsService.totalFailure == 0

    }

    def "retrieve total number of provisioned instances that failed being provisioned"() {
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
        provisioningMetricsService.retrieveTotalMetrics()

        expect:
        provisioningMetricsService.total == 1
        provisioningMetricsService.totalSuccess == 0
        provisioningMetricsService.totalFailure == 1
    }

    def "retrieve total number of provisioned instances per service"() {
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
        provisioningMetricsService.retrieveTotalMetricsPerService()

        expect:
        provisioningMetricsService.totalPerService.size() == 1
        provisioningMetricsService.totalSuccessPerService.size() == 1
        provisioningMetricsService.totalFailurePerService.size() == 0

        and:
        provisioningMetricsService.totalPerService.get(cfService.name) == 1
        provisioningMetricsService.totalSuccessPerService.get(cfService.name) == 1
        provisioningMetricsService.totalFailurePerService.get(cfService.name) == null
    }

    def "exclude deleted service instances from total nr of provisioned instances per service"(){
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
        provisioningMetricsService.retrieveTotalMetricsPerService()

        expect:
        provisioningMetricsService.totalPerService.size() == 0
        provisioningMetricsService.totalSuccessPerService.size() == 0
        provisioningMetricsService.totalFailurePerService.size() == 0

        and:
        provisioningMetricsService.totalPerService.get(cfService.name) == null
        provisioningMetricsService.totalSuccessPerService.get(cfService.name) == null
        provisioningMetricsService.totalFailurePerService.get(cfService.name) == null
    }

    def "retrieve total number of provisioned instances that failed being provisioned per service"(){
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
        provisioningMetricsService.retrieveTotalMetricsPerService()

        expect:
        provisioningMetricsService.totalPerService.size() == 1
        provisioningMetricsService.totalSuccessPerService.size() == 0
        provisioningMetricsService.totalFailurePerService.size() == 1

        and:
        provisioningMetricsService.totalPerService.get(cfService.name) == 1
        provisioningMetricsService.totalSuccessPerService.get(cfService.name) == null
        provisioningMetricsService.totalFailurePerService.get(cfService.name) == 1
    }

    def "retrieve total number of provisioned instances per plan"() {
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
        provisioningMetricsService.retrieveTotalMetricsPerPlan()

        expect:
        provisioningMetricsService.totalPerPlan.size() == 1
        provisioningMetricsService.totalSuccessPerPlan.size() == 1
        provisioningMetricsService.totalFailurePerPlan.size() == 0

        and:
        provisioningMetricsService.totalPerPlan.get(plan.name) == 1
        provisioningMetricsService.totalSuccessPerPlan.get(plan.name) == 1
        provisioningMetricsService.totalFailurePerPlan.get(plan.name) == null
    }

    def "exclude deleted service instances from total nr of provisioned instances per plan"(){
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
        provisioningMetricsService.retrieveTotalMetricsPerPlan()

        expect:
        provisioningMetricsService.totalPerPlan.size() == 0
        provisioningMetricsService.totalSuccessPerPlan.size() == 0
        provisioningMetricsService.totalFailurePerPlan.size() == 0

        and:
        provisioningMetricsService.totalPerPlan.get(plan.name) == null
        provisioningMetricsService.totalSuccessPerPlan.get(plan.name) == null
        provisioningMetricsService.totalFailurePerPlan.get(plan.name) == null
    }

    def "retrieve total number of provisioned instances that failed being provisioned per plan"(){
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
        provisioningMetricsService.retrieveTotalMetricsPerPlan()

        expect:
        provisioningMetricsService.totalPerPlan.size() == 1
        provisioningMetricsService.totalSuccessPerPlan.size() == 0
        provisioningMetricsService.totalFailurePerPlan.size() == 1

        and:
        provisioningMetricsService.totalPerPlan.get(plan.name) == 1
        provisioningMetricsService.totalSuccessPerPlan.get(plan.name) == null
        provisioningMetricsService.totalFailurePerPlan.get(plan.name) == 1
    }
}
