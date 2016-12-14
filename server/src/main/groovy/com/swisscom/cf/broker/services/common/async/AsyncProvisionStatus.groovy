package com.swisscom.cf.broker.services.common.async

import com.swisscom.cf.broker.model.ServiceDetail
import groovy.transform.CompileStatic

@CompileStatic
class AsyncProvisionStatus implements AsyncOperationStatus {
    Collection<ServiceDetail> details = []
}
