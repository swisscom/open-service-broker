package com.swisscom.cf.broker.services.common

import com.swisscom.cf.broker.model.ServiceDetail
import groovy.transform.CompileStatic

@CompileStatic
class AsyncProvisionStatus implements AsyncOperationStatus {
    Collection<ServiceDetail> details = []
}
