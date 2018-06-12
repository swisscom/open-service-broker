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
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.endpoint.PublicMetrics
import org.springframework.boot.actuate.metrics.Metric

import java.util.function.ToDoubleFunction

@CompileStatic
@Slf4j
abstract class ServiceBrokerMetrics implements PublicMetrics {

    protected final String SUCCESS = "success"
    protected final String FAIL = "fail"
    protected final String TOTAL = "total"
    protected final String SERVICE = "service"
    protected final String PLAN = "plan"
    protected final String RATIO = "ratio"

    protected ServiceInstanceRepository serviceInstanceRepository
    protected CFServiceRepository cfServiceRepository
    protected LastOperationRepository lastOperationRepository

    @Autowired
    ServiceBrokerMetrics(ServiceInstanceRepository serviceInstanceRepository, CFServiceRepository cfServiceRepository, LastOperationRepository lastOperationRepository) {
        this.serviceInstanceRepository = serviceInstanceRepository
        this.cfServiceRepository = cfServiceRepository
        this.lastOperationRepository = lastOperationRepository
    }

    //abstract void addMetricsToMeterRegistry(MeterRegistry meterRegistry, List<?> list)

    abstract Collection<Metric<?>> metrics()

    protected MetricsResult retrieveTotalMetrics(List<ServiceInstance> serviceInstanceList) {
        def totalCounter = 0
        def successCounter = 0
        def failCounter = 0

        serviceInstanceList.each { serviceInstance ->
            if (considerServiceInstance(serviceInstance)) {
                totalCounter++
                if (serviceInstance.completed) {
                    successCounter++
                } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                    // only failed provisions are counted, the ones in progress are ignored
                    failCounter++
                }
            }
        }

        log.info("${tag()} total: ${totalCounter}")
        log.info("${tag()} total success: ${successCounter}")
        log.info("${tag()} total failure: ${failCounter}")

        return new MetricsResult(total: totalCounter, totalSuccess: successCounter, totalFailures: failCounter)
    }

    HashMap<String, Long> harmonizeServicesHashMapsWithServicesInRepository(HashMap<String, Long> hashMap, CFServiceRepository cfServiceRepository) {
        cfServiceRepository.findAll().each { cfService ->
            if (hashMap.get(cfService.name) == null) {
                hashMap.put(cfService.name, 0L)
            }
        }
        return hashMap
    }

    MetricsResultMap retrieveTotalMetricsPerService(List<ServiceInstance> serviceInstanceList) {
        HashMap<String, Long> totalHm = new HashMap<>()
        HashMap<String, Long> successHm = new HashMap<>()
        HashMap<String, Long> failHm = new HashMap<>()

        serviceInstanceList.each { serviceInstance ->
            def serviceName = getServiceName(serviceInstance)
            if (considerServiceInstance(serviceInstance)) {
                totalHm = addOrUpdateEntryOnHashMap(totalHm, serviceName)
                if (serviceInstance.completed) {
                    successHm = addOrUpdateEntryOnHashMap(successHm, serviceName)
                } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                    // only failed provisions are counted, the ones in progress are ignored
                    failHm = addOrUpdateEntryOnHashMap(failHm, serviceName)
                }
            }
        }

        log.info("${tag()} total service: ${totalHm}")
        log.info("${tag()} total success service: ${successHm}")
        log.info("${tag()} total failure service: ${failHm}")

        return new MetricsResultMap(total: totalHm, totalSuccess: successHm, totalFailures: failHm)
    }

    MetricsResultMap retrieveTotalMetricsPerPlan(List<ServiceInstance> serviceInstanceList) {
        HashMap<String, Long> totalHm = new HashMap<>()
        HashMap<String, Long> successHm = new HashMap<>()
        HashMap<String, Long> failHm = new HashMap<>()

        serviceInstanceList.each { serviceInstance ->
            def planName = serviceInstance.plan.name
            if (considerServiceInstance(serviceInstance)) {
                totalHm = addOrUpdateEntryOnHashMap(totalHm, planName)
                if (serviceInstance.completed) {
                    successHm = addOrUpdateEntryOnHashMap(successHm, planName)
                } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                    // only failed provisions are counted, the ones in progress are ignored
                    failHm = addOrUpdateEntryOnHashMap(failHm, planName)
                }
            }
        }

        log.info("${tag()} total: ${totalHm}")
        log.info("${tag()} total success plan: ${successHm}")
        log.info("${tag()} total failure plan: ${failHm}")

        return new MetricsResultMap(total: totalHm, totalSuccess: successHm, totalFailures: failHm)
    }

    abstract boolean considerServiceInstance(ServiceInstance serviceInstance)

    abstract String tag()

    HashMap<String, Long> addOrUpdateEntryOnHashMap(HashMap<String, Long> hm, String key) {
        if (hm.get(key) == null) {
            hm.put(key, 1L)
        } else {
            def currentCounter = hm.get(key)
            def newCounter = currentCounter + 1
            hm.put(key, newCounter)
        }
        return hm
    }

    String getServiceName(ServiceInstance serviceInstance) {
        def cfService = serviceInstance.plan.service
        def cfServiceName = "someService"
        if (cfService) {
            cfServiceName = cfService.name
        }
        return cfServiceName
    }

    /*
    * The reason for checking for IN_PROGRESS rather than for FAILED is that when a service instance has been
    * provisioned unsuccessfully and is then deprovisioned, the operation in the lastOperationRepository is changed
    * from PROVISION to DEPROVISION and if the deprovisioning is successful, the status is set to SUCCESS, therefore
    * failed provisionings cannot be detected once they have been followed by deprovisioning. By checking for IN_PROGRESS
    * the case that a service instance is still being successfully provisioned is excluded from failures
    */

    boolean checkIfNotCompletedProvisionIsInProgress(String serviceInstanceGuid) {
        def lastOperation = lastOperationRepository.findByGuid(serviceInstanceGuid)
        return lastOperation.status == LastOperation.Status.IN_PROGRESS
    }

    double calculateRatio(long total, long amount) {
        if (total * amount != 0) {
            return 100 / total * amount
        }
        return 0
    }

    void addMetricsGauge(MeterRegistry meterRegistry, String name, Closure<Double> function) {
        ToDoubleFunction<ServiceBrokerMetrics> getBindingCountFunction = new ToDoubleFunction<ServiceBrokerMetrics>() {
            @Override
            double applyAsDouble(ServiceBrokerMetrics serviceBrokerMetrics) {
                function()
            }
        }
        meterRegistry.gauge(name, Tags.empty(), this, getBindingCountFunction)
    }

    void addMetricsToMeterRegistry(MeterRegistry meterRegistry, ServiceInstanceRepository serviceInstanceRepository, String kind) {
        addMetricsGauge(meterRegistry, "${kind}.${TOTAL}.${TOTAL}", {
            retrieveTotalMetrics(serviceInstanceRepository.findAll()).total.toDouble()
        })
        addMetricsGauge(meterRegistry, "${kind}.${TOTAL}.${SUCCESS}", {
            retrieveTotalMetrics(serviceInstanceRepository.findAll()).totalSuccess.toDouble()
        })
        addMetricsGauge(meterRegistry, "${kind}.${TOTAL}.${FAIL}", {
            retrieveTotalMetrics(serviceInstanceRepository.findAll()).totalFailures.toDouble()
        })
        addMetricsGauge(meterRegistry, "${kind}.${SUCCESS}.${RATIO}", {
            calculateRatio(retrieveTotalMetrics(serviceInstanceRepository.findAll()).total, retrieveTotalMetrics(serviceInstanceRepository.findAll()).totalSuccess).toDouble()
        })
        addMetricsGauge(meterRegistry, "${kind}.${FAIL}.${RATIO}", {
            calculateRatio(retrieveTotalMetrics(serviceInstanceRepository.findAll()).total, retrieveTotalMetrics(serviceInstanceRepository.findAll()).totalFailures).toDouble()
        })

        def totalMetricsPerService = retrieveTotalMetricsPerService(serviceInstanceRepository.findAll())
        totalMetricsPerService.total.each { entry ->
            addMetricsGauge(meterRegistry, "${kind}.${SERVICE}.${TOTAL}.${entry.getKey()}", {
                retrieveTotalMetricsPerService(serviceInstanceRepository.findAll()).total.get(entry.getKey()).toDouble()
            })
        }

        totalMetricsPerService.totalSuccess.each { entry ->
            addMetricsGauge(meterRegistry, "${kind}.${SERVICE}.${SUCCESS}.${entry.getKey()}", {
                retrieveTotalMetricsPerService(serviceInstanceRepository.findAll()).totalSuccess.get(entry.getKey()).toDouble()
            })
        }

        totalMetricsPerService.totalFailures.each { entry ->
            addMetricsGauge(meterRegistry, "${kind}.${SERVICE}.${FAIL}.${entry.getKey()}", {
                retrieveTotalMetricsPerService(serviceInstanceRepository.findAll()).totalFailures.get(entry.getKey()).toDouble()
            })
        }

        def totalMetricsPerPlan = retrieveTotalMetricsPerPlan(serviceInstanceRepository.findAll())
        totalMetricsPerPlan.total.each { entry ->
            addMetricsGauge(meterRegistry, "${kind}.${PLAN}.${TOTAL}.${entry.getKey()}", {
                retrieveTotalMetricsPerPlan(serviceInstanceRepository.findAll()).total.get(entry.getKey()).toDouble()
            })
        }

        totalMetricsPerPlan.totalSuccess.each { entry ->
            addMetricsGauge(meterRegistry, "${kind}.${PLAN}.${SUCCESS}.${entry.getKey()}", {
                retrieveTotalMetricsPerPlan(serviceInstanceRepository.findAll()).totalSuccess.get(entry.getKey()).toDouble()
            })
        }

        totalMetricsPerPlan.totalFailures.each { entry ->
            addMetricsGauge(meterRegistry, "${kind}.${PLAN}.${FAIL}.${entry.getKey()}", {
                retrieveTotalMetricsPerPlan(serviceInstanceRepository.findAll()).totalFailures.get(entry.getKey()).toDouble()
            })
        }
    }

    List<Metric<?>> addCountersFromHashMapToMetrics(HashMap<String, Long> totalhm, HashMap<String, Long> hm, List<Metric<?>> metrics, String kind, String level, String qualifier) {
        for (Map.Entry<String, Long> entry : hm.entrySet()) {
            metrics.add(new Metric<Long>("${kind}.${level}.${qualifier}.${entry.getKey()}", entry.getValue()))
            if (totalhm != hm) {
                def total = totalhm.get(entry.getKey())
                metrics.add(new Metric<Double>("${kind}.${level}.${entry.getKey()}.${qualifier}.${RATIO}", calculateRatio(total, entry.getValue())))
            }
        }
        return metrics
    }
}