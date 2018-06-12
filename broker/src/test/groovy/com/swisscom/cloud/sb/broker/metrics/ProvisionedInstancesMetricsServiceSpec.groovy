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
        lastOperationRepository.findByGuid(serviceInstance.guid) >> lastOperation

        then:
        def totalMetrics = provisioningMetricsService.retrieveTotalMetrics(serviceInstanceList)

        expect:
        totalMetrics.total == 1
        totalMetrics.totalSuccess == 1
        totalMetrics.totalFailures == 0
    }

    def "exclude deleted service instances from total nr of provisioned instances"() {
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def deletedServiceInstance = new ServiceInstance()

        and:
        deletedServiceInstance.completed = true
        deletedServiceInstance.deleted = true

        when:
        serviceInstanceList.add(deletedServiceInstance)

        then:
        def totalMetrics = provisioningMetricsService.retrieveTotalMetrics(serviceInstanceList)

        expect:
        totalMetrics.total == 0
        totalMetrics.totalSuccess == 0
        totalMetrics.totalFailures == 0

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
        lastOperationRepository.findByGuid(failedServiceInstance.guid) >> lastOperation

        then:
        def totalMetrics = provisioningMetricsService.retrieveTotalMetrics(serviceInstanceList)

        expect:
        totalMetrics.total == 1
        totalMetrics.totalSuccess == 0
        totalMetrics.totalFailures == 1
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

        when:
        serviceInstanceList.add(serviceInstance)

        then:
        def totalMetricsPerService = provisioningMetricsService.retrieveTotalMetricsPerService(serviceInstanceList)

        expect:
        totalMetricsPerService.total.size() == 1
        totalMetricsPerService.totalSuccess.size() == 1
        totalMetricsPerService.totalFailures.size() == 0

        and:
        totalMetricsPerService.total.get(cfService.name) == 1
        totalMetricsPerService.totalSuccess.get(cfService.name) == 1
        totalMetricsPerService.totalFailures.get(cfService.name) == null
    }

    def "exclude deleted service instances from total nr of provisioned instances per service"() {
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

        when:
        serviceInstanceList.add(deletedServiceInstance)

        then:
        def totalMetricsPerService = provisioningMetricsService.retrieveTotalMetricsPerService(serviceInstanceList)

        expect:
        totalMetricsPerService.total.size() == 0
        totalMetricsPerService.totalSuccess.size() == 0
        totalMetricsPerService.totalFailures.size() == 0

        and:
        totalMetricsPerService.total.get(cfService.name) == null
        totalMetricsPerService.totalSuccess.get(cfService.name) == null
        totalMetricsPerService.totalFailures.get(cfService.name) == null
    }

    def "retrieve total number of provisioned instances that failed being provisioned per service"() {
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
        lastOperationRepository.findByGuid(failedServiceInstance.guid) >> lastOperation

        then:
        def totalMetricsPerService = provisioningMetricsService.retrieveTotalMetricsPerService(serviceInstanceList)

        expect:
        totalMetricsPerService.total.size() == 1
        totalMetricsPerService.totalSuccess.size() == 0
        totalMetricsPerService.totalFailures.size() == 1

        and:
        totalMetricsPerService.total.get(cfService.name) == 1
        totalMetricsPerService.totalSuccess.get(cfService.name) == null
        totalMetricsPerService.totalFailures.get(cfService.name) == 1
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

        when:
        serviceInstanceList.add(serviceInstance)

        then:
        def totalMetricsPerPlan = provisioningMetricsService.retrieveTotalMetricsPerPlan(serviceInstanceList)

        expect:
        totalMetricsPerPlan.total.size() == 1
        totalMetricsPerPlan.totalSuccess.size() == 1
        totalMetricsPerPlan.totalFailures.size() == 0

        and:
        totalMetricsPerPlan.total.get(plan.name) == 1
        totalMetricsPerPlan.totalSuccess.get(plan.name) == 1
        totalMetricsPerPlan.totalFailures.get(plan.name) == null
    }

    def "exclude deleted service instances from total nr of provisioned instances per plan"() {
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def deletedServiceInstance = new ServiceInstance()
        def plan = new Plan()

        and:
        deletedServiceInstance.completed = true
        deletedServiceInstance.deleted = true
        plan.name = "plan"
        deletedServiceInstance.plan = plan

        when:
        serviceInstanceList.add(deletedServiceInstance)

        then:
        def totalMetricsPerPlan = provisioningMetricsService.retrieveTotalMetricsPerPlan(serviceInstanceList)

        expect:
        totalMetricsPerPlan.total.size() == 0
        totalMetricsPerPlan.totalSuccess.size() == 0
        totalMetricsPerPlan.totalFailures.size() == 0

        and:
        totalMetricsPerPlan.total.get(plan.name) == null
        totalMetricsPerPlan.totalSuccess.get(plan.name) == null
        totalMetricsPerPlan.totalFailures.get(plan.name) == null
    }

    def "retrieve total number of provisioned instances that failed being provisioned per plan"() {
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
        lastOperationRepository.findByGuid(failedServiceInstance.guid) >> lastOperation

        then:
        def totalMetricsPerPlan = provisioningMetricsService.retrieveTotalMetricsPerPlan(serviceInstanceList)

        expect:
        totalMetricsPerPlan.total.size() == 1
        totalMetricsPerPlan.totalSuccess.size() == 0
        totalMetricsPerPlan.totalFailures.size() == 1

        and:
        totalMetricsPerPlan.total.get(plan.name) == 1
        totalMetricsPerPlan.totalSuccess.get(plan.name) == null
        totalMetricsPerPlan.totalFailures.get(plan.name) == 1
    }
}
