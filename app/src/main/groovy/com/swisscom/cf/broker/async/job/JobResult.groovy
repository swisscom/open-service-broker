package com.swisscom.cf.broker.async.job

import com.swisscom.cf.broker.model.LastOperation
import groovy.transform.CompileStatic

@CompileStatic
class JobResult {
    String description
    LastOperation.Status status
}
