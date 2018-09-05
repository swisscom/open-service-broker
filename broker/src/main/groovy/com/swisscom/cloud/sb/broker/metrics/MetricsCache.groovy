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

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.client.model.LastOperationState
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDateTime

@Component
@Slf4j
class MetricsCache {
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    private ServiceBindingRepository serviceBindingRepository
    @Autowired
    private PlanRepository planRepository
    @Autowired
    private LastOperationRepository lastOperationRepository
    @Autowired
    private ServiceBrokerMetricsConfig serviceBrokerMetricsConfig

    private static ServiceInstanceList serviceInstanceList = new ServiceInstanceList()
    private static LocalDateTime serviceInstanceListLastModified = LocalDateTime.MIN

    private static Map<String, LastOperation> lastOperationMap = new HashMap<String, LastOperation>()
    private static LocalDateTime lastOperationMapLastModified = LocalDateTime.MIN

    private static List<Plan> planList = new ArrayList<>()
    private static LocalDateTime planListLastModified = LocalDateTime.MIN

    private static Map<String, Double> bindingCountByPlanGuid = new HashMap<String, Double>()
    private static LocalDateTime bindingCountByPlanGuidLastModified = LocalDateTime.MIN

    private static Map<String, Double> failedLastOperationCountByPlanGuid = new HashMap<String, Double>()
    private static LocalDateTime failedLastOperationCountByPlanGuidLastModified = LocalDateTime.MIN

    ServiceInstanceList getServiceInstanceList() {
        synchronized (serviceInstanceList) {
            if (hasExpired(serviceInstanceListLastModified)) {
                def allServiceInstances = serviceInstanceRepository.findAll();

                serviceInstanceList.refresh(getLastOperationMap(), allServiceInstances)
                serviceInstanceListLastModified = LocalDateTime.now()
            }

            return serviceInstanceList
        }
    }

    Map<String, LastOperation> getLastOperationMap() {
        synchronized (lastOperationMap) {
            if (hasExpired(lastOperationMapLastModified)) {
                lastOperationRepository.findAll().each { lO -> lastOperationMap.put(lO.guid, lO) }
                lastOperationMapLastModified = LocalDateTime.now()
            }

            return lastOperationMap
        }
    }

    Map<String, Double> getBindingCountByPlanGuid() {
        synchronized (bindingCountByPlanGuid) {
            if (hasExpired(bindingCountByPlanGuidLastModified))
                createBindingCountByPlanGuid().each { k, v -> bindingCountByPlanGuid.put(k, v) }
            return bindingCountByPlanGuid
        }
    }

    private Map<String, Double> createBindingCountByPlanGuid() {
        HashMap<String, Double> bindingCountByPlanGuid = new HashMap<>()

        Map<Integer, String> planIdToPlanGuid = new HashMap<>()
        planList.each { plan -> planIdToPlanGuid.put(plan.id, plan.guid) }

        Map<Integer, String> serviceInstanceIdToPlanGuid = new HashMap<>()
        serviceInstanceList.each { sI -> serviceInstanceIdToPlanGuid.put(sI.id, planIdToPlanGuid[sI.planId]) }

        def allBindings = serviceBindingRepository.findAll()
        allBindings.each { binding ->
            def planGuid = serviceInstanceIdToPlanGuid[binding.serviceInstanceId]
            bindingCountByPlanGuid.put(planGuid, bindingCountByPlanGuid.get(planGuid, 0.0D) + 1)
        }

        return bindingCountByPlanGuid
    }

    Double getFailedLastOperationCount(String planGuid) {
        synchronized (failedLastOperationCountByPlanGuid) {
            if (hasExpired(failedLastOperationCountByPlanGuidLastModified))
                createFailedLastOperationMap().each { k, v -> failedLastOperationCountByPlanGuid.put(k, v) }
            return failedLastOperationCountByPlanGuid.get(planGuid, 0.0D)
        }
    }

    private Map<String, Double> createFailedLastOperationMap() {
        Map<Integer, String> planIdToPlanGuid = new HashMap<>()
        planList.each { plan -> planIdToPlanGuid.put(plan.id, plan.guid) }

        Map<String, String> serviceInstanceGuidToPlanGuid = new HashMap<>()
        serviceInstanceList.each { sI -> serviceInstanceGuidToPlanGuid.put(sI.guid, planIdToPlanGuid[sI.planId]) }

        Map<String, Double> failedCountByPlanId = new HashMap<>()

        lastOperationMap.values().each { lastOperation ->
            if (lastOperation.status == LastOperationState.FAILED) {
                def planGuid = serviceInstanceGuidToPlanGuid.get(lastOperation.guid)
                failedCountByPlanId.put(planGuid, failedCountByPlanId.get(planGuid, 0.0D) + 1)
            }
        }

        failedCountByPlanId
    }

    private int getTimeoutInSeconds() {
        Integer.parseInt(serviceBrokerMetricsConfig.step.substring(0, serviceBrokerMetricsConfig.step.length()-1))
    }

    private boolean hasExpired(LocalDateTime dateOflastRefresh) {
        dateOflastRefresh.isBefore(LocalDateTime.now().minusSeconds(getTimeoutInSeconds()))
    }

    List<Plan> getListOfPlans() {
        synchronized (planList) {
            if (hasExpired(planListLastModified)) {
                planList.clear()
                planList.addAll(planRepository.findAll().findAll { p -> p.name && p.service != null && p.service.name })
                planListLastModified = LocalDateTime.now()
            }

            return planList
        }
    }
}
