package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.stereotype.Service

import java.util.Map.Entry

@Service
@CompileStatic
@Slf4j
class BindingMetricsService extends ServiceBrokerMetrics {

    final String BINDING = "binding"
    final String BINDING_REQUEST = "bindingRequest"

    ServiceBindingRepository serviceBindingRepository
    MeterRegistry meterRegistry

    HashMap<String, Long> totalBindingRequestsPerService = new HashMap<>()
    HashMap<String, Long> totalSuccessfulBindingRequestsPerService = new HashMap<>()
    HashMap<String, Long> totalFailedBindingRequestsPerService = new HashMap<>()

    @Autowired
    BindingMetricsService(ServiceInstanceRepository serviceInstanceRepository, CFServiceRepository cfServiceRepository, LastOperationRepository lastOperationRepository, ServiceBindingRepository serviceBindingRepository, MeterRegistry meterRegistry) {
        super(serviceInstanceRepository, cfServiceRepository, lastOperationRepository)
        this.serviceBindingRepository = serviceBindingRepository
        this.meterRegistry = meterRegistry
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
            if (successValue == null) {
                successValue = 0
            }
            def failureValue = totalValue - successValue
            totalFailedBindingRequestsPerService.put(key, failureValue)
        }
    }

    double getBindingCount() {
        addMetricsToMeterRegistry(meterRegistry, serviceBindingRepository)
        retrieveMetricsForTotalNrOfSuccessfulBindings(serviceBindingRepository.findAll()).toDouble()
    }

    double getSuccessfulBindingCount(Entry<String, Long> entry) {
        def serviceBindings = serviceBindingRepository.findAll()
        if (serviceBindings.size() > 0) {
            return retrieveTotalNrOfSuccessfulBindingsPerService(serviceBindings).get(entry.getKey()).toDouble()
        }
        0.0
    }

    double getBindingRequestCountForEntryFromHashMap(HashMap<String, Long> bindingRequestsPerService, Entry<String, Long> entry) {
        if (bindingRequestsPerService.size() > 0) {
            return bindingRequestsPerService.get(entry.getKey()).toDouble()
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
                getBindingRequestCountForEntryFromHashMap(totalBindingRequestsPerService, entry)
            })
        }

        if (totalSuccessfulBindingRequestsPerService.size() < cfServiceRepository.findAll().size()) {
            totalSuccessfulBindingRequestsPerService = harmonizeServicesHashMapsWithServicesInRepository(totalSuccessfulBindingRequestsPerService, cfServiceRepository)
        }
        totalSuccessfulBindingRequestsPerService.each { entry ->
            addMetricsGauge(meterRegistry, "${BINDING_REQUEST}.${SERVICE}.${SUCCESS}.${entry.getKey()}", {
                getBindingRequestCountForEntryFromHashMap(totalSuccessfulBindingRequestsPerService, entry)
            })
        }

        if (totalFailedBindingRequestsPerService.size() < cfServiceRepository.findAll().size()) {
            totalFailedBindingRequestsPerService = harmonizeServicesHashMapsWithServicesInRepository(totalFailedBindingRequestsPerService, cfServiceRepository)
        }
        totalFailedBindingRequestsPerService.each { entry ->
            addMetricsGauge(meterRegistry, "${BINDING_REQUEST}.${SERVICE}.${FAIL}.${entry.getKey()}", {
                getBindingRequestCountForEntryFromHashMap(totalFailedBindingRequestsPerService, entry)
            })
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
