/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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
        lastOperationRepository.findByGuid(serviceInstance.guid) >> lastOperation

        then:
        def totalMetrics = provisionRequestsMetricsService.retrieveTotalMetrics(serviceInstanceList)

        expect:
        totalMetrics.total == 1
        totalMetrics.totalSuccess == 1
        totalMetrics.totalFailures == 0
    }

    def "include deleted successfully provisioned service instances in total nr of provision requests"() {
        setup:
        def serviceInstanceList = new ArrayList<ServiceInstance>()
        def deletedServiceInstance = new ServiceInstance()

        and:
        deletedServiceInstance.completed = true
        deletedServiceInstance.deleted = true

        when:
        serviceInstanceList.add(deletedServiceInstance)

        then:
        def totalMetrics = provisionRequestsMetricsService.retrieveTotalMetrics(serviceInstanceList)

        expect:
        totalMetrics.total == 1
        totalMetrics.totalSuccess == 1
        totalMetrics.totalFailures == 0

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
        lastOperationRepository.findByGuid(deletedFailedServiceInstance.guid) >> lastOperation

        then:
        def totalMetrics = provisionRequestsMetricsService.retrieveTotalMetrics(serviceInstanceList)

        expect:
        totalMetrics.total == 1
        totalMetrics.totalSuccess == 0
        totalMetrics.totalFailures == 1

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
        lastOperationRepository.findByGuid(failedServiceInstance.guid) >> lastOperation

        then:
        def totalMetrics = provisionRequestsMetricsService.retrieveTotalMetrics(serviceInstanceList)

        expect:
        totalMetrics.total == 1
        totalMetrics.totalSuccess == 0
        totalMetrics.totalFailures == 1
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

        when:
        serviceInstanceList.add(serviceInstance)

        then:
        def totalMetricsPerService = provisionRequestsMetricsService.retrieveTotalMetricsPerService(serviceInstanceList)

        expect:
        totalMetricsPerService.total.size() == 1
        totalMetricsPerService.totalSuccess.size() == 1
        totalMetricsPerService.totalFailures.size() == 0

        and:
        totalMetricsPerService.total.get(cfService.name) == 1
        totalMetricsPerService.totalSuccess.get(cfService.name) == 1
        totalMetricsPerService.totalFailures.get(cfService.name) == null
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

        when:
        serviceInstanceList.add(deletedServiceInstance)

        then:
        def totalMetricsPerService = provisionRequestsMetricsService.retrieveTotalMetricsPerService(serviceInstanceList)

        expect:
        totalMetricsPerService.total.size() == 1
        totalMetricsPerService.totalSuccess.size() == 1
        totalMetricsPerService.totalFailures.size() == 0

        and:
        totalMetricsPerService.total.get(cfService.name) == 1
        totalMetricsPerService.totalSuccess.get(cfService.name) == 1
        totalMetricsPerService.totalFailures.get(cfService.name) == null
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
        lastOperationRepository.findByGuid(deletedFailedServiceInstance.guid) >> lastOperation


        then:
        def totalMetricsPerService = provisionRequestsMetricsService.retrieveTotalMetricsPerService(serviceInstanceList)

        expect:
        totalMetricsPerService.total.size() == 1
        totalMetricsPerService.totalSuccess.size() == 0
        totalMetricsPerService.totalFailures.size() == 1

        and:
        totalMetricsPerService.total.get(cfService.name) == 1
        totalMetricsPerService.totalSuccess.get(cfService.name) == null
        totalMetricsPerService.totalFailures.get(cfService.name) == 1
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
        lastOperationRepository.findByGuid(failedServiceInstance.guid) >> lastOperation

        then:
        def totalMetricsPerService = provisionRequestsMetricsService.retrieveTotalMetricsPerService(serviceInstanceList)

        expect:
        totalMetricsPerService.total.size() == 1
        totalMetricsPerService.totalSuccess.size() == 0
        totalMetricsPerService.totalFailures.size() == 1

        and:
        totalMetricsPerService.total.get(cfService.name) == 1
        totalMetricsPerService.totalSuccess.get(cfService.name) == null
        totalMetricsPerService.totalFailures.get(cfService.name) == 1
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

        when:
        serviceInstanceList.add(serviceInstance)

        then:
        def totalMetricsPerPlan = provisionRequestsMetricsService.retrieveTotalMetricsPerPlan(serviceInstanceList)

        expect:
        totalMetricsPerPlan.total.size() == 1
        totalMetricsPerPlan.totalSuccess.size() == 1
        totalMetricsPerPlan.totalFailures.size() == 0

        and:
        totalMetricsPerPlan.total.get(plan.name) == 1
        totalMetricsPerPlan.totalSuccess.get(plan.name) == 1
        totalMetricsPerPlan.totalFailures.get(plan.name) == null
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

        when:
        serviceInstanceList.add(deletedServiceInstance)

        then:
        def totalMetricsPerPlan = provisionRequestsMetricsService.retrieveTotalMetricsPerPlan(serviceInstanceList)

        expect:
        totalMetricsPerPlan.total.size() == 1
        totalMetricsPerPlan.totalSuccess.size() == 1
        totalMetricsPerPlan.totalFailures.size() == 0

        and:
        totalMetricsPerPlan.total.get(plan.name) == 1
        totalMetricsPerPlan.totalSuccess.get(plan.name) == 1
        totalMetricsPerPlan.totalFailures.get(plan.name) == null
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
        lastOperationRepository.findByGuid(deletedFailedServiceInstance.guid) >> lastOperation

        then:
        def totalMetricsPerPlan = provisionRequestsMetricsService.retrieveTotalMetricsPerPlan(serviceInstanceList)

        expect:
        totalMetricsPerPlan.total.size() == 1
        totalMetricsPerPlan.totalSuccess.size() == 0
        totalMetricsPerPlan.totalFailures.size() == 1

        and:
        totalMetricsPerPlan.total.get(plan.name) == 1
        totalMetricsPerPlan.totalSuccess.get(plan.name) == null
        totalMetricsPerPlan.totalFailures.get(plan.name) == 1
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
        lastOperationRepository.findByGuid(failedServiceInstance.guid) >> lastOperation

        then:
        def totalMetricsPerPlan = provisionRequestsMetricsService.retrieveTotalMetricsPerPlan(serviceInstanceList)

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
