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
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
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

import java.util.function.Function
import java.util.function.ToDoubleFunction

@Service
@CompileStatic
@Slf4j
class BindingMetricsService extends ServiceBrokerMetrics {

    private final String BINDING = "binding"
    private final String BINDING_REQUEST = "bindingRequest"

    private ServiceBindingRepository serviceBindingRepository

    private HashMap<String, Long> totalBindingRequestsPerService = new HashMap<>()
    private HashMap<String, Long> totalSuccessfulBindingRequestsPerService = new HashMap<>()
    private HashMap<String, Long> totalFailedBindingRequestsPerService = new HashMap<>()

    @Autowired
    BindingMetricsService(ServiceInstanceRepository serviceInstanceRepository, LastOperationRepository lastOperationRepository, ServiceBindingRepository serviceBindingRepository, MeterRegistry meterRegistry) {
        super(serviceInstanceRepository, lastOperationRepository)
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
    }

    void setSuccessfulBindingRequestsPerService(ServiceInstance serviceInstance) {
        def cfServiceName = getServiceName(serviceInstance)
        totalSuccessfulBindingRequestsPerService = addOrUpdateEntryOnHashMap(totalSuccessfulBindingRequestsPerService, cfServiceName)
        calculateFailedBindingRequestsPerService()
    }

    void calculateFailedBindingRequestsPerService() {
        totalBindingRequestsPerService.each { service ->
            def key = service.getKey()
            def totalValue = service.getValue()
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
        List<ServiceBinding> serviceBindingList = serviceBindingRepository.findAll()

        def totalNrOfSuccessfulBinding = retrieveMetricsForTotalNrOfSuccessfulBindings(serviceBindingList)
        metrics.add(new Metric<Long>("${BINDING}.${TOTAL}.${TOTAL}", totalNrOfSuccessfulBinding))

        def totalNrOfSuccessfulBindingsPerService = retrieveTotalNrOfSuccessfulBindingsPerService(serviceBindingList)
        metrics = addCountersFromHashMapToMetrics(totalNrOfSuccessfulBindingsPerService, totalNrOfSuccessfulBindingsPerService, metrics, BINDING, SERVICE, SUCCESS)
        metrics = addCountersFromHashMapToMetrics(totalBindingRequestsPerService, totalBindingRequestsPerService, metrics, BINDING_REQUEST, SERVICE, TOTAL)
        metrics = addCountersFromHashMapToMetrics(totalBindingRequestsPerService, totalSuccessfulBindingRequestsPerService, metrics, BINDING_REQUEST, SERVICE, SUCCESS)
        metrics = addCountersFromHashMapToMetrics(totalBindingRequestsPerService, totalFailedBindingRequestsPerService, metrics, BINDING_REQUEST, SERVICE, FAIL)

        return metrics
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
