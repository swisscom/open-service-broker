package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.endpoint.PublicMetrics
import org.springframework.boot.actuate.metrics.Metric

abstract class ServiceBrokerMetrics implements PublicMetrics {

    @Autowired
    protected LastOperationRepository lastOperationRepository

    protected final String SUCCESS = "success"
    protected final String FAIL = "fail"
    protected final String TOTAL = "total"
    protected final String SERVICE = "service"
    protected final String PLAN = "plan"
    protected final String RATIO = "ratio"

    abstract Collection<Metric<?>> metrics()

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
    * provisioned unsuccessfully and is then deprovisioned, the operation in the lastOperationRepositor is changed
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