package com.swisscom.cf.broker.services.common.async

import com.swisscom.cf.broker.async.job.JobResult
import com.swisscom.cf.broker.model.ServiceDetail
import groovy.transform.CompileStatic

@CompileStatic
class AsyncOperationResult extends JobResult {
    String internalStatus
    Collection<ServiceDetail> details = []
}
