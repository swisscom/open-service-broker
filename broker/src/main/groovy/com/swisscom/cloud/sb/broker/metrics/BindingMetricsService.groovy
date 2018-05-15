package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.stereotype.Service

@Service
@CompileStatic
@Slf4j
class BindingMetricsService extends ServiceBrokerMetrics {

    @Autowired
    private ServiceBindingRepository serviceBindingRepository

    private final String BINDING = "binding"
    private final String BINDING_REQUEST = "bindingRequest"


    private long totalSuccessfulNrOfBindings
    private HashMap<String, Long> totalSuccessfulBindingsPerService
    private HashMap<String, Long> totalBindingRequestsPerService = new HashMap<>()
    private HashMap<String, Long> totalSuccessfulBindingRequestsPerService = new HashMap<>()
    private HashMap<String, Long> totalFailedBindingRequestsPerService = new HashMap<>()

    void retrieveMetricsForTotalNrOfBindings() {
        def it = getServiceBindingIterator()
        totalSuccessfulNrOfBindings = it.size()

        log.info("Total nr of provision requests: ${totalSuccessfulNrOfBindings}")
    }

    void retrieveTotalSuccessfulBindingsPerService() {
        HashMap<String, Long> totalHm = new HashMap<>()

        def it = getServiceBindingIterator()
        while (it.hasNext()) {
            def serviceBinding = it.next()
            def serviceInstance = serviceBinding.serviceInstance
            def service = serviceInstance.plan.service
            def serviceName = "someService"
            if (service) {
                serviceName = service.name
            }
            totalHm = addEntryToHm(totalHm, serviceName)
        }
        totalSuccessfulBindingsPerService = totalHm
        log.info("${tag()} total bindings per service: ${totalSuccessfulBindingsPerService}")
    }

    ListIterator<ServiceBinding> getServiceBindingIterator() {
        def allServiceBindings = serviceBindingRepository.findAll()
        return allServiceBindings.listIterator()
    }

    public void setTotalBindingRequestsPerService(ServiceInstance serviceInstance) {
        def cfServiceName = getServiceName(serviceInstance)
        totalBindingRequestsPerService = addEntryToHm(totalBindingRequestsPerService, cfServiceName)
    }

    public void setSuccessfulBindingRequestsPerService(ServiceInstance serviceInstance) {
        def cfServiceName = getServiceName(serviceInstance)
        totalSuccessfulBindingRequestsPerService = addEntryToHm(totalSuccessfulBindingRequestsPerService, cfServiceName)
        calculateFailedBindingRequestsPerService()
    }

    void calculateFailedBindingRequestsPerService() {
        def it = totalBindingRequestsPerService.iterator()
        while (it.hasNext()) {
            def next = (Map.Entry<String, Long>) it.next()
            def key = next.getKey()
            def totalValue = next.getValue()
            def successValue = totalSuccessfulBindingRequestsPerService.get(key)
            def failureValue = totalValue - successValue
            totalFailedBindingRequestsPerService.put(key, failureValue)
        }
    }

    @Override
    Collection<Metric<?>> metrics() {
        List<Metric<?>> metrics = new ArrayList<>()

        retrieveMetricsForTotalNrOfBindings()
        metrics.add(new Metric<Long>("${BINDING}.${TOTAL}.${TOTAL}", totalSuccessfulNrOfBindings))

        retrieveTotalSuccessfulBindingsPerService()
        metrics = addCountersFromHashMapToMetrics(totalBindingRequestsPerService, totalBindingRequestsPerService, metrics, BINDING_REQUEST, SERVICE, TOTAL)
        metrics = addCountersFromHashMapToMetrics(totalBindingRequestsPerService, totalSuccessfulBindingRequestsPerService, metrics, BINDING_REQUEST, SERVICE, SUCCESS)
        metrics = addCountersFromHashMapToMetrics(totalBindingRequestsPerService, totalFailedBindingRequestsPerService, metrics, BINDING_REQUEST, SERVICE, FAIL)
        metrics = addCountersFromHashMapToMetrics(totalSuccessfulBindingsPerService, totalSuccessfulBindingsPerService, metrics, BINDING, SERVICE, SUCCESS)

        return metrics
    }

    @Override
    boolean considerServiceInstance(ServiceInstance serviceInstance) {
        // TODO
        return false
    }

    @Override
    String tag() {
        return BindingMetricsService.class.getSimpleName()
    }
}
