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

import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.Map.Entry

@Service
@CompileStatic
@Slf4j
class BindingMetricsService extends ServiceBrokerMetricsService {

    final String BINDING = "binding"
    final String BINDING_REQUEST = "bindingRequest"

    ServiceBindingRepository serviceBindingRepository
    MeterRegistry meterRegistry

    HashMap<String, Long> totalBindingRequestsPerService = new HashMap<>()
    HashMap<String, Long> totalSuccessfulBindingRequestsPerService = new HashMap<>()
    HashMap<String, Long> totalFailedBindingRequestsPerService = new HashMap<>()

    @Autowired
    BindingMetricsService(LastOperationRepository lastOperationRepository, MetricsCache metricsCache, MeterRegistry meterRegistry) {
        super(lastOperationRepository, metricsCache)
        addMetricsToMeterRegistry(meterRegistry)
    }

    long retrieveMetricsForTotalNrOfSuccessfulBindings(List<ServiceBinding> serviceBindingList) {
        def totalNrOfSuccessfulBindings = serviceBindingList.size()
        log.info("Total nr of provision requests: ${totalNrOfSuccessfulBindings}")
        return totalNrOfSuccessfulBindings
    }

    HashMap<String, Long> retrieveTotalNrOfSuccessfulBindingsPerService(List<ServiceBinding> serviceBindingList) {
        HashMap<String, Long> totalHm = new HashMap<>()
        totalHm = harmonizeServicesHashMapsWithServicesInRepository(totalHm)

        serviceBindingList.each { serviceBinding ->
            def serviceInstance = serviceBinding.serviceInstance
            if (serviceInstance != null) {
                def service = serviceInstance.plan.service
                def serviceName = "someService"
                if (service) {
                    serviceName = service.name
                }
                totalHm = addOrUpdateEntryOnHashMap(totalHm, serviceName)
            }
        }
        log.info("${tag()} total bindings per service: ${totalHm}")
        return totalHm
    }

    void setTotalBindingRequestsPerService(ServiceInstance serviceInstance) {
        def cfServiceName = getServiceName(serviceInstance)
        totalBindingRequestsPerService = addOrUpdateEntryOnHashMap(totalBindingRequestsPerService, cfServiceName)
        calculateFailedBindingRequestsPerService()
    }

    void setSuccessfulBindingRequestsPerService(ServiceInstance serviceInstance) {
        def cfServiceName = getServiceName(serviceInstance)
        totalSuccessfulBindingRequestsPerService = addOrUpdateEntryOnHashMap(totalSuccessfulBindingRequestsPerService, cfServiceName)
        calculateFailedBindingRequestsPerService()
    }

    void calculateFailedBindingRequestsPerService() {
        totalFailedBindingRequestsPerService = harmonizeServicesHashMapsWithServicesInRepository(totalFailedBindingRequestsPerService)
        totalBindingRequestsPerService.each { service ->
            def key = service.getKey()
            def totalValue = service.getValue()
            if (totalValue == null) {
                totalValue = 0
            }
            def successValue = totalSuccessfulBindingRequestsPerService.get(key)
            if (totalValue == null) {
                totalValue = 0
            }
            if (successValue == null) {
                successValue = 0
            }
            def failureValue = totalValue - successValue
            totalFailedBindingRequestsPerService.put(key, failureValue)
        }
    }

    double getBindingCount() {
        retrieveMetricsForTotalNrOfSuccessfulBindings(metricsCache.serviceBindingList).toDouble()
    }

    double getSuccessfulBindingCount(Entry<String, Long> entry) {
        if (retrieveTotalNrOfSuccessfulBindingsPerService(metricsCache.serviceBindingList).containsKey(entry.getKey())) {
            return retrieveTotalNrOfSuccessfulBindingsPerService(metricsCache.serviceBindingList).get(entry.getKey()).toDouble()
        }
        0.0
    }

    void addMetricsToMeterRegistry(MeterRegistry meterRegistry) {
        addMetricsGauge(meterRegistry, "${BINDING}.${TOTAL}.${TOTAL}", { getBindingCount() }, TOTAL)

        def totalNrOfSuccessfulBindingsPerService = retrieveTotalNrOfSuccessfulBindingsPerService(metricsCache.serviceBindingList)
        totalNrOfSuccessfulBindingsPerService.each { entry ->
            addMetricsGauge(meterRegistry, "${BINDING}.${SERVICE}.${TOTAL}.${entry.getKey()}", {
                def result = getSuccessfulBindingCount(entry)
                log.info("${BINDING}.${SERVICE}.${TOTAL}.${entry.getKey()} : result${result}")

                return result
            }, SERVICE)
        }

        totalBindingRequestsPerService = harmonizeServicesHashMapsWithServicesInRepository(totalBindingRequestsPerService)
        totalBindingRequestsPerService.each { entry ->
            addMetricsGauge(meterRegistry, "${BINDING_REQUEST}.${SERVICE}.${TOTAL}.${entry.getKey()}", {
                getCountForEntryFromHashMap(totalBindingRequestsPerService, entry)
            }, SERVICE)
        }

        totalSuccessfulBindingRequestsPerService = harmonizeServicesHashMapsWithServicesInRepository(totalSuccessfulBindingRequestsPerService)
        totalSuccessfulBindingRequestsPerService.each { entry ->
            addMetricsGauge(meterRegistry, "${BINDING_REQUEST}.${SERVICE}.${SUCCESS}.${entry.getKey()}", {
                getCountForEntryFromHashMap(totalSuccessfulBindingRequestsPerService, entry)
            }, SERVICE)
        }

        totalFailedBindingRequestsPerService = harmonizeServicesHashMapsWithServicesInRepository(totalFailedBindingRequestsPerService)
        totalFailedBindingRequestsPerService.each { entry ->
            addMetricsGauge(meterRegistry, "${BINDING_REQUEST}.${SERVICE}.${FAIL}.${entry.getKey()}", {
                getCountForEntryFromHashMap(totalFailedBindingRequestsPerService, entry)
            }, SERVICE)
        }
    }

    @Override
    boolean considerServiceInstance(ServiceInstance serviceInstance) {
        return false
    }

    @Override
    String tag() {
        return BindingMetricsService.class.getSimpleName()
    }
}
