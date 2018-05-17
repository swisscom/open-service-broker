package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.stereotype.Service

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
        meterRegistry = configureInfluxMeterRegistry()
        addMetricsToMeterRegistry(meterRegistry)
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
            def service = serviceInstance.plan.service
            def serviceName = "someService"
            if (service) {
                serviceName = service.name
            }
            totalHm = addOrUpdateEntryOnHashMap(totalHm, serviceName)
        }
        log.info("${tag()} total bindings per service: ${totalHm}")
        return totalHm
    }

    public void setTotalBindingRequestsPerService(ServiceInstance serviceInstance) {
        def cfServiceName = getServiceName(serviceInstance)
        totalBindingRequestsPerService = addOrUpdateEntryOnHashMap(totalBindingRequestsPerService, cfServiceName)
    }

    public void setSuccessfulBindingRequestsPerService(ServiceInstance serviceInstance) {
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

    @Override
    void addMetricsToMeterRegistry(MeterRegistry meterRegistry) {
        List<ServiceBinding> serviceBindingList = serviceBindingRepository.findAll()

        def totalNrOfSuccessfulBinding = retrieveMetricsForTotalNrOfSuccessfulBindings(serviceBindingList)
        meterRegistry.gauge("${BINDING}.${TOTAL}.${TOTAL}", Tags.empty(), totalNrOfSuccessfulBinding)

        def totalNrOfSuccessfulBindingsPerService = retrieveTotalNrOfSuccessfulBindingsPerService(serviceBindingList)
        totalNrOfSuccessfulBindingsPerService.each { entry ->
            meterRegistry.gauge("${BINDING}.${SERVICE}.${TOTAL}.${entry.getKey()}", Tags.empty(), entry.getValue())
        }

        totalBindingRequestsPerService.each { entry ->
            meterRegistry.gauge("${BINDING_REQUEST}.${SERVICE}.${TOTAL}", Tags.empty(), entry.getValue())
        }

        totalSuccessfulBindingRequestsPerService.each { entry ->
            meterRegistry.gauge("${BINDING_REQUEST}.${SERVICE}.${SUCCESS}", Tags.empty(), entry.getValue())
        }

        totalFailedBindingRequestsPerService.each { entry ->
            meterRegistry.gauge("${BINDING_REQUEST}.${SERVICE}.${FAIL}", Tags.empty(), entry.getValue())
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
