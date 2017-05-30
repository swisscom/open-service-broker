package com.swisscom.cloud.sb.broker.provisioning.async

import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import groovy.transform.CompileStatic

@CompileStatic
interface AsyncServiceProvisioner {
    AsyncOperationResult requestProvision(LastOperationJobContext context)
}