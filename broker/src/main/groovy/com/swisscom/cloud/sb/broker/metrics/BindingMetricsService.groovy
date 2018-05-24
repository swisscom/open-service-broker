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
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.stereotype.Service

import java.util.Map.Entry

import java.util.function.ToDoubleFunction

@Service
@CompileStatic
@Slf4j
class BindingMetricsServiceService extends ServiceBrokerMetricsService {

    final String BINDING = "binding"
    final String BINDING_REQUEST = "bindingRequest"

    ServiceBindingRepository serviceBindingRepository
    MeterRegistry meterRegistry

    HashMap<String, Long> totalBindingRequestsPerService = new HashMap<>()
    HashMap<String, Long> totalSuccessfulBindingRequestsPerService = new HashMap<>()
    HashMap<String, Long> totalFailedBindingRequestsPerService = new HashMap<>()

    @Autowired
    BindingMetricsServiceService(ServiceInstanceRepository serviceInstanceRepository, CFServiceRepository cfServiceRepository, LastOperationRepository lastOperationRepository, ServiceBindingRepository serviceBindingRepository, PlanRepository planRepository, MeterRegistry meterRegistry) {
        super(serviceInstanceRepository, cfServiceRepository, lastOperationRepository, planRepository)
        this.serviceBindingRepository = serviceBindingRepository
        addMetricsToMeterRegistry(meterRegistry, serviceBindingRepository)
    }

    long retrieveMetricsForTotalNrOfSuccessfulBindings(List<ServiceBinding> serviceBindingList) {
        def totalNrOfSuccessfulBindings = serviceBindingList.size()
        log.info("Total nr of provision requests: ${totalNrOfSuccessfulBindings}")
        return totalNrOfSuccessfulBindings
    }

    HashMap<String, Long> retrieveTotalNrOfSuccessfulBindingsPerService(List<ServiceBinding> serviceBindingList) {
        HashMap<String, Long> totalHm = new HashMap<>()
        totalHm = harmonizeServicesHashMapsWithServicesInRepository(totalHm, cfServiceRepository)

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
        if (totalFailedBindingRequestsPerService.size() < cfServiceRepository.findAll().size()) {
            totalFailedBindingRequestsPerService = harmonizeServicesHashMapsWithServicesInRepository(totalFailedBindingRequestsPerService, cfServiceRepository)
        }
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
        retrieveMetricsForTotalNrOfSuccessfulBindings(serviceBindingRepository.findAll()).toDouble()
    }

    void addMetricsGauge(MeterRegistry meterRegistry, String name, Closure<Double> function) {
        ToDoubleFunction<BindingMetricsService> getBindingCountFunction  = new ToDoubleFunction<BindingMetricsService>() {
            @Override
            double applyAsDouble(BindingMetricsService bindingMetricsService) {
                function()
            }
        }
        meterRegistry.gauge(name, Tags.empty(), this, getBindingCountFunction)
    }

    void addMetricsToMeterRegistry(MeterRegistry meterRegistry, ServiceBindingRepository serviceBindingRepository) {
        List<ServiceBinding> serviceBindingList = serviceBindingRepository.findAll()
        //ServiceBindingRepositoryHelper serviceBindingRepositoryHelper = new ServiceBindingRepositoryHelper()
        //List<ServiceBinding> serviceBindingList = serviceBindingRepositoryHelper.serviceBindingRepository.findAll()



        addMetricsGauge(meterRegistry, "${BINDING}.${TOTAL}.${TOTAL}", { getBindingCount() })
        //def totalNrOfSuccessfulBindings = retrieveMetricsForTotalNrOfSuccessfulBindings(serviceBindingRepository.findAll())
        // meterRegistry.gauge("${BINDING}.${TOTAL}.${TOTAL}", Tags.empty(), totalNrOfSuccessfulBindings)




        //meterRegistry.gauge("${BINDING}.${TOTAL}.${TOTAL}", Tags.empty(), serviceBindingRepository.findAll(), {retrieveMetricsForTotalNrOfSuccessfulBindings(serviceBindingRepository.findAll())})
        def totalNrOfSuccessfulBindingsPerService = retrieveTotalNrOfSuccessfulBindingsPerService(serviceBindingList)
        totalNrOfSuccessfulBindingsPerService.each { entry ->
            meterRegistry.gauge("${BINDING}.${SERVICE}.${TOTAL}.${entry.getKey()}".toString(), Tags.empty(), entry.getValue())
        }

        totalBindingRequestsPerService.each { entry ->
            meterRegistry.gauge("${BINDING_REQUEST}.${SERVICE}.${TOTAL}".toString(), Tags.empty(), entry.getValue())
        }

        totalSuccessfulBindingRequestsPerService.each { entry ->
            meterRegistry.gauge("${BINDING_REQUEST}.${SERVICE}.${SUCCESS}".toString(), Tags.empty(), entry.getValue())
        }

        totalFailedBindingRequestsPerService.each { entry ->
            meterRegistry.gauge("${BINDING_REQUEST}.${SERVICE}.${FAIL}".toString(), Tags.empty(), entry.getValue())
        }
    }

    @Override
    Collection<Metric<?>> metrics() {
        List<Metric<?>> metrics = new ArrayList<>()
    double getBindingCount() {
        retrieveMetricsForTotalNrOfSuccessfulBindings(serviceBindingRepository.findAll()).toDouble()
    }

    double getSuccessfulBindingCount(Entry<String, Long> entry) {
        def serviceBindings = serviceBindingRepository.findAll()
        if (serviceBindings.size() > 0) {
            return retrieveTotalNrOfSuccessfulBindingsPerService(serviceBindings).get(entry.getKey()).toDouble()
        }
        0.0
    }

    void addMetricsToMeterRegistry(MeterRegistry meterRegistry, ServiceBindingRepository serviceBindingRepository) {
        List<ServiceBinding> serviceBindingList = serviceBindingRepository.findAll()
        addMetricsGauge(meterRegistry, "${BINDING}.${TOTAL}.${TOTAL}", { getBindingCount() })

        def totalNrOfSuccessfulBindingsPerService = retrieveTotalNrOfSuccessfulBindingsPerService(serviceBindingList)
        totalNrOfSuccessfulBindingsPerService.each { entry ->
            addMetricsGauge(meterRegistry, "${BINDING}.${SERVICE}.${TOTAL}.${entry.getKey()}", {
                getSuccessfulBindingCount(entry)
            })
        }

        if (totalBindingRequestsPerService.size() < cfServiceRepository.findAll().size()) {
            totalBindingRequestsPerService = harmonizeServicesHashMapsWithServicesInRepository(totalBindingRequestsPerService, cfServiceRepository)
        }
        totalBindingRequestsPerService.each { entry ->
            addMetricsGauge(meterRegistry, "${BINDING_REQUEST}.${SERVICE}.${TOTAL}.${entry.getKey()}", {
                getCountForEntryFromHashMap(totalBindingRequestsPerService, entry)
            })
        }

        if (totalSuccessfulBindingRequestsPerService.size() < cfServiceRepository.findAll().size()) {
            totalSuccessfulBindingRequestsPerService = harmonizeServicesHashMapsWithServicesInRepository(totalSuccessfulBindingRequestsPerService, cfServiceRepository)
        }
        totalSuccessfulBindingRequestsPerService.each { entry ->
            addMetricsGauge(meterRegistry, "${BINDING_REQUEST}.${SERVICE}.${SUCCESS}.${entry.getKey()}", {
                getCountForEntryFromHashMap(totalSuccessfulBindingRequestsPerService, entry)
            })
        }

        if (totalFailedBindingRequestsPerService.size() < cfServiceRepository.findAll().size()) {
            totalFailedBindingRequestsPerService = harmonizeServicesHashMapsWithServicesInRepository(totalFailedBindingRequestsPerService, cfServiceRepository)
        }
        totalFailedBindingRequestsPerService.each { entry ->
            addMetricsGauge(meterRegistry, "${BINDING_REQUEST}.${SERVICE}.${FAIL}.${entry.getKey()}", {
                getCountForEntryFromHashMap(totalFailedBindingRequestsPerService, entry)
            })
        }
    }

    @Override
    boolean considerServiceInstance(ServiceInstance serviceInstance) {
        return false
    }

    @Override
    String tag() {
        return BindingMetricsServiceService.class.getSimpleName()
    }
}
