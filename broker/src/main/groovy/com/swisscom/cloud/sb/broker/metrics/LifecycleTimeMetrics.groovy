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
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import io.micrometer.influx.InfluxConfig
import io.micrometer.influx.InfluxMeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.stereotype.Service

import java.time.Duration

@Service
@CompileStatic
class LifecycleTimeMetrics extends ServiceBrokerMetrics {

    private final String LIFECYCLE_TIME = "lifecycleTime"

    private HashMap<String, Long> totalLifecycleTimePerService
    private HashMap<String, Long> totalNrOfDeleteInstancesPerService

    @Autowired
    LifecycleTimeMetrics(ServiceInstanceRepository serviceInstanceRepository, LastOperationRepository lastOperationRepository, MeterRegistry meterRegistry) {
        super(serviceInstanceRepository, lastOperationRepository)
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
        return calculateLifecycleTimePerService(serviceInstanceList)
    }

    void addMetricsToMeterRegistry(MeterRegistry meterRegistry) {
        def lifecycleTimePerService = prepareMetricsForMetericsCollection()
        lifecycleTimePerService.each { entry ->
            meterRegistry.gauge(LIFECYCLE_TIME + "." + SERVICE + "." + TOTAL + "." + entry.getKey() + 2, Tags.empty(), entry.getValue())
        }
    }

    @Override
    Collection<Metric<?>> metrics() {
        List<Metric<?>> metrics = new ArrayList<>()

        def lifecycleTimePerService = prepareMetricsForMetericsCollection()
        metrics = addCountersFromHashMapToMetrics(lifecycleTimePerService, lifecycleTimePerService, metrics, LIFECYCLE_TIME, SERVICE, TOTAL)
        return metrics
    }

    @Override
    boolean considerServiceInstance(ServiceInstance serviceInstance) {
        return false
    }

    @Override
    String tag() {
        return LifecycleTimeMetrics.class.getSimpleName()
    }
}
