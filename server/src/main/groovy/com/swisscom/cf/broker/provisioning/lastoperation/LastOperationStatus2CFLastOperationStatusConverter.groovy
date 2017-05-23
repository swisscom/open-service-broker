package com.swisscom.cf.broker.provisioning.lastoperation

import com.swisscom.cf.broker.model.LastOperation
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@CompileStatic
@Component
class LastOperationStatus2CFLastOperationStatusConverter {
    CFLastOperationStatus convert(LastOperation.Status jobStatus) {
        switch (jobStatus) {
            case LastOperation.Status.SUCCESS:
                return CFLastOperationStatus.SUCCEEDED
            case LastOperation.Status.FAILED:
                return CFLastOperationStatus.FAILED
            case LastOperation.Status.IN_PROGRESS:
                return CFLastOperationStatus.IN_PROGRESS
            default:
                throw new RuntimeException("Unknown CFLastOperationStatus:${jobStatus.toString()}")
        }
    }
}
