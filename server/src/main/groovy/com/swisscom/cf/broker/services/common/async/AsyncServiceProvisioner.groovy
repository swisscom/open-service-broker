package com.swisscom.cf.broker.services.common.async

import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext
import groovy.transform.CompileStatic

@CompileStatic
interface AsyncServiceProvisioner {
    AsyncOperationResult requestProvision(LastOperationJobContext context)
}