package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.stereotype.Service

@Service
@CompileStatic
class LifecycleTimeMetrics extends ServiceBrokerMetrics {

    private final String LIFECYCLE_TIME = "lifecycleTime"
    private MeterRegistry meterRegistry

    private HashMap<String, Long> totalLifecycleTimePerService = new HashMap<>()
    private HashMap<String, Long> totalNrOfDeleteInstancesPerService = new HashMap<>()

    @Autowired
    LifecycleTimeMetrics(ServiceInstanceRepository serviceInstanceRepository, CFServiceRepository cfServiceRepository, LastOperationRepository lastOperationRepository, MeterRegistry meterRegistry) {
        super(serviceInstanceRepository, cfServiceRepository, lastOperationRepository)
        this.meterRegistry = meterRegistry
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

    double getRandomDouble() {
        addMetricsToMeterRegistry(meterRegistry)
        Math.random()
    }

    void addMetricsToMeterRegistry(MeterRegistry meterRegistry) {
        addMetricsGauge(meterRegistry, "fake", {getRandomDouble()})
        if(totalLifecycleTimePerService.size() < cfServiceRepository.findAll().size()) {
            totalLifecycleTimePerService = harmonizeServicesHashMapsWithServicesInRepository(totalLifecycleTimePerService, cfServiceRepository)
        }
        totalLifecycleTimePerService.each { entry ->
            addMetricsGauge(meterRegistry, "${LIFECYCLE_TIME}.${SERVICE}.${TOTAL}.${entry.getKey()}", {prepareMetricsForMetericsCollection().get(entry.getKey()).toDouble()})
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
