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

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@CompileStatic
class LifecycleTimeMetricsService extends ServiceBrokerMetricsService {

    private final String LIFECYCLE_TIME = "lifecycleTime"

    private HashMap<String, Long> totalLifecycleTimePerService = new HashMap<>()
    private HashMap<String, Long> totalNrOfDeleteInstancesPerService = new HashMap<>()

    @Autowired
    LifecycleTimeMetricsService(ServiceInstanceRepository serviceInstanceRepository, CFServiceRepository cfServiceRepository, LastOperationRepository lastOperationRepository, PlanRepository planRepository, MeterRegistry meterRegistry) {
        super(serviceInstanceRepository, cfServiceRepository, lastOperationRepository, planRepository)
        addMetricsToMeterRegistry(meterRegistry)
    }

    HashMap<String, Long> calculateLifecycleTimePerService(List<ServiceInstance> serviceInstanceList) {
        HashMap<String, Long> total = new HashMap<>()
        HashMap<String, Long> totalLifecycleTime = new HashMap<>()

        serviceInstanceList.findAll { instance -> instance.deleted }.each {
            serviceInstance ->
                def serviceName = getServiceName(serviceInstance)
                total = addOrUpdateEntryOnHashMap(total, serviceName)
                totalLifecycleTime = addUpLifecycleTime(totalLifecycleTime, serviceName, serviceInstance)
        }
        totalNrOfDeleteInstancesPerService = total
        totalLifecycleTimePerService = totalLifecycleTime
        return calculateMeanLifecycleTime(totalNrOfDeleteInstancesPerService)
    }

    HashMap<String, Long> addUpLifecycleTime(HashMap<String, Long> totalLifecycleTimePerServiceName, String serviceName, ServiceInstance serviceInstance) {
        def dateCreated = serviceInstance.dateCreated.getTime()
        def dateDeleted = serviceInstance.dateDeleted.getTime()
        def lifecycleTime = dateDeleted - dateCreated
        if (totalLifecycleTimePerServiceName.get(serviceName) == null) {
            totalLifecycleTimePerServiceName.put(serviceName, lifecycleTime)
        } else {
            def currentValue = totalLifecycleTimePerServiceName.get(serviceName)
            def newValue = currentValue + lifecycleTime
            totalLifecycleTimePerServiceName.put(serviceName, newValue)
        }
        return totalLifecycleTimePerServiceName
    }

    HashMap<String, Long> calculateMeanLifecycleTime(HashMap<String, Long> totalDeletedServiceInstanceMap) {
        HashMap<String, Long> meanLifecycleTimePerService = new HashMap<>()
        meanLifecycleTimePerService = harmonizeServicesHashMapsWithServicesInRepository(meanLifecycleTimePerService, cfServiceRepository)
        totalDeletedServiceInstanceMap.each { service ->
            def serviceName = service.getKey()
            def totalNrOfInstances = service.getValue()
            def totalLifecycleTime = totalLifecycleTimePerService.get(serviceName)
            def meanLifecycleTime = (totalLifecycleTime / totalNrOfInstances).toLong()
            meanLifecycleTimePerService.put(serviceName, meanLifecycleTime)
        }
        return meanLifecycleTimePerService
    }

    HashMap<String, Long> prepareMetricsForMetericsCollection() {
        List<ServiceInstance> serviceInstanceList = serviceInstanceRepository.findAll()
        def list = calculateLifecycleTimePerService(serviceInstanceList)
        totalLifecycleTimePerService = harmonizeServicesHashMapsWithServicesInRepository(list, cfServiceRepository)
        return totalLifecycleTimePerService
    }

    double getTotalLifecycleTime(Map.Entry<String, Long> entry) {
        def totalLifecycleTime = prepareMetricsForMetericsCollection()
        if (totalLifecycleTime.containsKey(entry.getKey())) {
            return totalLifecycleTime.get(entry.getKey()).toDouble()
        }
        0.0
    }

    void addMetricsToMeterRegistry(MeterRegistry meterRegistry) {
        totalLifecycleTimePerService = harmonizeServicesHashMapsWithServicesInRepository(totalLifecycleTimePerService, cfServiceRepository)
        totalLifecycleTimePerService.each { entry ->
            addMetricsGauge(meterRegistry, "${LIFECYCLE_TIME}.${SERVICE}.${TOTAL}.${entry.getKey()}", {
                getTotalLifecycleTime(entry)
            }, SERVICE)
        }
    }

    @Override
    boolean considerServiceInstance(ServiceInstance serviceInstance) {
        return false
    }

    @Override
    String tag() {
        return LifecycleTimeMetricsService.class.getSimpleName()
    }
}
