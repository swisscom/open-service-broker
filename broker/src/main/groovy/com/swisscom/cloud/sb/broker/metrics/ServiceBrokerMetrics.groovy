package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.endpoint.PublicMetrics
import org.springframework.boot.actuate.metrics.Metric

@CompileStatic
@Slf4j
abstract class ServiceBrokerMetrics implements PublicMetrics {

    @Autowired
    protected ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    protected LastOperationRepository lastOperationRepository

    protected final String SUCCESS = "success"
    protected final String FAIL = "fail"
    protected final String TOTAL = "total"
    protected final String SERVICE = "service"
    protected final String PLAN = "plan"
    protected final String RATIO = "ratio"

    protected long total
    protected HashMap<String, Long> totalPerService
    protected HashMap<String, Long> totalPerPlan

    protected long totalSuccess
    protected HashMap<String, Long> totalSuccessPerService
    protected HashMap<String, Long> totalSuccessPerPlan

    protected long totalFailure
    protected HashMap<String, Long> totalFailurePerService
    protected HashMap<String, Long> totalFailurePerPlan

    abstract Collection<Metric<?>> metrics()

    void retrieveTotalMetrics() {
        def totalCounter = 0
        def successCounter = 0
        def failCounter = 0

        def it = getServiceInstanceIterator()
        while (it.hasNext()) {
            def serviceInstance = it.next()
            if(considerServiceInstance(serviceInstance)) {
                totalCounter++
                if (serviceInstance.completed) {
                    successCounter++
                } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                    // only failed provisions are counted, the ones in progress are ignored
                    failCounter++
                }
            }
        }
        total = totalCounter
        log.info("${tag()} total: ${total}")

        totalSuccess = successCounter
        log.info("${tag()} total success: ${totalSuccess}")

        totalFailure = failCounter
        log.info("${tag()} total failure: ${totalFailure}")
    }

    void retrieveTotalMetricsPerService() {
        HashMap<String, Long> totalHm = new HashMap<>()
        HashMap<String, Long> successHm = new HashMap<>()
        HashMap<String, Long> failHm = new HashMap<>()

        def it = getServiceInstanceIterator()
        while (it.hasNext()) {
            def serviceInstance = it.next()
            //for services where no service is set the name cannot be deduced
            def serviceName = "someService"
            if(serviceInstance.plan.service) {
                serviceName = serviceInstance.plan.service.name
            }
            if (considerServiceInstance(serviceInstance)) {
                totalHm = addEntryToHm(totalHm, serviceName)
                if (serviceInstance.completed) {
                    successHm = addEntryToHm(successHm, serviceName)
                } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                    // only failed provisions are counted, the ones in progress are ignored
                    failHm = addEntryToHm(failHm, serviceName)
                }
            }
        }
        totalPerService = totalHm
        log.info("${tag()} total service: ${totalPerService}")

        totalSuccessPerService = successHm
        log.info("${tag()} total success service: ${totalSuccessPerService}")

        totalFailurePerService = failHm
        log.info("${tag()} total failure service: ${totalFailurePerService}")
    }

    void retrieveTotalMetricsPerPlan() {
        HashMap<String, Long> totalHm = new HashMap<>()
        HashMap<String, Long> successHm = new HashMap<>()
        HashMap<String, Long> failHm = new HashMap<>()

        def it = getServiceInstanceIterator()
        while (it.hasNext()) {
            def serviceInstance = it.next()
            def planName = serviceInstance.plan.name
            if (considerServiceInstance(serviceInstance)) {
                totalHm = addEntryToHm(totalHm, planName)
                if (serviceInstance.completed) {
                    successHm = addEntryToHm(successHm, planName)
                } else if (!checkIfNotCompletedProvisionIsInProgress(serviceInstance.guid)) {
                    // only failed provisions are counted, the ones in progress are ignored
                    failHm = addEntryToHm(failHm, planName)
                }
            }
        }
        totalPerPlan = totalHm
        log.info("${tag()} total: ${totalPerPlan}")

        totalSuccessPerPlan = successHm
        log.info("${tag()} total success plan: ${totalSuccessPerPlan}")

        totalFailurePerPlan = failHm
        log.info("${tag()} total failure plan: ${totalFailurePerPlan}")
    }

    ListIterator<ServiceInstance> getServiceInstanceIterator() {
        def allServiceInstances = serviceInstanceRepository.findAll()
        return allServiceInstances.listIterator()
    }

    abstract boolean considerServiceInstance(ServiceInstance serviceInstance)

    abstract String tag()

    HashMap<String, Long> addEntryToHm(HashMap<String, Long> hm, String key) {
        if (hm.get(key) == null) {
            hm.put(key, 1L)
        } else {
            def currentCounter = hm.get(key)
            def newCounter = currentCounter + 1
            hm.put(key, newCounter)
        }
        return hm
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
        return 100 / total * amount
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